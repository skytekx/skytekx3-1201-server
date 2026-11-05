package ianm1647.expandeddelight.common.block;

import ianm1647.expandeddelight.common.registry.EDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.CabinetBlock;

public class CinnamonCabinetBlock extends CabinetBlock {

    public CinnamonCabinetBlock() {
        super(Properties.copy(Blocks.BARREL));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return EDBlockEntityTypes.CINNAMON_CABINET.get().create(pos, state);
    }
}
