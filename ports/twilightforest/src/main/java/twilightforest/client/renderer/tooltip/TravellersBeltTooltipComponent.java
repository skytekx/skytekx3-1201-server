package twilightforest.client.renderer.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersContainerContents;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The hotbar stored in a Traveller's Belt (or wings with Swap Hotbar), drawn as a row of nine bundle slots under the
 * tooltip. Backport of 4.8's TravellersBeltTooltipComponent, a modified ClientBundleTooltip; 1.20.1 has no GUI sprite
 * atlas, so the slots and the border come from the bundle texture the way 1.20.1's ClientBundleTooltip draws them.
 */
public class TravellersBeltTooltipComponent implements ClientTooltipComponent {
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
	private static final int TEXTURE_SIZE = 128;
	private static final int SLOT_SIZE_X = 18;
	private static final int SLOT_SIZE_Y = 20;
	private final List<ItemStack> contents = new ArrayList<>();

	public TravellersBeltTooltipComponent(TravellersArmorBeltItem.Tooltip tooltip) {
		TravellersContainerContents contents = tooltip.contents();
		for (int i = 0; i < 9; i++) {
			this.contents.add(contents.getStackInSlot(i));
		}
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
		int k = 0;

		for (int gridY = 0; gridY < this.gridSizeY(); gridY++) {
			for (int gridX = 0; gridX < this.gridSizeX(); gridX++) {
				int renderX = x + gridX * SLOT_SIZE_X + 1;
				int renderY = y + gridY * SLOT_SIZE_Y + 1;
				this.renderSlot(renderX, renderY, k++, guiGraphics, font);
			}
		}

		this.drawBorder(x, y, this.gridSizeX(), this.gridSizeY(), guiGraphics);
	}

	private void renderSlot(int x, int y, int itemIndex, GuiGraphics guiGraphics, Font font) {
		this.blit(guiGraphics, x, y, Texture.SLOT);
		if (itemIndex < this.contents.size()) {
			ItemStack itemstack = this.contents.get(itemIndex);
			guiGraphics.renderItem(itemstack, x + 1, y + 1, itemIndex);
			guiGraphics.renderItemDecorations(font, itemstack, x + 1, y + 1);
		}
	}

	// [VanillaCopy] 1.20.1 ClientBundleTooltip#drawBorder, the frame 1.21 draws with the bundle background sprite
	private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics guiGraphics) {
		this.blit(guiGraphics, x, y, Texture.BORDER_CORNER_TOP);
		this.blit(guiGraphics, x + slotWidth * SLOT_SIZE_X + 1, y, Texture.BORDER_CORNER_TOP);

		for (int i = 0; i < slotWidth; i++) {
			this.blit(guiGraphics, x + 1 + i * SLOT_SIZE_X, y, Texture.BORDER_HORIZONTAL_TOP);
			this.blit(guiGraphics, x + 1 + i * SLOT_SIZE_X, y + slotHeight * SLOT_SIZE_Y, Texture.BORDER_HORIZONTAL_BOTTOM);
		}

		for (int i = 0; i < slotHeight; i++) {
			this.blit(guiGraphics, x, y + i * SLOT_SIZE_Y + 1, Texture.BORDER_VERTICAL);
			this.blit(guiGraphics, x + slotWidth * SLOT_SIZE_X + 1, y + i * SLOT_SIZE_Y + 1, Texture.BORDER_VERTICAL);
		}

		this.blit(guiGraphics, x, y + slotHeight * SLOT_SIZE_Y, Texture.BORDER_CORNER_BOTTOM);
		this.blit(guiGraphics, x + slotWidth * SLOT_SIZE_X + 1, y + slotHeight * SLOT_SIZE_Y, Texture.BORDER_CORNER_BOTTOM);
	}

	private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
		guiGraphics.blit(TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, TEXTURE_SIZE, TEXTURE_SIZE);
	}

	private int backgroundWidth() {
		return this.gridSizeX() * SLOT_SIZE_X + 2;
	}

	private int backgroundHeight() {
		return this.gridSizeY() * SLOT_SIZE_Y + 2;
	}

	private int gridSizeX() {
		return 9;
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
