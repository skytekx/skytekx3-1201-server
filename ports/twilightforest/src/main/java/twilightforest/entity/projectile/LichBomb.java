package twilightforest.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import twilightforest.entity.boss.Lich;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFEntities;

public class LichBomb extends TFThrowable {

	public LichBomb(EntityType<? extends LichBomb> type, Level world) {
		super(type, world);
	}

	public LichBomb(Level world, LivingEntity thrower) {
		super(TFEntities.LICH_BOMB.get(), world, thrower);
	}

	@Override
	public void tick() {
		super.tick();
		this.makeTrail();
	}

	private void makeTrail() {
		for (int i = 0; i < 1; i++) {
			double sx = 0.5 * (this.random.nextDouble() - this.random.nextDouble()) + this.getDeltaMovement().x();
			double sy = 0.5 * (this.random.nextDouble() - this.random.nextDouble()) + this.getDeltaMovement().y();
			double sz = 0.5 * (this.random.nextDouble() - this.random.nextDouble()) + this.getDeltaMovement().z();

			// behind the bomb, drifting back (4.8 65e29df27)
			double dx = this.getX() - sx;
			double dy = this.getY() - sy;
			double dz = this.getZ() - sz;

			this.level().addParticle(ParticleTypes.FLAME, dx, dy, dz, sx * -0.25, sy * -0.25, sz * -0.25);
		}
	}

	@Override
	public boolean isOnFire() {
		return true;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public float getPickRadius() {
		return 1.0F;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		super.hurt(source, amount);

		if (source.getDirectEntity() != null) {
			if (!source.is(DamageTypeTags.IS_EXPLOSION))
				this.explode();
			return true;
		} else {
			return false;
		}
	}

	private void explode() {
		if (!this.level().isClientSide()) {
			this.level().explode(this, TFDamageTypes.getIndirectEntityDamageSource(this.level(), TFDamageTypes.LICH_BOMB, this, this.getOwner(), TFEntities.LICH.get()), null, this.getX(), this.getY(), this.getZ(), 2.0F, false, Level.ExplosionInteraction.NONE);
			this.discard();
		}
	}

	@Override
	protected float getGravity() {
		return 0.001F;
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		this.explode();
	}

	/**
	 * Bombs fly through every Lich, other Lich projectiles and wand bolts instead of colliding with them (4.8 2a37097ce,
	 * 1e04187ab).
	 */
	@Override
	protected boolean canHitEntity(Entity target) {
		return super.canHitEntity(target) && !(target instanceof LichBomb) && !(target instanceof Lich) && !(target instanceof LichBolt) && !(target instanceof TwilightWandBolt);
	}

	// Explosions, such as another bomb going off, do not push or chain-detonate bombs (4.8 48d6e5b09)
	@Override
	public boolean ignoreExplosion() {
		return true;
	}
}
