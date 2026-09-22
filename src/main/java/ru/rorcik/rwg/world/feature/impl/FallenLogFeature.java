package ru.rorcik.rwg.world.feature.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;

public class FallenLogFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasLogged = false;

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

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY, origin.getZ());

        BlockState belowState = level.getBlockState(surfacePos.below());
        if (!belowState.isSolid()) {
            return false;
        }

        Block logType = pickLogType(random);
        int length = 3 + random.nextInt(3);
        boolean isXAxis = random.nextBoolean();
        Direction.Axis axis = isXAxis ? Direction.Axis.X : Direction.Axis.Z;

        boolean isStripped = random.nextFloat() < 0.3f;
        Block finalLog = isStripped ? getStrippedLog(logType) : logType;
        RotatedPillarBlock pillar = (RotatedPillarBlock) finalLog;

        int placedCount = 0;
        for (int i = 0; i < length; i++) {
            int dx = isXAxis ? i : 0;
            int dz = isXAxis ? 0 : i;
            BlockPos pos = surfacePos.offset(dx, 1, dz);

            BlockState existing = level.getBlockState(pos);
            if (!existing.canBeReplaced() && !existing.isAir()) {
                continue;
            }

            BlockState below = level.getBlockState(pos.below());
            if (!below.isSolid()) {
                continue;
            }

            BlockState logState = pillar.defaultBlockState()
                    .setValue(BlockStateProperties.AXIS, axis);
            level.setBlock(pos, logState, 3);
            placedCount++;
        }

        if (placedCount > 0) {
            LOGGER.info("[RWG] Placed fallen log ({} blocks) at {}", placedCount, surfacePos);
        }
        return placedCount > 0;
    }

    private Block pickLogType(RandomSource random) {
        int roll = random.nextInt(10);
        if (roll < 4) return Blocks.OAK_LOG;
        if (roll < 7) return Blocks.SPRUCE_LOG;
        if (roll < 9) return Blocks.BIRCH_LOG;
        return Blocks.DARK_OAK_LOG;
    }

    private Block getStrippedLog(Block logType) {
        if (logType == Blocks.OAK_LOG) return Blocks.STRIPPED_OAK_LOG;
        if (logType == Blocks.SPRUCE_LOG) return Blocks.STRIPPED_SPRUCE_LOG;
        if (logType == Blocks.BIRCH_LOG) return Blocks.STRIPPED_BIRCH_LOG;
        if (logType == Blocks.DARK_OAK_LOG) return Blocks.STRIPPED_DARK_OAK_LOG;
        return Blocks.STRIPPED_OAK_LOG;
    }
}