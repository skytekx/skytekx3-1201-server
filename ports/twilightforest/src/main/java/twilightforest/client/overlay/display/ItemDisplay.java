package twilightforest.client.overlay.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A HUD widget of the Traveller's Goggles' Item Display, drawn for an item stored in the goggles. Backport of 4.8's
 * ItemDisplay; the widgets are mapped to the slot types in {@link twilightforest.client.overlay.ItemDisplayOverlay}.
 */
public interface ItemDisplay {

	void render(ItemStack item, GuiGraphics graphics, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth);

	default DisplayPosition displayPosition() {
		return DisplayPosition.DEFAULT;
	}

	Bounds getWidgetSize(ItemStack item, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth);

	enum DisplayPosition {
		TOP,
		DEFAULT,
		BOTTOM
	}

	record Bounds(int startX, int startY, int width, int height) {

	}
}
