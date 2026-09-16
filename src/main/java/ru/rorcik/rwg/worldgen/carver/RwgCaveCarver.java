package ru.rorcik.rwg.worldgen.carver;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import ru.rorcik.rwg.RwgConfig;

import java.util.function.Function;

public class RwgCaveCarver extends WorldCarver<CaveCarverConfiguration> {

    private static final BlockState WATER = Blocks.WATER.defaultBlockState();

    public RwgCaveCarver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean carve(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask carvingMask
    ) {
        if (!RwgConfig.enableOldCaves) {
            return false;
        }

        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        int tunnelCount = random.nextInt(random.nextInt(random.nextInt(RwgConfig.caveFrequency) + 1) + 1);

        for (int i = 0; i < tunnelCount; i++) {
            if (random.nextDouble() > RwgConfig.caveRarity) {
                continue;
            }

            double startX = chunkX * 16 + random.nextInt(16);
            double startY = RwgConfig.caveMinY + random.nextInt(RwgConfig.caveMaxY - RwgConfig.caveMinY);
            double startZ = chunkZ * 16 + random.nextInt(16);

            if (!canCarveAtPosition(chunk, (int)startX, (int)startY, (int)startZ)) {
                continue;
            }

            int branches = 1;

            if (random.nextInt(4) == 0) {
                generateCaveNode(context, config, chunk, biomeAccessor, random.nextLong(),
                        aquifer, startX, startY, startZ, carvingMask);
                branches += random.nextInt(4);
            }

            for (int branch = 0; branch < branches; branch++) {
                float horizontalAngle = random.nextFloat() * (float) Math.PI * 2.0F;
                float verticalAngle = (random.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float radius = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;

                generateCaveTunnel(
                        context, config, chunk, biomeAccessor, random.nextLong(),
                        aquifer, startX, startY, startZ,
                        radius, horizontalAngle, verticalAngle,
                        0, 0, 1.0, carvingMask
                );
            }
        }

        return true;
    }

    private boolean canCarveAtPosition(ChunkAccess chunk, int x, int y, int z) {
        int localX = x & 15;
        int localZ = z & 15;

        int surfaceY = chunk.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                localX,
                localZ
        );

        if (surfaceY - y < RwgConfig.caveMinSurfaceDistance) {
            if (RwgConfig.exposeCavesInMountains) {
                Holder<Biome> biome = chunk.getNoiseBiome(localX >> 2, y >> 2, localZ >> 2);
                String biomeName = biome.unwrapKey().map(key -> key.location().toString()).orElse("");

                if (biomeName.contains("mountain") || biomeName.contains("peak") ||
                        biomeName.contains("grove") || biomeName.contains("slopes")) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    private void generateCaveNode(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            long seed,
            Aquifer aquifer,
            double centerX,
            double centerY,
            double centerZ,
            CarvingMask carvingMask
    ) {
        RandomSource random = RandomSource.create(seed);
        double radius = 1.5 + (random.nextDouble() * 6.0 + random.nextDouble() * 6.0);

        carveSphere(
                context, config, chunk, biomeAccessor, aquifer,
                centerX, centerY, centerZ, radius, carvingMask
        );
    }

    private void generateCaveTunnel(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            long seed,
            Aquifer aquifer,
            double startX,
            double startY,
            double startZ,
            float baseRadius,
            float horizontalAngle,
            float verticalAngle,
            int startStep,
            int endStep,
            double heightScale,
            CarvingMask carvingMask
    ) {
        RandomSource random = RandomSource.create(seed);
        double centerX = (chunk.getPos().getMiddleBlockX());
        double centerZ = (chunk.getPos().getMiddleBlockZ());

        float horizAngle = horizontalAngle;
        float vertAngle = verticalAngle;

        float radiusVariation = 0.0F;
        float angleVariation = 0.0F;

        if (endStep <= 0) {
            int range = this.getRange() * 16 - 16;
            endStep = range - random.nextInt(range / 4);
        }

        boolean generateRoom = false;
        if (startStep == -1) {
            startStep = endStep / 2;
            generateRoom = true;
        }

        int nextRoomStep = random.nextInt(endStep / 2) + endStep / 4;

        for (int step = startStep; step < endStep; step++) {
            double radiusXZ = 1.5 + Mth.sin((float)step * (float)Math.PI / (float)endStep) * baseRadius;
            double radiusY = radiusXZ * heightScale;

            if (RwgConfig.smoothCaveWalls) {
                radiusXZ *= 0.92;
            }

            float stepLength = Mth.cos(vertAngle);
            startX += Mth.cos(horizAngle) * stepLength;
            startY += Mth.sin(vertAngle);
            startZ += Mth.sin(horizAngle) * stepLength;

            vertAngle *= 0.7F;
            vertAngle += angleVariation * 0.05F;
            horizAngle += radiusVariation * 0.05F;

            angleVariation *= 0.8F;
            radiusVariation *= 0.5F;
            angleVariation += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            radiusVariation += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!generateRoom && step == nextRoomStep && baseRadius > 1.0F) {
                generateCaveTunnel(
                        context, config, chunk, biomeAccessor, random.nextLong(),
                        aquifer, startX, startY, startZ,
                        random.nextFloat() * 0.5F + 0.5F,
                        horizAngle - ((float)Math.PI / 2F),
                        vertAngle / 3.0F,
                        step, endStep, 1.0, carvingMask
                );
                generateCaveTunnel(
                        context, config, chunk, biomeAccessor, random.nextLong(),
                        aquifer, startX, startY, startZ,
                        random.nextFloat() * 0.5F + 0.5F,
                        horizAngle + ((float)Math.PI / 2F),
                        vertAngle / 3.0F,
                        step, endStep, 1.0, carvingMask
                );
                return;
            }

            if (generateRoom || random.nextInt(4) != 0) {
                if (!canReachChunk(centerX, centerZ, startX, startZ, step, endStep, baseRadius)) {
                    return;
                }

                carveEllipsoid(
                        context, config, chunk, biomeAccessor, aquifer,
                        startX, startY, startZ, radiusXZ, radiusY, carvingMask
                );
            }
        }
    }

    private void carveSphere(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer,
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            CarvingMask carvingMask
    ) {
        carveEllipsoid(context, config, chunk, biomeAccessor, aquifer,
                centerX, centerY, centerZ, radius, radius, carvingMask);
    }

    private void carveEllipsoid(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            Aquifer aquifer,
            double centerX,
            double centerY,
            double centerZ,
            double radiusXZ,
            double radiusY,
            CarvingMask carvingMask
    ) {
        ChunkPos chunkPos = chunk.getPos();
        double chunkMinX = chunkPos.getMinBlockX();
        double chunkMinZ = chunkPos.getMinBlockZ();

        if (centerX < chunkMinX - radiusXZ - 1 || centerZ < chunkMinZ - radiusXZ - 1 ||
                centerX > chunkMinX + 16 + radiusXZ + 1 || centerZ > chunkMinZ + 16 + radiusXZ + 1) {
            return;
        }

        int minX = Math.max(Mth.floor(centerX - radiusXZ) - chunkPos.getMinBlockX() - 1, 0);
        int maxX = Math.min(Mth.floor(centerX + radiusXZ) - chunkPos.getMinBlockX() + 1, 16);
        int minY = Math.max(Mth.floor(centerY - radiusY) - 1, context.getMinGenY() + 1);
        int maxY = Math.min(Mth.floor(centerY + radiusY) + 1, context.getMinGenY() + context.getGenDepth() - 8);
        int minZ = Math.max(Mth.floor(centerZ - radiusXZ) - chunkPos.getMinBlockZ() - 1, 0);
        int maxZ = Math.min(Mth.floor(centerZ + radiusXZ) - chunkPos.getMinBlockZ() + 1, 16);

        int seaLevel = 63;

        for (int x = minX; x < maxX; x++) {
            double distX = ((double)(chunkPos.getMinBlockX() + x) + 0.5 - centerX) / radiusXZ;
            distX = distX * distX;

            for (int z = minZ; z < maxZ; z++) {
                double distZ = ((double)(chunkPos.getMinBlockZ() + z) + 0.5 - centerZ) / radiusXZ;
                distZ = distZ * distZ;

                if (distX + distZ >= 1.0) {
                    continue;
                }

                for (int y = maxY; y > minY; y--) {
                    double distY = ((double)y + 0.5 - centerY) / radiusY;
                    distY = distY * distY;

                    if (distX + distZ + distY >= 1.0) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);

                    if (!canReplaceBlock(chunk.getBlockState(pos))) {
                        continue;
                    }

                    if (!canCarveAtPosition(chunk, pos.getX(), pos.getY(), pos.getZ())) {
                        continue;
                    }

                    chunk.setBlockState(pos, CAVE_AIR, false);
                    carvingMask.set(x, y, z);

                    if (y < seaLevel) {
                        chunk.setBlockState(pos, WATER, false);
                    }
                }
            }
        }
    }

    private boolean canReplaceBlock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.GRANITE) ||
                state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE) ||
                state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.DEEPSLATE) || state.is(Blocks.TUFF);
    }

    private boolean canReachChunk(double chunkCenterX, double chunkCenterZ, double tunnelX, double tunnelZ,
                                  int step, int totalSteps, float radius) {
        double dx = tunnelX - chunkCenterX;
        double dz = tunnelZ - chunkCenterZ;
        double stepsRemaining = totalSteps - step;
        double maxDist = radius + 2.0F + 16.0F;

        return dx * dx + dz * dz - stepsRemaining * stepsRemaining <= maxDist * maxDist;
    }

    @Override
    public boolean isStartChunk(CaveCarverConfiguration config, RandomSource random) {
        return random.nextFloat() <= config.probability;
    }
}