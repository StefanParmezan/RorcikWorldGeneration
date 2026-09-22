package ru.rorcik.rwg.world.feature.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slf4j.Logger;
import ru.rorcik.rwg.RwgBlocks;

public class SlabSmoothingFeature extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasLogged = false;

    public SlabSmoothingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        if (!hasLogged) {
            LOGGER.info("[RWG] SlabSmoothingFeature called at {}", origin);
            hasLogged = true;
        }

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos surfacePos = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        BlockState topState = level.getBlockState(surfacePos);

        if (isStoneBlock(topState)) {
            return false;
        }
        if (!topState.getFluidState().isEmpty()) {
            return false;
        }
        if (topState.getBlock() instanceof SlabBlock) {
            return false;
        }

        int currentY = surfacePos.getY();
        int lowerNeighbors = 0;
        int minNeighborY = currentY;

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int nx = surfacePos.getX() + dir[0];
            int nz = surfacePos.getZ() + dir[1];
            int neighborY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, nx, nz);
            if (neighborY < currentY) {
                lowerNeighbors++;
                minNeighborY = Math.min(minNeighborY, neighborY);
            }
        }

        if (lowerNeighbors > 0 && lowerNeighbors < 4 && (currentY - minNeighborY) == 1) {
            BlockState slabState = getSlabStateFor(topState);
            if (slabState != null) {
                level.setBlock(surfacePos, slabState, 3);
                LOGGER.info("[RWG] Placed slab at {} (type: {})", surfacePos, topState.getBlock());
                return true;
            }
        }

        return false;
    }

    private BlockState getSlabStateFor(BlockState state) {
        if (state.is(Blocks.DIRT))
            return RwgBlocks.DIRT_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.GRASS_BLOCK))
            return RwgBlocks.GRASS_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.COARSE_DIRT))
            return RwgBlocks.COARSE_DIRT_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.PODZOL))
            return RwgBlocks.PODZOL_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DIRT_PATH))
            return RwgBlocks.DIRT_PATH_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.SAND))
            return RwgBlocks.SAND_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.RED_SAND))
            return RwgBlocks.RED_SAND_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.GRAVEL))
            return RwgBlocks.GRAVEL_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        return null;
    }

    private boolean isStoneBlock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.TUFF) || state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.BLACKSTONE);
    }
}