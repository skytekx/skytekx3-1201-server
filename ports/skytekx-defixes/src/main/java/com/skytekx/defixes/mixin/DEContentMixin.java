package com.skytekx.defixes.mixin;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.draconicevolution.init.DEContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Fix 10 (upstream d6a3ceae, "Fixed Draconic pickaxe mining level"): {@code DEContent.DRACONIC_TIER} is
 * {@code new DETier(TechLevel.DRACONIUM)}, the second of the three tiers built in the static initializer.
 * Upstream builds it with {@code TechLevel.DRACONIC}. That gives the Draconic tools harvest level 128
 * instead of 3 (Wyvern has 32), and through DETier also the draconic harvest speed, attack damage,
 * swing speed and enchantability of the equipment config instead of the draconium values.
 */
@Mixin(value = DEContent.class, remap = false)
public abstract class DEContentMixin {
    @ModifyArg(method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/brandon3055/draconicevolution/items/equipment/DETier;<init>(Lcom/brandon3055/brandonscore/api/TechLevel;)V", ordinal = 1))
    private static TechLevel skytekx_defixes$draconicTier(TechLevel level) {
        return level == TechLevel.DRACONIUM ? TechLevel.DRACONIC : level;
    }
}
