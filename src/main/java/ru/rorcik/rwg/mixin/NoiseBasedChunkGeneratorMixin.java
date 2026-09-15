package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    private static final Map<Block, Supplier<Block>> BLOCK_TO_SLAB = new HashMap<>();

    static {
        BLOCK_TO_SLAB.put(Blocks.DIRT, () -> RwgBlocks.DIRT_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.GRASS_BLOCK, () -> RwgBlocks.GRASS_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.COARSE_DIRT, () -> RwgBlocks.COARSE_DIRT_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.PODZOL, () -> RwgBlocks.PODZOL_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.DIRT_PATH, () -> RwgBlocks.DIRT_PATH_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.SAND, () -> RwgBlocks.SAND_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.RED_SAND, () -> RwgBlocks.RED_SAND_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.GRAVEL, () -> RwgBlocks.GRAVEL_SLAB.get());
        BLOCK_TO_SLAB.put(Blocks.STONE, () -> Blocks.STONE_SLAB);
        BLOCK_TO_SLAB.put(Blocks.COBBLESTONE, () -> Blocks.COBBLESTONE_SLAB);
        BLOCK_TO_SLAB.put(Blocks.ANDESITE, () -> Blocks.ANDESITE_SLAB);
        BLOCK_TO_SLAB.put(Blocks.GRANITE, () -> Blocks.GRANITE_SLAB);
        BLOCK_TO_SLAB.put(Blocks.DIORITE, () -> Blocks.DIORITE_SLAB);
    }

    @Inject(method = "buildSurface", at = @At("TAIL"), remap = true)
    private void rwg$applySlabSmoothing(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk,
            CallbackInfo ci
    ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                processColumn(chunk, x, z);
            }
        }
    }

    private static void processColumn(ChunkAccess chunk, int x, int z) {
        int worldX = chunk.getPos().getMinBlockX() + x;
        int worldZ = chunk.getPos().getMinBlockZ() + z;

        int currentHeight = findActualSurface(chunk, x, z);

        if (currentHeight < chunk.getMinBuildHeight()) {
            return;
        }

        BlockPos topPos = new BlockPos(worldX, currentHeight, worldZ);
        BlockState topState = chunk.getBlockState(topPos);
        Block topBlock = topState.getBlock();

        if (!BLOCK_TO_SLAB.containsKey(topBlock)) {
            return;
        }

        if (!isActualSurface(chunk, topPos)) {
            return;
        }

        if (!isSolidGround(chunk, topPos)) {
            return;
        }

        boolean hasWater = !topState.getFluidState().isEmpty();

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        int lowerNeighbors = 0;
        int minNeighborHeight = currentHeight;

        for (int[] dir : directions) {
            int nx = x + dir[0];
            int nz = z + dir[1];

            if (nx >= 0 && nx < 16 && nz >= 0 && nz < 16) {
                int neighborHeight = findActualSurface(chunk, nx, nz);

                if (neighborHeight < currentHeight) {
                    lowerNeighbors++;
                    minNeighborHeight = Math.min(minNeighborHeight, neighborHeight);
                }

                if (!hasWater) {
                    BlockPos neighborPos = new BlockPos(
                            chunk.getPos().getMinBlockX() + nx,
                            neighborHeight,
                            chunk.getPos().getMinBlockZ() + nz
                    );
                    BlockState neighborState = chunk.getBlockState(neighborPos);
                    if (!neighborState.getFluidState().isEmpty()) {
                        hasWater = true;
                    }
                }
            }
        }

        if (lowerNeighbors > 0 && lowerNeighbors < 4 && (currentHeight - minNeighborHeight) == 1) {
            Supplier<Block> slabSupplier = BLOCK_TO_SLAB.get(topBlock);
            Block slabBlock = slabSupplier.get();

            BlockState slabState = slabBlock.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM);

            if (hasWater) {
                slabState = slabState.setValue(SlabBlock.WATERLOGGED, true);
            }

            chunk.setBlockState(topPos, slabState, false);
        }
    }

    private static int findActualSurface(ChunkAccess chunk, int x, int z) {
        int maxY = chunk.getMaxBuildHeight() - 1;
        int minY = chunk.getMinBuildHeight();

        for (int y = maxY; y >= minY; y--) {
            int worldX = chunk.getPos().getMinBlockX() + x;
            int worldZ = chunk.getPos().getMinBlockZ() + z;
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            BlockState state = chunk.getBlockState(pos);

            if (state.isAir() ||
                    state.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock ||
                    state.getBlock() instanceof net.minecraft.world.level.block.TallGrassBlock ||
                    state.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock ||
                    state.getBlock() instanceof net.minecraft.world.level.block.DoublePlantBlock) {
                continue;
            }

            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                for (int dy = y - 1; dy >= minY; dy--) {
                    BlockPos belowPos = new BlockPos(worldX, dy, worldZ);
                    BlockState belowState = chunk.getBlockState(belowPos);

                    if (!belowState.isAir() &&
                            !belowState.is(Blocks.WATER) &&
                            !belowState.is(Blocks.LAVA)) {
                        return dy;
                    }
                }
                continue;
            }

            return y;
        }

        return minY;
    }

    private static boolean isActualSurface(ChunkAccess chunk, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = chunk.getBlockState(above);

        if (!aboveState.isAir() &&
                !(aboveState.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock) &&
                !(aboveState.getBlock() instanceof net.minecraft.world.level.block.TallGrassBlock) &&
                !(aboveState.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock) &&
                !(aboveState.getBlock() instanceof net.minecraft.world.level.block.DoublePlantBlock) &&
                !(aboveState.getBlock() instanceof net.minecraft.world.level.block.SnowLayerBlock)) {

            BlockPos above2 = pos.above(2);
            BlockState above2State = chunk.getBlockState(above2);

            if (!above2State.isAir()) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSolidGround(ChunkAccess chunk, BlockPos pos) {
        int airCount = 0;
        int consecutiveAir = 0;

        for (int i = 1; i <= 8; i++) {
            BlockPos below = pos.below(i);

            if (below.getY() < chunk.getMinBuildHeight()) {
                break;
            }

            BlockState state = chunk.getBlockState(below);

            if (state.isAir() || !state.getFluidState().isEmpty()) {
                airCount++;
                consecutiveAir++;

                if (consecutiveAir >= 3) {
                    return false;
                }

                if (airCount > i / 2) {
                    return false;
                }
            } else {
                consecutiveAir = 0;
            }
        }

        return true;
    }
}