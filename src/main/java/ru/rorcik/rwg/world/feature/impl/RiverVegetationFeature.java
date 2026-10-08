package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RiverVegetationFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SEARCH_RADIUS = 2;

    private static final float KELP_CHANCE = 0.45f;
    private static final float SEAGRASS_CHANCE = 0.55f;
    private static final float LILY_PAD_CHANCE = 0.35f;
    private static final int KELP_MAX_HEIGHT = 8;

    public RiverVegetationFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        boolean placedAnything = false;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                if (dx * dx + dz * dz > SEARCH_RADIUS * SEARCH_RADIUS) continue;

                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;

                int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, surfaceY, z);

                while (cursor.getY() > level.getMinBuildHeight() && level.getBlockState(cursor).isAir()) {
                    cursor.move(Direction.DOWN);
                }

                if (level.getBlockState(cursor).is(Blocks.WATER)) {
                    BlockPos bottomWaterPos = cursor.immutable();

                    while (cursor.getY() > level.getMinBuildHeight() && level.getBlockState(cursor).is(Blocks.WATER)) {
                        bottomWaterPos = cursor.immutable();
                        cursor.move(Direction.DOWN);
                    }

                    BlockState groundState = level.getBlockState(cursor);
                    if (!isValidUnderwaterGround(groundState)) continue;

                    int waterDepth = 0;
                    BlockPos.MutableBlockPos depthCursor = new BlockPos.MutableBlockPos(bottomWaterPos.getX(), bottomWaterPos.getY(), bottomWaterPos.getZ());
                    while (level.getBlockState(depthCursor).is(Blocks.WATER) && waterDepth < KELP_MAX_HEIGHT + 2) {
                        waterDepth++;
                        depthCursor.move(Direction.UP);
                    }
                    BlockPos waterSurface = depthCursor.below();

                    if (waterDepth >= 2 && random.nextFloat() < KELP_CHANCE) {
                        placeKelp(level, bottomWaterPos, waterDepth, random);
                        placedAnything = true;
                    }
                    else if (random.nextFloat() < SEAGRASS_CHANCE) {
                        placeSeagrass(level, bottomWaterPos, waterDepth, random);
                        placedAnything = true;
                    }

                    if (waterDepth <= 2 && random.nextFloat() < LILY_PAD_CHANCE) {
                        BlockPos aboveWater = waterSurface.above();
                        if (level.getBlockState(aboveWater).isAir()) {
                            level.setBlock(aboveWater, Blocks.LILY_PAD.defaultBlockState(), 3);
                            placedAnything = true;
                        }
                    }
                }
            }
        }
        return placedAnything;
    }

    private boolean isValidUnderwaterGround(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.SAND) ||
                state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MUD) ||
                state.is(Blocks.CLAY) || state.is(Blocks.GRAVEL) ||
                state.is(Blocks.COARSE_DIRT);
    }

    private void placeKelp(WorldGenLevel level, BlockPos bottomWaterPos, int waterDepth, RandomSource random) {
        int maxPossibleHeight = Math.min(waterDepth, KELP_MAX_HEIGHT);

        int kelpHeight = 1 + Math.min(random.nextInt(maxPossibleHeight), random.nextInt(maxPossibleHeight));

        BlockPos current = bottomWaterPos;

        for (int i = 0; i < kelpHeight; i++) {
            if (!level.getBlockState(current).is(Blocks.WATER)) break;

            boolean isTop = (i == kelpHeight - 1);
            BlockState kelpState = isTop
                    ? Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, random.nextInt(25))
                    : Blocks.KELP_PLANT.defaultBlockState();

            level.setBlock(current, kelpState, 3);
            current = current.above();
        }
    }

    private void placeSeagrass(WorldGenLevel level, BlockPos bottomWaterPos, int waterDepth, RandomSource random) {
        if (waterDepth >= 2 && random.nextFloat() < 0.3f) {
            BlockPos above = bottomWaterPos.above();
            if (level.getBlockState(above).is(Blocks.WATER)) {
                level.setBlock(bottomWaterPos,
                        Blocks.TALL_SEAGRASS.defaultBlockState()
                                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 3);
                level.setBlock(above,
                        Blocks.TALL_SEAGRASS.defaultBlockState()
                                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 3);
                return;
            }
        }
        level.setBlock(bottomWaterPos, Blocks.SEAGRASS.defaultBlockState(), 3);
    }
}