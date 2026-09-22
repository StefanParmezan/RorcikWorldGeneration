package ru.rorcik.rwg.world.surface;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.api.SurfaceRuleManager;

public class RwgSurfaceRules {

    public static void init() {
        SurfaceRuleManager.addSurfaceRules(
                SurfaceRuleManager.RuleCategory.OVERWORLD,
                "rwg",
                makeRules()
        );
    }

    private static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.ConditionSource isUnderWater = SurfaceRules.waterBlockCheck(1, 0);

        SurfaceRules.ConditionSource isSurface = SurfaceRules.ON_FLOOR;
        SurfaceRules.ConditionSource isSubSurface = SurfaceRules.UNDER_FLOOR;

        SurfaceRules.ConditionSource sandNoise = SurfaceRules.noiseCondition(Noises.SURFACE, 0.1, 0.35);
        SurfaceRules.ConditionSource gravelNoise = SurfaceRules.noiseCondition(Noises.SURFACE, 0.35, 0.55);
        SurfaceRules.ConditionSource clayNoise = SurfaceRules.noiseCondition(Noises.SURFACE, 0.85, 0.92);
        SurfaceRules.ConditionSource mudNoise = SurfaceRules.noiseCondition(Noises.SURFACE, 0.55, 0.75);

        SurfaceRules.RuleSource riverBed = SurfaceRules.sequence(
                SurfaceRules.ifTrue(sandNoise, SurfaceRules.state(Blocks.SAND.defaultBlockState())),
                SurfaceRules.ifTrue(gravelNoise, SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())),
                SurfaceRules.ifTrue(clayNoise, SurfaceRules.state(Blocks.CLAY.defaultBlockState())),
                SurfaceRules.ifTrue(mudNoise, SurfaceRules.state(Blocks.MUD.defaultBlockState())),
                SurfaceRules.state(Blocks.DIRT.defaultBlockState())
        );

        SurfaceRules.ConditionSource podzolNoise = SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, 0.78, 0.85);
        SurfaceRules.ConditionSource coarseDirtNoise = SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, 0.68, 0.75);
        SurfaceRules.ConditionSource mossNoise = SurfaceRules.noiseCondition(Noises.SURFACE_SECONDARY, 0.90, 0.94);

        SurfaceRules.RuleSource organicPatches = SurfaceRules.sequence(
                SurfaceRules.ifTrue(podzolNoise, SurfaceRules.state(Blocks.PODZOL.defaultBlockState())),
                SurfaceRules.ifTrue(coarseDirtNoise, SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState())),
                SurfaceRules.ifTrue(mossNoise, SurfaceRules.state(Blocks.MOSS_BLOCK.defaultBlockState()))
        );

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(isUnderWater,
                        SurfaceRules.ifTrue(isSurface, riverBed)
                ),

                SurfaceRules.ifTrue(isSurface, organicPatches),

                SurfaceRules.ifTrue(isUnderWater,
                        SurfaceRules.ifTrue(isSubSurface,
                                SurfaceRules.state(Blocks.DIRT.defaultBlockState()))
                )
        );
    }
}