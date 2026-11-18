package ru.rorcik.rwg.world.city;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import ru.rorcik.core.event.ZoneEvents;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.RwgMod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = RwgMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CityZoneEventListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onZoneReserved(ZoneEvents.ZoneReservedEvent event) {
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
        if (level == null) {
            if (RwgConfig.enableLogging) {
                LOGGER.warn("Warn: overworld is null");
            }
            return;
        }

        CityZoneCache cache = CityZoneCache.get(level);
        cache.addZone(event.centerX, event.centerZ, event.rules, event.targetHeight, event.affectedChunks);

        GlobalCityRegistry.CityZoneInfo info = new GlobalCityRegistry.CityZoneInfo(event.rules.id(), event.targetHeight);
        GlobalCityRegistry.registerCity(event.centerX, event.centerZ, event.affectedChunks, info);

        if (RwgConfig.enableLogging) {
            LOGGER.info("Info: City {} at [{}, {}] registered (chunks: {})", event.rules.id(), event.centerX, event.centerZ, event.affectedChunks.size());
        }
    }

    /**
     * The registry is a purely in-memory structure. Without this hook it stays
     * empty after a server restart, so already reserved cities silently lose
     * their plateau/biome for all chunks generated after the reload.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        // Make sure no stale entries from a previous world survive in this JVM.
        GlobalCityRegistry.clear();

        CityZoneCache cache = CityZoneCache.get(level);
        int restored = 0;
        for (CityZoneCache.CityZoneInfo zone : cache.getAllZones()) {
            if (GlobalCityRegistry.isRegistered(zone.centerX(), zone.centerZ())) continue;

            Set<Long> chunks = cache.getChunksForZone(zone);
            if (chunks.isEmpty()) continue;

            GlobalCityRegistry.registerCity(zone.centerX(), zone.centerZ(), chunks,
                    new GlobalCityRegistry.CityZoneInfo(zone.rules().id(), zone.targetHeight()));
            restored++;
        }

        if (restored > 0 && RwgConfig.enableLogging) {
            LOGGER.info("Info: Restored {} city zone(s) into GlobalCityRegistry after world load", restored);
        }
    }
}
