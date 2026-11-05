package com.enderio.base.data.recipe;

import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.tag.EIOTags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class FilterRecipeProvider extends RecipeProvider {

    public FilterRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.BASIC_ITEM_FILTER.get())
            .define('P', Ingredient.of(Items.PAPER, EIOItems.BLACK_PAPER)) // TODO: forge:paper?
            .define('H', Items.HOPPER)
            .pattern(" P ")
            .pattern("PHP")
            .pattern(" P ")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(Items.HOPPER))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.ADVANCED_ITEM_FILTER.get())
            .define('P', Ingredient.of(Items.PAPER, EIOItems.BLACK_PAPER)) // TODO: forge:paper?
            .define('Z', EIOItems.Z_LOGIC_CONTROLLER)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .pattern("RPR")
            .pattern("PZP")
            .pattern("RPR")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.Z_LOGIC_CONTROLLER))
            .save(consumer);

        // SkyTekx: the big filter recipes of Ender IO 8.x (a829f16dd), with the paper or black paper of the 6.x filters.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.BIG_ITEM_FILTER.get())
            .define('P', Ingredient.of(Items.PAPER, EIOItems.BLACK_PAPER))
            .define('S', EIOItems.SKELETAL_CONTRACTOR)
            .define('D', EIOTags.Items.DUSTS_OBSIDIAN)
            .pattern("DPD")
            .pattern("PSP")
            .pattern("DPD")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.SKELETAL_CONTRACTOR))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.BIG_ADVANCED_ITEM_FILTER.get())
            .define('S', Items.SHULKER_SHELL)
            .define('F', EIOItems.ADVANCED_ITEM_FILTER)
            .pattern("S")
            .pattern("F")
            .pattern("S")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.ADVANCED_ITEM_FILTER))
            .save(consumer);

        // SkyTekx: the limited item filter recipe of Ender IO 8.x (1a85694c2).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.LIMITED_ITEM_FILTER.get())
            .define('C', Items.COMPARATOR)
            .define('F', EIOItems.ADVANCED_ITEM_FILTER)
            .pattern("CFC")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.ADVANCED_ITEM_FILTER))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.BASIC_FLUID_FILTER.get())
            .define('P', Ingredient.of(Items.PAPER, EIOItems.BLACK_PAPER)) // TODO: forge:paper?
            .define('B', Items.BUCKET)
            .pattern(" P ")
            .pattern("PBP")
            .pattern(" P ")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BUCKET))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EIOItems.ENTITY_FILTER.get())
            .define('P', Ingredient.of(Items.PAPER, EIOItems.BLACK_PAPER)) // TODO: c:paper?
            .define('S', EIOItems.EMPTY_SOUL_VIAL)
            .pattern(" P ")
            .pattern("PSP")
            .pattern(" P ")
            .unlockedBy("has_ingredient", InventoryChangeTrigger.TriggerInstance.hasItems(EIOItems.EMPTY_SOUL_VIAL))
            .save(consumer);
    }
}
