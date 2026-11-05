package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.gui.widgets.EnumIconWidget;
import com.enderio.core.client.gui.widgets.MultiIconButton;
import com.enderio.core.client.gui.widgets.ToggleImageButton;
import com.enderio.machines.client.gui.widget.ActiveWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.client.gui.widget.ioconfig.IOConfigButton;
import com.enderio.machines.common.blockentity.WirelessChargerBlockEntity;
import com.enderio.machines.common.menu.WirelessChargerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * SkyTekx: the Wireless Charger screen of 8.x (4ad73fff0) with the 6.x widgets: energy, redstone control, IO config,
 * range visibility and range buttons, and the max range in the middle.
 */
public class WirelessChargerScreen extends MachineScreen<WirelessChargerMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/wireless_charger.png");
    private static final ResourceLocation RANGE_BUTTON_TEXTURE = EnderIO.loc("textures/gui/icons/range_buttons.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int RANGE_OFFSET = 50;

    public WirelessChargerScreen(WirelessChargerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        WirelessChargerBlockEntity blockEntity = menu.getBlockEntity();

        addRenderableOnly(new CapacitorEnergyWidget(this, blockEntity::getEnergyStorage, blockEntity::isCapacitorInstalled, 16 + leftPos, 14 + topPos, 9, 42));

        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 6 - 16, topPos + 6, () -> menu.getBlockEntity().getRedstoneControl(),
            control -> menu.getBlockEntity().setRedstoneControl(control), EIOLang.REDSTONE_MODE));

        addRenderableWidget(new IOConfigButton<>(this, leftPos + imageWidth - 6 - 16, topPos + 24, 16, 16, menu, this::addRenderableWidget, font));

        addRenderableWidget(new ToggleImageButton<>(this, leftPos + imageWidth - 6 - 16, topPos + RANGE_OFFSET, 16, 16, 0, 0, 16, 0, RANGE_BUTTON_TEXTURE,
            () -> menu.getBlockEntity().isRangeVisible(), state -> menu.getBlockEntity().setIsRangeVisible(state),
            () -> menu.getBlockEntity().isRangeVisible() ? EIOLang.HIDE_RANGE : EIOLang.SHOW_RANGE));

        addRenderableWidget(MultiIconButton.createAddButton(
            new Vector2i(leftPos + imageWidth - 2 * 16, topPos + RANGE_OFFSET + 1),
            () -> menu.getBlockEntity().getRange(),
            range -> menu.getBlockEntity().increaseRange()));
        addRenderableWidget(MultiIconButton.createMinusButton(
            new Vector2i(leftPos + imageWidth - 2 * 16, topPos + RANGE_OFFSET + 9),
            () -> menu.getBlockEntity().getRange(),
            range -> menu.getBlockEntity().decreaseRange()));

        addRenderableWidget(new ActiveWidget(this, menu.getBlockEntity()::getMachineStates, leftPos + imageWidth - 6 - 16, topPos + RANGE_OFFSET + 16));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        String range = getMenu().getBlockEntity().getRange() + "";
        guiGraphics.drawString(font, range, leftPos + imageWidth - 8 - 16 - font.width(range) - 10, topPos + RANGE_OFFSET + 5, 0, false);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        guiGraphics.drawString(font, EIOLang.RANGE, imageWidth - 9 - font.width(EIOLang.RANGE), RANGE_OFFSET - 10, 4210752, false);

        guiGraphics.drawString(font, EIOLang.MAX_RANGE, imageWidth / 2 - font.width(EIOLang.MAX_RANGE) / 2, 20, 4210752, false);
        String maxRange = getMenu().getBlockEntity().getMaxRange() + "";
        guiGraphics.drawString(font, maxRange, imageWidth / 2 - font.width(maxRange) / 2, 20 + font.lineHeight + 3, 4210752, false);

        super.renderLabels(guiGraphics, pMouseX, pMouseY);
    }

    @Override
    public ResourceLocation getBackgroundImage() {
        return BG_TEXTURE;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return new Vector2i(WIDTH, HEIGHT);
    }
}
