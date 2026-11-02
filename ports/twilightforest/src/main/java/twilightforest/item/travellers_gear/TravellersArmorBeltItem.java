package twilightforest.item.travellers_gear;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersContainerContents;
import twilightforest.components.item.TravellersGearData;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.Optional;
import java.util.Set;

/**
 * The Traveller's Belt and the Traveller's Wings, the two leg pieces. Backport of 4.8's TravellersArmorBeltItem: the
 * belt's built-in Swap Hotbar (also an insertable modifier of the wings) keeps a second hotbar of nine stacks in the
 * {@code minecraft:container} component and swaps it with the player's hotbar when its key is pressed
 * (SwapHotbarPacket).
 */
public class TravellersArmorBeltItem extends TravellersArmorItem {
	/**
	 * The hotbar of a new belt: nine empty slots, saved as {@code []}.
	 */
	public static final TravellersContainerContents DEFAULT_EMPTY_BELT_CONTAINER = TravellersContainerContents.fromItems(NonNullList.withSize(9, ItemStack.EMPTY));

	public TravellersArmorBeltItem(Type type, Properties properties, int insertableModifierSlots, int durability, Set<Part> parts) {
		super(type, properties, insertableModifierSlots, durability, parts);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		// 4.8 skips it for hide_tooltip and hide_additional_tooltip; 1.20.1 has the "additional" HideFlags bit
		CompoundTag tag = stack.getTag();
		if (tag != null && (tag.getInt("HideFlags") & ItemStack.TooltipPart.ADDITIONAL.getMask()) != 0)
			return Optional.empty();
		return Optional.ofNullable(TravellersGearData.CONTAINER.get(stack)).map(Tooltip::new);
	}

	/**
	 * Belts and wings never go into shulker boxes or bundles, and Swap Hotbar never stores them. 4.8 lets wings without
	 * a stored hotbar in through the stack-aware NeoForge overload, which 1.20.1 does not have.
	 */
	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	/**
	 * Swaps the player's hotbar with the hotbar stored in their leg piece. A stack that cannot go into containers or is
	 * tagged {@code twilightforest:travellers_belt_blacklisted} stays where it is. While the piece is broken (or the
	 * player is a spectator) the stored stacks can still be taken out into empty hotbar slots, but nothing goes in
	 * (4.8 1f5533483).
	 */
	public static void travellersTrySwapHotbar(Player player) {
		ItemStack legArmor = player.getInventory().getArmor(EquipmentSlot.LEGS.getIndex());
		TravellersContainerContents containerContents = TravellersGearData.CONTAINER.get(legArmor);
		if (!hasSwapHotbar(player, legArmor) || containerContents == null)
			return;

		NonNullList<ItemStack> hotbarStacks = NonNullList.withSize(9, ItemStack.EMPTY);
		Inventory inventory = player.getInventory();
		boolean isSwapHotbarActive = isSwapHotbarActive(player, legArmor);
		boolean hasChanged = false;
		for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
			ItemStack inventoryStack = inventory.getItem(slotIndex);
			ItemStack beltStack = containerContents.getStackInSlot(slotIndex);
			if (inventoryStack.getItem().canFitInsideContainerItems() && !inventoryStack.is(ItemTagGenerator.TRAVELLERS_BELT_BLACKLISTED) && (isSwapHotbarActive || inventoryStack.isEmpty())) {
				hotbarStacks.set(slotIndex, inventoryStack);
				inventory.setItem(slotIndex, beltStack);
				// 4.8 compares the stacks by identity, so only two empty slots count as unchanged
				if (!beltStack.isEmpty() || !inventoryStack.isEmpty())
					hasChanged = true;
			} else {
				hotbarStacks.set(slotIndex, beltStack);  // keep the same item in belt inventory
			}
		}
		TravellersGearData.CONTAINER.set(legArmor, TravellersContainerContents.fromItems(hotbarStacks));
		if (hasChanged)
			player.level().playSound(null, player, TFSounds.SWAP_HOTBAR.get(), SoundSource.PLAYERS, 1F, 1F);
	}

	public static boolean isSwapHotbarActive(Player player, ItemStack stack) {
		return (TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.SWAP_HOTBAR_MODIFIER) || TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.SWAP_HOTBAR_ABILITY))
			&& TravellersGearData.CONTAINER.has(stack);
	}

	public static boolean hasSwapHotbar(Player player, ItemStack stack) {
		RegistryAccess registries = player.level().registryAccess();
		return (TravellersModifiersManager.hasTravellersModifier(registries, stack, TravellersModifiersManager.SWAP_HOTBAR_MODIFIER) || TravellersModifiersManager.hasTravellersModifier(registries, stack, TravellersModifiersManager.SWAP_HOTBAR_ABILITY))
			&& TravellersGearData.CONTAINER.has(stack);
	}

	/**
	 * The stored hotbar shown under the belt's tooltip (TravellersBeltTooltipComponent).
	 */
	public record Tooltip(TravellersContainerContents contents) implements TooltipComponent {
	}
}
