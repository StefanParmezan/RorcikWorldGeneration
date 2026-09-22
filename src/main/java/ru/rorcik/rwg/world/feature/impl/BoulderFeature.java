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

public class BoulderFeature extends Feature<NoneFeatureConfiguration> {

    public BoulderFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        if (!SurfaceFinder.isSolidGround(level, surfacePos)) return false;

        int radius = 2 + random.nextInt(2);

        Block stoneType = pickStoneType(random);

        placeBoulder(level, surfacePos, stoneType, radius, random);
        return true;
    }

    private Block pickStoneType(RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 3) return Blocks.STONE;
        if (roll < 5) return Blocks.ANDESITE;
        if (roll < 7) return Blocks.TUFF;
        if (roll < 9) return Blocks.MOSSY_COBBLESTONE;
        return Blocks.GRANITE;
    }

    private void placeBoulder(WorldGenLevel level, BlockPos center, Block blockType, int radius, RandomSource random) {
        double centerY = center.getY() - radius * 0.3;
        double radiusY = radius * 1.2;

        for (int dy = -radius - 1; dy <= radius + 2; dy++) {
            double distY = (center.getY() + dy - centerY) / radiusY;
            double distY2 = distY * distY;

            for (int dx = -radius; dx <= radius; dx++) {
                double distX = dx / (double) radius;
                double distX2 = distX * distX;

                if (distX2 + distY2 > 1.0) continue;

                for (int dz = -radius; dz <= radius; dz++) {
                    double distZ = dz / (double) radius;
                    if (distX2 + distY2 + distZ * distZ > 1.0) continue;

                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState existing = level.getBlockState(pos);

                    if (isReplaceable(existing) || (existing.isAir() && pos.getY() >= center.getY())) {
                        BlockState finalState = blockType.defaultBlockState();

                        if (blockType == Blocks.STONE && random.nextFloat() < 0.15f) {
                            finalState = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        }

                        level.setBlock(pos, finalState, 3);
                    }
                }
            }
        }
    }

    private boolean isReplaceable(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.STONE) || state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.GRAVEL) || state.is(Blocks.SAND);
    }
}