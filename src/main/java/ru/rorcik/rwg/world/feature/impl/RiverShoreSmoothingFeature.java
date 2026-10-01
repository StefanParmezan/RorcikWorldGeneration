package ru.rorcik.rwg.world.feature.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import ru.rorcik.rwg.RwgBlocks;

import java.util.Set;

public class RiverShoreSmoothingFeature extends Feature<NoneFeatureConfiguration> {

    private static final Logger LOG = LogUtils.getLogger();
    private static boolean hasLoggedFirstCall = false;

    private static final int SEARCH_ABOVE = 0;
    private static final int SEARCH_BELOW = 1;

    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private static final Set<Block> NATURAL_TERRAIN = Set.of(
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.MYCELIUM,
            Blocks.ROOTED_DIRT,
            Blocks.MUD,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.SUSPICIOUS_SAND,
            Blocks.GRAVEL,
            Blocks.SUSPICIOUS_GRAVEL,
            Blocks.CLAY,
            Blocks.SOUL_SAND,
            Blocks.SOUL_SOIL,
            Blocks.STONE,
            Blocks.CALCITE,
            Blocks.COBBLESTONE,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.DEEPSLATE,
            Blocks.TUFF,
            Blocks.BLACKSTONE,
            Blocks.BASALT,
            Blocks.SMOOTH_BASALT,
            Blocks.NETHERRACK,
            Blocks.END_STONE,
            Blocks.SANDSTONE,
            Blocks.RED_SANDSTONE,
            Blocks.TERRACOTTA,
            Blocks.WHITE_TERRACOTTA,
            Blocks.ORANGE_TERRACOTTA,
            Blocks.RED_TERRACOTTA,
            Blocks.BROWN_TERRACOTTA,
            Blocks.YELLOW_TERRACOTTA,
            Blocks.LIGHT_GRAY_TERRACOTTA,
            Blocks.PRISMARINE,
            Blocks.DARK_PRISMARINE
    );

