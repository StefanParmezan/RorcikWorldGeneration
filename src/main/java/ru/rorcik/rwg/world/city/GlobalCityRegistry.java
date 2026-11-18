package ru.rorcik.rwg.world.city;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import ru.rorcik.rwg.RwgConfig;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalCityRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentHashMap<Long, CityZoneInfo> CITY_CHUNKS = new ConcurrentHashMap<>();
    private static final List<CityBounds> CITIES = new CopyOnWriteArrayList<>();

    public record CityZoneInfo(String typeId, int targetHeight) {}

    public record CityBounds(int centerX, int centerZ, int minX, int maxX, int minZ, int maxZ, CityZoneInfo info) {}

    public static void registerCity(int centerX, int centerZ, Set<Long> chunks, CityZoneInfo info) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (long key : chunks) {
            int cx = (int)(key >> 32);
            int cz = (int)(key & 0xFFFFFFFFL);
            minX = Math.min(minX, cx);
            maxX = Math.max(maxX, cx);
            minZ = Math.min(minZ, cz);
            maxZ = Math.max(maxZ, cz);
            CITY_CHUNKS.put(key, info);
        }
        CityBounds bounds = new CityBounds(centerX, centerZ, minX << 4, (maxX << 4) | 15, minZ << 4, (maxZ << 4) | 15, info);
        CITIES.add(bounds);
        if (RwgConfig.enableLogging) {
            LOGGER.info("Info: Registered city '{}' at [{}, {}], size: {} chunks, bounds: [{},{}] - [{},{}]",
                    info.typeId(), centerX, centerZ, chunks.size(), bounds.minX(), bounds.minZ(), bounds.maxX(), bounds.maxZ());
        }
    }

    public static CityZoneInfo getZone(int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        return CITY_CHUNKS.get(key);
    }

    public static boolean isRegistered(int centerX, int centerZ) {
        for (CityBounds city : CITIES) {
            if (city.centerX() == centerX && city.centerZ() == centerZ) {
                return true;
            }
        }
        return false;
    }

    public static CityBounds findNearestCity(int blockX, int blockZ, int maxDistance) {
        CityBounds nearest = null;
        double minDist = maxDistance;
        for (CityBounds city : CITIES) {
            double dist = distanceToBoundingBox(blockX, blockZ, city);
            if (dist < minDist) {
                minDist = dist;
                nearest = city;
            }
        }
        return nearest;
    }

    public static CityBounds findCityBoundsAt(int blockX, int blockZ) {
        for (CityBounds city : CITIES) {
            if (blockX >= city.minX() && blockX <= city.maxX()
                    && blockZ >= city.minZ() && blockZ <= city.maxZ()) {
                return city;
            }
        }
        return null;
    }

    public static double distanceToBoundingBox(int x, int z, CityBounds bb) {
        int dx = Math.max(Math.max(bb.minX() - x, 0), x - bb.maxX());
        int dz = Math.max(Math.max(bb.minZ() - z, 0), z - bb.maxZ());
        return Math.sqrt((double)dx * dx + (double)dz * dz);
    }

    public static void clear() {
        if (RwgConfig.enableLogging) {
            LOGGER.info("Cleanup: Cleared registry ({} entries, {} cities)", CITY_CHUNKS.size(), CITIES.size());
        }
        CITY_CHUNKS.clear();
        CITIES.clear();
    }

    public static int size() {
        return CITY_CHUNKS.size();
    }
}
