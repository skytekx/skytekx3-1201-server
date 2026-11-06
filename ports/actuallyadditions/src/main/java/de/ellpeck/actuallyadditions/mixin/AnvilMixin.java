package de.ellpeck.actuallyadditions.mixin;

import de.ellpeck.actuallyadditions.mod.items.ItemTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// SkyTekx: the mixin of upstream 6f567725. CommonEvents.onAnvilEvent gives a renamed Item Tag the
// cost 0, and vanilla's mayPickup refuses a result of cost 0, so this lets the player take it.
// Arclight leaves mayPickup alone (its RepairContainerMixin works in createResult)
@Mixin(AnvilMenu.class)
public class AnvilMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void actuallyadditions$pickupItemTag(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        AnvilMenu anvilMenu = (AnvilMenu) (Object) this;
        if (((ItemCombinerMenuAccessor) anvilMenu).getInputSlots().getItem(0).getItem() instanceof ItemTag) {
            cir.setReturnValue(true);
        }
    }

    // SkyTekx: a local addition. When onAnvilEvent cancels the event for a name that names no item
    // tag, ForgeHooks.onAnvilChange returns false and createResult returns before it clears the
    // result slot, so the result of the last valid name stayed there and mayPickup above let the
    // player take it. Clearing the slot first leaves the anvil empty on that path. Arclight's
    // RepairContainerMixin only redirects the setItem calls in the body of createResult, so this
    // separate handler does not touch them
    @Inject(method = "createResult", at = @At("HEAD"))
    public void actuallyadditions$clearItemTagResult(CallbackInfo ci) {
        ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) (Object) this;
        if (accessor.getInputSlots().getItem(0).getItem() instanceof ItemTag) {
            accessor.getResultSlots().setItem(0, ItemStack.EMPTY);
        }
    }
}
