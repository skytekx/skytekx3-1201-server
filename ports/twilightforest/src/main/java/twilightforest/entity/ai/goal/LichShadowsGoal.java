package twilightforest.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.projectile.LichBolt;
import twilightforest.entity.projectile.LichBomb;
import twilightforest.init.TFAttributes;
import twilightforest.init.TFItems;

import java.util.EnumSet;
import java.util.List;

/**
 * Phase 1: the Lich teleports to a new spot (and possibly a new target) every 100 ticks, summons shadow clones there and
 * fires bolts and bombs together with them. Clones copy their master's attack timing and teleport invisibility.
 */
public class LichShadowsGoal extends Goal {

	private final Lich lich;
	private final float attackRange;

	public LichShadowsGoal(Lich boss, float attackRange) {
		this.lich = boss;
		this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
		this.attackRange = attackRange;
	}

	@Override
	public boolean canUse() {
		return this.lich.getPhase() == 1 && this.lich.tickCount > 20 && this.lich.getTarget() != null;
	}

	@Override
	public void start() {
		this.lich.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TFItems.TWILIGHT_SCEPTER.get()));
	}

	@Override
	public void stop() {
		this.lich.despawnClones();
	}

	@Override
	public void tick() {
		if (!this.lich.isShadowClone()) {
			this.lich.getAllClones().forEach(clone -> {
				clone.setAttackCooldown(this.lich.getAttackCooldown());
				clone.setTeleportInvisibility(this.lich.getTeleportInvisibility());
			});
		}

		if (this.lich.getTeleportInvisibility() > 0) return;

		if (!this.lich.isShadowClone()) {
			LivingEntity targetedEntity = this.lich.getTarget();

			if (this.lich.getAttackCooldown() == 60) {
				if (!this.lich.teleportToNewTarget(targetedEntity, this.attackRange, this)) this.lich.teleportHome();
			} else if (targetedEntity != null && this.lich.getAttackCooldown() == 0) {
				if (this.lich.distanceTo(targetedEntity) < this.attackRange) {
					this.attack(this.lich);
					for (Lich clone : this.lich.getAllClones()) {
						clone.setTarget(this.lich.getTarget());
						this.attack(clone);
					}
				}
			}
		} else this.checkForMaster();
	}

	protected void attack(Lich lich) {
		if (lich.getNextAttackType() == 0) {
			lich.launchProjectileAt(new LichBolt(lich.level(), lich));
		} else {
			lich.launchProjectileAt(new LichBomb(lich.level(), lich));
		}

		lich.swing(InteractionHand.MAIN_HAND);

		if (lich.getRandom().nextInt(3) > 0) lich.setNextAttackType(0);
		else lich.setNextAttackType(1);
		lich.setAttackCooldown(100);
	}

	private void checkForMaster() {
		if (this.lich.getMaster() == null) {
			this.findNewMaster();
		}
		Lich master = this.lich.getMaster();
		if (!this.lich.level().isClientSide() && (master == null || !master.isAlive() || master.getPhase() != 1)) {
			this.lich.discard();
		}
	}

	public void checkAndSpawnClones(LivingEntity targetedEntity) {
		// if not, spawn one!
		if (this.lich.countMyClones() < this.lich.getAttributeValue(TFAttributes.CLONE_COUNT.get())) this.spawnShadowClone(targetedEntity);
	}

	private void spawnShadowClone(LivingEntity targetedEntity) {
		// find a good spot
		Vec3 cloneSpot = this.lich.findVecInLOSOf(targetedEntity);

		if (cloneSpot != null) {
			// put a clone there
			Lich newClone = new Lich(this.lich.level(), this.lich);
			newClone.setPos(cloneSpot.x(), cloneSpot.y(), cloneSpot.z());
			this.lich.level().addFreshEntity(newClone);

			newClone.setTarget(targetedEntity);
			newClone.setAttackCooldown(60 + this.lich.getRandom().nextInt(3) - this.lich.getRandom().nextInt(3));
			newClone.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TFItems.TWILIGHT_SCEPTER.get()));
			newClone.setTeleportInvisibility(this.lich.getTeleportInvisibility());
			this.lich.addClone(newClone.getUUID());
		}
	}

	/**
	 * Lets a clone without a master (one saved by 4.3) join a nearby Lich that wants another clone.
	 */
	private void findNewMaster() {
		for (Lich nearbyLich : this.getNearbyLiches()) {
			if (!nearbyLich.isShadowClone() && nearbyLich.wantsNewClone(this.lich)) {
				this.lich.setMasterUUID(nearbyLich.getUUID());
				nearbyLich.addClone(this.lich.getUUID());

				this.lich.setTarget(nearbyLich.getTarget());
				break;
			}
		}
	}

	private List<? extends Lich> getNearbyLiches() {
		return this.lich.level().getEntitiesOfClass(this.lich.getClass(), new AABB(this.lich.getX(), this.lich.getY(), this.lich.getZ(), this.lich.getX() + 1, this.lich.getY() + 1, this.lich.getZ() + 1).inflate(32.0D, 16.0D, 32.0D));
	}
}
