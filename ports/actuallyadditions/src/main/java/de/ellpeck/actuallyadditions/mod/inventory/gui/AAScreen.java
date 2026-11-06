/*
 * This file ("GuiWtfMojang.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;

public abstract class AAScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    // SkyTekx: the side names of ccfb8d16 as translatable components, main resolves them with I18n
    // once when the class loads
    public static final Component[] SIDES = {
        Component.translatable("info.actuallyadditions.gui.disabled"),
        Component.translatable("info.actuallyadditions.gui.up"),
        Component.translatable("info.actuallyadditions.gui.down"),
        Component.translatable("info.actuallyadditions.gui.north"),
        Component.translatable("info.actuallyadditions.gui.east"),
        Component.translatable("info.actuallyadditions.gui.south"),
        Component.translatable("info.actuallyadditions.gui.west")
    };

    public AAScreen(T container, Inventory inventory, Component pTitle) {
        super(container, inventory, pTitle);
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderLabels(@Nonnull GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.drawString(this.font, this.title, titleLabelX, titleLabelY, 0xFFFFFF, false);
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (int) (imageWidth / 2.0f - font.width(title) / 2.0f);
        titleLabelY = -10;
    }
}
