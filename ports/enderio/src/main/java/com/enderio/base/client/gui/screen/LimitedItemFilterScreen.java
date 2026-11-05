package com.enderio.base.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.client.gui.widget.DamageFilterModeButton;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.base.common.menu.ItemFilterMenu;
import com.enderio.base.common.menu.LimitedItemFilterMenu;
import com.enderio.core.client.gui.screen.EIOScreen;
import com.enderio.core.client.gui.widgets.EIOImageButton;
import com.enderio.core.client.gui.widgets.ToggleImageButton;
import com.enderio.core.common.capability.DamageFilterMode;
import com.enderio.core.common.capability.LimitedItemFilterCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The screen of the limited item filter, from LimitedItemFilterScreen of Ender IO 8.x (1a85694c2) with the 6.x
 * widgets: the NBT toggle and a damage mode cycle button in the row under the two rows of filter slots.
 */
public class LimitedItemFilterScreen extends EIOScreen<LimitedItemFilterMenu> {

    private static final Vector2i BG_SIZE = new Vector2i(183, 163);
    private static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/screens/filter_2x9.png");
    private static final ResourceLocation NBT_TEXTURE = EnderIO.loc("textures/gui/icons/range_buttons.png");
    private static final ResourceLocation BACK_TEXTURE = EnderIO.loc("textures/gui/icons/back.png");

    public LimitedItemFilterScreen(LimitedItemFilterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, true);
        this.titleLabelX = 28;
        this.titleLabelY = 14;
        this.inventoryLabelX += 6;
        this.inventoryLabelY = 34 + LimitedItemFilterMenu.ROWS * 18;
    }

    @Override
    protected void init() {
        super.init();
        LimitedItemFilterCapability filter = getMenu().getFilter();

        int xPos = getGuiLeft() + BG_SIZE.x() - 25;
        int yPos = getGuiTop() + 27 + LimitedItemFilterMenu.ROWS * 18;
        addRenderableWidget(new DamageFilterModeButton(xPos, yPos, filter::getDamageMode, this::setDamageMode));
        xPos -= 18;
        addRenderableWidget(new ToggleImageButton<>(this, xPos, yPos, 16, 16, 0, 0, 16, 0, NBT_TEXTURE, filter::isNbt, getMenu()::setNbt, () -> filter.isNbt() ? EIOLang.NBT_FILTER : EIOLang.NO_NBT_FILTER));

        addRenderableWidget(new EIOImageButton(this, getGuiLeft() + 3, getGuiTop() + 3, 16, 16, 0, 0, 0, BACK_TEXTURE, button -> FilterScreens.back(getMenu())));
    }

    private void setDamageMode(DamageFilterMode mode) {
        Minecraft minecraft = Minecraft.getInstance();
        int buttonId = ItemFilterMenu.DAMAGE_MODE_BUTTON_ID + mode.ordinal();
        if (minecraft.player != null && minecraft.gameMode != null && getMenu().clickMenuButton(minecraft.player, buttonId)) {
            minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, buttonId);
        }
    }

    @Override
    public ResourceLocation getBackgroundImage() {
        return BG_TEXTURE;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return BG_SIZE;
    }
}
