package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

public class EarthyPatchesFeature extends Feature<NoneFeatureConfiguration> {

    private static final float DEAD_BUSH_CHANCE = 0.05f;

    private static final float FERN_CHANCE = 0.22f;

    private static final float LARGE_FERN_RATIO = 0.40f;

    private static final float MOSS_CHANCE = 0.35f;

    public EarthyPatchesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

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

                if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT)) continue;

                level.setBlock(pos, patchBlock.defaultBlockState(), 3);
                placed++;

                if (!RwgConfig.enableEarthyPatchDecorations) continue;

                BlockPos abovePos = pos.above();
                if (!level.getBlockState(abovePos).isAir()) continue;

                if (patchBlock == Blocks.PODZOL
                        && RwgConfig.enableDeadBushOnPodzol
                        && random.nextFloat() < DEAD_BUSH_CHANCE) {
                    level.setBlock(abovePos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
                }

                else if (patchBlock == Blocks.COARSE_DIRT
                        && RwgConfig.enableFernOnCoarseDirt
                        && random.nextFloat() < FERN_CHANCE) {
                    placeFern(level, abovePos, random);
                }

                else if (patchBlock == Blocks.MOSSY_COBBLESTONE
                        && RwgConfig.enableMossOnMossyCobblestone
                        && random.nextFloat() < MOSS_CHANCE) {
                    level.setBlock(abovePos, Blocks.MOSS_CARPET.defaultBlockState(), 3);
                }
            }
        }

        return placed > 0;
    }

    private void placeFern(WorldGenLevel level, BlockPos lowerPos, RandomSource random) {
        if (random.nextFloat() < LARGE_FERN_RATIO) {
            BlockPos upperPos = lowerPos.above();
            if (level.getBlockState(upperPos).isAir()) {
                level.setBlock(lowerPos, Blocks.LARGE_FERN.defaultBlockState()
                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 3);
                level.setBlock(upperPos, Blocks.LARGE_FERN.defaultBlockState()
                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 3);
                return;
            }
        }
        level.setBlock(lowerPos, Blocks.FERN.defaultBlockState(), 3);
    }

    private Block pickPatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 25) return Blocks.PODZOL;
        if (roll < 50) return Blocks.COARSE_DIRT;
        if (roll < 75) return Blocks.MOSS_BLOCK;
        return Blocks.MOSSY_COBBLESTONE;
    }
}