package ru.rorcik.rwg.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class QuartzClusterBlock extends Block implements SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final VoxelShape upAabb;
    private final VoxelShape downAabb;
    private final VoxelShape northAabb;
    private final VoxelShape southAabb;
    private final VoxelShape eastAabb;
    private final VoxelShape westAabb;

    public QuartzClusterBlock(int height, int offset, Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false));

        int min = offset;
        int max = 16 - offset;

        this.upAabb    = Block.box(min, 0.0, min, max, height, max);
        this.downAabb  = Block.box(min, 16 - height, min, max, 16.0, max);
        this.northAabb = Block.box(min, min, 16 - height, max, max, 16.0);
        this.southAabb = Block.box(min, min, 0.0, max, max, height);
        this.eastAabb  = Block.box(0.0, min, min, height, max, max);
        this.westAabb  = Block.box(16 - height, min, min, 16.0, max, max);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case UP    -> this.upAabb;
            case DOWN  -> this.downAabb;
            case NORTH -> this.northAabb;
            case SOUTH -> this.southAabb;
            case EAST  -> this.eastAabb;
            case WEST  -> this.westAabb;
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return this.getShape(state, level, pos, ctx);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, ctx.getClickedFace())
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.getValue(WATERLOGGED);
    }
}