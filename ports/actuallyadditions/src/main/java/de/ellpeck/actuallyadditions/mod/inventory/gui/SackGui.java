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
import de.ellpeck.actuallyadditions.mod.inventory.SackContainer;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// SkyTekx: the screen of the Traveler's Sack of main (85725a0f, 79c8bcfb, 38f48247, 87601c1e). The
// Void Sack has its own VoidSackGui
@OnlyIn(Dist.CLIENT)
public class SackGui extends AAScreen<SackContainer> {
    private static final ResourceLocation RES_LOC = AssetUtil.getGuiLocation("gui_bag");

    private final SackContainer container;
    private FilterSettingsGui filter;
    private Button buttonAutoInsert;

    public SackGui(SackContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 90 + 86;
        this.container = container;
    }

    @Override
    public void init() {
        super.init();

        this.filter = new FilterSettingsGui(this.container.getFilter(), this.leftPos + 137, this.topPos + 10, true, this::addRenderableWidget, this::buttonClicked, 1);

        this.buttonAutoInsert = Button.builder(autoInsertLabel(this.container.autoInsert),
                (button) -> {
                    this.container.autoInsert = !this.container.autoInsert;
                    this.buttonAutoInsert.setMessage(autoInsertLabel(this.container.autoInsert));
                    this.buttonClicked(0);
                }).pos(this.leftPos - 21, this.topPos + 8).size(20, 20)
                .build();

        this.addRenderableWidget(this.buttonAutoInsert);
    }

    // The label of main's pickup button, I when on, O when off. Main shows I before the first click
    static Component autoInsertLabel(boolean autoInsert) {
        return Component.literal(autoInsert ? "I" : "O").withStyle(autoInsert ? ChatFormatting.DARK_GREEN : ChatFormatting.RED);
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
            renderAutoInsertTooltip(guiGraphics, this.font, mouseX, mouseY, this.container.autoInsert, "sack");
        }
    }

    // The pickup tooltip of main, wrapped at 200 pixels as main's TODO asks. Main says sack in this
    // screen and bag in the one of the Void Sack. SkyTekx: main writes the lines as English literals,
    // here they are lang keys, info.actuallyadditions.gui.autoInsert.* with the noun sack or bag
    static void renderAutoInsertTooltip(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, boolean autoInsert, String noun) {
        List<Component> text = new ArrayList<>();
        text.add(Component.translatable("info.actuallyadditions.gui.autoInsert." + (autoInsert
            ? "on"
            : "off")).withStyle(ChatFormatting.BOLD));
        text.add(Component.translatable("info.actuallyadditions.gui.autoInsert.desc." + noun));
        text.add(Component.translatable("info.actuallyadditions.gui.autoInsert.note." + noun).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        guiGraphics.renderTooltip(font, FilterSettingsGui.wrap(font, text), mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        guiGraphics.blit(AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 90, 0, 0, 176, 86);

        guiGraphics.blit(RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 90);
    }
}
