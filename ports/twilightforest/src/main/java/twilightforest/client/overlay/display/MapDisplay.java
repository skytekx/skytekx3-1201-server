package twilightforest.client.overlay.display;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

/**
 * The map widget: the chosen map, 100 pixels wide, with its markers. Backport of 4.8's MapDisplay. Forge's
 * MapItem#getSavedData(ItemStack, Level) asks the map item for its data, so the Twilight Forest's Magic, Maze and Ore
 * Maps are drawn from their own data like in the hand.
 */
public class MapDisplay implements ItemDisplay {

	private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));

	@Override
	public void render(ItemStack item, GuiGraphics graphics, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		PoseStack stack = graphics.pose();
		Integer mapid = MapItem.getMapId(item);
		if (mapid == null)
			return;

		MapItemSavedData data = MapItem.getSavedData(item, minecraft.level);
		if (data == null)
			return;
		VertexConsumer consumer = graphics.bufferSource().getBuffer(MAP_BACKGROUND_CHECKERBOARD);
		Matrix4f matrix4f = stack.last().pose();
		//render map background
		float start = Math.max(widestWidgetWidth / 2 - 50, 0);
		consumer.vertex(matrix4f, start, 100.0F, -2.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(matrix4f, start + 100.0F, 100.0F, -2.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(matrix4f, start + 100.0F, 0.0F, -2.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
		consumer.vertex(matrix4f, start, 0.0F, -2.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(LightTexture.FULL_BRIGHT).endVertex();
		//render map data
		stack.pushPose();
		//these transformations are very important, otherwise icons render behind the map graphics
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		stack.translate(-4.75F, -4.75F, -1.0F);
		stack.translate(-start, 0.0F, 0.0F);
		stack.scale(0.7075F, 0.7075F, -0.7075F);
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		minecraft.gameRenderer.getMapRenderer().render(stack, graphics.bufferSource(), mapid, data, false, LightTexture.FULL_BRIGHT);
		graphics.flush();
		stack.popPose();
	}

	@Override
	public DisplayPosition displayPosition() {
		return DisplayPosition.TOP;
	}

	@Override
	public Bounds getWidgetSize(ItemStack item, Minecraft minecraft, Gui gui, Player player, int widestWidgetWidth) {
		return new Bounds(Math.max(widestWidgetWidth / 2 - 50, 0), 0, 100, 102);
	}
}
