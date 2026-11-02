package twilightforest.compat.emi.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import twilightforest.TwilightForestMod;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.emi.TFEmiCategories;
import twilightforest.compat.emi.widget.EmiEntityWidget;
import twilightforest.item.recipe.TransformPowderRecipe;

import java.util.ArrayList;
import java.util.List;

// 4.8 reads the transformation_powder data map; here the transformations are still 4.3's twilightforest:transformation recipes
public class EmiTransformationPowderRecipe implements EmiRecipe {
	private static final int WIDTH = RecipeViewerConstants.GENERIC_RECIPE_WIDTH;
	//height is adjusted slightly to allow 2 entries per page
	private static final int HEIGHT = RecipeViewerConstants.GENERIC_RECIPE_HEIGHT - 8;

	public static final EmiTexture SLOT = new EmiTexture(TwilightForestMod.getGuiTexture("big_slot.png"), 0, 0, 34, 34, 34, 34, 34, 34);
	public static final EmiTexture SINGLE_ARROW = new EmiTexture(TwilightForestMod.getGuiTexture("transformation_arrow.png"), 0, 0, 23, 30, 23, 30, 23, 30);
	public static final EmiTexture DOUBLE_ARROW = new EmiTexture(TwilightForestMod.getGuiTexture("transformation_double_arrow.png"), 0, 0, 23, 30, 23, 30, 23, 30);

	private final ResourceLocation id;
	private final EntityType<?> input;
	private final EntityType<?> output;
	private final boolean isReversible;
	public final EmiTexture arrow;

	public EmiTransformationPowderRecipe(TransformPowderRecipe recipe) {
		// keyed by the recipe rather than the output type like 4.8, so two inputs with the same result stay apart
		this.id = TwilightForestMod.prefix("/transformation/" + recipe.getId().getNamespace() + "/" + recipe.getId().getPath());
		this.input = recipe.input();
		this.output = recipe.result();
		this.isReversible = recipe.isReversible();
		this.arrow = this.isReversible ? DOUBLE_ARROW : SINGLE_ARROW;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return TFEmiCategories.TRANSFORMATION;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		List<EmiIngredient> inputs = new ArrayList<>();
		SpawnEggItem inputEgg = ForgeSpawnEggItem.fromEntityType(this.input);
		SpawnEggItem outputEgg = ForgeSpawnEggItem.fromEntityType(this.output);
		if (inputEgg != null) {
			inputs.add(EmiStack.of(inputEgg));
			if (this.isReversible && outputEgg != null) {
				inputs.add(EmiStack.of(outputEgg));
			}
		}
		return inputs;
	}

	@Override
	public List<EmiStack> getOutputs() {
		List<EmiStack> outputs = new ArrayList<>();

		SpawnEggItem inputEgg = ForgeSpawnEggItem.fromEntityType(this.input);
		SpawnEggItem outputEgg = ForgeSpawnEggItem.fromEntityType(this.output);
		if (outputEgg != null) {
			outputs.add(EmiStack.of(outputEgg));
			if (this.isReversible && inputEgg != null) {
				outputs.add(EmiStack.of(inputEgg));
			}
		}
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return WIDTH;
	}

	@Override
	public int getDisplayHeight() {
		return HEIGHT;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(SLOT, 7, 10);
		widgets.add(new EmiEntityWidget(this.input, 7, 12, 32));
		widgets.addTexture(this.arrow, 46, 7);
		widgets.addTexture(SLOT, 75, 10);
		widgets.add(new EmiEntityWidget(this.output, 75, 12, 32));
	}

	//doesn't make sense to have this on recipe trees
	@Override
	public boolean supportsRecipeTree() {
		return false;
	}
}
