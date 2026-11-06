package de.ellpeck.actuallyadditions.mixin;

import de.ellpeck.actuallyadditions.mod.items.ItemTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
