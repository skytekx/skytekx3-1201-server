package de.ellpeck.actuallyadditions.mixin.client;

import de.ellpeck.actuallyadditions.mod.event.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * SkyTekx: the outline of the Engineer's Infrared Goggles (upstream 3b297993, #1523, changelog
 * 1.3.26). Minecraft#shouldEntityAppearGlowing decides on the client whether the renderer draws the
 * glowing outline of an entity, so the mobs glow for the wearer alone and no entity data changes
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true)
    public void actuallyadditions$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ClientEvents.shouldGlow(entity))
            cir.setReturnValue(true);
    }
}
