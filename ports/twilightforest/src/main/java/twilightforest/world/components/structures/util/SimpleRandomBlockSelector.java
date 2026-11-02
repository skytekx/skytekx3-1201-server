package twilightforest.world.components.structures.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import twilightforest.util.WorldUtil;

import java.util.List;

/**
 * A weighted {@link StructurePiece.BlockSelector}: walls pick one of the weighted states, the interior is air.
 * Unlike the old hand-written selectors it also exposes every state it can place (see {@link #getStates()}),
 * which the Knight Stronghold ruins use to decide whether they may keep stacking.
 */
public class SimpleRandomBlockSelector extends StructurePiece.BlockSelector {
	protected final List<Pair<BlockState, Float>> blockStatesAndWeights;
	protected final List<BlockState> blockStates;

	public SimpleRandomBlockSelector(List<Pair<BlockState, Float>> blockStatesAndWeights) {
		this.blockStatesAndWeights = blockStatesAndWeights;
		this.blockStates = blockStatesAndWeights.stream().map(Pair::getFirst).toList();
	}

	@Override
	public void next(RandomSource random, int x, int y, int z, boolean wall) {
		this.next(random, wall);
	}

	public void next(RandomSource random, boolean wall) {
		this.next = wall ? WorldUtil.getRandomElementWithWeights(this.blockStatesAndWeights, random) : Blocks.AIR.defaultBlockState();
	}

	public List<BlockState> getStates() {
		return this.blockStates;
	}
}
