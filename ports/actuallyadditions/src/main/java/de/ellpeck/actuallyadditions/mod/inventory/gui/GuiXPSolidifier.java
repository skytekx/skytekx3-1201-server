/*
 * This file ("GuiXPSolidifier.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerXPSolidifier;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityXPSolidifier;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class GuiXPSolidifier extends AAScreen<ContainerXPSolidifier> {

    private static final ResourceLocation RES_LOC = AssetUtil.getGuiLocation("gui_xp_solidifier");
    private static final int[] BUTTON_X = {62, 80, 99};
    private final TileEntityXPSolidifier solidifier;

    public GuiXPSolidifier(ContainerXPSolidifier container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.solidifier = container.solidifier;
        this.imageWidth = 176;
        this.imageHeight = 93 + 86;
    }

    // SkyTekx: upstream a4cc12d9 brings the buttons back with vanilla buttons. The client predicts
    // the change of its experience, and the server checks the press (packet hardening)
    @Override
    public void init() {
        super.init();

        String[] labels = {"1", "5", "10", "20", "30", "40", "50", "64", "All"};
        for (int i = 0; i < labels.length; i++) {
            int id = i;
            this.addRenderableWidget(Button.builder(Component.literal(labels[i]), btn -> {
                PacketHelperClient.sendButtonPacket(this.solidifier, id);
                this.solidifier.onButtonPressed(id, this.minecraft.player);
            }).bounds(this.leftPos + BUTTON_X[i % 3], this.topPos + 44 + (i / 3) * 17, 16, 16).build());
        }
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, float f, int x, int y) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        guiGraphics.blit(AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86);

        guiGraphics.blit(RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93);

        guiGraphics.drawCenteredString(this.font, Integer.toString(this.solidifier.amount), this.leftPos + 88, this.topPos + 34, 0xFFFFFF);
    }
}
