package ru.rorcik.rwg.world.feature.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class DoublePlantHelper {

    private DoublePlantHelper() {
    }

    public static boolean isHalf(BlockState state) {
        return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
    }

    public static void clearWholePlant(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!isHalf(state)) return;

        DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
        BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPos);

        if (otherState.getBlock() == state.getBlock()
                && isHalf(otherState)
                && otherState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != half) {
            level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 3);
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
