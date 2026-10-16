package ru.rorcik.rwg;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class RwgBiomes {
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(Registries.BIOME, RwgMod.MODID);

    public static final ResourceKey<Biome> PLAINS_CITY = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "plains_city")
    );

    public static final ResourceKey<Biome> DESERT_CITY = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "desert_city")
    );

    public static final RegistryObject<Biome> PLAINS_CITY_BIOME = BIOMES.register("plains_city", () ->
            new Biome.BiomeBuilder()
                    .hasPrecipitation(true)
                    .temperature(0.8f)
                    .downfall(0.4f)
                    .specialEffects(new BiomeSpecialEffects.Builder()
                            .waterColor(4159204)
                            .waterFogColor(329011)
                            .fogColor(5921370)
                            .skyColor(7907327)
                            .build())
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .build()
    );

    public static final RegistryObject<Biome> DESERT_CITY_BIOME = BIOMES.register("desert_city", () ->
            new Biome.BiomeBuilder()
                    .hasPrecipitation(false)
                    .temperature(2.0f)
                    .downfall(0.0f)
                    .specialEffects(new BiomeSpecialEffects.Builder()
                            .waterColor(4159204)
                            .waterFogColor(329011)
                            .fogColor(13421772)
                            .skyColor(11330815)
                            .build())
                    .mobSpawnSettings(MobSpawnSettings.EMPTY)
                    .generationSettings(BiomeGenerationSettings.EMPTY)
                    .build()
    );
}