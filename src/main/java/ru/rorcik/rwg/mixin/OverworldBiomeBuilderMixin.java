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

import java.util.List;
import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {

    @ModifyVariable(
            method = "addBiomes",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> rwg$filterBlacklisted(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> original) {

        return pair -> {
            List<? extends String> blacklist = RwgConfig.blacklistedBiomes;
            if (blacklist == null || blacklist.isEmpty()) {
                original.accept(pair);
                return;
            }
            String name = pair.getSecond().location().toString();
            if (!blacklist.contains(name)) {
                original.accept(pair);
            }
        };
    }
}