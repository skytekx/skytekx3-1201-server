package de.ellpeck.actuallyadditions.mod.patchouli;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.resources.ResourceLocation;
import vazkii.patchouli.client.book.ClientBookRegistry;

// SkyTekx: upstream 1207a27d. Client only, and only called when Patchouli is loaded (a20c25eb)
public class PatchouliPages {
	public static void init() {
		ClientBookRegistry.INSTANCE.pageTypes.put(new ResourceLocation(ActuallyAdditions.MODID, "reconstructor"), PageReconstructor.class);
	}
}
