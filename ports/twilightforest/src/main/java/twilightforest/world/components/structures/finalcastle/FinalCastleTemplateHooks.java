package twilightforest.world.components.structures.finalcastle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.FrontAndTop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import twilightforest.world.components.structures.TwilightJigsawPiece;
import twilightforest.world.components.structures.util.StructureTemplateDefinitions;

/**
 * The template hooks TF 4.8 added to the Final Castle "for the modpackers" (72c7e5cf2, 5fdb2cfe6): the bell tower, the large tower and the
 * boss gazebo each attach a template from a final_castle/temp/* pool, so a datapack can replace their contents (for example with a boss arena).
 */
public final class FinalCastleTemplateHooks {
	/**
	 * Saved as true by the hooked pieces of castles generated since the hooks exist. Pieces saved before have no template piece and keep
	 * placing what the template now places (floors, signs and the gazebo) themselves.
	 */
	static final String NBT_KEY = "template_hooks";

	private FinalCastleTemplateHooks() {
	}

	// TF 4.8 takes the template manager from ServerLifecycleHooks.getCurrentServer() as well, since the old-style castle pieces never receive it
	@Nullable
	static TwilightJigsawPiece initializeTemplateFromPool(ResourceLocation templatePool, BlockPos parentJunctionPos, FrontAndTop parentOrientation, String selectName, RandomSource rand, int genDepth) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return null;

		return StructureTemplateDefinitions.INSTANCE.initializeTemplateFromPool(templatePool, parentJunctionPos, parentOrientation, selectName, rand, genDepth, server.getStructureManager());
	}
}
