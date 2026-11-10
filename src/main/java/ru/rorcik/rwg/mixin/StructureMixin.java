package ru.rorcik.rwg.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Vanilla only checks the biome when validating a structure start. A land
 * structure can therefore start on top of a lake/ocean if that column belongs
 * to a land biome. Reject such surface starts, while leaving explicitly
 * ocean-tagged structures (shipwrecks, ruins, monuments, ...) alone.
 */
@Mixin(Structure.class)
public abstract class StructureMixin {

    @Inject(method = "findValidGenerationPoint", at = @At("RETURN"), cancellable = true)
    private void rwg$rejectSurfaceStructureOnWater(
            Structure.GenerationContext context,
            CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir) {

        Structure self = (Structure) (Object) this;
        Optional<Structure.GenerationStub> result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;
        if (self.step() != GenerationStep.Decoration.SURFACE_STRUCTURES) return;
        if (rwg$isOceanStructure(self)) return;

        BlockPos start = result.get().position();
        int surface = context.chunkGenerator().getFirstOccupiedHeight(
                start.getX(), start.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        int floor = context.chunkGenerator().getFirstOccupiedHeight(
                start.getX(), start.getZ(),
                Heightmap.Types.OCEAN_FLOOR_WG,
                context.heightAccessor(), context.randomState());

        if (surface <= floor) return;

        NoiseColumn column = context.chunkGenerator().getBaseColumn(
                start.getX(), start.getZ(),
                context.heightAccessor(), context.randomState());

        for (int y = surface; y > floor; y--) {
            if (column.getBlock(y).getFluidState().is(Fluids.WATER)) {
                cir.setReturnValue(Optional.empty());
                return;
            }
        }
    }

    private static boolean rwg$isOceanStructure(Structure structure) {
        for (Holder<Biome> biome : structure.biomes()) {
            if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN)) {
                return true;
            }
        }
        return false;
    }
}
