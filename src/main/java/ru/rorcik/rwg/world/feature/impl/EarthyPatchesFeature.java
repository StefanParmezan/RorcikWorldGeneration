package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
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
    private static final float DRIPSTONE_CHANCE = 0.25f;

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

        if (!isValidSurface(topState)) return false;

        boolean mountainBiome = isMountainBiome(level, surfacePos);

        Block patchBlock = mountainBiome
                ? pickStonePatchBlock(random)
                : pickEarthyPatchBlock(random);

        int radius = 1;
        int placed = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;

                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos pos = SurfaceFinder.findSurface(level, x, z);
                BlockState state = level.getBlockState(pos);

                if (!isValidSurface(state)) continue;

                level.setBlock(pos, patchBlock.defaultBlockState(), 3);
                placed++;

                if (!RwgConfig.enableEarthyPatchDecorations) continue;

                BlockPos abovePos = pos.above();
                if (!level.getBlockState(abovePos).isAir()) continue;

                if (!mountainBiome) {
                    decorateEarthy(level, abovePos, patchBlock, random);
                } else {
                    decorateStone(level, abovePos, patchBlock, random);
                }
            }
        }

        return placed > 0;
    }

    private void decorateEarthy(WorldGenLevel level, BlockPos abovePos,
                                Block patchBlock, RandomSource random) {
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

    private void decorateStone(WorldGenLevel level, BlockPos abovePos,
                               Block patchBlock, RandomSource random) {
        if (patchBlock == Blocks.DRIPSTONE_BLOCK
                && random.nextFloat() < DRIPSTONE_CHANCE) {
            placeStalagmite(level, abovePos, random);
        }
    }

    private boolean isValidSurface(BlockState s) {
        return s.is(Blocks.GRASS_BLOCK)
                || s.is(Blocks.DIRT)
                || s.is(Blocks.COARSE_DIRT)
                || s.is(Blocks.PODZOL)
                || s.is(Blocks.STONE)
                || s.is(Blocks.DEEPSLATE)
                || s.is(Blocks.GRANITE)
                || s.is(Blocks.DIORITE)
                || s.is(Blocks.ANDESITE)
                || s.is(Blocks.TUFF)
                || s.is(Blocks.CALCITE)
                || s.is(Blocks.GRAVEL);
    }

    private boolean isMountainBiome(WorldGenLevel level, BlockPos pos) {
        String name = level.getBiome(pos).unwrapKey()
                .map(k -> k.location().toString())
                .orElse("");

        return name.contains("stony")
                || name.contains("jagged_peaks")
                || name.contains("frozen_peaks")
                || name.contains("snowy_slopes")
                || name.contains("grove")
                || name.contains("peaks")
                || name.contains("slopes")
                || name.contains("cliffs");
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

    private void placeStalagmite(WorldGenLevel level, BlockPos basePos, RandomSource random) {
        int height = 1 + random.nextInt(3);
        for (int i = 0; i < height; i++) {
            BlockPos pos = basePos.above(i);
            if (!level.getBlockState(pos).isAir()) break;

            boolean isTip = (i == height - 1);
            BlockState state = Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP)
                    .setValue(PointedDripstoneBlock.THICKNESS,
                            isTip ? DripstoneThickness.TIP : DripstoneThickness.FRUSTUM);
            level.setBlock(pos, state, 3);
        }
    }

    private Block pickEarthyPatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 30) return Blocks.PODZOL;
        if (roll < 55) return Blocks.COARSE_DIRT;
        if (roll < 80) return Blocks.MOSS_BLOCK;
        return Blocks.MOSSY_COBBLESTONE;
    }

    private Block pickStonePatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 30) return Blocks.GRANITE;
        if (roll < 55) return Blocks.DIORITE;
        if (roll < 80) return Blocks.ANDESITE;
        if (roll < 92) return Blocks.TUFF;
        return Blocks.DRIPSTONE_BLOCK;
    }
}