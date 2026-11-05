package com.blakebr0.mysticalautomation.client.screen.widget;

import com.blakebr0.cucumber.util.Formatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntSupplier;

/**
 * SkyTekx port: Cucumber 8's {@code com.blakebr0.cucumber.client.screen.widget.FuelWidget} (Cucumber, MIT License,
 * Copyright (c) 2018 BlakeBr0), which Cucumber 7 lacks.
 * <p>
 * Cucumber 8 draws the flame from its widgets texture, whose flame sprites are copies of the vanilla furnace sprites.
 * Cucumber 7's widgets texture has no flame, and the GUI backgrounds of this mod already show the unlit flame, so this
 * widget draws only the burning part, straight from the vanilla furnace texture.
 */
public class FuelWidget extends AbstractWidget {
    private static final ResourceLocation FURNACE_TEXTURE = new ResourceLocation("textures/gui/container/furnace.png");

    private final IntSupplier fuelValue;
    private final IntSupplier fuelLeft;

    public FuelWidget(int x, int y, IntSupplier fuelValue, IntSupplier fuelLeft) {
        super(x, y, 14, 14, Component.literal("Fuel"));
        this.fuelValue = fuelValue;
        this.fuelLeft = fuelLeft;
        this.active = false; // not a clickable element
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int offset = this.getBurnLeftScaled();

        if (offset > 0) {
            gfx.blit(FURNACE_TEXTURE, this.getX(), this.getY() + this.height - offset, 176, this.height - offset, this.width, offset);
        }

        if (mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height) {
            var font = Minecraft.getInstance().font;
            var text = Formatting.energy(this.fuelLeft.getAsInt());

            gfx.renderTooltip(font, text, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    private int getBurnLeftScaled() {
        int i = this.fuelLeft.getAsInt();
        int j = this.fuelValue.getAsInt();
        return (int) (j != 0 && i != 0 ? Math.min((long) i * this.height / j, this.height) : 0);
    }
}
