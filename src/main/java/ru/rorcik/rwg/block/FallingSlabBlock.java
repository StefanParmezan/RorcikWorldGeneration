package ru.rorcik.rwg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ru.rorcik.rwg.RwgMod.MODID)
public class FallingSlabBlock extends SlabBlock {

    public FallingSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            checkFallable(state, level, pos);
            tryMergeWithAdjacent(state, level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            checkFallable(state, level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        checkFallable(state, level, pos);
    }

    private void checkFallable(BlockState state, Level level, BlockPos pos) {
        BlockPos checkPos = pos.below();
        if (isFree(level.getBlockState(checkPos)) && pos.getY() >= level.getMinBuildHeight()) {
            SlabType type = state.getValue(TYPE);
            BlockState fallingState = state;

            if (type == SlabType.TOP) {
                fallingState = state.setValue(TYPE, SlabType.BOTTOM);
            }

            FallingBlockEntity.fall(level, pos, fallingState);
        }
    }

    private void tryMergeWithAdjacent(BlockState state, Level level, BlockPos pos) {
        SlabType currentType = state.getValue(TYPE);

        if (currentType == SlabType.BOTTOM) {
            BlockState aboveState = level.getBlockState(pos.above());
            if (aboveState.getBlock() == this && aboveState.getValue(TYPE) == SlabType.TOP) {
                level.setBlock(pos, state.setValue(TYPE, SlabType.DOUBLE), 3);
                level.removeBlock(pos.above(), false);
            }
        } else if (currentType == SlabType.TOP) {
            BlockState belowState = level.getBlockState(pos.below());
            if (belowState.getBlock() == this && belowState.getValue(TYPE) == SlabType.BOTTOM) {
                level.setBlock(pos.below(), belowState.setValue(TYPE, SlabType.DOUBLE), 3);
                level.removeBlock(pos, false);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof FallingBlockEntity) {
            BlockState placedState = event.getPlacedBlock();
            if (placedState.getBlock() instanceof FallingSlabBlock) {
                Level level = (Level) event.getLevel();
                BlockPos pos = event.getPos();
                ((FallingSlabBlock) placedState.getBlock()).tryMergeWithAdjacent(placedState, level, pos);
            }
        }
    }

    public static boolean isFree(BlockState state) {
        return state.isAir() || state.is(Blocks.FIRE) || state.is(Blocks.WATER) || state.is(Blocks.LAVA) || state.getPistonPushReaction() == PushReaction.DESTROY;
    }
}