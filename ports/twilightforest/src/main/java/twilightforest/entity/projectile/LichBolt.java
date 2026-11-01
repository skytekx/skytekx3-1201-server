package twilightforest.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.entity.boss.Lich;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFEntities;

public class LichBolt extends TFThrowable {

	public LichBolt(EntityType<? extends LichBolt> type, Level level) {
		super(type, level);
	}

	public LichBolt(Level level, LivingEntity owner) {
		super(TFEntities.LICH_BOLT.get(), level, owner);
	}

	@Override
	public void tick() {
		super.tick();
		this.makeTrail();
	}

	private void makeTrail() {
		double s1 = ((this.random.nextFloat() * 0.5F) + 0.5F) * 0.17F;
		double s2 = ((this.random.nextFloat() * 0.5F) + 0.5F) * 0.80F;
		double s3 = ((this.random.nextFloat() * 0.5F) + 0.5F) * 0.69F;

		this.makeTrail(ParticleTypes.ENTITY_EFFECT, s1, s2, s3, 5);
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
	public boolean hurt(DamageSource damagesource, float amount) {
		super.hurt(damagesource, amount);

		if (!this.level().isClientSide() && damagesource.getEntity() != null) {
			Vec3 vec3d = damagesource.getEntity().getLookAngle();
			// reflect faster and more accurately
			this.shoot(vec3d.x(), vec3d.y(), vec3d.z(), 1.5F, 0.1F);  // reflect faster and more accurately

			if (damagesource.getDirectEntity() instanceof LivingEntity)
				this.setOwner(damagesource.getDirectEntity());

			return true;
		}

		return false;
	}

	@Override
	protected float getGravity() {
		return 0.001F;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			ItemStack itemId = new ItemStack(Items.ENDER_PEARL);
			for (int i = 0; i < 8; ++i) {
				this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemId), this.getX(), this.getY(), this.getZ(), random.nextGaussian() * 0.05D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.05D);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		this.level().broadcastEntityEvent(this, (byte) 3);
		this.discard();
	}

	/**
	 * Lich bolts fly through shadow clones, other Lich projectiles and wand bolts, and only hit a Lich once a player has
	 * reflected them, so a Lich no longer hits itself or its clones (4.8 9bc309831, 2a37097ce, 1e04187ab).
	 */
	@Override
	protected boolean canHitEntity(Entity target) {
		if (!super.canHitEntity(target)) return false;
		if (target instanceof Lich lich && (lich.isShadowClone() || !(this.getOwner() instanceof Player))) return false;
		return !(target instanceof LichBomb) && !(target instanceof LichBolt) && !(target instanceof TwilightWandBolt);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity hit = result.getEntity();

		if (!this.level().isClientSide()) {
			if (hit instanceof LivingEntity) {
				hit.hurt(TFDamageTypes.getIndirectEntityDamageSource(this.level(), TFDamageTypes.LICH_BOLT, this, this.getOwner(), TFEntities.LICH.get()), 6);
			}
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}

	// Explosions, such as the Lich's own bombs, do not push or detonate bolts (4.8 48d6e5b09)
	@Override
	public boolean ignoreExplosion() {
		return true;
	}
}
