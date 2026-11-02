package twilightforest.item.travellers_gear;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.Optional;
import java.util.Set;

/**
 * The Traveller's Goggles. Backport of 4.8's TravellersGogglesItem: All-Night Goggles keep Endermen calm when looked at,
 * and goggles with Item Display store maps, a compass, a clock and a Moon Dial, put in and taken out with a right click
 * like a bundle, and keep the shown map updated while worn. The built-in Zoom ability is an item default (TFItems).
 */
public class TravellersGogglesItem extends TravellersArmorItem {

	public TravellersGogglesItem(Properties properties) {
		super(Type.HELMET, properties, 3, 12, Set.of());
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		// 4.8 skips it for hide_tooltip and hide_additional_tooltip; 1.20.1 has the "additional" HideFlags bit
		CompoundTag tag = stack.getTag();
		if (tag != null && (tag.getInt("HideFlags") & ItemStack.TooltipPart.ADDITIONAL.getMask()) != 0)
			return Optional.empty();
		return Optional.ofNullable(TravellersGearData.ITEM_DISPLAY.get(stack)).map(Tooltip::new);
	}

	/**
	 * Right-clicking a slot with the goggles: an empty slot gets the first stored item it accepts, a filled one has an
	 * item of its stack put in (or swapped with the stored one of its kind).
	 */
	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		if (stack.getCount() != 1 || action != ClickAction.SECONDARY)
			return false;

		ItemDisplayContents contents = TravellersGearData.ITEM_DISPLAY.get(stack);
		if (contents == null)
			return false;

		ItemDisplayContents.Mutable mutableContents = new ItemDisplayContents.Mutable(contents);
		ItemStack itemstack = slot.getItem();
		if (itemstack.isEmpty()) {
			ItemStack removedStack = mutableContents.removeFirstFree(slot);
			if (removedStack != null) {
				slot.safeInsert(removedStack);
				this.playRemoveOneSound(player);
			}
		} else if (itemstack.getItem().canFitInsideContainerItems()) {
			if (mutableContents.trySwap(slotAccess(slot), player))
				this.playInsertSound(player);
		}

		TravellersGearData.ITEM_DISPLAY.set(stack, mutableContents.toImmutable());
		return true;
	}

	/**
	 * Right-clicking the goggles with an item puts one of it in (or swaps it with the stored one of its kind); with an
	 * empty cursor the first stored item comes out. Works in the armor slot too (4.8 37c8d6763).
	 */
	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		if (stack.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player))
			return false;

		ItemDisplayContents contents = TravellersGearData.ITEM_DISPLAY.get(stack);
		if (contents == null)
			return false;

		ItemDisplayContents.Mutable mutableContents = new ItemDisplayContents.Mutable(contents);
		if (other.isEmpty()) {
			ItemStack itemstack = mutableContents.removeFirstFree(null);
			if (itemstack != null) {
				this.playRemoveOneSound(player);
				access.set(itemstack);
			}
		} else {
			if (mutableContents.trySwap(access, player))
				this.playInsertSound(player);
		}

		TravellersGearData.ITEM_DISPLAY.set(stack, mutableContents.toImmutable());
		return true;
	}

	/**
	 * Keeps the map the Item Display shows up to date, as if it were held: the map is filled in where the wearer walks
	 * and its update packets are sent from here, since the goggles are not a complex item. 4.8 does this in
	 * inventoryTick for the head slot (slot 39 on NeoForge); Forge 47 ticks worn armor with its armor list index
	 * there, so onArmorTick, which Forge only calls for worn armor, takes its place.
	 * {@code META-INF/asm/goggles_maps.js} keeps the wearer on the map (4.8 UpdateMapsInGogglesTransformer).
	 */
	@Override
	public void onArmorTick(ItemStack stack, Level level, Player player) {
		//only tick while on the player's head
		if (player.getItemBySlot(EquipmentSlot.HEAD) != stack)
			return;
		if (level.isClientSide() || !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER))
			return;

		ItemDisplayContents contents = TravellersGearData.ITEM_DISPLAY.get(stack);
		if (contents == null || contents.isEmpty())
			return;

		int mapSlot = contents.findActiveMapSlot();
		if (mapSlot == -1)
			return;

		ItemStack map = contents.getItem(mapSlot);
		if (map.isEmpty() || !(map.getItem() instanceof MapItem mapItem))
			return;

		//mark as selected so map properly updates
		mapItem.inventoryTick(map, level, player, Inventory.INVENTORY_SIZE + EquipmentSlot.HEAD.getIndex(), true);
		//send update packets here instead as the goggles aren't considered a complex item
		if (player instanceof ServerPlayer serverPlayer) {
			Packet<?> packet = mapItem.getUpdatePacket(map, level, serverPlayer);
			if (packet != null)
				serverPlayer.connection.send(packet);
		}
	}

	@Override
	public boolean isEnderMask(ItemStack stack, Player player, EnderMan enderman) {
		return TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ALL_NIGHT_GOGGLES_MODIFIER);
	}

	// 4.8 SlotAccess.of(slot::getItem, slot::set), which 1.20.1 does not have
	private static SlotAccess slotAccess(Slot slot) {
		return new SlotAccess() {
			@Override
			public ItemStack get() {
				return slot.getItem();
			}

			@Override
			public boolean set(ItemStack stack) {
				slot.set(stack);
				return true;
			}
		};
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	/**
	 * The stored items shown under the goggles' tooltip (ItemDisplayTooltipComponent).
	 */
	public record Tooltip(ItemDisplayContents contents) implements TooltipComponent {
	}
}
