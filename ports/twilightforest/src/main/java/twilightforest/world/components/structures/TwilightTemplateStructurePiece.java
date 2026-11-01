package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import twilightforest.util.ArrayUtil;
import twilightforest.util.BoundingBoxUtils;

public abstract class TwilightTemplateStructurePiece extends TemplateStructurePiece {
    protected final StructureTemplateManager structureManager;
    private final BlockPos originalPlacement;
    private final BoundingBox originalBox;

    public TwilightTemplateStructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag, StructurePieceSerializationContext ctx, StructurePlaceSettings rl2SettingsFunction) {
        super(structurePieceType, compoundTag, ctx.structureTemplateManager(), rl -> rl2SettingsFunction);
        this.rotation = this.getRotation();
        this.mirror = this.getMirror();

        this.structureManager = ctx.structureTemplateManager();

        this.originalPlacement = this.templatePosition;
        this.originalBox = BoundingBoxUtils.clone(this.boundingBox);
    }

    public TwilightTemplateStructurePiece(StructurePieceType type, int genDepth, StructureTemplateManager structureManager, ResourceLocation templateLocation, StructurePlaceSettings placeSettings, BlockPos startPosition) {
        super(type, genDepth, structureManager, templateLocation, templateLocation.toString(), placeSettings, startPosition);
        this.rotation = this.getRotation();
        this.mirror = this.getMirror();

        this.structureManager = structureManager;

        this.originalPlacement = this.templatePosition;
        this.originalBox = BoundingBoxUtils.clone(this.boundingBox);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag structureTag) {
        super.addAdditionalSaveData(ctx, structureTag);

        structureTag.putInt("rotation", this.placeSettings.getRotation().ordinal());
        structureTag.putInt("mirror", this.placeSettings.getMirror().ordinal());
        BlockPos pivot = this.placeSettings.getRotationPivot();
        structureTag.putInt("pivot_x", pivot.getX());
        structureTag.putInt("pivot_y", pivot.getY());
        structureTag.putInt("pivot_z", pivot.getZ());
    }

    // This will be required if you want to dig a piece into a noise beard
    protected void placePieceAdjusted(WorldGenLevel level, StructureManager structureFeatureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos pos, int dY) {
        this.templatePosition = this.templatePosition.above(dY);

        super.postProcess(level, structureFeatureManager, chunkGenerator, random, boundingBox, chunkPos, pos.above(dY));

        this.templatePosition = this.originalPlacement;
        this.boundingBox = BoundingBoxUtils.clone(this.originalBox);

        this.placeSettings.setBoundingBox(this.boundingBox);
    }

    public static StructurePlaceSettings readSettings(CompoundTag compoundTag) {
        return new StructurePlaceSettings()
                .setRotation(ArrayUtil.wrapped(Rotation.values(), compoundTag.getInt("rotation")))
                .setMirror(ArrayUtil.wrapped(Mirror.values(), compoundTag.getInt("mirror")))
                .setRotationPivot(new BlockPos(compoundTag.getInt("pivot_x"), compoundTag.getInt("pivot_y"), compoundTag.getInt("pivot_z")))
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    public static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings().setRotation(rotation).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    public static StructurePlaceSettings makeSettings(Rotation rotation, Mirror mirror) {
        return new StructurePlaceSettings().setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
    }

    public static StructurePlaceSettings randomRotation(RandomSource random) {
        return makeSettings(Rotation.getRandom(random));
    }

    // VANILLACOPY: TemplateStructurePiece#postProcess() without its trailing jigsaw pass, which re-parses the final_state of every jigsaw
    // and setBlock()s it with flag 3 (neighbour updates during worldgen) after the template was placed. Data markers go through the extended
    // handleDataMarker() below. Opt-in (TF 4.8 makes it the postProcess() of every template piece): pieces that do not call it keep the
    // vanilla behaviour, so 4.3 templates that still rely on that jigsaw pass are unaffected
    protected void placeTemplateWithoutJigsaws(WorldGenLevel level, ChunkGenerator chunkGen, RandomSource random, BoundingBox chunkBounds, BlockPos structureBottomCenter, int placeFlag) {
        this.placeSettings.setBoundingBox(chunkBounds);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (this.template.placeInWorld(level, this.templatePosition, structureBottomCenter, this.placeSettings, random, placeFlag)) {
            Rotation rotation = this.placeSettings.getRotation();
            for (StructureTemplate.StructureBlockInfo blockInfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
                if (blockInfo.nbt() != null) {
                    StructureMode structureMode = StructureMode.valueOf(blockInfo.nbt().getString("mode"));
                    if (structureMode == StructureMode.DATA) {
                        this.handleDataMarker(blockInfo.nbt().getString("metadata"), blockInfo.pos(), level, random, chunkBounds, chunkGen, rotation);
                    }
                }
            }
        }
    }

    // Extended TemplateStructurePiece#handleDataMarker() that up-levels ServerLevelAccessor into WorldGenLevel and adds the ChunkGenerator and
    // the piece rotation. Only placeTemplateWithoutJigsaws() calls it; by default it hands over to the vanilla-signature method
    protected void handleDataMarker(String label, BlockPos pos, WorldGenLevel level, RandomSource random, BoundingBox chunkBounds, ChunkGenerator chunkGen, Rotation rotation) {
        this.handleDataMarker(label, pos, level, random, chunkBounds);
    }


    // Worse case scenario if the terrain still isn't being risen for the Lich Tower,
    // then we'll need to do via this. I still have other solutions I'd like to explore first
    //@Override
    //public BoundingBox getBoundingBox() {
    //    return this.boundingBox = StructureBoundingBoxUtils.clone(this.originalBox);
    //}
}
