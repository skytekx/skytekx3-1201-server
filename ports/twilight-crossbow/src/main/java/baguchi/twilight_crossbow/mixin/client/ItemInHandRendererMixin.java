package baguchi.twilight_crossbow.mixin.client;

import baguchi.twilight_crossbow.item.TwilightCrossbowItem;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * First-person: a loaded Twilight crossbow is held like a loaded vanilla crossbow (vanilla checks {@code is(Items.CROSSBOW)}).
 * Upstream lists triple, ice and seeker; the ender crossbow (added later) is included here too.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "isChargedCrossbow", at = @At("HEAD"), cancellable = true)
    private static void twilight_crossbow$isChargedCrossbow(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof TwilightCrossbowItem) {
            cir.setReturnValue(CrossbowItem.isCharged(stack));
        }
    }
}
