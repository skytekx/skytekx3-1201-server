package com.enderio.machines.common.integrations.jei.category;

import com.enderio.EnderIO;
import com.enderio.machines.client.gui.screen.VatScreen;
import com.enderio.machines.common.blockentity.VatBlockEntity;
import com.enderio.machines.common.init.MachineBlocks;
import com.enderio.machines.common.integrations.jei.util.MachineRecipeCategory;
import com.enderio.machines.common.lang.MachineLang;
import com.enderio.machines.common.recipe.FermentingRecipe;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The JEI category of the Vat (b7b355a74). The reagents fill their two slots in either order (e4645aa88).
 */
public class VATCategory extends MachineRecipeCategory<FermentingRecipe> {
    public static final RecipeType<FermentingRecipe> TYPE = RecipeType.create(EnderIO.MODID, "vat_fermenting", FermentingRecipe.class);

    // Names the slot that the recipe transfer leaves out, the fluid goes in by bucket or pipe.
    public static final String FLUID_INPUT_SLOT = "fluid_input";

    private final IDrawable background;
    private final IDrawable icon;

    public VATCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(VatScreen.VAT_BG, 28, 10, 120, 53);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(MachineBlocks.VAT.get()));
    }

    @Override
    public RecipeType<FermentingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return MachineLang.CATEGORY_VAT;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FermentingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 2)
            .addIngredients(Ingredient.of(recipe.getFirstReagent()))
            .addTooltipCallback((recipeSlotView, tooltip) -> tooltip.add(Component.literal("x"
                + FermentingRecipe.getModifier(recipeSlotView.getDisplayedItemStack().orElse(ItemStack.EMPTY), recipe.getFirstReagent()))));

        builder.addSlot(RecipeIngredientRole.INPUT, 77, 2)
            .addIngredients(Ingredient.of(recipe.getSecondReagent()))
            .addTooltipCallback((recipeSlotView, tooltip) -> tooltip.add(Component.literal("x"
                + FermentingRecipe.getModifier(recipeSlotView.getDisplayedItemStack().orElse(ItemStack.EMPTY), recipe.getSecondReagent()))));

        // One slot that cycles through the fluids of a tag, upstream adds one slot per fluid.
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 2)
            .setSlotName(FLUID_INPUT_SLOT)
            .addIngredients(ForgeTypes.FLUID_STACK, recipe.getInput().getDisplayStacks())
            .setFluidRenderer(VatBlockEntity.TANK_CAPACITY, false, 15, 47);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 2)
            .addIngredient(ForgeTypes.FLUID_STACK, recipe.getOutput().copy())
            .setFluidRenderer(VatBlockEntity.TANK_CAPACITY, false, 15, 47);
    }

    @Override
    public void draw(FermentingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // first modifier
        drawModifier(guiGraphics, font, recipeSlotsView.getSlotViews().get(0).getDisplayedItemStack().orElse(ItemStack.EMPTY), recipe.getFirstReagent(), 28);

        // second modifier
        drawModifier(guiGraphics, font, recipeSlotsView.getSlotViews().get(1).getDisplayedItemStack().orElse(ItemStack.EMPTY), recipe.getSecondReagent(), 77);
    }

    private static void drawModifier(GuiGraphics guiGraphics, Font font, ItemStack item, TagKey<Item> reagent, int slotX) {
        String text = "x" + FermentingRecipe.getModifier(item, reagent);
        int x = slotX + 8 - font.width(text) / 2;
        guiGraphics.drawString(font, text, x, 22, 4210752, false);
    }
}
