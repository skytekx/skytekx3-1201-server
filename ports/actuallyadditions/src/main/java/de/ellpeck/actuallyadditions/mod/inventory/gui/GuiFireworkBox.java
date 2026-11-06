/*
 * This file ("GuiFireworkBox.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import de.ellpeck.actuallyadditions.mod.inventory.ContainerFireworkBox;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityFireworkBox;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * SkyTekx: the settings screen of upstream fd92ee85 and 5c13128a on the 1.20.1 widgets. The ranges
 * are the ones the box clamps every number to on the server (TileEntityFireworkBox). A slider sends
 * its value when the mouse lets go of it or a key moves it, not on every step of a drag, and an
 * integer slider sends the whole number it shows
 */
@OnlyIn(Dist.CLIENT)
public class GuiFireworkBox extends AAScreen<ContainerFireworkBox> {

    private static final ResourceLocation RES_LOC = AssetUtil.getGuiLocation("gui_firework_box");

    @Nullable
    protected final TileEntityFireworkBox tile;

    public GuiFireworkBox(ContainerFireworkBox screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.tile = screenContainer.fireworkbox;
        this.imageWidth = 302;
        this.imageHeight = 130;
    }

    @Override
    public void init() {
        super.init();
        if (this.tile == null) {
            return;
        }

        DecimalFormat intFormatter = new DecimalFormat("0");
        int left = this.leftPos + 2;
        this.addRenderableWidget(new CustomSlider(left, this.topPos + 2, Component.literal("Value Play"), 0F, 5F, this.tile.intValuePlay, intFormatter, (slider) -> this.send(slider, 0)));
        this.addRenderableWidget(new CustomSlider(left, this.topPos + 23, Component.literal("Average Charge Amount"), 1F, 4F, this.tile.chargeAmount, intFormatter, (slider) -> this.send(slider, 1)));
        this.addRenderableWidget(new CustomSlider(left, this.topPos + 44, Component.literal("Average Flight Time"), 1F, 3F, this.tile.flightTime, intFormatter, (slider) -> this.send(slider, 2)));
        this.addRenderableWidget(new CustomSlider(left, this.topPos + 65, Component.literal("Effect Chance"), 0F, 1F, this.tile.trailOrFlickerChance, null, (slider) -> this.send(slider, 3)));
        this.addRenderableWidget(new CustomSlider(left, this.topPos + 86, Component.literal("Flicker/Trail Ratio"), 0F, 1F, this.tile.flickerChance, null, (slider) -> this.send(slider, 4)));
        this.addRenderableWidget(new CustomSlider(left, this.topPos + 107, Component.literal("Color Amount"), 1, 6, this.tile.colorAmount, intFormatter, (slider) -> this.send(slider, 5)));

        this.addRenderableWidget(new CustomSlider(left + 150, this.topPos + 2, Component.literal("Small Ball"), 0F, 1F, this.tile.typeChance0, null, (slider) -> this.send(slider, 6)));
        this.addRenderableWidget(new CustomSlider(left + 150, this.topPos + 23, Component.literal("Large Ball"), 0F, 1F, this.tile.typeChance1, null, (slider) -> this.send(slider, 7)));
        this.addRenderableWidget(new CustomSlider(left + 150, this.topPos + 44, Component.literal("Star Shape"), 0F, 1F, this.tile.typeChance2, null, (slider) -> this.send(slider, 8)));
        this.addRenderableWidget(new CustomSlider(left + 150, this.topPos + 65, Component.literal("Creeper Shape"), 0F, 1F, this.tile.typeChance3, null, (slider) -> this.send(slider, 9)));
        this.addRenderableWidget(new CustomSlider(left + 150, this.topPos + 86, Component.literal("Burst"), 0F, 1F, this.tile.typeChance4, null, (slider) -> this.send(slider, 10)));

        this.addRenderableWidget(new CustomSlider(left + 150, this.topPos + 107, Component.literal("Area of Effect"), 0, 4, this.tile.areaOfEffect, intFormatter, (slider) -> this.send(slider, 11)));
    }

    private void send(CustomSlider slider, int id) {
        if (this.tile != null) {
            PacketHelperClient.sendNumberPacket(this.tile, slider.getValue(), id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        guiGraphics.blit(RES_LOC, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 512, 512);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.getFocused() != null && this.isDragging() && pButton == 0) {
            return this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    // SkyTekx: a drag that ends outside of the slider gets no onRelease, so the screen ends it
    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 && this.getFocused() instanceof CustomSlider slider) {
            slider.finishDrag();
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    private static class CustomSlider extends AbstractSliderButton {
        private final double min;
        private final double max;
        @Nullable
        private final DecimalFormat format;
        private final Component sliderName;
        private final NumberFormat defaultFormat = new DecimalFormat("0.00000000");
        private final OnApply onApply;
        private boolean held;
        private boolean changedWhileHeld;

        public CustomSlider(int x, int y, Component message, double min, double max, double defaultValue, @Nullable DecimalFormat format, OnApply onApply) {
            super(x, y, 148, 20, message, 0);
            this.sliderName = message;
            this.min = min;
            this.max = max;
            this.format = format;
            this.value = (Mth.clamp(defaultValue, min, max) - min) / (max - min);
            this.onApply = onApply;

            this.updateMessage();
        }

        public double getValue() {
            double value = this.value * (this.max - this.min) + this.min;
            return this.format != null ? Math.round(value) : value;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.held = true;
            super.onClick(mouseX, mouseY);
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            this.finishDrag();
        }

        public void finishDrag() {
            if (this.held && this.changedWhileHeld) {
                this.onApply.onApply(this);
            }
            this.held = false;
            this.changedWhileHeld = false;
        }

        public String getValueString() {
            if (this.format == null) {
                if (this.getValue() == (int) this.getValue()) {
                    return String.valueOf((double) ((int) this.getValue()));
                } else {
                    return this.defaultFormat.format(this.getValue());
                }
            } else {
                return this.format.format(this.getValue());
            }
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.sliderName.copy().append(": ").append(this.getValueString()));
        }

        @Override
        protected void applyValue() {
            if (this.held) {
                this.changedWhileHeld = true;
            } else {
                this.onApply.onApply(this);
            }
        }

        public interface OnApply {
            void onApply(CustomSlider slider);
        }
    }
}
