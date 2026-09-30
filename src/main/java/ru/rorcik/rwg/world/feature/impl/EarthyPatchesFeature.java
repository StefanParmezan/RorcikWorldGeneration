package ru.rorcik.rwg.world.feature.impl;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import ru.rorcik.rwg.RwgBlocks;
import ru.rorcik.rwg.RwgConfig;
import ru.rorcik.rwg.block.BuddingDioriteBlock;
import ru.rorcik.rwg.world.feature.config.ShoreMaterialConfig;
import ru.rorcik.rwg.world.feature.util.SurfaceFinder;

import java.util.ArrayList;
import java.util.List;

public class EarthyPatchesFeature extends Feature<NoneFeatureConfiguration> {

    private static final float DEAD_BUSH_CHANCE      = 0.05f;
    private static final float FERN_CHANCE           = 0.22f;
    private static final float LARGE_FERN_RATIO      = 0.40f;
    private static final float MOSS_CHANCE           = 0.35f;
    private static final float DRIPSTONE_CHANCE      = 0.25f;
    private static final float DRY_GRASS_CHANCE      = 0.3f;

    private static final float MUD_TALL_GRASS_P = 0.245f;
    private static final float MUD_GRASS_P      = 0.49f;
    private static final float MUD_FERN_P       = 0.735f;
    private static final float MUD_LARGE_FERN_P = 0.98f;
    private static final float MUD_PITCHER_P    = 0.99f;

    private static final int SHORE_RADIUS = 3;

    private enum PatchKind { EARTHY, STONE, SANDY, RED_SANDY }

    public EarthyPatchesFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos surfacePos = SurfaceFinder.findSurface(level, origin.getX(), origin.getZ());
        BlockState topState = level.getBlockState(surfacePos);

        if (!isValidSurface(topState)) return false;

        PatchKind kind = pickPatchKind(level, surfacePos);

        if (isNearWater(level, surfacePos)) {
            return placeShorePatch(level, origin, random, kind);
        }

        Block patchBlock = pickPatchBlock(kind, random);

        int radius = 1;
        int placed = 0;
        List<BlockPos> buddingPositions = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;

                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos pos = SurfaceFinder.findSurface(level, x, z);
                BlockState state = level.getBlockState(pos);

                if (!isValidSurface(state)) continue;

                level.setBlock(pos, patchBlock.defaultBlockState(), 3);
                placed++;

                if (patchBlock == RwgBlocks.BUDDING_DIORITE.get()) {
                    buddingPositions.add(pos.immutable());
                }

                if (!RwgConfig.enableEarthyPatchDecorations) continue;

                BlockPos abovePos = pos.above();
                if (!level.getBlockState(abovePos).isAir()) continue;

