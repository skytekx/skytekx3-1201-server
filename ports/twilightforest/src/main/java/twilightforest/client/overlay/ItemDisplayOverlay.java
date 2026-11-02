package twilightforest.client.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.client.overlay.display.ClockDisplay;
import twilightforest.client.overlay.display.CompassDisplay;
import twilightforest.client.overlay.display.ItemDisplay;
import twilightforest.client.overlay.display.MapDisplay;
import twilightforest.client.overlay.display.MoonDialDisplay;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.custom.ItemDisplays;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The Traveller's Goggles' Item Display on the HUD: the chosen map, the compass, clock and Moon Dial stored in the
 * goggles of the viewed player, stacked in the top left corner (offset and scale in the client config). Backport of
 * 4.8's ItemDisplayOverlay, registered in TFOverlays.
 * <p>
 * 4.8 keeps the widget factory in the registered ItemDisplayType; that is a client class, so here the widgets are
 * mapped to the type ids with {@link #registerDisplay} and the registry stays safe for a dedicated server.
 */
public class ItemDisplayOverlay {
	private static final Map<ResourceLocation, Supplier<ItemDisplay>> DISPLAYS = new ConcurrentHashMap<>();

	// the stored items, decoded again only when the goggles' item_display NBT changes, not every frame
	@Nullable
	private static Tag cachedTag;
	@Nullable
	private static ItemDisplayContents cachedContents;

	static {
		registerDisplay(ItemDisplays.MAP, MapDisplay::new);
		registerDisplay(ItemDisplays.COMPASS, CompassDisplay::new);
		registerDisplay(ItemDisplays.CLOCK, ClockDisplay::new);
		registerDisplay(ItemDisplays.MOON_DIAL, MoonDialDisplay::new);
	}

	/**
	 * Sets the HUD widget of an Item Display slot type.
	 */
	public static void registerDisplay(RegistryObject<ItemDisplayType> type, Supplier<ItemDisplay> display) {
		DISPLAYS.put(type.getId(), display);
	}

	public static void render(GuiGraphics graphics, Minecraft minecraft, Gui gui, @Nullable Player player) {
		if (player == null || minecraft.options.renderDebug || minecraft.options.hideGui)
			return;

		ItemStack goggles = player.getItemBySlot(EquipmentSlot.HEAD);
		if (!TravellersArmorItem.isTravellersGear(goggles) || !TravellersModifiersManager.isModifierActive(player, goggles, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER))
			return;

		ItemDisplayContents contents = getContents(goggles);
		if (contents == null || contents.isEmpty())
			return;

		List<DisplayHolder> typesToRender = new ArrayList<>();
		int widest = fillDisplayHolders(typesToRender, contents, minecraft, gui, player);
		if (typesToRender.isEmpty())
			return;

		renderHolders(graphics, minecraft, gui, player, typesToRender, widest);
	}

	@Nullable
	private static ItemDisplayContents getContents(ItemStack goggles) {
		CompoundTag tag = goggles.getTag();
		Tag displayTag = tag == null ? null : tag.get(TravellersGearData.ITEM_DISPLAY.key());
		if (displayTag == null)
			return TravellersGearData.ITEM_DISPLAY.get(goggles);
		if (!displayTag.equals(cachedTag)) {
			cachedContents = TravellersGearData.ITEM_DISPLAY.get(goggles);
			cachedTag = displayTag.copy();
		}
		return cachedContents;
	}

	private static void renderHolders(GuiGraphics graphics, Minecraft minecraft, Gui gui, Player player, List<DisplayHolder> typesToRender, int widest) {
		float scale = TFConfig.CLIENT_CONFIG.itemDisplayScreenScale.get().floatValue();
		graphics.pose().pushPose();
		graphics.pose().translate(TFConfig.CLIENT_CONFIG.itemDisplayScreenOffsetX.get(), TFConfig.CLIENT_CONFIG.itemDisplayScreenOffsetY.get(), 0.0F);
		graphics.pose().scale(scale, scale, scale);
		typesToRender.sort(Comparator.comparing(holder -> holder.display().displayPosition()));
		for (DisplayHolder holder : typesToRender) {
			graphics.pose().pushPose();
			holder.display().render(holder.stack(), graphics, minecraft, gui, player, widest);
			//debug fill to see widget sizes
			//graphics.fill(holder.bounds().startX(), holder.bounds().startY(), holder.bounds().startX() + holder.bounds().width(), holder.bounds().startY() + holder.bounds().height(), 0x80FF0000);
			graphics.pose().popPose();
			graphics.pose().translate(0.0F, holder.bounds().height(), 0.0F);
		}
		graphics.pose().popPose();
	}

	private static int fillDisplayHolders(List<DisplayHolder> typesToRender, ItemDisplayContents contents, Minecraft minecraft, Gui gui, Player player) {
		int widest = 0;

		int slots = Math.min(ItemDisplayContents.LAYOUT.size(), contents.size());
		int activeMapSlot = contents.findActiveMapSlot();

		for (int i = 0; i < slots; i++) {
			ItemStack stack = contents.getItem(i);
			if (stack.isEmpty()) continue;

			RegistryObject<ItemDisplayType> typeObject = ItemDisplayContents.LAYOUT.get(i);
			if (typeObject == ItemDisplays.MAP && i != activeMapSlot)
				continue;

			if (!typeObject.get().validItems().test(stack))
				continue;

			Supplier<ItemDisplay> displayFactory = DISPLAYS.get(typeObject.getId());
			if (displayFactory == null)
				continue;

			ItemDisplay display = displayFactory.get();
			ItemDisplay.Bounds bounds = display.getWidgetSize(stack, minecraft, gui, player, widest);
			widest = Math.max(widest, bounds.width());
			typesToRender.add(new DisplayHolder(stack, display, bounds));
		}
		return widest;
	}

	public record DisplayHolder(ItemStack stack, ItemDisplay display, ItemDisplay.Bounds bounds) {

	}
}
