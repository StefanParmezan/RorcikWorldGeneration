package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RiverVegetationFeature extends Feature<NoneFeatureConfiguration> {

    public RiverVegetationFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos waterPos = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        BlockState waterState = level.getBlockState(waterPos);

        boolean placed = false;

        if (waterState.is(Blocks.WATER)) {
            BlockPos groundPos = waterPos.below();
            BlockState groundState = level.getBlockState(groundPos);

            if (groundState.is(Blocks.DIRT) || groundState.is(Blocks.SAND) ||
                    groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.MUD) ||
                    groundState.is(Blocks.CLAY)) {

                if (random.nextFloat() < 0.6f) {
                    level.setBlock(waterPos, Blocks.LILY_PAD.defaultBlockState(), 3);
                    placed = true;
                }
            }
        }

        BlockPos groundPos = waterPos.below();
        BlockState groundState = level.getBlockState(groundPos);

        if (groundState.is(Blocks.DIRT) || groundState.is(Blocks.SAND) ||
                groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.MUD)) {

            boolean hasWaterNearby = false;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos checkPos = groundPos.offset(dx, 0, dz);
                    if (level.getBlockState(checkPos).is(Blocks.WATER) ||
                            level.getBlockState(checkPos.below()).is(Blocks.WATER)) {
                        hasWaterNearby = true;
                        break;
                    }
                }
                if (hasWaterNearby) break;
            }

            if (hasWaterNearby && random.nextFloat() < 0.4f) {
                int height = 1 + random.nextInt(3);
                for (int h = 0; h < height; h++) {
                    BlockPos canePos = groundPos.above(h + 1);
                    if (level.getBlockState(canePos).canBeReplaced()) {
                        level.setBlock(canePos, Blocks.SUGAR_CANE.defaultBlockState(), 3);
                    } else {
                        break;
                    }
                }
                placed = true;
            }
        }

        return placed;
    }
}