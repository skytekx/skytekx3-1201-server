package com.enderio.machines.common.integrations.jei.transfer;

import com.enderio.machines.common.blockentity.VatBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import com.enderio.machines.common.integrations.jei.category.VATCategory;
import com.enderio.machines.common.menu.VatMenu;
import com.enderio.machines.common.recipe.FermentingRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Moves the reagents of a Vat recipe into the Vat, the port of the 8.x VATTransferHelper. Like upstream it reports a
 * missing input fluid. The items move with JEI's own transfer, which counts every input slot of the recipe, so it only
 * gets to see the two reagent slots.
 */
public class VatRecipeTransferHandler implements IRecipeTransferHandler<VatMenu, FermentingRecipe> {

    private final IRecipeTransferHandlerHelper handlerHelper;
    private final IRecipeTransferHandler<VatMenu, FermentingRecipe> itemTransfer;

    public VatRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;

        IRecipeTransferInfo<VatMenu, FermentingRecipe> info = handlerHelper.createBasicRecipeTransferInfo(VatMenu.class, MachineMenus.VAT.get(),
            VATCategory.TYPE, VatMenu.INPUTS_INDEX, VatMenu.INPUT_COUNT, VatMenu.LAST_INDEX + 1, 36);
        this.itemTransfer = handlerHelper.createUnregisteredRecipeTransferHandler(info);
    }

    @Override
    public Class<? extends VatMenu> getContainerClass() {
        return VatMenu.class;
    }

    @Override
    public Optional<MenuType<VatMenu>> getMenuType() {
        return Optional.of(MachineMenus.VAT.get());
    }

    @Override
    public RecipeType<FermentingRecipe> getRecipeType() {
        return VATCategory.TYPE;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(VatMenu container, FermentingRecipe recipe, IRecipeSlotsView recipeSlots, Player player,
        boolean maxTransfer, boolean doTransfer) {

        VatBlockEntity vat = container.getBlockEntity();
        if (vat == null) {
            return handlerHelper.createInternalError();
        }

        List<IRecipeSlotView> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        List<IRecipeSlotView> itemInputs = inputs.stream()
            .filter(view -> !view.getSlotName().map(VATCategory.FLUID_INPUT_SLOT::equals).orElse(false))
            .toList();

        // The input tank must hold the recipe fluid, as upstream checks.
        if (!recipe.getInput().test(vat.getInputTank().getFluid())) {
            List<IRecipeSlotView> fluidSlots = inputs.stream().filter(view -> !itemInputs.contains(view)).toList();
            return handlerHelper.createUserErrorForMissingSlots(Component.translatable("jei.tooltip.error.recipe.transfer.missing"), fluidSlots);
        }

        return itemTransfer.transferRecipe(container, recipe, handlerHelper.createRecipeSlotsView(itemInputs), player, maxTransfer, doTransfer);
    }
}
