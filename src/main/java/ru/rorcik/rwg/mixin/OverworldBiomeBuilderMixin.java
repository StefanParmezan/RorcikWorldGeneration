package ru.rorcik.rwg.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.rorcik.rwg.RwgConfig;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {

    @ModifyVariable(
            method = "addBiomes",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> rwg$filterVanilla(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> original) {

        return pair -> {
            String name = pair.getSecond().location().toString();

            if (RwgConfig.blacklistedBiomes != null && RwgConfig.blacklistedBiomes.contains(name)) {
                return;
            }
            if (RwgConfig.ZONED_BIOME_IDS.contains(name)) {
                return;
            }

            original.accept(pair);
        };
    }
}