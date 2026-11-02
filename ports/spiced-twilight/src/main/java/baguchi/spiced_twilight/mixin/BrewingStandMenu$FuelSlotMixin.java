package baguchi.spiced_twilight.mixin;

import baguchi.spiced_twilight.item.ModItems;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandMenu.FuelSlot.class)
public class BrewingStandMenu$FuelSlotMixin {

    @Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
    private static void spicedTwilight$mayPlaceItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack.is(ModItems.FIRE_BEETLE_POWDER.get())) {
            cir.setReturnValue(true);
        }
    }
}
