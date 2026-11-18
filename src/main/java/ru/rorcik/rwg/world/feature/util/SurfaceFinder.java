package ru.rorcik.rwg.world.feature.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceFinder {

    /**
     * Returns the highest dry, solid ground block in the column. Submerged
     * columns return {@link Integer#MIN_VALUE} instead of the water surface,
     * so land features cannot be placed on top of lakes/oceans.
     */
    public static int findSurfaceY(WorldGenLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        int minY = level.getMinBuildHeight();

        for (int i = 0; i < 32 && y >= minY; i++, y--) {
            BlockState state = level.getBlockState(new BlockPos(x, y, z));

            // If we hit a fluid before a solid block, this column is submerged.
            if (!state.getFluidState().isEmpty()) return Integer.MIN_VALUE;

            if (state.isSolid() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)) {
                return y;
            }
        }

        return Integer.MIN_VALUE;
    }

    public static BlockPos findSurface(WorldGenLevel level, int x, int z) {
        int y = findSurfaceY(level, x, z);
        return new BlockPos(x, y == Integer.MIN_VALUE ? level.getMinBuildHeight() : y, z);
    }

    public static boolean isSolidGround(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolid()
                && !state.is(Blocks.BEDROCK)
                && state.getFluidState().isEmpty()
                && !state.is(BlockTags.LEAVES)
                && !state.is(BlockTags.LOGS);
    }

    public static boolean isAirOrReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
