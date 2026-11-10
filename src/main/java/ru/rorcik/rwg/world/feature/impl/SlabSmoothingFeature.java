package ru.rorcik.rwg.world.feature.impl;

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
import ru.rorcik.rwg.RwgBlocks;
import ru.rorcik.rwg.world.feature.util.DoublePlantHelper;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

import java.util.HashMap;
import java.util.Map;

public class SlabSmoothingFeature extends Feature<NoneFeatureConfiguration> {

    private static final Map<Block, Block> SLAB_FOR_BLOCK = buildSlabMap();

    private static Map<Block, Block> buildSlabMap() {
        Map<Block, Block> m = new HashMap<>();
        m.put(Blocks.DIRT, RwgBlocks.DIRT_SLAB.get());
        m.put(Blocks.GRASS_BLOCK, RwgBlocks.GRASS_SLAB.get());
        m.put(Blocks.COARSE_DIRT, RwgBlocks.COARSE_DIRT_SLAB.get());
        m.put(Blocks.PODZOL, RwgBlocks.PODZOL_SLAB.get());
        m.put(Blocks.DIRT_PATH, RwgBlocks.DIRT_PATH_SLAB.get());
        m.put(Blocks.SAND, RwgBlocks.SAND_SLAB.get());
        m.put(Blocks.RED_SAND, RwgBlocks.RED_SAND_SLAB.get());
        m.put(Blocks.GRAVEL, RwgBlocks.GRAVEL_SLAB.get());

        m.put(Blocks.STONE, Blocks.STONE_SLAB);
        m.put(Blocks.CALCITE, Blocks.DIORITE_SLAB);
        m.put(RwgBlocks.BUDDING_DIORITE.get(), Blocks.DIORITE_SLAB);
        m.put(Blocks.COBBLESTONE, Blocks.COBBLESTONE_SLAB);
        m.put(Blocks.MOSSY_COBBLESTONE, Blocks.MOSSY_COBBLESTONE_SLAB);
        m.put(Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB);

        m.put(Blocks.GRANITE, Blocks.GRANITE_SLAB);
        m.put(Blocks.POLISHED_GRANITE, Blocks.POLISHED_GRANITE_SLAB);
        m.put(Blocks.DIORITE, Blocks.DIORITE_SLAB);
        m.put(Blocks.POLISHED_DIORITE, Blocks.POLISHED_DIORITE_SLAB);
        m.put(Blocks.ANDESITE, Blocks.ANDESITE_SLAB);
        m.put(Blocks.POLISHED_ANDESITE, Blocks.POLISHED_ANDESITE_SLAB);

        m.put(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE_SLAB);
        m.put(Blocks.COBBLED_DEEPSLATE, Blocks.COBBLED_DEEPSLATE_SLAB);
        m.put(Blocks.POLISHED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE_SLAB);
        m.put(Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_BRICK_SLAB);
        m.put(Blocks.DEEPSLATE_TILES, Blocks.DEEPSLATE_TILE_SLAB);

        m.put(Blocks.BLACKSTONE, Blocks.BLACKSTONE_SLAB);
        m.put(Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_SLAB);
        m.put(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
        m.put(Blocks.GILDED_BLACKSTONE, Blocks.BLACKSTONE_SLAB);

        m.put(Blocks.SANDSTONE, Blocks.SANDSTONE_SLAB);
        m.put(Blocks.SMOOTH_SANDSTONE, Blocks.SMOOTH_SANDSTONE_SLAB);
        m.put(Blocks.CUT_SANDSTONE, Blocks.CUT_SANDSTONE_SLAB);
        m.put(Blocks.RED_SANDSTONE, Blocks.RED_SANDSTONE_SLAB);
        m.put(Blocks.SMOOTH_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE_SLAB);
        m.put(Blocks.CUT_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE_SLAB);

        m.put(Blocks.SUSPICIOUS_SAND, RwgBlocks.SAND_SLAB.get());
        return m;
    }

    public SlabSmoothingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;

        BlockPos[] groundPos = new BlockPos[16 * 16];
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                groundPos[lx * 16 + lz] = findGround(level, chunkMinX + lx, chunkMinZ + lz);
            }
        }

        int placed = 0;
        int[][] dirs     = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int[][] opposite = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                BlockPos ground = groundPos[lx * 16 + lz];
                if (ground == null) continue;

                BlockState groundState = level.getBlockState(ground);
                if (isWaterloggedSlab(groundState)) continue;

                BlockState slabState = getSlabStateFor(groundState);
                if (slabState == null) continue;

                int currentY = ground.getY();
                boolean shouldPlace = false;

                for (int i = 0; i < 4; i++) {
                    int nlx = lx + dirs[i][0];
                    int nlz = lz + dirs[i][1];
                    if (nlx < 0 || nlx > 15 || nlz < 0 || nlz > 15) continue;

                    BlockPos nGround = groundPos[nlx * 16 + nlz];
                    if (nGround == null) continue;
                    if (isWaterloggedSlab(level.getBlockState(nGround))) continue;
                    if (nGround.getY() != currentY + 1) continue;

                    int olx = lx + opposite[i][0];
                    int olz = lz + opposite[i][1];
                    if (olx < 0 || olx > 15 || olz < 0 || olz > 15) {
                        shouldPlace = true;
                        break;
                    }
                    BlockPos oGround = groundPos[olx * 16 + olz];
                    if (oGround == null || oGround.getY() >= currentY) {
                        shouldPlace = true;
                        break;
                    }
                }
                if (!shouldPlace) continue;

                BlockPos abovePos = ground.above();
                BlockState aboveState = level.getBlockState(abovePos);

                if (aboveState.is(BlockTags.LOGS)) continue;
                if (aboveState.is(BlockTags.LEAVES)) continue;
                if (aboveState.is(Blocks.BEDROCK)) continue;
                if (!aboveState.getFluidState().isEmpty()) continue;

                if (!canReplaceAbove(level, abovePos, aboveState)) continue;

                if (DoublePlantHelper.isHalf(aboveState)) {
                    DoublePlantHelper.clearWholePlant(level, abovePos, aboveState);
                }

                level.setBlock(abovePos, slabState, 3);
                placed++;
            }
        }

        return placed > 0;
    }

    private BlockPos findGround(WorldGenLevel level, int x, int z) {
        int startY = SurfaceFinder.findSurfaceY(level, x, z);
        if (startY == Integer.MIN_VALUE) return null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);
        for (int i = 0; i < 6 && pos.getY() >= level.getMinBuildHeight(); i++) {
            BlockState s = level.getBlockState(pos);
            if (isWaterloggedSlab(s)) return pos.immutable();
            if (getSlabStateFor(s) != null) return pos.immutable();
            pos.move(0, -1, 0);
        }
        return null;
    }

    private boolean isWaterloggedSlab(BlockState state) {
        if (!(state.getBlock() instanceof SlabBlock)) return false;
        return state.hasProperty(SlabBlock.WATERLOGGED)
                && state.getValue(SlabBlock.WATERLOGGED);
    }

    private boolean canReplaceAbove(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) return true;
        if (state.is(Blocks.SNOW)) return true;

        if (state.is(BlockTags.REPLACEABLE_BY_TREES)) return true;
        if (state.is(BlockTags.SAPLINGS)) return true;
        if (state.is(BlockTags.FLOWERS)) return true;
        if (state.is(BlockTags.TALL_FLOWERS)) return true;

        if (state.getCollisionShape(level, pos).isEmpty()) {
            
            if (state.hasBlockEntity()) return false;

            if (state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY) || state.is(Blocks.BARRIER) || state.is(Blocks.LIGHT)) {
                return false;
            }

            return true;
        }

        return false;
    }

    private BlockState getSlabStateFor(BlockState state) {
        if (state.getBlock() instanceof SlabBlock) return null;
        Block slab = SLAB_FOR_BLOCK.get(state.getBlock());
        if (slab == null) return null;
        return slab.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
    }
}