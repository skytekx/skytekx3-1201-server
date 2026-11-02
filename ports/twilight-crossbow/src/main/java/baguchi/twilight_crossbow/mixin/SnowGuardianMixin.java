package baguchi.twilight_crossbow.mixin;

import baguchi.twilight_crossbow.entity.ai.CrossbowAwareMeleeAttackGoal;
import baguchi.twilight_crossbow.init.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.monster.BaseIceMob;
import twilightforest.entity.monster.SnowGuardian;

/**
 * Snow Guardians can spawn with a (Twilight) crossbow and then shoot instead of meleeing.
 */
@Mixin(SnowGuardian.class)
public abstract class SnowGuardianMixin extends BaseIceMob implements CrossbowAttackMob {
    protected SnowGuardianMixin(EntityType<? extends BaseIceMob> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void twilight_crossbow$registerCrossbowGoals(CallbackInfo ci) {
        this.goalSelector.getAvailableGoals().stream().map(WrappedGoal::getGoal).filter(it -> it instanceof MeleeAttackGoal).findFirst().ifPresent(goal -> {
            this.goalSelector.removeGoal(goal);
        });
        this.goalSelector.addGoal(1, new RangedCrossbowAttackGoal<>(this, 1.0F, 8.0F));
        this.goalSelector.addGoal(1, new CrossbowAwareMeleeAttackGoal(this, 1.0F, false));
    }

    // SnowGuardian#makeItemForSlot is Twilight Forest's own private method: not a Minecraft name, so no remapping
    @Inject(method = "makeItemForSlot", at = @At("HEAD"), cancellable = true, remap = false)
    private void twilight_crossbow$makeCrossbowForSlot(EquipmentSlot slot, int type, CallbackInfoReturnable<Item> cir) {
        if (slot == EquipmentSlot.MAINHAND && this.random.nextInt(2) == 0) {
            Item item = switch (type) {
                case 1 -> ModItems.SEEKER_CROSSBOW.get();
                case 2 -> Items.CROSSBOW;
                case 3 -> ModItems.ICE_CROSSBOW.get();
                default -> ModItems.TRIPLE_CROSSBOW.get();
            };
            cir.setReturnValue(item);
        }
    }

    @Override
    public void setChargingCrossbow(boolean chargingCrossbow) {

    }

    // 1.20.1 CrossbowAttackMob has this extra abstract method (the 1.21 one is gone); same as Pillager
    @Override
    public void shootCrossbowProjectile(LivingEntity target, ItemStack crossbowStack, Projectile projectile, float projectileAngle) {
        this.shootCrossbowProjectile(this, target, projectile, projectileAngle, 1.6F);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        this.performCrossbowAttack(this, 1.6F);
    }
}
