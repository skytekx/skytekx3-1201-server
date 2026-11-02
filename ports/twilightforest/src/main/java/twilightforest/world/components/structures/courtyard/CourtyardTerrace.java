package twilightforest.world.components.structures.courtyard;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.common.world.PieceBeardifierModifier;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.world.components.processors.NagastoneVariants;
import twilightforest.world.components.processors.StoneBricksVariants;
import twilightforest.world.components.structures.TwilightTemplateStructurePiece;

public class CourtyardTerrace extends TwilightTemplateStructurePiece implements PieceBeardifierModifier {
	// TF 4.3 saved its terraces (TFNCTr brazier, TFNCDu duct, TFNCSt statue) 3 blocks above the spot they occupy and dug them in while placing
	private static final int LEGACY_DIG_IN = -3;

	private final boolean legacyDugIn;

	public CourtyardTerrace(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFNCTe.get(), nbt, ctx, readSettings(nbt).addProcessor(CourtyardTerraceTemplateProcessor.INSTANCE).addProcessor(NagastoneVariants.INSTANCE).addProcessor(StoneBricksVariants.INSTANCE));
		this.legacyDugIn = false;
	}

	public CourtyardTerrace(int i, int x, int y, int z, Rotation rotation, StructureTemplateManager structureManager, ResourceLocation templateLocation) {
		super(TFStructurePieceTypes.TFNCTe.get(), i, structureManager, templateLocation, makeSettings(rotation).addProcessor(CourtyardTerraceTemplateProcessor.INSTANCE).addProcessor(NagastoneVariants.INSTANCE).addProcessor(StoneBricksVariants.INSTANCE), new BlockPos(x, y, z));
		this.legacyDugIn = false;
	}

	protected CourtyardTerrace(StructurePieceType structurePieceType, StructurePieceSerializationContext ctx, CompoundTag nbt) {
		this(structurePieceType, ctx, nbt, false);
	}

	private CourtyardTerrace(StructurePieceType structurePieceType, StructurePieceSerializationContext ctx, CompoundTag nbt, boolean legacyDugIn) {
		super(structurePieceType, nbt, ctx, readSettings(nbt).addProcessor(CourtyardTerraceTemplateProcessor.INSTANCE).addProcessor(NagastoneVariants.INSTANCE).addProcessor(StoneBricksVariants.INSTANCE));
		this.legacyDugIn = legacyDugIn;
	}

	public CourtyardTerrace(StructurePieceType structurePieceType, int i, int x, int y, int z, Rotation rotation, StructureTemplateManager structureManager, ResourceLocation templateLocation) {
		super(structurePieceType, i, structureManager, templateLocation, makeSettings(rotation).addProcessor(CourtyardTerraceTemplateProcessor.INSTANCE).addProcessor(NagastoneVariants.INSTANCE).addProcessor(StoneBricksVariants.INSTANCE), new BlockPos(x, y, z));
		this.legacyDugIn = false;
	}

	/**
	 * Loads a terrace of a courtyard saved by TF 4.3 under one of its old piece types (TFNCTr, TFNCDu or TFNCSt). TF 4.8 drops those
	 * types through a registry alias to TFNCTe, which Forge 47 does not offer for structure piece types, so they stay registered and
	 * load here. The piece keeps its old type, and with it the old position 3 blocks above the terrace, when its structure start is saved again.
	 */
	public static CourtyardTerrace loadLegacy(StructurePieceType legacyType, StructurePieceSerializationContext ctx, CompoundTag nbt) {
		return new CourtyardTerrace(legacyType, ctx, nbt, true);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGen, RandomSource random, BoundingBox chunkBounds, ChunkPos chunkPos, BlockPos structureBottomCenter) {
		if (this.legacyDugIn) {
			this.placePieceAdjusted(level, structureManager, chunkGen, random, chunkBounds, chunkPos, structureBottomCenter, LEGACY_DIG_IN);
		} else {
			super.postProcess(level, structureManager, chunkGen, random, chunkBounds, chunkPos, structureBottomCenter);
		}
	}

	@Override
	protected void handleDataMarker(String label, BlockPos pos, ServerLevelAccessor levelAccessor, RandomSource random, BoundingBox boundingBox) {
	}

	@Override
	public BoundingBox getBeardifierBox() {
		return this.boundingBox;
	}

	@Override
	public TerrainAdjustment getTerrainAdjustment() {
		// A TF 4.3 terrace keeps the courtyard-wide beard_thin that the Beardifier gave it before, on its raised box
		return this.legacyDugIn ? TerrainAdjustment.BEARD_THIN : TerrainAdjustment.BEARD_BOX;
	}

	@Override
	public int getGroundLevelDelta() {
		return this.legacyDugIn ? 0 : 3;
	}
}
