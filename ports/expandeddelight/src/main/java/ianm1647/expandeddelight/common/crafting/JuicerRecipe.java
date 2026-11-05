package ianm1647.expandeddelight.common.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.registry.EDItems;
import ianm1647.expandeddelight.common.registry.EDRecipeSerializers;
import ianm1647.expandeddelight.common.registry.EDRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Upstream's juicing recipe with a codec and a stream codec (Minecraft 1.21.1). On 1.20.1 the recipe keeps
 * its id and the serializer reads JSON and the network buffer by hand, in the shape of Farmer's Delight's
 * 1.20.1 CookingPotRecipe (MIT). The JSON keys are upstream's: group, recipe_book_tab, ingredients, result,
 * container, experience and juicingtime.
 */
public class JuicerRecipe implements Recipe<RecipeWrapper> {
   public static final int INPUT_SLOTS = 2;
   private final ResourceLocation id;
   private final String group;
   private final JuicerRecipeBookTab tab;
   private final NonNullList<Ingredient> inputItems;
   private final ItemStack output;
   private final ItemStack container;
   private final ItemStack containerOverride;
   private final float experience;
   private final int juiceTime;

   public JuicerRecipe(ResourceLocation id, String group, @Nullable JuicerRecipeBookTab tab, NonNullList<Ingredient> inputItems, ItemStack output, ItemStack container, float experience, int juiceTime) {
      this.id = id;
      this.group = group;
      this.tab = tab;
      this.inputItems = inputItems;
      this.output = output;
      if (!container.isEmpty()) {
         this.container = container;
      } else if (!output.getCraftingRemainingItem().isEmpty()) {
         this.container = output.getCraftingRemainingItem();
      } else {
         this.container = ItemStack.EMPTY;
      }
      this.containerOverride = container;
      this.experience = experience;
      this.juiceTime = juiceTime;
   }

   @Override
   public ResourceLocation getId() {
      return this.id;
   }

   @Override
   public String getGroup() {
      return this.group;
   }

   @Nullable
   public JuicerRecipeBookTab getRecipeBookTab() {
      return this.tab;
   }

   @Override
   public NonNullList<Ingredient> getIngredients() {
      return this.inputItems;
   }

   @Override
   public ItemStack getResultItem(RegistryAccess access) {
      return this.output;
   }

   public ItemStack getOutputContainer() {
      return this.container;
   }

   public ItemStack getContainerOverride() {
      return this.containerOverride;
   }

   @Override
   public ItemStack assemble(RecipeWrapper inv, RegistryAccess access) {
      return this.output.copy();
   }

   public float getExperience() {
      return this.experience;
   }

   public int getJuiceTime() {
      return this.juiceTime;
   }

   @Override
   public boolean matches(RecipeWrapper inv, Level level) {
      List<ItemStack> inputs = new ArrayList<>();
      int i = 0;

      for(int j = 0; j < INPUT_SLOTS; ++j) {
         ItemStack itemstack = inv.getItem(j);
         if (!itemstack.isEmpty()) {
            ++i;
            inputs.add(itemstack);
         }
      }

      return i == this.inputItems.size() && RecipeMatcher.findMatches(inputs, this.inputItems) != null;
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return width * height >= this.inputItems.size();
   }

   @Override
   public RecipeSerializer<?> getSerializer() {
      return EDRecipeSerializers.JUICING.get();
   }

   @Override
   public RecipeType<?> getType() {
      return EDRecipeTypes.JUICING.get();
   }

