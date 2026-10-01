package ru.rorcik.rwg.mixin;

import net.minecraft.core.Holder;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.rorcik.rwg.RwgConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Уменьшает частоту спавна руд.
 *
 * divisor разбивается на два множителя sqrtDiv × sqrtDiv:
 *   - CountPlacement: originalCount / sqrtDiv
 *   - RarityFilter: 1 / sqrtDiv (добавляется после count)
 *
 * Итоговое уменьшение = divisor раз.
 *
 * Масштабирование выполняется лениво — при первом вызове placeWithBiomeCheck,
 * когда Holder уже привязан к реестру.
 */
@Mixin(PlacedFeature.class)
public abstract class PlacedFeatureMixin {

    @Shadow @Final @Mutable
    public List<PlacementModifier> placement;

    @Shadow @Final
    public Holder<ConfiguredFeature<?, ?>> feature;

    private boolean rwg$scaled = false;

    @Inject(method = "placeWithBiomeCheck", at = @At("HEAD"))
    private void rwg$scaleOnFirstUse(CallbackInfoReturnable<Boolean> cir) {
        if (rwg$scaled) return;
        synchronized (this) {
            if (rwg$scaled) return;
            rwg$scaled = true;
            rwg$scaleNow();
        }
    }

    private void rwg$scaleNow() {
        double divisor = RwgConfig.oreFrequencyDivisor;
        if (divisor <= 1.0) return;

        ConfiguredFeature<?, ?> cf;
        try {
            cf = feature.value();
        } catch (Throwable t) {
            return;
        }
        if (!(cf.config() instanceof OreConfiguration)) return;

        int sqrtDiv = Math.max(1, (int) Math.round(Math.sqrt(divisor)));

        List<PlacementModifier> scaled = new ArrayList<>(placement.size() + 1);
        for (PlacementModifier pm : placement) {
            if (pm instanceof CountPlacement cp) {
                CountPlacement replaced = rwg$scaleCount(cp, sqrtDiv);
                scaled.add(replaced != null ? replaced : cp);
            } else {
                scaled.add(pm);
            }
        }

        if (sqrtDiv > 1) {
            scaled.add(1, RarityFilter.onAverageOnceEvery(sqrtDiv));
        }

        this.placement = scaled;
    }

    private static CountPlacement rwg$scaleCount(CountPlacement original, int sqrtDiv) {
        IntProvider provider = ((CountPlacementAccessor) original).rwg$getCount();
        if (!(provider instanceof ConstantInt ci)) return null;
        int value = ci.getValue();
        int scaledValue = Math.max(1, (int) Math.round((double) value / sqrtDiv));
        if (scaledValue == value) return null;
        return CountPlacement.of(scaledValue);
    }
}