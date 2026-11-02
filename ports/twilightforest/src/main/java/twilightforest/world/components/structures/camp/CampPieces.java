package twilightforest.world.components.structures.camp;

import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;

// Template pool ids of the Traveller camps. TF 4.8 keeps them in a Beanification @Component; plain constants here
public final class CampPieces {
	public static final ResourceLocation START = TwilightForestMod.prefix("camp/structure_start");
	public static final ResourceLocation TENT = TwilightForestMod.prefix("camp/tent");
	public static final ResourceLocation RACK_PATH = TwilightForestMod.prefix("camp/rack_path");
	public static final ResourceLocation MAIN_PATH = TwilightForestMod.prefix("camp/main_path");
	public static final ResourceLocation PATH = TwilightForestMod.prefix("camp/path");
	public static final ResourceLocation DECO = TwilightForestMod.prefix("camp/deco");
	public static final ResourceLocation RACK = TwilightForestMod.prefix("camp/rack");

	private CampPieces() {
	}
}
