package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.rorcik.rwg.RwgConfig;

import java.util.function.Function;

@Mixin(CanyonWorldCarver.class)
public class CanyonWorldCarverMixin {

    /**
     * Отключаем каньоны/ravines
     */
    @Inject(
            method = "carve",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rwg$disableCanyons(
            CarvingContext context,
            CanyonCarverConfiguration config,
            ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biomeAccessor,
            RandomSource random,
            Aquifer aquifer,
            ChunkPos chunkPos,
            CarvingMask carvingMask,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (RwgConfig.disableCanyons) {
            cir.setReturnValue(false);
        }
    }
}