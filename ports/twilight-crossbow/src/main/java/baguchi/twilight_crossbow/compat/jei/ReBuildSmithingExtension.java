package baguchi.twilight_crossbow.compat.jei;

import baguchi.twilight_crossbow.recipe.RebuildSmithingRecipe;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;

/**
 * Shows the rebuild recipes in JEI's smithing category. Upstream extends JEI's internal SmithingCategoryExtension and
 * reassembles the output on focus changes; the result here never depends on the inputs, so the public API is enough.
 */
public class ReBuildSmithingExtension implements ISmithingCategoryExtension<RebuildSmithingRecipe> {

    @Override
    public <T extends IIngredientAcceptor<T>> void setTemplate(RebuildSmithingRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(recipe.getTemplate());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setBase(RebuildSmithingRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(recipe.getBase());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setAddition(RebuildSmithingRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addIngredients(recipe.getAddition());
    }

    @Override
    public <T extends IIngredientAcceptor<T>> void setOutput(RebuildSmithingRecipe recipe, T ingredientAcceptor) {
        ingredientAcceptor.addItemStack(recipe.getResult());
    }
}
