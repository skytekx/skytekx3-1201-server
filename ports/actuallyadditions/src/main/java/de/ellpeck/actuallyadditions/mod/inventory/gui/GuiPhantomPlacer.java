/*
 * This file ("GuiPhantomPlacer.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerPhantomPlacer;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomPlacer;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GuiPhantomPlacer extends AAScreen<ContainerPhantomPlacer> {

    private static final ResourceLocation RES_LOC = AssetUtil.getGuiLocation("gui_breaker");
    private final TileEntityPhantomPlacer placer;
    private Button buttonSide;

    public GuiPhantomPlacer(ContainerPhantomPlacer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.placer = container.placer;
        this.imageWidth = 176;
        this.imageHeight = 93 + 86;
    }

    @Override
    public void init() {
        super.init();

        // SkyTekx: the side button of ccfb8d16, the base had it commented out
        if (!this.placer.isBreaker) {
            this.buttonSide = Button.builder(this.getSide(), (button) -> PacketHelperClient.sendButtonPacket(this.placer, 0))
                    .bounds(this.leftPos + 63, this.topPos + 75, 50, 20).build();
            this.addRenderableWidget(this.buttonSide);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (!this.placer.isBreaker && this.buttonSide != null) {
            this.buttonSide.setMessage(this.getSide());
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (!this.placer.isBreaker && this.buttonSide != null && this.buttonSide.isMouseOver(mouseX, mouseY)) {
            String loc = "info.actuallyadditions.placer.sides";

            List<FormattedCharSequence> textList = new ArrayList<>();
            textList.add(Component.translatable(loc + ".1").withStyle(ChatFormatting.GOLD).getVisualOrderText());
            textList.addAll(this.font.split(Component.translatable(loc + ".2"), 200));
            guiGraphics.renderTooltip(this.font, textList, mouseX, mouseY);
        }
    }

    private Component getSide() {
        int index = this.placer.side + 1;
        return index >= 0 && index < SIDES.length
            ? SIDES[index]
            : SIDES[0];
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, float f, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86);

        guiGraphics.blit(RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93);
    }
}
