package twilightforest.compat.jei.categories;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.compat.DryingRecipeViewerHelper;
import twilightforest.init.TFBlocks;
import twilightforest.item.recipe.DryingRecipe;

import java.util.List;

public class DryingCategory implements IRecipeCategory<DryingRecipe> {

	public static final RecipeType<DryingRecipe> DRYING = RecipeType.create(TwilightForestMod.ID, "drying", DryingRecipe.class);
	public static final int WIDTH = 70;
	public static final int HEIGHT = 30;
	private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");
	private final IDrawable background;
	private final IDrawable slot;
	private final IDrawable emptyArrow;
	private final IDrawable arrow;
	private final IDrawable icon;
	private final Component localizedName;

	public DryingCategory(IGuiHelper helper) {
		this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
		// JEI 15.2 has neither 4.8's standard slot backgrounds nor createAnimatedRecipeArrow, so the slots and the furnace arrow
		// (filling up over a minute, like 4.8's) are drawn by hand
		this.slot = helper.getSlotDrawable();
		this.emptyArrow = helper.createDrawable(FURNACE_TEXTURE, 79, 34, 24, 17);
		this.arrow = helper.drawableBuilder(FURNACE_TEXTURE, 176, 14, 24, 17).buildAnimated(20 * 60, IDrawableAnimated.StartDirection.LEFT, false);
		this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, TFBlocks.SORTING_DRYING_RACK.get().asItem().getDefaultInstance());
		this.localizedName = Component.translatable("gui.twilightforest.drying_jei");
	}

	@Override
	public RecipeType<DryingRecipe> getRecipeType() {
		return DRYING;
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
	public void draw(DryingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.slot.draw(graphics, 0, 0);
		this.slot.draw(graphics, 52, 0);
		this.emptyArrow.draw(graphics, 23, 1);
		this.arrow.draw(graphics, 23, 1);

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		Component time = DryingRecipeViewerHelper.getDryingTime(recipe.getDryingTime());
		graphics.drawString(font, time, 35 - font.width(time.getString()) / 2, 20, 0xFF808080, false);

		DryingRecipeViewerHelper.renderFlatBlock(graphics.pose(), TFBlocks.OAK_DRYING_RACK.get().defaultBlockState(), new Vec3(-1.0F, 19.0F, 201.0D), 20.0F);
		DryingRecipeViewerHelper.renderFlatBlock(graphics.pose(), TFBlocks.OAK_DRYING_RACK.get().defaultBlockState(), new Vec3(51.0F, 19.0F, 201.0D), 20.0F);
	}

	@Override
	public List<Component> getTooltipStrings(DryingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		if (mouseX > 23 && mouseX < 47 && mouseY > 1 && mouseY < 17) {
			return List.of(Component.translatable("gui.twilightforest.drying_ticks", recipe.getDryingTime()).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
		return List.of();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(recipe.getInput());

		builder.addSlot(RecipeIngredientRole.OUTPUT, 53, 1).addItemStack(recipe.getResult());
	}
}
