package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

public class BoulderFeature extends Feature<NoneFeatureConfiguration> {

    private static final float ORE_CHANCE = 0.02f;
    private static final float GILDED_CHANCE = 0.03f;

    public BoulderFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public enum BoulderType {
        STONE, BLACKSTONE, DEEPSLATE, CALCITE
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        if (!SurfaceFinder.isSolidGround(level, surfacePos)) return false;

        int radius = 2 + random.nextInt(2);

        BoulderType type = pickBoulderType(random);

        placeBoulder(level, surfacePos, type, radius, random);
        smoothEdges(level, surfacePos, radius, pickSlabFor(type));

        return true;
    }

    private BoulderType pickBoulderType(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 55) return BoulderType.STONE;
        if (roll < 75) return BoulderType.DEEPSLATE;
        if (roll < 88) return BoulderType.CALCITE;
        return BoulderType.BLACKSTONE;
    }

    private Block pickSlabFor(BoulderType type) {
        return switch (type) {
            case BLACKSTONE -> Blocks.BLACKSTONE_SLAB;
            case DEEPSLATE  -> Blocks.COBBLED_DEEPSLATE_SLAB;
            case CALCITE    -> Blocks.STONE_SLAB;
            default         -> Blocks.STONE_SLAB;
        };
    }

    private void placeBoulder(WorldGenLevel level, BlockPos center, BoulderType type,
                              int radius, RandomSource random) {
        int r = radius;

        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int dist2 = dx * dx + dy * dy + dz * dz;
                    double noise = (random.nextDouble() - 0.5) * 1.2;
                    double effectiveR = r + noise;
                    if (dist2 > effectiveR * effectiveR) continue;

                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState existing = level.getBlockState(pos);
                    if (!canBoulderOverride(existing, dy)) continue;

                    BlockState toPlace = pickBlockForPosition(type, dy, random);
                    level.setBlock(pos, toPlace, 3);
                }
            }
        }
    }

    private BlockState pickBlockForPosition(BoulderType type, int dy, RandomSource random) {
        switch (type) {
            case BLACKSTONE -> {
                if (dy <= 0 && random.nextFloat() < GILDED_CHANCE) {
                    return Blocks.GILDED_BLACKSTONE.defaultBlockState();
                }
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            case DEEPSLATE -> {
                if (random.nextFloat() < ORE_CHANCE) {
                    return (random.nextBoolean()
                            ? Blocks.DEEPSLATE_COPPER_ORE
                            : Blocks.DEEPSLATE_COAL_ORE).defaultBlockState();
                }
                return Blocks.DEEPSLATE.defaultBlockState();
            }
            case CALCITE -> {
                if (random.nextFloat() < ORE_CHANCE) {
                    return (random.nextBoolean()
                            ? Blocks.COPPER_ORE
                            : Blocks.COAL_ORE).defaultBlockState();
                }
                return Blocks.CALCITE.defaultBlockState();
            }
            default -> {  // STONE
                if (random.nextFloat() < ORE_CHANCE) {
                    return (random.nextBoolean()
                            ? Blocks.COPPER_ORE
                            : Blocks.COAL_ORE).defaultBlockState();
                }
                if (random.nextFloat() < 0.08f) {
                    return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                }
                return Blocks.STONE.defaultBlockState();
            }
        }
    }

    private boolean canBoulderOverride(BlockState state, int dy) {
        if (!state.getFluidState().isEmpty()) return false;
        if (state.is(BlockTags.LOGS)) return false;
        if (state.is(BlockTags.LEAVES)) return false;
        if (state.is(BlockTags.FLOWERS)) return false;
        if (state.is(BlockTags.SAPLINGS)) return false;
        if (state.is(Blocks.BEDROCK)) return false;
        if (state.is(Blocks.OBSIDIAN)) return false;
        if (state.is(Blocks.CRYING_OBSIDIAN)) return false;

        if (state.isAir()) return dy >= 0;
        return true;
    }

    private boolean isBoulderBlock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.TUFF) || state.is(Blocks.GRANITE)
                || state.is(Blocks.MOSSY_COBBLESTONE) || state.is(Blocks.COBBLESTONE)
                || state.is(Blocks.BLACKSTONE) || state.is(Blocks.GILDED_BLACKSTONE)
                || state.is(Blocks.DEEPSLATE) || state.is(Blocks.CALCITE)
                || state.is(Blocks.COPPER_ORE) || state.is(Blocks.COAL_ORE)
                || state.is(Blocks.DEEPSLATE_COPPER_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE);
    }

    private void smoothEdges(WorldGenLevel level, BlockPos center, int radius, Block slabBlock) {
        int searchR = radius + 2;

        for (int dx = -searchR; dx <= searchR; dx++) {
            for (int dz = -searchR; dz <= searchR; dz++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;

                BlockPos surfacePos = SurfaceFinder.findSurface(level, x, z);
                BlockState surfaceState = level.getBlockState(surfacePos);

                if (isBoulderBlock(surfaceState)) continue;
                if (!surfaceState.isSolid()) continue;

                boolean neighborOneHigher = false;
                for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int nx = x + dir[0];
                    int nz = z + dir[1];
                    BlockPos nSurface = SurfaceFinder.findSurface(level, nx, nz);
                    BlockState nState = level.getBlockState(nSurface);
                    if (isBoulderBlock(nState) && nSurface.getY() == surfacePos.getY() + 1) {
                        neighborOneHigher = true;
                        break;
                    }
                }
                if (!neighborOneHigher) continue;

                BlockPos slabPos = surfacePos.above();
                if (!level.getBlockState(slabPos).canBeReplaced()) continue;

                level.setBlock(slabPos, slabBlock.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
            }
        }
    }
}