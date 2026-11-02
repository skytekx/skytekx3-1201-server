package twilightforest.world.components.structures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.data.tags.BiomeTagGenerator;
import twilightforest.init.TFEntities;
import twilightforest.init.TFStructureTypes;
import twilightforest.util.WorldUtil;
import twilightforest.util.jigsaw.JigsawPlaceContext;
import twilightforest.world.components.chunkgenerators.BoxDensityFunction;
import twilightforest.world.components.structures.CustomDensitySource;
import twilightforest.world.components.structures.TwilightJigsawPiece;
import twilightforest.world.components.structures.lichtower.TowerMainComponent;
import twilightforest.world.components.structures.lichtowerrevamp.LichTowerBaseTrim;
import twilightforest.world.components.structures.lichtowerrevamp.LichTowerFoyer;
import twilightforest.world.components.structures.lichtowerrevamp.LichTowerPieces;
import twilightforest.world.components.structures.lichtowerrevamp.LichTowerWingBeard;
import twilightforest.world.components.structures.lichtowerrevamp.LichYardBox;
import twilightforest.world.components.structures.util.ControlledSpawningStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TF 4.8's jigsaw Lich Tower: a foyer and vestibule lead into a central tower of stacked slices topped by the boss room, with side wings
 * of rooms, bridges, roofs and beards, and a graveyard yard flattened by {@link #getStructureTerraformer}. Towers that TF 4.3 generated
 * start with a {@link TowerMainComponent} and keep loading with their old pieces, whose TFLT* piece types stay registered.
 */
public class LichTowerStructure extends ControlledSpawningStructure implements CustomDensitySource {
	public static final Codec<LichTowerStructure> CODEC = RecordCodecBuilder.create(instance ->
		controlledSpawningCodec(instance).apply(instance, LichTowerStructure::new)
	);

	public LichTowerStructure(ControlledSpawningConfig controlledSpawningConfig, AdvancementLockConfig advancementLockConfig, HintConfig hintConfig, DecorationConfig decorationConfig, StructureSettings structureSettings) {
		super(controlledSpawningConfig, advancementLockConfig, hintConfig, decorationConfig, structureSettings);
	}

	@Override
	protected @Nullable StructurePiece getFirstPiece(GenerationContext context, RandomSource random, ChunkPos chunkPos, int x, int y, int z) {
		Direction direction = Rotation.getRandom(random).rotate(Direction.SOUTH);
		BlockPos placePos = new BlockPos(x, y, z).relative(direction, 24);
		FrontAndTop oriented = FrontAndTop.fromFrontAndTop(Direction.UP, direction);

		JigsawPlaceContext placeContext = JigsawPlaceContext.pickPlaceableJunction(placePos, BlockPos.ZERO, oriented, context.structureTemplateManager(), TwilightForestMod.prefix("lich_tower/tower_foyer"), "twilightforest:lich_tower/vestibule", random);

		return placeContext == null ? null : new LichTowerFoyer(context.structureTemplateManager(), placeContext, true, random.nextBoolean());
	}

	@Override
	protected void generateFromStartingPiece(StructurePiece startingPiece, GenerationContext context, StructurePiecesBuilder structurePiecesBuilder) {
		structurePiecesBuilder.addPiece(startingPiece);
		if (startingPiece instanceof TwilightJigsawPiece jigsaw) {
			jigsaw.addJigsaws(jigsaw, structurePiecesBuilder, context);
		} else {
			startingPiece.addChildren(startingPiece, structurePiecesBuilder, context.random());
		}

		if (startingPiece instanceof LichTowerFoyer foyerPiece) {
			LichYardBox.beginYard(foyerPiece, context, structurePiecesBuilder);
		}
	}

	@Override
	public StructureType<?> type() {
		return TFStructureTypes.LICH_TOWER.get();
	}

	public static LichTowerStructure buildLichTowerConfig(BootstapContext<Structure> context) {
		// Spawn list indices, see LichTowerPieces: 0 the yard, 1 the tower interior. The foyer (2) has no list, so nothing spawns there
		List<MobSpawnSettings.SpawnerData> yardSpawns = List.of(
			new MobSpawnSettings.SpawnerData(TFEntities.RISING_ZOMBIE.get(), 2, 1, 2)
		);
		List<MobSpawnSettings.SpawnerData> interiorSpawns = List.of(
			new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 10, 1, 2),
			new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 10, 1, 2),
			new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 1, 1, 1),
			new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 1, 2),
			new MobSpawnSettings.SpawnerData(TFEntities.DEATH_TOME.get(), 10, 2, 3),
			new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)
		);

		return new LichTowerStructure(
			// TF 4.8's ControlledSpawningConfig.justMonsters(yardSpawns, interiorSpawns)
			ControlledSpawningConfig.create(List.of(yardSpawns, interiorSpawns), List.of(), List.of()),
			new AdvancementLockConfig(List.of(TwilightForestMod.prefix("progress_naga"))),
			new HintConfig(HintConfig.book("lichtower", 4), TFEntities.KOBOLD.get()),
			new DecorationConfig(0, false, true, false, true),
			new StructureSettings(
				context.lookup(Registries.BIOME).getOrThrow(BiomeTagGenerator.VALID_LICH_TOWER_BIOMES),
				Arrays.stream(MobCategory.values()).collect(Collectors.toMap(category -> category, category -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), // Landmarks have Controlled Mob spawning
				GenerationStep.Decoration.SURFACE_STRUCTURES,
				TerrainAdjustment.BEARD_THIN
			)
		);
	}

	/**
	 * Towers generated by TF 4.3 used one monster list (label "0") for all their pieces. Since 4.8 that label is the yard's Rising Zombies,
	 * so the pieces of an old tower take the interior list instead.
	 */
	public static int remapLegacySpawnIndex(int legacySpawnIndex) {
		return legacySpawnIndex < 0 ? legacySpawnIndex : LichTowerPieces.INTERIOR_SPAWNS;
	}

	// Flattens the ground under the foyer, the central tower's trim, the grounded side tower trims and the yard (TF 4.8 4012cf413)
	@Override
	public DensityFunction getStructureTerraformer(ChunkPos chunkPosAt, StructureStart structurePieceSource) {
		List<BoundingBox> trimBoxes = new ArrayList<>();

		List<StructurePiece> pieces = structurePieceSource.getPieces();
		if (pieces.isEmpty() || pieces.get(0) instanceof TowerMainComponent) {
			// A tower generated by TF 4.3, or an empty start: no yard to flatten
			return DensityFunctions.zero();
		}

		int yBase = pieces.get(0).getBoundingBox().minY();

		DensityFunction activator = DensityFunctions.yClampedGradient(yBase - 2, yBase - 1, 1, 0);

		for (var piece : pieces) {
			if (piece instanceof LichTowerFoyer || piece instanceof LichTowerBaseTrim || (piece instanceof LichTowerWingBeard beard && beard.isTrim())) {
				trimBoxes.add(piece.getBoundingBox());
			} else if (piece instanceof LichYardBox yard && yard.getTerrainAdjustment() != TerrainAdjustment.NONE) {
				trimBoxes.add(piece.getBoundingBox().moved(0, -5, 0));
			}
		}

		return DensityFunctions.mul(activator, BoxDensityFunction.combine(trimBoxes, -5, -5, TerrainAdjustment.BURY));
	}

	// The tower's height comes from the terrain around its chunk, a weighted mean that favours the lower samples (TF 4.8 b7fe8333f)
	@Override
	public int adjustForTerrain(GenerationContext context, int x, int z) {
		return WorldUtil.adjustForTerrain(context, x, z, 32, 4);
	}
}
