package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureTypes;
import twilightforest.world.components.structures.start.TFStructureStart;
import twilightforest.world.components.structures.util.CustomStructureData;
import twilightforest.world.components.structures.util.ProgressionStructure;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Datapack structure type {@code twilightforest:progression}: wraps any structure (for example a vanilla or modded
 * jigsaw structure) and locks it behind Twilight Forest progression like the TF landmarks: players without the required
 * advancements get the protection effects and an optional hint creature. Generation, biomes, spawn overrides, step and
 * terrain adaptation all come from the wrapped structure. The start is a {@link TFStructureStart}, so the structure can be
 * conquered.
 * <pre>{@code
 * {
 *   "type": "twilightforest:progression",
 *   "wrapped": { "type": "minecraft:jigsaw", ... },
 *   "advancements_required": [ "twilightforest:progress_lich" ],
 *   "hint_creature": {                             // optional, no hint creature spawns if absent
 *     "hint_item": { "id": "minecraft:written_book", "Count": 1, "tag": { ... } },
 *     "hint_mob": "twilightforest:kobold"
 *   },
 *   "decoration_clearance": { ... },               // optional, see LandmarkWrappedStructure
 *   "center_in_chunk": true                        // optional, unused by the wrapper
 * }
 * }</pre>
 * The progression checks look up the start at the nearest landmark grid centre, so the wrapped structure needs a
 * landmark placement and the {@code twilightforest:landmark} structure tag for the locking to apply. 4.8's
 * {@code structure_icon} field is not supported on 1.20.1 and is ignored.
 */
public class ProgressionWrappedStructure extends ProgressionStructure implements CustomStructureData {
    public static final Codec<ProgressionWrappedStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Structure.DIRECT_CODEC.fieldOf("wrapped").forGetter(o -> o.wrappedStructure),
            AdvancementLockConfig.CODEC.fieldOf("advancements_required").forGetter(o -> o.advancementLockConfig),
            HintConfig.CODEC.optionalFieldOf("hint_creature").forGetter(o -> o.optionalHintConfig),
            DecorationConfig.CODEC.optionalFieldOf("decoration_clearance").forGetter(o -> o.optionalDecorationConfig),
            Codec.BOOL.optionalFieldOf("center_in_chunk", true).forGetter(o -> o.centerInChunk)
    ).apply(instance, ProgressionWrappedStructure::new));

    private final Structure wrappedStructure;
    private final Optional<HintConfig> optionalHintConfig;
    private final Optional<DecorationConfig> optionalDecorationConfig;
    private final boolean centerInChunk;

    public ProgressionWrappedStructure(Structure wrappedStructure, AdvancementLockConfig advancementLockConfig, Optional<HintConfig> hintConfig, Optional<DecorationConfig> decorationConfig, boolean centerInChunk) {
        // The inherited hintConfig stays null; every use of it is overridden below to read the optional one instead
        super(advancementLockConfig, null, decorationConfig.orElse(LandmarkWrappedStructure.DEFAULT_DECORATION_CONFIG), LandmarkWrappedStructure.placeholderSettings());
        this.wrappedStructure = wrappedStructure;
        this.optionalHintConfig = hintConfig;
        this.optionalDecorationConfig = decorationConfig;
        this.centerInChunk = centerInChunk;
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

    // [VANILLA COPY] Structure.generate
    //  StructureStart construction swapped for TFStructureStart construction, like 4.8's ConquerableStructure
    @Override
    public StructureStart generate(RegistryAccess registryAccess, ChunkGenerator chunkGen, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager templateManager, long seed, ChunkPos chunkPos, int references, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> isValidBiome) {
        GenerationContext context = new GenerationContext(registryAccess, chunkGen, biomeSource, randomState, templateManager, seed, chunkPos, heightAccessor, isValidBiome);
        Optional<GenerationStub> optional = this.findValidGenerationPoint(context);
        if (optional.isPresent()) {
            StructureStart structureStart = new TFStructureStart(this, chunkPos, references, optional.get().getPiecesBuilder().build());
            if (structureStart.isValid()) {
                return structureStart;
            }
        }

        return StructureStart.INVALID_START;
    }

    @Override
    public void trySpawnHintMonster(Level world, Player player, BlockPos pos) {
        if (this.optionalHintConfig.isPresent())
            super.trySpawnHintMonster(world, player, pos);
    }

    @Override
    public ItemStack createHintBook() {
        return this.optionalHintConfig.map(config -> config.hintItem().copy()).orElse(ItemStack.EMPTY);
    }

    @Override
    @Nullable
    public Mob createHintMonster(Level world) {
        return this.optionalHintConfig.map(config -> config.hintMob().create(world)).orElse(null);
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
        return TFStructureTypes.PROGRESSION_WRAPPED.get();
    }
}
