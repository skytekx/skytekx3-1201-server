package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.gui.widgets.EnumIconWidget;
import com.enderio.core.client.gui.widgets.MultiIconButton;
import com.enderio.core.client.gui.widgets.ToggleImageButton;
import com.enderio.core.common.util.TooltipUtil;
import com.enderio.machines.client.gui.widget.ActiveWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.common.blockentity.base.ObeliskBlockEntity;
import com.enderio.machines.common.lang.MachineLang;
import com.enderio.machines.common.menu.MachineMenu;
import com.enderio.machines.common.menu.MachineSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * SkyTekx: the screen of the backported obelisks. It keeps the layout of the 6.x AversionObeliskScreen (energy, redstone
 * control, range visibility and range buttons) and adds the upkeep line and the faded soul filter in an empty filter
 * slot of the 8.x ObeliskScreen (461cf0511).
 */
public abstract class ObeliskScreen<B extends ObeliskBlockEntity<B>, M extends MachineMenu<B>> extends MachineScreen<M> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    // The menus of the backported obelisks add the capacitor slot first and the filter slot second.
    private static final int FILTER_MENU_SLOT = 1;

    private static final ResourceLocation RANGE_BUTTON_TEXTURE = EnderIO.loc("textures/gui/icons/range_buttons.png");

    private final ResourceLocation background;

    protected ObeliskScreen(M menu, Inventory playerInventory, Component title, ResourceLocation background) {
        super(menu, playerInventory, title);
        this.background = background;

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        B blockEntity = menu.getBlockEntity();

        addRenderableOnly(new CapacitorEnergyWidget(this, blockEntity::getEnergyStorage, blockEntity::isCapacitorInstalled, 16 + leftPos, 14 + topPos, 9, 42));

        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 6 - 16, topPos + 6, () -> menu.getBlockEntity().getRedstoneControl(),
            control -> menu.getBlockEntity().setRedstoneControl(control), EIOLang.REDSTONE_MODE));

        addRenderableWidget(new ToggleImageButton<>(this, leftPos + imageWidth - 6 - 16, topPos + 2*16 + 2, 16, 16, 0, 0, 16, 0, RANGE_BUTTON_TEXTURE,
            () -> menu.getBlockEntity().isRangeVisible(), state -> menu.getBlockEntity().setIsRangeVisible(state),
            () -> menu.getBlockEntity().isRangeVisible() ? EIOLang.HIDE_RANGE : EIOLang.SHOW_RANGE));

        addRenderableWidget(MultiIconButton.createAddButton(
            new Vector2i(leftPos + imageWidth - 2*16, topPos + 2 + 16*2),
            () -> menu.getBlockEntity().getRange(),
            range -> menu.getBlockEntity().increaseRange()));
        addRenderableWidget(MultiIconButton.createMinusButton(
            new Vector2i(leftPos + imageWidth - 2*16, topPos + 2 + 16*2 + 8),
            () -> menu.getBlockEntity().getRange(),
            range -> menu.getBlockEntity().decreaseRange()));

        addRenderableWidget(new ActiveWidget(this, menu.getBlockEntity()::getMachineStates, leftPos + imageWidth - 6 - 16, topPos + 16*4));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        String range = getMenu().getBlockEntity().getRange() + "";
        guiGraphics.drawString(font, range, leftPos + imageWidth - 8 - 16 - font.width(range) - 10, topPos + 16*2 + 6, 0, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        guiGraphics.drawString(font, EIOLang.RANGE, imageWidth - 6 - font.width(EIOLang.RANGE), 16 + 8, 4210752, false);

        Component upkeep = TooltipUtil.withArgs(MachineLang.OBELISK_UPKEEP, getMenu().getBlockEntity().getPerTickEnergyCost());
        guiGraphics.drawString(font, upkeep, imageWidth / 2 - font.width(upkeep) / 2, 62, 4210752, false);

        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.index == FILTER_MENU_SLOT && slot instanceof MachineSlot && !slot.hasItem() && getMenu().getBlockEntity().requiresFilter()) {
            RenderSystem.setShaderColor(1, 1, 1, 100 / 255f);
            RenderSystem.enableBlend();
            guiGraphics.renderFakeItem(new ItemStack(EIOItems.ENTITY_FILTER.get()), slot.x, slot.y);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            return;
        }

        super.renderSlot(guiGraphics, slot);
    }

    @Override
    public ResourceLocation getBackgroundImage() {
        return background;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return new Vector2i(WIDTH, HEIGHT);
    }
}
