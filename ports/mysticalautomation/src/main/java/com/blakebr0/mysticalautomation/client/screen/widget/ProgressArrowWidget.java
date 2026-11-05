package com.blakebr0.mysticalautomation.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntSupplier;

/**
 * SkyTekx port: Cucumber 8's {@code com.blakebr0.cucumber.client.screen.widget.ProgressArrowWidget} (Cucumber, MIT
 * License, Copyright (c) 2018 BlakeBr0), which Cucumber 7 lacks.
 * <p>
 * Cucumber 8 draws the arrow from its widgets texture, whose arrow sprites are copies of the vanilla furnace sprites.
 * Cucumber 7's widgets texture has no arrow, and the GUI backgrounds of this mod already show the empty arrow, so this
 * widget draws only the progress, straight from the vanilla furnace texture.
 */
public class ProgressArrowWidget extends AbstractWidget {
    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    private final IntSupplier progress;
    private final IntSupplier total;

    public ProgressArrowWidget(int x, int y, IntSupplier progress, IntSupplier total) {
        super(x, y, 24, 16, Component.literal("Progress Arrow"));
        this.progress = progress;
        this.total = total;
        this.active = false; // not a clickable element
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int offset = this.getProgressScaled();

        if (offset > 0) {
            gfx.blit(FURNACE_TEXTURE, this.getX(), this.getY(), 176, 14, Math.min(offset + 1, this.width), this.height);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    private int getProgressScaled() {
        int i = this.progress.getAsInt();
        int j = this.total.getAsInt();
        return j != 0 && i != 0 ? i * this.width / j : 0;
    }
}
