package ru.rorcik.rwg.world;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import ru.rorcik.rwg.RwgMod;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.city.GlobalCityRegistry;

@Mod.EventBusSubscriber(modid = RwgMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldCleanupListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            if (level.dimension().equals(Level.OVERWORLD)) {
                GlobalCityRegistry.clear();
                if (RwgConfig.enableLogging) {
                    LOGGER.info("Cleanup: [RWG] GlobalCityRegistry cleared for world unload.");
                }
            }
        }
    }
}