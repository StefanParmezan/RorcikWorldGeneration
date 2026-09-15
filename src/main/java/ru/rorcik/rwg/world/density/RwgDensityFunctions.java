package ru.rorcik.rwg.world.density;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = "rwg", bus = Mod.EventBusSubscriber.Bus.MOD)
public class RwgDensityFunctions {
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(
                Registries.DENSITY_FUNCTION_TYPE,
                ResourceLocation.fromNamespaceAndPath("rwg", "z_based_temperature"),
                () -> ZBasedTemperature.CODEC
        );
    }
}