    public RiverShoreSmoothingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!hasLoggedFirstCall) {
            LOG.info("[RWG-RIVER] place() called at {}", context.origin());
            hasLoggedFirstCall = true;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;
        int seaLevel = level.getSeaLevel();
        int waterY = seaLevel - 1;

        int placed = 0;
        int waterlogged = 0;

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = chunkMinX + lx;
                int z = chunkMinZ + lz;

                int landTopY = Integer.MIN_VALUE;
                BlockState landState = null;

                for (int y = seaLevel + SEARCH_ABOVE; y >= seaLevel - SEARCH_BELOW; y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s = level.getBlockState(p);

                    if (s.isAir()) continue;
                    if (!s.getFluidState().isEmpty()) continue;
                    if (s.is(BlockTags.LEAVES)) continue;
                    if (s.is(BlockTags.LOGS)) continue;

                    landTopY = y;
                    landState = s;
                    break;
                }

                if (landState == null) continue;

                if (landTopY > seaLevel) continue;
                if (landTopY < waterY) continue;

                if (!NATURAL_TERRAIN.contains(landState.getBlock())) continue;

                BlockPos abovePos = new BlockPos(x, landTopY + 1, z);
                BlockState above = level.getBlockState(abovePos);
                if (!above.isAir() && above.getFluidState().isEmpty()) continue;

                boolean hasWaterNeighbor = false;
                for (int[] dir : DIRECTIONS) {
                    BlockPos wp = new BlockPos(x + dir[0], waterY, z + dir[1]);
                    if (level.getBlockState(wp).getFluidState().is(Fluids.WATER)) {
                        hasWaterNeighbor = true;
                        break;
                    }
                }
                if (!hasWaterNeighbor) continue;

                BlockPos belowPos = new BlockPos(x, landTopY - 1, z);
                if (level.getBlockState(belowPos).getFluidState().is(Fluids.WATER)) continue;

                BlockPos slabPos = new BlockPos(x, landTopY, z);
                BlockState atSlab = level.getBlockState(slabPos);
                if (atSlab.getBlock() instanceof SlabBlock) continue;
                if (!isFullBlockForReplacement(atSlab)) continue;

                Block slab = getSlabFor(landState);
                if (slab == null) continue;

                boolean shouldWaterlog = false;
                for (int[] dir : DIRECTIONS) {
                    BlockPos wp = new BlockPos(x + dir[0], landTopY, z + dir[1]);
                    if (level.getBlockState(wp).getFluidState().is(Fluids.WATER)) {
                        shouldWaterlog = true;
                        break;
                    }
                }

                BlockState slabState = slab.defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.BOTTOM);

                if (shouldWaterlog && slabState.hasProperty(SlabBlock.WATERLOGGED)) {
                    slabState = slabState.setValue(SlabBlock.WATERLOGGED, true);
                    waterlogged++;
                }

                level.setBlock(slabPos, slabState, 3);
                placed++;
            }
        }

        if (placed > 0) {
            LOG.debug("[RWG-RIVER] chunk [{}, {}] placed={} (waterlogged={})",
                    chunkMinX >> 4, chunkMinZ >> 4, placed, waterlogged);
        }
        return placed > 0;
    }

    private boolean isFullBlockForReplacement(BlockState state) {
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (state.getBlock() instanceof SlabBlock) return false;
        return true;
    }

    private Block getSlabFor(BlockState state) {
        Block block = state.getBlock();

        if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK
                || block == Blocks.PODZOL || block == Blocks.ROOTED_DIRT
                || block == Blocks.MUD)
            return RwgBlocks.DIRT_SLAB.get();

        if (block == Blocks.COARSE_DIRT) return RwgBlocks.COARSE_DIRT_SLAB.get();
        if (block == Blocks.MYCELIUM) return RwgBlocks.PODZOL_SLAB.get();

        if (block == Blocks.SAND || block == Blocks.SUSPICIOUS_SAND)
            return RwgBlocks.SAND_SLAB.get();
        if (block == Blocks.RED_SAND) return RwgBlocks.RED_SAND_SLAB.get();

        if (block == Blocks.GRAVEL || block == Blocks.SUSPICIOUS_GRAVEL
                || block == Blocks.CLAY || block == Blocks.SOUL_SAND
                || block == Blocks.SOUL_SOIL)
            return RwgBlocks.GRAVEL_SLAB.get();

        if (block == Blocks.STONE || block == Blocks.CALCITE)
            return Blocks.STONE_SLAB;

        if (block == Blocks.COBBLESTONE) return Blocks.COBBLESTONE_SLAB;
        if (block == Blocks.MOSSY_COBBLESTONE) return Blocks.MOSSY_COBBLESTONE_SLAB;

        if (block == Blocks.GRANITE) return Blocks.GRANITE_SLAB;
        if (block == Blocks.DIORITE) return Blocks.DIORITE_SLAB;
        if (block == Blocks.ANDESITE) return Blocks.ANDESITE_SLAB;

        if (block == Blocks.DEEPSLATE) return Blocks.COBBLED_DEEPSLATE_SLAB;

        if (block == Blocks.BLACKSTONE) return Blocks.BLACKSTONE_SLAB;
        if (block == Blocks.BASALT || block == Blocks.SMOOTH_BASALT)
            return Blocks.BLACKSTONE_SLAB;

        if (block == Blocks.SANDSTONE) return Blocks.SANDSTONE_SLAB;
        if (block == Blocks.RED_SANDSTONE) return Blocks.RED_SANDSTONE_SLAB;

        if (block == Blocks.TERRACOTTA
                || block == Blocks.WHITE_TERRACOTTA
                || block == Blocks.ORANGE_TERRACOTTA
                || block == Blocks.RED_TERRACOTTA
                || block == Blocks.BROWN_TERRACOTTA
                || block == Blocks.YELLOW_TERRACOTTA
                || block == Blocks.LIGHT_GRAY_TERRACOTTA)
            return RwgBlocks.RED_SAND_SLAB.get();

        if (block == Blocks.PRISMARINE) return Blocks.PRISMARINE_SLAB;
        if (block == Blocks.DARK_PRISMARINE) return Blocks.DARK_PRISMARINE_SLAB;

        return null;
    }
}