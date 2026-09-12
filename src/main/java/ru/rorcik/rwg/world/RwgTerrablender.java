package ru.rorcik.rwg.world;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;

import java.util.function.Consumer;

public class RwgTerrablender {

    public static void init() {
        Regions.register(new RwgRegion(
                ResourceLocation.fromNamespaceAndPath("rwg", "overworld"),
                RegionType.OVERWORLD,
                100000
        ));
    }

    public static class RwgRegion extends Region {

        private static final Climate.Parameter EXTREME_COLD = Climate.Parameter.span(-1.0f, -0.7f);
        private static final Climate.Parameter COLD         = Climate.Parameter.span(-0.7f, -0.4f);
        private static final Climate.Parameter COOL         = Climate.Parameter.span(-0.4f, -0.1f);
        private static final Climate.Parameter NEUTRAL      = Climate.Parameter.span(-0.1f, 0.2f);
        private static final Climate.Parameter WARM         = Climate.Parameter.span(0.2f, 0.5f);
        private static final Climate.Parameter HOT          = Climate.Parameter.span(0.5f, 0.8f);
        private static final Climate.Parameter EXTREME_HOT  = Climate.Parameter.span(0.8f, 1.0f);

        private static final Climate.Parameter FULL_RANGE   = Climate.Parameter.span(-1.0f, 1.0f);
        private static final Climate.Parameter DEEP_OCEAN   = Climate.Parameter.span(-1.0f, -0.45f);
        private static final Climate.Parameter OCEAN        = Climate.Parameter.span(-0.45f, -0.19f);
        private static final Climate.Parameter COAST        = Climate.Parameter.span(-0.19f, -0.03f);
        private static final Climate.Parameter INLAND       = Climate.Parameter.span(-0.03f, 0.3f);
        private static final Climate.Parameter HILLS        = Climate.Parameter.span(0.3f, 1.0f);

        public RwgRegion(ResourceLocation name, RegionType type, int uniqueness) {
            super(name, type, uniqueness);
        }

