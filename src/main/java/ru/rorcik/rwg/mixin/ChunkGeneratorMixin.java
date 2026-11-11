package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.world.city.GlobalCityRegistry;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {

    private static final int WATER_CELL_THRESHOLD = 8;

    @Inject(method = "applyBiomeDecoration", at = @At("HEAD"))
    private void rwg$replaceWaterBiomes(
            WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {

        if (!RwgConfig.enableWaterBiomeFix) return;
        if (GlobalCityRegistry.getZone(chunk.getPos().x, chunk.getPos().z) != null) return;

        Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> river = biomeRegistry.getHolderOrThrow(Biomes.RIVER);
        Holder<Biome> ocean = biomeRegistry.getHolderOrThrow(Biomes.OCEAN);
        Holder<Biome> plains = biomeRegistry.getHolderOrThrow(Biomes.PLAINS);
        Holder<Biome> snowyPlains = biomeRegistry.getHolderOrThrow(Biomes.SNOWY_PLAINS);
        Holder<Biome> savanna = biomeRegistry.getHolderOrThrow(Biomes.SAVANNA);
        Holder<Biome> desert = biomeRegistry.getHolderOrThrow(Biomes.DESERT);

        int minY = chunk.getMinBuildHeight();

        for (int cellX = 0; cellX < 4; cellX++) {
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                int waterColumns = 0;
                int waterDepth = 0;
                int topY = minY;

                for (int x = cellX * 4; x < cellX * 4 + 4; x++) {
                    for (int z = cellZ * 4; z < cellZ * 4 + 4; z++) {
                        int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                        if (top < minY) continue;

                        BlockState state = chunk.getBlockState(new BlockPos(x, top, z));
                        while (top >= minY && state.isAir()) {
                            top--;
                            state = chunk.getBlockState(new BlockPos(x, top, z));
                        }
                        if (state.getFluidState().isEmpty()) continue;

                        waterColumns++;
                        topY = Math.max(topY, top);
                        int depth = 0;
                        for (int y = top; y >= minY; y--) {
                            if (chunk.getBlockState(new BlockPos(x, y, z)).getFluidState().isEmpty()) break;
                            depth++;
                        }
                        waterDepth += depth;
                    }
                }

                int middleX = cellX * 4 + 2;
                int middleZ = cellZ * 4 + 2;
                int middleTop = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, middleX, middleZ) - 1;
                if (middleTop < minY) middleTop = topY;
                if (middleTop < minY) continue;

                Holder<Biome> current = chunk.getNoiseBiome(
                        QuartPos.fromBlock(middleX), QuartPos.fromBlock(middleTop), QuartPos.fromBlock(middleZ));
                if (!rwg$isVanilla(current)) continue;

                boolean waterCell = waterColumns >= WATER_CELL_THRESHOLD;
                if (waterCell) {
                    if (rwg$isWaterBiome(current)) continue;
                    int averageDepth = Math.max(1, waterDepth / Math.max(1, waterColumns));
                    rwg$setBiome(chunk, middleX, topY, middleZ, averageDepth <= 3 ? river : ocean);
                } else if (rwg$isWaterOnlyBiome(current)) {
                    rwg$setBiome(chunk, middleX, middleTop, middleZ,
                            rwg$landFor(current, plains, snowyPlains, savanna, desert));
                }
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
        if (id.contains("frozen") || id.contains("cold")) return snowyPlains;
        if (id.contains("lukewarm") || id.contains("warm")) return savanna;
        if (id.contains("desert") || id.contains("badlands")) return desert;
        return plains;
    }

    private static boolean rwg$isVanilla(Holder<Biome> biome) {
        return biome.unwrapKey()
                .map(key -> "minecraft".equals(key.location().getNamespace()))
                .orElse(false);
    }

    private static boolean rwg$isWaterOnlyBiome(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER);
    }

    private static boolean rwg$isWaterBiome(Holder<Biome> biome) {
        if (rwg$isWaterOnlyBiome(biome)) return true;

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
