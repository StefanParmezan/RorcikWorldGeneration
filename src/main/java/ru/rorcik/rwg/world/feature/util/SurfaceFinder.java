package ru.rorcik.rwg.world.feature.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceFinder {

    public static int findSurfaceY(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
    }

    public static BlockPos findSurface(WorldGenLevel level, int x, int z) {
        return new BlockPos(x, findSurfaceY(level, x, z), z);
    }

    public static boolean isSolidGround(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolid() && !state.is(Blocks.BEDROCK);
    }

    public static boolean isAirOrReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}