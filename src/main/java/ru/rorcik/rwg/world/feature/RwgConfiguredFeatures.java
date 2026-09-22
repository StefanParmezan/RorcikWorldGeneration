package ru.rorcik.rwg.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.RwgMod;

public class RwgConfiguredFeatures {

    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES =
            DeferredRegister.create(Registries.CONFIGURED_FEATURE, RwgMod.MODID);

    public static final RegistryObject<ConfiguredFeature<?, ?>> SLAB_SMOOTHING =
            CONFIGURED_FEATURES.register("slab_smoothing",
                    () -> new ConfiguredFeature<>(RwgFeatures.SLAB_SMOOTHING.get(), NoneFeatureConfiguration.NONE));

    public static final RegistryObject<ConfiguredFeature<?, ?>> WILD_FARM =
            CONFIGURED_FEATURES.register("wild_farm",
                    () -> new ConfiguredFeature<>(RwgFeatures.WILD_FARM.get(), NoneFeatureConfiguration.NONE));

    public static final RegistryObject<ConfiguredFeature<?, ?>> BOULDER =
            CONFIGURED_FEATURES.register("boulder",
                    () -> new ConfiguredFeature<>(RwgFeatures.BOULDER.get(), NoneFeatureConfiguration.NONE));

    public static final RegistryObject<ConfiguredFeature<?, ?>> FALLEN_LOG =
            CONFIGURED_FEATURES.register("fallen_log",
                    () -> new ConfiguredFeature<>(RwgFeatures.FALLEN_LOG.get(), NoneFeatureConfiguration.NONE));

    public static final RegistryObject<ConfiguredFeature<?, ?>> RIVER_VEGETATION =
            CONFIGURED_FEATURES.register("river_vegetation",
                    () -> new ConfiguredFeature<>(RwgFeatures.RIVER_VEGETATION.get(), NoneFeatureConfiguration.NONE));

    public static final RegistryObject<ConfiguredFeature<?, ?>> MOSS_CARPET =
            CONFIGURED_FEATURES.register("moss_carpet",
                    () -> new ConfiguredFeature<>(RwgFeatures.MOSS_CARPET.get(), NoneFeatureConfiguration.NONE));
}