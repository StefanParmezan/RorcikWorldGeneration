package ru.rorcik.rwg;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = RwgMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RwgConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BIOMES = BUILDER
            .comment("Biomes completely excluded from generation (also filters biome_zoning).")
            .defineList("blacklisted_biomes", Arrays.asList(
                    "minecraft:ocean", "minecraft:deep_ocean", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean",
                    "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:lukewarm_ocean",
                    "minecraft:deep_lukewarm_ocean", "minecraft:warm_ocean",
                    "minecraft:mushroom_fields", "minecraft:jungle", "minecraft:sparse_jungle",
                    "minecraft:bamboo_jungle", "minecraft:mangrove_swamp", "minecraft:swamp",
                    "minecraft:ice_spikes", "minecraft:cherry_grove", "minecraft:deep_dark", "minecraft:lush_caves"
            ), obj -> obj instanceof String);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PHYSICS_AFFECTED_BLOCKS = BUILDER
            .comment("Blocks that activate MineCollapse physics after a block update")
            .defineList("physics_affected_blocks", Arrays.asList(
                    "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:stone",
                    "minecraft:cobblestone", "rwg:dirt_slab"
            ), obj -> obj instanceof String);

    public static final ForgeConfigSpec.DoubleValue HIGHWAY_SPAWN_CHANCE =
            BUILDER.defineInRange("highway_spawn_chance", 0.05, 0.0, 1.0);
    public static final ForgeConfigSpec.BooleanValue APPLY_JOIN_WORLD_SPAWN_MIXIN =
            BUILDER.define("apply_join_world_spawn_mixin", true);

    public static final ForgeConfigSpec.BooleanValue ENABLE_OLD_CAVES;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_CAVES;
    public static final ForgeConfigSpec.BooleanValue DISABLE_CANYONS;
    public static final ForgeConfigSpec.IntValue CAVE_MIN_SURFACE_DISTANCE;
    public static final ForgeConfigSpec.IntValue CAVE_FREQUENCY;
    public static final ForgeConfigSpec.DoubleValue CAVE_RARITY;
    public static final ForgeConfigSpec.IntValue CAVE_MIN_Y;
    public static final ForgeConfigSpec.IntValue CAVE_MAX_Y;
    public static final ForgeConfigSpec.BooleanValue SMOOTH_CAVE_WALLS;
    public static final ForgeConfigSpec.BooleanValue EXPOSE_CAVES_IN_MOUNTAINS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EARTHY_PATCH_DECORATIONS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEAD_BUSH_ON_PODZOL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FERN_ON_COARSE_DIRT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MOSS_ON_MOSSY_COBBLESTONE;
    public static final ForgeConfigSpec.DoubleValue FROGSPAWN_INSTANT_CHANCE;
    public static final ForgeConfigSpec.DoubleValue ORE_VEIN_SIZE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue ORE_FREQUENCY_DIVISOR;
    public static final ForgeConfigSpec.BooleanValue SUPPRESS_DENSITY_WARNINGS;

    public static final ForgeConfigSpec.BooleanValue BIOME_ZONING_ENABLED;

    public static final java.util.Set<String> ZONED_BIOME_IDS = new java.util.LinkedHashSet<>();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTREME_COLD_INLAND, EXTREME_COLD_HILLS, EXTREME_COLD_COAST, EXTREME_COLD_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> COLD_INLAND, COLD_HILLS, COLD_COAST, COLD_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> COOL_INLAND, COOL_HILLS, COOL_COAST, COOL_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NEUTRAL_INLAND, NEUTRAL_HILLS, NEUTRAL_COAST, NEUTRAL_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> WARM_INLAND, WARM_HILLS, WARM_COAST, WARM_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> HOT_INLAND, HOT_HILLS, HOT_COAST, HOT_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTREME_HOT_INLAND, EXTREME_HOT_HILLS, EXTREME_HOT_COAST, EXTREME_HOT_OCEAN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> RIVER_FROZEN, RIVER_NORMAL;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_BOULDER;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_FALLEN_LOG;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_WILD_FARM;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_EARTHY_PATCHES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_RIVER_VEGETATION;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_SLAB_SMOOTHING;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> FEATURE_RIVER_SHORE_SMOOTHING;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOULDER_TYPE_COLD;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOULDER_TYPE_DESERT;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOULDER_TYPE_BADLANDS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPE_JUNGLE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPE_BIRCH;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPE_DARK_OAK;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPE_CHERRY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPE_ACACIA;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LOG_TYPE_SPRUCE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PATCH_KIND_RED_SANDY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PATCH_KIND_SANDY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PATCH_KIND_STONE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PATCH_KIND_SKIP;

    static {
        BUILDER.push("caves");
        ENABLE_OLD_CAVES = BUILDER.comment("Use old-style cave generation (pre-1.18)").define("enable_old_caves", true);
        DISABLE_VANILLA_CAVES = BUILDER.comment("Completely disable vanilla cave carvers").define("disable_vanilla_caves", true);
        DISABLE_CANYONS = BUILDER.comment("Disable large canyon/ravine generation").define("disable_canyons", true);
        CAVE_MIN_SURFACE_DISTANCE = BUILDER.defineInRange("cave_min_surface_distance", 8, 3, 32);
        CAVE_FREQUENCY = BUILDER.defineInRange("cave_frequency", 7, 1, 20);
        CAVE_RARITY = BUILDER.defineInRange("cave_rarity", 0.14, 0.01, 1.0);
        CAVE_MIN_Y = BUILDER.defineInRange("cave_min_y", -54, -64, 64);
        CAVE_MAX_Y = BUILDER.defineInRange("cave_max_y", 128, 0, 256);
        SMOOTH_CAVE_WALLS = BUILDER.define("smooth_cave_walls", true);
        EXPOSE_CAVES_IN_MOUNTAINS = BUILDER.define("expose_caves_in_mountains", true);
        BUILDER.pop();

        BUILDER.push("ores");
        ORE_VEIN_SIZE_MULTIPLIER = BUILDER.defineInRange("ore_vein_size_multiplier", 2.5, 0.1, 10.0);
        ORE_FREQUENCY_DIVISOR = BUILDER.defineInRange("ore_frequency_divisor", 150.0, 1.0, 1000.0);
        BUILDER.pop();

        BUILDER.push("logging");
        SUPPRESS_DENSITY_WARNINGS = BUILDER.define("suppress_density_warnings", true);
        BUILDER.pop();

        BUILDER.push("vegetation");
        ENABLE_EARTHY_PATCH_DECORATIONS = BUILDER.define("enable_earthy_patch_decorations", true);
        ENABLE_DEAD_BUSH_ON_PODZOL = BUILDER.define("enable_dead_bush_on_podzol", true);
        ENABLE_FERN_ON_COARSE_DIRT = BUILDER.define("enable_fern_on_coarse_dirt", true);
        ENABLE_MOSS_ON_MOSSY_COBBLESTONE = BUILDER.define("enable_moss_on_mossy_cobblestone", true);
        FROGSPAWN_INSTANT_CHANCE = BUILDER.defineInRange("frogspawn_instant_chance", 0.15, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("biome_zoning");
        BIOME_ZONING_ENABLED = BUILDER
                .comment("Enable explicit biome zoning based on ConcentricTemperature rings.")
                .define("enabled", true);

        BUILDER.push("extreme_cold");
        EXTREME_COLD_INLAND = defineBiomeList("inland", "minecraft:snowy_plains");
        EXTREME_COLD_HILLS = defineBiomeList("hills", "minecraft:frozen_peaks", "minecraft:snowy_slopes");
        EXTREME_COLD_COAST = defineBiomeList("coast", "minecraft:snowy_plains", "minecraft:snowy_beach");
        EXTREME_COLD_OCEAN = defineBiomeList("ocean", "minecraft:snowy_plains");
        BUILDER.pop();

        BUILDER.push("cold");
        COLD_INLAND = defineBiomeList("inland", "minecraft:snowy_taiga");
        COLD_HILLS = defineBiomeList("hills", "minecraft:jagged_peaks", "minecraft:grove");
        COLD_COAST = defineBiomeList("coast", "minecraft:snowy_beach");
        COLD_OCEAN = defineBiomeList("ocean", "minecraft:snowy_taiga");
        BUILDER.pop();

        BUILDER.push("cool");
        COOL_INLAND = defineBiomeList("inland", "minecraft:old_growth_spruce_taiga", "minecraft:old_growth_pine_taiga", "minecraft:taiga", "minecraft:dark_forest");
        COOL_HILLS = defineBiomeList("hills", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills", "minecraft:windswept_forest");
        COOL_COAST = defineBiomeList("coast", "minecraft:stony_shore");
        COOL_OCEAN = defineBiomeList("ocean", "minecraft:taiga");
        BUILDER.pop();

        BUILDER.push("neutral");
        NEUTRAL_INLAND = defineBiomeList("inland", "minecraft:sunflower_plains", "minecraft:old_growth_birch_forest", "minecraft:plains", "minecraft:flower_forest", "minecraft:forest");
        NEUTRAL_HILLS = defineBiomeList("hills", "minecraft:meadow");
        NEUTRAL_COAST = defineBiomeList("coast", "minecraft:beach");
        NEUTRAL_OCEAN = defineBiomeList("ocean", "minecraft:plains");
        BUILDER.pop();

        BUILDER.push("warm");
        WARM_INLAND = defineBiomeList("inland", "minecraft:savanna");
        WARM_HILLS = defineBiomeList("hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna");
        WARM_COAST = defineBiomeList("coast", "minecraft:beach");
        WARM_OCEAN = defineBiomeList("ocean", "minecraft:savanna");
        BUILDER.pop();

        BUILDER.push("hot");
        HOT_INLAND = defineBiomeList("inland", "minecraft:desert");
        HOT_HILLS = defineBiomeList("hills", "minecraft:stony_peaks");
        HOT_COAST = defineBiomeList("coast", "minecraft:desert");
        HOT_OCEAN = defineBiomeList("ocean", "minecraft:desert");
        BUILDER.pop();

        BUILDER.push("extreme_hot");
        EXTREME_HOT_INLAND = defineBiomeList("inland", "minecraft:badlands", "minecraft:wooded_badlands");
        EXTREME_HOT_HILLS = defineBiomeList("hills", "minecraft:eroded_badlands");
        EXTREME_HOT_COAST = defineBiomeList("coast", "minecraft:badlands");
        EXTREME_HOT_OCEAN = defineBiomeList("ocean", "minecraft:badlands");
        BUILDER.pop();

        BUILDER.push("rivers");
        RIVER_FROZEN = defineBiomeList("frozen", "minecraft:frozen_river");
        RIVER_NORMAL = defineBiomeList("normal", "minecraft:river");
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("feature_biomes");
        FEATURE_BOULDER = defineBiomeList("boulder",
                "minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest", "minecraft:flower_forest",
                "minecraft:birch_forest", "minecraft:old_growth_birch_forest", "minecraft:dark_forest",
                "minecraft:taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
                "minecraft:meadow", "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills",
                "minecraft:windswept_forest", "minecraft:savanna", "minecraft:savanna_plateau",
                "minecraft:windswept_savanna", "minecraft:stony_peaks", "minecraft:desert", "minecraft:beach",
                "minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands",
                "minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:snowy_beach",
                "minecraft:ice_spikes", "minecraft:grove", "minecraft:snowy_slopes",
                "minecraft:jagged_peaks", "minecraft:frozen_peaks",
                "minecraft:river", "minecraft:frozen_river");

        FEATURE_FALLEN_LOG = defineBiomeList("fallen_log",
                "minecraft:forest", "minecraft:flower_forest", "minecraft:birch_forest",
                "minecraft:old_growth_birch_forest", "minecraft:dark_forest", "minecraft:taiga",
                "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
                "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow",
                "minecraft:cherry_grove", "minecraft:windswept_forest", "minecraft:windswept_hills",
                "minecraft:windswept_gravelly_hills", "minecraft:grove",
                "minecraft:river", "minecraft:frozen_river");

        FEATURE_WILD_FARM = defineBiomeList("wild_farm",
                "minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest",
                "minecraft:flower_forest", "minecraft:birch_forest", "minecraft:meadow", "minecraft:taiga");

        FEATURE_EARTHY_PATCHES = defineBiomeList("earthy_patches",
                "minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest", "minecraft:flower_forest",
                "minecraft:birch_forest", "minecraft:old_growth_birch_forest", "minecraft:dark_forest",
                "minecraft:meadow", "minecraft:cherry_grove",
                "minecraft:taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
                "minecraft:windswept_forest", "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills",
                "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:windswept_savanna",
                "minecraft:desert", "minecraft:beach",
                "minecraft:badlands", "minecraft:wooded_badlands", "minecraft:eroded_badlands",
                "minecraft:stony_peaks", "minecraft:jagged_peaks", "minecraft:frozen_peaks",
                "minecraft:snowy_slopes", "minecraft:grove", "minecraft:stony_shore",
                "minecraft:river", "minecraft:frozen_river");

        FEATURE_RIVER_VEGETATION = defineBiomeList("river_vegetation", "*");
        FEATURE_SLAB_SMOOTHING = defineBiomeList("slab_smoothing", "*");
        FEATURE_RIVER_SHORE_SMOOTHING = defineBiomeList("river_shore_smoothing",
                "minecraft:river", "minecraft:frozen_river", "minecraft:plains", "minecraft:sunflower_plains",
                "minecraft:forest", "minecraft:flower_forest", "minecraft:birch_forest", "minecraft:old_growth_birch_forest",
                "minecraft:dark_forest", "minecraft:taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
                "minecraft:meadow", "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:windswept_savanna",
                "minecraft:desert", "minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:grove",
                "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills");
        BUILDER.pop();

        BUILDER.push("behavior_mapping");
        BUILDER.push("boulder_types");
        BOULDER_TYPE_COLD = defineBiomeList("cold", "minecraft:snowy_plains", "minecraft:snowy_taiga", "minecraft:snowy_beach", "minecraft:ice_spikes", "minecraft:frozen_river", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:snowy_slopes", "minecraft:jagged_peaks", "minecraft:frozen_peaks", "minecraft:grove");
        BOULDER_TYPE_DESERT = defineBiomeList("desert", "minecraft:desert", "minecraft:beach");
        BOULDER_TYPE_BADLANDS = defineBiomeList("badlands", "#minecraft:is_badlands");
        BUILDER.pop();

        BUILDER.push("log_types");
        LOG_TYPE_JUNGLE = defineBiomeList("jungle", "#minecraft:is_jungle");
        LOG_TYPE_BIRCH = defineBiomeList("birch", "minecraft:birch_forest", "minecraft:old_growth_birch_forest");
        LOG_TYPE_DARK_OAK = defineBiomeList("dark_oak", "minecraft:dark_forest");
        LOG_TYPE_CHERRY = defineBiomeList("cherry", "minecraft:cherry_grove");
        LOG_TYPE_ACACIA = defineBiomeList("acacia", "#minecraft:is_savanna");
        LOG_TYPE_SPRUCE = defineBiomeList("spruce", "#minecraft:is_taiga", "minecraft:grove", "minecraft:snowy_taiga", "minecraft:frozen_river");
        BUILDER.pop();

        BUILDER.push("patch_kinds");
        PATCH_KIND_RED_SANDY = defineBiomeList("red_sandy", "#minecraft:is_badlands");
        PATCH_KIND_SANDY = defineBiomeList("sandy", "minecraft:desert", "minecraft:beach");
        PATCH_KIND_STONE = defineBiomeList("stone", "#minecraft:is_mountain", "minecraft:stony_peaks", "minecraft:jagged_peaks", "minecraft:frozen_peaks", "minecraft:snowy_slopes", "minecraft:grove", "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills", "minecraft:windswept_forest", "minecraft:stony_shore");
        PATCH_KIND_SKIP = defineBiomeList("skip", "minecraft:river", "minecraft:frozen_river", "minecraft:ocean", "minecraft:deep_ocean", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:warm_ocean");
        BUILDER.pop();
        BUILDER.pop();
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> defineBiomeList(String name, String... defaults) {
        return BUILDER.comment(
                        "Biome IDs, tags ('#minecraft:is_forest'), or '*' for ALL biomes.",
                        "ORDER MATTERS for biome_zoning: humidity [-1..1] is split into N equal bands, biome[i] gets band[i].",
                        "First entry = driest, last entry = wettest.")
                .defineList(name, Arrays.asList(defaults), obj -> obj instanceof String);
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static List<? extends String> blacklistedBiomes;
    public static List<? extends String> physicsAffectedBlocks;
    public static double highwaySpawnChance;
    public static boolean applyJoinWorldSpawnMixin;
    public static boolean enableOldCaves, disableVanillaCaves, disableCanyons;
    public static int caveMinSurfaceDistance, caveFrequency, caveMinY, caveMaxY;
    public static double caveRarity;
    public static boolean smoothCaveWalls, exposeCavesInMountains;
    public static boolean enableEarthyPatchDecorations, enableDeadBushOnPodzol, enableFernOnCoarseDirt, enableMossOnMossyCobblestone;
    public static double frogspawnInstantChance;
    public static double oreVeinSizeMultiplier = 1.0, oreFrequencyDivisor = 1.0;
    public static boolean suppressDensityWarnings;
    public static boolean biomeZoningEnabled;

    public static final Map<String, List<? extends String>> ZONE_BIOMES = new LinkedHashMap<>();
    public static final Map<String, List<? extends String>> FEATURE_BIOMES = new LinkedHashMap<>();
    public static final Map<String, List<? extends String>> BOULDER_TYPE_MAP = new LinkedHashMap<>();
    public static final Map<String, List<? extends String>> LOG_TYPE_MAP = new LinkedHashMap<>();
    public static final Map<String, List<? extends String>> PATCH_KIND_MAP = new LinkedHashMap<>();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        blacklistedBiomes = BLACKLISTED_BIOMES.get();
        physicsAffectedBlocks = PHYSICS_AFFECTED_BLOCKS.get();
        highwaySpawnChance = HIGHWAY_SPAWN_CHANCE.get();
        applyJoinWorldSpawnMixin = APPLY_JOIN_WORLD_SPAWN_MIXIN.get();
        enableOldCaves = ENABLE_OLD_CAVES.get();
        disableVanillaCaves = DISABLE_VANILLA_CAVES.get();
        disableCanyons = DISABLE_CANYONS.get();
        caveMinSurfaceDistance = CAVE_MIN_SURFACE_DISTANCE.get();
        caveFrequency = CAVE_FREQUENCY.get();
        caveRarity = CAVE_RARITY.get();
        caveMinY = CAVE_MIN_Y.get();
        caveMaxY = CAVE_MAX_Y.get();
        smoothCaveWalls = SMOOTH_CAVE_WALLS.get();
        exposeCavesInMountains = EXPOSE_CAVES_IN_MOUNTAINS.get();
        enableEarthyPatchDecorations = ENABLE_EARTHY_PATCH_DECORATIONS.get();
        enableDeadBushOnPodzol = ENABLE_DEAD_BUSH_ON_PODZOL.get();
        enableFernOnCoarseDirt = ENABLE_FERN_ON_COARSE_DIRT.get();
        enableMossOnMossyCobblestone = ENABLE_MOSS_ON_MOSSY_COBBLESTONE.get();
        frogspawnInstantChance = FROGSPAWN_INSTANT_CHANCE.get();
        oreVeinSizeMultiplier = ORE_VEIN_SIZE_MULTIPLIER.get();
        oreFrequencyDivisor = ORE_FREQUENCY_DIVISOR.get();
        suppressDensityWarnings = SUPPRESS_DENSITY_WARNINGS.get();
        biomeZoningEnabled = BIOME_ZONING_ENABLED.get();

        ZONE_BIOMES.clear();
        putAll(ZONE_BIOMES,
                "extreme_cold.inland", EXTREME_COLD_INLAND, "extreme_cold.hills", EXTREME_COLD_HILLS,
                "extreme_cold.coast", EXTREME_COLD_COAST, "extreme_cold.ocean", EXTREME_COLD_OCEAN,
                "cold.inland", COLD_INLAND, "cold.hills", COLD_HILLS,
                "cold.coast", COLD_COAST, "cold.ocean", COLD_OCEAN,
                "cool.inland", COOL_INLAND, "cool.hills", COOL_HILLS,
                "cool.coast", COOL_COAST, "cool.ocean", COOL_OCEAN,
                "neutral.inland", NEUTRAL_INLAND, "neutral.hills", NEUTRAL_HILLS,
                "neutral.coast", NEUTRAL_COAST, "neutral.ocean", NEUTRAL_OCEAN,
                "warm.inland", WARM_INLAND, "warm.hills", WARM_HILLS,
                "warm.coast", WARM_COAST, "warm.ocean", WARM_OCEAN,
                "hot.inland", HOT_INLAND, "hot.hills", HOT_HILLS,
                "hot.coast", HOT_COAST, "hot.ocean", HOT_OCEAN,
                "extreme_hot.inland", EXTREME_HOT_INLAND, "extreme_hot.hills", EXTREME_HOT_HILLS,
                "extreme_hot.coast", EXTREME_HOT_COAST, "extreme_hot.ocean", EXTREME_HOT_OCEAN,
                "river.frozen", RIVER_FROZEN, "river.normal", RIVER_NORMAL
        );

        FEATURE_BIOMES.clear();
        putAll(FEATURE_BIOMES,
                "boulder", FEATURE_BOULDER,
                "fallen_log", FEATURE_FALLEN_LOG,
                "wild_farm", FEATURE_WILD_FARM,
                "earthy_patches", FEATURE_EARTHY_PATCHES,
                "river_vegetation", FEATURE_RIVER_VEGETATION,
                "slab_smoothing", FEATURE_SLAB_SMOOTHING,
                "river_shore_smoothing", FEATURE_RIVER_SHORE_SMOOTHING
        );

        BOULDER_TYPE_MAP.clear();
        putAll(BOULDER_TYPE_MAP, "cold", BOULDER_TYPE_COLD, "desert", BOULDER_TYPE_DESERT, "badlands", BOULDER_TYPE_BADLANDS);
        LOG_TYPE_MAP.clear();
        putAll(LOG_TYPE_MAP, "jungle", LOG_TYPE_JUNGLE, "birch", LOG_TYPE_BIRCH, "dark_oak", LOG_TYPE_DARK_OAK, "cherry", LOG_TYPE_CHERRY, "acacia", LOG_TYPE_ACACIA, "spruce", LOG_TYPE_SPRUCE);
        PATCH_KIND_MAP.clear();
        putAll(PATCH_KIND_MAP, "red_sandy", PATCH_KIND_RED_SANDY, "sandy", PATCH_KIND_SANDY, "stone", PATCH_KIND_STONE, "skip", PATCH_KIND_SKIP);

        ZONED_BIOME_IDS.clear();
        for (java.util.List<? extends String> list : ZONE_BIOMES.values()) {
            if (list == null) continue;
            for (String id : list) {
                if (id != null && !id.isEmpty() && !id.startsWith("#")) {
                    ZONED_BIOME_IDS.add(id);
                }
            }
        }
    }

    @SafeVarargs
    private static void putAll(Map<String, List<? extends String>> target, Object... pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            String key = (String) pairs[i];
            @SuppressWarnings("unchecked")
            ForgeConfigSpec.ConfigValue<List<? extends String>> val = (ForgeConfigSpec.ConfigValue<List<? extends String>>) pairs[i + 1];
            target.put(key, val.get());
        }
    }

    public static String boulderTypeFor(ResourceLocation biomeId) {
        return firstMatchingKey(BOULDER_TYPE_MAP, biomeId);
    }

    public static String logTypeFor(ResourceLocation biomeId) {
        String k = firstMatchingKey(LOG_TYPE_MAP, biomeId);
        return k != null ? k : "oak";
    }

    public static String patchKindFor(ResourceLocation biomeId) {
        String k = firstMatchingKey(PATCH_KIND_MAP, biomeId);
        return k != null ? k : "earthy";
    }

    public static boolean biomeHasFeature(String featureKey, ResourceLocation biomeId) {
        List<? extends String> list = FEATURE_BIOMES.get(featureKey);
        return matchesAny(list, biomeId);
    }

    private static String firstMatchingKey(Map<String, List<? extends String>> map, ResourceLocation biomeId) {
        if (biomeId == null) return null;
        for (Map.Entry<String, List<? extends String>> e : map.entrySet()) {
            if (matchesAny(e.getValue(), biomeId)) return e.getKey();
        }
        return null;
    }

    private static boolean matchesAny(List<? extends String> list, ResourceLocation biomeId) {
        if (list == null || biomeId == null) return false;
        String idStr = biomeId.toString();
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeId);
        var holderOpt = ForgeRegistries.BIOMES.getHolder(biomeKey);
        for (String entry : list) {
            if (entry == null || entry.isEmpty()) continue;
            if ("*".equals(entry)) return true;
            if (entry.charAt(0) == '#') {
                try {
                    TagKey<Biome> tag = TagKey.create(Registries.BIOME, ResourceLocation.parse(entry.substring(1)));
                    if (holderOpt.isPresent() && holderOpt.get().is(tag)) return true;
                    try {
                        Biome b = ForgeRegistries.BIOMES.getValue(biomeId);
                        if (b != null && ForgeRegistries.BIOMES.tags().getTag(tag).contains(b)) {
                            return true;
                        }
                    } catch (Throwable ignored) {
                    }
                } catch (Exception ignored) {
                }
            } else if (entry.equals(idStr)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAllowedSpawnBiome(ResourceLocation biomeId) {
        if (biomeId == null) return false;
        List<? extends String> neutralInland = ZONE_BIOMES.get("neutral.inland");
        if (neutralInland == null || neutralInland.isEmpty()) {
            return biomeId.toString().equals("minecraft:plains") || biomeId.toString().equals("minecraft:forest");
        }
        return matchesAny(neutralInland, biomeId);
    }
}