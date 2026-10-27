package ru.rorcik.rwg.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.slf4j.Logger;
import ru.rorcik.core.zone.BiomeZoneRegistry;
import ru.rorcik.core.zone.IBiomeZoneProvider;
import ru.rorcik.core.zone.TemperatureZone;
import ru.rorcik.rwg.RwgBiomes;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.RwgMod;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class RwgTerrablender {
    private static final Logger LOG = LogUtils.getLogger();

    public static void init() {
        BiomeZoneRegistry.register(new RwgBiomeZoneProvider());
        Regions.register(new RwgRegion(
                ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "overworld"),
                RegionType.OVERWORLD,
                100
        ));

        registerCitySurfaceRules();

        if (RwgConfig.enableLogging) {
            LOG.info("Info: Terrablender region, BiomeZoneProvider and Surface Rules registered successfully.");
        }
    }

    private static void registerCitySurfaceRules() {
        
        SurfaceRules.RuleSource plainsCityRules = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(RwgBiomes.PLAINS_CITY),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.DIRT.defaultBlockState()))
                        )
                )
        );

        SurfaceRules.RuleSource desertCityRules = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(RwgBiomes.DESERT_CITY),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.SAND.defaultBlockState())),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.SAND.defaultBlockState()))
                        )
                )
        );

        SurfaceRules.RuleSource combinedCityRules = SurfaceRules.sequence(plainsCityRules, desertCityRules);

        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, RwgMod.MODID, combinedCityRules);
    }

    public static class RwgBiomeZoneProvider implements IBiomeZoneProvider {
        @Override
        public Optional<TemperatureZone> getZone(ResourceLocation biomeId) {
            if (biomeId == null) return Optional.empty();
            String idStr = biomeId.toString();

            for (Map.Entry<String, List<? extends String>> entry : RwgConfig.ZONE_BIOMES.entrySet()) {
                String zoneKey = entry.getKey();
                List<? extends String> biomeIds = entry.getValue();
                if (biomeIds == null || biomeIds.isEmpty()) continue;

                if (matchesBiome(biomeIds, biomeId)) {
                    String[] parts = zoneKey.split("\\.");
                    if (parts.length >= 1) {
                        return Optional.ofNullable(parseTemperatureZone(parts[0]));
                    }
                }
            }
            return Optional.of(getFallbackZoneByTemperature(biomeId));
        }

        private boolean matchesBiome(List<? extends String> list, ResourceLocation biomeId) {
            if (list == null || biomeId == null) return false;
            String idStr = biomeId.toString();
            for (String entry : list) {
                if (entry == null || entry.isEmpty()) continue;
                if ("*".equals(entry)) return true;
                if (entry.equals(idStr)) return true;
            }
            return false;
        }

        private TemperatureZone parseTemperatureZone(String tempKey) {
            return switch (tempKey.toLowerCase()) {
                case "extreme_cold" -> TemperatureZone.EXTREME_COLD;
                case "cold" -> TemperatureZone.COLD;
                case "cool" -> TemperatureZone.COOL;
                case "neutral", "river" -> TemperatureZone.NEUTRAL;
                case "warm" -> TemperatureZone.WARM;
                case "hot" -> TemperatureZone.HOT;
                case "extreme_hot" -> TemperatureZone.EXTREME_HOT;
                default -> TemperatureZone.NEUTRAL;
            };
        }

        private TemperatureZone getFallbackZoneByTemperature(ResourceLocation biomeId) {
            String id = biomeId.toString().toLowerCase();

            if (id.contains("frozen_river") || id.contains("frozen_ocean")) {
                return TemperatureZone.COLD;
            }
            if (id.contains("snowy") || id.contains("frozen") || id.contains("ice") || id.contains("grove")) {
                return TemperatureZone.COLD;
            }
            if (id.contains("desert") || id.contains("badlands") || id.contains("savanna")) {
                return TemperatureZone.HOT;
            }
            if (id.contains("taiga") || id.contains("forest") || id.contains("jungle")) {
                return TemperatureZone.COOL;
            }
            if (id.contains("river")) {
                return TemperatureZone.NEUTRAL;
            }
            return TemperatureZone.NEUTRAL;
        }
    }

    public static class RwgRegion extends Region {
        private static final Climate.Parameter EXTREME_COLD = Climate.Parameter.span(-1.0f, -0.7f);
        private static final Climate.Parameter COLD         = Climate.Parameter.span(-0.7f, -0.4f);
        private static final Climate.Parameter COOL         = Climate.Parameter.span(-0.4f, -0.1f);
        private static final Climate.Parameter NEUTRAL      = Climate.Parameter.span(-0.1f,  0.2f);
        private static final Climate.Parameter WARM         = Climate.Parameter.span( 0.2f,  0.5f);
        private static final Climate.Parameter HOT          = Climate.Parameter.span( 0.5f,  0.8f);
        private static final Climate.Parameter EXTREME_HOT  = Climate.Parameter.span( 0.8f,  1.0f);
        private static final Climate.Parameter FULL_RANGE   = Climate.Parameter.span(-1.0f,  1.0f);
        private static final Climate.Parameter DEEP_OCEAN   = Climate.Parameter.span(-1.0f, -0.45f);
        private static final Climate.Parameter OCEAN        = Climate.Parameter.span(-0.45f, -0.19f);
        private static final Climate.Parameter COAST        = Climate.Parameter.span(-0.19f, -0.11f);
        private static final Climate.Parameter INLAND       = Climate.Parameter.span( 0.03f,  0.3f);
        private static final Climate.Parameter HILLS        = Climate.Parameter.span( 0.3f,  1.0f);
        private static final Climate.Parameter RIVER_WEIRDNESS = Climate.Parameter.span(-0.24f, 0.24f);
        private static final float HUMIDITY_MIN = -1.0f;
        private static final float HUMIDITY_MAX =  1.0f;

        public RwgRegion(ResourceLocation name, RegionType type, int uniqueness) {
            super(name, type, uniqueness);
        }

        @Override
        public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            Map<String, List<? extends String>> zones = RwgConfig.ZONE_BIOMES;
            if (!RwgConfig.biomeZoningEnabled || zones == null || zones.isEmpty()) {
                if (RwgConfig.enableLogging) {
                    LOG.warn("Warn: Biome zoning config is empty or disabled, using emergency fallback.");
                }
                addEmergencyFallback(mapper);
                return;
            }

            int placed = 0;
            int skipped = 0;
            for (Map.Entry<String, List<? extends String>> entry : zones.entrySet()) {
                String zoneKey = entry.getKey();
                List<? extends String> biomeIds = entry.getValue();
                if (biomeIds == null || biomeIds.isEmpty()) continue;

                if (zoneKey.startsWith("river.")) {
                    placed += addRivers(zoneKey, biomeIds, registry, mapper);
                    continue;
                }

                String[] parts = zoneKey.split("\\.");
                if (parts.length != 2) {
                    if (RwgConfig.enableLogging) {
                        LOG.warn("Warn: Unknown zone key: '{}' (expected 'temp.cont')", zoneKey);
                    }
                    continue;
                }
                Climate.Parameter temp = temperatureBand(parts[0]);
                Climate.Parameter cont = continentalnessBand(parts[1]);
                if (temp == null || cont == null) {
                    if (RwgConfig.enableLogging) {
                        LOG.warn("Warn: Unknown zone: '{}'", zoneKey);
                    }
                    continue;
                }
                int[] counts = addZonedBiomes(mapper, registry, temp, cont, biomeIds);
                placed += counts[0];
                skipped += counts[1];
            }

            mapper.accept(Pair.of(
                    Climate.parameters(NEUTRAL, Climate.Parameter.span(0.9f, 1.0f), INLAND, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f),
                    RwgBiomes.PLAINS_CITY
            ));
            mapper.accept(Pair.of(
                    Climate.parameters(HOT, Climate.Parameter.span(0.9f, 1.0f), INLAND, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f),
                    RwgBiomes.DESERT_CITY
            ));

            if (RwgConfig.enableLogging) {
                LOG.info("Info: Biome zoning applied: {} entries placed, {} skipped.", placed, skipped);
            }
        }

        private int[] addZonedBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper, Registry<Biome> registry, Climate.Parameter temp, Climate.Parameter cont, List<? extends String> biomeIds) {
            int n = biomeIds.size();
            if (n == 0) return new int[]{0, 0};
            if (n == 1) {
                ResourceKey<Biome> key = resolveBiome(registry, biomeIds.get(0));
                if (key == null) return new int[]{0, 1};
                mapper.accept(Pair.of(Climate.parameters(temp, FULL_RANGE, cont, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f), key));
                return new int[]{1, 0};
            }
            float step = (HUMIDITY_MAX - HUMIDITY_MIN) / n;
            int placed = 0;
            int skipped = 0;
            for (int i = 0; i < n; i++) {
                String id = biomeIds.get(i);
                ResourceKey<Biome> key = resolveBiome(registry, id);
                if (key == null) { skipped++; continue; }
                float lo = HUMIDITY_MIN + i * step;
                float hi = (i == n - 1) ? HUMIDITY_MAX : (lo + step);
                Climate.Parameter humidity = Climate.Parameter.span(lo, hi);
                mapper.accept(Pair.of(Climate.parameters(temp, humidity, cont, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f), key));
                placed++;
            }
            return new int[]{placed, skipped};
        }

        private int addRivers(String zoneKey, List<? extends String> biomeIds, Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            String kind = zoneKey.substring("river.".length());
            Climate.Parameter[] temps = switch (kind) {
                case "frozen" -> new Climate.Parameter[]{EXTREME_COLD, COLD};
                case "normal" -> new Climate.Parameter[]{COOL, NEUTRAL, WARM, HOT, EXTREME_HOT};
                default -> null;
            };
            if (temps == null) {
                if (RwgConfig.enableLogging) {
                    LOG.warn("Warn: Unknown river kind: '{}'", kind);
                }
                return 0;
            }
            int placed = 0;
            for (Climate.Parameter t : temps) {
                for (String id : biomeIds) {
                    ResourceKey<Biome> key = resolveBiome(registry, id);
                    if (key == null) continue;
                    mapper.accept(Pair.of(Climate.parameters(t, FULL_RANGE, FULL_RANGE, FULL_RANGE, RIVER_WEIRDNESS, Climate.Parameter.point(0.0f), 0.0f), key));
                    placed++;
                }
            }
            return placed;
        }

        private Climate.Parameter temperatureBand(String key) {
            return switch (key) {
                case "extreme_cold" -> EXTREME_COLD;
                case "cold" -> COLD;
                case "cool" -> COOL;
                case "neutral" -> NEUTRAL;
                case "warm" -> WARM;
                case "hot" -> HOT;
                case "extreme_hot" -> EXTREME_HOT;
                default -> null;
            };
        }

        private Climate.Parameter continentalnessBand(String key) {
            return switch (key) {
                case "inland" -> INLAND;
                case "hills" -> HILLS;
                case "coast" -> COAST;
                case "ocean" -> OCEAN;
                case "deep_ocean" -> DEEP_OCEAN;
                default -> null;
            };
        }

        private ResourceKey<Biome> resolveBiome(Registry<Biome> registry, String id) {
            if (id == null || id.isEmpty()) return null;
            List<? extends String> blacklist = RwgConfig.blacklistedBiomes;
            if (blacklist != null && blacklist.contains(id)) return null;
            ResourceLocation rl = ResourceLocation.tryParse(id);
            if (rl == null) {
                if (RwgConfig.enableLogging) {
                    LOG.warn("Warn: Invalid biome ID: '{}'", id);
                }
                return null;
            }
            ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, rl);
            if (!registry.containsKey(key)) {
                if (RwgConfig.enableLogging) {
                    LOG.warn("Warn: Biome '{}' is not registered (missing mod?), skipped.", id);
                }
                return null;
            }
            return key;
        }

        private void addEmergencyFallback(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
            mapper.accept(Pair.of(Climate.parameters(NEUTRAL, FULL_RANGE, INLAND, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f), Biomes.PLAINS));
            mapper.accept(Pair.of(Climate.parameters(HOT, FULL_RANGE, INLAND, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f), Biomes.DESERT));
            mapper.accept(Pair.of(Climate.parameters(COLD, FULL_RANGE, INLAND, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f), Biomes.SNOWY_TAIGA));
            mapper.accept(Pair.of(Climate.parameters(NEUTRAL, FULL_RANGE, COAST, FULL_RANGE, FULL_RANGE, FULL_RANGE, 0.0f), Biomes.BEACH));
        }
    }
}