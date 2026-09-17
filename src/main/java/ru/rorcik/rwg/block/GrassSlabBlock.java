package ru.rorcik.rwg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import ru.rorcik.rwg.RwgBlocks;

public class GrassSlabBlock extends SlabBlock {
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassSlabBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(SNOWY, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SNOWY);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean snowy = level.getBlockState(pos.above()).is(Blocks.SNOW_BLOCK) ||
                level.getBlockState(pos.above()).is(Blocks.SNOW);
        return state.setValue(SNOWY, snowy);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            boolean shouldBeSnowy = neighborState.is(Blocks.SNOW) || neighborState.is(Blocks.SNOW_BLOCK);
            if (state.getValue(SNOWY) != shouldBeSnowy) {
                return state.setValue(SNOWY, shouldBeSnowy);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide) return;

        SlabType currentType = state.getValue(TYPE);
        boolean isSnowy = state.getValue(SNOWY);
        boolean shouldBeSnowy;
        if (currentType == SlabType.DOUBLE) {
            shouldBeSnowy = hasSnowAbove(level, pos);
        } else {
            shouldBeSnowy = isSnowyBiome(level, pos) || hasSnowAbove(level, pos);
        }
        if (shouldBeSnowy != isSnowy) {
            state = state.setValue(SNOWY, shouldBeSnowy);
            level.setBlock(pos, state, 3);
        }

        if (!canBeGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, RwgBlocks.DIRT_SLAB.get().defaultBlockState()
                    .setValue(TYPE, state.getValue(TYPE))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED)));
        } else if (level.getMaxLocalRawBrightness(pos.above()) >= 9) {
            BlockState grassState = this.defaultBlockState()
                    .setValue(TYPE, state.getValue(TYPE))
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                    .setValue(SNOWY, state.getValue(SNOWY));

            for (int i = 0; i < 4; ++i) {
                BlockPos targetPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(RwgBlocks.DIRT_SLAB.get()) && canPropagate(grassState, level, targetPos)) {
                    boolean snowy = isSnowyBiome(level, targetPos) || hasSnowAbove(level, targetPos);
                    level.setBlockAndUpdate(targetPos, grassState
                            .setValue(TYPE, targetState.getValue(TYPE))
                            .setValue(WATERLOGGED, targetState.getValue(WATERLOGGED))
                            .setValue(SNOWY, snowy));
                }
            }
        }
    }

    private boolean hasSnowAbove(Level level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        return above.is(Blocks.SNOW_BLOCK) || above.is(Blocks.SNOW);
    }

    private boolean isSnowyBiome(Level level, BlockPos pos) {
        var biome = level.getBiome(pos);
        return biome.value().coldEnoughToSnow(pos);
    }

    private boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        if (aboveState.is(Blocks.SNOW)) {
            return true;
        } else if (aboveState.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int light = level.getMaxLocalRawBrightness(abovePos);
            return light >= 9;
        }
    }

    private boolean canPropagate(BlockState state, LevelReader level, BlockPos pos) {
        return canBeGrass(state, level, pos) && !level.getBlockState(pos.above()).is(Blocks.SNOW);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (!ToolActions.SHOVEL_FLATTEN.equals(toolAction)) {
            return null;
        }

        SlabType slabType = state.getValue(TYPE);
        if (slabType == SlabType.DOUBLE) {
            return null;
        }

        ItemStack heldItem = context.getItemInHand();
        if (heldItem.isEmpty()) {
            return null;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Level level = context.getLevel();

        BlockState aboveState = level.getBlockState(clickedPos.above());
        if (aboveState.isAir() || aboveState.is(Blocks.SNOW) || clickedFace == Direction.UP) {
            if (!simulate) {
                level.playSound(context.getPlayer(), clickedPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return RwgBlocks.DIRT_PATH_SLAB.get().defaultBlockState()
                    .setValue(TYPE, slabType)
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
        }

        return null;
    }
}