package com.enderio.core.common.capability;

import com.enderio.api.filter.LimitingItemStackFilter;
import com.enderio.core.common.util.NbtUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The limited item filter of Ender IO 8.x (content/filters/item/limited/LimitedItemFilter) on the 6.x NBT filter
 * capability. The count of each entry is the stock level: as an insert filter it lets a stack in until the target
 * holds that many matching items, as an extract filter it leaves that many in the source. It has no deny list.
 * <p>
 * It uses the keys of {@link ItemFilterCapability}: ItemEntries (the entries keep their Count), IsNbt and DamageMode.
 */
public class LimitedItemFilterCapability implements IFilterCapability<ItemStack>, LimitingItemStackFilter {

    public static final int SIZE = 18;

    private static final String NBT_KEY = "IsNbt";
    private static final String ENTRIES_KEY = "ItemEntries";
    private static final String DAMAGE_MODE_KEY = "DamageMode";

    private final ItemStack container;

    public LimitedItemFilterCapability(ItemStack container) {
        this.container = container;

        CompoundTag tag = container.getOrCreateTag();
        if (!tag.contains(ENTRIES_KEY, CompoundTag.TAG_LIST)) {
            ListTag entriesList = new ListTag();
            ensureSize(entriesList);
            tag.put(ENTRIES_KEY, entriesList);
        }
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public List<ItemStack> getEntries() {
        CompoundTag tag = container.getOrCreateTag();

        List<ItemStack> entries = new ArrayList<>();
        if (tag.contains(ENTRIES_KEY, CompoundTag.TAG_LIST)) {
            ListTag entriesList = tag.getList(ENTRIES_KEY, CompoundTag.TAG_COMPOUND);
            ensureSize(entriesList);

            for (var entry : entriesList) {
                entries.add(ItemStack.of((CompoundTag) entry));
            }
        }

        return entries;
    }

    @Override
    public ItemStack getEntry(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IndexOutOfBoundsException(index);
        }

        CompoundTag tag = container.getOrCreateTag();

        if (!tag.contains(ENTRIES_KEY, CompoundTag.TAG_LIST)) {
            return ItemStack.EMPTY;
        }

        ListTag entriesList = tag.getList(ENTRIES_KEY, CompoundTag.TAG_COMPOUND);
        ensureSize(entriesList);
        return ItemStack.of((CompoundTag) entriesList.get(index));
    }

    @Override
    public void setEntry(int index, ItemStack stack) {
        if (index < 0 || index >= SIZE) {
            throw new IndexOutOfBoundsException(index);
        }

        CompoundTag tag = container.getOrCreateTag();

        ListTag entriesList;
        if (tag.contains(ENTRIES_KEY, CompoundTag.TAG_LIST)) {
            entriesList = tag.getList(ENTRIES_KEY, CompoundTag.TAG_COMPOUND);
        } else {
            entriesList = new ListTag();
            tag.put(ENTRIES_KEY, entriesList);
        }

        ensureSize(entriesList);
        // f0b62d322: the stock level of an entry is at most one stack of its item.
        ItemStack entry = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
        entriesList.set(index, entry.serializeNBT());
    }

    @Override
    public void setNbt(Boolean nbt) {
        CompoundTag tag = container.getOrCreateTag();
        tag.putBoolean(NBT_KEY, nbt);
    }

    @Override
    public boolean isNbt() {
        CompoundTag tag = container.getOrCreateTag();
        return tag.contains(NBT_KEY, CompoundTag.TAG_BYTE) && tag.getBoolean(NBT_KEY);
    }

    /**
     * The limited filter only allows the listed items, so it cannot be inverted.
     */
    @Override
    public void setInverted(Boolean inverted) {
    }

    @Override
    public boolean isInvert() {
        return false;
    }

    public DamageFilterMode getDamageMode() {
        CompoundTag tag = container.getTag();
        if (tag == null || !tag.contains(DAMAGE_MODE_KEY, CompoundTag.TAG_STRING)) {
            return DamageFilterMode.IGNORE;
        }

        return DamageFilterMode.byName(tag.getString(DAMAGE_MODE_KEY));
    }

    public void setDamageMode(DamageFilterMode mode) {
        CompoundTag tag = container.getOrCreateTag();
        if (mode == DamageFilterMode.IGNORE) {
            tag.remove(DAMAGE_MODE_KEY);
        } else {
            tag.putString(DAMAGE_MODE_KEY, mode.getSerializedName());
        }
    }

    /**
     * @return whether the stack matches an entry, without looking at any inventory
     */
    @Override
    public boolean test(ItemStack stack) {
        return findMatch(stack).isPresent();
    }

    /**
     * Port of LimitedItemFilter.test with an ItemFilterContext from 8.x (1a85694c2, 292bd84ed). The first entry that
     * matches the stack sets the limit, and the matching items in the handler are counted against it.
     */
    @Override
    public int getTransferLimit(ItemStack stack, IItemHandler handler, boolean insert) {
        var match = findMatch(stack);
        if (match.isEmpty()) {
            return 0;
        }

        ItemStack entry = match.get();
        boolean nbt = isNbt();
        int limit = entry.getCount();

        int currentCount = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (!inSlot.isEmpty() && matches(entry, inSlot, nbt)) {
                currentCount += inSlot.getCount();
            }
        }

        return insert ? limit - currentCount : currentCount - limit;
    }

    private Optional<ItemStack> findMatch(ItemStack stack) {
        if (stack.isEmpty() || !getDamageMode().test(stack)) {
            return Optional.empty();
        }

        boolean nbt = isNbt();
        for (ItemStack entry : getEntries()) {
            if (!entry.isEmpty() && matches(entry, stack, nbt)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    private static boolean matches(ItemStack entry, ItemStack stack, boolean nbt) {
        return nbt ? ItemStack.isSameItemSameTags(entry, stack) : ItemStack.isSameItem(entry, stack);
    }

    private void ensureSize(ListTag list) {
        NbtUtil.ensureSize(list, SIZE, () -> ItemStack.EMPTY.save(new CompoundTag()));
    }
}
