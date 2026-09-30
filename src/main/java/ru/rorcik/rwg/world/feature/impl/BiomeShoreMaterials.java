package ru.rorcik.rwg.world.feature.impl;

import net.minecraft.world.level.block.Blocks;
import ru.rorcik.rwg.RwgBlocks;
import ru.rorcik.rwg.world.feature.config.ShoreMaterialConfig;

final class BiomeShoreMaterials {

    static final ShoreMaterialConfig EARTHY = ShoreMaterialConfig.builder()
            .add(Blocks.MUD,                 26)
            .add(Blocks.COARSE_DIRT,         10)
            .add(Blocks.GRAVEL,              20)
            .add(Blocks.SUSPICIOUS_GRAVEL,   19)
            .add(Blocks.SAND,                12)
            .add(Blocks.SUSPICIOUS_SAND,     12)
            .add(Blocks.CLAY,                 1)
            .build();

    static final ShoreMaterialConfig STONE = ShoreMaterialConfig.builder()
            .add(Blocks.GRAVEL,          60)
            .add(Blocks.COBBLESTONE,     10)
            .add(Blocks.STONE,           10)
            .add(Blocks.ANDESITE,        10)
            .add(Blocks.DIORITE,          5)
            .add(Blocks.TUFF,             5)
            .build();

    static final ShoreMaterialConfig SANDY = ShoreMaterialConfig.builder()
            .add(Blocks.SAND,            55)
            .add(Blocks.SUSPICIOUS_SAND, 14)
            .add(Blocks.SANDSTONE,       30)
            .add(Blocks.CLAY,             1)
            .build();

    static final ShoreMaterialConfig RED_SANDY = ShoreMaterialConfig.builder()
            .add(Blocks.RED_SAND,        55)
            .add(Blocks.RED_SANDSTONE,   24)
            .add(Blocks.GRAVEL,          20)
            .add(Blocks.CLAY,             1)
            .build();

    private BiomeShoreMaterials() {}
}