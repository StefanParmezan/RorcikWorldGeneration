package ru.rorcik.rwg.world.city;

import ru.rorcik.core.city.CityZone;
import ru.rorcik.core.city.CityZoneProvider;
import ru.rorcik.core.city.CityZoneProviders;
import ru.rorcik.core.zone.ZoneRules;

public final class CityZoneLookup {

    private CityZoneLookup() {
    }

    public static CityZone getZoneAtChunk(int chunkX, int chunkZ) {
        CityZoneProvider provider = CityZoneProviders.getProvider();
        if (provider != null) {
            CityZone zone = provider.getZoneAtChunk(chunkX, chunkZ);
            if (zone != null) return zone;
        }
        return legacyZoneAtChunk(chunkX, chunkZ);
    }

    public static CityZone getNearestZone(int blockX, int blockZ, int maxDistance) {
        CityZone best = null;
        double bestDistance = maxDistance;

        CityZoneProvider provider = CityZoneProviders.getProvider();
        if (provider != null) {
            for (CityZone zone : provider.getNearbyZones(blockX, blockZ, maxDistance)) {
                double distance = distanceToZone(blockX, blockZ, zone);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = zone;
                }
            }
        }

        GlobalCityRegistry.CityBounds legacy = GlobalCityRegistry.findNearestCity(blockX, blockZ, maxDistance);
        if (legacy != null) {
            double distance = GlobalCityRegistry.distanceToBoundingBox(blockX, blockZ, legacy);
            if (distance < bestDistance) {
                best = fromLegacyBounds(legacy);
            }
        }

        return best;
    }

    public static boolean isInCity(int blockX, int blockZ) {
        return getZoneAtChunk(blockX >> 4, blockZ >> 4) != null;
    }

    public static double distanceToZone(int blockX, int blockZ, CityZone zone) {
        int dx = Math.max(Math.max(zone.minBlockX() - blockX, 0), blockX - zone.maxBlockX());
        int dz = Math.max(Math.max(zone.minBlockZ() - blockZ, 0), blockZ - zone.maxBlockZ());
        return Math.sqrt((double) dx * dx + (double) dz * dz);
    }

    private static CityZone legacyZoneAtChunk(int chunkX, int chunkZ) {
        GlobalCityRegistry.CityZoneInfo info = GlobalCityRegistry.getZone(chunkX, chunkZ);
        if (info == null) return null;

        GlobalCityRegistry.CityBounds bounds = GlobalCityRegistry.findCityBoundsAt(
                (chunkX << 4) + 8, (chunkZ << 4) + 8
        );
        if (bounds == null) return null;

        return fromLegacyBounds(bounds);
    }

    private static CityZone fromLegacyBounds(GlobalCityRegistry.CityBounds bounds) {
        GlobalCityRegistry.CityZoneInfo info = bounds.info();
        return new CityZone(
                bounds.centerX(),
                bounds.centerZ(),
                new ZoneRules(info.typeId(), 0),
                info.targetHeight(),
                bounds.minX() >> 4,
                bounds.minZ() >> 4,
                bounds.maxX() >> 4,
                bounds.maxZ() >> 4
        );
    }
}
