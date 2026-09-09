package ru.rorcik.rwg;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RwgMod.MODID)
public class RwgMod {
    public static final String MODID = "rwg";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RwgMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, RwgConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Rorcik World Generation initialized");
    }
}