package twilightforest.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.List;

/**
 * Twilight Forest key bindings. Backport of 4.8's TFKeyBinds and TFKeyBindsCategories, kept in the client package so a
 * dedicated server never loads KeyMapping. The Traveller's Gear features add their keys (zoom, red thread vision, swap
 * hotbar, item display map cycle) here with {@link #register}, in the {@link #TRAVELLERS_GEAR} category.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TFKeyBinds {
	// must stay above the key fields, which register themselves while the class initializes
	private static final List<KeyMapping> KEY_MAPPINGS = new ArrayList<>();

	public static final String TRAVELLERS_GEAR = categoryName("travellers_gear");

	private TFKeyBinds() {
	}

	static KeyMapping register(String name, InputConstants.Type type, int key, String category) {
		KeyMapping keyMapping = new KeyMapping("key." + TwilightForestMod.ID + "." + name, type, key, category);
		KEY_MAPPINGS.add(keyMapping);
		return keyMapping;
	}

	private static String categoryName(String name) {
		return "key." + TwilightForestMod.ID + ".categories." + name;
	}

	/**
	 * The key bound to the Twilight Forest key mapping {@code name}, with its modifier, for the
	 * {@code ${tfkeybinds/<name>}} placeholders of the Traveller's modifier names (4.8 TooltipStringInterpolator).
	 */
	public static String getBoundKeyName(String name) {
		return KEY_MAPPINGS.stream()
			.filter(keyMapping -> keyMapping.getName().equals(name))
			.findFirst()
			.map(keyMapping -> {
				InputConstants.Key key = keyMapping.getKey();
				return keyMapping.getKeyModifier().getCombinedName(key, key::getDisplayName).getString();
			})
			.orElse("Nonexistent key");
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		KEY_MAPPINGS.forEach(event::register);
	}
}
