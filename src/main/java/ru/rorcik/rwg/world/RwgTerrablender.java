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

            // === EXTREME_COLD ===
            addBiome(mapper, EXTREME_COLD, INLAND, FULL_RANGE, Biomes.SNOWY_PLAINS);
            addBiome(mapper, EXTREME_COLD, HILLS, FULL_RANGE, Biomes.FROZEN_PEAKS);
            addBiome(mapper, EXTREME_COLD, HILLS, FULL_RANGE, Biomes.SNOWY_SLOPES);
            addOceanBiome(mapper, EXTREME_COLD, DEEP_OCEAN, Biomes.FROZEN_OCEAN);
            addOceanBiome(mapper, EXTREME_COLD, OCEAN, Biomes.FROZEN_OCEAN);
            addCoastBiome(mapper, EXTREME_COLD, Biomes.SNOWY_PLAINS);

            // === COLD ===
            addBiome(mapper, COLD, INLAND, FULL_RANGE, Biomes.SNOWY_TAIGA);
            addBiome(mapper, COLD, HILLS, FULL_RANGE, Biomes.JAGGED_PEAKS);
            addBiome(mapper, COLD, HILLS, FULL_RANGE, Biomes.GROVE);
            addOceanBiome(mapper, COLD, DEEP_OCEAN, Biomes.COLD_OCEAN);
            addOceanBiome(mapper, COLD, OCEAN, Biomes.COLD_OCEAN);
            addCoastBiome(mapper, COLD, Biomes.SNOWY_BEACH);

            // === COOL (Тайга и её вариации) ===
            // Делим влажность на 4 непересекающихся диапазона, чтобы вариации ГАРАНТИРОВАННО спавнились
            addBiome(mapper, COOL, INLAND, Climate.Parameter.span(-1.0f, -0.5f), Biomes.OLD_GROWTH_SPRUCE_TAIGA);
            addBiome(mapper, COOL, INLAND, Climate.Parameter.span(-0.5f, 0.0f), Biomes.DARK_FOREST);
            addBiome(mapper, COOL, INLAND, Climate.Parameter.span(0.0f, 0.5f), Biomes.TAIGA);
            addBiome(mapper, COOL, INLAND, Climate.Parameter.span(0.5f, 1.0f), Biomes.OLD_GROWTH_PINE_TAIGA);

            addBiome(mapper, COOL, HILLS, FULL_RANGE, Biomes.WINDSWEPT_HILLS);
            addBiome(mapper, COOL, HILLS, FULL_RANGE, Biomes.WINDSWEPT_GRAVELLY_HILLS);
            addBiome(mapper, COOL, HILLS, FULL_RANGE, Biomes.WINDSWEPT_FOREST);
            addOceanBiome(mapper, COOL, DEEP_OCEAN, Biomes.COLD_OCEAN);
            addOceanBiome(mapper, COOL, OCEAN, Biomes.OCEAN);
            addCoastBiome(mapper, COOL, Biomes.STONY_SHORE);

            // === NEUTRAL (Равнины и Леса) ===
            // Делим влажность на 5 непересекающихся диапазонов
            addBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(-1.0f, -0.6f), Biomes.SUNFLOWER_PLAINS);
            addBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(-0.6f, -0.2f), Biomes.OLD_GROWTH_BIRCH_FOREST);
            addBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(-0.2f, 0.2f), Biomes.PLAINS);       // Равнины теперь в центре влажности
            addBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(0.2f, 0.6f), Biomes.FLOWER_FOREST);
            addBiome(mapper, NEUTRAL, INLAND, Climate.Parameter.span(0.6f, 1.0f), Biomes.FOREST);

            addBiome(mapper, NEUTRAL, HILLS, FULL_RANGE, Biomes.MEADOW);
            addOceanBiome(mapper, NEUTRAL, DEEP_OCEAN, Biomes.OCEAN);
            addOceanBiome(mapper, NEUTRAL, OCEAN, Biomes.OCEAN);
            addCoastBiome(mapper, NEUTRAL, Biomes.BEACH);

            // === WARM ===
            addBiome(mapper, WARM, INLAND, FULL_RANGE, Biomes.SAVANNA);
            addBiome(mapper, WARM, HILLS, FULL_RANGE, Biomes.SAVANNA_PLATEAU);
            addBiome(mapper, WARM, HILLS, FULL_RANGE, Biomes.WINDSWEPT_SAVANNA);
            addOceanBiome(mapper, WARM, DEEP_OCEAN, Biomes.LUKEWARM_OCEAN);
            addOceanBiome(mapper, WARM, OCEAN, Biomes.LUKEWARM_OCEAN);
            addCoastBiome(mapper, WARM, Biomes.BEACH);

            // === HOT ===
            addBiome(mapper, HOT, INLAND, FULL_RANGE, Biomes.DESERT);
            addBiome(mapper, HOT, HILLS, FULL_RANGE, Biomes.STONY_PEAKS);
            addOceanBiome(mapper, HOT, DEEP_OCEAN, Biomes.WARM_OCEAN);
            addOceanBiome(mapper, HOT, OCEAN, Biomes.WARM_OCEAN);
            addCoastBiome(mapper, HOT, Biomes.DESERT);

            // === EXTREME_HOT (Пустоши и их вариации) ===
            addBiome(mapper, EXTREME_HOT, INLAND, Climate.Parameter.span(-1.0f, 0.2f), Biomes.BADLANDS);
            addBiome(mapper, EXTREME_HOT, INLAND, Climate.Parameter.span(0.2f, 1.0f), Biomes.WOODED_BADLANDS);
            addBiome(mapper, EXTREME_HOT, HILLS, FULL_RANGE, Biomes.ERODED_BADLANDS);
            addOceanBiome(mapper, EXTREME_HOT, DEEP_OCEAN, Biomes.WARM_OCEAN);
            addOceanBiome(mapper, EXTREME_HOT, OCEAN, Biomes.WARM_OCEAN);
            addCoastBiome(mapper, EXTREME_HOT, Biomes.BADLANDS);
        }

        // Универсальный метод для inland биомов с контролем влажности
        private void addBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper,
                              Climate.Parameter temperature,
                              Climate.Parameter continentalness,
                              Climate.Parameter humidity,
                              ResourceKey<Biome> biome) {
            mapper.accept(Pair.of(
                    Climate.parameters(
                            temperature,
                            humidity,       // 2-й параметр: влажность
                            continentalness, // 3-й параметр: континентальность
                            FULL_RANGE,     // erosion
                            FULL_RANGE,     // depth
                            FULL_RANGE,     // weirdness
                            0.0f            // offset
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