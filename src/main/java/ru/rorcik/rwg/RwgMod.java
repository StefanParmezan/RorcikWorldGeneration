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
import ru.rorcik.rwg.worldgen.RwgCarvers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(RwgMod.MODID)
public class RwgMod {
    public static final String MODID = "rwg";
    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        if (shouldSuppressDensityWarnings()) {
            suppressDensityFunctionLogs();
        }
    }

    public RwgMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        RwgBlocks.BLOCKS.register(modEventBus);
        RwgBlocks.ITEMS.register(modEventBus);
        RwgBlocks.CREATIVE_MODE_TABS.register(modEventBus);
        RwgCarvers.CARVERS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, RwgConfig.SPEC);
    }

    private static boolean shouldSuppressDensityWarnings() {
        try {
            Path configPath = Paths.get("config", "rwg-common.toml");

            if (!Files.exists(configPath)) {
                return true;
            }

            String content = Files.readString(configPath);

            if (content.contains("suppress_density_warnings")) {
                String[] lines = content.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("suppress_density_warnings")) {
                        String value = line.split("=")[1].trim();
                        return Boolean.parseBoolean(value);
                    }
                }
            }

            return true;

        } catch (Exception e) {
            LOGGER.debug("Could not read config early, using default value for suppress_density_warnings");
            return true;
        }
    }

    private static void suppressDensityFunctionLogs() {
        try {
            Class<?> interfaceClass = Class.forName("net.minecraft.world.level.levelgen.DensityFunctions$TwoArgumentSimpleFunction");
            Field loggerField = interfaceClass.getDeclaredField("LOGGER");
            loggerField.setAccessible(true);

            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(loggerField, loggerField.getModifiers() & ~Modifier.FINAL);
            } catch (NoSuchFieldException e) {
                var lookup = java.lang.invoke.MethodHandles.privateLookupIn(
                        interfaceClass,
                        java.lang.invoke.MethodHandles.lookup()
                );
                var varHandle = lookup.findStaticVarHandle(
                        interfaceClass,
                        "LOGGER",
                        org.slf4j.Logger.class
                );
                varHandle.set(NOPLogger.NOP_LOGGER);
                return;
            }

            loggerField.set(null, NOPLogger.NOP_LOGGER);

        } catch (Exception e) {
            LOGGER.warn("Could not suppress DensityFunctions logger: {}", e.getMessage());
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Rorcik World Generation initialized");
        event.enqueueWork(RwgTerrablender::init);
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