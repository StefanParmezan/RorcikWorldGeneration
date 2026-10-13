package ru.rorcik.rwg.world.city;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import ru.rorcik.core.zone.ZoneRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Глобальный кэш зон городов для быстрого O(1) доступа во время генерации.
 * Заполняется при получении события ZoneReservedEvent из RorcikStructures.
 */
public class CityZoneCache extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "rwg_city_zone_cache";

    // Быстрый доступ по координатам чанка
    private final Map<Long, CityZoneInfo> chunkZoneMap = new ConcurrentHashMap<>();

    // Список всех зон для итерации
    private final List<CityZoneInfo> allZones = new ArrayList<>();

    public record CityZoneInfo(
            int centerX,
            int centerZ,
            ZoneRules rules,
            int targetHeight
    ) {}

    public static CityZoneCache get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CityZoneCache::load,
                CityZoneCache::new,
                DATA_NAME
        );
    }

    public static CityZoneCache load(CompoundTag tag) {
        CityZoneCache cache = new CityZoneCache();
        ListTag list = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag zoneTag = list.getCompound(i);
            CityZoneInfo info = new CityZoneInfo(
                    zoneTag.getInt("centerX"),
                    zoneTag.getInt("centerZ"),
                    new ZoneRules(
                            zoneTag.getString("id"),
                            zoneTag.getInt("minDist"),
                            zoneTag.getInt("radius")
                    ),
                    zoneTag.getInt("targetHeight")
            );
            cache.allZones.add(info);

            // Добавляем в карту все чанки в радиусе зоны
            int chunkRadius = (info.rules.estimatedRadius() + 15) / 16;
            int centerChunkX = info.centerX >> 4;
            int centerChunkZ = info.centerZ >> 4;
            for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                    long key = chunkKey(centerChunkX + dx, centerChunkZ + dz);
                    cache.chunkZoneMap.put(key, info);
                }
            }
        }
        LOGGER.info("[CITY-CACHE] Loaded {} city zones ({} chunks mapped)",
                cache.allZones.size(), cache.chunkZoneMap.size());
        return cache;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (CityZoneInfo info : allZones) {
            CompoundTag zoneTag = new CompoundTag();
            zoneTag.putInt("centerX", info.centerX);
            zoneTag.putInt("centerZ", info.centerZ);
            zoneTag.putString("id", info.rules.id());
            zoneTag.putInt("minDist", info.rules.minDistance());
            zoneTag.putInt("radius", info.rules.estimatedRadius());
            zoneTag.putInt("targetHeight", info.targetHeight);
            list.add(zoneTag);
        }
        tag.put("zones", list);
        return tag;
    }

    /**
     * Добавляет новую зону в кэш. Вызывается при получении ZoneReservedEvent.
     */
    public synchronized void addZone(int centerX, int centerZ, ZoneRules rules, int targetHeight) {
        CityZoneInfo info = new CityZoneInfo(centerX, centerZ, rules, targetHeight);
        allZones.add(info);

        // Добавляем в карту все чанки в радиусе зоны
        int chunkRadius = (rules.estimatedRadius() + 15) / 16;
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                long key = chunkKey(centerChunkX + dx, centerChunkZ + dz);
                chunkZoneMap.put(key, info);
            }
        }

        setDirty();
        LOGGER.info("[CITY-CACHE] Added {} zone at [{}, {}] (targetHeight={}, radius={} blocks, {} chunks mapped)",
                rules.id(), centerX, centerZ, targetHeight, rules.estimatedRadius(), chunkZoneMap.size());
    }

    /**
     * Проверяет, находится ли чанк в зоне города.
     * @return информация о зоне или null
     */
    public CityZoneInfo getZoneAtChunk(int chunkX, int chunkZ) {
        return chunkZoneMap.get(chunkKey(chunkX, chunkZ));
    }

    /**
     * Проверяет, находится ли блок в зоне города.
     * @return информация о зоне или null
     */
    public CityZoneInfo getZoneAtBlock(int blockX, int blockZ) {
        return getZoneAtChunk(blockX >> 4, blockZ >> 4);
    }

    public List<CityZoneInfo> getAllZones() {
        return new ArrayList<>(allZones);
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}