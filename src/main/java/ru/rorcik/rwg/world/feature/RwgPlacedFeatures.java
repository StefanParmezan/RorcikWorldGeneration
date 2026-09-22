package ru.rorcik.rwg.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.RwgMod;

import java.util.List;

public class RwgPlacedFeatures {

    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES =
            DeferredRegister.create(Registries.PLACED_FEATURE, RwgMod.MODID);

    public static final RegistryObject<PlacedFeature> SLAB_SMOOTHING =
            PLACED_FEATURES.register("slab_smoothing", () -> new PlacedFeature(
                    RwgConfiguredFeatures.SLAB_SMOOTHING.getHolder().orElseThrow(),
                    List.of(
                            CountPlacement.of(1),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            ));

    public static final RegistryObject<PlacedFeature> WILD_FARM =
            PLACED_FEATURES.register("wild_farm", () -> new PlacedFeature(
                    RwgConfiguredFeatures.WILD_FARM.getHolder().orElseThrow(),
                    List.of(
                            RarityFilter.onAverageOnceEvery(7),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            ));

    public static final RegistryObject<PlacedFeature> BOULDER =
            PLACED_FEATURES.register("boulder", () -> new PlacedFeature(
                    RwgConfiguredFeatures.BOULDER.getHolder().orElseThrow(),
                    List.of(
                            RarityFilter.onAverageOnceEvery(33),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            ));

    public static final RegistryObject<PlacedFeature> FALLEN_LOG =
            PLACED_FEATURES.register("fallen_log", () -> new PlacedFeature(
                    RwgConfiguredFeatures.FALLEN_LOG.getHolder().orElseThrow(),
                    List.of(
                            RarityFilter.onAverageOnceEvery(33),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            ));

    public static final RegistryObject<PlacedFeature> RIVER_VEGETATION =
            PLACED_FEATURES.register("river_vegetation", () -> new PlacedFeature(
                    RwgConfiguredFeatures.RIVER_VEGETATION.getHolder().orElseThrow(),
                    List.of(
                            CountPlacement.of(4),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            ));

    public static final RegistryObject<PlacedFeature> MOSS_CARPET =
            PLACED_FEATURES.register("moss_carpet", () -> new PlacedFeature(
                    RwgConfiguredFeatures.MOSS_CARPET.getHolder().orElseThrow(),
                    List.of(
                            CountPlacement.of(2),
                            InSquarePlacement.spread(),
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            BiomeFilter.biome()
                    )
            ));
}