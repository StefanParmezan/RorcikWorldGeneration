package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

public class MossCarpetFeature extends Feature<NoneFeatureConfiguration> {

    public MossCarpetFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        BlockState topState = level.getBlockState(surfacePos);

        if (!isCarpetableSurface(topState)) return false;
        if (!SurfaceFinder.isAirOrReplaceable(level, surfacePos.above())) return false;

        if (random.nextFloat() < 0.18f) {
            level.setBlock(surfacePos.above(), Blocks.MOSS_CARPET.defaultBlockState(), 3);
            return true;
        }

        return false;
    }

    private boolean isCarpetableSurface(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.PODZOL) || state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.STONE) || state.is(Blocks.MOSSY_COBBLESTONE) ||
                state.is(Blocks.COBBLESTONE);
    }
}