package com.skytekx.defixes.mixin.client;

import com.brandon3055.draconicevolution.client.gui.GrinderGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Fix 9 (upstream 3ae9baa2, label half): the Mob Grinder's Claim XP button showed "Collect XP", the label of
 * the collect toggle, because its label supplier asks for {@code collect.xp}. Upstream asks for
 * {@code claim.xp} ("Claim XP", already in the lang file). In the 3.1.2.621 jar the Claim XP label is the
 * synthetic lambda {@code lambda$buildGui$14} (bootstrap 18, the supplier right before the
 * {@code claim.xp.info} tooltip); the collect toggle's identical lambda is {@code lambda$buildGui$13}.
 */
@Mixin(value = GrinderGui.class, remap = false)
public abstract class GrinderGuiMixin {
    @ModifyConstant(method = "lambda$buildGui$14", constant = @Constant(stringValue = "collect.xp"))
    private static String skytekx_defixes$claimXpLabel(String key) {
        return "claim.xp";
    }
}
