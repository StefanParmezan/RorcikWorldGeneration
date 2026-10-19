package ru.rorcik.rwg.world.city;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import ru.rorcik.core.event.ZoneEvents;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.RwgMod;

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
}