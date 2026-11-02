package twilightforest.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
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
import twilightforest.client.overlay.ItemDisplayOverlay;
import twilightforest.components.item.OreScannerData;
import twilightforest.entity.passive.QuestRam;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.events.HostileMountEvents;
import twilightforest.init.TFItems;
import twilightforest.item.OreMeterItem;
import twilightforest.util.ComponentAlignment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TFOverlays {
	//for some reason we need a 256x256 texture to actually render anything so i'll just make this a generic icons sheet
	//if we want to add any more overlay things in the future, we can simply add more icons!
	private static final ResourceLocation TF_ICONS_SHEET = TwilightForestMod.prefix("textures/gui/tf_icons.png");
	private static final ResourceLocation FORTIFICATION_SHIELD_TEXTURE = TwilightForestMod.prefix("textures/gui/fortification_shield.png");

	//Ore Meter HUD tables, keyed by the ore_data tag they were built from (4.8 keys them by a hash of the scan).
	//Only a few meters are ever held, so the cache is simply dropped when it grows past a handful of scans.
	private static final int ORE_METER_STAT_CACHE_SIZE = 16;
	private static final Map<CompoundTag, OreMeterInfoCache> ORE_METER_STAT_CACHE = new HashMap<>();

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

		event.registerAboveAll("ore_meter_stats", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player != null && !minecraft.options.hideGui && !minecraft.options.renderDebug && minecraft.screen == null) {
				renderOreMeterStats(graphics, player);
			}
		});

		// the Traveller's Goggles' Item Display, for the player the camera follows (4.8 OverlayHandler#getCameraPlayer)
		event.registerAboveAll("item_display_overlay", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			Minecraft minecraft = Minecraft.getInstance();
			ItemDisplayOverlay.render(graphics, minecraft, gui, minecraft.getCameraEntity() instanceof Player player ? player : null);
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
			if ((minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || gui.canRenderCrosshairForSpectator(minecraft.hitResult)) && minecraft.crosshairPickEntity instanceof QuestRam ram) {
				ItemStack stack = player.getInventory().getItem(player.getInventory().selected);
				if (!stack.isEmpty()) {
					// the quest items come from the data pack quest the server synced (SyncQuestsPacket)
					for (var questEntry : QuestingRamCurrentContext.getClientContext().questItems().entrySet()) {
						if (questEntry.getValue().test(stack)) {
							RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
							int j = ((screenHeight - 1) / 2) - 11;
							int k = ((screenWidth - 1) / 2) - 3;
							if (!ram.isColorPresent(questEntry.getKey())) {
								graphics.blit(TF_ICONS_SHEET, k, j, 0, 0, 7, 7);
							} else {
								graphics.blit(TF_ICONS_SHEET, k, j, 7, 0, 7, 7);
							}
							RenderSystem.defaultBlendFunc();
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * The Ore Meter HUD from 4.8 (1514b923b, 50819aa83, 97f03a8a2, 6988946d8): a loading indicator while the held meter scans,
	 * then a table of the ores it found with their count and their share of all scanned blocks.
	 */
	private static void renderOreMeterStats(GuiGraphics graphics, Player player) {
		if (player.isHolding(TFItems.ORE_METER.get())) {
			InteractionHand handToUse = player.getItemInHand(InteractionHand.MAIN_HAND).is(TFItems.ORE_METER.get()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			ItemStack selectedMeter = player.getItemInHand(handToUse);
			Font font = Minecraft.getInstance().font;
			if (OreMeterItem.isLoading(selectedMeter)) {
				int dots = (OreMeterItem.getLoadProgress(selectedMeter) / 5) % 3;
				MutableComponent component = Component.translatable("misc.twilightforest.ore_meter_loading");
				for (int i = 0; i <= dots; i++) {
					component.append(".");
				}
				//4.8 uses a fixed 56 pixel wide box, which longer translations overflow
				int width = Math.max(56, font.width(Component.translatable("misc.twilightforest.ore_meter_loading").append("...")) + 8);
				graphics.fill(0, 0, width, 16, 0x9b_00_00_00);
				graphics.drawString(font, component, 4, 4, 0x00_ff_ff_ff, false);
			} else if (OreMeterItem.hasScanData(selectedMeter)) {
				CompoundTag dataTag = Objects.requireNonNull(selectedMeter.getTag()).getCompound(OreMeterItem.DATA_TAG);
				boolean pretty = TFConfig.CLIENT_CONFIG.prettifyOreMeterGui.get();

				OreMeterInfoCache info = ORE_METER_STAT_CACHE.get(dataTag);
				if (info == null || info.prettified() != pretty) {
					info = initTooltips(OreScannerData.load(dataTag), pretty);
					if (ORE_METER_STAT_CACHE.size() >= ORE_METER_STAT_CACHE_SIZE) ORE_METER_STAT_CACHE.clear();
					ORE_METER_STAT_CACHE.put(dataTag.copy(), info);
				}

				info.renderData(graphics);
			}
		}
	}

	private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

	private static OreMeterInfoCache initTooltips(OreScannerData data, boolean pretty) {
		ChunkPos pos = data.scannedChunk();
		int totalScanned = data.totalScannedBlocks();

		List<Component> headerRowTexts = ImmutableList.of(
				Component.translatable("misc.twilightforest.ore_meter_range", data.scannedRange(), pos.x, pos.z),
				Component.translatable("misc.twilightforest.ore_meter_total", totalScanned)
		);

		List<ComponentColumn> columns = new ArrayList<>();

		List<Map.Entry<String, Integer>> scanData = data.counts().entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey())) // Sort by quantity
				.toList();

		if (scanData.isEmpty()) {
			columns.add(ComponentColumn.build(List.of(Component.translatable("misc.twilightforest.ore_meter_no_blocks")), ComponentAlignment.LEFT));
		} else if (pretty) {
			ComponentColumn padding = ComponentColumn.padding(1);
			List<Integer> counts = scanData.stream().map(Map.Entry::getValue).toList();

			columns.add(nameColumn(scanData.stream().map(Map.Entry::getKey).toList()));
			columns.add(padding);
			columns.add(dashColumn(scanData.size()));
			columns.add(padding);
			columns.add(countColumn(counts));
			columns.add(padding);
			columns.add(ratioColumn(totalScanned, counts));
		} else {
			columns.add(withoutPrettyPrinting(totalScanned, scanData));
		}

		return OreMeterInfoCache.build(headerRowTexts, columns, pretty);
	}

	private static String percentage(int count, int totalScanned) {
		return FORMAT.format(totalScanned > 0 ? count * 100.0F / totalScanned : 0.0F);
	}

	private static ComponentColumn withoutPrettyPrinting(int totalScanned, List<Map.Entry<String, Integer>> entries) {
		List<Component> tooltips = new ArrayList<>();

		for (Map.Entry<String, Integer> entry : entries) {
			Component formattedEntry = Component.translatable(entry.getKey())
					.append(Component.literal(" "))
					.append(Component.translatable("misc.twilightforest.ore_meter_separator"))
					.append(Component.literal(" " + entry.getValue() + " "))
					.append(Component.translatable("misc.twilightforest.ore_meter_ratio", percentage(entry.getValue(), totalScanned)));

			tooltips.add(formattedEntry);
		}

		return ComponentColumn.build(tooltips, ComponentAlignment.LEFT);
	}

	private static ComponentColumn nameColumn(List<String> oreNameKeys) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.translatable("misc.twilightforest.ore_meter_header_block").withStyle(ChatFormatting.GRAY));

		for (String oreNameKey : oreNameKeys) {
			toList.add(Component.translatable(oreNameKey));
		}

		return ComponentColumn.build(toList.build(), ComponentAlignment.LEFT);
	}

	private static ComponentColumn dashColumn(int size) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.empty());

		MutableComponent dash = Component.translatable("misc.twilightforest.ore_meter_separator");
		for (int i = 0; i < size; i++)
			toList.add(dash);

		return ComponentColumn.build(toList.build(), ComponentAlignment.CENTER);
	}

	private static ComponentColumn countColumn(List<Integer> oreCounts) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.translatable("misc.twilightforest.ore_meter_header_count").withStyle(ChatFormatting.GRAY));

		for (int count : oreCounts) {
			toList.add(Component.literal(String.valueOf(count)));
		}

		return ComponentColumn.build(toList.build(), ComponentAlignment.RIGHT);
	}

	private static ComponentColumn ratioColumn(int totalScanned, List<Integer> oreCounts) {
		ImmutableList.Builder<Component> toList = ImmutableList.builder();

		toList.add(Component.translatable("misc.twilightforest.ore_meter_header_ratio").withStyle(ChatFormatting.GRAY));

		for (int count : oreCounts) {
			toList.add(Component.translatable("misc.twilightforest.ore_meter_ratio", percentage(count, totalScanned)));
		}

		return ComponentColumn.build(toList.build(), ComponentAlignment.RIGHT);
	}

	private record ComponentColumn(List<? extends Component> textRows, int maxPixelWidth, ComponentAlignment textAlignment) {
		private static ComponentColumn build(List<? extends Component> rowTexts, ComponentAlignment textAlignment) {
			int maxColumnPixelWidth = rowTexts.stream().mapToInt(c -> Minecraft.getInstance().font.width(c)).max().orElse(0);
			return new ComponentColumn(rowTexts, maxColumnPixelWidth, textAlignment);
		}

		private static ComponentColumn padding(int forcedExtraMaxWidthBySpaces) {
			return new ComponentColumn(List.of(), forcedExtraMaxWidthBySpaces * Minecraft.getInstance().font.width(" "), ComponentAlignment.LEFT);
		}

		private int renderColumn(GuiGraphics graphics, int xOff, int yOff, int verticalTextPixelsAdvance) {
			Font font = Minecraft.getInstance().font;
			for (Component rowText : this.textRows()) {
				int textXPos = xOff + this.textAlignment().getTextOffset(font.width(rowText), this.maxPixelWidth());
				graphics.drawString(font, rowText, textXPos, yOff, 0x00_ff_ff_ff, false);
				yOff += verticalTextPixelsAdvance;
			}

			return this.maxPixelWidth();
		}
	}

	private record OreMeterInfoCache(int totalPixelWidth, int totalRowCount, List<Component> headerRows, List<ComponentColumn> textColumns, boolean prettified) {
		private static OreMeterInfoCache build(List<Component> headers, List<ComponentColumn> columns, boolean prettified) {
			// All these widths are measured in pixels, used for dealing with the font
			int summedColumnMaxWidths = columns.stream().mapToInt(ComponentColumn::maxPixelWidth).sum();
			int maxHeaderWidth = headers.stream().mapToInt(c -> Minecraft.getInstance().font.width(c)).max().orElse(0);

			int maxPixelWidth = Math.max(summedColumnMaxWidths, maxHeaderWidth);

			// Not measured in pixels, instead goes by element count - How many total rows of text will be shown in the GUI
			int totalRowCount = headers.size() + columns.stream().mapToInt(column -> column.textRows().size()).max().orElse(0);

			return new OreMeterInfoCache(maxPixelWidth, totalRowCount, ImmutableList.copyOf(headers), ImmutableList.copyOf(columns), prettified);
		}

		private void renderData(GuiGraphics graphics) {
			Font font = Minecraft.getInstance().font;
			int verticalTextPixelsAdvance = font.lineHeight + 1;

			graphics.fill(0, 0, this.totalPixelWidth() + 8, this.totalRowCount() * verticalTextPixelsAdvance + 6, 0x9b_00_00_00);

			int xOff = 4;
			int yOff = 4;

			for (Component headerRowText : this.headerRows()) {
				graphics.drawString(font, headerRowText, xOff, yOff, 0x00_ff_ff_ff, false);
				yOff += verticalTextPixelsAdvance;
			}

			for (ComponentColumn column : this.textColumns()) {
				xOff += column.renderColumn(graphics, xOff, yOff, verticalTextPixelsAdvance);
			}
		}
	}
}
