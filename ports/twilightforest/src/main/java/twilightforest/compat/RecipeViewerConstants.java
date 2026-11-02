package twilightforest.compat;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.crafting.IShapedRecipe;
import twilightforest.TFConfig;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFDataMaps;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.util.datamaps.EntityTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Shared by the recipe viewer plugins. Client only: only ever loaded by a recipe viewer.
 */
public class RecipeViewerConstants {
	public static final int GENERIC_RECIPE_WIDTH = 116;
	public static final int GENERIC_RECIPE_HEIGHT = 54;
	public static final Component MOONWORM_QUEEN_TOOLTIP = Component.translatable("item.twilightforest.moonworm_queen.jei_info_message").withStyle(ChatFormatting.GREEN);

	public static final ItemStack DAMAGED_MOONWORM_QUEEN = Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(256));
	//trickery is afoot: every slot of a recipe cycles in lockstep, so these line up with MOONWORM_QUEEN_LIST (1 to 4 berries)
	public static final List<ItemStack> BERRY_2_LIST = List.of(ItemStack.EMPTY, new ItemStack(TFItems.TORCHBERRIES.get()), new ItemStack(TFItems.TORCHBERRIES.get()), new ItemStack(TFItems.TORCHBERRIES.get()));
	public static final List<ItemStack> BERRY_3_LIST = List.of(ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(TFItems.TORCHBERRIES.get()), new ItemStack(TFItems.TORCHBERRIES.get()));
	public static final List<ItemStack> BERRY_4_LIST = List.of(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(TFItems.TORCHBERRIES.get()));
	public static final List<ItemStack> MOONWORM_QUEEN_LIST = List.of(
		Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(192)),
		Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(128)),
		Util.make(new ItemStack(TFItems.MOONWORM_QUEEN.get()), stack -> stack.setDamageValue(64)),
		new ItemStack(TFItems.MOONWORM_QUEEN.get()));

	public static List<CraftingRecipe> getAllUncraftingRecipes(RecipeManager manager) {
		TFConfig.Common.UncraftingStuff config = TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS;
		if (!config.disableUncraftingOnly.get()) { //we only do this if uncrafting is not disabled
			List<CraftingRecipe> recipes = manager.getAllRecipesFor(RecipeType.CRAFTING);
			recipes = recipes.stream().filter(recipe ->
					!recipe.getResultItem(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()).isEmpty() && //get rid of empty items
						!recipe.getResultItem(Minecraft.getInstance().level.registryAccess()).is(ItemTagGenerator.BANNED_UNCRAFTABLES) && //Prevents things that are tagged as banned from showing up
						config.reverseRecipeBlacklist.get() == config.disableUncraftingRecipes.get().contains(recipe.getId().toString()) && //remove disabled recipes
						config.flipUncraftingModIdList.get() == config.blacklistedUncraftingModIds.get().contains(recipe.getId().getNamespace())) //remove blacklisted mod ids
				.collect(Collectors.toList());
			recipes.removeIf(recipe -> (recipe instanceof ShapelessRecipe && !config.allowShapelessUncrafting.get()));
			recipes.addAll(manager.getAllRecipesFor(TFRecipes.UNCRAFTING_RECIPE.get()));
			return recipes;
		} else {
			return new ArrayList<>(manager.getAllRecipesFor(TFRecipes.UNCRAFTING_RECIPE.get()));
		}
	}

	/**
	 * All recipe viewers run this once when initializing recipes. The ominous fire transformations are a data map, read from
	 * the copy the server synced (SyncDataMapPacket), so a client on a dedicated server sees the server's data packs.
	 */
	public static List<OminousFireInfo> getOminousFireRecipes() {
		Map<ResourceLocation, EntityTransformation> synced = TFDataMaps.OMINOUS_FIRE.getSyncedValues();
		List<EntityType<?>> inputs = new ArrayList<>();
		List<OminousFireInfo> info = new ArrayList<>();
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			if (synced.containsKey(BuiltInRegistries.ENTITY_TYPE.getKey(type))) {
				inputs.add(type);
			}
		}

		for (EntityType<?> input : inputs) {
			EntityTransformation output = synced.get(BuiltInRegistries.ENTITY_TYPE.getKey(input));
			if (output != null) {
				OminousFireInfo dummy = new OminousFireInfo(output.result(), input);
				if (!info.contains(dummy)) {
					info.add(new OminousFireInfo(input, output.result()));
				}
			}
		}
		return info;
	}

	/**
	 * The XP levels the Uncrafting Table charges for these ingredients (#2059), the same count as
	 * UncraftingMenu#countDamageableParts times the uncraftingXpCostMultiplier the server synced.
	 */
	public static int getRecipeCost(List<ItemStack> inputs) {
		int cost = 0;
		for (ItemStack stack : inputs) {
			if (UncraftingMenu.isDamageableComponent(stack) && !UncraftingMenu.isIngredientProblematic(stack) && !UncraftingMenu.isMarked(stack)) {
				cost++;
			}
		}
		return (int) Math.round(cost * TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.uncraftingXpCostMultiplier.get());
	}

	public static int getXPColor(int cost) {
		Player player = Minecraft.getInstance().player;
		if (player != null && player.experienceLevel < cost && !player.getAbilities().instabuild) {
			return 0xA00000;
		} else {
			return 0x80FF20;
		}
	}

	//copy of JEI's CraftingGridHelper.getCraftingIndex
	public static int getCraftingIndex(CraftingRecipe recipe, int i) {
		int width = getRecipeWidth(recipe);
		int height = getRecipeHeight(recipe);
		int index;
		if (width == 1) {
			if (height == 3) {
				index = (i * 3) + 1;
			} else if (height == 2) {
				index = (i * 3) + 1;
			} else {
				index = 4;
			}
		} else if (height == 1) {
			index = i + 3;
		} else if (width == 2) {
			index = i;
			if (i > 1) {
				index++;
				if (i > 3) {
					index++;
				}
			}
		} else if (height == 2) {
			index = i + 3;
		} else {
			index = i;
		}
		return index;
	}

	// 4.8 checks ShapedRecipe and JEI's internal JeiShapedRecipe; Forge's IShapedRecipe covers vanilla, modded and TF uncrafting recipes
	public static int getRecipeWidth(CraftingRecipe recipe) {
		if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
			return shapedRecipe.getRecipeWidth();
		}
		return 0;
	}

	public static int getRecipeHeight(CraftingRecipe recipe) {
		if (recipe instanceof IShapedRecipe<?> shapedRecipe) {
			return shapedRecipe.getRecipeHeight();
		}
		return 0;
	}

	public record OminousFireInfo(EntityType<?> input, EntityType<?> output) {
	}
}
