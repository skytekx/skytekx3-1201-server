package twilightforest.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiAnvilRecipe;
import dev.emi.emi.recipe.EmiGrindstoneRecipe;
import dev.emi.emi.recipe.special.EmiAnvilEnchantRecipe;
import dev.emi.emi.recipe.special.EmiAnvilRepairItemRecipe;
import dev.emi.emi.recipe.special.EmiGrindstoneDisenchantingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import twilightforest.TFConfig;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.emi.recipes.*;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.item.recipe.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The native EMI plugin (4.8). EMI finds it through {@link EmiEntrypoint}, so nothing here loads without EMI; it only
 * runs on the client. While EMI is installed the JEI plugin registers nothing ({@code JEICompat#isEmiInstalled}), so EMI
 * does not show every Twilight Forest recipe twice through its JEI bridge.
 */
@EmiEntrypoint
public class TFEmiCompat implements EmiPlugin {

	// 4.8 also lists the six Traveller's Gear pieces, which this port does not have yet
	private static final Function<List<EmiIngredient>, Boolean> CANT_USE_ENCHANTS = stack ->
		stack.contains(EmiStack.of(TFItems.MOONWORM_QUEEN.get())) || stack.contains(EmiStack.of(TFItems.LAMP_OF_CINDERS.get())) || stack.contains(EmiStack.of(TFItems.ORE_MAGNET.get())) ||
			stack.contains(EmiStack.of(TFItems.TWILIGHT_SCEPTER.get())) || stack.contains(EmiStack.of(TFItems.LIFEDRAIN_SCEPTER.get())) ||
			stack.contains(EmiStack.of(TFItems.ZOMBIE_SCEPTER.get())) || stack.contains(EmiStack.of(TFItems.FORTIFICATION_SCEPTER.get()));

	private static final Function<List<EmiIngredient>, Boolean> NO_REPAIRING = stack ->
		stack.contains(EmiStack.of(TFItems.LAMP_OF_CINDERS.get())) || stack.contains(EmiStack.of(TFItems.GLASS_SWORD.get())) || stack.contains(EmiStack.of(TFItems.MAZEBREAKER_PICKAXE.get()));

	@Override
	public void register(EmiRegistry registry) {

		registry.addCategory(TFEmiCategories.UNCRAFTING);
		registry.addCategory(TFEmiCategories.CRUMBLE_HORN);
		registry.addCategory(TFEmiCategories.TRANSFORMATION);
		registry.addCategory(TFEmiCategories.EXANIMATE);
		registry.addCategory(TFEmiCategories.DRYING);

		boolean tableEnabled = !TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get();
		if (tableEnabled) {
			registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(TFBlocks.UNCRAFTING_TABLE.get()));
			registry.addWorkstation(TFEmiCategories.UNCRAFTING, EmiStack.of(TFBlocks.UNCRAFTING_TABLE.get()));
		}
		registry.addWorkstation(TFEmiCategories.CRUMBLE_HORN, EmiStack.of(TFItems.CRUMBLE_HORN.get()));
		registry.addWorkstation(TFEmiCategories.TRANSFORMATION, EmiStack.of(TFItems.TRANSFORMATION_POWDER.get()));
		registry.addWorkstation(TFEmiCategories.EXANIMATE, EmiStack.of(TFItems.EXANIMATE_ESSENCE.get()));
		// 4.8 leaves the drying category without workstations; list the racks like the JEI plugin's catalysts
		for (var block : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTagGenerator.DRYING_RACKS)) {
			registry.addWorkstation(TFEmiCategories.DRYING, EmiStack.of(block.value()));
		}

		RecipeManager manager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
		if (tableEnabled) {
			for (CraftingRecipe recipe : RecipeViewerConstants.getAllUncraftingRecipes(manager)) {
				registry.addRecipe(new EmiUncraftingRecipe<>(recipe));
			}
		}

		// 4.8 reads transformations and crumbles from data maps; this port still has 4.3's recipe types for them
		for (TransformPowderRecipe recipe : manager.getAllRecipesFor(TFRecipes.TRANSFORM_POWDER_RECIPE.get())) {
			registry.addRecipe(new EmiTransformationPowderRecipe(recipe));
		}

		for (RecipeViewerConstants.OminousFireInfo info : RecipeViewerConstants.getOminousFireRecipes()) {
			registry.addRecipe(new EmiOminousFireRecipe(info.input(), info.output()));
		}

		for (CrumbleRecipe recipe : manager.getAllRecipesFor(TFRecipes.CRUMBLE_RECIPE.get())) {
			registry.addRecipe(new EmiCrumbleHornRecipe(recipe));
		}

		for (SmithingRecipe smithing : manager.getAllRecipesFor(RecipeType.SMITHING)) {
			if (smithing instanceof NoTemplateSmithingRecipe recipe) {
				registry.addRecipe(new EmiNoSmithingTemplateRecipe(EmiIngredient.of(recipe.getBase()), EmiIngredient.of(recipe.getAddition()), EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())), recipe));
			}
		}

		// Stale Bread stays a secret
		for (DryingRecipe recipe : manager.getAllRecipesFor(TFRecipes.DRYING_RECIPE.get())) {
			if (!recipe.getResult().is(TFItems.STALE_BREAD.get())) {
				registry.addRecipe(new EmiDryingRecipe(recipe));
			}
		}

		// the special crafting recipes are only listed while a data pack provides them (#2320)
		for (CraftingRecipe crafting : manager.getAllRecipesFor(RecipeType.CRAFTING)) {
			EmiRecipe emiRecipe = null;
			if (crafting instanceof MoonwormQueenRepairRecipe recipe) {
				emiRecipe = new EmiMoonwormQueenRecipe(recipe);
			} else if (crafting instanceof EmperorsClothRecipe) {
				emiRecipe = new EmiEmperorsClothRecipe();
			} else if (crafting instanceof ScepterRepairRecipe recipe) {
				emiRecipe = new EmiScepterRepairRecipe(
					recipe.getIngredients().stream()
						.map(EmiIngredient::of)
						.toList(),
					EmiStack.of(recipe.getScepter()),
					recipe.getRepairDurability(),
					recipe.getId()
				);
			} else if (crafting instanceof CasketRepairRecipe recipe) {
				emiRecipe = new EmiCasketRepairRecipe(recipe.getId());
			} else if (crafting instanceof EssenceRepairRecipe recipe) {
				emiRecipe = new EmiExanimateEssenceRepairRecipe(recipe.getId());
			}

			if (emiRecipe != null) {
				registry.addRecipe(emiRecipe);
			}
		}

		//remove other recipes as they aren't actually possible recipes to use
		//emi makes a few assumptions about damageable items that it honestly shouldnt
		registry.removeRecipes(recipe -> {
			if (recipe instanceof EmiPatternCraftingRecipe || recipe instanceof EmiGrindstoneRecipe) {
				return recipe.getInputs().contains(EmiStack.of(TFItems.MOONWORM_QUEEN.get())) || NO_REPAIRING.apply(recipe.getInputs());
			} else if (recipe instanceof EmiGrindstoneDisenchantingRecipe || recipe instanceof EmiAnvilEnchantRecipe) {
				return CANT_USE_ENCHANTS.apply(recipe.getInputs());
			} else if (recipe instanceof EmiAnvilRepairItemRecipe || recipe instanceof EmiAnvilRecipe) {
				return NO_REPAIRING.apply(recipe.getInputs());
			}
			return false;
		});

		// the painting variants differ only by NBT, so EMI would list one arbitrary variant (4.8 3ae3c3575)
		registry.removeEmiStacks(stack -> TFItems.MAGIC_PAINTING.getId().equals(stack.getId()));
	}
}
