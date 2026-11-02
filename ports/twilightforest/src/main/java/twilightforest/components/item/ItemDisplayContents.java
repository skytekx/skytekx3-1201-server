package twilightforest.components.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.custom.ItemDisplays;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;
import twilightforest.util.TFItemStackUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * What the Traveller's Goggles' Item Display holds: up to three maps, a compass, a clock and a Moon Dial, one per slot
 * of {@link #LAYOUT}, and which of the maps the HUD shows ({@code chosen_map_slot}, -1 for none). Backport of 4.8's
 * ItemDisplayContents, a data component there; here the value of the {@code twilightforest:item_display} stack NBT key
 * ({@link TravellersGearData#ITEM_DISPLAY}), with 4.8's form: {@code {"slots": [{"slot": <index>, "item": <stack>}],
 * "chosen_map_slot": <index>}}, the empty slots left out. The stacks are saved with ItemStack#save like the belt's
 * {@link TravellersContainerContents}.
 * <p>
 * Immutable: changes go through {@link Mutable}, which works on copies.
 */
public final class ItemDisplayContents {
	public static final List<RegistryObject<ItemDisplayType>> LAYOUT = List.of(ItemDisplays.MAP, ItemDisplays.MAP, ItemDisplays.MAP, ItemDisplays.COMPASS, ItemDisplays.CLOCK, ItemDisplays.MOON_DIAL);
	private static final int FIRST_MAP_SLOT_INDEX = LAYOUT.indexOf(ItemDisplays.MAP);
	private static final int MAX_SIZE = 256;
	public static final ItemDisplayContents EMPTY = new ItemDisplayContents(LAYOUT.size(), FIRST_MAP_SLOT_INDEX);
	public static final Codec<ItemDisplayContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		DisplaySlot.CODEC.listOf().fieldOf("slots").forGetter(ItemDisplayContents::asSlots),
		Codec.INT.fieldOf("chosen_map_slot").forGetter(ItemDisplayContents::findActiveMapSlot)
	).apply(instance, ItemDisplayContents::fromSlots));

	private final NonNullList<ItemStack> items;
	private final int chosenMapSlot;

	private ItemDisplayContents(int size, int chosenMapSlot) {
		this.items = NonNullList.withSize(size, ItemStack.EMPTY);
		this.chosenMapSlot = chosenMapSlot;
	}

	private ItemDisplayContents(List<ItemStack> items, int chosenMapSlot) {
		this.items = NonNullList.withSize(items.size(), ItemStack.EMPTY);
		for (int i = 0; i < items.size(); i++)
			this.items.set(i, items.get(i).copy());
		this.chosenMapSlot = chosenMapSlot;
	}

	private void copyInto(NonNullList<ItemStack> list) {
		for (int i = 0; i < list.size(); i++) {
			ItemStack itemstack = i < this.items.size() ? this.items.get(i) : ItemStack.EMPTY;
			list.set(i, itemstack.copy());
		}
	}

	private static ItemDisplayContents fromSlots(List<DisplaySlot> slots, int chosenMapSlot) {
		OptionalInt optionalint = slots.stream().mapToInt(DisplaySlot::index).max();
		if (optionalint.isEmpty()) {
			return EMPTY;
		} else {
			ItemDisplayContents contents = new ItemDisplayContents(optionalint.getAsInt() + 1, chosenMapSlot);

			for (DisplaySlot slot : slots) {
				contents.items.set(slot.index(), slot.item());
			}

			return contents;
		}
	}

	private List<DisplaySlot> asSlots() {
		List<DisplaySlot> list = new ArrayList<>();

		for (int i = 0; i < this.items.size(); i++) {
			ItemStack itemstack = this.items.get(i);
			if (!itemstack.isEmpty()) {
				list.add(new DisplaySlot(i, itemstack));
			}
		}

		return list;
	}

	public int findActiveMapSlot() {
		return this.chosenMapSlot;
	}

	/**
	 * The stack in {@code index}, empty past the last slot. Not a copy: do not change it.
	 */
	public ItemStack getItem(int index) {
		return index >= 0 && index < this.items.size() ? this.items.get(index) : ItemStack.EMPTY;
	}

	/**
	 * The slots, read-only; the stacks are not copies, so do not change them.
	 */
	public List<ItemStack> items() {
		return Collections.unmodifiableList(this.items);
	}

	/**
	 * Copies of the stacks held, for giving them back to a player.
	 */
	public Stream<ItemStack> nonEmptyStream() {
		return this.items.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy);
	}

	public int size() {
		return this.items.size();
	}

	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	@Override
	public String toString() {
		return "ItemDisplayContents" + this.items + "chosenMapSlot" + this.chosenMapSlot;
	}

	public static class Mutable {
		private final NonNullList<ItemStack> items;
		private int chosenMapSlot;

		public Mutable(ItemDisplayContents contents) {
			this.items = NonNullList.withSize(LAYOUT.size(), ItemStack.EMPTY);
			this.chosenMapSlot = contents.chosenMapSlot;
			contents.copyInto(this.items);
		}

		public int chosenMapSlot() {
			return this.chosenMapSlot;
		}

		private int findSwapSlot(ItemStack stack) {
			for (int i = 0; i < LAYOUT.size(); i++) {
				if (LAYOUT.get(i).get().validItems().test(stack)) {
					return i;
				}
			}
			return -1;
		}

		private int findInsertSlot(ItemStack stack) {
			for (int i = 0; i < LAYOUT.size(); i++) {
				if (LAYOUT.get(i).get().validItems().test(stack) && this.items.get(i).isEmpty()) {
					return i;
				}
			}
			return -1;
		}

		public boolean trySwap(SlotAccess source, Player player) {
			return this.trySwap(source, player, TFItemStackUtils::giveOrDrop);
		}

		/**
		 * Puts one item of the stack in {@code source} into the first free slot that takes it, or swaps it with the
		 * item in the first slot that takes it; the swapped out item goes to {@code source} and the rest of the stack
		 * to {@code remainder}.
		 */
		public boolean trySwap(SlotAccess source, Player player, BiConsumer<ItemStack, Player> remainder) {
			ItemStack slottedStack = source.get();
			// 1.20.1 asks the item, 4.8 the stack
			if (slottedStack.isEmpty() || !slottedStack.getItem().canFitInsideContainerItems()) {
				return false;
			}

			int slotForStack = this.findInsertSlot(slottedStack);
			if (slotForStack < 0) {
				slotForStack = this.findSwapSlot(slottedStack);
			}
			if (slotForStack < 0) {
				return false;
			}

			ItemStack targetStack = this.items.get(slotForStack);
			if (!targetStack.isEmpty() && ItemStack.isSameItemSameTags(slottedStack, targetStack)) {
				return false;
			}

			ItemStack insert = slottedStack.split(1);
			ItemStack replaced = this.items.set(slotForStack, insert);

			if (replaced.isEmpty()) {
				this.tryResetChosenMapSlot(slotForStack);
				return source.set(slottedStack);
			} else {
				boolean ret = source.set(replaced);
				remainder.accept(slottedStack, player);
				return ret;
			}
		}

		/**
		 * Takes the first stack out that {@code slot} accepts (any stack without a slot); the HUD moves on to the next
		 * map when the shown one is taken.
		 */
		@Nullable
		public ItemStack removeFirstFree(@Nullable Slot slot) {
			for (int i = 0; i < this.items.size(); i++) {
				ItemStack itemstack = this.items.get(i);
				if (!itemstack.isEmpty() && (slot == null || slot.mayPlace(itemstack))) {
					if (i == this.chosenMapSlot)
						this.cycleChosenMapSlot();
					return this.items.set(i, ItemStack.EMPTY);
				}
			}
			return null;
		}

		public ItemDisplayContents toImmutable() {
			return new ItemDisplayContents(this.items, this.chosenMapSlot);
		}

		/**
		 * Shows the next stored map, or none after the last one.
		 */
		public int cycleChosenMapSlot() {
			// local guard over 4.8: a negative chosen_map_slot in the NBT below -1 starts at the first slot
			for (int index = Math.max(this.chosenMapSlot + 1, 0); index < this.items.size(); index++) {
				if (LAYOUT.get(index) == ItemDisplays.MAP && !this.items.get(index).isEmpty()) {
					this.chosenMapSlot = index;
					return this.chosenMapSlot;
				}
			}
			this.chosenMapSlot = -1;
			return this.chosenMapSlot;
		}

		private void tryResetChosenMapSlot(int index) {
			if (index < LAYOUT.size() && LAYOUT.get(index) == ItemDisplays.MAP && !this.hasOtherMaps(index)) {
				this.chosenMapSlot = index;
			}
		}

		private boolean hasOtherMaps(int mapIndex) {
			for (int i = 0; i < Math.min(this.items.size(), LAYOUT.size()); i++) {
				if (!this.items.get(i).isEmpty() && LAYOUT.get(i) == ItemDisplays.MAP && mapIndex != i)
					return true;
			}
			return false;
		}
	}

	private record DisplaySlot(int index, ItemStack item) {
		private static final Codec<ItemStack> ITEM_CODEC = CompoundTag.CODEC.xmap(ItemStack::of, stack -> stack.save(new CompoundTag()));
		// 4.8 reads any int; a negative index would throw while decoding, so the range is checked like the belt's
		public static final Codec<DisplaySlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.intRange(0, MAX_SIZE - 1).fieldOf("slot").forGetter(DisplaySlot::index),
			ITEM_CODEC.fieldOf("item").forGetter(DisplaySlot::item)
		).apply(instance, DisplaySlot::new));
	}
}
