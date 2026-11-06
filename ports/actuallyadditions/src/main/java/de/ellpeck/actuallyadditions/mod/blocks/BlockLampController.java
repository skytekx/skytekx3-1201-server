/*
 * This file ("BlockLampPowerer.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

import de.ellpeck.actuallyadditions.mod.blocks.base.FullyDirectionalBlock;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class BlockLampController extends FullyDirectionalBlock {

    // SkyTekx: upper bound of lamps that one controller switches in a single update
    private static final int MAX_LAMPS = 4096;

    public BlockLampController() {
        super(ActuallyBlocks.defaultPickProps());
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        this.updateLamp(worldIn, pos);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        this.updateLamp(world, pos);
    }

    // SkyTekx: walk the connected lamps breadth first with a hash set and a cap. Upstream recursed
    // once per lamp and kept the visited positions in a list that never held the checked offsets,
    // so a large lamp field lagged the server and could overflow the stack in a neighbour update
    private void updateLamp(Level world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        BlockPos start = pos.relative(WorldUtil.getDirectionByPistonRotation(state));
        if (!world.isLoaded(start)) {
            return;
        }
        boolean powered = world.getBestNeighborSignal(pos) > 0;

        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(start);
        seen.add(start);
        int lamps = 0;
        while (!queue.isEmpty() && lamps < MAX_LAMPS) {
            BlockPos current = queue.poll();
            BlockState lamp = world.getBlockState(current);
            if (!(lamp.getBlock() instanceof BlockColoredLamp)) {
                continue;
            }
            lamps++;
            if (lamp.getValue(BlockStateProperties.LIT) != powered) {
                world.setBlockAndUpdate(current, lamp.setValue(BlockStateProperties.LIT, powered));
            }
            for (Direction side : Direction.values()) {
                BlockPos offset = current.relative(side);
                if (seen.add(offset) && world.isLoaded(offset)) {
                    queue.add(offset);
                }
            }
        }
    }
}
