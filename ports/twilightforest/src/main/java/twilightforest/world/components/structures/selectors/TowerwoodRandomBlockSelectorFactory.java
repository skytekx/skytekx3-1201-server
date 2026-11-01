package twilightforest.world.components.structures.selectors;

import com.mojang.datafixers.util.Pair;
import twilightforest.init.TFBlocks;
import twilightforest.world.components.structures.util.SimpleRandomBlockSelector;

import java.util.List;

/**
 * Creates a fresh selector per call: a BlockSelector carries mutable state, so sharing one instance across
 * concurrently generating chunks is not safe. Blocks are resolved lazily, never in a static initialiser.
 */
public final class TowerwoodRandomBlockSelectorFactory {
	private TowerwoodRandomBlockSelectorFactory() {
	}

	public static SimpleRandomBlockSelector make() {
		return new SimpleRandomBlockSelector(
			List.of(
				Pair.of(TFBlocks.CRACKED_TOWERWOOD.get().defaultBlockState(), 0.1F),
				Pair.of(TFBlocks.MOSSY_TOWERWOOD.get().defaultBlockState(), 0.1F),
				Pair.of(TFBlocks.INFESTED_TOWERWOOD.get().defaultBlockState(), 0.025F),
				Pair.of(TFBlocks.TOWERWOOD.get().defaultBlockState(), 0.775F)
			)
		);
	}
}
