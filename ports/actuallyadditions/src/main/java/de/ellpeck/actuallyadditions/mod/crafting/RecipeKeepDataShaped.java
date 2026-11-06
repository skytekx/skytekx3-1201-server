package de.ellpeck.actuallyadditions.mod.crafting;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class RecipeKeepDataShaped extends ShapedRecipe {
    public static String NAME = "copy_nbt";
    public static final Logger LOGGER = LogManager.getLogger();
    public RecipeKeepDataShaped(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn) {
        super(idIn, groupIn, CraftingBookCategory.MISC, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    public RecipeKeepDataShaped(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getId(), shapedRecipe.getGroup(), CraftingBookCategory.MISC, shapedRecipe.getRecipeWidth(), shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem(null));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        final ItemStack craftingResult = super.assemble(inv, registryAccess);
        TargetNBTIngredient donorIngredient = null;
        ItemStack datasource = ItemStack.EMPTY;
        NonNullList<Ingredient> ingredients = getIngredients();
        for (Ingredient ingredient : ingredients) {
            if (ingredient instanceof TargetNBTIngredient) {
                donorIngredient = (TargetNBTIngredient) ingredient;
                break;
            }
        }
        if (donorIngredient != null && !inv.isEmpty()) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (!item.isEmpty() && donorIngredient.test(item)) {
                    datasource = item;
                    break;
                }
            }
        }

        if (!datasource.isEmpty())
            copyData(datasource, craftingResult);

        return craftingResult;
    }

    /**
     * SkyTekx: copies the NBT of the donor onto the result, and the donor's energy with it. Upstream
     * 99ea93eb copies the stack attachments, where 1.20.4 keeps item energy. On 1.20.1 the energy
     * capability writes the NBT key Energy only when the stack is saved or copied, so the key of the
     * donor can lag behind its storage. The result gets the energy the capability reports
     */
    public static void copyData(ItemStack datasource, ItemStack result) {
        if (datasource.hasTag())
            result.setTag(datasource.getTag().copy());

        if (datasource.getItem() instanceof ItemEnergy donor && result.getItem() instanceof ItemEnergy) {
            int energy = donor.getEnergyStored(datasource);
            if (energy > 0) {
                result.getOrCreateTag().putInt("Energy", energy);
            }
        }
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return ActuallyRecipes.KEEP_DATA_SHAPED_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<RecipeKeepDataShaped> {
        @Nullable
        @Override
        public RecipeKeepDataShaped fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new RecipeKeepDataShaped(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
        }

        @Override
        public RecipeKeepDataShaped fromJson(ResourceLocation recipeId, JsonObject json) {
            try {
                return new RecipeKeepDataShaped(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
            }
            catch (Exception exception) {
                LOGGER.info("Error reading "+ NAME +" Recipe from packet: ", exception);
                throw exception;
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RecipeKeepDataShaped recipe) {
            try {
                RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
            }
            catch (Exception exception) {
                LOGGER.info("Error writing "+ NAME +" Recipe to packet: ", exception);
                throw exception;
            }
        }
    }
}
