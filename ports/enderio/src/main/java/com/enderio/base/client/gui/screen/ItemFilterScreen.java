package com.enderio.base.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.api.misc.Vector2i;
import com.enderio.base.client.gui.widget.DamageFilterModeButton;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.base.common.menu.ItemFilterMenu;
import com.enderio.core.client.gui.screen.EIOScreen;
import com.enderio.core.client.gui.widgets.EIOImageButton;
import com.enderio.core.client.gui.widgets.ToggleImageButton;
import com.enderio.core.common.capability.DamageFilterMode;
import com.enderio.core.common.capability.ItemFilterCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.NotImplementedException;

public class ItemFilterScreen extends EIOScreen<ItemFilterMenu> {

    private static final Vector2i BG_SIZE = new Vector2i(183,201);
    // SkyTekx: the 4 row layout and texture of the 8.x EnderItemFilterScreen for the big filters (183 x 199).
    private static final int BIG_BG_HEIGHT = 199;
    private static final ResourceLocation BLACKLIST_TEXTURE = EnderIO.loc("textures/gui/icons/blacklist.png");
    private static final ResourceLocation NBT_TEXTURE = EnderIO.loc("textures/gui/icons/range_buttons.png");
    private static final ResourceLocation BACK_TEXTURE = EnderIO.loc("textures/gui/icons/back.png");

    private final ResourceLocation backgroundTexture;
    private final boolean bigLayout;

    public ItemFilterScreen(ItemFilterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, ItemFilterMenu.isBigLayout(pMenu.getFilter().size()));
        int size = pMenu.getFilter().size();
        this.bigLayout = ItemFilterMenu.isBigLayout(size);
        backgroundTexture = switch (size) {
            case 5 -> EnderIO.loc("textures/gui/40/basic_item_filter.png");
            case 2*5 ->  EnderIO.loc("textures/gui/40/advanced_item_filter.png");
            case 4*9 ->  EnderIO.loc("textures/gui/screens/filter_4x9.png");
            default -> throw new NotImplementedException();
        };

        if (bigLayout) {
            int rows = ItemFilterMenu.rowCount(size);
            this.imageHeight = BIG_BG_HEIGHT - (4 - rows) * 18;
            this.titleLabelX = 28;
            this.titleLabelY = 14;
            this.inventoryLabelX += 6;
            this.inventoryLabelY = 34 + rows * 18;
        }
    }

    @Override
    protected void init() {
        super.init();
        ItemFilterCapability filter = getMenu().getFilter();
        if (bigLayout) {
            // 8.x places the buttons from the right in the row between the filter slots and the inventory.
            int xPos = getGuiLeft() + BG_SIZE.x() - 25;
            int yPos = getGuiTop() + 27 + ItemFilterMenu.rowCount(filter.size()) * 18;
            if (filter.canFilterByDamage()) {
                addRenderableWidget(new DamageFilterModeButton(xPos, yPos, filter::getDamageMode, this::setDamageMode));
                xPos -= 18;
            }
            if (filter.canMatchNbt()) {
                addRenderableWidget(new ToggleImageButton<>(this, xPos, yPos, 16, 16, 0, 0, 16, 0, NBT_TEXTURE, filter::isNbt, getMenu()::setNbt, () -> filter.isNbt() ? EIOLang.NBT_FILTER : EIOLang.NO_NBT_FILTER));
                xPos -= 18;
            }
            addRenderableWidget(new ToggleImageButton<>(this, xPos, yPos, 16, 16, 0, 0, 16, 0, BLACKLIST_TEXTURE, filter::isInvert, getMenu()::setInverted, () -> filter.isInvert() ? EIOLang.BLACKLIST_FILTER : EIOLang.WHITELIST_FILTER));
        } else {
            addRenderableWidget(new ToggleImageButton<>(this, getGuiLeft() + 110,getGuiTop() + 36, 16, 16, 0, 0, 16, 0, NBT_TEXTURE, getMenu().getFilter()::isNbt, getMenu()::setNbt, () -> getMenu().getFilter().isNbt() ? EIOLang.NBT_FILTER : EIOLang.NO_NBT_FILTER));
            addRenderableWidget(new ToggleImageButton<>(this, getGuiLeft() + 110,getGuiTop() + 36 + 20, 16, 16, 0, 0, 16, 0, BLACKLIST_TEXTURE, getMenu().getFilter()::isInvert, getMenu()::setInverted, () -> getMenu().getFilter().isInvert() ? EIOLang.BLACKLIST_FILTER : EIOLang.WHITELIST_FILTER));
            if (filter.canFilterByDamage()) {
                addRenderableWidget(new DamageFilterModeButton(getGuiLeft() + 110, getGuiTop() + 36 + 40, filter::getDamageMode, this::setDamageMode));
            }
        }
        addRenderableWidget(new EIOImageButton(this, getGuiLeft() + 3, getGuiTop() + 3, 16, 16, 0, 0, 0, BACK_TEXTURE, button -> closeContainer()));
    }

    private void setDamageMode(DamageFilterMode mode) {
        // Like the vanilla stonecutter screen: apply the button on the client menu and send it to the server menu.
        Minecraft minecraft = Minecraft.getInstance();
        int buttonId = ItemFilterMenu.DAMAGE_MODE_BUTTON_ID + mode.ordinal();
        if (minecraft.player != null && minecraft.gameMode != null && getMenu().clickMenuButton(minecraft.player, buttonId)) {
            minecraft.gameMode.handleInventoryButtonClick(getMenu().containerId, buttonId);
        }
    }

    @Override
    public ResourceLocation getBackgroundImage() {
        return backgroundTexture;
    }

    @Override
    protected Vector2i getBackgroundImageSize() {
        return BG_SIZE;
    }
}
