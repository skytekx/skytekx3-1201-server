package com.enderio.base.client.gui.screen;

import com.enderio.base.common.menu.AbstractFilterMenu;
import net.minecraft.client.Minecraft;

/**
 * Shared behaviour of the filter screens.
 */
final class FilterScreens {

    private FilterScreens() {
    }

    /**
     * The back arrow. A filter opened from a conduit returns to the conduit screen through the menu button of Ender IO 8.x
     * (AbstractFilterMenu.BACK_BUTTON_ID, 04e69b8e6), which the server answers by opening the conduit menu again. A filter in the
     * main hand just closes, as in 6.x.
     */
    static void back(AbstractFilterMenu menu) {
        Minecraft minecraft = Minecraft.getInstance();
        if (menu.hasParentMenu() && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, AbstractFilterMenu.BACK_BUTTON_ID);
        } else if (minecraft.player != null) {
            // EIOScreen.closeContainer
            minecraft.player.closeContainer();
        }
    }
}
