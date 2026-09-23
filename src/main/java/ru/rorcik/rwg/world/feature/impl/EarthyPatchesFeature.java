package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

public class EarthyPatchesFeature extends Feature<NoneFeatureConfiguration> {
    public EarthyPatchesFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        BlockState topState = level.getBlockState(surfacePos);

        if (!topState.is(Blocks.GRASS_BLOCK) && !topState.is(Blocks.DIRT)) return false;

        Block patchBlock = pickPatchBlock(random);
        int radius = 1;
        int placed = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos pos = SurfaceFinder.findSurface(level, x, z);
                BlockState state = level.getBlockState(pos);

                if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
                    level.setBlock(pos, patchBlock.defaultBlockState(), 3);
                    placed++;
                }
            }
        }
        return placed > 0;
    }

    private Block pickPatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 25) return Blocks.PODZOL;
        if (roll < 50) return Blocks.COARSE_DIRT;
        if (roll < 75) return Blocks.MOSS_BLOCK;
        return Blocks.MOSSY_COBBLESTONE;
    }
}