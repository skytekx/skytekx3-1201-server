package baguchi.spiced_twilight.entity;

import baguchi.spiced_twilight.entity.projectile.MazeSlimeBallProjectile;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import twilightforest.entity.monster.SlimeBeetle;
import twilightforest.init.TFSounds;

public class MazeSlimeBeetle extends SlimeBeetle {
    public MazeSlimeBeetle(EntityType<? extends MazeSlimeBeetle> type, Level world) {
        super(type, world);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 3.0F, 1.25F, 2.0F));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0F, 40, 10.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0F).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.ATTACK_DAMAGE, 2.0F);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ThrowableProjectile projectile = new MazeSlimeBallProjectile(ModEntities.MAZE_SLIME_BALL.get(), this.level(), this);
        this.playSound(TFSounds.SLIME_BEETLE_SQUISH.get(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        double tx = target.getX() - this.getX();
        double ty = target.getY() + (double)target.getEyeHeight() - 1.1 - projectile.getY();
        double tz = target.getZ() - this.getZ();
        float heightOffset = Mth.sqrt((float)(tx * tx + tz * tz)) * 0.2F;
        projectile.shoot(tx, ty + (double)heightOffset, tz, 0.6F, 7.0F);
        this.level().addFreshEntity(projectile);
    }
}
