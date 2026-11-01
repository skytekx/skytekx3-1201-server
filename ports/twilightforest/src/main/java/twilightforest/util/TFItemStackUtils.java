package twilightforest.util;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import twilightforest.TwilightForestMod;
import twilightforest.block.KeepsakeCasketBlock;
import twilightforest.events.CharmEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TFItemStackUtils {

	@Deprecated
	public static boolean consumeInventoryItem(LivingEntity living, final Predicate<ItemStack> matcher, final int count) {
		TwilightForestMod.LOGGER.warn("consumeInventoryItem accessed! Forge requires the player to be alive before we can access this cap. This cap is most likely being accessed for an Afterdeath Charm!");

		return living.getCapability(ForgeCapabilities.ITEM_HANDLER).map(inv -> {
			int innerCount = count;
			boolean consumedSome = false;

			for (int i = 0; i < inv.getSlots() && innerCount > 0; i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (matcher.test(stack)) {
					ItemStack consumed = inv.extractItem(i, innerCount, false);
					innerCount -= consumed.getCount();
					consumedSome = true;
				}
			}

			return consumedSome;
		}).orElse(false);
	}

	/**
	 * Consumes one of the given item from the player's armor, main inventory or offhand (in that order).
	 *
	 * @param persistentTag the player's persisted data. If the consumed stack is a damaged Keepsake Casket, its damage is stored here under {@link CharmEvents#CASKET_DAMAGE_TAG}.
	 * @param saveItemToTag if true, the consumed stack is saved under {@link CharmEvents#CONSUMED_CHARM_TAG} so its effect can be played later
	 */
	public static boolean consumeInventoryItem(final Player player, final Item item, CompoundTag persistentTag, boolean saveItemToTag) {
		return consumeInventoryItem(player.getInventory().armor, item, persistentTag, saveItemToTag)
				|| consumeInventoryItem(player.getInventory().items, item, persistentTag, saveItemToTag)
				|| consumeInventoryItem(player.getInventory().offhand, item, persistentTag, saveItemToTag);
	}

	public static boolean consumeInventoryItem(final NonNullList<ItemStack> stacks, final Item item, CompoundTag persistentTag, boolean saveItemToTag) {
		for (ItemStack stack : stacks) {
			if (stack.is(item)) {
				if (saveItemToTag) persistentTag.put(CharmEvents.CONSUMED_CHARM_TAG, stack.save(new CompoundTag()));
				CompoundTag nbt = stack.getTag();
				if (nbt != null && nbt.contains(BlockItem.BLOCK_STATE_TAG, Tag.TAG_COMPOUND)) {
					CompoundTag stateTag = nbt.getCompound(BlockItem.BLOCK_STATE_TAG);
					String breakage = KeepsakeCasketBlock.BREAKAGE.getName();
					if (stateTag.contains(breakage)) {
						//a casket broken by hand stores its damage as an int, the loot table's CopyBlockState stores it as a string
						int damage = stateTag.get(breakage) instanceof NumericTag numeric ? numeric.getAsInt() : KeepsakeCasketBlock.BREAKAGE.getValue(stateTag.getString(breakage)).orElse(0);
						persistentTag.putInt(CharmEvents.CASKET_DAMAGE_TAG, damage);
					}
				}
				stack.shrink(1);
				return true;
			}
		}

		return false;
	}

	/**
	 * @deprecated kept for addons built against 4.3. It no longer records the casket damage anywhere (4.3 used a static field shared by all players).
	 * Use {@link #consumeInventoryItem(Player, Item, CompoundTag, boolean)}.
	 */
	@Deprecated
	public static boolean consumeInventoryItem(final Player player, final Item item) {
		return consumeInventoryItem(player, item, new CompoundTag(), false);
	}

	/**
	 * @deprecated kept for addons built against 4.3. Use {@link #consumeInventoryItem(NonNullList, Item, CompoundTag, boolean)}.
	 */
	@Deprecated
	public static boolean consumeInventoryItem(final NonNullList<ItemStack> stacks, final Item item) {
		return consumeInventoryItem(stacks, item, new CompoundTag(), false);
	}

	public static NonNullList<ItemStack> sortArmorForCasket(Player player) {
		NonNullList<ItemStack> armor = player.getInventory().armor;
		Collections.reverse(armor);
		return armor;
	}

	public static NonNullList<ItemStack> sortInvForCasket(Player player) {
		NonNullList<ItemStack> inv = player.getInventory().items;
		NonNullList<ItemStack> sorted = NonNullList.create();
		//hotbar at the bottom
		sorted.addAll(inv.subList(9, 36));
		sorted.addAll(inv.subList(0, 9));

		return sorted;
	}

	public static NonNullList<ItemStack> splitToSize(ItemStack stack) {

		NonNullList<ItemStack> result = NonNullList.create();

		int size = stack.getMaxStackSize();

		while (!stack.isEmpty()) {
			result.add(stack.split(size));
		}

		return result;
	}

	public static boolean hasToolMaterial(ItemStack stack, Tier tier) {

		Item item = stack.getItem();

		// see TileEntityFurnace.getItemBurnTime
		if (item instanceof TieredItem tieredItem && tier.equals(tieredItem.getTier())) {
			return true;
		}
		if (item instanceof SwordItem sword && tier.equals(sword.getTier())) {
			return true;
		}
		return item instanceof HoeItem hoe && tier.equals(hoe.getTier());
	}

	public static void clearInfoTag(ItemStack stack, String key) {
		CompoundTag nbt = stack.getTag();
		if (nbt != null) {
			nbt.remove(key);
			if (nbt.isEmpty()) {
				stack.setTag(null);
			}
		}
	}

	//[VanillaCopy] of Inventory.load, but removed clearing all slots
	//also add a handler to move items to the next available slot if the slot they want to go to isnt available
	public static void loadNoClear(ListTag tag, Inventory inventory) {

		List<ItemStack> blockedItems = new ArrayList<>();

		for (int i = 0; i < tag.size(); ++i) {
			CompoundTag compoundtag = tag.getCompound(i);
			int j = compoundtag.getByte("Slot") & 255;
			ItemStack itemstack = ItemStack.of(compoundtag);
			if (!itemstack.isEmpty()) {
				if (j < inventory.items.size()) {
					if (inventory.items.get(j).isEmpty()) {
						inventory.items.set(j, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				} else if (j >= 100 && j < inventory.armor.size() + 100) {
					if (inventory.armor.get(j - 100).isEmpty()) {
						inventory.armor.set(j - 100, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				} else if (j >= 150 && j < inventory.offhand.size() + 150) {
					if (inventory.offhand.get(j - 150).isEmpty()) {
						inventory.offhand.set(j - 150, itemstack);
					} else {
						blockedItems.add(itemstack);
					}
				}
			}
		}

		if(!blockedItems.isEmpty()) blockedItems.forEach(inventory::add);
	}
}
