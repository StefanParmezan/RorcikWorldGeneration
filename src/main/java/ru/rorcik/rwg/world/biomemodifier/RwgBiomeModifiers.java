package ru.rorcik.rwg.world.biomemodifier;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.RwgMod;

public class RwgBiomeModifiers {

    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
                    RwgMod.MODID);

    public static final RegistryObject<Codec<ConfigDrivenFeatureModifier>> CONFIG_FEATURE =
            SERIALIZERS.register("config_feature",
                    () -> ConfigDrivenFeatureModifier.CODEC);
}