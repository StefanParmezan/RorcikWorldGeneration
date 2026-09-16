package ru.rorcik.rwg.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.worldgen.carver.RwgCaveCarverHelper;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorCarverMixin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasLogged = false;

    /**
     * После ванильного карвинга применяем наши старые пещеры
     */
    @Inject(
            method = "applyCarvers",
            at = @At("TAIL")
    )
    private void rwg$applyOldCaves(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step,
            CallbackInfo ci
    ) {
        if (!RwgConfig.enableOldCaves) {
            return;
        }

        if (step == GenerationStep.Carving.AIR) {
            if (!hasLogged) {
                LOGGER.warn("[RWG] Applying old-style cave carving");
                hasLogged = true;
            }

            RwgCaveCarverHelper.carveChunk(region, seed, randomState, biomeManager, structureManager, chunk);
        }
    }
}