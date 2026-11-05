package ianm1647.expandeddelight.common.block.entity.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

public class JuicerItemHandler implements IItemHandler {
    private static final int SLOTS_INPUT = 2;
    private static final int SLOT_CONTAINER_INPUT = 3;
    private static final int SLOT_MEAL_OUTPUT = 4;
    private final IItemHandler itemHandler;
    private final Direction side;
    private final Predicate<ItemStack> isContainer;

    public JuicerItemHandler(IItemHandler itemHandler, @Nullable Direction side, Predicate<ItemStack> isContainer) {
        this.itemHandler = itemHandler;
        this.side = side;
        this.isContainer = isContainer;
    }

    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.itemHandler.isItemValid(slot, stack);
    }

    public int getSlots() {
        return this.itemHandler.getSlots();
    }

    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        return this.itemHandler.getStackInSlot(slot);
    }

    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.side != null && !this.side.equals(Direction.UP)) {
            // upstream lets a pipe put any item into the container slot, where it stays until a player takes it
            // out, so the sides take only what shift clicking in the menu would put there
            return slot == SLOT_CONTAINER_INPUT && this.isContainer.test(stack) ? this.itemHandler.insertItem(slot, stack, simulate) : stack;
        } else {
            return slot < 2 ? this.itemHandler.insertItem(slot, stack, simulate) : stack;
        }
    }

    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.side != null && !this.side.equals(Direction.UP)) {
            return slot == 4 ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        } else {
            return slot < 2 ? this.itemHandler.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
    }

    public int getSlotLimit(int slot) {
        return this.itemHandler.getSlotLimit(slot);
    }
}
