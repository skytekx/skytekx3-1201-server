package baguchi.spiced_twilight.data;

import baguchi.spiced_twilight.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import java.util.function.Consumer;

public class CraftingGenerator extends RecipeProvider {
    public CraftingGenerator(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.CINDER_POUCH.get(), 1)
                .pattern("HFH")
                .pattern(" H ")
                .define('H', ModItems.HYDRA_HIDE.get())
                .define('F', ModItems.FIRE_BEETLE_POWDER.get())
                .unlockedBy("has_item", has(ModItems.HYDRA_HIDE.get()))
                .save(consumer);
    }
}
