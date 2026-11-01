package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFEntities;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.start.TFStructureStart;
import twilightforest.world.components.structures.stronghold.StrongholdEntranceComponent;
import twilightforest.world.components.structures.util.ControlledSpawningStructure;
import twilightforest.world.registration.TFGenerationSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KnightStrongholdStructure extends ControlledSpawningStructure {
    public static final Codec<KnightStrongholdStructure> CODEC = RecordCodecBuilder.create(instance ->
            controlledSpawningCodec(instance).apply(instance, KnightStrongholdStructure::new)
    );

    public KnightStrongholdStructure(ControlledSpawningConfig controlledSpawningConfig, AdvancementLockConfig advancementLockConfig, HintConfig hintConfig, DecorationConfig decorationConfig, StructureSettings structureSettings) {
        super(controlledSpawningConfig, advancementLockConfig, hintConfig, decorationConfig, structureSettings);
    }

    @Override
    protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
        // Entrances may sit underground again: one in three keeps the old height, the rest are 4 blocks deeper
        return new StrongholdEntranceComponent(0, x, y + (random.nextInt(3) == 0 ? 5 : 1), z);
    }

    @Override
    public StructureType<?> type() {
        return TFStructureTypes.KNIGHT_STRONGHOLD.get();
    }

    public static KnightStrongholdStructure buildKnightStrongholdConfig(BootstapContext<Structure> context) {
        return new KnightStrongholdStructure(
                ControlledSpawningConfig.firstIndexMonsters(
                        new MobSpawnSettings.SpawnerData(TFEntities.BLOCKCHAIN_GOBLIN.get(), 10, 1, 2),
                        new MobSpawnSettings.SpawnerData(TFEntities.LOWER_GOBLIN_KNIGHT.get(), 5, 1, 2),
                        new MobSpawnSettings.SpawnerData(TFEntities.HELMET_CRAB.get(), 10, 2, 4),
                        new MobSpawnSettings.SpawnerData(TFEntities.SLIME_BEETLE.get(), 10, 2, 3),
                        new MobSpawnSettings.SpawnerData(TFEntities.REDCAP_SAPPER.get(), 2, 1, 2),
                        new MobSpawnSettings.SpawnerData(TFEntities.KOBOLD.get(), 10, 2, 4),
                        new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 5, 1, 2),
                        new MobSpawnSettings.SpawnerData(EntityType.SLIME, 5, 4, 4)
                ),
                new AdvancementLockConfig(List.of(TwilightForestMod.prefix("progress_trophy_pedestal"))),
                new HintConfig(HintConfig.book("tfstronghold", 4), TFEntities.KOBOLD.get()),
                new DecorationConfig(3, true, false, false),
                new StructureSettings(
                        context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_KNIGHT_STRONGHOLD_BIOMES),
                        Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), // Landmarks have Controlled Mob spawning
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.BURY
                )
        );
    }

    @Override
    protected StructureStart createStart(ChunkPos chunkPos, int references, GenerationStub generationStub) {
        KnightStructureStart start = new KnightStructureStart(this, chunkPos, references, generationStub.getPiecesBuilder().build());
        start.setStartY(generationStub.position().getY() - 1);
        return start;
    }

    /**
     * The stronghold's ruins and access chambers poke out above ground, so the start's bounding box (used for the
     * progression lock effect, protection and controlled spawning) is clipped to just below the generation height.
     * That height is persisted as "knight_y" so the clipped box survives a save/load cycle (#2138).
     */
    public static class KnightStructureStart extends TFStructureStart {
        public static final String KNIGHT_Y_TAG = "knight_y";

        private int startY = 0;
        @Nullable
        private volatile BoundingBox clippedBoundingBox;

        public KnightStructureStart(Structure structure, ChunkPos chunkPos, int references, PiecesContainer pieces) {
            super(structure, chunkPos, references, pieces);
        }

        public void setStartY(int startY) {
            this.startY = startY;
            this.clippedBoundingBox = null;
        }

        @Override
        public BoundingBox getBoundingBox() {
            BoundingBox boundingBox = this.clippedBoundingBox;
            if (boundingBox == null) {
                BoundingBox bBox = super.getBoundingBox();
                // Never invert the box: if nothing lies below the clip height, keep the full box
                boundingBox = bBox.minY() > this.startY ? bBox : new BoundingBox(bBox.minX(), bBox.minY(), bBox.minZ(), bBox.maxX(), Math.min(bBox.maxY(), this.startY), bBox.maxZ());
                this.clippedBoundingBox = boundingBox; // Cached, since this may get called every tick
            }
            return boundingBox;
        }

        @Override
        public CompoundTag createTag(StructurePieceSerializationContext context, ChunkPos chunkPos) {
            CompoundTag tag = super.createTag(context, chunkPos);
            tag.putInt(KNIGHT_Y_TAG, this.startY);
            return tag;
        }

        @Override
        public void load(CompoundTag nbt) {
            super.load(nbt);
            this.setStartY(nbt.contains(KNIGHT_Y_TAG, Tag.TAG_ANY_NUMERIC) ? nbt.getInt(KNIGHT_Y_TAG) : this.getLegacyStartY());
        }

        /**
         * Starts saved before "knight_y" existed always placed their entrance (the first piece) with its floor
         * 6 blocks below the generation height, so the clip height can be recovered exactly from it.
         */
        private int getLegacyStartY() {
            List<StructurePiece> pieces = this.getPieces();
            if (!pieces.isEmpty() && pieces.get(0) instanceof StrongholdEntranceComponent entrance)
                return entrance.getBoundingBox().minY() + 5;
            return TFGenerationSettings.SEALEVEL - 1;
        }
    }
}
