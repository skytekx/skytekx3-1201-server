/*
 * This file ("GuiBag.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.actuallyadditions.mod.inventory.VoidSackContainer;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

// SkyTekx: the screen of the Void Sack of main (87601c1e), with the filter buttons in a row above
// the four filter slots
@OnlyIn(Dist.CLIENT)
public class VoidSackGui extends AAScreen<VoidSackContainer> {
    private static final ResourceLocation RES_LOC_VOID = AssetUtil.getGuiLocation("gui_void_bag");

    private final VoidSackContainer container;
    private FilterSettingsGui filter;
    private Button buttonAutoInsert;

    public VoidSackGui(VoidSackContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 90 + 43;
        this.container = container;
    }

    @Override
    public void init() {
        super.init();

        this.filter = new FilterSettingsGui(this.container.filter, this.leftPos + 97, this.topPos + 3, false, this::addRenderableWidget, this::buttonClicked, 1);

        this.buttonAutoInsert = Button.builder(SackGui.autoInsertLabel(this.container.autoInsert),
                (button) -> {
                    this.container.autoInsert = !this.container.autoInsert;
                    this.buttonAutoInsert.setMessage(SackGui.autoInsertLabel(this.container.autoInsert));
                    this.buttonClicked(0);
                }).pos(this.leftPos - 17, this.topPos + 16).size(20, 20)
                .build();

        this.addRenderableWidget(this.buttonAutoInsert);
    }

    public void buttonClicked(int id) {
        PacketHelperClient.sendContainerButtonPacket(id);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.filter.tick();
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.filter.drawHover(guiGraphics, mouseX, mouseY);

        if (this.buttonAutoInsert.isMouseOver(mouseX, mouseY)) {
            SackGui.renderAutoInsertTooltip(guiGraphics, this.font, mouseX, mouseY, this.container.autoInsert, "bag");
        }
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        guiGraphics.blit(AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 43, 0, 0, 176, 86);

        guiGraphics.blit(RES_LOC_VOID, this.leftPos, this.topPos, 0, 0, 176, 43);
    }
}
