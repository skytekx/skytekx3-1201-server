package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.inventory.DETileMenu;
import com.skytekx.defixes.chest.ChestResultGuard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draconium Chest crafting dupe, see {@link ChestResultGuard}. Every click in a menu reaches
 * {@code AbstractContainerMenu.clicked}, which {@code DETileMenu} overrides, and that holds for every kind
 * of click: pick up, shift click, number keys, drop and drag. {@code DraconiumChestMenu} does not override
 * it, so the hook sits here and acts only on the chest menu, and only on the server.
 */
@Mixin(DETileMenu.class)
public abstract class DETileMenuMixin {
    @Inject(method = "clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"))
    private void skytekx_defixes$refreshChestResult(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if ((Object) this instanceof ChestResultGuard guard && player instanceof ServerPlayer) {
            guard.skytekx_defixes$refreshStaleResult();
        }
    }
}
