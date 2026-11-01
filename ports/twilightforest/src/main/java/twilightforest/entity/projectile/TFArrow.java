package twilightforest.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class TFArrow extends AbstractArrow implements ITFProjectile {

	public TFArrow(EntityType<? extends TFArrow> type, Level worldIn) {
		super(type, worldIn);
	}

	public TFArrow(EntityType<? extends TFArrow> type, Level worldIn, @Nullable Entity shooter) {
		super(type, worldIn);
		this.setOwner(shooter);
		// The bows build these from AbstractArrow#getOwner(), which is null once the owner is unloaded or
		// when another mod calls BowItem#customArrow with an ownerless arrow. (#1958, upstream 02b6a0dfc)
		if (shooter != null) {
			this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
		}
	}

	@Override
	protected ItemStack getPickupItem() {
		return new ItemStack(Items.ARROW);
	}
}
