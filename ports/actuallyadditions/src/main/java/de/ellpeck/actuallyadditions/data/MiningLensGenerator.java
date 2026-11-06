package de.ellpeck.actuallyadditions.data;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.crafting.MiningLensRecipe;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MiningLensGenerator extends RecipeProvider {
    public MiningLensGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "Mining Lens " + super.getName();
    }

    @Override
    protected @Nullable CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
        return null; //Nope...
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        buildMiningLens(consumer);
    }

//    private String getItemName(ItemLike item) {
//        return ForgeRegistries.ITEMS.getKey(item.asItem()).getPath();
//    }

    private ResourceLocation folderRecipe(String folder, String recipe) {
        return new ResourceLocation(ActuallyAdditions.MODID, folder + "/" + recipe);
    }

    // SkyTekx: the ore replaceable tags of af8f1f18. forge:stone holds deepslate and tuff too, so a
    // deepslate block got the stone and the deepslate ores. The nether ores use the Forge name of
    // main's c:ore_bearing_ground/netherrack
    private void buildStoneOre(Consumer<FinishedRecipe> consumer, int weight, ItemLike output) {
        buildTagOre(consumer, ActuallyTags.Items.STONE_ORE_REPLACEABLES, "stone", weight, output);
    }
    private void buildNetherOre(Consumer<FinishedRecipe> consumer, int weight, ItemLike output) {
        buildTagOre(consumer, Tags.Items.ORE_BEARING_GROUND_NETHERRACK, "nether", weight, output);
    }

    private void buildDeepSlateOre(Consumer<FinishedRecipe> consumer, int weight, ItemLike output) {
        buildTagOre(consumer, ActuallyTags.Items.DEEPSLATE_ORE_REPLACEABLES, "deepslate", weight, output);
    }

    private void buildTagOre(Consumer<FinishedRecipe> consumer, TagKey<Item> tag, String prefix, int weight, ItemLike output) {
        consumer.accept(new MiningLensRecipe.Result(
                folderRecipe("mininglens", prefix + "_" + getItemName(output)),
                Ingredient.of(tag),
                weight,
                output
        ));
    }

    private void buildMiningLens(Consumer<FinishedRecipe> consumer) {
        buildStoneOre(consumer, 5000, Items.COAL_ORE);
        buildStoneOre(consumer, 3000, Items.IRON_ORE);
        buildStoneOre(consumer, 500, Items.GOLD_ORE);
        buildNetherOre(consumer, 500, Items.NETHER_GOLD_ORE);
        buildStoneOre(consumer, 50, Items.DIAMOND_ORE);
        buildStoneOre(consumer, 250, Items.LAPIS_ORE);
        buildStoneOre(consumer, 200, Items.REDSTONE_ORE);
        buildStoneOre(consumer, 30, Items.EMERALD_ORE);
        buildNetherOre(consumer, 3000, Items.NETHER_QUARTZ_ORE);

        // SkyTekx: the ores of e1cd1b42 (black quartz), f471f4f1 (ancient debris), 482fc7ac (deepslate)
        // and 6005bee8 and d9cae5b5 (copper), in main's order
        buildStoneOre(consumer, 5000, Items.COPPER_ORE);
        buildStoneOre(consumer, 3000, ActuallyBlocks.BLACK_QUARTZ_ORE.getItem());
        buildNetherOre(consumer, 1, Items.ANCIENT_DEBRIS);

        buildDeepSlateOre(consumer, 2000, Items.DEEPSLATE_COAL_ORE);
        buildDeepSlateOre(consumer, 3000, Items.DEEPSLATE_IRON_ORE);
        buildDeepSlateOre(consumer, 3000, Items.DEEPSLATE_COPPER_ORE);
        buildDeepSlateOre(consumer, 500, Items.DEEPSLATE_GOLD_ORE);
        buildDeepSlateOre(consumer, 50, Items.DEEPSLATE_DIAMOND_ORE);
        buildDeepSlateOre(consumer, 250, Items.DEEPSLATE_LAPIS_ORE);
        buildDeepSlateOre(consumer, 200, Items.DEEPSLATE_REDSTONE_ORE);
        buildDeepSlateOre(consumer, 30, Items.DEEPSLATE_EMERALD_ORE);
    }
}
