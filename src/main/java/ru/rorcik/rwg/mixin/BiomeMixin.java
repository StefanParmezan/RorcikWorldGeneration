package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {
    @Inject(method = "coldEnoughToSnow", at = @At("HEAD"), cancellable = true)
    private void rwg$coldEnoughToSnow(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Biome self = (Biome) (Object) this;
        
        cir.setReturnValue(self.getBaseTemperature() < 0.15f);
        
    }
}