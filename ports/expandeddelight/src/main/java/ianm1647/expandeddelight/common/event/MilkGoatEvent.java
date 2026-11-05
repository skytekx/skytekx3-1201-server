package ianm1647.expandeddelight.common.event;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Milking a goat with a bucket or a glass bottle gives goat milk.
 * <p>
 * Upstream lets the interaction go on and stops the vanilla milking with a mixin into Goat#mobInteract
 * (RemoveMilkGoatMixin), which answers FAIL for every bucket. This port cancels the Forge event instead,
 * so that no mixin touches the goat, a class that Bukkit patches for its PlayerBucketFillEvent on the
 * Arclight server, and the vanilla milking cannot run after this one.
 */
@Mod.EventBusSubscriber(modid = ExpandedDelight.MODID)
public class MilkGoatEvent {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Goat goat) || goat.isBaby()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        Item item = null;
        if (stack.is(Items.BUCKET)) {
            item = EDItems.GOAT_MILK_BUCKET.get();
        } else if (stack.is(Items.GLASS_BOTTLE)) {
            item = EDItems.GOAT_MILK_BOTTLE.get();
        }
        if (item != null) {
            Player player = event.getEntity();
            player.playSound(SoundEvents.GOAT_MILK, 1.0f, 1.0f);
            player.swing(event.getHand());
            ItemStack nonNullStack = ItemUtils.createFilledResult(stack, player, item.getDefaultInstance());
            player.setItemInHand(event.getHand(), nonNullStack);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
    }

}
