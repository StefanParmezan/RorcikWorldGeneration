package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgConfig;

import java.util.Random;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    private static final Random RANDOM = new Random();
    private static final double NEUTRAL_ZONE_MIN = 9000.0;
    private static final double NEUTRAL_ZONE_MAX = 14000.0;

    @Inject(
            method = "setInitialSpawn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void overrideInitialSpawn(
            ServerLevel level,
            ServerLevelData levelData,
            boolean generateBonusChest,
            boolean debug,
            CallbackInfo ci
    ) {
        if (!RwgConfig.applyJoinWorldSpawnMixin) {
            return;
        }

        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        BlockPos customSpawn = findNeutralZoneSpawn(level);

        level.setDefaultSpawnPos(customSpawn, 0.0f);
        ci.cancel();
    }

    private static BlockPos findNeutralZoneSpawn(ServerLevel level) {
        int maxAttempts = 100;

        for (int i = 0; i < maxAttempts; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = NEUTRAL_ZONE_MIN + RANDOM.nextDouble() * (NEUTRAL_ZONE_MAX - NEUTRAL_ZONE_MIN);

            int x = (int) (Math.cos(angle) * distance);
            int z = (int) (Math.sin(angle) * distance);

            ChunkAccess chunk = level.getChunk(x >> 4, z >> 4, ChunkStatus.FULL, true);
            if (chunk == null) {
                continue;
            }

            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15);
            BlockPos candidate = new BlockPos(x, y, z);

            BlockPos groundPos = candidate.below();
            if (!level.getBlockState(groundPos).isSolid()) {
                continue;
            }

            if (!level.getBlockState(candidate).getFluidState().isEmpty()) {
                continue;
            }

            if (!isFlatSurface(level, candidate)) {
                continue;
            }

            return candidate;
        }

        int fallbackDist = (int) ((NEUTRAL_ZONE_MIN + NEUTRAL_ZONE_MAX) / 2);
        return new BlockPos(fallbackDist, 70, 0);
    }

    private static boolean isFlatSurface(ServerLevel level, BlockPos center) {
        int centerY = center.getY();
        int radius = 3;
        int maxVariance = 2;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = center.offset(dx, 0, dz);
                ChunkAccess chunk = level.getChunk(
                        checkPos.getX() >> 4,
                        checkPos.getZ() >> 4,
                        ChunkStatus.FULL,
                        false
                );

                if (chunk == null) return false;

                int y = chunk.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        checkPos.getX() & 15,
                        checkPos.getZ() & 15
                );

                if (Math.abs(y - centerY) > maxVariance) {
                    return false;
                }
            }
        }

        return true;
    }
}