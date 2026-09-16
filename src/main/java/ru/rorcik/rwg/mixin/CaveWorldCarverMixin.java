package ru.rorcik.rwg.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.rorcik.rwg.RwgConfig;

import java.util.function.Function;

@Mixin(CaveWorldCarver.class)
public class CaveWorldCarverMixin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean hasLogged = false;

    @Inject(
            method = "carve",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rwg$disableVanillaCaves(
            CarvingContext context,
            CaveCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask carvingMask,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!hasLogged) {
            LOGGER.warn("[RWG] Vanilla cave carver intercepted! Config: enableOldCaves={}, disableVanillaCaves={}",
                    RwgConfig.enableOldCaves, RwgConfig.disableVanillaCaves);
            hasLogged = true;
        }

        if (RwgConfig.disableVanillaCaves && RwgConfig.enableOldCaves) {
            cir.setReturnValue(false);
        }
    }
}