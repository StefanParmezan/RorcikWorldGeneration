package ru.rorcik.rwg.world.feature.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import ru.rorcik.rwg.world.city.CityZoneLookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StructureGuard {

    private static final StructureGuard EMPTY = new StructureGuard(List.of());

    private final List<BoundingBox> boxes;

    private StructureGuard(List<BoundingBox> boxes) {
        this.boxes = boxes;
    }

    public static StructureGuard forArea(WorldGenLevel level, int minX, int minZ, int maxX, int maxZ) {
        if (!(level instanceof WorldGenRegion region)) return EMPTY;

        StructureManager manager = region.getLevel().structureManager().forWorldGenRegion(region);
        if (!manager.shouldGenerateStructures()) return EMPTY;

        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        Map<Structure, LongSet> refs = new HashMap<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                manager.getAllStructuresAt(new BlockPos(chunkX << 4, 0, chunkZ << 4))
                        .forEach((structure, set) -> refs.merge(structure, set, StructureGuard::mergeSets));
            }
        }
        if (refs.isEmpty()) return EMPTY;

        List<BoundingBox> boxes = new ArrayList<>();
        for (Map.Entry<Structure, LongSet> entry : refs.entrySet()) {
            manager.fillStartsForStructure(entry.getKey(), entry.getValue(), start -> {
                if (!start.isValid()) return;
                for (StructurePiece piece : start.getPieces()) {
                    boxes.add(piece.getBoundingBox());
                }
            });
        }

        return boxes.isEmpty() ? EMPTY : new StructureGuard(List.copyOf(boxes));
    }

    private static LongSet mergeSets(LongSet first, LongSet second) {
        LongSet merged = new LongOpenHashSet(first);
        merged.addAll(second);
        return merged;
    }

    public boolean contains(BlockPos pos) {
        for (BoundingBox box : boxes) {
            if (box.isInside(pos)) return true;
        }
        return false;
    }

    public boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        for (BoundingBox box : boxes) {
            if (box.intersects(minX, minZ, maxX, maxZ)
                    && box.maxY() >= minY
                    && box.minY() <= maxY) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return boxes.isEmpty();
    }

    public static boolean isInCity(WorldGenLevel level, BlockPos pos) {
        if (!level.getLevel().dimension().equals(Level.OVERWORLD)) return false;
        return CityZoneLookup.getZoneAtChunk(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }
}
