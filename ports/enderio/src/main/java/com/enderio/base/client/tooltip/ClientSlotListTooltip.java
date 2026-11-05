package com.enderio.base.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * A grid of slots in the style of the bundle tooltip, from AbstractClientSlotListTooltip of Ender IO 8.x (824d70feb).
 * 1.20.1 has no GUI sprites, so the slots and the border come from the bundle texture like in ClientBundleTooltip.
 */
public abstract class ClientSlotListTooltip implements ClientTooltipComponent {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/bundle.png");
    private static final int TEXTURE_SIZE = 128;
    private static final int SLOT_SIZE_X = 18;
    private static final int SLOT_SIZE_Y = 20;

    private final int maxSlotWidth;

    protected ClientSlotListTooltip() {
        this(5);
    }

    protected ClientSlotListTooltip(int maxSlotWidth) {
        this.maxSlotWidth = maxSlotWidth;
    }

    protected abstract int slotCount();

    protected abstract void renderSlotContent(int x, int y, int slotIndex, GuiGraphics guiGraphics, Font font);

    @Override
    public int getHeight() {
        return slotCount() <= 0 ? 0 : gridSizeY() * SLOT_SIZE_Y + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return slotCount() <= 0 ? 0 : gridSizeX() * SLOT_SIZE_X + 2;
    }

    private int gridSizeX() {
        return Math.max(1, Math.min(maxSlotWidth, slotCount()));
    }

    private int gridSizeY() {
        return (slotCount() + gridSizeX() - 1) / gridSizeX();
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        if (slotCount() <= 0) {
            return;
        }

        int columns = gridSizeX();
        int rows = gridSizeY();
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slotX = x + column * SLOT_SIZE_X + 1;
                int slotY = y + row * SLOT_SIZE_Y + 1;
                if (index < slotCount()) {
                    blit(guiGraphics, slotX, slotY, 0, 0, SLOT_SIZE_X, SLOT_SIZE_Y);
                    renderSlotContent(slotX, slotY, index, guiGraphics, font);
                } else {
                    // The blocked slot of the bundle texture fills the rest of the last row.
                    blit(guiGraphics, slotX, slotY, 0, 40, SLOT_SIZE_X, SLOT_SIZE_Y);
                }
                index++;
            }
        }

        drawBorder(x, y, columns, rows, guiGraphics);
    }

    private void drawBorder(int x, int y, int columns, int rows, GuiGraphics guiGraphics) {
        blit(guiGraphics, x, y, 0, 20, 1, 1);
        blit(guiGraphics, x + columns * SLOT_SIZE_X + 1, y, 0, 20, 1, 1);

        for (int column = 0; column < columns; column++) {
            blit(guiGraphics, x + 1 + column * SLOT_SIZE_X, y, 0, 20, SLOT_SIZE_X, 1);
            blit(guiGraphics, x + 1 + column * SLOT_SIZE_X, y + rows * SLOT_SIZE_Y, 0, 60, SLOT_SIZE_X, 1);
        }

        for (int row = 0; row < rows; row++) {
            blit(guiGraphics, x, y + row * SLOT_SIZE_Y + 1, 0, 18, 1, SLOT_SIZE_Y);
            blit(guiGraphics, x + columns * SLOT_SIZE_X + 1, y + row * SLOT_SIZE_Y + 1, 0, 18, 1, SLOT_SIZE_Y);
        }

        blit(guiGraphics, x, y + rows * SLOT_SIZE_Y, 0, 60, 1, 1);
        blit(guiGraphics, x + columns * SLOT_SIZE_X + 1, y + rows * SLOT_SIZE_Y, 0, 60, 1, 1);
    }

    private static void blit(GuiGraphics guiGraphics, int x, int y, int u, int v, int width, int height) {
        guiGraphics.blit(TEXTURE, x, y, 0, (float) u, (float) v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
