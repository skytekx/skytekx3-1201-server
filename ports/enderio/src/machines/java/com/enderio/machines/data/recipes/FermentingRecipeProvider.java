package com.enderio.machines.data.recipes;

import com.enderio.EnderIO;
import com.enderio.base.common.init.EIOFluids;
import com.enderio.base.common.tag.EIOTags;
import com.enderio.core.data.recipes.EnderRecipeProvider;
import com.enderio.machines.common.init.MachineRecipes;
import com.enderio.machines.common.recipe.FluidInput;
import com.enderio.machines.common.tag.MachineTags;
import com.google.gson.JsonObject;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The Vat recipes of Ender IO 8.x (6bc3ec343, b1e1ab917, 4b37b9d86), with the 6.x fluid ids and forge tags. Liquid darkness
 * is left out, it only feeds the 8.x solar panel rework.
 */
public class FermentingRecipeProvider extends EnderRecipeProvider {

    public FermentingRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> recipeConsumer) {
        build(new FluidStack(EIOFluids.HOOTCH.getSource(), 250), FluidInput.of(FluidTags.WATER, 1000),
            MachineTags.Items.SEEDS, MachineTags.Items.CROPS, 200, recipeConsumer);
        build(new FluidStack(EIOFluids.ROCKET_FUEL.getSource(), 1000), FluidInput.of(EIOFluids.HOOTCH.getSource(), 1000),
            MachineTags.Items.EXPLOSIVES, Tags.Items.DUSTS_REDSTONE, 400, recipeConsumer);
        build(new FluidStack(EIOFluids.NUTRIENT_DISTILLATION.getSource(), 250), FluidInput.of(FluidTags.WATER, 1000),
            MachineTags.Items.MEAT, MachineTags.Items.CROPS, 200, recipeConsumer);
        build(new FluidStack(EIOFluids.FIRE_WATER.getSource(), 1000), FluidInput.of(EIOFluids.HOOTCH.getSource(), 1000),
            MachineTags.Items.BLAZE_POWDER, Tags.Items.DUSTS_REDSTONE, 400, recipeConsumer);
        build(new FluidStack(EIOFluids.LIQUID_SUNSHINE.getSource(), 1000), FluidInput.of(FluidTags.WATER, 1000),
            MachineTags.Items.NATURAL_LIGHTS, MachineTags.Items.SUNFLOWER, 200, recipeConsumer);
        build(new FluidStack(EIOFluids.CLOUD_SEED.getSource(), 1000), FluidInput.of(FluidTags.WATER, 1000),
            MachineTags.Items.PRISMARINE, MachineTags.Items.CLOUD_COLD, 400, recipeConsumer);
        build(new FluidStack(EIOFluids.CLOUD_SEED_CONCENTRATED.getSource(), 500), FluidInput.of(EIOFluids.CLOUD_SEED.getSource(), 1000),
            MachineTags.Items.LIGHTNING_ROD, MachineTags.Items.WIND_CHARGES, 600, recipeConsumer);
        build(new FluidStack(EIOFluids.DEW_OF_THE_VOID.getSource(), 1000), FluidInput.of(EIOFluids.NUTRIENT_DISTILLATION.getSource(), 4000),
            EIOTags.Items.DUSTS_GRAINS_OF_PIZEALLITY, EIOTags.Items.DUSTS_GRAINS_OF_THE_END, 400, recipeConsumer);
        build(new FluidStack(EIOFluids.VAPOR_OF_LEVITY.getSource(), 1000), FluidInput.of(EIOFluids.DEW_OF_THE_VOID.getSource(), 1000),
            EIOTags.Items.DUSTS_GRAINS_OF_PIZEALLITY, EIOTags.Items.DUSTS_GRAINS_OF_PRESCIENCE, 400, recipeConsumer);
    }

    protected void build(FluidStack output, FluidInput input, TagKey<Item> firstReagent, TagKey<Item> secondReagent, int ticks,
        Consumer<FinishedRecipe> recipeConsumer) {
        ResourceLocation id = EnderIO.loc("fermenting/" + Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(output.getFluid())).getPath());
        recipeConsumer.accept(new FinishedFermentingRecipe(id, input, firstReagent, secondReagent, output, ticks));
    }

    protected static class FinishedFermentingRecipe extends EnderFinishedRecipe {

        private final FluidInput input;
        private final TagKey<Item> firstReagent;
        private final TagKey<Item> secondReagent;
        private final FluidStack output;
        private final int ticks;

        protected FinishedFermentingRecipe(ResourceLocation id, FluidInput input, TagKey<Item> firstReagent, TagKey<Item> secondReagent,
            FluidStack output, int ticks) {
            super(id);
            this.input = input;
            this.firstReagent = firstReagent;
            this.secondReagent = secondReagent;
            this.output = output;
            this.ticks = ticks;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("input", input.toJson());
            // The upstream field names, kept for data compatibility with 8.x.
            json.addProperty("left_reagent", firstReagent.location().toString());
            json.addProperty("right_reagent", secondReagent.location().toString());

            JsonObject outputJson = new JsonObject();
            outputJson.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(output.getFluid())).toString());
            outputJson.addProperty("amount", output.getAmount());
            json.add("output", outputJson);

            json.addProperty("ticks", ticks);

            super.serializeRecipeData(json);
        }

        @Override
        protected Set<String> getModDependencies() {
            Set<String> mods = new HashSet<>();
            input.fluid().ifLeft(fluid -> mods.add(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).getNamespace()));
            mods.add(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(output.getFluid())).getNamespace());
            return mods;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return MachineRecipes.VAT_FERMENTING.serializer().get();
        }
    }
}
