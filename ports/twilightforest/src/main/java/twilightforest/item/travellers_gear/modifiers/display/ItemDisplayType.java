package twilightforest.item.travellers_gear.modifiers.display;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A kind of slot of the Traveller's Goggles' Item Display: which items it takes and the outline its empty slot shows in
 * the goggles' tooltip. Registered in {@code twilightforest.init.custom.ItemDisplays}. Backport of 4.8's ItemDisplayType.
 * <p>
 * 4.8 also keeps the factory of the HUD widget here; that is a client class, so on 1.20.1 the widgets are mapped to
 * the types in the client's {@code twilightforest.client.overlay.ItemDisplayOverlay} instead, and a dedicated server
 * never sees them.
 */
public record ItemDisplayType(Predicate<ItemStack> validItems, Optional<ResourceLocation> slotTexture) {
}
