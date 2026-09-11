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
            SlabType currentType = state.getValue(TYPE);

            if (currentType == SlabType.DOUBLE) {
                boolean shouldBeSnowy = neighborState.is(Blocks.SNOW) || neighborState.is(Blocks.SNOW_BLOCK);
                if (state.getValue(SNOWY) != shouldBeSnowy) {
                    return state.setValue(SNOWY, shouldBeSnowy);
                }
            } else {
                boolean shouldBeSnowy = neighborState.is(Blocks.SNOW) || neighborState.is(Blocks.SNOW_BLOCK);
                if (state.getValue(SNOWY) != shouldBeSnowy) {
                    return state.setValue(SNOWY, shouldBeSnowy);
                }
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide()) {
            SlabType currentType = state.getValue(TYPE);
            boolean isSnowy = state.getValue(SNOWY);
            boolean shouldBeSnowy;

            if (currentType == SlabType.DOUBLE) {
                shouldBeSnowy = hasSnowAbove(level, pos);
            } else {
                shouldBeSnowy = isSnowyBiome(level, pos) || hasSnowAbove(level, pos);
            }

            if (shouldBeSnowy && !isSnowy) {
                level.setBlock(pos, state.setValue(SNOWY, Boolean.TRUE), 3);
            } else if (!shouldBeSnowy && isSnowy) {
                level.setBlock(pos, state.setValue(SNOWY, Boolean.FALSE), 3);
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