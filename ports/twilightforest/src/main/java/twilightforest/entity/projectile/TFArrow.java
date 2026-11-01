package twilightforest.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class TFArrow extends AbstractArrow implements ITFProjectile {

	/**
	 * The arrow the bow would have fired (plain, tipped, spectral or modded). Kept so its on-hit effects still apply. Not saved.
	 */
	@Nullable
	protected final AbstractArrow parentArrow;
	private ItemStack pickupItem = ItemStack.EMPTY;

	public TFArrow(EntityType<? extends TFArrow> type, Level worldIn) {
		super(type, worldIn);
		this.parentArrow = null;
	}

	public TFArrow(EntityType<? extends TFArrow> type, Level worldIn, @Nullable Entity shooter) {
		super(type, worldIn);
		this.setOwner(shooter);
		// The bows build these from AbstractArrow#getOwner(), which is null once the owner is unloaded or
		// when another mod calls BowItem#customArrow with an ownerless arrow. (#1958, upstream 02b6a0dfc)
		if (shooter != null) {
			this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
		}
		this.parentArrow = null;
	}

	/**
	 * Replaces the arrow a bow created (see BowItem#customArrow), taking over its shooter, pickup rule and ammo item.
	 */
	public TFArrow(EntityType<? extends TFArrow> type, AbstractArrow parentArrow) {
		super(type, parentArrow.level());
		Entity shooter = parentArrow.getOwner();
		this.setOwner(shooter);
		if (shooter != null) {
			this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
		} else {
			this.setPos(parentArrow.getX(), parentArrow.getY(), parentArrow.getZ());
		}
		this.pickup = parentArrow.pickup;
		this.pickupItem = parentArrow.getPickupItem().copyWithCount(1);
		this.parentArrow = parentArrow;
	}

	@Override
	public ItemStack getPickupItem() {
		return this.pickupItem.isEmpty() ? new ItemStack(Items.ARROW) : this.pickupItem.copy();
	}

	@Override
	public void doPostHurtEffects(LivingEntity target) {
		// add the effects of the original arrow, such as tipped arrow potions or spectral glowing (#2399)
		if (this.parentArrow != null) {
			this.parentArrow.doPostHurtEffects(target);
		}
		super.doPostHurtEffects(target);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (!this.pickupItem.isEmpty()) {
			tag.put("PickupItem", this.pickupItem.save(new CompoundTag()));
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.contains("PickupItem", Tag.TAG_COMPOUND)) {
			this.pickupItem = ItemStack.of(tag.getCompound("PickupItem"));
		}
	}
}
