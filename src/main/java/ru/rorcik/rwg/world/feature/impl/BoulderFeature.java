package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.feature.util.DoublePlantHelper;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BoulderFeature extends Feature<NoneFeatureConfiguration> {

    private static final float ORE_CHANCE    = 0.02f;
    private static final float GILDED_CHANCE = 0.03f;

    private static final Set<String> COLD_BIOMES = Set.of(
            "minecraft:snowy_plains",
            "minecraft:snowy_taiga",
            "minecraft:snowy_beach",
            "minecraft:ice_spikes",
            "minecraft:frozen_river",
            "minecraft:frozen_ocean",
            "minecraft:deep_frozen_ocean",
            "minecraft:snowy_slopes",
            "minecraft:jagged_peaks",
            "minecraft:frozen_peaks",
            "minecraft:grove"
    );

    private static final Set<Block> BOULDER_BLOCKS = Set.of(
            Blocks.STONE, Blocks.ANDESITE, Blocks.TUFF, Blocks.GRANITE,
            Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE,
            Blocks.BLACKSTONE, Blocks.GILDED_BLACKSTONE,
            Blocks.DEEPSLATE, Blocks.CALCITE,
            Blocks.COPPER_ORE, Blocks.COAL_ORE,
            Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE,
            Blocks.RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE,
            Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.ICE,
            Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW
    );

    private static final Map<BoulderType, Block> CAP_SLAB = buildCapSlabMap();

    private static Map<BoulderType, Block> buildCapSlabMap() {
        Map<BoulderType, Block> m = new HashMap<>();
        m.put(BoulderType.STONE, Blocks.STONE_SLAB);
        m.put(BoulderType.BLACKSTONE, Blocks.BLACKSTONE_SLAB);
        m.put(BoulderType.DEEPSLATE, Blocks.COBBLED_DEEPSLATE_SLAB);
        m.put(BoulderType.CALCITE, Blocks.STONE_SLAB);
        m.put(BoulderType.SANDSTONE, Blocks.SANDSTONE_SLAB);
        m.put(BoulderType.RED_SANDSTONE, Blocks.RED_SANDSTONE_SLAB);
        return m;
    }

    public BoulderFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public enum BoulderType {
        STONE, BLACKSTONE, DEEPSLATE, CALCITE,
        SANDSTONE, RED_SANDSTONE, ICE, SNOW
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        if (!SurfaceFinder.isSolidGround(level, surfacePos)) return false;
        if (isTooSteep(level, surfacePos)) return false;

        BoulderType type = pickBoulderTypeByBiome(level, surfacePos, random);
        int radius = 2 + random.nextInt(2);

        List<BlockPos> placed = placeBoulder(level, surfacePos, type, radius, random);
        if (placed.isEmpty()) return false;

        placeCapSlab(level, type, placed, random);
        smoothBase(level, surfacePos, radius, type);

        return true;
    }

    private BoulderType pickBoulderTypeByBiome(WorldGenLevel level, BlockPos pos, RandomSource random) {
        ResourceLocation biomeId = level.getBiome(pos).unwrapKey()
                .map(k -> k.location()).orElse(null);
        String kind = RwgConfig.boulderTypeFor(biomeId);
        if (kind != null) {
            switch (kind) {
                case "cold":     return random.nextBoolean() ? BoulderType.ICE : BoulderType.SNOW;
                case "desert":   return BoulderType.SANDSTONE;
                case "badlands": return BoulderType.RED_SANDSTONE;
            }
        }
        
        int roll = random.nextInt(100);
        if (roll < 55) return BoulderType.STONE;
        if (roll < 75) return BoulderType.DEEPSLATE;
        if (roll < 88) return BoulderType.CALCITE;
        return BoulderType.BLACKSTONE;
    }

    private List<BlockPos> placeBoulder(WorldGenLevel level, BlockPos center,
                                        BoulderType type, int radius, RandomSource random) {
        List<BlockPos> placed = new ArrayList<>();
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

                    if (DoublePlantHelper.isHalf(existing)) {
                        DoublePlantHelper.clearWholePlant(level, pos, existing);
                    }

                    BlockState toPlace = pickBlockForPosition(type, dy, random);
                    level.setBlock(pos, toPlace, 3);
                    placed.add(pos);
                }
            }
        }
        return placed;
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
            case SANDSTONE -> {
                int r = random.nextInt(10);
                if (r < 5) return Blocks.SANDSTONE.defaultBlockState();
                if (r < 8) return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
                return Blocks.CUT_SANDSTONE.defaultBlockState();
            }
            case RED_SANDSTONE -> {
                int r = random.nextInt(10);
                if (r < 5) return Blocks.RED_SANDSTONE.defaultBlockState();
                if (r < 8) return Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
                return Blocks.CUT_RED_SANDSTONE.defaultBlockState();
            }
            case ICE -> {
                int r = random.nextInt(10);
                if (r < 6) return Blocks.PACKED_ICE.defaultBlockState();
                if (r < 9) return Blocks.BLUE_ICE.defaultBlockState();
                return Blocks.ICE.defaultBlockState();
            }
            case SNOW -> {
                int r = random.nextInt(10);
                if (r < 8) return Blocks.SNOW_BLOCK.defaultBlockState();
                return Blocks.POWDER_SNOW.defaultBlockState();
            }
            default -> {
                if (random.nextFloat() < ORE_CHANCE) {
                    return (random.nextBoolean()
                            ? Blocks.COPPER_ORE
                            : Blocks.COAL_ORE).defaultBlockState();
                }
                int r = random.nextInt(10);
                if (r < 6) return Blocks.STONE.defaultBlockState();
                if (r < 8) return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                return Blocks.ANDESITE.defaultBlockState();
            }
        }
    }

    private void placeCapSlab(WorldGenLevel level, BoulderType type,
                              List<BlockPos> placed, RandomSource random) {
        BlockPos highest = null;
        for (BlockPos p : placed) {
            if (highest == null
                    || p.getY() > highest.getY()
                    || (p.getY() == highest.getY() && random.nextBoolean())) {
                highest = p;
            }
        }
        if (highest == null) return;

        BlockPos above = highest.above();
        if (!level.getBlockState(above).isAir()) return;

        Block slabBlock = CAP_SLAB.get(type);
        if (slabBlock != null) {
            level.setBlock(above, slabBlock.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
        } else {
            level.setBlock(above, Blocks.SNOW.defaultBlockState(), 3);
        }
    }

    private void smoothBase(WorldGenLevel level, BlockPos center, int radius, BoulderType type) {
        Block slabBlock = CAP_SLAB.get(type);
        if (slabBlock == null) return;

        int searchR = radius + 2;
        for (int dx = -searchR; dx <= searchR; dx++) {
            for (int dz = -searchR; dz <= searchR; dz++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;

                BlockPos surfacePos = SurfaceFinder.findSurface(level, x, z);
                BlockState surfaceState = level.getBlockState(surfacePos);

                if (BOULDER_BLOCKS.contains(surfaceState.getBlock())) continue;
                if (!surfaceState.isSolid()) continue;

                boolean neighborHigher = false;
                for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int nx = x + dir[0];
                    int nz = z + dir[1];
                    BlockPos nSurface = SurfaceFinder.findSurface(level, nx, nz);
                    BlockState nState = level.getBlockState(nSurface);
                    if (BOULDER_BLOCKS.contains(nState.getBlock())
                            && nSurface.getY() == surfacePos.getY() + 1) {
                        neighborHigher = true;
                        break;
                    }
                }
                if (!neighborHigher) continue;

                BlockPos slabPos = surfacePos.above();
                BlockState aboveState = level.getBlockState(slabPos);
                if (!aboveState.canBeReplaced()) continue;

                if (DoublePlantHelper.isHalf(aboveState)) {
                    DoublePlantHelper.clearWholePlant(level, slabPos, aboveState);
                }

                level.setBlock(slabPos, slabBlock.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM), 3);
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

    private boolean isTooSteep(WorldGenLevel level, BlockPos center) {
        int y0 = center.getY();
        int maxDiff = 0;
        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {2, 0}, {-2, 0}, {0, 2}, {0, -2}
        };
        for (int[] d : dirs) {
            int nY = SurfaceFinder.findSurfaceY(level,
                    center.getX() + d[0], center.getZ() + d[1]);
            int diff = Math.abs(nY - y0);
            if (diff > maxDiff) maxDiff = diff;
            if (maxDiff > 3) return true;
        }
        return false;
    }
}