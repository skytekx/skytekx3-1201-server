package com.enderio.base.common.integrations.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * SkyTekx: optional Curios support, backported from Ender IO 8.2.5 (565ef4c3e, compat/curios/CuriosCompat). Curios is
 * an optional dependency, so every Curios class is used in {@link Curios} only, which loads after the mod check.
 */
public final class CuriosCompat {

    public static final String MOD_ID = "curios";

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * The stacks in the player's Curios slots that the filter accepts. The filter sees the empty stacks of free slots
     * too. The stacks are the live ones, so changes to them change the equipped items.
     *
     * @param filter the stacks to keep, or null for all
     * @return the stacks, empty when Curios is not installed
     */
    public static List<ItemStack> getActiveCurios(Player player, @Nullable Predicate<ItemStack> filter) {
        if (!isLoaded()) {
            return List.of();
        }

        return Curios.getActiveCurios(player, filter);
    }

    private static final class Curios {

        private static List<ItemStack> getActiveCurios(Player player, @Nullable Predicate<ItemStack> filter) {
            Optional<ICuriosItemHandler> inventory = CuriosApi.getCuriosInventory(player).resolve();
            if (inventory.isEmpty()) {
                return List.of();
            }

            List<ItemStack> result = new ArrayList<>();
            for (ICurioStacksHandler handler : inventory.get().getCurios().values()) {
                IDynamicStackHandler stacks = handler.getStacks();
                for (int slot = 0; slot < stacks.getSlots(); slot++) {
                    ItemStack stack = stacks.getStackInSlot(slot);
                    if (filter == null || filter.test(stack)) {
                        result.add(stack);
                    }
                }
            }

            return result;
        }
    }
}
