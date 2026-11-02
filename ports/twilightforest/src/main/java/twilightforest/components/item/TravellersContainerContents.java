package twilightforest.components.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The stacks held in a {@code minecraft:container} Traveller's Gear component: the hotbar a Traveller's Belt (or wings
 * with Swap Hotbar) stores. The 1.20.1 stand-in for 1.21's ItemContainerContents, with its form: a list of
 * {@code {"slot": <index>, "item": <stack>}} entries for the filled slots, so the empty container is {@code []}. The
 * stacks are saved with ItemStack#save, which keeps their NBT and Forge capability data.
 * <p>
 * Immutable: a copy goes in and a copy comes out, so a default value shared by every belt is never changed.
 */
public final class TravellersContainerContents {
	private static final int MAX_SIZE = 256;
	private static final Codec<ItemStack> ITEM_CODEC = CompoundTag.CODEC.xmap(ItemStack::of, stack -> stack.save(new CompoundTag()));
	private static final Codec<Slot> SLOT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.intRange(0, MAX_SIZE - 1).fieldOf("slot").forGetter(Slot::index),
		ITEM_CODEC.fieldOf("item").forGetter(Slot::item)
	).apply(instance, Slot::new));
	public static final Codec<TravellersContainerContents> CODEC = SLOT_CODEC.listOf().xmap(TravellersContainerContents::fromSlots, TravellersContainerContents::asSlots);

	public static final TravellersContainerContents EMPTY = new TravellersContainerContents(NonNullList.create());

	private final NonNullList<ItemStack> items;

	private TravellersContainerContents(NonNullList<ItemStack> items) {
		this.items = items;
	}

	/**
	 * Contents with one slot per entry of {@code items}, empty ones included.
	 */
	public static TravellersContainerContents fromItems(List<ItemStack> items) {
		int size = Math.min(items.size(), MAX_SIZE);
		NonNullList<ItemStack> copy = NonNullList.withSize(size, ItemStack.EMPTY);
		for (int i = 0; i < size; i++)
			copy.set(i, items.get(i).copy());
		return new TravellersContainerContents(copy);
	}

	private static TravellersContainerContents fromSlots(List<Slot> slots) {
		int size = slots.stream().mapToInt(Slot::index).max().orElse(-1) + 1;
		NonNullList<ItemStack> items = NonNullList.withSize(size, ItemStack.EMPTY);
		for (Slot slot : slots)
			items.set(slot.index(), slot.item());
		return new TravellersContainerContents(items);
	}

	private List<Slot> asSlots() {
		List<Slot> slots = new ArrayList<>();
		for (int i = 0; i < this.items.size(); i++) {
			ItemStack stack = this.items.get(i);
			if (!stack.isEmpty())
				slots.add(new Slot(i, stack));
		}
		return slots;
	}

	public int getSlots() {
		return this.items.size();
	}

	/**
	 * A copy of the stack in {@code slot}; empty past the last slot.
	 */
	public ItemStack getStackInSlot(int slot) {
		return slot >= 0 && slot < this.items.size() ? this.items.get(slot).copy() : ItemStack.EMPTY;
	}

	/**
	 * Copies of the filled slots' stacks.
	 */
	public Stream<ItemStack> nonEmptyStream() {
		return this.items.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy);
	}

	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public String toString() {
		return "TravellersContainerContents" + this.items;
	}

	private record Slot(int index, ItemStack item) {
	}
}
