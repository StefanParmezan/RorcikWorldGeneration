package ru.rorcik.rwg.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.RwgMod;
import ru.rorcik.rwg.world.feature.impl.*;

public class RwgFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, RwgMod.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SLAB_SMOOTHING =
            FEATURES.register("slab_smoothing", () -> new SlabSmoothingFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> WILD_FARM =
            FEATURES.register("wild_farm", () -> new WildFarmFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BOULDER =
            FEATURES.register("boulder", () -> new BoulderFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FALLEN_LOG =
            FEATURES.register("fallen_log", () -> new FallenLogFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> RIVER_VEGETATION =
            FEATURES.register("river_vegetation", () -> new RiverVegetationFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> EARTHY_PATCHES =
            FEATURES.register("earthy_patches", () -> new EarthyPatchesFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> RIVER_SHORE_SMOOTHING =
            FEATURES.register("river_shore_smoothing", () -> new RiverShoreSmoothingFeature(NoneFeatureConfiguration.CODEC));
}