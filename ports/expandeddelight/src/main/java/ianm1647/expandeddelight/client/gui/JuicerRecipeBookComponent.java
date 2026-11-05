package ianm1647.expandeddelight.client.gui;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.block.entity.container.JuicerMenu;
import ianm1647.expandeddelight.common.crafting.JuicerRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nonnull;
import java.util.List;

public class JuicerRecipeBookComponent extends RecipeBookComponent {
    // Minecraft 1.21.1 takes the filter button from four sprites, 1.20.1 from one sheet: the four upstream
    // sprites of textures/gui/sprites/recipe_book/ laid out as 1.20.1's StateSwitchingButton expects them
    protected static final ResourceLocation RECIPE_BOOK_BUTTONS = new ResourceLocation(ExpandedDelight.MODID, "textures/gui/recipe_book_buttons.png");

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(0, 0, 28, 18, RECIPE_BOOK_BUTTONS);
    }

    public void hide() {
        this.setVisible(false);
    }

    @Override
    @Nonnull
    protected Component getRecipeFilterName() {
        return Component.translatable("expandeddelight.container.recipe_book.juiceable");
    }

    @Override
    public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
        ItemStack resultStack = recipe.getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipe.setRecipe(recipe);
        if (slots.get(JuicerMenu.INDEX_DRINK).getItem().isEmpty()) {
            this.ghostRecipe.addIngredient(Ingredient.of(resultStack), slots.get(JuicerMenu.INDEX_DRINK).x, slots.get(JuicerMenu.INDEX_DRINK).y);
        }

        if (recipe instanceof JuicerRecipe juicerRecipe) {
            ItemStack containerStack = juicerRecipe.getOutputContainer();
            if (!containerStack.isEmpty()) {
                this.ghostRecipe.addIngredient(Ingredient.of(containerStack), slots.get(JuicerMenu.INDEX_CONTAINER).x, slots.get(JuicerMenu.INDEX_CONTAINER).y);
            }
        }

        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
    }
}
