package ru.rorcik.rwg.world.city;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalCityRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentHashMap<Long, CityZoneInfo> CITY_CHUNKS = new ConcurrentHashMap<>();

    public record CityZoneInfo(String typeId, int targetHeight) {}

    public static void registerChunk(int chunkX, int chunkZ, String typeId, int targetHeight) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        CITY_CHUNKS.put(key, new CityZoneInfo(typeId, targetHeight));
    }

    public static CityZoneInfo getZone(int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
        CityZoneInfo info = CITY_CHUNKS.get(key);

        return info;
    }

    public static void clear() {
        LOGGER.info("[GLOBAL-REGISTRY] Очистка реестра ({} записей)", CITY_CHUNKS.size());
        CITY_CHUNKS.clear();
    }

    public static int size() {
        return CITY_CHUNKS.size();
    }
}