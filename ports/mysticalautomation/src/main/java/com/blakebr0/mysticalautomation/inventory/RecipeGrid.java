package com.blakebr0.mysticalautomation.inventory;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * SkyTekx port: a {@link CraftingContainer} view of a ghost recipe inventory, the 1.20.1 form of upstream's
 * {@code BaseItemStackHandler#toCraftingInput(3, 3)} (Cucumber 8). Vanilla 1.20.1 crafting recipes match a
 * CraftingContainer, and the vanilla implementation needs a menu. The view reads and writes the stacks of the
 * handler, so recipe code must not change them, as with the 1.21 CraftingInput.
 */
public class RecipeGrid implements CraftingContainer {
    private final BaseItemStackHandler inventory;
    private final int width;
    private final int height;

    public RecipeGrid(BaseItemStackHandler inventory, int width, int height) {
        this.inventory = inventory;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public List<ItemStack> getItems() {
        return List.copyOf(this.inventory.getStacks().subList(0, this.getContainerSize()));
    }

    @Override
    public int getContainerSize() {
        return this.width * this.height;
    }

    @Override
    public boolean isEmpty() {
        for (var i = 0; i < this.getContainerSize(); i++) {
            if (!this.getItem(i).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.getContainerSize() ? this.inventory.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var stack = this.getItem(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var stack = this.getItem(slot);
        this.setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.setStackInSlot(slot, stack);
    }

    @Override
    public void setChanged() { }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (var i = 0; i < this.getContainerSize(); i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (var i = 0; i < this.getContainerSize(); i++) {
            contents.accountSimpleStack(this.getItem(i));
        }
    }
}
