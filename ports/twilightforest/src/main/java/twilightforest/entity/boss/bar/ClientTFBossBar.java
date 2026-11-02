package twilightforest.entity.boss.bar;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

/**
 * Client side of {@link ServerTFBossBar} (backported from TF 4.8): a lerping boss bar drawn in any RGB colour, rendered by
 * {@code TFClientEvents} in place of the vanilla bar. Client-only; only the client handlers of the boss bar packets create it.
 */
@OnlyIn(Dist.CLIENT)
public class ClientTFBossBar extends LerpingBossEvent {
	// 1.20.1 still draws boss bars from one sheet: each colour has a 5 px background row followed by a 5 px progress row, and the
	// overlay rows start at v 80. 4.8 blits the 1.20.2+ sprites boss_bar/white_background, boss_bar/white_progress and the
	// notched_* overlays, which are the same images.
	private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
	private static final int BAR_WIDTH = 182;
	private static final int BAR_HEIGHT = 5;
	private static final int WHITE_BAR_V = BossBarColor.WHITE.ordinal() * BAR_HEIGHT * 2;
	private static final int OVERLAY_V = 80;

	private int color;

	public ClientTFBossBar(UUID id, Component name, float progress, int color, BossBarOverlay overlay, boolean darkenScreen, boolean bossMusic, boolean worldFog) {
		super(id, name, progress, BossBarColor.WHITE, overlay, darkenScreen, bossMusic, worldFog);
		this.color = color;
	}

	public void setBarColor(int color) {
		this.color = color;
	}

	public long getSetTime() {
		return this.setTime;
	}

	public void setSetTime(long setTime) {
		this.setTime = setTime;
	}

	public void renderBossBar(GuiGraphics guiGraphics, int x, int y) {
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(((this.color >> 16) & 255) / 255F, ((this.color >> 8) & 255) / 255F, (this.color & 255) / 255F, 1.0F);

		this.drawBar(guiGraphics, x, y, BAR_WIDTH, 0);
		// same width rounding as BossHealthOverlay.drawBar, so our bars line up with vanilla ones
		int i = (int) (this.getProgress() * 183.0F);
		if (i > 0) {
			this.drawBar(guiGraphics, x, y, i, BAR_HEIGHT);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		Component title = this.getName();
		int l = Minecraft.getInstance().font.width(title);
		int i1 = guiGraphics.guiWidth() / 2 - l / 2;
		int j1 = y - 9;
		guiGraphics.drawString(Minecraft.getInstance().font, title, i1, j1, 16777215);

		RenderSystem.disableBlend();
	}

	private void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int vOffset) {
		guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, WHITE_BAR_V + vOffset, width, BAR_HEIGHT);
		if (this.overlay != BossBarOverlay.PROGRESS) {
			guiGraphics.blit(GUI_BARS_LOCATION, x, y, 0, OVERLAY_V + (this.overlay.ordinal() - 1) * BAR_HEIGHT * 2 + vOffset, width, BAR_HEIGHT);
		}
	}
}
