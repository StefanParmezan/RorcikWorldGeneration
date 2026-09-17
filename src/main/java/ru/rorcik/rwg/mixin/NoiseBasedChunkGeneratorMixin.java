package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgBlocks;

import java.util.*;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    private static final Map<String, Block> LOG_BIOMES = new HashMap<>();
    private static final Set<String> BOULDER_BIOMES = new HashSet<>();

    static {
        LOG_BIOMES.put("minecraft:plains", Blocks.OAK_LOG);
        LOG_BIOMES.put("minecraft:sunflower_plains", Blocks.OAK_LOG);
        LOG_BIOMES.put("minecraft:forest", Blocks.OAK_LOG);
        LOG_BIOMES.put("minecraft:flower_forest", Blocks.OAK_LOG);
        LOG_BIOMES.put("minecraft:birch_forest", Blocks.BIRCH_LOG);
        LOG_BIOMES.put("minecraft:old_growth_birch_forest", Blocks.BIRCH_LOG);
        LOG_BIOMES.put("minecraft:dark_forest", Blocks.DARK_OAK_LOG);
        LOG_BIOMES.put("minecraft:taiga", Blocks.SPRUCE_LOG);
        LOG_BIOMES.put("minecraft:old_growth_spruce_taiga", Blocks.SPRUCE_LOG);
        LOG_BIOMES.put("minecraft:old_growth_pine_taiga", Blocks.SPRUCE_LOG);
        LOG_BIOMES.put("minecraft:grove", Blocks.SPRUCE_LOG);
        LOG_BIOMES.put("minecraft:meadow", Blocks.OAK_LOG);
        LOG_BIOMES.put("minecraft:savanna", Blocks.ACACIA_LOG);
        LOG_BIOMES.put("minecraft:savanna_plateau", Blocks.ACACIA_LOG);
        LOG_BIOMES.put("minecraft:windswept_savanna", Blocks.ACACIA_LOG);
        LOG_BIOMES.put("minecraft:jungle", Blocks.JUNGLE_LOG);
        LOG_BIOMES.put("minecraft:sparse_jungle", Blocks.JUNGLE_LOG);
        LOG_BIOMES.put("minecraft:bamboo_jungle", Blocks.JUNGLE_LOG);
        LOG_BIOMES.put("minecraft:swamp", Blocks.OAK_LOG);
        LOG_BIOMES.put("minecraft:mangrove_swamp", Blocks.MANGROVE_LOG);

        BOULDER_BIOMES.add("minecraft:plains");
        BOULDER_BIOMES.add("minecraft:sunflower_plains");
        BOULDER_BIOMES.add("minecraft:forest");
        BOULDER_BIOMES.add("minecraft:flower_forest");
        BOULDER_BIOMES.add("minecraft:birch_forest");
        BOULDER_BIOMES.add("minecraft:old_growth_birch_forest");
        BOULDER_BIOMES.add("minecraft:dark_forest");
        BOULDER_BIOMES.add("minecraft:taiga");
        BOULDER_BIOMES.add("minecraft:old_growth_spruce_taiga");
        BOULDER_BIOMES.add("minecraft:old_growth_pine_taiga");
        BOULDER_BIOMES.add("minecraft:grove");
        BOULDER_BIOMES.add("minecraft:meadow");
        BOULDER_BIOMES.add("minecraft:savanna");
        BOULDER_BIOMES.add("minecraft:savanna_plateau");
        BOULDER_BIOMES.add("minecraft:windswept_savanna");
        BOULDER_BIOMES.add("minecraft:jungle");
        BOULDER_BIOMES.add("minecraft:sparse_jungle");
        BOULDER_BIOMES.add("minecraft:bamboo_jungle");
        BOULDER_BIOMES.add("minecraft:swamp");
        BOULDER_BIOMES.add("minecraft:mangrove_swamp");
        BOULDER_BIOMES.add("minecraft:desert");
        BOULDER_BIOMES.add("minecraft:badlands");
        BOULDER_BIOMES.add("minecraft:eroded_badlands");
        BOULDER_BIOMES.add("minecraft:wooded_badlands");
        BOULDER_BIOMES.add("minecraft:stony_peaks");
        BOULDER_BIOMES.add("minecraft:windswept_hills");
        BOULDER_BIOMES.add("minecraft:windswept_gravelly_hills");
        BOULDER_BIOMES.add("minecraft:windswept_forest");
    }

    @Inject(method = "applyCarvers", at = @At("TAIL"), remap = true)
    private void rwg$applySurfaceEnhancements(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step,
            CallbackInfo ci
    ) {
        if (step != GenerationStep.Carving.AIR) return;

        long chunkHash = hash(seed, chunk.getPos().x * 31415, chunk.getPos().z * 27182);

        int boulderX = 4 + (int) (Math.abs(chunkHash) % 8L);
        int boulderZ = 4 + (int) (Math.abs(chunkHash >> 16) % 8L);
        int logX = 4 + (int) (Math.abs(chunkHash >> 32) % 8L);
        int logZ = 4 + (int) (Math.abs(chunkHash >> 48) % 8L);

        int farmX = 5 + (int) (Math.abs(chunkHash >> 24) % 6L);
        int farmZ = 5 + (int) (Math.abs(chunkHash >> 40) % 6L);
        boolean spawnFarm = (Math.abs(chunkHash >> 16) % 100L) < 15L;

        boolean spawnBoulder = (Math.abs(chunkHash) % 100L) < 3L;
        boolean spawnLog = (Math.abs(chunkHash >> 8) % 100L) < 3L;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                processColumn(region, seed, chunk, x, z,
                        x == boulderX && z == boulderZ && spawnBoulder,
                        x == logX && z == logZ && spawnLog,
                        x == farmX && z == farmZ && spawnFarm);
            }
        }
    }

    private static void processColumn(WorldGenRegion region, long seed, ChunkAccess chunk, int x, int z, boolean isBoulderSpot, boolean isLogSpot, boolean isFarmCenter) {
        int worldX = chunk.getPos().getMinBlockX() + x;
        int worldZ = chunk.getPos().getMinBlockZ() + z;

        int currentHeight = findActualSurface(chunk, x, z);
        if (currentHeight < chunk.getMinBuildHeight()) return;

        BlockPos topPos = new BlockPos(worldX, currentHeight, worldZ);
        BlockState topState = chunk.getBlockState(topPos);

        Holder<Biome> biomeHolder = region.getBiome(topPos);
        String biomeName = biomeHolder.unwrapKey().map(key -> key.location().toString()).orElse("");
        boolean allowMoss = isMossAllowed(biomeName);

        applyOrganicSurfacePalette(region, seed, chunk, topPos, topState, worldX, worldZ, allowMoss);
        topState = chunk.getBlockState(topPos);

        if (!isStoneBlock(topState)) {
            applySlabSmoothing(chunk, topPos, topState, x, z, currentHeight);
        }
        topState = chunk.getBlockState(topPos);

        if (isFarmCenter) {
            applyWildFarm(region, chunk, topPos, currentHeight, seed, worldX, worldZ, biomeName);
            topState = chunk.getBlockState(topPos);
        }

        if (isBoulderSpot || isLogSpot) {
            applyMicroDecor(region, chunk, topPos, currentHeight, seed, worldX, worldZ, x, z, isBoulderSpot, isLogSpot, biomeName);
        }
        topState = chunk.getBlockState(topPos);

        applySurfaceCarpet(chunk, topPos, topState, worldX, worldZ, seed, allowMoss);
    }

    private static boolean isMossAllowed(String biomeName) {
        return !biomeName.contains("snow") &&
                !biomeName.contains("frozen") &&
                !biomeName.contains("ice") &&
                !biomeName.contains("peak") &&
                !biomeName.contains("slope") &&
                !biomeName.contains("grove");
    }

    private static boolean isFarmAllowed(String biomeName) {
        return biomeName.equals("minecraft:plains") ||
                biomeName.equals("minecraft:sunflower_plains") ||
                biomeName.equals("minecraft:forest") ||
                biomeName.equals("minecraft:flower_forest") ||
                biomeName.equals("minecraft:birch_forest") ||
                biomeName.equals("minecraft:old_growth_birch_forest") ||
                biomeName.equals("minecraft:meadow") ||
                biomeName.equals("minecraft:taiga") ||
                biomeName.equals("minecraft:old_growth_spruce_taiga") ||
                biomeName.equals("minecraft:old_growth_pine_taiga");
    }

    private static void applyWildFarm(WorldGenRegion region, ChunkAccess chunk, BlockPos centerPos, int currentHeight, long seed, int worldX, int worldZ, String biomeName) {
        if (!isFarmAllowed(biomeName)) return;

        try {
            List<BlockPos> farmBlocks = new ArrayList<>();
            Set<BlockPos> checked = new HashSet<>();
            Queue<BlockPos> queue = new LinkedList<>();

            queue.add(centerPos);
            checked.add(centerPos);

            int maxBlocks = 8;

            while (!queue.isEmpty() && farmBlocks.size() < maxBlocks) {
                BlockPos current = queue.poll();

                int localX = current.getX() - chunk.getPos().getMinBlockX();
                int localZ = current.getZ() - chunk.getPos().getMinBlockZ();

                if (localX < 0 || localX > 15 || localZ < 0 || localZ > 15) continue;

                int h = findActualSurface(chunk, localX, localZ);
                if (Math.abs(h - currentHeight) > 2) continue;

                BlockState state = chunk.getBlockState(current);
                if (!state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.DIRT) &&
                        !state.is(Blocks.COARSE_DIRT) && !state.is(Blocks.PODZOL)) {
                    continue;
                }

                if (!chunk.getBlockState(current.above()).isAir()) {
                    continue;
                }

                farmBlocks.add(current);

                if (farmBlocks.size() >= maxBlocks) break;

                double expansionNoise = getNoise(seed + 55555L, current.getX(), current.getZ(), 0.5);

                if (expansionNoise > 0.4) {
                    for (int[] dir : new int[][]{{1,0}, {-1,0}, {0,1}, {0,-1}}) {
                        BlockPos neighbor = current.offset(dir[0], 0, dir[1]);
                        if (!checked.contains(neighbor)) {
                            checked.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }

            for (BlockPos pos : farmBlocks) {
                chunk.setBlockState(pos, Blocks.FARMLAND.defaultBlockState(), false);
                BlockState crop = getRandomWildCrop(seed, pos.getX(), pos.getZ());
                chunk.setBlockState(pos.above(), crop, false);
            }

        } catch (Exception e) {
            return;
        }
    }

    private static BlockState getRandomWildCrop(long seed, int x, int z) {
        long h = hash(seed + 77777L, x, z);
        int type = (int) (Math.abs(h) % 4L);
        int age = (int) (Math.abs(h >> 8) % 8L);

        if (age > 2 && age < 7) {
            age = 1;
        }

        switch (type) {
            case 0: return Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
            case 1: return Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, age);
            case 2: return Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, age);
            case 3: return Blocks.BEETROOTS.defaultBlockState().setValue(CropBlock.AGE, Math.min(age, 3));
        }
        return Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, age);
    }

    private static void applyOrganicSurfacePalette(WorldGenRegion region, long seed, ChunkAccess chunk, BlockPos topPos, BlockState topState, int worldX, int worldZ, boolean allowMoss) {
        if (topState.getBlock() instanceof SlabBlock ||
                topState.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ||
                topState.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock ||
                isStoneBlock(topState)) {
            return;
        }

        if (!topState.is(Blocks.GRASS_BLOCK) && !topState.is(Blocks.DIRT)) {
            return;
        }

        double patchNoise = getNoise(seed, worldX, worldZ, 0.35);

        if (allowMoss && patchNoise > 0.88) {
            chunk.setBlockState(topPos, Blocks.MOSS_BLOCK.defaultBlockState(), false);
        } else if (patchNoise > 0.78) {
            chunk.setBlockState(topPos, Blocks.PODZOL.defaultBlockState(), false);
        } else if (allowMoss && patchNoise > 0.74) {
            chunk.setBlockState(topPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), false);
        } else if (patchNoise > 0.55) {
            chunk.setBlockState(topPos, Blocks.COARSE_DIRT.defaultBlockState(), false);
        }
    }

    private static void applySlabSmoothing(ChunkAccess chunk, BlockPos topPos, BlockState topState, int x, int z, int currentHeight) {
        if (!topState.getFluidState().isEmpty() || !isActualSurface(chunk, topPos) || !isSolidGround(chunk, topPos) || hasNearbyWater(chunk, x, z, currentHeight)) {
            return;
        }

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
            }
        }

        if (lowerNeighbors > 0 && lowerNeighbors < 4 && (currentHeight - minNeighborHeight) == 1) {
            BlockState slabState = getSlabStateFor(topState);
            if (slabState != null) {
                chunk.setBlockState(topPos, slabState, false);
            }
        }
    }

    private static BlockState getSlabStateFor(BlockState state) {
        if (state.is(Blocks.DIRT)) return RwgBlocks.DIRT_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.GRASS_BLOCK)) return RwgBlocks.GRASS_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.COARSE_DIRT)) return RwgBlocks.COARSE_DIRT_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.PODZOL)) return RwgBlocks.PODZOL_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DIRT_PATH)) return RwgBlocks.DIRT_PATH_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.SAND)) return RwgBlocks.SAND_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.RED_SAND)) return RwgBlocks.RED_SAND_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.GRAVEL)) return RwgBlocks.GRAVEL_SLAB.get().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.MOSS_BLOCK)) return Blocks.MOSS_CARPET.defaultBlockState();
        if (state.is(Blocks.MOSSY_COBBLESTONE)) return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.STONE)) return Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.COBBLESTONE)) return Blocks.COBBLESTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.ANDESITE)) return Blocks.ANDESITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.GRANITE)) return Blocks.GRANITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DIORITE)) return Blocks.DIORITE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        if (state.is(Blocks.TUFF) || state.is(Blocks.CALCITE)) return Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.DEEPSLATE)) return Blocks.COBBLED_DEEPSLATE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        if (state.is(Blocks.BLACKSTONE)) return Blocks.BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);

        return null;
    }

    private static void applyMicroDecor(WorldGenRegion region, ChunkAccess chunk, BlockPos topPos, int currentHeight, long seed, int worldX, int worldZ, int localX, int localZ, boolean isBoulderSpot, boolean isLogSpot, String biomeName) {
        if (topPos.getY() < chunk.getMinBuildHeight() || topStateIsInvalidForDecor(topPos, chunk)) return;

        if (isBoulderSpot) {
            if (!BOULDER_BIOMES.contains(biomeName)) return;
            if (!isGentleSlope(chunk, localX, localZ, 2)) return;

            int radius = 3 + (int) (Math.abs(hash(seed + 888L, worldX, worldZ)) % 2L);
            long h = hash(seed + 111L, worldX, worldZ);
            Block b = (h % 3L == 0L) ? Blocks.MOSSY_COBBLESTONE : ((h % 3L == 1L) ? Blocks.STONE : Blocks.TUFF);

            if (h % 10L < 3L) {
                b = (h % 2L == 0L) ? Blocks.DEEPSLATE : Blocks.BLACKSTONE;
            } else if (h % 10L < 6L) {
                b = (h % 3L == 0L) ? Blocks.DIORITE : ((h % 3L == 1L) ? Blocks.ANDESITE : Blocks.CALCITE);
            }

            placeBoulder(chunk, topPos, b, radius, seed, worldX, worldZ);
        }

        if (isLogSpot) {
            if (!LOG_BIOMES.containsKey(biomeName)) return;
            Block logType = LOG_BIOMES.get(biomeName);
            placeFallenLog(chunk, topPos, seed, worldX, worldZ, localX, localZ, logType);
        }
    }

    private static void applySurfaceCarpet(ChunkAccess chunk, BlockPos topPos, BlockState topState, int worldX, int worldZ, long seed, boolean allowMoss) {
        if (!allowMoss) return;

        if (topState.getBlock() instanceof SlabBlock ||
                topState.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock ||
                topState.getBlock() instanceof net.minecraft.world.level.block.FlowerBlock) {
            return;
        }

        if (!isCarpetableSurface(topState)) {
            return;
        }

        BlockPos abovePos = topPos.above();
        if (chunk.getBlockState(abovePos).isAir()) {
            double scatterNoise = getNoise(seed + 12345L, worldX, worldZ, 0.8);
            if (scatterNoise > 0.82) {
                chunk.setBlockState(abovePos, Blocks.MOSS_CARPET.defaultBlockState(), false);
            }
        }
    }

    private static boolean isCarpetableSurface(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.PODZOL) ||
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.MOSSY_COBBLESTONE) ||
                state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.GRANITE) ||
                state.is(Blocks.DIORITE) || state.is(Blocks.TUFF) || state.is(Blocks.CALCITE) ||
                state.is(Blocks.DEEPSLATE) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.GILDED_BLACKSTONE) ||
                state.is(Blocks.OAK_LOG) || state.is(Blocks.SPRUCE_LOG) || state.is(Blocks.BIRCH_LOG) ||
                state.is(Blocks.JUNGLE_LOG) || state.is(Blocks.ACACIA_LOG) || state.is(Blocks.DARK_OAK_LOG) ||
                state.is(Blocks.MANGROVE_LOG) || state.is(Blocks.STRIPPED_OAK_LOG) ||
                state.is(Blocks.STRIPPED_SPRUCE_LOG) || state.is(Blocks.STRIPPED_BIRCH_LOG) ||
                state.is(Blocks.STRIPPED_JUNGLE_LOG) || state.is(Blocks.STRIPPED_ACACIA_LOG) ||
                state.is(Blocks.STRIPPED_DARK_OAK_LOG) || state.is(Blocks.STRIPPED_MANGROVE_LOG);
    }

    private static boolean topStateIsInvalidForDecor(BlockPos pos, ChunkAccess chunk) {
        BlockState state = chunk.getBlockState(pos);
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    private static boolean isGentleSlope(ChunkAccess chunk, int centerX, int centerZ, int radius) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = centerX + dx;
                int cz = centerZ + dz;
                if (cx >= 0 && cx < 16 && cz >= 0 && cz < 16) {
                    int y = findActualSurface(chunk, cx, cz);
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }
        return (maxY - minY) <= 2;
    }

    private static void placeBoulder(ChunkAccess chunk, BlockPos startPos, Block blockType, int radius, long seed, int worldX, int worldZ) {
        int surfaceY = startPos.getY();
        double centerY = surfaceY - radius * 0.3;
        double radiusY = radius * 1.2;

        List<BlockPos> boulderBlocks = new ArrayList<>();

        for (int dy = -radius - 1; dy <= radius + 2; dy++) {
            int actualY = surfaceY + dy;
            double distY = (actualY - centerY) / radiusY;
            double distY2 = distY * distY;

            for (int dx = -radius; dx <= radius; dx++) {
                double distX = dx / (double) radius;
                double distX2 = distX * distX;
                if (distX2 + distY2 > 1.0) continue;

                for (int dz = -radius; dz <= radius; dz++) {
                    double distZ = dz / (double) radius;
                    if (distX2 + distY2 + distZ * distZ <= 1.0) {
                        BlockPos placePos = startPos.offset(dx, dy, dz);
                        BlockState existing = chunk.getBlockState(placePos);

                        if (isReplaceableByBoulder(existing)) {
                            Block finalBlockType = blockType;

                            if (blockType == Blocks.BLACKSTONE) {
                                long posHash = hash(seed + 98765L, placePos.getX(), placePos.getZ());
                                if (Math.abs(posHash) % 100L < 5L) {
                                    finalBlockType = Blocks.GILDED_BLACKSTONE;
                                }
                            }

                            chunk.setBlockState(placePos, finalBlockType.defaultBlockState(), false);
                            boulderBlocks.add(placePos);
                        }
                        else if (existing.isAir() && placePos.getY() >= surfaceY) {
                            BlockPos below = placePos.below();
                            if (chunk.getBlockState(below).isSolid()) {
                                chunk.setBlockState(placePos, blockType.defaultBlockState(), false);
                                boulderBlocks.add(placePos);
                            }
                        }
                    }
                }
            }
        }

        if (!boulderBlocks.isEmpty() && Math.abs(hash(seed + 7777L, worldX, worldZ)) % 100L < 15L) {
            int oreIndex = (int) (Math.abs(hash(seed + 8888L, worldX, worldZ)) % boulderBlocks.size());
            BlockPos orePos = boulderBlocks.get(oreIndex);

            Block oreBlock;
            if (blockType == Blocks.DEEPSLATE) {
                oreBlock = Math.abs(hash(seed + 9999L, worldX, worldZ)) % 2L == 0L ?
                        Blocks.DEEPSLATE_COPPER_ORE : Blocks.DEEPSLATE_COAL_ORE;
            } else {
                oreBlock = Math.abs(hash(seed + 9999L, worldX, worldZ)) % 2L == 0L ?
                        Blocks.COPPER_ORE : Blocks.COAL_ORE;
            }

            chunk.setBlockState(orePos, oreBlock.defaultBlockState(), false);
        }
    }

    private static void placeFallenLog(ChunkAccess chunk, BlockPos startPos, long seed, int worldX, int worldZ, int localX, int localZ, Block logType) {
        int length = 3 + (int) (Math.abs(hash(seed + 555L, worldX, worldZ)) % 4L);
        boolean isXAxis = (Math.abs(hash(seed + 666L, worldX, worldZ)) % 2L) == 0L;

        if (!isTerrainFlat(chunk, localX, localZ, isXAxis, length)) {
            return;
        }

        Block strippedLogType = getStrippedLogForLog(logType);
        Block leafType = getLeavesForLog(logType);
        Block mossType = Blocks.MOSS_CARPET;
        Block mushroomType = (Math.abs(hash(seed + 991L, worldX, worldZ)) % 2L == 0L) ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;

        for (int i = 0; i < length; i++) {
            int checkLocalX = isXAxis ? localX + i : localX;
            int checkLocalZ = isXAxis ? localZ : localZ + i;

            int surfaceY = findActualSurface(chunk, checkLocalX, checkLocalZ);
            BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + checkLocalX, surfaceY + 1, chunk.getPos().getMinBlockZ() + checkLocalZ);

            if (!chunk.getBlockState(pos).isAir()) {
                continue;
            }

            BlockState below = chunk.getBlockState(pos.below());

            if (below.isSolid() || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.DIRT) ||
                    below.is(Blocks.COARSE_DIRT) || below.is(Blocks.PODZOL) || below.is(Blocks.MOSS_BLOCK) ||
                    below.is(Blocks.STONE) || below.is(Blocks.MOSSY_COBBLESTONE) || below.getBlock() instanceof SlabBlock) {

                boolean isStripped = Math.abs(hash(seed + 1001L, worldX + i, worldZ)) % 10L < 3L;
                RotatedPillarBlock pillar = (RotatedPillarBlock) (isStripped ? strippedLogType : logType);
                Direction.Axis axis = isXAxis ? Direction.Axis.X : Direction.Axis.Z;

                chunk.setBlockState(pos, pillar.defaultBlockState().setValue(BlockStateProperties.AXIS, axis), false);

                BlockPos abovePos = pos.above();
                if (chunk.getBlockState(abovePos).isAir()) {
                    if (i == 0 || i == length - 1) {
                        if (Math.abs(hash(seed + 999L, worldX + i, worldZ)) % 10L < 4L) {
                            chunk.setBlockState(abovePos, leafType.defaultBlockState(), false);
                        }
                    } else {
                        long decorHash = Math.abs(hash(seed + 888L, worldX + i, worldZ)) % 10L;
                        if (decorHash < 2L) {
                            chunk.setBlockState(abovePos, mossType.defaultBlockState(), false);
                        } else if (decorHash >= 2L && decorHash < 4L) {
                            chunk.setBlockState(abovePos, mushroomType.defaultBlockState(), false);
                        }
                    }
                }
            }
        }
    }

    private static Block getStrippedLogForLog(Block logType) {
        if (logType == Blocks.BIRCH_LOG) return Blocks.STRIPPED_BIRCH_LOG;
        if (logType == Blocks.DARK_OAK_LOG) return Blocks.STRIPPED_DARK_OAK_LOG;
        if (logType == Blocks.JUNGLE_LOG) return Blocks.STRIPPED_JUNGLE_LOG;
        if (logType == Blocks.ACACIA_LOG) return Blocks.STRIPPED_ACACIA_LOG;
        if (logType == Blocks.SPRUCE_LOG) return Blocks.STRIPPED_SPRUCE_LOG;
        if (logType == Blocks.MANGROVE_LOG) return Blocks.STRIPPED_MANGROVE_LOG;
        return Blocks.STRIPPED_OAK_LOG;
    }

    private static Block getLeavesForLog(Block logType) {
        if (logType == Blocks.BIRCH_LOG) return Blocks.BIRCH_LEAVES;
        if (logType == Blocks.DARK_OAK_LOG) return Blocks.DARK_OAK_LEAVES;
        if (logType == Blocks.JUNGLE_LOG) return Blocks.JUNGLE_LEAVES;
        if (logType == Blocks.ACACIA_LOG) return Blocks.ACACIA_LEAVES;
        if (logType == Blocks.SPRUCE_LOG) return Blocks.SPRUCE_LEAVES;
        if (logType == Blocks.MANGROVE_LOG) return Blocks.MANGROVE_LEAVES;
        return Blocks.OAK_LEAVES;
    }

    private static boolean isTerrainFlat(ChunkAccess chunk, int startLocalX, int startLocalZ, boolean isXAxis, int length) {
        int baseY = findActualSurface(chunk, startLocalX, startLocalZ);

        for (int i = 0; i < length; i++) {
            int checkLocalX = isXAxis ? startLocalX + i : startLocalX;
            int checkLocalZ = isXAxis ? startLocalZ : startLocalZ + i;

            if (checkLocalX < 0 || checkLocalX > 15 || checkLocalZ < 0 || checkLocalZ > 15) {
                return false;
            }

            int surfaceY = findActualSurface(chunk, checkLocalX, checkLocalZ);

            if (Math.abs(surfaceY - baseY) > 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isStoneBlock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.MOSSY_COBBLESTONE) ||
                state.is(Blocks.COBBLESTONE) || state.is(Blocks.TUFF) ||
                state.is(Blocks.DEEPSLATE) || state.is(Blocks.BLACKSTONE) ||
                state.is(Blocks.GILDED_BLACKSTONE) ||
                state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE) ||
                state.is(Blocks.GRANITE) || state.is(Blocks.CALCITE);
    }

    private static boolean isReplaceableByBoulder(BlockState state) {
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.STONE) ||
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MOSS_BLOCK) ||
                state.is(Blocks.ANDESITE) || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE) ||
                state.is(Blocks.TUFF) || state.is(Blocks.CALCITE) || state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.BLACKSTONE) || state.is(Blocks.MOSSY_COBBLESTONE) ||
                state.getBlock() instanceof SlabBlock;
    }

    private static double getNoise(long seed, int x, int z, double frequency) {
        double fx = x * frequency;
        double fz = z * frequency;
        int ix = (int) Math.floor(fx);
        int iz = (int) Math.floor(fz);
        double dx = fx - ix;
        double dz = fz - iz;

        dx = dx * dx * (3 - 2 * dx);
        dz = dz * dz * (3 - 2 * dz);

        long h00 = hash(seed, ix, iz);
        long h10 = hash(seed, ix + 1, iz);
        long h01 = hash(seed, ix, iz + 1);
        long h11 = hash(seed, ix + 1, iz + 1);

        double v00 = (h00 & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL;
        double v10 = (h10 & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL;
        double v01 = (h01 & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL;
        double v11 = (h11 & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL;

        double ix0 = v00 + dx * (v10 - v00);
        double ix1 = v01 + dx * (v11 - v01);
        return ix0 + dz * (ix1 - ix0);
    }

    private static long hash(long seed, int x, int z) {
        long h = seed;
        h ^= x * 0x8da6b343L;
        h ^= z * 0xd8163841L;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        return (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
    }

    private static boolean hasNearbyWater(ChunkAccess chunk, int x, int z, int surfaceY) {
        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = x + dx;
                int cz = z + dz;
                if (cx < 0 || cx > 15 || cz < 0 || cz > 15) continue;

                int wx = chunk.getPos().getMinBlockX() + cx;
                int wz = chunk.getPos().getMinBlockZ() + cz;

                for (int dy = -1; dy <= 4; dy++) {
                    int y = surfaceY + dy;
                    if (y < minY || y >= maxY) continue;

                    BlockPos checkPos = new BlockPos(wx, y, wz);
                    if (!chunk.getBlockState(checkPos).getFluidState().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int findActualSurface(ChunkAccess chunk, int x, int z) {
        int maxY = chunk.getMaxBuildHeight() - 1;
        int minY = chunk.getMinBuildHeight();

        for (int y = maxY; y >= minY; y--) {
            int worldX = chunk.getPos().getMinBlockX() + x;
            int worldZ = chunk.getPos().getMinBlockZ() + z;
            BlockPos pos = new BlockPos(worldX, y, worldZ);
            BlockState state = chunk.getBlockState(pos);

            if (state.isAir() || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN)) {
                continue;
            }

            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                for (int dy = y - 1; dy >= minY; dy--) {
                    BlockPos belowPos = new BlockPos(worldX, dy, worldZ);
                    BlockState belowState = chunk.getBlockState(belowPos);
                    if (!belowState.isAir() && !belowState.is(Blocks.WATER) && !belowState.is(Blocks.LAVA)) {
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

        if (!aboveState.isAir() && !aboveState.is(Blocks.TALL_GRASS) &&
                !aboveState.is(Blocks.FERN) && !aboveState.is(Blocks.SNOW)) {
            BlockPos above2 = pos.above(2);
            if (!chunk.getBlockState(above2).isAir()) {
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
            if (below.getY() < chunk.getMinBuildHeight()) break;

            BlockState state = chunk.getBlockState(below);
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                airCount++;
                consecutiveAir++;
                if (consecutiveAir >= 3 || airCount > i / 2) return false;
            } else {
                consecutiveAir = 0;
            }
        }
        return true;
    }
}