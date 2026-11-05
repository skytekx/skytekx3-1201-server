package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.inventory.CraftingInventoryWrapper;
import com.brandon3055.draconicevolution.inventory.DraconiumChestMenu;
import com.skytekx.defixes.chest.ChestResultGuard;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/**
 * Draconium Chest crafting dupe, see {@link ChestResultGuard}. The menu keeps a copy of the grid from the
 * moment it last computed its result ({@code slotChangedCraftingGrid}) and computes the result again when
 * the grid differs from that copy. Nothing is saved: the result stays in the menu, as in stock Draconic
 * Evolution, so chests and their items keep their format.
 */
@Mixin(value = DraconiumChestMenu.class, remap = false)
public abstract class DraconiumChestMenuMixin implements ChestResultGuard {
    @Shadow
    private CraftingInventoryWrapper craftInventory;

    @Unique
    @Nullable
    private ItemStack[] skytekx_defixes$resultBasis;

    @Inject(method = "slotChangedCraftingGrid", at = @At("TAIL"))
    private void skytekx_defixes$rememberBasis(int containerId, Level level, Player player, CraftingContainer grid, ResultContainer result, CallbackInfo ci) {
        ItemStack[] basis = new ItemStack[grid.getContainerSize()];
        for (int i = 0; i < basis.length; i++) {
            basis[i] = grid.getItem(i).copy();
        }
        skytekx_defixes$resultBasis = basis;
    }

    @Override
    public void skytekx_defixes$refreshStaleResult() {
        if (skytekx_defixes$basisDiffers()) {
            ((AbstractContainerMenu) (Object) this).slotsChanged(craftInventory);
        }
    }

    @Unique
    private boolean skytekx_defixes$basisDiffers() {
        ItemStack[] basis = skytekx_defixes$resultBasis;
        if (basis == null || basis.length != craftInventory.getContainerSize()) {
            return true;
        }
        for (int i = 0; i < basis.length; i++) {
            if (!ItemStack.matches(basis[i], craftInventory.getItem(i))) {
                return true;
            }
        }
        return false;
    }
}
