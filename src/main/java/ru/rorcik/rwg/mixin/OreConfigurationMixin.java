package ru.rorcik.rwg.mixin;

import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.rorcik.rwg.RwgConfig;

@Mixin(OreConfiguration.class)
public abstract class OreConfigurationMixin {

    @ModifyVariable(
            method = "<init>(Ljava/util/List;IF)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static int rwg$scaleVeinSize(int size) {
        double mult = RwgConfig.oreVeinSizeMultiplier;
        if (mult == 1.0) return size;
        return Math.max(1, (int) Math.round(size * mult));
    }
}