package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFStructureTypes;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.structures.courtyard.CourtyardMain;
import twilightforest.world.components.structures.util.ConquerableStructure;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class NagaCourtyardStructure extends ConquerableStructure {
    public static final Codec<NagaCourtyardStructure> CODEC = RecordCodecBuilder.create(instance -> landmarkCodec(instance).apply(instance, NagaCourtyardStructure::new));

    public NagaCourtyardStructure(DecorationConfig decorationConfig, StructureSettings structureSettings) {
        super(decorationConfig, structureSettings);
    }

    @Override
    protected StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
        return new CourtyardMain(random, 0, x + 1, y, z + 1, context.structureTemplateManager());
    }

    @Override
    public int adjustForTerrain(GenerationContext context, int x, int z) {
        return this.getArenaGroundLevel(context.chunkGenerator(), context.heightAccessor(), context.randomState(), x, z);
    }

    /**
     * First air level of the ground ChunkGeneratorTwilight levels for a courtyard centred in the chunk holding (x, z).
     * The courtyard is placed at exactly this height by {@link #adjustForTerrain}.
     */
    public int getArenaGroundLevel(ChunkGenerator generator, LevelHeightAccessor heightAccessor, RandomState randomState, int x, int z) {
        if (!this.shouldAdjustToTerrain())
            return generator.getSeaLevel() + 5; // TF 4.3's fixed height

        // TF 4.8 samples 4x4 points over the courtyard and 40 blocks around its centre chunk, which is the area ChunkGeneratorTwilight levels, and lifts it by 2.
        // The TF generator's base height skips water, so keep the courtyard from sinking to the bed of a lake or stream it overlaps.
        return Math.max(generator.getSeaLevel() + 1, WorldUtil.adjustForTerrain(generator, heightAccessor, randomState, x, z, 40, 4)) + 2;
    }

    @Override
    public StructureType<?> type() {
        return TFStructureTypes.NAGA_COURTYARD.get();
    }

    public static NagaCourtyardStructure buildStructureConfig(BootstapContext<Structure> context) {
        return new NagaCourtyardStructure(
                new DecorationConfig(3, false, true, true),
                new StructureSettings(
                        context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_NAGA_COURTYARD_BIOMES),
                        Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), // Landmarks have Controlled Mob spawning
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                )
        );
    }
}
