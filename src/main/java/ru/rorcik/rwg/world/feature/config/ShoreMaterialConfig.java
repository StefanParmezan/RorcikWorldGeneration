package ru.rorcik.rwg.world.feature.config;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class ShoreMaterialConfig {

    public record Entry(Block block, int weight) {}

    private final List<Entry> entries;
    private final int totalWeight;

    private ShoreMaterialConfig(List<Entry> entries) {
        this.entries = List.copyOf(entries);
        int sum = 0;
        for (Entry e : entries) sum += e.weight;
        this.totalWeight = sum;
    }

    public Block pick(RandomSource random) {
        if (totalWeight <= 0) return null;
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Entry e : entries) {
            cumulative += e.weight;
            if (roll < cumulative) return e.block;
        }
        return entries.get(entries.size() - 1).block;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<Entry> entries = new ArrayList<>();

        public Builder add(Block block, int weight) {
            if (weight > 0) entries.add(new Entry(block, weight));
            return this;
        }

        public ShoreMaterialConfig build() {
            return new ShoreMaterialConfig(entries);
        }
    }
}