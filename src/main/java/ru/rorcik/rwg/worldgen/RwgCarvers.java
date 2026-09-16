package ru.rorcik.rwg.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.RwgMod;
import ru.rorcik.rwg.worldgen.carver.RwgCaveCarver;

public class RwgCarvers {
    public static final DeferredRegister<WorldCarver<?>> CARVERS =
            DeferredRegister.create(Registries.CARVER, RwgMod.MODID);

    public static final RegistryObject<RwgCaveCarver> OLD_CAVE = CARVERS.register("old_cave",
            () -> new RwgCaveCarver(CaveCarverConfiguration.CODEC));
}