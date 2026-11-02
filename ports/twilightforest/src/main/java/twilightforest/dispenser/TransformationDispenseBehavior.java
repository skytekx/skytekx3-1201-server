package twilightforest.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import twilightforest.item.TransformPowderItem;

public class TransformationDispenseBehavior extends DefaultDispenseItemBehavior {

	boolean fired = false;

	@Override
	protected ItemStack execute(BlockSource source, ItemStack stack) {
		Level level = source.getLevel();
		BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
		if (!level.isClientSide()) {
			for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, new AABB(blockpos), EntitySelector.NO_SPECTATORS)) {
				if (TransformPowderItem.transformEntityIfPossible(level, livingentity, stack)) {
					this.fired = true;
				}
			}
		}
		return stack;
	}

	@Override
	protected void playSound(BlockSource source) {
		if (this.fired) {
			super.playSound(source);
		} else {
			source.getLevel().levelEvent(1001, source.getPos(), 0);
		}
	}
}
