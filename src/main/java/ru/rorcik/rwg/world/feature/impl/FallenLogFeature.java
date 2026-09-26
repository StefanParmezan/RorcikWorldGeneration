package ru.rorcik.rwg.world.feature.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.slf4j.Logger;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

import java.util.ArrayList;
import java.util.List;

public class FallenLogFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasLogged = false;

    private static final float SPAWN_CHANCE                 = 0.35f;
    private static final float STRIPPED_PER_BLOCK_CHANCE    = 0.25f;
    private static final float LEAF_CAP_CHANCE              = 0.20f;
    private static final float MUSHROOM_ON_LOG_CHANCE       = 0.35f;
    private static final float MUSHROOM_ON_GROUND_CHANCE    = 0.35f;

    public FallenLogFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (!hasLogged) {
            LOGGER.info("[RWG] FallenLogFeature called at {}", origin);
            hasLogged = true;
        }

        if (random.nextFloat() >= SPAWN_CHANCE) {
            return false;
        }

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        if (!SurfaceFinder.isSolidGround(level, surfacePos)) {
            return false;
        }

        Block baseLog = pickLogTypeByBiome(level, surfacePos, random);
        int length = 3 + random.nextInt(3);
        boolean isXAxis = random.nextBoolean();
        Direction.Axis axis = isXAxis ? Direction.Axis.X : Direction.Axis.Z;

        List<BlockPos> placedLogs = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            int dx = isXAxis ? i : 0;
            int dz = isXAxis ? 0 : i;
            BlockPos pos = surfacePos.offset(dx, 1, dz);

            BlockState existing = level.getBlockState(pos);
            if (!canLogOverride(level, pos, existing)) {
                continue;
            }

            BlockState below = level.getBlockState(pos.below());
            if (!below.isSolid()) {
                continue;
            }

            Block logVariant = (random.nextFloat() < STRIPPED_PER_BLOCK_CHANCE)
                    ? getStrippedLog(baseLog)
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
            tryPlaceMushroomOnLog(level, logPos, random);
        }

        if (random.nextFloat() < LEAF_CAP_CHANCE) {
            placeLeafCap(level, lastLogPos, getLeavesForLog(baseLog), random);
        }

        tryPlaceMushroomOnGround(level, lastLogPos, isXAxis, random);

        LOGGER.info("[RWG] Placed fallen log ({} blocks) at {}", placedLogs.size(), surfacePos);
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

    private void placeLeafCap(WorldGenLevel level, BlockPos tip, Block leafBlock, RandomSource random) {
        int r = 1 + random.nextInt(2);
        for (int dy = 0; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int dist2 = dx * dx + dy * dy + dz * dz;
                    if (dist2 > r * r + 1) continue;
                    if (random.nextFloat() < 0.30f) continue;

                    BlockPos p = tip.offset(dx, dy, dz);
                    if (p.equals(tip)) continue;

                    BlockState existing = level.getBlockState(p);
                    if (!existing.isAir() && !existing.canBeReplaced()) continue;

                    level.setBlock(p, leafBlock.defaultBlockState(), 3);
                }
            }
        }
    }

    private void tryPlaceMushroomOnLog(WorldGenLevel level, BlockPos logPos, RandomSource random) {
        if (random.nextFloat() >= MUSHROOM_ON_LOG_CHANCE) return;

        BlockPos above = logPos.above();
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.isAir()) return;

        Block mushroomBlock = random.nextBoolean()
                ? Blocks.RED_MUSHROOM
                : Blocks.BROWN_MUSHROOM;

        level.setBlock(above, mushroomBlock.defaultBlockState(), 3);
    }

    private void tryPlaceMushroomOnGround(WorldGenLevel level, BlockPos lastLogPos,
                                          boolean isXAxis, RandomSource random) {
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
            BlockState existing = level.getBlockState(placePos);
            if (!existing.isAir()) continue;

            BlockState below = level.getBlockState(placePos.below());
            if (!below.isSolid()) continue;

            level.setBlock(placePos, mushroomBlock.defaultBlockState(), 3);
            return;
        }
    }

    private Block pickLogTypeByBiome(WorldGenLevel level, BlockPos pos, RandomSource random) {
        String name = level.getBiome(pos).unwrapKey()
                .map(k -> k.location().toString())
                .orElse("");

        if (name.contains("jungle")) return Blocks.JUNGLE_LOG;
        if (name.contains("birch")) return Blocks.BIRCH_LOG;
        if (name.contains("dark_forest")) return Blocks.DARK_OAK_LOG;
        if (name.contains("cherry")) return Blocks.CHERRY_LOG;
        if (name.contains("savanna")) return Blocks.ACACIA_LOG;
        if (name.contains("taiga") || name.contains("grove")
                || name.contains("snowy") || name.contains("frozen")) {
            return Blocks.SPRUCE_LOG;
        }
        if (name.equals("minecraft:plains")
                || name.equals("minecraft:sunflower_plains")
                || name.equals("minecraft:meadow")
                || name.equals("minecraft:flower_forest")) {
            return Blocks.OAK_LOG;
        }

        int roll = random.nextInt(10);
        if (roll < 4) return Blocks.OAK_LOG;
        if (roll < 6) return Blocks.SPRUCE_LOG;
        if (roll < 8) return Blocks.BIRCH_LOG;
        return Blocks.DARK_OAK_LOG;
    }

    private Block getStrippedLog(Block logType) {
        if (logType == Blocks.OAK_LOG) return Blocks.STRIPPED_OAK_LOG;
        if (logType == Blocks.SPRUCE_LOG) return Blocks.STRIPPED_SPRUCE_LOG;
        if (logType == Blocks.BIRCH_LOG) return Blocks.STRIPPED_BIRCH_LOG;
        if (logType == Blocks.DARK_OAK_LOG) return Blocks.STRIPPED_DARK_OAK_LOG;
        if (logType == Blocks.JUNGLE_LOG) return Blocks.STRIPPED_JUNGLE_LOG;
        if (logType == Blocks.ACACIA_LOG) return Blocks.STRIPPED_ACACIA_LOG;
        if (logType == Blocks.CHERRY_LOG) return Blocks.STRIPPED_CHERRY_LOG;
        return Blocks.STRIPPED_OAK_LOG;
    }

    private Block getLeavesForLog(Block logType) {
        if (logType == Blocks.OAK_LOG) return Blocks.OAK_LEAVES;
        if (logType == Blocks.SPRUCE_LOG) return Blocks.SPRUCE_LEAVES;
        if (logType == Blocks.BIRCH_LOG) return Blocks.BIRCH_LEAVES;
        if (logType == Blocks.DARK_OAK_LOG) return Blocks.DARK_OAK_LEAVES;
        if (logType == Blocks.JUNGLE_LOG) return Blocks.JUNGLE_LEAVES;
        if (logType == Blocks.ACACIA_LOG) return Blocks.ACACIA_LEAVES;
        if (logType == Blocks.CHERRY_LOG) return Blocks.CHERRY_LEAVES;
        return Blocks.OAK_LEAVES;
    }
}