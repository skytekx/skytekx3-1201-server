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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import twilightforest.TwilightForestMod;
import twilightforest.compat.DryingRecipeViewerHelper;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.jei.JEICompat;
import twilightforest.compat.jei.renderers.EntityRenderer;
import twilightforest.compat.jei.util.OminousFireRecipe;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;

/**
 * Mobs killed by ominous fire and what they come back as (the twilightforest:ominous_fire data map).
 */
public class OminousFireCategory implements IRecipeCategory<OminousFireRecipe> {
	public static final RecipeType<OminousFireRecipe> OMINOUS_FIRE = RecipeType.create(TwilightForestMod.ID, "ominous_fire", OminousFireRecipe.class);
	private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable arrow;
	private final IDrawable slot;
	private final Component localizedName;
	private final EntityRenderer entityRenderer = new EntityRenderer(32);

	public OminousFireCategory(IGuiHelper helper) {
		this.background = helper.createBlankDrawable(RecipeViewerConstants.GENERIC_RECIPE_WIDTH, RecipeViewerConstants.GENERIC_RECIPE_HEIGHT);
		// JEI 15.2 has no getRecipeArrow (4.8), so this is the furnace arrow it draws
		this.arrow = helper.createDrawable(FURNACE_TEXTURE, 79, 34, 24, 17);
		this.slot = helper.drawableBuilder(TwilightForestMod.getGuiTexture("big_slot.png"), 0, 0, 34, 34).setTextureSize(34, 34).build();
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(TFItems.EXANIMATE_ESSENCE.get()));
		this.localizedName = Component.translatable("gui.twilightforest.ominous_fire_jei");
	}

	@Override
	public RecipeType<OminousFireRecipe> getRecipeType() {
		return OMINOUS_FIRE;
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
	public void draw(OminousFireRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.arrow.draw(graphics, 46, 19);
		this.slot.draw(graphics, 7, 10);
		this.slot.draw(graphics, 75, 10);

		// 4.8 scales by 20 and then moves by (2.35, -2.2, 19), the same spot as this
		DryingRecipeViewerHelper.renderFlatBlock(graphics.pose(), TFBlocks.OMINOUS_FIRE.get().defaultBlockState(), new Vec3(47.0D, 44.0D, 380.0D), 20.0F);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, OminousFireRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 8, 11)
			.setCustomRenderer(JEICompat.ENTITY_TYPE, this.entityRenderer)
			.addIngredient(JEICompat.ENTITY_TYPE, recipe.input());

		SpawnEggItem inputEgg = ForgeSpawnEggItem.fromEntityType(recipe.input().type());
		if (inputEgg != null) {
			//make it so hovering over the entity shows its name
			builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(new ItemStack(inputEgg));
		}
		builder.addSlot(RecipeIngredientRole.OUTPUT, 76, 11)
			.setCustomRenderer(JEICompat.ENTITY_TYPE, this.entityRenderer)
			.addIngredient(JEICompat.ENTITY_TYPE, recipe.output());

		SpawnEggItem outputEgg = ForgeSpawnEggItem.fromEntityType(recipe.output().type());
		if (outputEgg != null) {
			//make it so hovering over the entity shows its name
			builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(new ItemStack(outputEgg));
		}
	}
}
