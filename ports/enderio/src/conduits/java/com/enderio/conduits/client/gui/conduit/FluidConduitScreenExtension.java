package com.enderio.conduits.client.gui.conduit;

import com.enderio.EnderIO;
import com.enderio.api.conduit.screen.ConduitScreenExtension;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.conduits.common.conduit.type.fluid.FluidConduitData;
import com.enderio.core.client.gui.widgets.MultiIconButton;
import com.enderio.core.common.util.TooltipUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class FluidConduitScreenExtension implements ConduitScreenExtension<FluidConduitData> {

    public static final FluidConduitScreenExtension INSTANCE = new FluidConduitScreenExtension(false);

    /**
     * For the fluid conduits with insert priorities, the pressurized and ender conduits of Ender IO 8.x (6ecc1c788).
     */
    public static final FluidConduitScreenExtension PRIORITY_INSTANCE = new FluidConduitScreenExtension(true);

    private static final ResourceLocation WIDGET_TEXTURE = EnderIO.loc("textures/gui/fluidbackground.png");

    private final boolean showPriority;

    private FluidConduitScreenExtension(boolean showPriority) {
        this.showPriority = showPriority;
    }

    @Override
    public List<AbstractWidget> createWidgets(Screen screen, Supplier<FluidConduitData> conduitDataSupplier, UpdateExtendedData<FluidConduitData> updateConduitData, Supplier<Direction> direction, Vector2i widgetsStart) {
        List<AbstractWidget> widgets = new ArrayList<>();
        if (!conduitDataSupplier.get().isMultiFluid) {
            widgets.add(new FluidWidget(widgetsStart.add(0, 20),
                () -> conduitDataSupplier.get().lockedFluid(),
                () -> {
                    updateConduitData.update(data -> {
                        data.setShouldReset(true);
                        return data;
                    });
                    // The reset request is one shot. The update runs on the client's live data of the bundle, and the packet
                    // has already serialized it, so clear the flag again. Server syncs never send ShouldReset, so without
                    // this every later screen update of the conduit would ask for another reset.
                    conduitDataSupplier.get().setShouldReset(false);
                }
            ));
        }

        if (showPriority) {
            // The plus and minus buttons of the item conduit priority (6.x 37890e67d). The conduit screen draws the value.
            // A priority click never asks for a fluid reset, whatever the client's data still holds.
            widgets.add(MultiIconButton.createAddButton(
                widgetsStart.add(70, 4 + 4 + 8 + 16 + 8),
                () -> conduitDataSupplier.get().getPriority(direction.get()),
                priority -> updateConduitData.update(data -> {
                    data.setShouldReset(false);
                    data.compute(direction.get()).setPriority(priority);
                    return data;
                })));
            widgets.add(MultiIconButton.createMinusButton(
                widgetsStart.add(70, 4 + 4 + 8 + 16 + 16),
                () -> conduitDataSupplier.get().getPriority(direction.get()),
                priority -> updateConduitData.update(data -> {
                    data.setShouldReset(false);
                    data.compute(direction.get()).setPriority(priority);
                    return data;
                })));
        }

        return widgets;
    }

    private static class FluidWidget extends AbstractWidget {
        private final Runnable onPress;
        private final Supplier<Fluid> currentFluid;

        FluidWidget(Vector2i pos, Supplier<Fluid> fluid, Runnable onPress) {
            super(pos.x(), pos.y(), 14, 14, Component.empty());
            this.onPress = onPress;
            this.currentFluid = fluid;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            if (isHoveredOrFocused()) {
                MutableComponent tooltip = EIOLang.FLUID_CONDUIT_CHANGE_FLUID1.copy();
                tooltip.append("\n").append(EIOLang.FLUID_CONDUIT_CHANGE_FLUID2);
                if (currentFluid.get() != null) {
                    tooltip.append("\n").append(TooltipUtil.withArgs(EIOLang.FLUID_CONDUIT_CHANGE_FLUID3, currentFluid.get().getFluidType().getDescription()));
                }
                setTooltip(Tooltip.create(TooltipUtil.style(tooltip)));
            }

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            guiGraphics.blit(WIDGET_TEXTURE, getX(), getY(), 0, 0, this.width, this.height);
            if (currentFluid.get() == null) {
                return;
            }

            IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(currentFluid.get());
            ResourceLocation still = props.getStillTexture();
            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
            if (texture instanceof TextureAtlas atlas) {
                TextureAtlasSprite sprite = atlas.getSprite(still);

                int color = props.getTintColor();
                RenderSystem.setShaderColor(
                    FastColor.ARGB32.red(color) / 255.0F,
                    FastColor.ARGB32.green(color) / 255.0F,
                    FastColor.ARGB32.blue(color) / 255.0F,
                    FastColor.ARGB32.alpha(color) / 255.0F);
                RenderSystem.enableBlend();


                int atlasWidth = (int)(sprite.contents().width() / (sprite.getU1() - sprite.getU0()));
                int atlasHeight = (int)(sprite.contents().height() / (sprite.getV1() - sprite.getV0()));

                guiGraphics.blit(TextureAtlas.LOCATION_BLOCKS, getX() + 1, getY() + 1, 0, sprite.getU0()*atlasWidth, sprite.getV0()*atlasHeight, 12, 12, atlasWidth, atlasHeight);

                RenderSystem.setShaderColor(1, 1, 1, 1);
            }

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        }

        @Override
        public void onClick(double pMouseX, double pMouseY) {
            onPress.run();
        }
    }
}
