package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.handlers.ModularArmorEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Fix 2 (upstream 4683a54f): /kill against modular armor is cancelled and replaced by the
 * {@code administrative.kill} damage of Float.MAX_VALUE / 5, upstream MAX_VALUE / 100. Only that damage
 * changes; the check for the /kill amount (Float.MAX_VALUE) stays as it is.
 */
@Mixin(value = ModularArmorEventHandler.class, remap = false)
public abstract class ModularArmorEventHandlerMixin {
    @ModifyConstant(method = "onEntityAttacked", constant = @Constant(floatValue = Float.MAX_VALUE / 5F))
    private static float skytekx_defixes$killCommandDamage(float damage) {
        return Float.MAX_VALUE / 100F;
    }
}
