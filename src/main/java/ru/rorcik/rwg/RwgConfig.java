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
                    "minecraft:mangrove_swamp"
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

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static List<? extends String> blacklistedBiomes;
    public static List<? extends String> physicsAffectedBlocks;
    public static double highwaySpawnChance;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        blacklistedBiomes = BLACKLISTED_BIOMES.get();
        physicsAffectedBlocks = PHYSICS_AFFECTED_BLOCKS.get();
        highwaySpawnChance = HIGHWAY_SPAWN_CHANCE.get();
    }
}