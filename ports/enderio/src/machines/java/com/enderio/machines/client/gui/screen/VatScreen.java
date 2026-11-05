package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.gui.widgets.EIOImageButton;
import com.enderio.core.client.gui.widgets.EnumIconWidget;
import com.enderio.machines.client.gui.widget.ActiveWidget;
import com.enderio.machines.client.gui.widget.FermentationWidget;
import com.enderio.machines.client.gui.widget.FluidStackWidget;
import com.enderio.machines.client.gui.widget.ProgressWidget;
import com.enderio.machines.client.gui.widget.ioconfig.IOConfigButton;
import com.enderio.machines.common.blockentity.VatBlockEntity;
import com.enderio.machines.common.io.fluid.MachineFluidTank;
import com.enderio.machines.common.lang.MachineLang;
import com.enderio.machines.common.menu.VatMenu;
import com.enderio.machines.common.recipe.FermentingRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Vat screen (1cb2e1420, b7b355a74). The 1.20.1 GUI has no sprite atlas, so the 8.x sprites are drawn as plain textures.
 */
public class VatScreen extends MachineScreen<VatMenu> {

    public static final ResourceLocation VAT_BG = EnderIO.loc("textures/gui/vat.png");
    private static final ResourceLocation VAT_COVER = EnderIO.loc("textures/gui/sprites/vat_cover.png");
    public static final ResourceLocation MOVE_FLUID = EnderIO.loc("textures/gui/sprites/buttons/move_fluid.png");
    public static final ResourceLocation VOID_FLUID = EnderIO.loc("textures/gui/sprites/buttons/void_fluid.png");

    @Nullable
    private FermentingRecipe recipeCache;
    @Nullable
    private ResourceLocation recipeId;

    public VatScreen(VatMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        updateRecipeCache();

        addRenderableOnly(new FluidStackWidget(this, this::wrappedInputTank, 30 + leftPos, 12 + topPos, 15, 47));
        addRenderableOnly(new FluidStackWidget(this, getMenu().getBlockEntity()::getOutputTank, 132 + leftPos, 12 + topPos, 15, 47));

        addRenderableOnly(new FermentationWidget(this::isCrafting, this::inputFluidStack, this::outputFluidStack, this::getProgress,
            76 + leftPos, 34 + topPos, 26, 28));

        addRenderableOnly(new ProgressWidget.BottomUp(this, this::getProgress, 82 + leftPos, 64 + topPos, 14, 14, 176, 0));

        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 6 - 16, topPos + 6, () -> menu.getBlockEntity().getRedstoneControl(),
            control -> menu.getBlockEntity().setRedstoneControl(control), EIOLang.REDSTONE_MODE));

        addRenderableWidget(new IOConfigButton<>(this, leftPos + imageWidth - 6 - 16, topPos + 24, 16, 16, menu, this::addRenderableWidget, font));
        addRenderableWidget(new ActiveWidget(this, menu.getBlockEntity()::getMachineStates, leftPos + imageWidth - 6 - 16, topPos + 16 * 4));

        EIOImageButton transfer = new EIOImageButton(this, leftPos + 29, topPos + 62, 16, 16, 0, 0, 0, MOVE_FLUID, 16, 16,
            press -> menu.getBlockEntity().moveFluidToOutputTank(), MachineLang.TRANSFER_TANK);
        transfer.setTooltip(Tooltip.create(MachineLang.TRANSFER_TANK));
        addRenderableWidget(transfer);

        EIOImageButton dump = new EIOImageButton(this, leftPos + 131, topPos + 62, 16, 16, 0, 0, 0, VOID_FLUID, 16, 16,
            press -> menu.getBlockEntity().dumpOutputTank(), MachineLang.DUMP_TANK);
        dump.setTooltip(Tooltip.create(MachineLang.DUMP_TANK));
        addRenderableWidget(dump);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        updateRecipeCache();
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }

    // Drawn over the fermentation widget, but below the tooltips.
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        super.renderLabels(guiGraphics, pMouseX, pMouseY);

        guiGraphics.blit(VAT_COVER, 76, 34, 0, 0, 26, 28, 26, 28);
        drawModifierStrings(guiGraphics);
    }

    @Override
    public ResourceLocation getBackgroundImage() {
        return VAT_BG;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return new Vector2i(176, 166);
    }

    private void updateRecipeCache() {
        VatBlockEntity vat = getMenu().getBlockEntity();
        if (vat == null || minecraft == null || minecraft.level == null) {
            return;
        }

        ResourceLocation id = vat.getRecipeId();
        if (!id.equals(recipeId)) {
            recipeId = id;
            recipeCache = minecraft.level.getRecipeManager().byKey(id).filter(FermentingRecipe.class::isInstance).map(FermentingRecipe.class::cast).orElse(null);
        }
    }

    private boolean isCrafting() {
        return recipeCache != null && getMenu().getBlockEntity().getCraftingProgress() > 0;
    }

    private FluidStack inputFluidStack() {
        return getMenu().getBlockEntity().getInputTank().getFluid();
    }

    private FluidStack outputFluidStack() {
        return recipeCache != null ? recipeCache.getOutput() : FluidStack.EMPTY;
    }

    private float getProgress() {
        return menu.getBlockEntity().getCraftingProgress();
    }

    /**
     * Wraps the essential parts of the input tank. Removes the amount the recipe takes, to show the fluid as used up while it ferments.
     */
    private MachineFluidTank wrappedInputTank() {
        MachineFluidTank tank = getMenu().getBlockEntity().getInputTank();
        return new MachineFluidTank(tank.getCapacity()) {
            @Override
            public @NotNull FluidStack getFluid() {
                return tank.getFluid();
            }

            @Override
            public int getFluidAmount() {
                int reduced = 0;
                if (isCrafting() && recipeCache != null) {
                    reduced = recipeCache.getInput().amount();
                }
                return Math.max(tank.getFluidAmount() - reduced, 0);
            }

            @Override
            public int getCapacity() {
                return tank.getCapacity();
            }
        };
    }

    private void drawModifierStrings(GuiGraphics guiGraphics) {
        if (!isCrafting() || recipeCache == null) {
            return;
        }

        // first modifier
        ItemStack item = getMenu().getSlot(VatMenu.INPUTS_INDEX).getItem();
        ItemStack other = getMenu().getSlot(VatMenu.INPUTS_INDEX + 1).getItem();
        boolean swapped = !(item.is(recipeCache.getFirstReagent()) && other.is(recipeCache.getSecondReagent()));

        double modifier = FermentingRecipe.getModifier(item, swapped ? recipeCache.getSecondReagent() : recipeCache.getFirstReagent());
        String text = "x" + modifier;
        int x = 63 - font.width(text) / 2;
        guiGraphics.drawString(font, text, x, 32, 4210752, false);

        // second modifier
        modifier = FermentingRecipe.getModifier(other, swapped ? recipeCache.getFirstReagent() : recipeCache.getSecondReagent());
        text = "x" + modifier;
        x = 113 - font.width(text) / 2;
        guiGraphics.drawString(font, text, x, 32, 4210752, false);
    }
}
