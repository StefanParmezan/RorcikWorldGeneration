package ru.rorcik.rwg.world.density;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ConcentricTemperature() implements DensityFunction {

    private static final double CYCLE = 46000.0;
    
    private static final double HALF_CYCLE = CYCLE * 0.5;

    public static final Codec<ConcentricTemperature> CODEC = Codec.unit(ConcentricTemperature::new);
    public static final KeyDispatchDataCodec<? extends DensityFunction> KEY_CODEC =
            KeyDispatchDataCodec.of(CODEC);

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }

    @Override
    public double compute(FunctionContext context) {
        
        double x = context.blockX();
        double z = context.blockZ();
        double distance = Math.sqrt(x * x + z * z);

        double phase = distance % CYCLE;
        double sampled = (phase <= HALF_CYCLE) ? phase : (CYCLE - phase);

        return Mth.clamp(temperatureForDistance(sampled), -1.0, 1.0);
    }

    @Override
    public void fillArray(double[] array, ContextProvider provider) {
        for (int i = 0; i < array.length; i++) {
            array[i] = compute(provider.forIndex(i));
        }
    }

    private static double temperatureForDistance(double distance) {
        if (distance <=  2000.0) return lerp(distance,     0.0,  2000.0, -1.0, -0.7);
        if (distance <=  5000.0) return lerp(distance,  2000.0,  5000.0, -0.7, -0.4);
        if (distance <=  9000.0) return lerp(distance,  5000.0,  9000.0, -0.4, -0.1);
        if (distance <= 14000.0) return lerp(distance,  9000.0, 14000.0, -0.1,  0.2);
        if (distance <= 18000.0) return lerp(distance, 14000.0, 18000.0,  0.2,  0.5);
        if (distance <= 21000.0) return lerp(distance, 18000.0, 21000.0,  0.5,  0.8);
        if (distance <= HALF_CYCLE) return lerp(distance, 21000.0, HALF_CYCLE, 0.8, 1.0);
        return 1.0;
    }

    private static double lerp(double x, double x0, double x1, double y0, double y1) {
        double t = (x - x0) / (x1 - x0);
        return y0 + t * (y1 - y0);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(this);
    }

    @Override
    public double minValue() { return -1.0; }

    @Override
    public double maxValue() { return 1.0; }
}