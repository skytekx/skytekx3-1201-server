package twilightforest.world.components.structures.markerhandler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import twilightforest.init.custom.TemplateMarkerHandlers;

// Places a container block and gives it a loot table. TF 4.8 takes a ResourceKey<LootTable>; on 1.20.1 the loot table is a plain id in the
// same JSON format. Only RandomizableContainerBlockEntity (chests, barrels, shulker boxes, hoppers, dispensers, TF chests) takes a loot table
// here: 1.20.1 decorated pots have no inventory, so a pot is placed empty and the marker counts as not handled
public record LootMarkerHandler(BlockStateProvider provider, ResourceLocation lootTable) implements TemplateMarkerHandler {
	public static final Codec<LootMarkerHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockStateProvider.CODEC.fieldOf("loot_block").forGetter(LootMarkerHandler::provider),
		ResourceLocation.CODEC.fieldOf("loot_table").forGetter(LootMarkerHandler::lootTable)
	).apply(instance, LootMarkerHandler::new));

	@Override
	public boolean handleDataMarker(String label, BlockPos pos, WorldGenLevel level, RandomSource random, BoundingBox chunkBounds, ChunkGenerator chunkGen, Rotation rotation) {
		BlockState state = this.provider.getState(random, pos).rotate(rotation);

		if (level.setBlock(pos, state, Block.UPDATE_ALL) && level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
			// RandomizableContainer.setBlockEntityLootTable() of MC 1.20.3+, which seeds the loot table the same way
			container.setLootTable(this.lootTable, random.nextLong());

			return true;
		}

		return false;
	}

	@Override
	public TemplateMarkerHandlerType getType() {
		return TemplateMarkerHandlers.LOOT.get();
	}
}
