package ianm1647.expandeddelight.integration.jei.category;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.crafting.JuicerRecipe;
import ianm1647.expandeddelight.common.registry.EDItems;
import ianm1647.expandeddelight.integration.jei.JEIRecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableAnimated.StartDirection;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

/**
 * The JEI juicing category. Upstream 0.1.4 draws it on textures/gui/jei/juicer.png, a sheet built from
 * Farmer's Respite's kettle that this build does not ship. This port cuts the background out of the
 * author's juicer sheet of the 1.20 branch, textures/gui/juicer_gui.png, so the slots sit where the
 * juicer screen has them, shifted by the cut-out's corner (38, 6).
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JuicingRecipeCategory implements IRecipeCategory<JuicerRecipe> {
   private static final int U = 38;
   private static final int V = 6;
   protected final IDrawableAnimated arrow;
   private final Component title = Component.translatable("expandeddelight.jei.juicing");
   private final IDrawable background;
   private final IDrawable icon;

   public JuicingRecipeCategory(IGuiHelper helper) {
      ResourceLocation backgroundImage = new ResourceLocation(ExpandedDelight.MODID, "textures/gui/juicer_gui.png");
      this.background = helper.createDrawable(backgroundImage, U, V, 101, 73);
      this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(EDItems.JUICER.get()));
      this.arrow = helper.drawableBuilder(backgroundImage, 177, 0, 22, 17).buildAnimated(200, StartDirection.LEFT, false);
   }

   @Override
   public RecipeType<JuicerRecipe> getRecipeType() {
      return JEIRecipeTypes.JUICING;
   }

   @Override
   public Component getTitle() {
      return this.title;
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
   public void setRecipe(IRecipeLayoutBuilder builder, JuicerRecipe recipe, IFocusGroup focusGroup) {
      NonNullList<Ingredient> recipeIngredients = recipe.getIngredients();
      RegistryAccess access = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.registryAccess() : RegistryAccess.EMPTY;
      ItemStack resultStack = recipe.getResultItem(access);
      ItemStack containerStack = recipe.getOutputContainer();
      int borderSlotSize = 18;

      for (int row = 0; row < JuicerRecipe.INPUT_SLOTS; ++row) {
         if (row < recipeIngredients.size()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 48 - U, 25 - V + row * borderSlotSize)
               .addItemStacks(Arrays.asList(recipeIngredients.get(row).getItems()));
         }
      }
      builder.addSlot(RecipeIngredientRole.OUTPUT, 116 - U, 35 - V).addItemStack(resultStack);
      if (!containerStack.isEmpty()) {
         builder.addSlot(RecipeIngredientRole.CATALYST, 80 - U, 60 - V).addItemStack(containerStack);
      }
      builder.addSlot(RecipeIngredientRole.OUTPUT, 116 - U, 60 - V).addItemStack(resultStack);
   }

   @Override
   public void draw(JuicerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
      this.arrow.draw(guiGraphics, 79 - U, 35 - V);
   }
}
