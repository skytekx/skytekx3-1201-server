package twilightforest.compat;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Drying recipe display helpers shared by the recipe viewer plugins (TF 4.8 keeps them in RecipeViewerConstants).
 * Client only: only ever loaded by a recipe viewer.
 */
public class DryingRecipeViewerHelper {

	public static Component getDryingTime(int dryingTicks) {
		int dryingMinutes = dryingTicks / 60 / 20;
		int dryingSeconds = (dryingTicks / 20) % 60;
		Component time;

		if (dryingMinutes > 0) {
			if (dryingSeconds > 0) {
				time = Component.translatable("gui.twilightforest.drying_time", dryingMinutes, dryingSeconds);
			} else {
				if (dryingMinutes == 1) {
					time = Component.translatable("gui.twilightforest.drying_minute", dryingMinutes);
				} else {
					time = Component.translatable("gui.twilightforest.drying_minutes", dryingMinutes);
				}
			}
		} else {
			if (dryingSeconds == 1) {
				time = Component.translatable("gui.twilightforest.drying_second", dryingSeconds);
			} else {
				time = Component.translatable("gui.twilightforest.drying_seconds", dryingSeconds);
			}
		}
		return time;
	}

	public static void renderFlatBlock(PoseStack stack, BlockState state, Vec3 location, float scale) {
		Minecraft minecraft = Minecraft.getInstance();
		MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
		stack.pushPose();
		Lighting.setupForFlatItems();
		stack.translate(location.x(), location.y(), location.z());
		stack.scale(scale, -scale, scale);
		minecraft.getBlockRenderer().renderSingleBlock(state, stack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
		stack.popPose();
		bufferSource.endBatch();
		Lighting.setupFor3DItems();
	}
}
