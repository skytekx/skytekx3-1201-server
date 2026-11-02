package baguchi.twilight_crossbow.entity;

import baguchi.twilight_crossbow.init.ModDamageTypes;
import baguchi.twilight_crossbow.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public class IceFireworkRocketEntity extends FireworkRocketEntity {
    public IceFireworkRocketEntity(EntityType<? extends IceFireworkRocketEntity> entityType, Level level) {
        super(entityType, level);
    }

    // Vanilla FireworkRocketEntity(Level, double, double, double, ItemStack) with our entity type
    public IceFireworkRocketEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.ICE_FIREWORK.get(), level);
        this.life = 0;
        this.setPos(x, y, z);
        int i = 1;
        if (!stack.isEmpty()) {
            this.entityData.set(DATA_ID_FIREWORKS_ITEM, stack.copy());
            CompoundTag fireworks = stack.getTagElement("Fireworks");
            if (fireworks != null) {
                i += fireworks.getByte("Flight");
            }
        }

        this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
        this.lifetime = 10 * i + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public IceFireworkRocketEntity(Level level, @Nullable Entity shooter, double x, double y, double z, ItemStack stack) {
        this(level, x, y, z, stack);
        this.setOwner(shooter);
    }

    public IceFireworkRocketEntity(Level level, ItemStack stack, LivingEntity shooter) {
        this(level, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(shooter.getId()));
        this.attachedToEntity = shooter;
    }

    public IceFireworkRocketEntity(Level level, ItemStack stack, double x, double y, double z, boolean shotAtAngle) {
        this(level, x, y, z, stack);
        this.entityData.set(DATA_SHOT_AT_ANGLE, shotAtAngle);
    }

    public IceFireworkRocketEntity(Level level, ItemStack stack, Entity shooter, double x, double y, double z, boolean shotAtAngle) {
        this(level, stack, x, y, z, shotAtAngle);
        this.setOwner(shooter);
    }

    /**
     * 1.21 {@code FireworkRocketEntity#getExplosions}: the 1.20.1 rocket keeps them in its item's
     * {@code Fireworks.Explosions} NBT.
     */
    public ListTag getExplosions() {
        ItemStack stack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        CompoundTag fireworks = stack.isEmpty() ? null : stack.getTagElement("Fireworks");
        return fireworks != null ? fireworks.getList("Explosions", Tag.TAG_COMPOUND) : new ListTag();
    }

    /**
     * Replaces the vanilla (private) {@code FireworkRocketEntity#explode} for this entity, see
     * {@link baguchi.twilight_crossbow.mixin.FireworkRocketEntityMixin}. Upstream declares a private {@code explode()}
     * here, which on 1.21 never overrides the private vanilla one; the mixin makes the freezing blast actually happen.
     */
    public void explodeIce() {
        this.level().broadcastEntityEvent(this, (byte) 17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealIceExplosionDamage();
        if (!this.getExplosions().isEmpty()) {
            this.playSound(SoundEvents.PLAYER_HURT_FREEZE, 1F, 1F);
            this.playSound(SoundEvents.GLASS_BREAK, 1F, 1F);
        }
        this.discard();
    }

    private DamageSource iceFireworkDamage(@Nullable Entity direct, @Nullable Entity causing) {
        return new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageTypes.ICE_FIREWORK), direct, causing);
    }

    private void dealIceExplosionDamage() {
        float f = 0.0F;
        ListTag list = this.getExplosions();
        if (!list.isEmpty()) {
            f = 5.0F + (float) (list.size() * 2);
        }

        if (f > 0.0F) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurt(this.iceFireworkDamage(this.getOwner(), this.getOwner()), 5.0F + (float) (list.size() * 2));
                this.attachedToEntity.setTicksFrozen((int) (this.attachedToEntity.getTicksFrozen() + (5 + (float) (list.size() * 2)) * 20));

            }

            double d0 = 5.0;
            Vec3 vec3 = this.position();

            for (LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(d0))) {
                if (livingentity != this.attachedToEntity && !(this.distanceToSqr(livingentity) > 25.0)) {
                    boolean flag = false;

                    for (int i = 0; i < 2; i++) {
                        Vec3 vec31 = new Vec3(livingentity.getX(), livingentity.getY(0.5 * (double) i), livingentity.getZ());
                        HitResult hitresult = this.level().clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                        if (hitresult.getType() == HitResult.Type.MISS) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        float f1 = f * (float) Math.sqrt((5.0 - (double) this.distanceTo(livingentity)) / 5.0);
                        livingentity.hurt(this.iceFireworkDamage(this, this.getOwner()), f1);
                        livingentity.setTicksFrozen((int) (livingentity.getTicksFrozen() + (f1 * 20)));
                    }
                }
            }
        }
    }

    /**
     * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 17 && this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            createIceFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, this.getExplosions());
        } else {
            super.handleEntityEvent(id);
        }
    }

    public void createIceFireworks(
            double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ListTag explosions
    ) {
        if (explosions.isEmpty()) {
            for (int i = 0; i < this.random.nextInt(3) + 2; i++) {
                this.level().addParticle(
                        ParticleTypes.POOF, x, y, z, this.random.nextGaussian() * 0.05, 0.005, this.random.nextGaussian() * 0.05
                );
            }
        } else {
            for (int i = 0; i < this.random.nextInt(3) + 10; i++) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE, x, y, z, this.random.nextGaussian() * 0.5, this.random.nextGaussian() * 0.5, this.random.nextGaussian() * 0.5
                );
            }
        }
    }
}
