package com.blakebr0.mysticalautomation.crafting.recipe;

import com.blakebr0.mysticalautomation.api.crafting.IFarmerRecipe;
import com.blakebr0.mysticalautomation.init.ModRecipeSerializers;
import com.blakebr0.mysticalautomation.init.ModRecipeTypes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class FarmerRecipe implements IFarmerRecipe {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final int stages;
    private final List<FarmerResult> results;

    private final Ingredient seeds;
    private final Ingredient soil;
    private final Ingredient crux;

    private final int inputCount;

    public FarmerRecipe(ResourceLocation id, Ingredient seeds, Ingredient soil, Ingredient crux, int stages, List<FarmerResult> results) {
        this.id = id;
        this.seeds = seeds;
        this.soil = soil;
        this.crux = crux;
        this.ingredients = NonNullList.of(Ingredient.EMPTY, seeds, soil, crux);
        this.stages = stages;
        this.results = results;

        this.inputCount = (int) this.ingredients.stream().filter(i -> !i.isEmpty()).count();
    }

    // SkyTekx port: a 1.21 CraftingInput drops the empty rows at its end, so upstream compares its size with the
    // number of ingredients. The 1.20.1 container keeps all three slots, so this counts the filled ones
    @Override
    public boolean matches(Container input, Level level) {
        var filled = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            if (!input.getItem(i).isEmpty())
                filled++;
        }

        if (filled != this.inputCount)
            return false;

        for (int i = 0; i < Math.min(input.getContainerSize(), this.ingredients.size()); i++) {
            var ingredient = this.ingredients.get(i);
            var stack = input.getItem(i);

            if (!ingredient.test(stack))
                return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container input, RegistryAccess access) {
        return this.results.get(0).stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width <= 3 && height == 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return this.results.get(0).stack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.FARMER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.FARMER.get();
    }

    // SkyTekx port: keeps the recipe out of the vanilla recipe book, which knows no Farmer category
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public int getStages() {
        return this.stages;
    }

    @Override
    public List<FarmerResult> getResults() {
        return this.results;
    }

    @Override
    public List<ItemStack> getRolledResults() {
        var results = new ArrayList<ItemStack>();

        for (var result : this.results) {
            if (result.chance() > Math.random())
                results.add(result.stack().copy());
        }

        return results;
    }

    /**
     * SkyTekx port: the 1.20.1 serializer for upstream's MapCodec and StreamCodec. The JSON form is upstream's, except
     * that a result stack is an item object of 1.20.1 ({@code "item"} and {@code "count"}). The 1.21 key {@code "id"}
     * for the item works as well.
     */
    public static class Serializer implements RecipeSerializer<FarmerRecipe> {
        @Override
        public FarmerRecipe fromJson(ResourceLocation id, JsonObject json) {
            var seeds = Ingredient.fromJson(GsonHelper.getNonNull(json, "seeds"));
            var soil = Ingredient.fromJson(GsonHelper.getNonNull(json, "soil"));
            var crux = json.has("crux") ? Ingredient.fromJson(json.get("crux")) : Ingredient.EMPTY;
            var stages = GsonHelper.getAsInt(json, "stages");

            var results = new ArrayList<FarmerResult>();
            for (var element : GsonHelper.getAsJsonArray(json, "results")) {
                var result = GsonHelper.convertToJsonObject(element, "result");
                var item = GsonHelper.getAsJsonObject(result, "item").deepCopy();

                if (!item.has("item") && item.has("id")) {
                    item.add("item", item.get("id"));
                    item.remove("id");
                }

                var stack = ShapedRecipe.itemStackFromJson(item);
                var chance = GsonHelper.getAsFloat(result, "chance");

                results.add(new FarmerResult(stack, chance));
            }

            if (results.isEmpty())
                throw new JsonParseException("No results for farmer recipe");

            return new FarmerRecipe(id, seeds, soil, crux, stages, results);
        }

        @Override
        public FarmerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var seeds = Ingredient.fromNetwork(buffer);
            var soil = Ingredient.fromNetwork(buffer);
            var crux = Ingredient.fromNetwork(buffer);

            var stages = buffer.readVarInt();

            var size = buffer.readVarInt();
            var results = new ArrayList<FarmerResult>();

            for (int i = 0; i < size; i++) {
                results.add(new FarmerResult(buffer.readItem(), buffer.readFloat()));
            }

            return new FarmerRecipe(id, seeds, soil, crux, stages, results);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FarmerRecipe recipe) {
            recipe.seeds.toNetwork(buffer);
            recipe.soil.toNetwork(buffer);
            recipe.crux.toNetwork(buffer);

            buffer.writeVarInt(recipe.stages);

            buffer.writeVarInt(recipe.results.size());

            for (var result : recipe.results) {
                buffer.writeItem(result.stack());
                buffer.writeFloat(result.chance());
            }
        }
    }
}
