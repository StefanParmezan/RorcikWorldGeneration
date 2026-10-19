package ru.rorcik.rwg.world.city;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import ru.rorcik.core.zone.ZoneRules;
import ru.rorcik.rwg.RwgConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CityZoneCache extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_NAME = "rwg_city_zone_cache";
    private final Map<Long, CityZoneInfo> chunkZoneMap = new ConcurrentHashMap<>();
    private final List<CityZoneInfo> allZones = new ArrayList<>();

    public record CityZoneInfo(int centerX, int centerZ, ZoneRules rules, int targetHeight) {}

    public static CityZoneCache get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CityZoneCache::load, CityZoneCache::new, DATA_NAME);
    }

    public static CityZoneCache load(CompoundTag tag) {
        CityZoneCache cache = new CityZoneCache();
        var zonesList = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < zonesList.size(); i++) {
            CompoundTag zoneTag = zonesList.getCompound(i);
            CityZoneInfo info = new CityZoneInfo(
                    zoneTag.getInt("centerX"),
                    zoneTag.getInt("centerZ"),
                    new ZoneRules(zoneTag.getString("id"), zoneTag.getInt("minDist")),
                    zoneTag.getInt("targetHeight")
            );
            cache.allZones.add(info);
            long[] chunksArray = zoneTag.getLongArray("chunks");
            for (long chunkKey : chunksArray) {
                cache.chunkZoneMap.put(chunkKey, info);
            }
        }
        if (RwgConfig.enableLogging) {
            LOGGER.info("Info: Loaded {} city zones ({} chunks mapped)", cache.allZones.size(), cache.chunkZoneMap.size());
        }
        return cache;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        var zonesList = new net.minecraft.nbt.ListTag();
        for (CityZoneInfo info : allZones) {
            CompoundTag zoneTag = new CompoundTag();
            zoneTag.putInt("centerX", info.centerX);
            zoneTag.putInt("centerZ", info.centerZ);
            zoneTag.putString("id", info.rules.id());
            zoneTag.putInt("minDist", info.rules.minDistance());
            zoneTag.putInt("targetHeight", info.targetHeight);
            List<Long> chunkKeysList = new ArrayList<>();
            for (Map.Entry<Long, CityZoneInfo> entry : chunkZoneMap.entrySet()) {
                if (entry.getValue() == info) {
                    chunkKeysList.add(entry.getKey());
                }
            }
            long[] chunksArray = new long[chunkKeysList.size()];
            for (int i = 0; i < chunkKeysList.size(); i++) {
                chunksArray[i] = chunkKeysList.get(i);
            }
            zoneTag.putLongArray("chunks", chunksArray);
            zonesList.add(zoneTag);
        }
        tag.put("zones", zonesList);
        return tag;
    }

    public synchronized void addZone(int centerX, int centerZ, ZoneRules rules, int targetHeight, Set<Long> affectedChunks) {
        CityZoneInfo info = new CityZoneInfo(centerX, centerZ, rules, targetHeight);
        allZones.add(info);
        for (long chunkKey : affectedChunks) {
            chunkZoneMap.put(chunkKey, info);
        }
        setDirty();
        if (RwgConfig.enableLogging) {
            LOGGER.info("Info: Added {} zone at [{}, {}] (targetHeight={}, {} chunks mapped)", rules.id(), centerX, centerZ, targetHeight, affectedChunks.size());
        }
    }

    public CityZoneInfo getZoneAtChunk(int chunkX, int chunkZ) {
        return chunkZoneMap.get(chunkKey(chunkX, chunkZ));
    }

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