package twilightforest.world.components.structures.util;

import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.Nullable;
import twilightforest.world.components.structures.TFStructureComponentTemplate;

import java.util.Comparator;
import java.util.Optional;

// Landmark structure without progression lock; Hollow Hills/Hedge Maze/Naga Courtyard/Quest Grove
public abstract class LandmarkStructure extends Structure implements DecorationClearance {
    protected static <S extends LandmarkStructure> Products.P2<RecordCodecBuilder.Mu<S>, DecorationConfig, StructureSettings> landmarkCodec(RecordCodecBuilder.Instance<S> instance) {
        return instance.group(
                DecorationConfig.FLAT_CODEC.forGetter(s -> s.decorationConfig),
                Structure.settingsCodec(instance)
        );
    }

    protected final DecorationConfig decorationConfig;

    public LandmarkStructure(DecorationConfig decorationConfig, StructureSettings structureSettings) {
        super(structureSettings);
        this.decorationConfig = decorationConfig;
    }

    private Structure.GenerationStub getStructurePieceGenerationStubFunction(StructurePiece startingPiece, GenerationContext context, int x, int y, int z) {
        return new GenerationStub(new BlockPos(x, y, z), structurePiecesBuilder -> {
            this.generateFromStartingPiece(startingPiece, context, structurePiecesBuilder);

            // Pieces place in list order, so later pieces overwrite the blocks of earlier ones (TF 4.8 Lich Tower).
            // List.sort is stable: pieces that are not SortablePiece keep their order, so the other structures generate as before
            structurePiecesBuilder.pieces.sort(Comparator.comparing(piece -> piece instanceof SortablePiece sortable ? sortable.getSortKey() : 0));

            structurePiecesBuilder.pieces.stream()
                    .filter(TFStructureComponentTemplate.class::isInstance)
                    .map(TFStructureComponentTemplate.class::cast)
                    .forEach(t -> t.LAZY_TEMPLATE_LOADER.run());
        });
    }

    protected void generateFromStartingPiece(StructurePiece startingPiece, GenerationContext context, StructurePiecesBuilder structurePiecesBuilder) {
        structurePiecesBuilder.addPiece(startingPiece);
        startingPiece.addChildren(startingPiece, structurePiecesBuilder, context.random());
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();

        boolean dontCenter = this.dontCenter();
        int x = (chunkPos.x << 4) + (dontCenter ? 0 : 7);
        int z = (chunkPos.z << 4) + (dontCenter ? 0 : 7);
        int y = this.adjustForTerrain(context, x, z);

        return Optional
                .ofNullable(this.getFirstPiece(context, RandomSource.create(context.seed() + chunkPos.x * 25117L + chunkPos.z * 151121L), chunkPos, x, y, z))
                .map(piece -> getStructurePieceGenerationStubFunction(piece, context, x, y, z))
                ;
    }

    @Deprecated
    protected boolean dontCenter() {
        return false;
    }

    @Nullable
    protected abstract StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z);

    @Override
    public boolean isSurfaceDecorationsAllowed() {
        return this.decorationConfig.surfaceDecorations();
    }

    @Override
    public boolean isUndergroundDecoAllowed() {
        return this.decorationConfig.undergroundDecorations();
    }

    @Override
    public boolean isGrassDecoAllowed() {
        return this.decorationConfig.vegetation();
    }

    @Override
    public boolean shouldAdjustToTerrain() {
        return this.decorationConfig.adjustElevation();
    }

    @Override
    public float chunkClearanceRadius() {
        return this.decorationConfig.chunkClearanceRadius();
    }
}
