package ru.rorcik.rwg.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

public class DebugTempCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rwgdebug")
                .then(Commands.literal("temp")
                        .executes(DebugTempCommand::executeTemp)
                )
        );
    }

    private static int executeTemp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 position = source.getPosition();
        final BlockPos pos = BlockPos.containing(position);

        final double x = pos.getX();
        final double z = pos.getZ();
        final double distance = Math.sqrt(x * x + z * z);
        final double period = 48000.0;
        final double halfPeriod = 24000.0;
        final double distMod = ((distance % period) + period) % period;
        final double expectedTemp = Math.max(-1.0, Math.min(1.0,
                distMod <= halfPeriod ? ascendingTemp(distMod) : ascendingTemp(period - distMod)
        ));

        final Biome biome = level.getBiome(pos).value();
        final ResourceLocation biomeId = level.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome);
        final String biomeName = biomeId != null ? biomeId.toString() : "Unknown";

        final double continentalness = level.getChunkSource().getGenerator()
                .getBiomeSource().getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2,
                        level.getChunkSource().randomState().sampler())
                .value().toString().contains("ocean") ? -0.6 : 0.1;

        source.sendSuccess(() -> Component.literal("§6=== RWG Temperature Debug ==="), false);
        source.sendSuccess(() -> Component.literal("§7Position: §fX=" + pos.getX() + " Z=" + pos.getZ()), false);
        source.sendSuccess(() -> Component.literal("§7Distance: §f" + String.format("%.2f", distance)), false);
        source.sendSuccess(() -> Component.literal("§7Expected Temp: §f" + String.format("%.4f", expectedTemp)), false);
        source.sendSuccess(() -> Component.literal("§7Continentalness: §f" + String.format("%.2f", continentalness)), false);
        source.sendSuccess(() -> Component.literal("§7Actual Biome: §f" + biomeName), false);

        final String verdict = checkMismatch(expectedTemp, biomeName);
        if (verdict != null) {
            source.sendSuccess(() -> Component.literal("§c[WARNING] " + verdict), false);
        } else {
            source.sendSuccess(() -> Component.literal("§a[OK] Биом соответствует температурной зоне"), false);
        }

        return 1;
    }

    private static String checkMismatch(double temp, String biome) {

        if (temp <= -0.7f) {
            if (!biome.contains("snowy") && !biome.contains("frozen")) {
                return "Биом '" + biome + "' не принадлежит экстремально холодной зоне!";
            }
        }

        else if (temp <= -0.4f) {
            if (!biome.contains("snowy_taiga") && !biome.contains("grove") &&
                    !biome.contains("jagged_peaks") && !biome.contains("cold_ocean") &&
                    !biome.contains("frozen_river") && !biome.contains("snowy_beach")) {
                return "Биом '" + biome + "' не принадлежит холодной зоне!";
            }
        }

        else if (temp <= -0.1f) {
            if (biome.contains("plains") || biome.contains("forest") ||
                    biome.contains("desert") || biome.contains("savanna") || biome.contains("badlands")) {
                return "Биом '" + biome + "' не принадлежит прохладной зоне (должна быть тайга/тёмный лес)!";
            }
        }

        else if (temp <= 0.2f) {
            if (biome.contains("desert") || biome.contains("savanna") || biome.contains("badlands") ||
                    biome.contains("taiga") || biome.contains("snowy")) {
                return "Биом '" + biome + "' не принадлежит нейтральной зоне (должны быть равнины/леса)!";
            }
        }

        else if (temp <= 0.5f) {
            if (!biome.contains("savanna") && !biome.contains("forest") && !biome.contains("lukewarm")) {
                return "Биом '" + biome + "' не принадлежит тёплой зоне!";
            }
        }

        else if (temp <= 0.8f) {
            if (!biome.contains("desert") && !biome.contains("stony_peaks") && !biome.contains("warm_ocean")) {
                return "Биом '" + biome + "' не принадлежит жаркой зоне!";
            }
        }

        else {
            if (!biome.contains("badlands")) {
                return "Биом '" + biome + "' не принадлежит экстремально жаркой зоне (должна быть mesa)!";
            }
        }
        return null;
    }

    private static double ascendingTemp(double distance) {
        if (distance <= 2000)  return lerp(distance, 0, 2000, -1.0, -0.7);
        if (distance <= 5000)  return lerp(distance, 2000, 5000, -0.7, -0.4);
        if (distance <= 9000)  return lerp(distance, 5000, 9000, -0.4, -0.1);
        if (distance <= 14000) return lerp(distance, 9000, 14000, -0.1, 0.2);
        if (distance <= 18000) return lerp(distance, 14000, 18000, 0.2, 0.5);
        if (distance <= 22000) return lerp(distance, 18000, 22000, 0.5, 0.8);
        return lerp(distance, 22000, 24000, 0.8, 1.0);
    }

    private static double lerp(double x, double x0, double x1, double y0, double y1) {
        double t = (x - x0) / (x1 - x0);
        return y0 + t * (y1 - y0);
    }
}