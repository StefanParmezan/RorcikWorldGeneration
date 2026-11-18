package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.ServerLevelData;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgConfig;

import java.util.Random;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();
    private static final double NEUTRAL_ZONE_MIN = 8000.0;
    private static final double NEUTRAL_ZONE_MAX = 12000.0;
    private static final int MAX_BIOME_SAMPLES = 8000;
    private static final int FLATNESS_RADIUS = 4;
    private static final int MAX_FLATNESS_VARIANCE = 3;

    @Inject(method = "setInitialSpawn", at = @At("HEAD"), cancellable = true)
    private static void overrideInitialSpawn(ServerLevel level, ServerLevelData levelData, boolean generateBonusChest, boolean debug, CallbackInfo ci) {
        if (!RwgConfig.applyJoinWorldSpawnMixin) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        BlockPos customSpawn = findNeutralZoneSpawn(level);
        if (customSpawn != null) {
            level.setDefaultSpawnPos(customSpawn, 0.0f);
            if (RwgConfig.enableLogging) {
                LOGGER.info("Info: Successfully relocated player spawn to neutral zone at: {}", customSpawn.toShortString());
            }
            ci.cancel();
        } else {
            if (RwgConfig.enableLogging) {
                LOGGER.warn("Warn: Could not find a valid neutral zone spawn point. Falling back to vanilla (0, 0).");
            }
        }
    }

    private static BlockPos findNeutralZoneSpawn(ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BiomeSource biomeSource = generator.getBiomeSource();
        RandomState randomState = level.getChunkSource().randomState();
        Climate.Sampler sampler = randomState.sampler();
        int sampleY = level.getSeaLevel();
        for (int i = 0; i < MAX_BIOME_SAMPLES; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = NEUTRAL_ZONE_MIN + RANDOM.nextDouble() * (NEUTRAL_ZONE_MAX - NEUTRAL_ZONE_MIN);
            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);
            if (!isAllowedBiomeAt(biomeSource, sampler, x, z, sampleY)) continue;
            BlockPos spawn = validateCandidate(level, generator, randomState, x, z);
            if (spawn != null) return spawn;
        }
        return null;
    }

    private static boolean isAllowedBiomeAt(BiomeSource source, Climate.Sampler sampler, int x, int z, int sampleY) {
        Holder<Biome> holder = source.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(sampleY), QuartPos.fromBlock(z), sampler);
        ResourceLocation biomeId = holder.unwrapKey().map(k -> k.location()).orElse(null);
        return RwgConfig.isAllowedSpawnBiome(biomeId);
    }

    private static BlockPos validateCandidate(ServerLevel level, ChunkGenerator generator,
                                              RandomState randomState, int x, int z) {
        int surfaceY = generator.getBaseHeight(
                x, z, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level, randomState);
        if (surfaceY <= level.getMinBuildHeight() || surfaceY >= level.getMaxBuildHeight()) return null;

        NoiseColumn column = generator.getBaseColumn(x, z, level, randomState);
        if (column == null) return null;

        BlockState at = column.getBlock(surfaceY);
        BlockState below = column.getBlock(surfaceY - 1);
        if (!at.getFluidState().isEmpty()) return null;
        if (!below.isSolid()) return null;

        ResourceLocation biomeName = generator.getBiomeSource().getNoiseBiome(
                        QuartPos.fromBlock(x), QuartPos.fromBlock(surfaceY), QuartPos.fromBlock(z),
                        randomState.sampler())
                .unwrapKey().map(k -> k.location()).orElse(null);
        if (!RwgConfig.isAllowedSpawnBiome(biomeName)) return null;
        if (!isFlatSurface(level, generator, randomState, x, z, surfaceY)) return null;
        return new BlockPos(x, surfaceY, z);
    }

    private static boolean isFlatSurface(ServerLevel level, ChunkGenerator generator,
                                         RandomState randomState, int x, int z, int centerY) {
        int[][] probes = {
                {FLATNESS_RADIUS, 0}, {-FLATNESS_RADIUS, 0},
                {0, FLATNESS_RADIUS}, {0, -FLATNESS_RADIUS},
                {FLATNESS_RADIUS, FLATNESS_RADIUS}, {-FLATNESS_RADIUS, -FLATNESS_RADIUS},
                {FLATNESS_RADIUS, -FLATNESS_RADIUS}, {-FLATNESS_RADIUS, FLATNESS_RADIUS}
        };
        for (int[] p : probes) {
            int sx = x + p[0];
            int sz = z + p[1];
            int y = generator.getBaseHeight(
                    sx, sz, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, level, randomState);
            if (Math.abs(y - centerY) > MAX_FLATNESS_VARIANCE) return false;
        }
        return true;
    }
}