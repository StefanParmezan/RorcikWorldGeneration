package ru.rorcik.rwg.world.biomemodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.RwgConfig;

public record ConfigDrivenFeatureModifier(
        String featureKey,
        Holder<PlacedFeature> placedFeature,
        GenerationStep.Decoration step
) implements BiomeModifier {

    public static final Codec<ConfigDrivenFeatureModifier> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.fieldOf("feature_key")
                            .forGetter(ConfigDrivenFeatureModifier::featureKey),
                    PlacedFeature.CODEC.fieldOf("placed_feature")
                            .forGetter(ConfigDrivenFeatureModifier::placedFeature),
                    GenerationStep.Decoration.CODEC.fieldOf("step")
                            .forGetter(ConfigDrivenFeatureModifier::step)
            ).apply(inst, ConfigDrivenFeatureModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase,
                       ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) return;

        ResourceLocation biomeId = biome.unwrapKey()
                .map(k -> k.location())
                .orElse(null);
        if (biomeId == null) return;

        if (!RwgConfig.biomeHasFeature(featureKey, biomeId)) return;

        builder.getGenerationSettings().addFeature(step, placedFeature);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return RwgBiomeModifiers.CONFIG_FEATURE.get();
    }
}