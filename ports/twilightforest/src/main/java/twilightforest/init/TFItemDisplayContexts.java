package twilightforest.init;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;

/**
 * Custom {@link ItemDisplayContext}s. 4.8 adds them through NeoForge's enum extensions; Forge 47 extends the enum with
 * {@link ItemDisplayContext#create} and registers the result in its display context registry.
 */
public final class TFItemDisplayContexts {

	public static final DeferredRegister<ItemDisplayContext> DISPLAY_CONTEXTS = DeferredRegister.create(ForgeRegistries.Keys.DISPLAY_CONTEXTS, TwilightForestMod.ID);

	/**
	 * Items displayed inside a Mason Jar. Item models set it with the {@code twilightforest:jarred} display key; models
	 * without one fall back to {@link ItemDisplayContext#FIXED}.
	 */
	public static final ItemDisplayContext JARRED = ItemDisplayContext.create("TWILIGHTFOREST_JARRED", TwilightForestMod.prefix("jarred"), ItemDisplayContext.FIXED);

	public static final RegistryObject<ItemDisplayContext> JARRED_ENTRY = DISPLAY_CONTEXTS.register("jarred", () -> JARRED);

	private TFItemDisplayContexts() {
	}
}
