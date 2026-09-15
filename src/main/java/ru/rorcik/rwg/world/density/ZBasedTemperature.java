package ru.rorcik.rwg.world.density;

import com.mojang.serialization.Codec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ZBasedTemperature() implements DensityFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger("RWG_TempDebug");
    private static int debugCounter = 0;

    private static final double PERIOD = 46000.0;
    private static final double HALF_PERIOD = 23000.0;

    public static final Codec<ZBasedTemperature> CODEC = Codec.unit(ZBasedTemperature::new);
    public static final KeyDispatchDataCodec<? extends DensityFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }

    @Override
    public double compute(FunctionContext context) {
        double x = context.blockX();
        double z = context.blockZ();
        double distance = Math.sqrt(x * x + z * z);

        double distMod = ((distance % PERIOD) + PERIOD) % PERIOD;

        double temperature;
        if (distMod <= HALF_PERIOD) {
            temperature = ascendingTemp(distMod);
        } else {
            temperature = ascendingTemp(PERIOD - distMod);
        }

        double finalTemp = Mth.clamp(temperature, -1.0, 1.0);

        if (debugCounter < 10 && distance >= 10000 && distance <= 12000) {
            LOGGER.warn(">>> [RWG DEBUG] X: {} | Z: {} | Distance: {} | Temp: {}",
                    x, z, distance, finalTemp);
            debugCounter++;
        }

        return finalTemp;
    }

    @Override
    public void fillArray(double[] array, ContextProvider provider) {
        for (int i = 0; i < array.length; i++) {
            array[i] = compute(provider.forIndex(i));
        }
    }

    private static double ascendingTemp(double distance) {
        if (distance <= 2000)  return lerp(distance, 0, 2000, -1.0, -0.7);
        if (distance <= 5000)  return lerp(distance, 2000, 5000, -0.7, -0.4);
        if (distance <= 9000)  return lerp(distance, 5000, 9000, -0.4, -0.1);
        if (distance <= 14000) return lerp(distance, 9000, 14000, -0.1, 0.2);
        if (distance <= 18000) return lerp(distance, 14000, 18000, 0.2, 0.5);
        if (distance <= 21000) return lerp(distance, 18000, 21000, 0.5, 0.8);
        if (distance <= 23000) return lerp(distance, 21000, 23000, 0.8, 1.0);
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