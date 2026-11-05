package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.handlers.LootEventHandler;
import net.minecraft.world.entity.animal.WaterAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fix 6 (upstream d297c265, "Fixed soul drop rate with water animals"): passive mobs drop souls with the
 * passive chance ({@code passiveSoulDropChance}), but the check was {@code entity instanceof Animal}, so
 * squid, fish, dolphins and other water animals used the far lower hostile chance. Upstream adds
 * {@code || entity instanceof WaterAnimal}.
 */
@Mixin(value = LootEventHandler.class, remap = false)
public abstract class LootEventHandlerMixin {
    @Redirect(method = "handleSoulDrops",
            at = @At(value = "CONSTANT", args = "classValue=net/minecraft/world/entity/animal/Animal"))
    private static boolean skytekx_defixes$waterAnimalsArePassive(Object entity, Class<?> animal) {
        return animal.isInstance(entity) || entity instanceof WaterAnimal;
    }
}
