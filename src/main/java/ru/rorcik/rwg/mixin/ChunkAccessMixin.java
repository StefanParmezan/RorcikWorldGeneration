package ru.rorcik.rwg.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.rorcik.rwg.RwgBiomes;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.city.GlobalCityRegistry;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @ModifyVariable(method = "fillBiomesFromNoise", at = @At("HEAD"), argsOnly = true)
    private BiomeResolver rwg$substituteBiomeResolver(BiomeResolver originalResolver) {
        return (quartX, quartY, quartZ, climateSampler) -> {
            int chunkX = quartX >> 2;
            int chunkZ = quartZ >> 2;
            GlobalCityRegistry.CityZoneInfo zone = GlobalCityRegistry.getZone(chunkX, chunkZ);
            if (zone != null) {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    Registry<Biome> liveBiomeRegistry = server.registryAccess().registryOrThrow(Registries.BIOME);
                    ResourceKey<Biome> cityBiomeKey = "plains".equals(zone.typeId()) ? RwgBiomes.PLAINS_CITY : RwgBiomes.DESERT_CITY;
                    Holder<Biome> cityBiomeHolder = liveBiomeRegistry.getHolder(cityBiomeKey).orElse(null);
                    if (cityBiomeHolder != null) {
                        if (RwgConfig.enableLogging) {
                            LOGGER.debug("Info: Successfully obtained valid Holder with network ID for: {}", cityBiomeKey.location());
                        }
                        return cityBiomeHolder;
                    } else {
                        if (RwgConfig.enableLogging) {
                            LOGGER.warn("Warn: Biome {} not found in dynamic world registry!", cityBiomeKey.location());
                        }
                    }
                }
            }
            return originalResolver.getNoiseBiome(quartX, quartY, quartZ, climateSampler);
        };
    }
}