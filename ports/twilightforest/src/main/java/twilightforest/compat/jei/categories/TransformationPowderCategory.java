package twilightforest.compat.jei.categories;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import twilightforest.TwilightForestMod;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.jei.FakeEntityType;
import twilightforest.compat.jei.JEICompat;
import twilightforest.compat.jei.renderers.EntityRenderer;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.TransformPowderRecipe;

public class TransformationPowderCategory implements IRecipeCategory<TransformPowderRecipe> {
	public static final RecipeType<TransformPowderRecipe> TRANSFORMATION = RecipeType.create(TwilightForestMod.ID, "transformation", TransformPowderRecipe.class);
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable arrow;
	private final IDrawable slot;
	private final IDrawable doubleArrow;
	private final Component localizedName;
	private final EntityRenderer entityRenderer = new EntityRenderer(32);

	public TransformationPowderCategory(IGuiHelper helper) {
		this.background = helper.createBlankDrawable(RecipeViewerConstants.GENERIC_RECIPE_WIDTH, RecipeViewerConstants.GENERIC_RECIPE_HEIGHT);
		this.arrow = helper.drawableBuilder(TwilightForestMod.getGuiTexture("transformation_arrow.png"), 0, 0, 23, 30).setTextureSize(23, 30).build();
		this.doubleArrow = helper.drawableBuilder(TwilightForestMod.getGuiTexture("transformation_double_arrow.png"), 0, 0, 23, 30).setTextureSize(23, 30).build();
		this.slot = helper.drawableBuilder(TwilightForestMod.getGuiTexture("big_slot.png"), 0, 0, 34, 34).setTextureSize(34, 34).build();
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, TFItems.TRANSFORMATION_POWDER.get().getDefaultInstance());
		this.localizedName = Component.translatable("gui.twilightforest.transformation_jei");
	}

	@Override
	public RecipeType<TransformPowderRecipe> getRecipeType() {
		return TRANSFORMATION;
	}

	@Override
	public Component getTitle() {
		return this.localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return this.background;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void draw(TransformPowderRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		if (recipe.isReversible()) {
			this.doubleArrow.draw(graphics, 46, 7);
		} else {
			this.arrow.draw(graphics, 46, 7);
		}
		this.slot.draw(graphics, 7, 10);
		this.slot.draw(graphics, 75, 10);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, TransformPowderRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 8, 11)
				.setCustomRenderer(JEICompat.ENTITY_TYPE, this.entityRenderer)
				.addIngredient(JEICompat.ENTITY_TYPE, new FakeEntityType(recipe.input()));

		SpawnEggItem inputEgg = ForgeSpawnEggItem.fromEntityType(recipe.input());
		if (inputEgg != null) {
			//make it so hovering over the entity shows its name
			builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(inputEgg));
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 76, 11)
				.setCustomRenderer(JEICompat.ENTITY_TYPE, this.entityRenderer)
				.addIngredient(JEICompat.ENTITY_TYPE, new FakeEntityType(recipe.result()));

		SpawnEggItem outputEgg = ForgeSpawnEggItem.fromEntityType(recipe.result());
		if (outputEgg != null) {
			//make it so hovering over the entity shows its name
			builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(outputEgg));
		}
	}
}
