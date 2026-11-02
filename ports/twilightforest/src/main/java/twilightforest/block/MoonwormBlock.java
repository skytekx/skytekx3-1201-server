package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import twilightforest.block.entity.MoonwormBlockEntity;
import twilightforest.init.TFBlockEntities;

import org.jetbrains.annotations.Nullable;
import twilightforest.loot.TFLootTables;

public class MoonwormBlock extends CritterBlock {

	public MoonwormBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MoonwormBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TFBlockEntities.MOONWORM.get(), MoonwormBlockEntity::tick);
	}

	// Flowing fluids are let in so placeLiquid below can wash the moonworm away instead of the moonworm damming them.
	// Upstream accepts every fluid here, but Forge's BucketItem then empties a lava bucket (or a water bucket on an already
	// waterlogged moonworm) into placeLiquid for nothing, so source fluids keep the normal waterlogging rule.
	@Override
	public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
		return !fluid.defaultFluidState().isSource() || super.canPlaceLiquid(level, pos, state, fluid);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
		//allow waterlogging but allow for fluids to destroy moonworms if it flows into them
		if (fluidState.isSource()) {
			// CritterBlock's SimpleWaterloggedBlock behaviour: water sources waterlog the moonworm
			return super.placeLiquid(level, pos, state, fluidState);
		} else {
			if (!state.isAir() && fluidState.getType() instanceof FlowingFluid fluid) {
				//would have been lovely if FlowingFluid.beforeDestroyingBlock wasnt protected...
				if (fluid instanceof WaterFluid) {
					BlockEntity blockentity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
					Block.dropResources(state, level, pos, blockentity);
				} else if (fluid instanceof LavaFluid) {
					level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
				}
			}

			level.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL);
			return false;
		}
	}

	@Override
	public @Nullable ResourceLocation getSquishLootTable() {
		return TFLootTables.MOONWORM_SQUISH_DROPS;
	}
}
