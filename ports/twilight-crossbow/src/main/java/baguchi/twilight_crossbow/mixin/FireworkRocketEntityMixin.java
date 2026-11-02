package baguchi.twilight_crossbow.mixin;

import baguchi.twilight_crossbow.entity.IceFireworkRocketEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets the Ice Firework replace the private vanilla {@code explode()} (freezing damage, ice sounds) instead of only
 * shadowing it. Only cancels for {@link IceFireworkRocketEntity}; other rockets are untouched.
 */
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void twilight_crossbow$explodeIce(CallbackInfo ci) {
        if ((Object) this instanceof IceFireworkRocketEntity iceFirework) {
            iceFirework.explodeIce();
            ci.cancel();
        }
    }
}
