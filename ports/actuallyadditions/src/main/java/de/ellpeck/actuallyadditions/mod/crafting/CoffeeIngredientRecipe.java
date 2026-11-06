package de.ellpeck.actuallyadditions.mod.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SkyTekx: the data driven coffee ingredient of upstream 971fdd65 (with the snake case keys of
 * 718494a9) on the 1.20.1 recipe API. A recipe names an ingredient, the effects it adds to a cup
 * (durations in seconds), the highest amplifier it may reach and an optional lang key that JEI
 * shows. The optional key "milk" is local: such an ingredient weakens the effects that the slots
 * before it added, as the 1.12 milk did (upstream main left milk without any effect)
 */
public class CoffeeIngredientRecipe implements Recipe<Container> {
    public static final String NAME = "coffee_ingredient";
    // SkyTekx: what milk does to every effect already in the cup, as in the 1.12 MilkIngredient
    public static final int MILK_EXTRA_SECONDS = 120;

    private final ResourceLocation id;
    protected final Ingredient ingredient;
    protected final List<EffectInstance> instances;
    protected final List<MobEffectInstance> effects;
    protected final int maxAmplifier;
    protected final String extraText;
    protected final boolean milk;

    public CoffeeIngredientRecipe(ResourceLocation id, Ingredient ingredient, List<EffectInstance> effectInstances, int maxAmplifier, String extraText, boolean milk) {
        this.id = id;
        this.ingredient = ingredient;
        this.maxAmplifier = maxAmplifier;
        this.instances = Collections.unmodifiableList(new ArrayList<>(effectInstances));
        List<MobEffectInstance> list = new ArrayList<>();
        for (EffectInstance instance : effectInstances) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(instance.effect());
            if (effect == null) {
                break;
            }
            list.add(new MobEffectInstance(effect, instance.duration(), instance.amplifier()));
        }
        this.effects = Collections.unmodifiableList(list);
        this.extraText = extraText;
        this.milk = milk;
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public int getMaxAmplifier() {
        return this.maxAmplifier;
    }

    public String getExtraText() {
        return this.extraText;
    }

    public boolean isMilk() {
        return this.milk;
    }

    @Override
    public boolean matches(@Nonnull Container container, @Nonnull Level level) {
        return false;
    }

    public boolean matches(ItemStack itemStack) {
        return this.ingredient.test(itemStack);
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull Container container, @Nonnull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(@Nonnull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ActuallyRecipes.COFFEE_INGREDIENT_RECIPE.get();
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return ActuallyRecipes.Types.COFFEE_INGREDIENT.get();
    }

    /**
     * Adds this ingredient to a cup of coffee.
     *
     * @return whether the ingredient was used up
     */
    public boolean effect(ItemStack stack) {
        if (this.milk) {
            ActuallyAdditionsAPI.methodHandler.weakenEffectsOnStack(stack, MILK_EXTRA_SECONDS);
            return true;
        }
        return ActuallyAdditionsAPI.methodHandler.addRecipeEffectToStack(stack, this);
    }

    public static Optional<CoffeeIngredientRecipe> getIngredientForStack(ItemStack ingredient) {
        return ActuallyAdditionsAPI.COFFEE_MACHINE_INGREDIENTS.stream().filter(recipe -> recipe.matches(ingredient)).findFirst();
    }

    /**
     * Simplified record for an effect of an ingredient, the duration is in seconds
     */
    public record EffectInstance(ResourceLocation effect, int duration, int amplifier) {
        public EffectInstance(MobEffectInstance effect) {
            this(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect()), effect.getDuration(), effect.getAmplifier());
        }

        public static EffectInstance fromJson(JsonObject json) {
            ResourceLocation effect = new ResourceLocation(GsonHelper.getAsString(json, "effect"));
            if (!BuiltInRegistries.MOB_EFFECT.containsKey(effect)) {
                throw new JsonSyntaxException("Unknown effect: " + effect);
            }
            return new EffectInstance(effect, GsonHelper.getAsInt(json, "duration"), GsonHelper.getAsInt(json, "amplifier"));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("effect", this.effect.toString());
            json.addProperty("duration", this.duration);
            json.addProperty("amplifier", this.amplifier);
            return json;
        }

        public static EffectInstance fromNetwork(FriendlyByteBuf pBuffer) {
            ResourceLocation effect = pBuffer.readResourceLocation();
            int duration = pBuffer.readVarInt();
            int amplifier = pBuffer.readVarInt();
            return new EffectInstance(effect, duration, amplifier);
        }

        public void toNetwork(FriendlyByteBuf pBuffer) {
            pBuffer.writeResourceLocation(this.effect);
            pBuffer.writeVarInt(this.duration);
            pBuffer.writeVarInt(this.amplifier);
        }
    }

