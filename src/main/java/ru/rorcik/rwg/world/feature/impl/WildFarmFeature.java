package ru.rorcik.rwg.world.feature.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

import java.util.ArrayList;
import java.util.List;

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

        if (!hasLogged && RwgConfig.enableLogging) {
            LOGGER.info("Info: WildFarmFeature called at {}", origin);
            hasLogged = true;
        }

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        BlockState topState = level.getBlockState(surfacePos);
        if (!isFarmable(topState)) {
            return false;
        }

        List<BlockPos> farmBlocks = new ArrayList<>();
        farmBlocks.add(surfacePos);
        for (int[] dir : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            BlockPos neighbor = surfacePos.offset(dir[0], 0, dir[1]);
            int neighborY = SurfaceFinder.findSurfaceY(level, neighbor.getX(), neighbor.getZ());
            if (Math.abs(neighborY - surfacePos.getY()) <= 1) {
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

        if (RwgConfig.enableLogging) {
            LOGGER.info("Info: Placed wild farm with {} blocks at {}", farmBlocks.size(), surfacePos);
        }
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
            case 3 -> Blocks.BEETROOTS.defaultBlockState().setValue(net.minecraft.world.level.block.BeetrootBlock.AGE, Math.min(age, 3));
            default -> Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
        };
    }
}