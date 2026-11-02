package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.util.LandmarkStructure;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Datapack structure type {@code twilightforest:landmark}: wraps any structure (for example a vanilla or modded jigsaw
 * structure) and gives it Twilight Forest landmark decoration clearance. Generation, biomes, spawn overrides, step and
 * terrain adaptation all come from the wrapped structure.
 * <pre>{@code
 * {
 *   "type": "twilightforest:landmark",
 *   "wrapped": { "type": "minecraft:jigsaw", ... },
 *   "decoration_clearance": {                      // optional, every decoration is allowed if absent
 *     "chunk_clearance_radius": 1.0,               // 0 to 8; 0 clears around each structure piece instead of a square
 *     "allow_biome_surface_decorations": false,
 *     "allow_biome_underground_decorations": true,
 *     "allow_biome_vegetation": true,              // optional, defaults to true
 *     "adjust_structure_elevation": false
 *   },
 *   "center_in_chunk": true                        // optional, unused by the wrapper
 * }
 * }</pre>
 * 4.8's {@code structure_icon} field is not supported on 1.20.1 (there is no map decoration type registry) and is ignored.
 */
public class LandmarkWrappedStructure extends LandmarkStructure {
    public static final Codec<LandmarkWrappedStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Structure.DIRECT_CODEC.fieldOf("wrapped").forGetter(o -> o.wrappedStructure),
            DecorationConfig.CODEC.optionalFieldOf("decoration_clearance").forGetter(o -> o.optionalDecorationConfig),
            Codec.BOOL.optionalFieldOf("center_in_chunk", true).forGetter(o -> o.centerInChunk)
    ).apply(instance, LandmarkWrappedStructure::new));

    // What 4.8 falls back to without a decoration_clearance block: clearance radius 1, all decorations allowed, no elevation adjustment
    public static final DecorationConfig DEFAULT_DECORATION_CONFIG = new DecorationConfig(1, true, true, false);

    private final Structure wrappedStructure;
    private final Optional<DecorationConfig> optionalDecorationConfig;
    private final boolean centerInChunk;

    public LandmarkWrappedStructure(Structure wrappedStructure, Optional<DecorationConfig> decorationConfig, boolean centerInChunk) {
        super(decorationConfig.orElse(DEFAULT_DECORATION_CONFIG), placeholderSettings());
        this.wrappedStructure = wrappedStructure;
        this.optionalDecorationConfig = decorationConfig;
        this.centerInChunk = centerInChunk;
    }

    // The wrapper's own settings are never read: biomes(), spawnOverrides(), step() and terrainAdaptation() delegate to the wrapped structure
    public static StructureSettings placeholderSettings() {
        return new StructureSettings(HolderSet.direct(List.of()), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return this.wrappedStructure.findGenerationPoint(context);
    }

    //not used since we override findGenerationPoint
    @Override
    protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
        return null;
    }

    @Override
    public HolderSet<Biome> biomes() {
        return this.wrappedStructure.biomes();
    }

    @Override
    public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
        return this.wrappedStructure.spawnOverrides();
    }

    @Override
    public GenerationStep.Decoration step() {
        return this.wrappedStructure.step();
    }

    @Override
    public TerrainAdjustment terrainAdaptation() {
        return this.wrappedStructure.terrainAdaptation();
    }

    @Override
    public StructureType<?> type() {
        return TFStructureTypes.LANDMARK_WRAPPED.get();
    }
}
