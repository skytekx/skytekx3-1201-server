package de.ellpeck.actuallyadditions.data;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.crafting.CoffeeIngredientRecipe;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

// SkyTekx: the coffee ingredients of upstream 971fdd65 as on main, written in the 1.20.1 form. The
// tagged milk looks at forge:milk instead of c:milk, and both milks carry the local key "milk"
public class CoffeeIngredientGenerator extends RecipeProvider {
    public CoffeeIngredientGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "Coffee Ingredient " + super.getName();
    }

    @Override
    protected @Nullable CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
        return null; //Nope...
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        buildMilk(consumer, ingredientId(Items.MILK_BUCKET), Ingredient.of(Items.MILK_BUCKET));

        //Pam's puts milk in a tag, so we'll use that
        TagKey<Item> milkTag = ItemTags.create(new ResourceLocation("forge", "milk"));
        ResourceLocation taggedId = new ResourceLocation(ActuallyAdditions.MODID, "coffee_ingredient/milk_tagged");
        ConditionalRecipe.builder()
                .addCondition(new NotCondition(new TagEmptyCondition(milkTag.location())))
                .addRecipe(milk(taggedId, Ingredient.of(milkTag)))
                .build(consumer, taggedId);

        buildIngredient(consumer, Items.SUGAR, 4, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0));
        buildIngredient(consumer, Items.MAGMA_CREAM, 2, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20, 0));
        buildIngredient(consumer, Items.PUFFERFISH, 2, new MobEffectInstance(MobEffects.WATER_BREATHING, 10, 0));
        buildIngredient(consumer, Items.GOLDEN_CARROT, 2, new MobEffectInstance(MobEffects.NIGHT_VISION, 30, 0));
        buildIngredient(consumer, Items.GHAST_TEAR, 3, new MobEffectInstance(MobEffects.REGENERATION, 5, 0));
        buildIngredient(consumer, Items.BLAZE_POWDER, 4, new MobEffectInstance(MobEffects.DAMAGE_BOOST, 15, 0));
        buildIngredient(consumer, Items.FERMENTED_SPIDER_EYE, 2, new MobEffectInstance(MobEffects.INVISIBILITY, 25, 0));
    }

    private static ResourceLocation ingredientId(ItemLike ingredient) {
        return new ResourceLocation(ActuallyAdditions.MODID, "coffee_ingredient/" + getItemName(ingredient));
    }

    private static CoffeeIngredientRecipe.Result milk(ResourceLocation id, Ingredient ingredient) {
        return new CoffeeIngredientRecipe.Result(id, ingredient, List.of(), 0, "jei.actuallyadditions.coffee.extra.milk", true);
    }

    private void buildMilk(Consumer<FinishedRecipe> consumer, ResourceLocation id, Ingredient ingredient) {
        consumer.accept(milk(id, ingredient));
    }

    private void buildIngredient(Consumer<FinishedRecipe> consumer, ItemLike ingredient, int maxAmplifier, MobEffectInstance... effects) {
        List<CoffeeIngredientRecipe.EffectInstance> instances = new ArrayList<>();
        for (MobEffectInstance effect : effects) {
            instances.add(new CoffeeIngredientRecipe.EffectInstance(effect));
        }
        consumer.accept(new CoffeeIngredientRecipe.Result(ingredientId(ingredient), Ingredient.of(ingredient), instances, maxAmplifier, "", false));
    }
}
