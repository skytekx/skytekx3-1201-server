package de.ellpeck.actuallyadditions.data;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.crafting.CrushingRecipe;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CrushingRecipeGenerator extends RecipeProvider {
    public CrushingRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "Crushing " + super.getName();
    }

    @Override
    protected @Nullable CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
        return null; //Nope...
    }

    // SkyTekx: the crushing recipes of upstream ef6e52f8 and 7c91da5a (#1405) as on main, written in
    // the 1.20.1 result form (item, count and chance per output) that CrushingRecipe.Serializer reads
    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        new CrushingBuilder(Ingredient.of(Items.BONE), new CrushingRecipe.CrushingResult(new ItemStack(Items.BONE_MEAL, 6), 1.0f))
                .save(consumer, "bone");
        new CrushingBuilder(Ingredient.of(Items.SUGAR_CANE), new CrushingRecipe.CrushingResult(new ItemStack(Items.SUGAR, 3), 1.0f))
                .save(consumer, "sugar_cane");
        new CrushingBuilder(Ingredient.of(Items.BLAZE_ROD), new CrushingRecipe.CrushingResult(new ItemStack(Items.BLAZE_POWDER, 3), 1.0f))
                .save(consumer, "blaze_rod");

        new CrushingBuilder(Ingredient.of(Items.DANDELION), new CrushingRecipe.CrushingResult(new ItemStack(Items.YELLOW_DYE, 3), 1.0f))
                .save(consumer, "danedlion");
        new CrushingBuilder(Ingredient.of(Items.POPPY), new CrushingRecipe.CrushingResult(new ItemStack(Items.RED_DYE, 3), 1.0f))
                .save(consumer, "poppy");
        new CrushingBuilder(Ingredient.of(Items.BLUE_ORCHID), new CrushingRecipe.CrushingResult(new ItemStack(Items.LIGHT_BLUE_DYE, 3), 1.0f))
                .save(consumer, "blue_orchid");
        new CrushingBuilder(Ingredient.of(Items.ALLIUM), new CrushingRecipe.CrushingResult(new ItemStack(Items.MAGENTA_DYE, 3), 1.0f))
                .save(consumer, "allium");
        new CrushingBuilder(Ingredient.of(Items.AZURE_BLUET), new CrushingRecipe.CrushingResult(new ItemStack(Items.LIGHT_GRAY_DYE, 3), 1.0f))
                .save(consumer, "azure_bluet");
        new CrushingBuilder(Ingredient.of(Items.RED_TULIP), new CrushingRecipe.CrushingResult(new ItemStack(Items.RED_DYE, 3), 1.0f))
                .save(consumer, "red_tulip");
        new CrushingBuilder(Ingredient.of(Items.ORANGE_TULIP), new CrushingRecipe.CrushingResult(new ItemStack(Items.ORANGE_DYE, 3), 1.0f))
                .save(consumer, "orange_tulip");
        new CrushingBuilder(Ingredient.of(Items.WHITE_TULIP), new CrushingRecipe.CrushingResult(new ItemStack(Items.LIGHT_GRAY_DYE, 3), 1.0f))
                .save(consumer, "white_tulip");
        new CrushingBuilder(Ingredient.of(Items.PINK_TULIP), new CrushingRecipe.CrushingResult(new ItemStack(Items.PINK_DYE, 3), 1.0f))
                .save(consumer, "pink_tulip");
        new CrushingBuilder(Ingredient.of(Items.PINK_PETALS), new CrushingRecipe.CrushingResult(new ItemStack(Items.PINK_DYE, 3), 1.0f))
                .save(consumer, "pink_petals");
        new CrushingBuilder(Ingredient.of(Items.OXEYE_DAISY), new CrushingRecipe.CrushingResult(new ItemStack(Items.LIGHT_GRAY_DYE, 3), 1.0f))
                .save(consumer, "oxeye_daisy");
        new CrushingBuilder(Ingredient.of(Items.CORNFLOWER), new CrushingRecipe.CrushingResult(new ItemStack(Items.BLUE_DYE, 3), 1.0f))
                .save(consumer, "cornflower");
        new CrushingBuilder(Ingredient.of(Items.LILY_OF_THE_VALLEY), new CrushingRecipe.CrushingResult(new ItemStack(Items.WHITE_DYE, 3), 1.0f))
                .save(consumer, "lily_of_the_valley");
        new CrushingBuilder(Ingredient.of(Items.WITHER_ROSE), new CrushingRecipe.CrushingResult(new ItemStack(Items.BLACK_DYE, 3), 1.0f))
                .save(consumer, "wither_rose");
        new CrushingBuilder(Ingredient.of(Items.SUNFLOWER), new CrushingRecipe.CrushingResult(new ItemStack(Items.YELLOW_DYE, 4), 1.0f))
                .save(consumer, "sunflower");
        new CrushingBuilder(Ingredient.of(Items.LILAC), new CrushingRecipe.CrushingResult(new ItemStack(Items.MAGENTA_DYE, 4), 1.0f))
                .save(consumer, "lilac");
        new CrushingBuilder(Ingredient.of(Items.ROSE_BUSH), new CrushingRecipe.CrushingResult(new ItemStack(Items.RED_DYE, 4), 1.0f))
                .save(consumer, "rose_bush");
        new CrushingBuilder(Ingredient.of(Items.PEONY), new CrushingRecipe.CrushingResult(new ItemStack(Items.PINK_DYE, 4), 1.0f))
                .save(consumer, "peony");
        new CrushingBuilder(Ingredient.of(Items.PITCHER_PLANT), new CrushingRecipe.CrushingResult(new ItemStack(Items.CYAN_DYE, 4), 1.0f))
                .save(consumer, "pitcher_plant");

        new CrushingBuilder(Ingredient.of(Items.REDSTONE_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.REDSTONE, 10), 1.0f))
                .save(consumer, "redstone_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_REDSTONE_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.REDSTONE, 10), 1.0f))
                .save(consumer, "deepslate_redstone_ore");
        new CrushingBuilder(Ingredient.of(Items.LAPIS_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.LAPIS_LAZULI, 12), 1.0f))
                .save(consumer, "lapis_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_LAPIS_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.LAPIS_LAZULI, 12), 1.0f))
                .save(consumer, "deepslate_lapis_ore");
        new CrushingBuilder(Ingredient.of(Items.COAL_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.COAL, 3), 1.0f))
                .save(consumer, "coal_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_COAL_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.COAL, 3), 1.0f))
                .save(consumer, "deepslate_coal_ore");
        new CrushingBuilder(Ingredient.of(Items.COAL_BLOCK), new CrushingRecipe.CrushingResult(new ItemStack(Items.COAL, 9), 1.0f))
                .save(consumer, "coal_block");
        new CrushingBuilder(Ingredient.of(Items.NETHER_QUARTZ_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.QUARTZ, 3), 1.0f))
                .save(consumer, "nether_quartz_ore");
        new CrushingBuilder(Ingredient.of(Items.COBBLESTONE), new CrushingRecipe.CrushingResult(new ItemStack(Items.SAND, 1), 1.0f))
                .save(consumer, "cobblestone");
        new CrushingBuilder(Ingredient.of(Items.GRAVEL), new CrushingRecipe.CrushingResult(new ItemStack(Items.FLINT, 1), 1.0f))
                .addResult(new CrushingRecipe.CrushingResult(new ItemStack(Items.FLINT, 1), 0.5f))
                .save(consumer, "gravel");
        new CrushingBuilder(Ingredient.of(ActuallyItems.RICE.get()), new CrushingRecipe.CrushingResult(new ItemStack(Items.SUGAR, 2), 1.0f))
                .save(consumer, "rice");
        new CrushingBuilder(Ingredient.of(Items.GLOWSTONE), new CrushingRecipe.CrushingResult(new ItemStack(Items.GLOWSTONE_DUST, 4), 1.0f))
                .save(consumer, "glowstone");
        new CrushingBuilder(Ingredient.of(Items.DIAMOND_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.DIAMOND, 2), 1.0f))
                .save(consumer, "diamond_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_DIAMOND_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.DIAMOND, 2), 1.0f))
                .save(consumer, "deepslate_diamond_ore");
        new CrushingBuilder(Ingredient.of(Items.EMERALD_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.EMERALD, 2), 1.0f))
                .save(consumer, "emerald_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_EMERALD_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.EMERALD, 2), 1.0f))
                .save(consumer, "deepslate_emerald_ore");
        new CrushingBuilder(Ingredient.of(Items.PRISMARINE_SHARD), new CrushingRecipe.CrushingResult(new ItemStack(Items.PRISMARINE_CRYSTALS, 1), 1.0f))
                .save(consumer, "prismarine_shard");
        new CrushingBuilder(Ingredient.of(ActuallyBlocks.BLACK_QUARTZ_ORE.get()), new CrushingRecipe.CrushingResult(new ItemStack(ActuallyItems.BLACK_QUARTZ.get(), 2), 1.0f))
                .save(consumer, "black_quartz_ore");
        new CrushingBuilder(Ingredient.of(Items.COPPER_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_COPPER, 2), 1.0f))
                .addResult(new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 1), 0.05f))
                .save(consumer, "copper_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_COPPER_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_COPPER, 2), 1.0f))
                .addResult(new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 1), 0.05f))
                .save(consumer, "deepslate_copper_ore");

        //TODO: Think about the recipes that returned crushed ores before and what to replace them with
        new CrushingBuilder(Ingredient.of(Items.IRON_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_IRON, 2), 1.0f))
                .addResult(new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 1), 0.2f))
                .save(consumer, "iron_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_IRON_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_IRON, 2), 1.0f))
                .addResult(new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 1), 0.2f))
                .save(consumer, "deepslate_iron_ore");
        new CrushingBuilder(Ingredient.of(Items.IRON_HORSE_ARMOR), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_IRON, 6), 1.0f))
                .save(consumer, "iron_horse_armor");
        new CrushingBuilder(Ingredient.of(Items.GOLDEN_HORSE_ARMOR), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 6), 1.0f))
                .save(consumer, "golden_horse_armor");
        new CrushingBuilder(Ingredient.of(Items.DIAMOND_HORSE_ARMOR), new CrushingRecipe.CrushingResult(new ItemStack(Items.DIAMOND, 6), 1.0f))
                .save(consumer, "diamond_horse_armor");
        new CrushingBuilder(Ingredient.of(Items.GOLD_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 2), 1.0f))
                .save(consumer, "gold_ore");
        new CrushingBuilder(Ingredient.of(Items.DEEPSLATE_GOLD_ORE), new CrushingRecipe.CrushingResult(new ItemStack(Items.RAW_GOLD, 2), 1.0f))
                .save(consumer, "deepslate_gold_ore");

    }

    public static class CrushingBuilder {
        private final Ingredient ingredient;
        private final List<CrushingRecipe.CrushingResult> results = new ArrayList<>();

        public CrushingBuilder(Ingredient ingredient, CrushingRecipe.CrushingResult result) {
            this.ingredient = ingredient;
            this.results.add(result);
        }

        public CrushingBuilder addResult(CrushingRecipe.CrushingResult result) {
            this.results.add(result);
            return this;
        }

        public void save(Consumer<FinishedRecipe> consumer, ResourceLocation name) {
            if (this.results.size() > 2) {
                throw new IllegalStateException("invalid result count: " + this.results.size() + ", recipe: " + name);
            }

            CrushingRecipe.CrushingResult first = this.results.get(0);
            CrushingRecipe.CrushingResult second = this.results.size() > 1
                ? this.results.get(1)
                : CrushingRecipe.CrushingResult.EMPTY;
            consumer.accept(new CrushingRecipe.Result(name, this.ingredient,
                first.stack().getItem(), first.stack().getCount(), first.chance(),
                second.stack().getItem(), second.stack().getCount(), second.chance()));
        }

        public void save(Consumer<FinishedRecipe> consumer, String name) {
            this.save(consumer, new ResourceLocation(ActuallyAdditions.MODID, "crushing/" + name));
        }
    }
}
