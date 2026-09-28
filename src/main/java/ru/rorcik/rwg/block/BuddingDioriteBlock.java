package ru.rorcik.rwg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import ru.rorcik.rwg.RwgBlocks;

public class BuddingDioriteBlock extends Block {

    private static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    private static final float INITIAL_CRYSTAL_CHANCE = 0.3f;

    private static final float STAGE_BUD_1_CHANCE   = 0.55f;
    private static final float STAGE_BUD_2_CHANCE   = 0.25f;
    private static final float STAGE_BUD_3_CHANCE   = 0.13f;
    private static final float STAGE_CLUSTER_CHANCE = 0.07f;

    public BuddingDioriteBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) return;
        tryGrowCrystal(level, pos, random);
    }

    private void tryGrowCrystal(Level level, BlockPos pos, RandomSource random) {
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);

        Block nextBlock = null;

        if (canClusterGrowAtState(targetState)) {
            nextBlock = RwgBlocks.QUARTZ_BUD_1.get();
        } else if (isFacing(targetState, RwgBlocks.QUARTZ_BUD_1.get(), direction)) {
            nextBlock = RwgBlocks.QUARTZ_BUD_2.get();
        } else if (isFacing(targetState, RwgBlocks.QUARTZ_BUD_2.get(), direction)) {
            nextBlock = RwgBlocks.QUARTZ_BUD_3.get();
        } else if (isFacing(targetState, RwgBlocks.QUARTZ_BUD_3.get(), direction)) {
            nextBlock = RwgBlocks.QUARTZ_CLUSTER.get();
        }

        if (nextBlock == null) return;

        BlockState newState = nextBlock.defaultBlockState()
                .setValue(QuartzClusterBlock.FACING, direction)
                .setValue(QuartzClusterBlock.WATERLOGGED,
                        targetState.getFluidState().getType() == Fluids.WATER);
        level.setBlockAndUpdate(targetPos, newState);
    }

    public static boolean trySeedInitialCrystal(WorldGenLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() >= INITIAL_CRYSTAL_CHANCE) return false;

        Block chosen = pickInitialStage(random);

        int start = random.nextInt(DIRECTIONS.length);
        for (int i = 0; i < DIRECTIONS.length; i++) {
            Direction direction = DIRECTIONS[(start + i) % DIRECTIONS.length];
            BlockPos targetPos = pos.relative(direction);
            BlockState targetState = level.getBlockState(targetPos);

            if (!canClusterGrowAtState(targetState)) continue;

            BlockState newState = chosen.defaultBlockState()
                    .setValue(QuartzClusterBlock.FACING, direction)
                    .setValue(QuartzClusterBlock.WATERLOGGED,
                            targetState.getFluidState().getType() == Fluids.WATER);
            level.setBlock(targetPos, newState, 3);
            return true;
        }
        return false;
    }

    private static Block pickInitialStage(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < STAGE_BUD_1_CHANCE) return RwgBlocks.QUARTZ_BUD_1.get();
        roll -= STAGE_BUD_1_CHANCE;
        if (roll < STAGE_BUD_2_CHANCE) return RwgBlocks.QUARTZ_BUD_2.get();
        roll -= STAGE_BUD_2_CHANCE;
        if (roll < STAGE_BUD_3_CHANCE) return RwgBlocks.QUARTZ_BUD_3.get();
        roll -= STAGE_BUD_3_CHANCE;
        if (roll < STAGE_CLUSTER_CHANCE) return RwgBlocks.QUARTZ_CLUSTER.get();
        return RwgBlocks.QUARTZ_BUD_1.get();
    }

    private static boolean isFacing(BlockState state, Block block, Direction direction) {
        return state.is(block)
                && state.getValue(QuartzClusterBlock.FACING) == direction;
    }

    private static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir()
                || (state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8);
    }
}