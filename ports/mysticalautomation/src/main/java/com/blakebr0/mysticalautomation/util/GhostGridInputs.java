package com.blakebr0.mysticalautomation.util;

import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * SkyTekx: takes the inputs of a machine with a ghost recipe grid (the Crafter and both Altarnators) from its real
 * input slots, and gives the stacks taken in the layout of the ghost grid, for the recipe to assemble its result from.
 * <p>
 * Upstream tests the real inputs only with {@code Ingredient#test}, which ignores NBT, and assembles the result from
 * the ghost grid. A player could set a plain ghost, feed the plain item, then put an enchanted or filled copy into the
 * ghost slot and keep it, and every recipe that copies NBT from an input (Cucumber's transfer recipes, the altar
 * recipes of Mystical Agriculture) put the NBT of the ghost onto new output. Here every filled ghost slot takes real
 * items that are equal to it (item and NBT, {@link StackHelper#areStacksEqual}), a real input only counts while it still
 * equals the ghost of its own slot, and the result is assembled from copies of the items taken.
 * <p>
 * Like upstream, the items for one ghost slot may come from any real input slot that holds them, so a single stack in
 * the first slot can feed the whole grid, as a hopper above fills it.
 */
public final class GhostGridInputs {
    private GhostGridInputs() { }

    /**
     * @param hasAll  every filled ghost slot found its items
     * @param amounts the count to take from each real input slot, in the order of {@code inputSlots}
     * @param taken   one item of each stack taken, in the ghost slot it was taken for
     */
    public record Result(boolean hasAll, int[] amounts, BaseItemStackHandler taken) { }

    /**
     * @param inventory  the machine inventory
     * @param inputSlots the real input slots, input slot {@code i} belongs to ghost slot {@code i}
     * @param ghosts     the ghost recipe grid
     * @param needs      how many items each ghost slot asks for, 0 for none
     */
    public static Result take(IItemHandler inventory, int[] inputSlots, IItemHandler ghosts, int[] needs) {
        var amounts = new int[inputSlots.length];
        var remaining = new int[inputSlots.length];
        var taken = BaseItemStackHandler.create(ghosts.getSlots());
        var required = 0;

        for (int i = 0; i < inputSlots.length; i++) {
            var stack = inventory.getStackInSlot(inputSlots[i]);

            // a real input whose ghost changed after it went in no longer counts
            if (i < ghosts.getSlots() && StackHelper.areStacksEqual(ghosts.getStackInSlot(i), stack)) {
                remaining[i] = stack.getCount();
            }
        }

        for (int p = 0; p < ghosts.getSlots(); p++) {
            var ghost = ghosts.getStackInSlot(p);
            var need = p < needs.length ? needs[p] : 0;

            if (ghost.isEmpty() || need <= 0)
                continue;

            required += need;

            for (int j = 0; j < inputSlots.length && need > 0; j++) {
                if (remaining[j] <= 0)
                    continue;

                var stack = inventory.getStackInSlot(inputSlots[j]);
                if (!StackHelper.areStacksEqual(ghost, stack))
                    continue;

                var amount = Math.min(need, remaining[j]);

                remaining[j] -= amount;
                amounts[j] += amount;
                need -= amount;

                if (taken.getStackInSlot(p).isEmpty()) {
                    taken.setStackInSlot(p, stack.copyWithCount(1));
                }
            }

            if (need > 0)
                return new Result(false, amounts, taken);
        }

        return new Result(required > 0, amounts, taken);
    }

    /**
     * One item for every filled ghost slot, the need of a crafting or Infusion Altar grid.
     */
    public static int[] onePerGhost(IItemHandler ghosts) {
        var needs = new int[ghosts.getSlots()];

        for (int p = 0; p < needs.length; p++) {
            needs[p] = ghosts.getStackInSlot(p).isEmpty() ? 0 : 1;
        }

        return needs;
    }
}
