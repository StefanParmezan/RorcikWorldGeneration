package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgConfig;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
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
    private static void overrideInitialSpawn(
            ServerLevel level,
            ServerLevelData levelData,
            boolean generateBonusChest,
            boolean debug,
            CallbackInfo ci
    ) {
        if (!RwgConfig.applyJoinWorldSpawnMixin) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        BlockPos customSpawn = findNeutralZoneSpawn(level);
        if (customSpawn != null) {
            level.setDefaultSpawnPos(customSpawn, 0.0f);
            LOGGER.info("[RWG-SPAWN] Successfully relocated player spawn to neutral zone at: {}", customSpawn.toShortString());
            ci.cancel();
        } else {
            LOGGER.warn("[RWG-SPAWN] Could not find a valid neutral zone spawn point. Falling back to vanilla (0, 0).");
        }
    }

    private static BlockPos findNeutralZoneSpawn(ServerLevel level) {
        BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        int sampleY = level.getSeaLevel();

        for (int i = 0; i < MAX_BIOME_SAMPLES; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = NEUTRAL_ZONE_MIN + RANDOM.nextDouble() * (NEUTRAL_ZONE_MAX - NEUTRAL_ZONE_MIN);
            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            if (!isAllowedBiomeAt(biomeSource, sampler, x, z, sampleY)) continue;

            BlockPos spawn = validateCandidate(level, x, z);
            if (spawn != null) return spawn;
        }
        return null;
    }

    private static boolean isAllowedBiomeAt(BiomeSource source, Climate.Sampler sampler, int x, int z, int sampleY) {
        Holder<Biome> holder = source.getNoiseBiome(
                QuartPos.fromBlock(x),
                QuartPos.fromBlock(sampleY),
                QuartPos.fromBlock(z),
                sampler
        );
        ResourceLocation biomeId = holder.unwrapKey().map(k -> k.location()).orElse(null);

        return RwgConfig.isAllowedSpawnBiome(biomeId);
    }

    private static BlockPos validateCandidate(ServerLevel level, int x, int z) {
        ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.SURFACE, true);
        if (chunk == null) return null;

        int surfaceY = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15);
        BlockPos candidate = new BlockPos(x, surfaceY, z);

        if (!level.getBlockState(candidate).getFluidState().isEmpty()) return null;
        if (!level.getBlockState(candidate.below()).isSolid()) return null;

        ResourceLocation biomeName = level.getBiome(candidate).unwrapKey().map(k -> k.location()).orElse(null);

        if (!RwgConfig.isAllowedSpawnBiome(biomeName)) return null;

        if (!isFlatSurface(level, x, z, surfaceY)) return null;

        return candidate;
    }

    private static boolean isFlatSurface(ServerLevel level, int x, int z, int centerY) {
        int[][] probes = {
                {FLATNESS_RADIUS, 0}, {-FLATNESS_RADIUS, 0},
                {0, FLATNESS_RADIUS}, {0, -FLATNESS_RADIUS},
                {FLATNESS_RADIUS, FLATNESS_RADIUS}, {-FLATNESS_RADIUS, -FLATNESS_RADIUS},
                {FLATNESS_RADIUS, -FLATNESS_RADIUS}, {-FLATNESS_RADIUS, FLATNESS_RADIUS}
        };
        for (int[] p : probes) {
            int sx = x + p[0];
            int sz = z + p[1];
            ChunkAccess chunk = level.getChunk(sx >> 4, sz >> 4, ChunkStatus.SURFACE, false);
            if (chunk == null) return false;
            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sx & 15, sz & 15);
            if (Math.abs(y - centerY) > MAX_FLATNESS_VARIANCE) return false;
        }
        return true;
    }
}