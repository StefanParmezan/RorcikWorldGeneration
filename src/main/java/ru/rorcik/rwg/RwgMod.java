package ru.rorcik.rwg;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.GrassColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import ru.rorcik.rwg.command.DebugTempCommand;
import ru.rorcik.rwg.world.RwgTerrablender;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ru.rorcik.rwg.world.feature.RwgFeatures;
import ru.rorcik.rwg.world.surface.RwgSurfaceRules;

@Mod(RwgMod.MODID)
public class RwgMod {
    public static final String MODID = "rwg";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RwgMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        RwgBlocks.BLOCKS.register(modEventBus);
        RwgBlocks.ITEMS.register(modEventBus);
        RwgBlocks.CREATIVE_MODE_TABS.register(modEventBus);

        RwgFeatures.FEATURES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, RwgConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Rorcik World Generation initialized");

        event.enqueueWork(RwgTerrablender::init);

        event.enqueueWork(RwgSurfaceRules::init);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        DebugTempCommand.register(event.getDispatcher());
    }

    @SuppressWarnings("deprecation")
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                Minecraft mc = Minecraft.getInstance();

                ItemBlockRenderTypes.setRenderLayer(RwgBlocks.GRASS_SLAB.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(RwgBlocks.DIRT_PATH_SLAB.get(), RenderType.cutoutMipped());

                mc.getBlockColors().register((state, level, pos, tintIndex) -> {
                    if (level != null && pos != null) {
                        return BiomeColors.getAverageGrassColor(level, pos);
                    }
                    return GrassColor.getDefaultColor();
                }, RwgBlocks.GRASS_SLAB.get());

                mc.getItemColors().register((stack, tintIndex) -> {
                    return GrassColor.getDefaultColor();
                }, RwgBlocks.GRASS_SLAB_ITEM.get());
            });
        }
    }
}