                switch (kind) {
                    case EARTHY    -> decorateEarthy(level, abovePos, patchBlock, random);
                    case STONE     -> decorateStone(level, abovePos, patchBlock, random);
                    case SANDY     -> decorateSandy(level, abovePos, patchBlock, random);
                    case RED_SANDY -> decorateSandy(level, abovePos, patchBlock, random);
                }
            }
        }

        for (BlockPos buddingPos : buddingPositions) {
            BuddingDioriteBlock.trySeedInitialCrystal(level, buddingPos, random);
        }

        return placed > 0;
    }

    private boolean placeShorePatch(WorldGenLevel level, BlockPos origin,
                                    RandomSource random, PatchKind kind) {
        ShoreMaterialConfig config = shoreConfigFor(kind);
        if (config.isEmpty()) return false;

        int waterY = level.getSeaLevel() - 1;
        boolean allowDryGrass = (kind == PatchKind.SANDY || kind == PatchKind.RED_SANDY);

        int radius = 2;
        int placed = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;

                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;

                BlockPos pos = findShoreSurface(level, x, z, waterY);
                if (pos == null) continue;

                BlockState state = level.getBlockState(pos);

                if (state.getBlock() instanceof SlabBlock) continue;
                if (!isValidSurface(state)) continue;

                BlockPos abovePos = pos.above();
                BlockState aboveState = level.getBlockState(abovePos);
                if (aboveState.is(Blocks.LARGE_FERN)) continue;
                if (level.getBlockState(pos.above(2)).is(Blocks.LARGE_FERN)) continue;

                Block chosen = config.pick(random);
                if (chosen == null) continue;
                if (state.is(chosen)) continue;

                if (!aboveState.isAir()
                        && aboveState.getFluidState().isEmpty()
                        && aboveState.canBeReplaced()) {
                    level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 3);
                }

                level.setBlock(pos, chosen.defaultBlockState(), 2);
                placed++;

                applyShoreDecorations(level, pos, chosen, random, allowDryGrass);
            }
        }
        return placed > 0;
    }

    private void applyShoreDecorations(WorldGenLevel level, BlockPos pos,
                                       Block chosen, RandomSource random,
                                       boolean allowDryGrass) {
        if (!RwgConfig.enableEarthyPatchDecorations) return;

        if (chosen == Blocks.MUD) {
            decorateMud(level, pos, random);
            return;
        }

        if (isStoneTypeBlock(chosen)) {
            placeDoublePlant(level, pos, Blocks.TALL_GRASS);
            return;
        }

        if (allowDryGrass
                && (chosen == Blocks.SAND
                || chosen == Blocks.RED_SAND
                || chosen == Blocks.SUSPICIOUS_SAND)) {
            BlockPos abovePos = pos.above();
            if (!level.getBlockState(abovePos).isAir()) return;
            if (random.nextFloat() < DRY_GRASS_CHANCE) {
                level.setBlock(abovePos,
                        RwgBlocks.DRY_GRASS.get().defaultBlockState(), 3);
            }
        }
    }

    private void decorateMud(WorldGenLevel level, BlockPos pos, RandomSource random) {
        BlockPos lower = pos.above();
        BlockState lowerState = level.getBlockState(lower);

        if (!lowerState.isAir() && !lowerState.canBeReplaced()) return;

        float roll = random.nextFloat();

        if (roll < MUD_TALL_GRASS_P) {
            placeDoublePlant(level, pos, Blocks.TALL_GRASS);
            return;
        }
        if (roll < MUD_GRASS_P) {
            level.setBlock(lower, Blocks.GRASS.defaultBlockState(), 3);
            return;
        }
        if (roll < MUD_FERN_P) {
            level.setBlock(lower, Blocks.FERN.defaultBlockState(), 3);
            return;
        }
        if (roll < MUD_LARGE_FERN_P) {
            placeDoublePlant(level, pos, Blocks.LARGE_FERN);
            return;
        }
        if (roll < MUD_PITCHER_P) {
            placeDoublePlant(level, pos, Blocks.PITCHER_PLANT);
            return;
        }
        level.setBlock(lower, Blocks.TORCHFLOWER.defaultBlockState(), 3);
    }

    private void placeDoublePlant(WorldGenLevel level, BlockPos below, Block plant) {
        BlockPos lower = below.above();
        BlockPos upper = below.above(2);

        BlockState lowerState = level.getBlockState(lower);
        BlockState upperState = level.getBlockState(upper);

        if (!plant.equals(Blocks.LARGE_FERN)) {
            if (lowerState.is(Blocks.LARGE_FERN)) return;
            if (upperState.is(Blocks.LARGE_FERN)) return;
        }

        if (!lowerState.isAir() && !lowerState.canBeReplaced()) return;
        if (!upperState.isAir() && !upperState.canBeReplaced()) return;

        if (!lowerState.isAir()) {
            level.setBlock(lower, Blocks.AIR.defaultBlockState(), 3);
        }
        if (!upperState.isAir()) {
            level.setBlock(upper, Blocks.AIR.defaultBlockState(), 3);
        }

        level.setBlock(lower, plant.defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 3);
        level.setBlock(upper, plant.defaultBlockState()
                .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 3);
    }

    private boolean isStoneTypeBlock(Block block) {
        return block == Blocks.STONE
                || block == Blocks.COBBLESTONE
                || block == Blocks.MOSSY_COBBLESTONE
                || block == Blocks.ANDESITE
                || block == Blocks.DIORITE
                || block == Blocks.GRANITE
                || block == Blocks.TUFF
                || block == Blocks.DEEPSLATE
                || block == Blocks.CALCITE
                || block == Blocks.BLACKSTONE
                || block == Blocks.BASALT
                || block == Blocks.SMOOTH_BASALT;
    }

    private BlockPos findShoreSurface(WorldGenLevel level, int x, int z, int waterY) {
        for (int y = waterY + 1; y >= waterY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState s = level.getBlockState(pos);

            if (s.isAir()) continue;
            if (!s.getFluidState().isEmpty()) continue;
            if (s.is(BlockTags.LEAVES)) continue;
            if (s.is(BlockTags.LOGS)) continue;

            if (s.getBlock() instanceof SlabBlock) return null;

            BlockPos abovePos = pos.above();
            BlockState above = level.getBlockState(abovePos);
            if (!above.isAir() && above.getFluidState().isEmpty()) {
                VoxelShape shape = above.getCollisionShape(level, abovePos);
                if (!shape.isEmpty()) continue;
            }

            return pos;
        }
        return null;
    }

    private ShoreMaterialConfig shoreConfigFor(PatchKind kind) {
        return switch (kind) {
            case EARTHY    -> BiomeShoreMaterials.EARTHY;
            case STONE     -> BiomeShoreMaterials.STONE;
            case SANDY     -> BiomeShoreMaterials.SANDY;
            case RED_SANDY -> BiomeShoreMaterials.RED_SANDY;
        };
    }

    private boolean isNearWater(WorldGenLevel level, BlockPos center) {
        int waterY = level.getSeaLevel() - 1;
        int cx = center.getX();
        int cz = center.getZ();
        for (int dx = -SHORE_RADIUS; dx <= SHORE_RADIUS; dx++) {
            for (int dz = -SHORE_RADIUS; dz <= SHORE_RADIUS; dz++) {
                BlockPos check = new BlockPos(cx + dx, waterY, cz + dz);
                if (level.getBlockState(check).getFluidState().is(Fluids.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }


    private void decorateSandy(WorldGenLevel level, BlockPos abovePos,
                               Block patchBlock, RandomSource random) {
        if (patchBlock == Blocks.SUSPICIOUS_SAND) {
            if (random.nextFloat() < DRY_GRASS_CHANCE) {
                level.setBlock(abovePos, RwgBlocks.DRY_GRASS.get().defaultBlockState(), 3);
            }
        }
    }

    private Block pickPatchBlock(PatchKind kind, RandomSource random) {
        return switch (kind) {
            case EARTHY    -> pickEarthyPatchBlock(random);
            case STONE     -> pickStonePatchBlock(random);
            case SANDY     -> pickSandyPatchBlock(random);
            case RED_SANDY -> pickRedSandyPatchBlock(random);
        };
    }

    private void decorateEarthy(WorldGenLevel level, BlockPos abovePos,
                                Block patchBlock, RandomSource random) {
        if (patchBlock == Blocks.PODZOL
                && RwgConfig.enableDeadBushOnPodzol
                && random.nextFloat() < DEAD_BUSH_CHANCE) {
            level.setBlock(abovePos, Blocks.DEAD_BUSH.defaultBlockState(), 3);
        }
        else if (patchBlock == Blocks.COARSE_DIRT
                && RwgConfig.enableFernOnCoarseDirt
                && random.nextFloat() < FERN_CHANCE) {
            placeFern(level, abovePos, random);
        }
        else if (patchBlock == Blocks.MOSSY_COBBLESTONE
                && RwgConfig.enableMossOnMossyCobblestone
                && random.nextFloat() < MOSS_CHANCE) {
            level.setBlock(abovePos, Blocks.MOSS_CARPET.defaultBlockState(), 3);
        }
    }

    private void decorateStone(WorldGenLevel level, BlockPos abovePos,
                               Block patchBlock, RandomSource random) {
        if (patchBlock == Blocks.DRIPSTONE_BLOCK
                && random.nextFloat() < DRIPSTONE_CHANCE) {
            placeStalagmite(level, abovePos, random);
        }
    }

    private boolean isValidSurface(BlockState s) {
        if (s.getBlock() instanceof SlabBlock) return false;
        return s.is(Blocks.GRASS_BLOCK)
                || s.is(Blocks.DIRT)
                || s.is(Blocks.MUD)
                || s.is(Blocks.COARSE_DIRT)
                || s.is(Blocks.PODZOL)
                || s.is(Blocks.STONE)
                || s.is(Blocks.DEEPSLATE)
                || s.is(Blocks.GRANITE)
                || s.is(Blocks.DIORITE)
                || s.is(Blocks.ANDESITE)
                || s.is(Blocks.TUFF)
                || s.is(Blocks.CALCITE)
                || s.is(Blocks.GRAVEL)
                || s.is(Blocks.SUSPICIOUS_GRAVEL)
                || s.is(Blocks.SAND)
                || s.is(Blocks.SUSPICIOUS_SAND)
                || s.is(Blocks.RED_SAND)
                || s.is(Blocks.SANDSTONE)
                || s.is(Blocks.SMOOTH_SANDSTONE)
                || s.is(Blocks.RED_SANDSTONE)
                || s.is(Blocks.SMOOTH_RED_SANDSTONE);
    }

    private PatchKind pickPatchKind(WorldGenLevel level, BlockPos pos) {
        String name = level.getBiome(pos).unwrapKey()
                .map(k -> k.location().toString())
                .orElse("");

        if (name.contains("badlands")) return PatchKind.RED_SANDY;
        if (name.equals("minecraft:desert") || name.equals("minecraft:beach")) {
            return PatchKind.SANDY;
        }
        if (isMountainBiome(name)) return PatchKind.STONE;
        return PatchKind.EARTHY;
    }

    private boolean isMountainBiome(String name) {
        return name.contains("stony")
                || name.contains("jagged_peaks")
                || name.contains("frozen_peaks")
                || name.contains("snowy_slopes")
                || name.contains("grove")
                || name.contains("peaks")
                || name.contains("slopes")
                || name.contains("cliffs");
    }

    private void placeFern(WorldGenLevel level, BlockPos lowerPos, RandomSource random) {
        if (random.nextFloat() < LARGE_FERN_RATIO) {
            BlockPos upperPos = lowerPos.above();
            if (level.getBlockState(upperPos).isAir()) {
                level.setBlock(lowerPos, Blocks.LARGE_FERN.defaultBlockState()
                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), 3);
                level.setBlock(upperPos, Blocks.LARGE_FERN.defaultBlockState()
                        .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), 3);
                return;
            }
        }
        level.setBlock(lowerPos, Blocks.FERN.defaultBlockState(), 3);
    }

    private void placeStalagmite(WorldGenLevel level, BlockPos basePos, RandomSource random) {
        int height = 1 + random.nextInt(3);
        for (int i = 0; i < height; i++) {
            BlockPos pos = basePos.above(i);
            if (!level.getBlockState(pos).isAir()) break;

            boolean isTip = (i == height - 1);
            BlockState state = Blocks.POINTED_DRIPSTONE.defaultBlockState()
                    .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.UP)
                    .setValue(PointedDripstoneBlock.THICKNESS,
                            isTip ? DripstoneThickness.TIP : DripstoneThickness.FRUSTUM);
            level.setBlock(pos, state, 3);
        }
    }

    private Block pickEarthyPatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 30) return Blocks.PODZOL;
        if (roll < 55) return Blocks.COARSE_DIRT;
        if (roll < 80) return Blocks.MOSS_BLOCK;
        return Blocks.MOSSY_COBBLESTONE;
    }

    private Block pickStonePatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 8) return RwgBlocks.BUDDING_DIORITE.get();
        if (roll < 35) return Blocks.GRANITE;
        if (roll < 58) return Blocks.DIORITE;
        if (roll < 80) return Blocks.ANDESITE;
        if (roll < 92) return Blocks.TUFF;
        return Blocks.DRIPSTONE_BLOCK;
    }

    private Block pickSandyPatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 15) return Blocks.SANDSTONE;
        if (roll < 30) return Blocks.SMOOTH_SANDSTONE;
        if (roll < 40) return Blocks.SUSPICIOUS_SAND;
        return Blocks.SAND;
    }

    private Block pickRedSandyPatchBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 15) return Blocks.RED_SANDSTONE;
        if (roll < 30) return Blocks.SMOOTH_RED_SANDSTONE;
        return Blocks.RED_SAND;
    }
}