package com.blakebr0.mysticalautomation.compat.jei.transfer;

import com.blakebr0.mysticalautomation.container.CrafterContainer;
import com.blakebr0.mysticalautomation.container.slot.FakeSlot;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.network.NetworkHandler;
import com.blakebr0.mysticalautomation.network.payload.SetFakeRecipeSlotPayload;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.Optional;

public class CrafterRecipeTransferHandler implements IRecipeTransferHandler<CrafterContainer, CraftingRecipe> {
    @Override
    public Class<? extends CrafterContainer> getContainerClass() {
        return CrafterContainer.class;
    }

    @Override
    public Optional<MenuType<CrafterContainer>> getMenuType() {
        return Optional.of(ModMenuTypes.CRAFTER.get());
    }

    @Override
    public RecipeType<CraftingRecipe> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public IRecipeTransferError transferRecipe(CrafterContainer container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            var views = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
            var index = 0;

            for (var slot : container.slots) {
                // SkyTekx: upstream stops at index > views.size() and reads views.get(index) one step too far, which
                // throws for a recipe with fewer inputs than ghost slots (an Infusion Altar recipe with fewer than
                // eight pedestals). Ghost slots past the inputs of the recipe are cleared instead
                if (slot instanceof FakeSlot) {
                    var stack = index < views.size() ? views.get(index).getDisplayedItemStack().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;

                    NetworkHandler.INSTANCE.sendToServer(new SetFakeRecipeSlotPayload(slot.index, stack));

                    index++;
                }
            }
        }

        return null;
    }
}
