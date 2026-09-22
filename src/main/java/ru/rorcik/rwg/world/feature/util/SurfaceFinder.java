package ru.rorcik.rwg.world.feature.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceFinder {

    public static int findSurfaceY(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
    }

    public static BlockPos findSurface(WorldGenLevel level, int x, int z) {
        int y = findSurfaceY(level, x, z);
        return new BlockPos(x, y, z);
    }

    public static boolean isSolidGround(WorldGenLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.isSolid() && !below.is(Blocks.BEDROCK);
    }

    public static boolean isAirOrReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}