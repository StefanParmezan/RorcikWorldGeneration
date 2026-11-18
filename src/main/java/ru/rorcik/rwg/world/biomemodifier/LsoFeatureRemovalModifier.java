package ru.rorcik.rwg.world.biomemodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import ru.rorcik.rwg.RwgConfig;

public record LsoFeatureRemovalModifier(
        HolderSet<Biome> biomes,
        HolderSet<PlacedFeature> features,
        String configKey
) implements BiomeModifier {

    public static final Codec<LsoFeatureRemovalModifier> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(LsoFeatureRemovalModifier::biomes),
                    PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(LsoFeatureRemovalModifier::features),
                    Codec.STRING.fieldOf("config_key").forGetter(LsoFeatureRemovalModifier::configKey)
            ).apply(inst, LsoFeatureRemovalModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        
        if (phase != Phase.REMOVE) return;
        if (!biomes.contains(biome)) return;

        if (RwgConfig.isLsoRemovalEnabled(configKey)) {
            
            for (net.minecraft.world.level.levelgen.GenerationStep.Decoration step : net.minecraft.world.level.levelgen.GenerationStep.Decoration.values()) {
                
                java.util.List<Holder<PlacedFeature>> stepFeatures = builder.getGenerationSettings().getFeatures(step);

                stepFeatures.removeAll(features.stream().toList());
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return RwgBiomeModifiers.LSO_REMOVAL.get();
    }
}