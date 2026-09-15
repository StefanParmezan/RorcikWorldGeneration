package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldCarver.class)
public class CaveWorldCarverMixin {

    private static final int SURFACE_BUFFER = 10;

    private static final int RADIUS = 4;

    @Redirect(
            method = "carveBlock(Lnet/minecraft/world/level/levelgen/carver/CarvingContext;" +
                    "Lnet/minecraft/world/level/levelgen/carver/CarverConfiguration;" +
                    "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
                    "Ljava/util/function/Function;" +
                    "Lnet/minecraft/world/level/chunk/CarvingMask;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;" +
                            "setBlockState(Lnet/minecraft/core/BlockPos;" +
                            "Lnet/minecraft/world/level/block/state/BlockState;Z)" +
                            "Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private BlockState rwg$preventSurfaceCarving(
            ChunkAccess chunk,
            BlockPos pos,
            BlockState state,
            boolean moved
    ) {
        int localX = pos.getX() & 15;
        int localZ = pos.getZ() & 15;

        int maxSurface = Integer.MIN_VALUE;

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            int x = localX + dx;
            if (x < 0 || x > 15) continue;

            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                int z = localZ + dz;
                if (z < 0 || z > 15) continue;

                int h = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (h > maxSurface) {
                    maxSurface = h;
                }
            }
        }

        if (pos.getY() > maxSurface - SURFACE_BUFFER) {
            return chunk.getBlockState(pos);
        }

        return chunk.setBlockState(pos, state, moved);
    }
}