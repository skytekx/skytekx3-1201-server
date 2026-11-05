package com.skytekx.defixes.mixin.client;

import com.brandon3055.brandonscore.client.hud.HudManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Brandon's Core 6d6c5a5 ("Properly hide hud elements in F1 mode"): the HUD elements of Brandon's Core
 * (the Draconic Evolution shield, energy and item HUDs) stayed on screen with the GUI hidden (F1).
 * Upstream returns early from the overlay and client tick handlers while {@code options.hideGui} is set.
 */
@Mixin(value = HudManager.class, remap = false)
public abstract class HudManagerMixin {
    @Inject(method = {"onDrawOverlayPre", "onDrawOverlayPost", "onClientTick"}, at = @At("HEAD"), cancellable = true, require = 3)
    private static void skytekx_defixes$hideInF1(CallbackInfo ci) {
        if (Minecraft.getInstance().options.hideGui) {
            ci.cancel();
        }
    }
}
