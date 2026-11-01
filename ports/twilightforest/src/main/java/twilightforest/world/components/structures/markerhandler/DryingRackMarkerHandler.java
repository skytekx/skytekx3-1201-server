package twilightforest.world.components.structures.markerhandler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import twilightforest.block.DryingRackBlock;
import twilightforest.block.entity.DryingRackBlockEntity;
import twilightforest.init.custom.TemplateMarkerHandlers;

// Places a drying rack and hangs an item rolled from a loot table on it. TF 4.8 takes a ResourceKey<LootTable>; on 1.20.1 the loot table is
// a plain id in the same JSON format
public record DryingRackMarkerHandler(BlockStateProvider provider, ResourceLocation lootTable) implements TemplateMarkerHandler {
	public static final Codec<DryingRackMarkerHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockStateProvider.CODEC.fieldOf("drying_rack_block").forGetter(DryingRackMarkerHandler::provider),
		ResourceLocation.CODEC.fieldOf("loot_table").forGetter(DryingRackMarkerHandler::lootTable)
	).apply(instance, DryingRackMarkerHandler::new));

	@Override
	public boolean handleDataMarker(String label, BlockPos pos, WorldGenLevel level, RandomSource random, BoundingBox chunkBounds, ChunkGenerator chunkGen, Rotation rotation) {
		BlockState state = this.provider.getState(random, pos).rotate(rotation);

		if (!(state.getBlock() instanceof DryingRackBlock) || !level.setBlock(pos, state, Block.UPDATE_ALL)) {
			return false;
		}

		if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity dryingRackBlock)) {
			return false;
		}

		return dryingRackBlock.fillFromLootTable(this.lootTable, random.nextLong(), level.getLevel());
	}

	@Override
	public TemplateMarkerHandlerType getType() {
		return TemplateMarkerHandlers.DRYING_RACK.get();
	}
}
