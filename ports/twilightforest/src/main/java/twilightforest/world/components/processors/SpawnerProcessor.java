package twilightforest.world.components.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructureProcessors;

import java.util.Map;
import java.util.Optional;

// Fills the SpawnData of template spawners from a weighted entity list, and optionally overrides the spawn range and the initial delay
public class SpawnerProcessor extends StructureProcessor {
	public static final Codec<SpawnerProcessor> CODEC = RecordCodecBuilder.create(inst -> inst.group(
		Codec.SHORT.optionalFieldOf("range").forGetter(SpawnerProcessor::serializeRange),
		Codec.FLOAT.optionalFieldOf("start_delay_factor").forGetter(SpawnerProcessor::getDelayFactor),
		Codec.FLOAT.optionalFieldOf("entity_width_max").forGetter(SpawnerProcessor::getEntityWidthMax),
		SpawnData.LIST_CODEC.fieldOf("entities").forGetter(SpawnerProcessor::getPossibleEntities)
	).apply(inst, SpawnerProcessor::new));

	private final Optional<Short> range;
	private final Optional<Float> startDelayFactor;
	private final Optional<Float> entityWidthMax;
	private final SimpleWeightedRandomList<SpawnData> entities;

	public static SpawnerProcessor compile(int range, Object2IntMap<EntityType<?>> weightMap) {
		return compile(range, 0, weightMap);
	}

	public static SpawnerProcessor compile(int range, float maxWidth, Object2IntMap<EntityType<?>> weightMap) {
		SimpleWeightedRandomList.Builder<SpawnData> entities = SimpleWeightedRandomList.builder();

		for (Map.Entry<EntityType<?>, Integer> entry : weightMap.entrySet()) {
			CompoundTag entityInfo = new CompoundTag();

			entityInfo.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString());

			// Spawns at any block light up to 7, at any sky light
			entities.add(new SpawnData(entityInfo, Optional.of(new SpawnData.CustomSpawnRules(new InclusiveRange<>(0, 7), new InclusiveRange<>(0, 15)))), entry.getValue());
		}

		return new SpawnerProcessor(range <= 0 ? Optional.empty() : Optional.of((short) range), Optional.of(0.25f), maxWidth <= 0 ? Optional.empty() : Optional.of(maxWidth), entities.build());
	}

	public SpawnerProcessor(Optional<Short> range, Optional<Float> startDelayFactor, Optional<Float> entityWidthMax, SimpleWeightedRandomList<SpawnData> entities) {
		this.range = range;
		this.startDelayFactor = startDelayFactor;
		this.entities = entities;
		this.entityWidthMax = entityWidthMax.isEmpty() ? Optional.empty() : entityWidthMax.get() <= 0 ? Optional.empty() : entityWidthMax;
	}

	@Nullable
	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos piecePos, StructureTemplate.StructureBlockInfo originalInfo, StructureTemplate.StructureBlockInfo modifiedInfo, StructurePlaceSettings placeSettings, @Nullable StructureTemplate template) {
		CompoundTag nbtInfo = modifiedInfo.nbt();

		if (nbtInfo != null && (modifiedInfo.state().is(Blocks.SPAWNER) || modifiedInfo.state().is(TFBlocks.SINISTER_SPAWNER.get()))) {
			if (this.range.isPresent()) {
				nbtInfo.putShort("SpawnRange", this.range.get());
			}
			if (this.startDelayFactor.isPresent()) {
				nbtInfo.putShort("Delay", (short) Math.round(nbtInfo.getShort("MinSpawnDelay") * this.startDelayFactor.get()));
			}

			if (!nbtInfo.contains("SpawnData") || nbtInfo.getList("SpawnData", Tag.TAG_COMPOUND).isEmpty()) {
				Optional<SpawnData> randomSpawn = this.entities.getRandomValue(placeSettings.getRandom(modifiedInfo.pos()));

				if (randomSpawn.isPresent()) {
					// TF 4.8 also shrinks entities wider than entity_width_max through the generic.scale attribute.
					// Minecraft 1.20.1 has no scale attribute, so on this version the entity keeps its normal size.
					Tag entitySpawnData = SpawnData.CODEC
						.encodeStart(NbtOps.INSTANCE, randomSpawn.get())
						.getOrThrow(false, s -> TwilightForestMod.LOGGER.error("Invalid SpawnData in processing: {}", s));

					nbtInfo.put("SpawnData", entitySpawnData);
				}
			}
		}

		return modifiedInfo;
	}

	/**
	 * @return the scale an entity of this width would need to fit within entity_width_max, or 1 for no rescaling.
	 * Kept for parity with TF 4.8; Minecraft 1.20.1 has no attribute to apply it with.
	 */
	public float rescaleToFitWidth(float entityWidth) {
		if (this.entityWidthMax.isEmpty() || entityWidth == 0)
			return 1; // No rescale, use default scale if NaN will be encountered

		float maxWidth = this.entityWidthMax.get();
		return entityWidth < maxWidth ? 1 : maxWidth / entityWidth;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.SPAWNER_PROCESSOR.get();
	}

	private Optional<Short> serializeRange() {
		return this.range;
	}

	private Optional<Float> getDelayFactor() {
		return this.startDelayFactor;
	}

	private Optional<Float> getEntityWidthMax() {
		return this.entityWidthMax;
	}

	private SimpleWeightedRandomList<SpawnData> getPossibleEntities() {
		return this.entities;
	}
}
