package com.blakebr0.mysticalautomation.compat.jei.transfer;

import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalautomation.compat.jei.JeiCompat;
import com.blakebr0.mysticalautomation.container.InfusionAltarnatorContainer;
import com.blakebr0.mysticalautomation.container.slot.FakeSlot;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.network.NetworkHandler;
import com.blakebr0.mysticalautomation.network.payload.SetFakeRecipeSlotPayload;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class InfusionAltarnatorRecipeTransferHandler implements IRecipeTransferHandler<InfusionAltarnatorContainer, IInfusionRecipe> {
    @Override
    public Class<? extends InfusionAltarnatorContainer> getContainerClass() {
        return InfusionAltarnatorContainer.class;
    }

    @Override
    public Optional<MenuType<InfusionAltarnatorContainer>> getMenuType() {
        return Optional.of(ModMenuTypes.INFUSION_ALTARNATOR.get());
    }

    @Override
    public RecipeType<IInfusionRecipe> getRecipeType() {
        return JeiCompat.INFUSION_ALTAR_RECIPE_TYPE;
    }

    @Override
    public IRecipeTransferError transferRecipe(InfusionAltarnatorContainer container, IInfusionRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
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