   @Override
   public ItemStack getToastSymbol() {
      return new ItemStack(EDItems.JUICER.get());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         JuicerRecipe that = (JuicerRecipe)o;
         if (Float.compare(that.getExperience(), this.getExperience()) != 0) {
            return false;
         } else if (this.getJuiceTime() != that.getJuiceTime()) {
            return false;
         } else if (!this.getId().equals(that.getId())) {
            return false;
         } else if (!this.getGroup().equals(that.getGroup())) {
            return false;
         } else if (this.tab != that.tab) {
            return false;
         } else if (!this.inputItems.equals(that.inputItems)) {
            return false;
         } else {
            return this.output.equals(that.output) && this.container.equals(that.container);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.getId().hashCode();
      result = 31 * result + this.getGroup().hashCode();
      result = 31 * result + (this.getRecipeBookTab() != null ? this.getRecipeBookTab().hashCode() : 0);
      result = 31 * result + this.inputItems.hashCode();
      result = 31 * result + this.output.hashCode();
      result = 31 * result + this.container.hashCode();
      result = 31 * result + (this.getExperience() != 0.0F ? Float.floatToIntBits(this.getExperience()) : 0);
      result = 31 * result + this.getJuiceTime();
      return result;
   }

   public static class Serializer implements RecipeSerializer<JuicerRecipe> {

      public Serializer() {
      }

      @Override
      public JuicerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
         final String group = GsonHelper.getAsString(json, "group", "");
         final NonNullList<Ingredient> inputItems = readIngredients(GsonHelper.getAsJsonArray(json, "ingredients"));
         if (inputItems.isEmpty()) {
            throw new JsonParseException("No ingredients for juicing recipe");
         } else if (inputItems.size() > INPUT_SLOTS) {
            throw new JsonParseException("Too many ingredients for juicing recipe! The max is " + INPUT_SLOTS);
         }
         // upstream's codec falls back to MISC for a missing tab and rejects an unknown one
         final String tabKey = GsonHelper.getAsString(json, "recipe_book_tab", null);
         JuicerRecipeBookTab tab = JuicerRecipeBookTab.MISC;
         if (tabKey != null) {
            tab = JuicerRecipeBookTab.findByName(tabKey);
            if (tab == null) {
               throw new JsonParseException("Optional field 'recipe_book_tab' does not match any valid tab. If defined, must be one of the following: " + EnumSet.allOf(JuicerRecipeBookTab.class));
            }
         }
         final ItemStack output = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
         final ItemStack container = GsonHelper.isValidNode(json, "container") ? CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "container"), true) : ItemStack.EMPTY;
         final float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
         final int juiceTime = GsonHelper.getAsInt(json, "juicingtime", 200);
         return new JuicerRecipe(recipeId, group, tab, inputItems, output, container, experience, juiceTime);
      }

      private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray) {
         NonNullList<Ingredient> list = NonNullList.create();
         for (int i = 0; i < ingredientArray.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(ingredientArray.get(i));
            if (!ingredient.isEmpty()) {
               list.add(ingredient);
            }
         }
         return list;
      }

      @Override
      @Nullable
      public JuicerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
         String groupIn = buffer.readUtf();
         JuicerRecipeBookTab tabIn = JuicerRecipeBookTab.findByName(buffer.readUtf());
         int i = buffer.readVarInt();
         if (i > INPUT_SLOTS) {
            ExpandedDelight.LOGGER.warn("Juicing recipe {} arrived with {} ingredients", recipeId, i);
         }
         NonNullList<Ingredient> inputItemsIn = NonNullList.withSize(i, Ingredient.EMPTY);
         for (int j = 0; j < inputItemsIn.size(); ++j) {
            inputItemsIn.set(j, Ingredient.fromNetwork(buffer));
         }
         ItemStack outputIn = buffer.readItem();
         ItemStack container = buffer.readItem();
         float experienceIn = buffer.readFloat();
         int juiceTimeIn = buffer.readVarInt();
         return new JuicerRecipe(recipeId, groupIn, tabIn, inputItemsIn, outputIn, container, experienceIn, juiceTimeIn);
      }

      @Override
      public void toNetwork(FriendlyByteBuf buffer, JuicerRecipe recipe) {
         buffer.writeUtf(recipe.group);
         buffer.writeUtf(recipe.tab != null ? recipe.tab.toString() : "");
         buffer.writeVarInt(recipe.inputItems.size());

         for (Ingredient ingredient : recipe.inputItems) {
            ingredient.toNetwork(buffer);
         }

         buffer.writeItem(recipe.output);
         buffer.writeItem(recipe.container);
         buffer.writeFloat(recipe.experience);
         buffer.writeVarInt(recipe.juiceTime);
      }
   }
}
