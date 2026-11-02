package twilightforest.client.renderer.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Matrix4f;
import twilightforest.TwilightForestMod;
import twilightforest.item.BrittleFlaskItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Backport of 4.8's potion flask tooltip: the potion's name and effects, then a bar showing the doses left.
 * <p>
 * 1.20.1 has no GUI sprite atlas, so the bar border and the dose separator are plain textures under
 * {@code textures/gui/}, and the border's 4.8 nine-slice scaling (12x12 with a 2px border) is done here by hand.
 */
public class PotionFlaskTooltipComponent implements ClientTooltipComponent {

	private static final ResourceLocation BORDER_TEXTURE = TwilightForestMod.getGuiTexture("flask_bar_border.png");
	private static final int BORDER_TEXTURE_SIZE = 12;
	private static final int BORDER_SLICE = 2;
	private static final ResourceLocation DOSE_TEXTURE = TwilightForestMod.getGuiTexture("flask_dose_bar.png");
	private static final Component EMPTY_DESCRIPTION = Component.translatable("item.twilightforest.flask.empty_description");

	public static final int WIDTH = 115; //hehe
	private static final int BAR_HEIGHT = 13;
	private static final int DESCRIPTION_COLOR = 11184810;

	private final BrittleFlaskItem.Tooltip tooltip;
	private final List<Component> potionTooltips;

	public PotionFlaskTooltipComponent(BrittleFlaskItem.Tooltip tooltip) {
		this.tooltip = tooltip;
		this.potionTooltips = this.createPotionTooltips();
	}

	private boolean hasPotion() {
		return this.tooltip.potion() != Potions.EMPTY;
	}

	@Override
	public int getHeight() {
		return this.getDescriptionHeight(Minecraft.getInstance().font) + BAR_HEIGHT + 8;
	}

	@Override
	public int getWidth(Font font) {
		return WIDTH;
	}

	private int getDescriptionHeight(Font font) {
		if (this.hasPotion()) {
			int height = 0;
			for (Component component : this.potionTooltips) {
				if (component.getString().isEmpty()) {
					height += font.lineHeight;
				}
				height += font.split(component, WIDTH).size() * font.lineHeight + 1;
			}

			return height;
		}
		return font.split(EMPTY_DESCRIPTION, WIDTH).size() * font.lineHeight + 1;
	}

	private List<Component> createPotionTooltips() {
		if (this.hasPotion()) {
			List<Component> tooltips = new ArrayList<>();
			tooltips.add(Component.translatable(this.tooltip.potion().getName("item.minecraft.potion.effect.")));
			PotionUtils.addPotionTooltip(this.tooltip.effects(), tooltips, 1.0F);
			return tooltips;
		}
		return List.of();
	}

	private int getContentXOffset(int offs) {
		return (offs - WIDTH) / 2;
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		int offs = 113;
		if (!this.hasPotion()) {
			graphics.drawWordWrap(font, EMPTY_DESCRIPTION, x, y, WIDTH, DESCRIPTION_COLOR);
		} else {
			int height = 0;
			for (Component component : this.potionTooltips) {
				int color = component.getStyle().getColor() != null ? component.getStyle().getColor().getValue() : DESCRIPTION_COLOR;
				if (component.getString().isEmpty()) {
					height += font.lineHeight;
				} else {
					graphics.drawWordWrap(font, component, x, y + height, WIDTH, color);
				}
				height += font.split(component, WIDTH).size() * font.lineHeight + 1;
			}
		}
		this.drawPotionBar(x + this.getContentXOffset(offs), y + this.getDescriptionHeight(font) + 4, font, graphics);
	}

	private void drawPotionBar(int x, int y, Font font, GuiGraphics graphics) {
		int maxDoses = this.tooltip.maxDoses();
		int segmentSplit = this.getWidth(font) / maxDoses;
		//flasks filled before the 4-to-3 dose rebalance can hold more than fits on the bar
		int doses = Math.min(this.tooltip.doses(), maxDoses);
		int breakage = Math.min(this.tooltip.breakage(), maxDoses);
		if (doses <= 0) {
			graphics.drawCenteredString(font, Component.translatable("item.twilightforest.flask.empty"), x + (WIDTH / 2) + 1, y + 3, 16777215);
		} else {
			renderPotion(graphics.pose(), x + 1, y + BAR_HEIGHT, doses * segmentSplit - 1, BAR_HEIGHT, this.tooltip.color());
		}

		if (breakage > 0) {
			int xPos = x + segmentSplit * (maxDoses - breakage);
			RenderSystem.enableBlend();
			graphics.fill(xPos, y, xPos + (segmentSplit * breakage), y + BAR_HEIGHT, 0xAA434343);
			RenderSystem.disableBlend();
		}
		int widthProg = segmentSplit;
		for (int i = 1; i < maxDoses; i++) {
			graphics.blit(DOSE_TEXTURE, x + widthProg, y, 0.0F, 0.0F, 1, BAR_HEIGHT, 1, BAR_HEIGHT);
			widthProg += segmentSplit;
		}

		blitBorder(graphics, x, y, WIDTH, BAR_HEIGHT);
	}

