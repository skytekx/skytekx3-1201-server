package com.blakebr0.mysticalautomation.client.screen;

import com.blakebr0.cucumber.client.screen.BaseContainerScreen;
import com.blakebr0.cucumber.client.screen.widget.EnergyBarWidget;
import com.blakebr0.mysticalautomation.client.screen.widget.EnergyStorageView;
import com.blakebr0.mysticalautomation.client.screen.widget.FuelWidget;
import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.container.FertilizerContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FertilizerScreen extends BaseContainerScreen<FertilizerContainer> {
    public static final ResourceLocation BACKGROUND = MysticalAutomation.resource("textures/gui/fertilizer.png");

    public FertilizerScreen(FertilizerContainer container, Inventory inv, Component title) {
        super(container, inv, title, BACKGROUND, 176, 194);
    }

    @Override
    protected void init() {
        super.init();

        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        // SkyTekx port: Cucumber 7's energy bar reads an IEnergyStorage and is clickable unless told otherwise
        var energyBar = new EnergyBarWidget(x + 7, y + 17, new EnergyStorageView(this.menu::getEnergyStored, this.menu::getMaxEnergyStored));
        energyBar.active = false;
        this.addRenderableWidget(energyBar);
        this.addRenderableWidget(new FuelWidget(x + 30, y + 39, this.menu::getFuelItemValue, this.menu::getFuelLeft));
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.title, (this.imageWidth / 2 - this.font.width(this.title) / 2), 6, 4210752, false);
        gfx.drawString(this.font, this.playerInventoryTitle, 8, (this.imageHeight - 96 + 2), 4210752, false);
    }

    // SkyTekx port: Cucumber 8's BaseContainerScreen draws the background in renderBg, Cucumber 7's in renderDefaultBg
    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        this.renderDefaultBg(gfx, partialTicks, mouseX, mouseY);
    }
}
