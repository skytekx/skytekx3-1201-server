package twilightforest.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TwilightForestMod;
import twilightforest.capabilities.CapabilityList;
import twilightforest.capabilities.portal.TFPortalCapability;
import twilightforest.init.TFBlocks;

/**
 * [4.8 PortalOverlay] The Twilight portal fades in over the screen while the player stands in the portal, and open
 * screens close, as with a nether portal. The server side of the warm-up is in {@link TFPortalCapability}.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT)
public class PortalOverlay {

	/**
	 * [4.8 TFPortalAttachment#tick, client part]
	 */
	@SubscribeEvent
	public static void tickLocalPlayer(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.player instanceof LocalPlayer local) {
			local.getCapability(CapabilityList.TF_PORTAL).ifPresent(portal -> {
				portal.tick(local);

				if (portal.isInsidePortal()) {
					Minecraft minecraft = Minecraft.getInstance();
					if (minecraft.screen != null && !minecraft.screen.isPauseScreen() && !(minecraft.screen instanceof DeathScreen)) {
						if (minecraft.screen instanceof AbstractContainerScreen) local.closeContainer();
						minecraft.setScreen(null);
					}
					// TFPortalBlock#entityInside sets it again next tick if the player is still in the portal
					portal.setInPortal(false);
				}
			});
		}
	}

	public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player != null) {
			int portalTimer = player.getCapability(CapabilityList.TF_PORTAL).map(TFPortalCapability::getPortalTimer).orElse(0);
			if (portalTimer > 0) {
				RenderSystem.disableDepthTest();
				RenderSystem.depthMask(false);
				RenderSystem.enableBlend();
				graphics.setColor(1.0F, 1.0F, 1.0F, (float) portalTimer / (float) TFPortalCapability.MAX_TICKS);
				TextureAtlasSprite sprite = minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(TFBlocks.TWILIGHT_PORTAL.get().defaultBlockState());
				graphics.blit(0, 0, -90, screenWidth, screenHeight, sprite);
				RenderSystem.disableBlend();
				RenderSystem.depthMask(true);
				RenderSystem.enableDepthTest();
				graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}

	@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {
		@SubscribeEvent
		public static void registerOverlay(RegisterGuiOverlaysEvent event) {
			event.registerAboveAll("portal_overlay", PortalOverlay::render);
		}
	}
}
