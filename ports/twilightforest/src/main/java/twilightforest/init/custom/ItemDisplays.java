package twilightforest.init.custom;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFItems;
import twilightforest.item.travellers_gear.modifiers.display.ItemDisplayType;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * The kinds of slots of the Traveller's Goggles' Item Display, the static {@code twilight:item_display_type} registry.
 * Backport of 4.8's ItemDisplays; the HUD widget of each type is mapped in the client's ItemDisplayOverlay.
 */
public final class ItemDisplays {
	public static final ResourceKey<Registry<ItemDisplayType>> ITEM_DISPLAY_TYPE_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("item_display_type"));

	public static final DeferredRegister<ItemDisplayType> DISPLAYS = DeferredRegister.create(ITEM_DISPLAY_TYPE_KEY, TwilightForestMod.ID);
	// Synced like 4.8's registry; no ids are saved, the slot layout of the goggles is fixed in ItemDisplayContents
	public static final Supplier<IForgeRegistry<ItemDisplayType>> REGISTRY = DISPLAYS.makeRegistry(() -> new RegistryBuilder<ItemDisplayType>().disableSaving());

	public static final RegistryObject<ItemDisplayType> MAP = DISPLAYS.register("map", () -> new ItemDisplayType(stack -> stack.getItem() instanceof MapItem, Optional.of(TwilightForestMod.prefix("textures/item/map_display.png"))));
	public static final RegistryObject<ItemDisplayType> COMPASS = DISPLAYS.register("compass", () -> new ItemDisplayType(stack -> stack.is(Items.COMPASS), Optional.of(TwilightForestMod.prefix("textures/item/compass_display.png"))));
	public static final RegistryObject<ItemDisplayType> CLOCK = DISPLAYS.register("clock", () -> new ItemDisplayType(stack -> stack.is(Items.CLOCK), Optional.of(TwilightForestMod.prefix("textures/item/clock_display.png"))));
	public static final RegistryObject<ItemDisplayType> MOON_DIAL = DISPLAYS.register("moon_dial", () -> new ItemDisplayType(stack -> stack.is(TFItems.MOON_DIAL.get()), Optional.of(TwilightForestMod.prefix("textures/item/moon_dial_display.png"))));

	private ItemDisplays() {
	}
}