        @Override
        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {

            addSimpleBiome(mapper, EXTREME_COLD, INLAND, Biomes.SNOWY_PLAINS);
            addSimpleBiome(mapper, EXTREME_COLD, HILLS, Biomes.FROZEN_PEAKS);
            addOceanBiome(mapper, EXTREME_COLD, DEEP_OCEAN, Biomes.FROZEN_OCEAN);
            addOceanBiome(mapper, EXTREME_COLD, OCEAN, Biomes.FROZEN_OCEAN);
            addCoastBiome(mapper, EXTREME_COLD, Biomes.SNOWY_PLAINS);

            addSimpleBiome(mapper, COLD, INLAND, Biomes.SNOWY_TAIGA);
            addSimpleBiome(mapper, COLD, HILLS, Biomes.JAGGED_PEAKS);
            addOceanBiome(mapper, COLD, DEEP_OCEAN, Biomes.COLD_OCEAN);
            addOceanBiome(mapper, COLD, OCEAN, Biomes.COLD_OCEAN);
            addCoastBiome(mapper, COLD, Biomes.SNOWY_BEACH);

            addSimpleBiome(mapper, COOL, INLAND, Biomes.TAIGA);
            addSimpleBiome(mapper, COOL, HILLS, Biomes.WINDSWEPT_HILLS);
            addOceanBiome(mapper, COOL, DEEP_OCEAN, Biomes.COLD_OCEAN);
            addOceanBiome(mapper, COOL, OCEAN, Biomes.OCEAN);
            addCoastBiome(mapper, COOL, Biomes.STONY_SHORE);
            addSimpleBiome(mapper, COOL, HILLS, Biomes.STONY_PEAKS);

            addVariantBiome(mapper, COOL, INLAND, Climate.Parameter.span(0.4f, 1.0f), Biomes.OLD_GROWTH_PINE_TAIGA);
            addVariantBiome(mapper, COOL, INLAND, Climate.Parameter.span(-1.0f, -0.5f), Biomes.DARK_FOREST);

            addSimpleBiome(mapper, NEUTRAL, INLAND, Biomes.PLAINS);
            addSimpleBiome(mapper, NEUTRAL, HILLS, Biomes.MEADOW);
            addOceanBiome(mapper, NEUTRAL, DEEP_OCEAN, Biomes.OCEAN);
            addOceanBiome(mapper, NEUTRAL, OCEAN, Biomes.OCEAN);
            addCoastBiome(mapper, NEUTRAL, Biomes.BEACH);

            addVariantBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(0.3f, 1.0f), Biomes.FOREST);
            addVariantBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(-0.3f, 0.3f), Biomes.BIRCH_FOREST);
            addVariantBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(-1.0f, -0.5f), Biomes.SUNFLOWER_PLAINS);

            addSimpleBiome(mapper, WARM, INLAND, Biomes.SAVANNA);
            addSimpleBiome(mapper, WARM, HILLS, Biomes.SAVANNA_PLATEAU);
            addOceanBiome(mapper, WARM, DEEP_OCEAN, Biomes.LUKEWARM_OCEAN);
            addOceanBiome(mapper, WARM, OCEAN, Biomes.LUKEWARM_OCEAN);
            addCoastBiome(mapper, WARM, Biomes.BEACH);

            addSimpleBiome(mapper, HOT, INLAND, Biomes.DESERT);
            addOceanBiome(mapper, HOT, DEEP_OCEAN, Biomes.WARM_OCEAN);
            addOceanBiome(mapper, HOT, OCEAN, Biomes.WARM_OCEAN);
            addCoastBiome(mapper, HOT, Biomes.DESERT);

            addSimpleBiome(mapper, EXTREME_HOT, INLAND, Biomes.BADLANDS);
            addSimpleBiome(mapper, EXTREME_HOT, HILLS, Biomes.ERODED_BADLANDS);
            addOceanBiome(mapper, EXTREME_HOT, DEEP_OCEAN, Biomes.WARM_OCEAN);
            addOceanBiome(mapper, EXTREME_HOT, OCEAN, Biomes.WARM_OCEAN);
            addCoastBiome(mapper, EXTREME_HOT, Biomes.BADLANDS);

            addVariantBiome(mapper, EXTREME_HOT, INLAND, Climate.Parameter.span(0.2f, 1.0f), Biomes.WOODED_BADLANDS);
        }

        private void addSimpleBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                                    Climate.Parameter temperature,
                                    Climate.Parameter continentalness,
                                    ResourceKey<Biome> biome) {
            mapper.accept(Pair.of(
                    Climate.parameters(
                            temperature,
                            FULL_RANGE,
                            continentalness,
                            FULL_RANGE,
                            FULL_RANGE,
                            FULL_RANGE,
                            0.0f
                    ),
                    biome
            ));
        }

        private void addVariantBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                                     Climate.Parameter temperature,
                                     Climate.Parameter continentalness,
                                     Climate.Parameter humidity,
                                     ResourceKey<Biome> biome) {
            mapper.accept(Pair.of(
                    Climate.parameters(
                            temperature,
                            humidity,
                            continentalness,
                            FULL_RANGE,
                            FULL_RANGE,
                            FULL_RANGE,
                            0.0f
                    ),
                    biome
            ));
        }

        private void addOceanBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                                   Climate.Parameter temperature,
                                   Climate.Parameter continentalness,
                                   ResourceKey<Biome> biome) {
            mapper.accept(Pair.of(
                    Climate.parameters(
                            temperature,
                            FULL_RANGE,
                            continentalness,
                            FULL_RANGE,
                            FULL_RANGE,
                            FULL_RANGE,
                            0.0f
                    ),
                    biome
            ));
        }

        private void addCoastBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                                   Climate.Parameter temperature,
                                   ResourceKey<Biome> biome) {
            mapper.accept(Pair.of(
                    Climate.parameters(
                            temperature,
                            FULL_RANGE,
                            COAST,
                            FULL_RANGE,
                            FULL_RANGE,
                            FULL_RANGE,
                            0.0f
                    ),
                    biome
            ));
        }
    }
}