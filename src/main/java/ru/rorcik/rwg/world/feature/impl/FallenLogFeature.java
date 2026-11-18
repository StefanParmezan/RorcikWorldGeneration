package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.feature.util.DoublePlantHelper;
import ru.rorcik.rwg.world.feature.util.StructureGuard;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FallenLogFeature extends Feature<NoneFeatureConfiguration> {

    private static final float SPAWN_CHANCE              = 0.35f;
    private static final float STRIPPED_PER_BLOCK_CHANCE = 0.25f;
    private static final float LEAF_CAP_CHANCE           = 0.20f;
    private static final float MUSHROOM_ON_LOG_CHANCE    = 0.35f;
    private static final float MUSHROOM_ON_GROUND_CHANCE = 0.35f;

    private static final Map<Block, Block> STRIPPED = new HashMap<>();
    private static final Map<Block, Block> LEAVES   = new HashMap<>();

    static {
        STRIPPED.put(Blocks.OAK_LOG,      Blocks.STRIPPED_OAK_LOG);
        STRIPPED.put(Blocks.SPRUCE_LOG,   Blocks.STRIPPED_SPRUCE_LOG);
        STRIPPED.put(Blocks.BIRCH_LOG,    Blocks.STRIPPED_BIRCH_LOG);
        STRIPPED.put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG);
        STRIPPED.put(Blocks.JUNGLE_LOG,   Blocks.STRIPPED_JUNGLE_LOG);
        STRIPPED.put(Blocks.ACACIA_LOG,   Blocks.STRIPPED_ACACIA_LOG);
        STRIPPED.put(Blocks.CHERRY_LOG,   Blocks.STRIPPED_CHERRY_LOG);

        LEAVES.put(Blocks.OAK_LOG,      Blocks.OAK_LEAVES);
        LEAVES.put(Blocks.SPRUCE_LOG,   Blocks.SPRUCE_LEAVES);
        LEAVES.put(Blocks.BIRCH_LOG,    Blocks.BIRCH_LEAVES);
        LEAVES.put(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_LEAVES);
        LEAVES.put(Blocks.JUNGLE_LOG,   Blocks.JUNGLE_LEAVES);
        LEAVES.put(Blocks.ACACIA_LOG,   Blocks.ACACIA_LEAVES);
        LEAVES.put(Blocks.CHERRY_LOG,   Blocks.CHERRY_LEAVES);
    }

    public FallenLogFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (StructureGuard.isInCity(level, origin)) return false;
        if (random.nextFloat() >= SPAWN_CHANCE) return false;

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        if (!SurfaceFinder.isSolidGround(level, surfacePos)) return false;

        Block baseLog = pickLogTypeByBiome(level, surfacePos, random);
        int length = 3 + random.nextInt(3);
        boolean isXAxis = random.nextBoolean();
        Direction.Axis axis = isXAxis ? Direction.Axis.X : Direction.Axis.Z;

        StructureGuard guard = StructureGuard.forArea(level,
                surfacePos.getX() - 3, surfacePos.getZ() - 3,
                surfacePos.getX() + length + 3, surfacePos.getZ() + length + 3);
        if (guard.contains(surfacePos)) return false;

        List<BlockPos> placedLogs = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int dx = isXAxis ? i : 0;
            int dz = isXAxis ? 0 : i;
            BlockPos pos = surfacePos.offset(dx, 1, dz);

            if (guard.contains(pos) || guard.contains(pos.below())) continue;
            BlockState existing = level.getBlockState(pos);
            if (!canLogOverride(level, pos, existing)) continue;
            if (!level.getBlockState(pos.below()).isSolid()) continue;

            if (DoublePlantHelper.isHalf(existing)) {
                DoublePlantHelper.clearWholePlant(level, pos, existing);
            }

            Block logVariant = (random.nextFloat() < STRIPPED_PER_BLOCK_CHANCE)
                    ? STRIPPED.getOrDefault(baseLog, Blocks.STRIPPED_OAK_LOG)
                    : baseLog;
            RotatedPillarBlock pillar = (RotatedPillarBlock) logVariant;

            BlockState logState = pillar.defaultBlockState()
                    .setValue(BlockStateProperties.AXIS, axis);
            level.setBlock(pos, logState, 3);
            placedLogs.add(pos);
        }

        if (placedLogs.isEmpty()) return false;

        BlockPos lastLogPos = placedLogs.get(placedLogs.size() - 1);

        for (BlockPos logPos : placedLogs) {
            tryPlaceMushroomOnLog(level, logPos, random, guard);
        }

        if (random.nextFloat() < LEAF_CAP_CHANCE) {
            placeLeafCap(level, lastLogPos,
                    LEAVES.getOrDefault(baseLog, Blocks.OAK_LEAVES), random, guard);
        }

        tryPlaceMushroomOnGround(level, lastLogPos, isXAxis, random, guard);
        return true;
    }

    private boolean canLogOverride(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!state.getFluidState().isEmpty()) return false;
        if (state.is(BlockTags.LOGS)) return false;
        if (state.is(BlockTags.LEAVES)) return false;
        if (state.is(Blocks.BEDROCK)) return false;
        if (state.is(Blocks.OBSIDIAN)) return false;
        if (state.is(Blocks.CRYING_OBSIDIAN)) return false;
        if (state.isAir()) return true;
        if (state.canBeReplaced()) return true;
        if (state.getCollisionShape(level, pos).isEmpty()) return true;
        return false;
    }

    private void placeLeafCap(WorldGenLevel level, BlockPos tip,
                              Block leafBlock, RandomSource random, StructureGuard guard) {
        int r = 1 + random.nextInt(2);
        for (int dy = 0; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int dist2 = dx * dx + dy * dy + dz * dz;
                    if (dist2 > r * r + 1) continue;
                    if (random.nextFloat() < 0.30f) continue;

                    BlockPos p = tip.offset(dx, dy, dz);
                    if (p.equals(tip)) continue;
                    if (guard.contains(p)) continue;

                    BlockState existing = level.getBlockState(p);
                    if (!existing.isAir() && !existing.canBeReplaced()) continue;

                    level.setBlock(p, leafBlock.defaultBlockState(), 3);
                }
            }
        }
    }

    private void tryPlaceMushroomOnLog(WorldGenLevel level, BlockPos logPos,
                                       RandomSource random, StructureGuard guard) {
        if (random.nextFloat() >= MUSHROOM_ON_LOG_CHANCE) return;

        BlockPos above = logPos.above();
        if (guard.contains(above)) return;
        if (!level.getBlockState(above).isAir()) return;

        Block mushroomBlock = random.nextBoolean()
                ? Blocks.RED_MUSHROOM
                : Blocks.BROWN_MUSHROOM;

        level.setBlock(above, mushroomBlock.defaultBlockState(), 3);
    }

    private void tryPlaceMushroomOnGround(WorldGenLevel level, BlockPos lastLogPos,
                                          boolean isXAxis, RandomSource random, StructureGuard guard) {
        if (random.nextFloat() >= MUSHROOM_ON_GROUND_CHANCE) return;

        Block mushroomBlock = random.nextBoolean()
                ? Blocks.RED_MUSHROOM
                : Blocks.BROWN_MUSHROOM;

        int[][] offsets = isXAxis
                ? new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}
                : new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        int start = random.nextInt(offsets.length);
        for (int k = 0; k < offsets.length; k++) {
            int[] off = offsets[(start + k) % offsets.length];
            BlockPos candidate = lastLogPos.offset(off[0], 0, off[1]);

            BlockPos groundPos = SurfaceFinder.findSurface(level, candidate.getX(), candidate.getZ());
            if (groundPos == null) continue;
            if (!SurfaceFinder.isSolidGround(level, groundPos)) continue;

            BlockPos placePos = groundPos.above();
            if (guard.contains(placePos)) continue;
            if (!level.getBlockState(placePos).isAir()) continue;

            if (!level.getBlockState(placePos.below()).isSolid()) continue;

            level.setBlock(placePos, mushroomBlock.defaultBlockState(), 3);
            return;
        }
    }

    private Block pickLogTypeByBiome(WorldGenLevel level, BlockPos pos, RandomSource random) {
        ResourceLocation biomeId = level.getBiome(pos).unwrapKey()
                .map(k -> k.location()).orElse(null);
        String kind = RwgConfig.logTypeFor(biomeId);
        return switch (kind) {
            case "jungle"   -> Blocks.JUNGLE_LOG;
            case "birch"    -> Blocks.BIRCH_LOG;
            case "dark_oak" -> Blocks.DARK_OAK_LOG;
            case "cherry"   -> Blocks.CHERRY_LOG;
            case "acacia"   -> Blocks.ACACIA_LOG;
            case "spruce"   -> Blocks.SPRUCE_LOG;
            default         -> Blocks.OAK_LOG;
        };
    }
}