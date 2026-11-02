package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFStructureTypes;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.structures.TwilightJigsawPiece;
import twilightforest.world.components.structures.camp.CampPieces;
import twilightforest.world.components.structures.util.DecorationClearance;
import twilightforest.world.components.structures.util.StructureTemplateDefinitions;

import java.util.Map;
import java.util.Optional;

/**
 * Traveller camps (TF 4.8): a small jigsaw surface structure grown from the twilightforest:camp/structure_start template pool. The pools
 * are data driven (twilight/template_definition/camp/), the data markers inside the templates are handled by
 * twilightforest:camp_marker_handlers.
 */
public class CampStructure extends Structure implements DecorationClearance {
	public static final Codec<CampStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Structure.settingsCodec(instance)
	).apply(instance, CampStructure::new));

	protected CampStructure(StructureSettings settings) {
		super(settings);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		WorldgenRandom random = context.random();

		int blockXCenter = WorldUtil.lerpDiscrete(random.nextFloat(), chunkPos.getMinBlockX(), chunkPos.getMaxBlockX());
		int blockZCenter = WorldUtil.lerpDiscrete(random.nextFloat(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ());
		// Weighted, high-biased average of a 3x3 height grid over the start chunk widened by 12 blocks, instead of the centre column alone
		int topFreeY = WorldUtil.adjustForTerrain(context, blockXCenter, blockZCenter, 12, 3);
		BlockPos freePosition = new BlockPos(blockXCenter, topFreeY, blockZCenter);

		Direction direction = Rotation.getRandom(random).rotate(Direction.SOUTH);
		FrontAndTop oriented = FrontAndTop.fromFrontAndTop(Direction.UP, direction);

		return Optional.of(new GenerationStub(freePosition, structurePiecesBuilder -> {
			ResourceLocation templatePool = CampPieces.START;
			TwilightJigsawPiece twilightJigsawPiece = StructureTemplateDefinitions.INSTANCE.initializeTemplateFromPool(templatePool, freePosition, oriented, templatePool.toString(), context, 0, false);

			if (twilightJigsawPiece == null) return;

			structurePiecesBuilder.addPiece(twilightJigsawPiece);

			twilightJigsawPiece.addJigsaws(twilightJigsawPiece, structurePiecesBuilder, context);
		}));
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.CAMP.get();
	}

	public static CampStructure buildStructureConfig(BootstapContext<Structure> context) {
		return new CampStructure(new StructureSettings(
			context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_CAMP_BIOMES),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			TerrainAdjustment.BEARD_BOX
		));
	}

	@Override
	public float chunkClearanceRadius() {
		return 1;
	}

	@Override
	public boolean isSurfaceDecorationsAllowed() {
		return false;
	}

	@Override
	public boolean isUndergroundDecoAllowed() {
		return true;
	}

	@Override
	public boolean isGrassDecoAllowed() {
		return true;
	}

	@Override
	public boolean shouldAdjustToTerrain() {
		return false;
	}
}
