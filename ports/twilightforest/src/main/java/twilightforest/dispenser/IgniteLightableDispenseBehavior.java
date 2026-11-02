package twilightforest.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.block.LightableBlock;

public class IgniteLightableDispenseBehavior extends OptionalDispenseItemBehavior {

	private final DispenseItemBehavior vanillaBehavior;

	public IgniteLightableDispenseBehavior(DispenseItemBehavior vanillaBehavior) {
		this.vanillaBehavior = vanillaBehavior;
	}

	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		ServerLevel level = source.getLevel();
		if (!level.isClientSide()) {
			BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
			this.setSuccess(tryLightBlock(level, blockpos));
			if (this.isSuccess() && stack.hurt(1, level.getRandom(), null)) {
				stack.setCount(0);
			}
		}

		return this.vanillaBehavior.dispense(source, stack);
	}

	private static boolean tryLightBlock(ServerLevel level, BlockPos pos) {
		BlockState blockstate = level.getBlockState(pos);
		// canBeLit, as 4.8's FIRESTARTER_LIGHT tool action asks: a candelabra without candles or under water stays dark
		if (blockstate.getBlock() instanceof LightableBlock lightable && lightable.canBeLit(blockstate)) {
			level.setBlockAndUpdate(pos, blockstate.setValue(LightableBlock.LIGHTING, LightableBlock.Lighting.NORMAL));
			return true;
		}

		return false;
	}
}
