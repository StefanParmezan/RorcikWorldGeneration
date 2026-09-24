package ru.rorcik.rwg;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = RwgMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RwgConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_BIOMES = BUILDER
            .comment("List of biomes completely excluded from world generation")
            .defineList("blacklisted_biomes", Arrays.asList(
                    "minecraft:mushroom_fields",
                    "minecraft:jungle",
                    "minecraft:sparse_jungle",
                    "minecraft:bamboo_jungle",
                    "minecraft:mangrove_swamp",
                    "minecraft:swamp",
                    "minecraft:lush_caves",
                    "minecraft:ice_spikes",
                    "minecraft:cherry_grove"
            ), obj -> obj instanceof String);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PHYSICS_AFFECTED_BLOCKS = BUILDER
            .comment("List of blocks that activate MineCollapse physics after a block update")
            .defineList("physics_affected_blocks", Arrays.asList(
                    "minecraft:dirt",
                    "minecraft:coarse_dirt",
                    "minecraft:stone",
                    "minecraft:cobblestone",
                    "rwg:dirt_slab"
            ), obj -> obj instanceof String);

    public static final ForgeConfigSpec.DoubleValue HIGHWAY_SPAWN_CHANCE = BUILDER
            .comment("Chance of generating an abandoned highway in forest biomes (0.0 - 1.0)")
            .defineInRange("highway_spawn_chance", 0.05, 0.0, 1.0);

    public static final ForgeConfigSpec.BooleanValue APPLY_JOIN_WORLD_SPAWN_MIXIN = BUILDER
            .define("apply_join_world_spawn_mixin", true);

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


    public static final ForgeConfigSpec.BooleanValue SUPPRESS_DENSITY_WARNINGS;

    static {
        BUILDER.push("caves");

        ENABLE_OLD_CAVES = BUILDER
                .comment("Use old-style cave generation (pre-1.18) instead of vanilla noise caves")
                .define("enable_old_caves", true);

        DISABLE_VANILLA_CAVES = BUILDER
                .comment("Completely disable vanilla cave carvers (requires enable_old_caves = true)")
                .define("disable_vanilla_caves", true);

        DISABLE_CANYONS = BUILDER
                .comment("Disable large canyon/ravine generation")
                .define("disable_canyons", true);

        CAVE_MIN_SURFACE_DISTANCE = BUILDER
                .comment("Minimum distance from surface before caves can generate (prevents swiss cheese effect)")
                .defineInRange("cave_min_surface_distance", 8, 3, 32);

        CAVE_FREQUENCY = BUILDER
                .comment("How often caves generate (higher = more caves). Default old caves: 7")
                .defineInRange("cave_frequency", 7, 1, 20);

        CAVE_RARITY = BUILDER
                .comment("Cave spawn chance per chunk (0.0 - 1.0). Lower = rarer but longer caves")
                .defineInRange("cave_rarity", 0.14, 0.01, 1.0);

        CAVE_MIN_Y = BUILDER
                .comment("Minimum Y level for cave generation")
                .defineInRange("cave_min_y", -54, -64, 64);

        CAVE_MAX_Y = BUILDER
                .comment("Maximum Y level for cave generation")
                .defineInRange("cave_max_y", 128, 0, 256);

        SMOOTH_CAVE_WALLS = BUILDER
                .comment("Make cave walls smoother (less jagged)")
                .define("smooth_cave_walls", true);

        EXPOSE_CAVES_IN_MOUNTAINS = BUILDER
                .comment("Allow cave entrances to spawn naturally in mountain biomes")
                .define("expose_caves_in_mountains", true);

        BUILDER.pop();

        BUILDER.push("logging");

        SUPPRESS_DENSITY_WARNINGS = BUILDER
                .comment(
                        "Suppress 'non-overlapping inputs' warnings from vanilla DensityFunctions.",
                        "These warnings are harmless and appear in vanilla Minecraft too.",
                        "Set to false to see original Mojang debug output (will spam logs).",
                        "Requires game restart to take effect."
                )
                .define("suppress_density_warnings", true);

        BUILDER.push("vegetation");

        ENABLE_EARTHY_PATCH_DECORATIONS = BUILDER
                .comment("Master switch: allow vegetation on top of earthy patches (dead bush, ferns, moss)")
                .define("enable_earthy_patch_decorations", true);

        ENABLE_DEAD_BUSH_ON_PODZOL = BUILDER
                .comment("Allow dead bushes to appear on podzol patches")
                .define("enable_dead_bush_on_podzol", true);

        ENABLE_FERN_ON_COARSE_DIRT = BUILDER
                .comment("Allow ferns and large ferns to appear on coarse dirt patches")
                .define("enable_fern_on_coarse_dirt", true);

        ENABLE_MOSS_ON_MOSSY_COBBLESTONE = BUILDER
                .comment("Allow moss carpet to appear on mossy cobblestone patches")
                .define("enable_moss_on_mossy_cobblestone", true);

        BUILDER.pop();

        BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static List<? extends String> blacklistedBiomes;
    public static List<? extends String> physicsAffectedBlocks;
    public static double highwaySpawnChance;
    public static boolean applyJoinWorldSpawnMixin;


    public static boolean enableEarthyPatchDecorations;
    public static boolean enableDeadBushOnPodzol;
    public static boolean enableFernOnCoarseDirt;
    public static boolean enableMossOnMossyCobblestone;
    public static boolean enableOldCaves;
    public static boolean disableVanillaCaves;
    public static boolean disableCanyons;
    public static int caveMinSurfaceDistance;
    public static int caveFrequency;
    public static double caveRarity;
    public static int caveMinY;
    public static int caveMaxY;
    public static boolean smoothCaveWalls;
    public static boolean exposeCavesInMountains;

    public static boolean suppressDensityWarnings;

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

        enableEarthyPatchDecorations    = ENABLE_EARTHY_PATCH_DECORATIONS.get();
        enableDeadBushOnPodzol          = ENABLE_DEAD_BUSH_ON_PODZOL.get();
        enableFernOnCoarseDirt          = ENABLE_FERN_ON_COARSE_DIRT.get();
        enableMossOnMossyCobblestone    = ENABLE_MOSS_ON_MOSSY_COBBLESTONE.get();
        suppressDensityWarnings = SUPPRESS_DENSITY_WARNINGS.get();
    }
}