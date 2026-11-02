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
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.*;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFStructureTypes;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.structures.CustomDensitySource;
import twilightforest.world.components.structures.HedgeMazeComponent;
import twilightforest.world.components.structures.util.DecorationClearance;
import twilightforest.world.components.structures.util.LandmarkStructure;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class HedgeMazeStructure extends LandmarkStructure implements CustomDensitySource {
    public static final Codec<HedgeMazeStructure> CODEC = RecordCodecBuilder.create(instance -> landmarkCodec(instance).apply(instance, HedgeMazeStructure::new));

    public HedgeMazeStructure(DecorationConfig decorationConfig, StructureSettings structureSettings) {
        super(decorationConfig, structureSettings);
    }

    @Override
    protected StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
        return new HedgeMazeComponent(0, x + 1, y + 4, z + 1);
    }

    @Override
    public int adjustForTerrain(GenerationContext context, int x, int z) {
        return this.getSurfaceHeight(context.chunkGenerator(), context.heightAccessor(), context.randomState(), x, z);
    }

    /**
     * First air level of the ground ChunkGeneratorTwilight levels for a maze centred in the chunk holding (x, z).
     * The maze floor sits on it, so it has to match the height {@link #adjustForTerrain} places the maze at.
     */
    public int getArenaGroundLevel(ChunkGenerator generator, LevelHeightAccessor heightAccessor, RandomState randomState, int x, int z) {
        return this.getSurfaceHeight(generator, heightAccessor, randomState, x, z) + 1;
    }

    // Top of the ground under the maze. TF 4.8 samples 4x4 points over the maze and 24 blocks around its centre chunk, which is the area ChunkGeneratorTwilight levels.
    private int getSurfaceHeight(ChunkGenerator generator, LevelHeightAccessor heightAccessor, RandomState randomState, int x, int z) {
        if (!this.shouldAdjustToTerrain())
            return generator.getSeaLevel() + 4; // TF 4.3's fixed height: the maze stands on sea level + 5

        // The TF generator's base height skips water, so keep the maze from sinking to the bed of a lake or stream it overlaps
        return Math.max(generator.getSeaLevel() + 1, WorldUtil.adjustForTerrain(generator, heightAccessor, randomState, x, z, 24, 4));
    }

    /**
     * Clears the terrain above the maze floor in an inverted pyramid, so that no hill overhangs or buries the maze (TF 4.8). Below the
     * floor the terrain is left alone.
     */
    @Override
    public DensityFunction getStructureTerraformer(ChunkPos chunkPosAt, StructureStart structurePieceSource) {
        return CustomDensitySource.getInvertedPyramidTerraformer(structurePieceSource, 0, 4);
    }

    @Override
    public StructureType<?> type() {
        return TFStructureTypes.HEDGE_MAZE.get();
    }

    public static HedgeMazeStructure buildStructureConfig(BootstapContext<Structure> context) {
        return new HedgeMazeStructure(
                new DecorationClearance.DecorationConfig(2, false, true, true),
                new Structure.StructureSettings(
                        context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_HEDGE_MAZE_BIOMES),
                        Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), // Landmarks have Controlled Mob spawning
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.BEARD_BOX
                )
        );
    }
}
