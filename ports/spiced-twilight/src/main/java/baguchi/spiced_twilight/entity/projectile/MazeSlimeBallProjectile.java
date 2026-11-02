package baguchi.spiced_twilight.entity.projectile;

import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.entity.projectile.TFThrowable;
import twilightforest.init.TFItems;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class MazeSlimeBallProjectile extends TFThrowable implements ItemSupplier {
    public MazeSlimeBallProjectile(EntityType<? extends MazeSlimeBallProjectile> type, Level world) {
        super(type, world);
    }

    public MazeSlimeBallProjectile(EntityType<? extends MazeSlimeBallProjectile> type, Level world, LivingEntity thrower) {
        super(type, world, thrower);
    }

    @Override
    public void tick() {
        super.tick();
        this.makeTrail(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(TFItems.MAZE_SLIME_BALL.get())), 2);
    }

    // 1.21 sets this with EntityType.Builder#eyeHeight
    @Override
    protected float getEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 0.325F;
    }

    @Override
    protected float getGravity() {
        return 0.006F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        super.hurt(source, amount);
        this.die();
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, this.random.nextDouble() * 0.2, this.random.nextGaussian() * 0.05);
            }
        } else {
            super.handleEntityEvent(id);
        }

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (!this.level().isClientSide() && target instanceof LivingEntity) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
        }
        this.die();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Vec3 projectileMovement = this.getDeltaMovement();
        if (projectileMovement.length() > 0.15) {
            Vec3i direction = result.getDirection().getNormal();
            switch (result.getDirection()) {
                case UP, SOUTH, EAST:
                    direction = direction.multiply(-1);
                default:
            }
            direction = new Vec3i(direction.getX() == 0 ? 1 : direction.getX(), direction.getY() == 0 ? 1 : direction.getY(), direction.getZ() == 0 ? 1 : direction.getZ());
            this.setDeltaMovement(projectileMovement.multiply(new Vec3(direction.getX(), direction.getY(), direction.getZ())).multiply(0.75, 0.65, 0.75));
            this.playSound(SoundEvents.SLIME_SQUISH, 0.4F, 1.0F);
        } else {
            this.die();
        }
    }

    private void die() {
        if (!this.level().isClientSide()) {
            this.playSound(SoundEvents.SLIME_SQUISH, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
            // backport: broadcast before discard, a removed entity is no longer tracked and the splat would never arrive
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }

    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(TFItems.MAZE_SLIME_BALL.get());
    }
}
