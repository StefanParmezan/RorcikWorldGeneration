package ru.rorcik.rwg.world.feature.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WildFarmFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasLogged = false;

    public WildFarmFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (!hasLogged) {
            LOGGER.info("[RWG] WildFarmFeature called at {}", origin);
            hasLogged = true;
        }

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY, origin.getZ());

        BlockState belowState = level.getBlockState(surfacePos.below());
        if (!belowState.isSolid()) {
            return false;
        }

        BlockState topState = level.getBlockState(surfacePos);
        if (!isFarmable(topState)) {
            return false;
        }

        List<BlockPos> farmBlocks = new ArrayList<>();
        farmBlocks.add(surfacePos);

        for (int[] dir : new int[][]{{1,0}, {-1,0}, {0,1}, {0,-1}}) {
            BlockPos neighbor = surfacePos.offset(dir[0], 0, dir[1]);
            int neighborY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, neighbor.getX(), neighbor.getZ());
            if (Math.abs(neighborY - surfaceY) <= 1) {
                BlockPos neighborSurface = new BlockPos(neighbor.getX(), neighborY, neighbor.getZ());
                if (isFarmable(level.getBlockState(neighborSurface))) {
                    farmBlocks.add(neighborSurface);
                }
            }
        }

        while (farmBlocks.size() > 5) {
            farmBlocks.remove(farmBlocks.size() - 1);
        }

        for (BlockPos pos : farmBlocks) {
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 3);
            BlockState crop = getRandomCrop(random);
            BlockPos abovePos = pos.above();
            if (level.getBlockState(abovePos).canBeReplaced()) {
                level.setBlock(abovePos, crop, 3);
            }
        }

        LOGGER.info("[RWG] Placed wild farm with {} blocks at {}", farmBlocks.size(), surfacePos);
        return !farmBlocks.isEmpty();
    }

    private boolean isFarmable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL);
    }

    private BlockState getRandomCrop(RandomSource random) {
        int type = random.nextInt(4);
        int age = random.nextInt(8);
        if (age > 2 && age < 7) age = 1;

        return switch (type) {
            case 0 -> Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
            case 1 -> Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, age);
            case 2 -> Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, age);
            case 3 -> Blocks.BEETROOTS.defaultBlockState().setValue(CropBlock.AGE, Math.min(age, 3));
            default -> Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
        };
    }
}