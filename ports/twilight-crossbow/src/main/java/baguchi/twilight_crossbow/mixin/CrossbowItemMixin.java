package baguchi.twilight_crossbow.mixin;

import baguchi.twilight_crossbow.item.TwilightCrossbowItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.20.1: {@code CrossbowItem#performShooting} is static, so the Twilight crossbows cannot override how they shoot.
 * Hand our crossbows to their instance pipeline ({@link TwilightCrossbowItem#shootLoadedProjectiles}); every other crossbow
 * keeps the vanilla code. Covers players ({@code CrossbowItem#use}) and mobs ({@code CrossbowAttackMob#performCrossbowAttack}).
 * <p>
 * (Upstream's CrossbowItemMixin changes getChargeDuration for the unreleased Knight Crossbow and is not ported.)
 */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {

    @Inject(method = "performShooting", at = @At("HEAD"), cancellable = true)
    private static void twilight_crossbow$performShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbowStack, float velocity, float inaccuracy, CallbackInfo ci) {
        if (crossbowStack.getItem() instanceof TwilightCrossbowItem crossbow) {
            crossbow.shootLoadedProjectiles(level, shooter, hand, crossbowStack, velocity, inaccuracy);
            ci.cancel();
        }
    }
}
