package ianm1647.expandeddelight.client.recipebook;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.crafting.JuicerRecipe;
import ianm1647.expandeddelight.common.crafting.JuicerRecipeBookTab;
import ianm1647.expandeddelight.common.registry.EDItems;
import ianm1647.expandeddelight.common.registry.EDRecipeTypes;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;

import java.util.function.Supplier;

/**
 * The juicer's recipe book categories. NeoForge 1.21.1 adds them through enumextensions.json with the icons
 * of upstream's EnumParameters, Forge 1.20.1 through IExtensibleEnum, as Farmer's Delight 1.20.1 does.
 */
public class RecipeCategories {

    public static final Supplier<RecipeBookCategories> JUICING_SEARCH = Suppliers.memoize(() -> RecipeBookCategories.create("EXPANDEDDELIGHT_JUICING_SEARCH", new ItemStack(Items.COMPASS)));
    public static final Supplier<RecipeBookCategories> JUICING_DRINKS = Suppliers.memoize(() -> RecipeBookCategories.create("EXPANDEDDELIGHT_JUICING_DRINKS", new ItemStack(EDItems.APPLE_JUICE.get())));
    public static final Supplier<RecipeBookCategories> JUICING_MISC = Suppliers.memoize(() -> RecipeBookCategories.create("EXPANDEDDELIGHT_JUICING_MISC", new ItemStack(Items.APPLE)));

    public RecipeCategories() {
    }

    public static void init(RegisterRecipeBookCategoriesEvent event) {
        event.registerBookCategories(ExpandedDelight.RECIPE_TYPE_JUICING, ImmutableList.of(JUICING_SEARCH.get(), JUICING_DRINKS.get(), JUICING_MISC.get()));
        event.registerAggregateCategory(JUICING_SEARCH.get(), ImmutableList.of(JUICING_DRINKS.get(), JUICING_MISC.get()));
        event.registerRecipeCategoryFinder(EDRecipeTypes.JUICING.get(), (recipe) -> {
            if (recipe instanceof JuicerRecipe juicerRecipe) {
                JuicerRecipeBookTab tab = juicerRecipe.getRecipeBookTab();
                if (tab != null) {
                    return switch (tab) {
                        case DRINKS -> JUICING_DRINKS.get();
                        case MISC -> JUICING_MISC.get();
                    };
                }
            }
            return JUICING_MISC.get();
        });
    }
}
