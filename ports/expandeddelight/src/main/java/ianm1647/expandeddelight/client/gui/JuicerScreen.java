package ianm1647.expandeddelight.client.gui;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.EDConfig;
import ianm1647.expandeddelight.common.block.entity.container.JuicerMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The juicer screen in the 1.20.1 shape of Farmer's Delight's CookingPotScreen (MIT). Upstream 0.1.4 draws
 * on a sheet built from Farmer's Respite's kettle, which this build does not ship. It draws on the author's
 * own juicer sheet of the 1.20 branch instead, whose progress arrow sits at (79, 35) and is 22 pixels long.
 */
@ParametersAreNonnullByDefault
public class JuicerScreen extends AbstractContainerScreen<JuicerMenu> implements RecipeUpdateListener {
    private static final ResourceLocation RECIPE_BUTTON_LOCATION = new ResourceLocation("textures/gui/recipe_button.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(ExpandedDelight.MODID, "textures/gui/juicer_gui.png");
    public static final Rectangle PROGRESS_ARROW = new Rectangle(79, 35, 22, 17);
    private static final int ARROW_U = 177;
    private static final int ARROW_V = 0;
    private final JuicerRecipeBookComponent recipeBookComponent = new JuicerRecipeBookComponent();
    private boolean widthTooNarrow;

    public JuicerScreen(JuicerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        if (EDConfig.ENABLE_RECIPE_BOOK_JUICER.get()) {
            this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_LOCATION, (button) -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                ((ImageButton) button).setPosition(this.leftPos + 5, this.height / 2 - 49);
            }));
        } else {
            this.recipeBookComponent.hide();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
        }

        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.addWidget(this.recipeBookComponent);
        this.setInitialFocus(this.recipeBookComponent);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.recipeBookComponent.tick();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);

        if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
            this.renderBg(gui, partialTicks, mouseX, mouseY);
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
            super.render(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.renderGhostRecipe(gui, this.leftPos, this.topPos, false, partialTicks);
        }

        this.renderMealDisplayTooltip(gui, mouseX, mouseY);
        this.recipeBookComponent.renderTooltip(gui, this.leftPos, this.topPos, mouseX, mouseY);
    }

    protected void renderMealDisplayTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        if (this.minecraft != null && this.minecraft.player != null && this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.hoveredSlot.index == JuicerMenu.INDEX_DRINK) {
                List<Component> tooltip = new ArrayList<>();
                ItemStack drinkStack = this.hoveredSlot.getItem();
                tooltip.add(((MutableComponent)drinkStack.getItem().getDescription()).withStyle(drinkStack.getRarity().getStyleModifier()));
                ItemStack containerStack = this.menu.tileEntity.getContainer();
                String container = !containerStack.isEmpty() ? containerStack.getItem().getDescription().getString() : "";
                tooltip.add(Component.translatable("container.farmersdelight.cooking_pot.served_on", container).withStyle(ChatFormatting.GRAY));
                gui.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            } else {
                gui.renderTooltip(this.font, this.hoveredSlot.getItem(), mouseX, mouseY);
            }
        }

    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            gui.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
            int l = this.menu.getJuiceProgressionScaled();
            if (l > 0) {
                gui.blit(BACKGROUND_TEXTURE, this.leftPos + PROGRESS_ARROW.x, this.topPos + PROGRESS_ARROW.y, ARROW_U, ARROW_V, l, PROGRESS_ARROW.height);
            }
        }
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, buttonId)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.recipeBookComponent.isVisible() || super.mouseClicked(mouseX, mouseY, buttonId);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y, int buttonIdx) {
        boolean flag = mouseX < (double)x || mouseY < (double)y || mouseX >= (double)(x + this.imageWidth) || mouseY >= (double)(y + this.imageHeight);
        return flag && this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, buttonIdx);
    }

    @Override
    protected void slotClicked(Slot slot, int mouseX, int mouseY, ClickType clickType) {
        super.slotClicked(slot, mouseX, mouseY, clickType);
        this.recipeBookComponent.slotClicked(slot);
    }

    @Override
    public void recipesUpdated() {
        this.recipeBookComponent.recipesUpdated();
    }

    @Override
    @Nonnull
    public RecipeBookComponent getRecipeBookComponent() {
        return this.recipeBookComponent;
    }
}