    public static class Serializer implements RecipeSerializer<CoffeeIngredientRecipe> {
        @Nonnull
        @Override
        public CoffeeIngredientRecipe fromJson(@Nonnull ResourceLocation pRecipeId, @Nonnull JsonObject pJson) {
            if (!pJson.has("ingredient")) {
                throw new JsonSyntaxException(pRecipeId + ": a coffee ingredient needs an ingredient");
            }
            Ingredient ingredient = Ingredient.fromJson(pJson.get("ingredient"), false);
            List<EffectInstance> effects = new ArrayList<>();
            JsonArray effectList = GsonHelper.getAsJsonArray(pJson, "effects", new JsonArray());
            for (JsonElement element : effectList) {
                effects.add(EffectInstance.fromJson(GsonHelper.convertToJsonObject(element, "effect")));
            }
            int maxAmplifier = GsonHelper.getAsInt(pJson, "max_amplifier");
            String extraText = GsonHelper.getAsString(pJson, "extra_text", "");
            boolean milk = GsonHelper.getAsBoolean(pJson, "milk", false);
            return new CoffeeIngredientRecipe(pRecipeId, ingredient, effects, maxAmplifier, extraText, milk);
        }

        @Override
        public CoffeeIngredientRecipe fromNetwork(@Nonnull ResourceLocation pRecipeId, @Nonnull FriendlyByteBuf pBuffer) {
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            int size = pBuffer.readVarInt();
            List<EffectInstance> effects = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                effects.add(EffectInstance.fromNetwork(pBuffer));
            }
            int maxAmplifier = pBuffer.readInt();
            String extraText = pBuffer.readUtf();
            boolean milk = pBuffer.readBoolean();
            return new CoffeeIngredientRecipe(pRecipeId, ingredient, effects, maxAmplifier, extraText, milk);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf pBuffer, CoffeeIngredientRecipe pRecipe) {
            pRecipe.ingredient.toNetwork(pBuffer);
            pBuffer.writeVarInt(pRecipe.instances.size());
            for (EffectInstance effect : pRecipe.instances) {
                effect.toNetwork(pBuffer);
            }
            pBuffer.writeInt(pRecipe.maxAmplifier);
            pBuffer.writeUtf(pRecipe.extraText);
            pBuffer.writeBoolean(pRecipe.milk);
        }
    }

    public static class Result implements FinishedRecipe {
        private final ResourceLocation id;
        private final Ingredient ingredient;
        private final List<EffectInstance> effects;
        private final int maxAmplifier;
        private final String extraText;
        private final boolean milk;

        public Result(ResourceLocation id, Ingredient ingredient, List<EffectInstance> effects, int maxAmplifier, String extraText, boolean milk) {
            this.id = id;
            this.ingredient = ingredient;
            this.effects = effects;
            this.maxAmplifier = maxAmplifier;
            this.extraText = extraText;
            this.milk = milk;
        }

        @Override
        public void serializeRecipeData(@Nonnull JsonObject pJson) {
            pJson.add("ingredient", this.ingredient.toJson());
            if (!this.effects.isEmpty()) {
                JsonArray effectList = new JsonArray();
                for (EffectInstance effect : this.effects) {
                    effectList.add(effect.toJson());
                }
                pJson.add("effects", effectList);
            }
            pJson.addProperty("max_amplifier", this.maxAmplifier);
            if (!this.extraText.isEmpty()) {
                pJson.addProperty("extra_text", this.extraText);
            }
            if (this.milk) {
                pJson.addProperty("milk", true);
            }
        }

        @Nonnull
        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Nonnull
        @Override
        public RecipeSerializer<?> getType() {
            return ActuallyRecipes.COFFEE_INGREDIENT_RECIPE.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