	/**
	 * Nine-slice equivalent of 4.8's {@code blitSprite} with the border's {@code gui.scaling} metadata. The edges and
	 * the centre of the texture are uniform, so stretching them looks the same as tiling.
	 */
	private static void blitBorder(GuiGraphics graphics, int x, int y, int width, int height) {
		int slice = BORDER_SLICE;
		int size = BORDER_TEXTURE_SIZE;
		int inner = size - slice * 2;
		int innerWidth = width - slice * 2;
		int innerHeight = height - slice * 2;
		int right = x + width - slice;
		int bottom = y + height - slice;
		//corners
		graphics.blit(BORDER_TEXTURE, x, y, 0.0F, 0.0F, slice, slice, size, size);
		graphics.blit(BORDER_TEXTURE, right, y, size - slice, 0.0F, slice, slice, size, size);
		graphics.blit(BORDER_TEXTURE, x, bottom, 0.0F, size - slice, slice, slice, size, size);
		graphics.blit(BORDER_TEXTURE, right, bottom, size - slice, size - slice, slice, slice, size, size);
		//edges
		graphics.blit(BORDER_TEXTURE, x + slice, y, innerWidth, slice, slice, 0.0F, inner, slice, size, size);
		graphics.blit(BORDER_TEXTURE, x + slice, bottom, innerWidth, slice, slice, size - slice, inner, slice, size, size);
		graphics.blit(BORDER_TEXTURE, x, y + slice, slice, innerHeight, 0.0F, slice, slice, inner, size, size);
		graphics.blit(BORDER_TEXTURE, right, y + slice, slice, innerHeight, size - slice, slice, slice, inner, size, size);
		//centre
		graphics.blit(BORDER_TEXTURE, x + slice, y + slice, innerWidth, innerHeight, slice, slice, inner, inner, size, size);
	}

	//tiles the still water texture, tinted with the potion colour, across the filled part of the bar
	private static void renderPotion(PoseStack stack, int xPosition, int yPosition, int desiredWidth, int desiredHeight, int color) {
		float red = (color >> 16 & 255) / 255.0F;
		float green = (color >> 8 & 255) / 255.0F;
		float blue = (color & 255) / 255.0F;
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(IClientFluidTypeExtensions.of(Fluids.WATER).getStillTexture());
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		int xTileCount = desiredWidth / 16;
		int xRemainder = desiredWidth - (xTileCount * 16);
		int yTileCount = desiredHeight / 16;
		int yRemainder = desiredHeight - (yTileCount * 16);
		float uMin = sprite.getU0();
		float uMax = sprite.getU1();
		float vMin = sprite.getV0();
		float vMax = sprite.getV1();
		float uDif = uMax - uMin;
		float vDif = vMax - vMin;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(red, green, blue, 1.0F);
		BufferBuilder vertexBuffer = Tesselator.getInstance().getBuilder();
		vertexBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix4f = stack.last().pose();
		for (int xTile = 0; xTile <= xTileCount; xTile++) {
			int width = (xTile == xTileCount) ? xRemainder : 16;
			if (width == 0) {
				break;
			}
			int x = xPosition + (xTile * 16);
			int maskRight = 16 - width;
			int shiftedX = x + 16 - maskRight;
			float uLocalDif = uDif * maskRight / 16;

			for (int yTile = 0; yTile <= yTileCount; yTile++) {
				int height = (yTile == yTileCount) ? yRemainder : 16;
				if (height == 0) {
					break;
				}
				int y = yPosition - ((yTile + 1) * 16);
				int maskTop = 16 - height;
				float vLocalDif = vDif * maskTop / 16;

				vertexBuffer.vertex(matrix4f, x, y + 16, 0).uv(uMin + uLocalDif, vMax).endVertex();
				vertexBuffer.vertex(matrix4f, shiftedX, y + 16, 0).uv(uMax, vMax).endVertex();
				vertexBuffer.vertex(matrix4f, shiftedX, y + maskTop, 0).uv(uMax, vMin + vLocalDif).endVertex();
				vertexBuffer.vertex(matrix4f, x, y + maskTop, 0).uv(uMin + uLocalDif, vMin + vLocalDif).endVertex();
			}
		}
		BufferUploader.drawWithShader(vertexBuffer.end());
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
