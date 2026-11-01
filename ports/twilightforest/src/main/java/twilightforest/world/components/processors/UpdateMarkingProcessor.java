package twilightforest.world.components.processors;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureProcessors;

import java.util.Arrays;
import java.util.List;

// Marks the listed blocks for post-processing, so fences, walls, bars and stairs connect to their neighbours once the chunk is finished
public class UpdateMarkingProcessor extends StructureProcessor {
	public static final Codec<UpdateMarkingProcessor> CODEC = BuiltInRegistries.BLOCK.byNameCodec().listOf().xmap(UpdateMarkingProcessor::new, p -> p.blocksToMarkUpdate).fieldOf("mark_updates").codec();

	private final List<Block> blocksToMarkUpdate;

	public static UpdateMarkingProcessor forBlocks(Block... blocks) {
		return new UpdateMarkingProcessor(Arrays.asList(blocks));
	}

	public UpdateMarkingProcessor(List<Block> blocksToMarkUpdate) {
		this.blocksToMarkUpdate = blocksToMarkUpdate;
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos piecePos, StructureTemplate.StructureBlockInfo originalInfo, StructureTemplate.StructureBlockInfo modifiedInfo, StructurePlaceSettings placeSettings, @Nullable StructureTemplate template) {
		if (this.blocksToMarkUpdate.contains(modifiedInfo.state().getBlock())) {
			level.getChunk(modifiedInfo.pos()).markPosForPostprocessing(modifiedInfo.pos());
		}

		return modifiedInfo;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.UPDATE_MARKING_PROCESSOR.get();
	}
}
