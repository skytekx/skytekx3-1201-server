package twilightforest.client.renderer.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.item.travellers_gear.TravellersGogglesItem;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

import java.util.ArrayList;
import java.util.List;

/**
 * The items stored in the Traveller's Goggles' Item Display, drawn as a row of bundle slots under the tooltip; an empty
 * slot shows the outline of the item it takes. Backport of 4.8's ItemDisplayTooltipComponent; 1.20.1 has no GUI sprite
 * atlas, so the slots and the border come from the bundle texture like in TravellersBeltTooltipComponent.
 */
public class ItemDisplayTooltipComponent implements ClientTooltipComponent {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
	private static final int TEXTURE_SIZE = 128;
	private static final int SLOT_WIDTH = 18;
	private static final int SLOT_HEIGHT = 20;

	private final List<ItemStack> contents;

	public ItemDisplayTooltipComponent(TravellersGogglesItem.Tooltip tooltip) {
		ItemDisplayContents contents = tooltip.contents();
		this.contents = new ArrayList<>(contents.items());
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		int k = 0;

		for (int gridY = 0; gridY < this.gridSizeY(); gridY++) {
			for (int gridX = 0; gridX < this.gridSizeX(); gridX++) {
				int renderX = x + gridX * SLOT_WIDTH + 1;
				int renderY = y + gridY * SLOT_HEIGHT + 1;
				this.renderSlot(renderX, renderY, k++, guiGraphics, font);
			}
		}

		this.drawBorder(x, y, this.gridSizeX(), this.gridSizeY(), guiGraphics);
	}

	private void renderSlot(int x, int y, int itemIndex, GuiGraphics graphics, Font font) {
		this.blit(graphics, x, y, Texture.SLOT);

		if (itemIndex < this.contents.size()) {
			ItemStack itemstack = this.contents.get(itemIndex);
			if (itemstack.isEmpty()) {
				this.renderBlankSlot(graphics, itemIndex, x, y);
			} else {
				graphics.renderItem(itemstack, x + 1, y + 1, itemIndex);
				graphics.renderItemDecorations(font, itemstack, x + 1, y + 1);
			}
		} else {
			this.renderBlankSlot(graphics, itemIndex, x, y);
		}
	}

	private void renderBlankSlot(GuiGraphics graphics, int index, int x, int y) {
		if (index < 0 || index >= ItemDisplayContents.LAYOUT.size()) return;
		ItemDisplayType type = ItemDisplayContents.LAYOUT.get(index).get();
		type.slotTexture().ifPresent(resourceLocation -> graphics.blit(resourceLocation, x + 1, y + 1, 0, 0, 16, 16, 16, 16));
	}

	// [VanillaCopy] 1.20.1 ClientBundleTooltip#drawBorder, the frame 1.21 draws with the bundle background sprite
	private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics guiGraphics) {
		this.blit(guiGraphics, x, y, Texture.BORDER_CORNER_TOP);
		this.blit(guiGraphics, x + slotWidth * SLOT_WIDTH + 1, y, Texture.BORDER_CORNER_TOP);

		for (int i = 0; i < slotWidth; i++) {
			this.blit(guiGraphics, x + 1 + i * SLOT_WIDTH, y, Texture.BORDER_HORIZONTAL_TOP);
			this.blit(guiGraphics, x + 1 + i * SLOT_WIDTH, y + slotHeight * SLOT_HEIGHT, Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int i = 0; i < slotHeight; i++) {
			this.blit(guiGraphics, x, y + i * SLOT_HEIGHT + 1, Texture.BORDER_VERTICAL);
			this.blit(guiGraphics, x + slotWidth * SLOT_WIDTH + 1, y + i * SLOT_HEIGHT + 1, Texture.BORDER_VERTICAL);
		}

		this.blit(guiGraphics, x, y + slotHeight * SLOT_HEIGHT, Texture.BORDER_CORNER_BOTTOM);
		this.blit(guiGraphics, x + slotWidth * SLOT_WIDTH + 1, y + slotHeight * SLOT_HEIGHT, Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
		guiGraphics.blit(TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, TEXTURE_SIZE, TEXTURE_SIZE);
	}

	private int backgroundWidth() {
		return this.gridSizeX() * SLOT_WIDTH + 2;
	}

	private int backgroundHeight() {
		return this.gridSizeY() * SLOT_HEIGHT + 2;
	}

	private int gridSizeX() {
		return ItemDisplayContents.LAYOUT.size();
	}

	private int gridSizeY() {
		return 1;
	}

	@Override
	public int getHeight() {
		return this.backgroundHeight() + 4;
	}

	@Override
	public int getWidth(Font font) {
		return this.backgroundWidth();
	}

	// [VanillaCopy] the parts of 1.20.1's ClientBundleTooltip.Texture used here
	private enum Texture {
		SLOT(0, 0, 18, 20),
		BORDER_VERTICAL(0, 18, 1, 20),
		BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
		BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
		BORDER_CORNER_TOP(0, 20, 1, 1),
		BORDER_CORNER_BOTTOM(0, 60, 1, 1);

		public final int x;
		public final int y;
		public final int w;
		public final int h;

		Texture(int x, int y, int w, int h) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
	}
}
