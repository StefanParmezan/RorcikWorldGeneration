package ru.rorcik.rwg.worldgen.carver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import ru.rorcik.rwg.RwgConfig;

import java.util.function.Function;

public class RwgCaveCarverHelper {

    private static final RwgCaveCarver CARVER = new RwgCaveCarver(CaveCarverConfiguration.CODEC);

    public static void carveChunk(
            WorldGenRegion region,
            long worldSeed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
        if (!RwgConfig.enableOldCaves) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        RandomSource random = RandomSource.create(worldSeed + chunkPos.x * 341873128712L + chunkPos.z * 132897987541L);

        ServerLevel level = region.getLevel();
        ServerChunkCache chunkSource = level.getChunkSource();
        NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator) chunkSource.getGenerator();

        NoiseGeneratorSettings settings = generator.generatorSettings().value();

        Aquifer.FluidPicker fluidPicker = createFluidPicker();

        NoiseChunk noiseChunk = NoiseChunk.forChunk(
                chunk,
                randomState,
                Beardifier.forStructuresInChunk(structureManager, chunkPos),
                settings,
                fluidPicker,
                Blender.empty()
        );

        CarvingContext context = new CarvingContext(
                generator,
                region.registryAccess(),
                chunk.getHeightAccessorForGeneration(),
                noiseChunk,
                randomState,
                settings.surfaceRule()
        );

        CaveCarverConfiguration config = createConfig();

        Function<BlockPos, Holder<Biome>> biomeAccessor = pos -> region.getBiome(pos);

        Aquifer aquifer = createDisabledAquifer();

        CarvingMask carvingMask;
        if (chunk instanceof ProtoChunk protoChunk) {
            carvingMask = protoChunk.getOrCreateCarvingMask(GenerationStep.Carving.AIR);
        } else {
            carvingMask = new CarvingMask(chunk.getHeight(), chunk.getMinBuildHeight());
        }

        CARVER.carve(
                context,
                config,
                chunk,
                biomeAccessor,
                random,
                aquifer,
                chunkPos,
                carvingMask
        );
    }

    private static Aquifer.FluidPicker createFluidPicker() {
        return (x, y, z) -> new Aquifer.FluidStatus(
                Integer.MIN_VALUE,
                Blocks.AIR.defaultBlockState()
        );
    }

    private static Aquifer createDisabledAquifer() {
        return new Aquifer() {
            private final BlockState STONE = Blocks.STONE.defaultBlockState();
            private final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();

            @Override
            public BlockState computeSubstance(DensityFunction.FunctionContext context, double density) {
                return density > 0 ? STONE : CAVE_AIR;
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    private static CaveCarverConfiguration createConfig() {
        return new CaveCarverConfiguration(
                (float) RwgConfig.caveRarity,
                net.minecraft.world.level.levelgen.heightproviders.UniformHeight.of(
                        net.minecraft.world.level.levelgen.VerticalAnchor.absolute(RwgConfig.caveMinY),
                        net.minecraft.world.level.levelgen.VerticalAnchor.absolute(RwgConfig.caveMaxY)
                ),
                net.minecraft.util.valueproviders.UniformFloat.of(0.1f, 0.9f),
                net.minecraft.world.level.levelgen.VerticalAnchor.absolute(10),
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOrCreateTag(
                        net.minecraft.tags.BlockTags.OVERWORLD_CARVER_REPLACEABLES
                ),
                net.minecraft.util.valueproviders.UniformFloat.of(0.7f, 1.4f),
                net.minecraft.util.valueproviders.UniformFloat.of(0.8f, 1.3f),
                net.minecraft.util.valueproviders.UniformFloat.of(-1.0f, -0.4f)
        );
    }
}