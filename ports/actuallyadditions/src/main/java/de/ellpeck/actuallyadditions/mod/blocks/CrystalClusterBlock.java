/*
 * This file ("BlockCrystalCluster.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

import de.ellpeck.actuallyadditions.mod.blocks.base.FullyDirectionalBlock;
import de.ellpeck.actuallyadditions.mod.items.metalists.Crystals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrystalClusterBlock extends FullyDirectionalBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CrystalClusterBlock(Crystals crystal) {
        super(Block.Properties.of()
            .instrument(NoteBlockInstrument.HAT)
            .lightLevel((e) -> 7)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .strength(0.25f, 1.0f));
    }

    // SkyTekx: upstream 745bb6c6, the cluster needs a sturdy face behind it. canSurvive and
    // updateShape are public on 1.20.1 (protected since 1.20.5)
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canAttach(level, pos, state.getValue(FACING).getOpposite());
    }

    private static boolean canAttach(LevelReader reader, BlockPos pos, Direction direction) {
        BlockPos blockpos = pos.relative(direction);
        return reader.getBlockState(blockpos).isFaceSturdy(reader, blockpos, direction.getOpposite());
    }

    // SkyTekx: upstream b8e1cd69, the cluster grows out of the clicked face
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState getBaseConstructorState() {
        return this.defaultBlockState().setValue(FACING, Direction.UP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case UP:
                return VoxelShapes.CrystalClusterShapes.SHAPE_U;
            case DOWN:
                return VoxelShapes.CrystalClusterShapes.SHAPE_D;
            case EAST:
                return VoxelShapes.CrystalClusterShapes.SHAPE_E;
            case SOUTH:
                return VoxelShapes.CrystalClusterShapes.SHAPE_S;
            case WEST:
                return VoxelShapes.CrystalClusterShapes.SHAPE_W;
            default:
                return VoxelShapes.CrystalClusterShapes.SHAPE_N;
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return state.getValue(FACING).getOpposite() == facing && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }
}
