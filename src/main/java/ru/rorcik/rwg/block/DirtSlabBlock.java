package ru.rorcik.rwg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import ru.rorcik.rwg.RwgBlocks;

public class DirtSlabBlock extends SlabBlock {

    public DirtSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (!ToolActions.SHOVEL_FLATTEN.equals(toolAction)) {
            return null;
        }

        SlabType slabType = state.getValue(TYPE);
        if (slabType == SlabType.DOUBLE) {
            return null;
        }

        ItemStack heldItem = context.getItemInHand();
        if (heldItem.isEmpty()) {
            return null;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Level level = context.getLevel();

        BlockState aboveState = level.getBlockState(clickedPos.above());
        if (aboveState.isAir() || clickedFace == Direction.UP) {
            if (!simulate) {
                level.playSound(context.getPlayer(), clickedPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return RwgBlocks.DIRT_PATH_SLAB.get().defaultBlockState().setValue(TYPE, slabType).setValue(WATERLOGGED, state.getValue(WATERLOGGED));
        }

        return null;
    }
}