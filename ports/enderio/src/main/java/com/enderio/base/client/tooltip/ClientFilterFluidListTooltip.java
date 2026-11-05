package com.enderio.base.client.tooltip;

import com.enderio.base.common.item.filter.FilterFluidListTooltip;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

/**
 * Draws the fluids of a fluid filter with their still texture and tint, from ClientEnderFluidFilterTooltip of
 * Ender IO 8.x (824d70feb).
 */
public class ClientFilterFluidListTooltip extends ClientSlotListTooltip {

    private final FilterFluidListTooltip tooltip;

    public ClientFilterFluidListTooltip(FilterFluidListTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    protected int slotCount() {
        return tooltip.fluids().size();
    }

    @Override
    protected void renderSlotContent(int x, int y, int slotIndex, GuiGraphics guiGraphics, Font font) {
        FluidStack fluidStack = tooltip.fluids().get(slotIndex);
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation still = props.getStillTexture(fluidStack);
        if (still == null) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(still);
        int color = props.getTintColor(fluidStack);
        RenderSystem.enableBlend();
        guiGraphics.blit(x + 1, y + 1, 0, 16, 16, sprite,
            FastColor.ARGB32.red(color) / 255.0F, FastColor.ARGB32.green(color) / 255.0F,
            FastColor.ARGB32.blue(color) / 255.0F, FastColor.ARGB32.alpha(color) / 255.0F);
        RenderSystem.disableBlend();
    }
}
