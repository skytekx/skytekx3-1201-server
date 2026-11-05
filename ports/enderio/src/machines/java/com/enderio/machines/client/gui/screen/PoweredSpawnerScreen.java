package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.client.gui.widgets.EnumIconWidget;
import com.enderio.core.client.gui.widgets.ToggleImageButton;
import com.enderio.machines.client.gui.widget.ActiveWidget;
import com.enderio.machines.client.gui.widget.CapacitorEnergyWidget;
import com.enderio.machines.client.gui.widget.ProgressWidget;
import com.enderio.machines.common.blockentity.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blockentity.PoweredSpawnerMode;
import com.enderio.machines.common.lang.MachineLang;
import com.enderio.machines.common.menu.PoweredSpawnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class PoweredSpawnerScreen extends MachineScreen<PoweredSpawnerMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/powered_spawner_spawn.png");
    // SkyTekx: the capture mode screen. The texture already shipped with 6.x and matches the slots of a90e5c512.
    public static final ResourceLocation BG_TEXTURE_CAPTURE = EnderIO.loc("textures/gui/powered_spawner_capture.png");
    private static final ResourceLocation RANGE_BUTTON_TEXTURE = EnderIO.loc("textures/gui/icons/range_buttons.png");

    private ProgressWidget spawnProgress;
    private ProgressWidget captureProgress;

    public PoweredSpawnerScreen(PoweredSpawnerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableOnly(new CapacitorEnergyWidget(this, getMenu().getBlockEntity()::getEnergyStorage, getMenu().getBlockEntity()::isCapacitorInstalled, 16 + leftPos, 14 + topPos, 9, 42));

        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 6 - 16, topPos + 6, () -> menu.getBlockEntity().getRedstoneControl(),
            control -> menu.getBlockEntity().setRedstoneControl(control), EIOLang.REDSTONE_MODE));

        addRenderableWidget(new ToggleImageButton<>(this, leftPos + imageWidth - 6 - 16, topPos + 24, 16, 16, 0, 0, 16, 0, RANGE_BUTTON_TEXTURE,
            () -> menu.getBlockEntity().isRangeVisible(), state -> menu.getBlockEntity().setIsRangeVisible(state),
            () -> menu.getBlockEntity().isRangeVisible() ? EIOLang.HIDE_RANGE : EIOLang.SHOW_RANGE));

        // SkyTekx: the mode button of 8.x (a90e5c512), below the range button like the mode button of the alloy smelter.
        addRenderableWidget(new EnumIconWidget<>(this, leftPos + imageWidth - 6 - 16, topPos + 6 + 18 * 2, () -> menu.getBlockEntity().getMode(),
            mode -> menu.getBlockEntity().setMode(mode), MachineLang.POWERED_SPAWNER_MODE));

        addRenderableWidget(new ActiveWidget(this, menu.getBlockEntity()::getMachineStates, leftPos + imageWidth - 6 - 16, topPos + 16*4));

        spawnProgress = addRenderableOnly(new ProgressWidget.BottomUp(this, () -> menu.getBlockEntity().getSpawnProgress(), getGuiLeft() + 82, getGuiTop() + 38, 14, 14, 176, 0));

        // The arrow of the capture texture, between the vial slots.
        captureProgress = addRenderableOnly(new ProgressWidget.LeftRight(this, () -> menu.getBlockEntity().getSpawnProgress(), getGuiLeft() + 75, getGuiTop() + 43, 24, 16, 176, 14));
        updateProgressVisibility();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        updateProgressVisibility();
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }

    private void updateProgressVisibility() {
        boolean capture = menu.getBlockEntity().getMode() == PoweredSpawnerMode.CAPTURE;
        spawnProgress.visible = !capture;
        captureProgress.visible = capture;
    }

    @Override
    public ResourceLocation getBackgroundImage() {
        PoweredSpawnerBlockEntity be = getMenu().getBlockEntity();
        return be.getMode() == PoweredSpawnerMode.CAPTURE ? BG_TEXTURE_CAPTURE : BG_TEXTURE;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return new Vector2i(176, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        Optional<ResourceLocation> rl = getMenu().getBlockEntity().getEntityType();
        if (rl.isPresent()) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl.get());
            if (type != null && ForgeRegistries.ENTITY_TYPES.getKey(type).equals(rl.get())) { // check we don't get the default pig
                String name = type.getDescription().getString();
                guiGraphics.drawString(font, name, imageWidth / 2f - font.width(name) / 2f, 15, 4210752, false);
            } else {
                guiGraphics.drawString(font, rl.get().toString(), imageWidth / 2f - font.width(rl.get().toString()) / 2f, 15, 4210752, false);
            }
        }

        // SkyTekx: the name of the mode under the mob, as 8.x shows it (a90e5c512).
        String modeLabel = getMenu().getBlockEntity().getMode().getTooltip().getString();
        guiGraphics.drawString(font, modeLabel, imageWidth / 2f - font.width(modeLabel) / 2f, 26, 4210752, false);
    }
}
