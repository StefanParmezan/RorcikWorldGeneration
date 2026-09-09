package ru.rorcik.rwg;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RwgBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RwgMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RwgMod.MODID);

    public static final RegistryObject<SlabBlock> DIRT_SLAB = BLOCKS.register("dirt_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .sound(SoundType.GRAVEL)
                    .strength(0.5F)));

    public static final RegistryObject<Item> DIRT_SLAB_ITEM = ITEMS.register("dirt_slab",
            () -> new BlockItem(DIRT_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<SlabBlock> GRASS_SLAB = BLOCKS.register("grass_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .sound(SoundType.GRAVEL)
                    .strength(0.6F)));

    public static final RegistryObject<Item> GRASS_SLAB_ITEM = ITEMS.register("grass_slab",
            () -> new BlockItem(GRASS_SLAB.get(), new Item.Properties()));
}