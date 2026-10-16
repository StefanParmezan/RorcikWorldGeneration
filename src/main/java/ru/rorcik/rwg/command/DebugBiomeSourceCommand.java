package ru.rorcik.rwg.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "rwg", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DebugBiomeSourceCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("debugbiomesource")
                        .executes(ctx -> {
                            ServerLevel level = ctx.getSource().getLevel();
                            BiomeSource biomeSource = level.getChunkSource().getGenerator().getBiomeSource();

                            ctx.getSource().sendSuccess(() -> Component.literal("BiomeSource class: " + biomeSource.getClass().getName()), false);
                            return 1;
                        })
        );
    }
}