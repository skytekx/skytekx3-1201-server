package twilightforest.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.capabilities.CapabilityList;
import twilightforest.capabilities.shield.IShieldCapability;
import twilightforest.entity.passive.QuestRam;
import twilightforest.events.HostileMountEvents;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TFOverlays {
	//for some reason we need a 256x256 texture to actually render anything so i'll just make this a generic icons sheet
	//if we want to add any more overlay things in the future, we can simply add more icons!
	private static final ResourceLocation TF_ICONS_SHEET = TwilightForestMod.prefix("textures/gui/tf_icons.png");
	private static final ResourceLocation FORTIFICATION_SHIELD_TEXTURE = TwilightForestMod.prefix("textures/gui/fortification_shield.png");

	@SubscribeEvent
	public static void registerOverlays(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "quest_ram_indicator", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player != null && !minecraft.options.hideGui && TFConfig.CLIENT_CONFIG.showQuestRamCrosshairIndicator.get()) {
				RenderSystem.enableBlend();
				renderIndicator(minecraft, graphics, gui, player, screenWidth, screenHeight);
				RenderSystem.disableBlend();
			}
		});
		// Forge's food_level overlay hides the hunger bar while riding a living vehicle; a grabbed player still starves, so show it.
		event.registerAbove(VanillaGuiOverlay.MOUNT_HEALTH.id(), "hostile_mount_hunger_bar", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (!gui.getMinecraft().options.hideGui && gui.shouldDrawSurvivalElements() && player != null && HostileMountEvents.isRidingUnfriendly(player)) {
				gui.setupOverlayRenderState(true, false);
				gui.renderFood(screenWidth, screenHeight, graphics);
			}
		});

		event.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "fortification_shield_count", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player != null && !minecraft.options.hideGui && minecraft.gameMode != null && (minecraft.gameMode.canHurtPlayer() || TFConfig.CLIENT_CONFIG.showFortificationShieldIndicatorInCreative.get()) && TFConfig.CLIENT_CONFIG.showFortificationShieldIndicator.get()) {
				int shieldCount = player.getCapability(CapabilityList.SHIELDS).map(IShieldCapability::shieldsLeft).orElse(0);
				if (shieldCount > 0) {
					RenderSystem.enableBlend();
					renderShieldCount(graphics, gui, screenWidth, screenHeight, shieldCount);
					RenderSystem.disableBlend();
				}
			}
		});
	}

	/**
	 * Draws one shield icon per active fortification shield (at most 10) in a row above the armor bar, like 4.8 does.
	 * Forge 47 always reserves the armor row's height, even without armor, so the row needs no extra offset.
	 */
	public static void renderShieldCount(GuiGraphics graphics, ForgeGui gui, int screenWidth, int screenHeight, int shieldCount) {
		for (int i = 0; i < Math.min(shieldCount, 10); i++) {
			graphics.blit(FORTIFICATION_SHIELD_TEXTURE, screenWidth / 2 - 91 + (i * 8), screenHeight - gui.leftHeight, 0.0F, 0.0F, 9, 9, 9, 9);
		}
		gui.leftHeight += 10;
	}

	public static void renderIndicator(Minecraft minecraft, GuiGraphics graphics, Gui gui, Player player, int screenWidth, int screenHeight) {
		Options options = minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			if (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || gui.canRenderCrosshairForSpectator(minecraft.hitResult)) {
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				int j = ((screenHeight - 1) / 2) - 11;
				int k = ((screenWidth - 1) / 2) - 3;
				if (minecraft.crosshairPickEntity instanceof QuestRam ram) {
					ItemStack stack = player.getInventory().getItem(player.getInventory().selected);
					if (!stack.isEmpty() && stack.is(ItemTags.WOOL)) {
						if (ram.guessColor(stack) != null && !ram.isColorPresent(Objects.requireNonNull(ram.guessColor(stack)))) {
							graphics.blit(TF_ICONS_SHEET, k, j, 0, 0, 7, 7);
						} else {
							graphics.blit(TF_ICONS_SHEET, k, j, 7, 0, 7, 7);
						}
					}
				}
			}
		}
	}
}
