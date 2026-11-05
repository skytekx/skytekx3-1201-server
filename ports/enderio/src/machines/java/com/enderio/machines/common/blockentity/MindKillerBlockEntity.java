package com.enderio.machines.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * SkyTekx: the block entity of the Mind Killer (84bfe2477). It holds no data and does not tick, it only lets the client
 * render the zombie head above the block.
 */
public class MindKillerBlockEntity extends BlockEntity {

    public MindKillerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
