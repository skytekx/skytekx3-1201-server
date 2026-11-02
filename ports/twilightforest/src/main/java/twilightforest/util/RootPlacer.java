package twilightforest.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;

/**
 * A root block placer paired with how many blocks deep (counting the traced position itself) every root
 * placement is checked. Placers that also write the block below the traced position use a penetrability of 2,
 * so that block is validated as well instead of being overwritten blindly.
 */
public class RootPlacer {
	private final BiConsumer<BlockPos, BlockState> rootPlacer;
	private final int rootPenetrability;

	public RootPlacer(BiConsumer<BlockPos, BlockState> placer, int penetrability) {
		this.rootPlacer = placer;
		this.rootPenetrability = penetrability;
	}

	public BiConsumer<BlockPos, BlockState> getPlacer() {
		return this.rootPlacer;
	}

	public int getRootPenetrability() {
		return this.rootPenetrability;
	}
}
