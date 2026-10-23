package ru.rorcik.rwg.world.density;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.blending.Blender;
import ru.rorcik.rwg.world.city.GlobalCityRegistry;

public class CityPlateauFunction implements DensityFunction {

    private final DensityFunction originalDensity;
    private final int blendDistance;
    private final double plateauScale;

    public static final Codec<CityPlateauFunction> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(f -> f.originalDensity),
                    Codec.INT.optionalFieldOf("blend_distance", 64).forGetter(f -> f.blendDistance),
                    Codec.DOUBLE.optionalFieldOf("plateau_scale", 8.0).forGetter(f -> f.plateauScale)
            ).apply(instance, CityPlateauFunction::new)
    );

    public static final KeyDispatchDataCodec<? extends DensityFunction> KEY_CODEC =
            KeyDispatchDataCodec.of(DIRECT_CODEC);

    public CityPlateauFunction(DensityFunction originalDensity, int blendDistance, double plateauScale) {
        this.originalDensity = originalDensity;
        this.blendDistance = blendDistance;
        this.plateauScale = plateauScale;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();

        double original = originalDensity.compute(context);

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        GlobalCityRegistry.CityZoneInfo zone = GlobalCityRegistry.getZone(chunkX, chunkZ);

        if (zone != null) {
            
            return computePlateauDensity(y, zone.targetHeight());
        }

        GlobalCityRegistry.CityBounds nearestCity = GlobalCityRegistry.findNearestCity(x, z, blendDistance);
        if (nearestCity == null) {
            return original;
        }

        double distance = GlobalCityRegistry.distanceToBoundingBox(x, z, nearestCity);
        double t = Mth.clamp(distance / blendDistance, 0.0, 1.0);

        double smoothT = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
        double blendFactor = 1.0 - smoothT; 

        if (blendFactor <= 0.0) {
            return original;
        }

        int cityTargetHeight = nearestCity.info().targetHeight();
        double slopeNoise = getSlopeNoise(x, z) * blendFactor * (1.0 - blendFactor) * 8.0;
        double effectiveCityHeight = cityTargetHeight + slopeNoise;

        double naturalHeight = estimateNaturalHeight(context, effectiveCityHeight);

        double blendedHeight = Mth.lerp(blendFactor, naturalHeight, effectiveCityHeight);
        double monotonicDensity = (blendedHeight - y) / plateauScale;

        if (y < naturalHeight) {
            
            return Mth.lerp(blendFactor, original, monotonicDensity);
        } else {
            
            return monotonicDensity;
        }
    }

    private double estimateNaturalHeight(FunctionContext context, double referenceY) {
        int y1 = Mth.floor(referenceY - 8.0);
        int y2 = Mth.floor(referenceY + 8.0);

        double d1 = sampleOriginalDensity(context, y1);
        double d2 = sampleOriginalDensity(context, y2);

        if (Math.abs(d1 - d2) < 1.0e-4) {
            return referenceY;
        }

        double h = y1 + (0.0 - d1) * (y2 - y1) / (d2 - d1);
        
        return Mth.clamp(h, referenceY - 64.0, referenceY + 64.0);
    }

    private double sampleOriginalDensity(FunctionContext context, int y) {
        return originalDensity.compute(new ShiftedContext(context, y));
    }

    private static class ShiftedContext implements DensityFunction.FunctionContext {
        private final DensityFunction.FunctionContext delegate;
        private final int y;

        private ShiftedContext(DensityFunction.FunctionContext delegate, int y) {
            this.delegate = delegate;
            this.y = y;
        }

        @Override
        public int blockX() {
            return delegate.blockX();
        }

        @Override
        public int blockY() {
            return y;
        }

        @Override
        public int blockZ() {
            return delegate.blockZ();
        }

        @Override
        public Blender getBlender() {
            return delegate.getBlender();
        }
    }

    private static double getSlopeNoise(int x, int z) {
        double freq = 0.06;
        double nx = x * freq;
        double nz = z * freq;

        return (Math.sin(nx) + Math.cos(nz) + Math.sin(nx * 2.3 + nz * 1.7)) * 1.2;
    }

    @Override
    public void fillArray(double[] array, ContextProvider provider) {
        for (int i = 0; i < array.length; i++) {
            array[i] = compute(provider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new CityPlateauFunction(
                originalDensity.mapAll(visitor),
                blendDistance,
                plateauScale
        ));
    }

    @Override
    public double minValue() {
        return -64.0;
    }

    @Override
    public double maxValue() {
        return 64.0;
    }

    private double computePlateauDensity(int y, int targetHeight) {
        return (targetHeight - y) / plateauScale;
    }
}