package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void rwg$replaceLandBiomesOverWater(
            WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {

        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> river = biomeRegistry.getHolderOrThrow(Biomes.RIVER);
        Holder<Biome> ocean = biomeRegistry.getHolderOrThrow(Biomes.OCEAN);
        Holder<Biome> plains = biomeRegistry.getHolderOrThrow(Biomes.PLAINS);
        Holder<Biome> snowyPlains = biomeRegistry.getHolderOrThrow(Biomes.SNOWY_PLAINS);
        Holder<Biome> savanna = biomeRegistry.getHolderOrThrow(Biomes.SAVANNA);
        Holder<Biome> desert = biomeRegistry.getHolderOrThrow(Biomes.DESERT);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                if (top < chunk.getMinBuildHeight()) continue;

                BlockState topState = chunk.getBlockState(new BlockPos(x, top, z));
                while (top >= chunk.getMinBuildHeight() && topState.isAir()) {
                    top--;
                    topState = chunk.getBlockState(new BlockPos(x, top, z));
                }

                Holder<Biome> current = chunk.getNoiseBiome(
                        QuartPos.fromBlock(x), QuartPos.fromBlock(top), QuartPos.fromBlock(z));

                boolean water = !topState.getFluidState().isEmpty();
                if (water) {
                    if (rwg$isWaterBiome(current)) continue;

                    int depth = 0;
                    for (int y = top; y >= chunk.getMinBuildHeight(); y--) {
                        if (chunk.getBlockState(new BlockPos(x, y, z)).getFluidState().isEmpty()) break;
                        depth++;
                    }
                    rwg$setBiome(chunk, x, top, z, depth <= 3 ? river : ocean);
                    continue;
                }

                if (!rwg$isWaterOnlyBiome(current)) continue;
                rwg$setBiome(chunk, x, top, z, rwg$landFor(current, plains, snowyPlains, savanna, desert));
            }
        }
    }

    private static void rwg$setBiome(ChunkAccess chunk, int x, int y, int z, Holder<Biome> biome) {
        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
        PalettedContainerRO<Holder<Biome>> biomes =
                ((LevelChunkSectionAccessor) section).rwg$getBiomes();
        if (biomes instanceof PalettedContainer<Holder<Biome>> container) {
            container.set(x >> 2, (y & 15) >> 2, z >> 2, biome);
        }
    }

    private static Holder<Biome> rwg$landFor(Holder<Biome> current, Holder<Biome> plains,
                                             Holder<Biome> snowyPlains, Holder<Biome> savanna,
                                             Holder<Biome> desert) {
        String id = current.unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("");
        if (id.contains("frozen")) return snowyPlains;
        if (id.contains("cold")) return snowyPlains;
        if (id.contains("lukewarm") || id.contains("warm")) return savanna;
        if (id.contains("desert") || id.contains("badlands")) return desert;
        return plains;
    }

    private static boolean rwg$isWaterOnlyBiome(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER);
    }

    private static boolean rwg$isWaterBiome(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return true;

        String id = biome.unwrapKey()
                .map(key -> key.location().getPath())
                .orElse("");
        return id.contains("swamp")
                || id.contains("marsh")
                || id.contains("bayou")
                || id.contains("floodplain")
                || id.contains("wetland");
    }
}
