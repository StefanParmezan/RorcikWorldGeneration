package ru.rorcik.rwg.mixin;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.rorcik.rwg.RwgBiomes;

@Mixin(Biome.class)
public class BiomeGenerationMixin {

    @Inject(method = "getGenerationSettings", at = @At("RETURN"), cancellable = true)
    private void rwg$forceEmptyGenerationForCities(CallbackInfoReturnable<BiomeGenerationSettings> cir) {
        Biome self = (Biome) (Object) this;

        if (self == RwgBiomes.PLAINS_CITY_BIOME.get() || self == RwgBiomes.DESERT_CITY_BIOME.get()) {
            
            cir.setReturnValue(BiomeGenerationSettings.EMPTY);
        }
    }
}