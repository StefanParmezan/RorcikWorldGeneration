package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.rorcik.rwg.RwgBlocks;

public class SlabSmoothingFeature extends Feature<NoneFeatureConfiguration> {

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

                level.setBlock(abovePos, slabState, 3);
                placed++;
            }
        }

        return placed > 0;
    }

    private BlockPos findGround(WorldGenLevel level, int x, int z) {
        int startY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);

        for (int i = 0; i < 6; i++) {
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

        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty();
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

        if (state.is(Blocks.STONE))
            return Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.CALCITE))
            return Blocks.DIORITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(RwgBlocks.BUDDING_DIORITE.get()))
            return Blocks.DIORITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.COBBLESTONE))
            return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.MOSSY_COBBLESTONE))
            return Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.SMOOTH_STONE))
            return Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.GRANITE))
            return Blocks.GRANITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.POLISHED_GRANITE))
            return Blocks.POLISHED_GRANITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DIORITE))
            return Blocks.DIORITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.POLISHED_DIORITE))
            return Blocks.POLISHED_DIORITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.ANDESITE))
            return Blocks.ANDESITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.POLISHED_ANDESITE))
            return Blocks.POLISHED_ANDESITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.DEEPSLATE))
            return Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.COBBLED_DEEPSLATE))
            return Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.POLISHED_DEEPSLATE))
            return Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DEEPSLATE_BRICKS))
            return Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DEEPSLATE_TILES))
            return Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.BLACKSTONE))
            return Blocks.BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.POLISHED_BLACKSTONE))
            return Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.POLISHED_BLACKSTONE_BRICKS))
            return Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.GILDED_BLACKSTONE))
            return Blocks.BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.SANDSTONE))
            return Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.SMOOTH_SANDSTONE))
            return Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.CUT_SANDSTONE))
            return Blocks.CUT_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.RED_SANDSTONE))
            return Blocks.RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.SMOOTH_RED_SANDSTONE))
            return Blocks.SMOOTH_RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.CUT_RED_SANDSTONE))
            return Blocks.CUT_RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.SUSPICIOUS_SAND))
            return RwgBlocks.SAND_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        return null;
    }
}