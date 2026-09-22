package ru.rorcik.rwg.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import ru.rorcik.rwg.RwgMod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = RwgMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RwgBiomeModifiers {

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(
                net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS,
                helper -> {
                    helper.register(
                            ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "add_slab_smoothing"),
                            new RwgAddFeatureModifier(
                                    Set.of("minecraft:plains", "minecraft:forest", "minecraft:birch_forest",
                                            "minecraft:taiga", "minecraft:meadow", "minecraft:sunflower_plains",
                                            "minecraft:dark_forest"),
                                    RwgPlacedFeatures.SLAB_SMOOTHING.getHolder().orElseThrow(),
                                    GenerationStep.Decoration.VEGETAL_DECORATION
                            ));

                    helper.register(
                            ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "add_wild_farm"),
                            new RwgAddFeatureModifier(
                                    Set.of("minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest",
                                            "minecraft:flower_forest", "minecraft:birch_forest", "minecraft:meadow",
                                            "minecraft:taiga"),
                                    RwgPlacedFeatures.WILD_FARM.getHolder().orElseThrow(),
                                    GenerationStep.Decoration.VEGETAL_DECORATION
                            ));

                    helper.register(
                            ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "add_boulder"),
                            new RwgAddFeatureModifier(
                                    Set.of("minecraft:plains", "minecraft:forest", "minecraft:taiga",
                                            "minecraft:meadow", "minecraft:windswept_hills",
                                            "minecraft:windswept_gravelly_hills", "minecraft:desert",
                                            "minecraft:badlands", "minecraft:stony_peaks"),
                                    RwgPlacedFeatures.BOULDER.getHolder().orElseThrow(),
                                    GenerationStep.Decoration.VEGETAL_DECORATION
                            ));

                    helper.register(
                            ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "add_fallen_log"),
                            new RwgAddFeatureModifier(
                                    Set.of("minecraft:forest", "minecraft:birch_forest", "minecraft:dark_forest",
                                            "minecraft:taiga", "minecraft:old_growth_pine_taiga",
                                            "minecraft:old_growth_spruce_taiga"),
                                    RwgPlacedFeatures.FALLEN_LOG.getHolder().orElseThrow(),
                                    GenerationStep.Decoration.VEGETAL_DECORATION
                            ));

                    helper.register(
                            ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "add_river_vegetation"),
                            new RwgAddFeatureModifier(
                                    Set.of("minecraft:river", "minecraft:frozen_river"),
                                    RwgPlacedFeatures.RIVER_VEGETATION.getHolder().orElseThrow(),
                                    GenerationStep.Decoration.VEGETAL_DECORATION
                            ));

                    helper.register(
                            ResourceLocation.fromNamespaceAndPath(RwgMod.MODID, "add_moss_carpet"),
                            new RwgAddFeatureModifier(
                                    Set.of("minecraft:forest", "minecraft:birch_forest", "minecraft:dark_forest",
                                            "minecraft:taiga", "minecraft:old_growth_pine_taiga"),
                                    RwgPlacedFeatures.MOSS_CARPET.getHolder().orElseThrow(),
                                    GenerationStep.Decoration.VEGETAL_DECORATION
                            ));
                }
        );
    }

    public record RwgAddFeatureModifier(
            Set<String> biomeIds,
            Holder<PlacedFeature> feature,
            GenerationStep.Decoration step
    ) implements BiomeModifier {

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD) return;

            String biomeName = biome.unwrapKey()
                    .map(key -> key.location().toString())
                    .orElse("");
            if (!biomeIds.contains(biomeName)) return;

            builder.getGenerationSettings().addFeature(step, feature);
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return Codec.unit(this);
        }
    }
}