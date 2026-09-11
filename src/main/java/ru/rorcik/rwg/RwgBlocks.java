package ru.rorcik.rwg;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.rorcik.rwg.block.DirtPathSlabBlock;
import ru.rorcik.rwg.block.DirtSlabBlock;
import ru.rorcik.rwg.block.FallingSlabBlock;
import ru.rorcik.rwg.block.GrassSlabBlock;

public class RwgBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RwgMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RwgMod.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RwgMod.MODID);

    public static final RegistryObject<DirtSlabBlock> DIRT_SLAB = BLOCKS.register("dirt_slab",
            () -> new DirtSlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .sound(SoundType.GRAVEL)
                    .strength(0.5F)));

    public static final RegistryObject<Item> DIRT_SLAB_ITEM = ITEMS.register("dirt_slab",
            () -> new BlockItem(DIRT_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<GrassSlabBlock> GRASS_SLAB = BLOCKS.register("grass_slab",
            () -> new GrassSlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .sound(SoundType.GRAVEL)
                    .strength(0.6F)
                    .randomTicks()));

    public static final RegistryObject<Item> GRASS_SLAB_ITEM = ITEMS.register("grass_slab",
            () -> new BlockItem(GRASS_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<DirtPathSlabBlock> DIRT_PATH_SLAB = BLOCKS.register("dirt_path_slab",
            () -> new DirtPathSlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .sound(SoundType.GRAVEL)
                    .strength(0.65F)));

    public static final RegistryObject<Item> DIRT_PATH_SLAB_ITEM = ITEMS.register("dirt_path_slab",
            () -> new BlockItem(DIRT_PATH_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<SlabBlock> PODZOL_SLAB = BLOCKS.register("podzol_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .sound(SoundType.GRAVEL)
                    .strength(0.5F)));

    public static final RegistryObject<Item> PODZOL_SLAB_ITEM = ITEMS.register("podzol_slab",
            () -> new BlockItem(PODZOL_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<SlabBlock> COARSE_DIRT_SLAB = BLOCKS.register("coarse_dirt_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .sound(SoundType.GRAVEL)
                    .strength(0.5F)));

    public static final RegistryObject<Item> COARSE_DIRT_SLAB_ITEM = ITEMS.register("coarse_dirt_slab",
            () -> new BlockItem(COARSE_DIRT_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<FallingSlabBlock> SAND_SLAB = BLOCKS.register("sand_slab",
            () -> new FallingSlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .sound(SoundType.SAND)
                    .strength(0.5F)));

    public static final RegistryObject<Item> SAND_SLAB_ITEM = ITEMS.register("sand_slab",
            () -> new BlockItem(SAND_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<FallingSlabBlock> RED_SAND_SLAB = BLOCKS.register("red_sand_slab",
            () -> new FallingSlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .sound(SoundType.SAND)
                    .strength(0.5F)));

    public static final RegistryObject<Item> RED_SAND_SLAB_ITEM = ITEMS.register("red_sand_slab",
            () -> new BlockItem(RED_SAND_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<FallingSlabBlock> GRAVEL_SLAB = BLOCKS.register("gravel_slab",
            () -> new FallingSlabBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .sound(SoundType.GRAVEL)
                    .strength(0.6F)));

    public static final RegistryObject<Item> GRAVEL_SLAB_ITEM = ITEMS.register("gravel_slab",
            () -> new BlockItem(GRAVEL_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> RWG_TAB = CREATIVE_MODE_TABS.register("rwg_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(GRASS_SLAB_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(DIRT_SLAB_ITEM.get());
                        output.accept(GRASS_SLAB_ITEM.get());
                        output.accept(DIRT_PATH_SLAB_ITEM.get());
                        output.accept(PODZOL_SLAB_ITEM.get());
                        output.accept(COARSE_DIRT_SLAB_ITEM.get());
                        output.accept(SAND_SLAB_ITEM.get());
                        output.accept(RED_SAND_SLAB_ITEM.get());
                        output.accept(GRAVEL_SLAB_ITEM.get());
                    })
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.rwg"))
                    .build());
}