package twilightforest.world.components.feature.trees.growers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import twilightforest.world.components.structures.TreeGrowerStartable;

/**
 * Grows a {@link TreeGrowerStartable} structure instead of a configured feature, e.g. the Giant Hollow Oak from a Robust Twilight Oak Sapling
 */
public class StructureTreeGrower extends AbstractTreeGrower {
	private final ResourceKey<Structure> structureKey;

	public StructureTreeGrower(ResourceKey<Structure> structureKey) {
		this.structureKey = structureKey;
	}

	// Never used, growTree places the structure
	@Nullable
	@Override
	protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
		return null;
	}

	//copied from PlaceCommand.placeStructure
	@Override
	public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random) {
		// 4.8 uses getOrThrow; a datapack that removes the structure must not crash the server on a sapling's random tick
		Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(this.structureKey);

		if (!(structure instanceof TreeGrowerStartable treeGrowerStartable))
			return false;

		// AbstractTreeGrower.growTree fires this for configured features; keep firing it (without a feature) so other mods can still deny the growth, as they could with 4.3's feature-grown Hollow Oak
		if (ForgeEventFactory.blockGrowFeature(level, random, pos, null).getResult() == Event.Result.DENY)
			return false;

		if (!treeGrowerStartable.checkSaplingClearance(level, pos))
			return false;

		StructureStart structurestart = treeGrowerStartable.generateFromSapling(level.registryAccess(), generator, generator.getBiomeSource(), level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), pos, level);

		if (!structurestart.isValid())
			return false;

		BoundingBox boundingbox = structurestart.getBoundingBox();
		ChunkPos start = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.minX()), SectionPos.blockToSectionCoord(boundingbox.minZ()));
		ChunkPos end = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.maxX()), SectionPos.blockToSectionCoord(boundingbox.maxZ()));

		// 4.8 only gives up when none of the covered chunks is loaded; like vanilla PlaceCommand, give up when any of them is unloaded,
		// so a sapling ticking near the edge of the simulation distance waits instead of loading/generating chunks on the server thread
		if (ChunkPos.rangeClosed(start, end).anyMatch(currentChunkPos -> !level.isLoaded(currentChunkPos.getWorldPosition())))
			return false;

		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

		ChunkPos.rangeClosed(start, end).forEach(
			chunkPos -> structurestart.placeInChunk(
				level,
				level.structureManager(),
				generator,
				level.getRandom(),
				new BoundingBox(
					chunkPos.getMinBlockX(),
					level.getMinBuildHeight(),
					chunkPos.getMinBlockZ(),
					chunkPos.getMaxBlockX(),
					level.getMaxBuildHeight(),
					chunkPos.getMaxBlockZ()
				),
				chunkPos
			)
		);

		return true;
	}
}
