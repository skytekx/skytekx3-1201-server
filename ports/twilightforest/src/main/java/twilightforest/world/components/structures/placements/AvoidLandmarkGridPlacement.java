package twilightforest.world.components.structures.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import twilightforest.init.TFStructurePlacementTypes;
import twilightforest.util.LegacyLandmarkPlacements;

import java.util.Optional;

/**
 * A {@link RandomSpreadStructurePlacement} for minor structures that also rejects every chunk within 80 blocks (on both
 * axes) of the nearest landmark grid centre, so the minor structure cannot cut into a landmark. With
 * {@code avoid_additional_structures} (structure set to chunk radius) it also keeps clear of other structure sets, on top
 * of the single vanilla {@code exclusion_zone}.
 */
public class AvoidLandmarkGridPlacement extends RandomSpreadStructurePlacement {
	public static final Codec<AvoidLandmarkGridPlacement> CODEC = ExtraCodecs.validate(RecordCodecBuilder.<AvoidLandmarkGridPlacement>mapCodec(instance -> instance.group(
		Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(AvoidLandmarkGridPlacement::locateOffset),
		StructurePlacement.FrequencyReductionMethod.CODEC
			.optionalFieldOf("frequency_reduction_method", StructurePlacement.FrequencyReductionMethod.DEFAULT)
			.forGetter(AvoidLandmarkGridPlacement::frequencyReductionMethod),
		Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(AvoidLandmarkGridPlacement::frequency),
		ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(AvoidLandmarkGridPlacement::salt),
		StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(AvoidLandmarkGridPlacement::exclusionZone),
		Codec.intRange(0, 4096).fieldOf("spacing").forGetter(AvoidLandmarkGridPlacement::spacing),
		Codec.intRange(0, 4096).fieldOf("separation").forGetter(AvoidLandmarkGridPlacement::separation),
		RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(AvoidLandmarkGridPlacement::spreadType),
		AvoidAdditionalStructures.CODEC.optionalFieldOf("avoid_additional_structures").forGetter(AvoidLandmarkGridPlacement::otherStructuresToAvoid)
	).apply(instance, AvoidLandmarkGridPlacement::new)), AvoidLandmarkGridPlacement::validate).codec();

	private final Optional<AvoidAdditionalStructures> avoidOtherStructures;

	private static DataResult<AvoidLandmarkGridPlacement> validate(AvoidLandmarkGridPlacement placement) {
		return placement.spacing() <= placement.separation() ? DataResult.error(() -> "Spacing has to be larger than separation") : DataResult.success(placement);
	}

	public AvoidLandmarkGridPlacement(int spacing, int separation, RandomSpreadType spreadType, int salt, Optional<AvoidAdditionalStructures> avoidOtherStructures) {
		super(spacing, separation, spreadType, salt);
		this.avoidOtherStructures = avoidOtherStructures;
	}

	public AvoidLandmarkGridPlacement(Vec3i locateOffset, FrequencyReductionMethod reduction, float frequencyModifier, int salt, Optional<ExclusionZone> exclusionZone, int spacing, int separation, RandomSpreadType spreadType, Optional<AvoidAdditionalStructures> avoidOtherStructures) {
		super(locateOffset, reduction, frequencyModifier, salt, exclusionZone, spacing, separation, spreadType);
		this.avoidOtherStructures = avoidOtherStructures;
	}

	@Override
	protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int chunkX, int chunkZ) {
		//copy of super
		ChunkPos chunkpos = this.getPotentialStructureChunk(state.getLevelSeed(), chunkX, chunkZ);
		if (chunkpos.x != chunkX || chunkpos.z != chunkZ) {
			return false;
		}

		// Feature Center
		BlockPos.MutableBlockPos featurePos = LegacyLandmarkPlacements.getNearestCenterXZ(chunkX, chunkZ).mutable();

		// Turn Feature Center into Feature Offset
		featurePos.set(Math.abs(featurePos.getX() - chunkpos.getWorldPosition().getX()), 0, Math.abs(featurePos.getZ() - chunkpos.getWorldPosition().getZ()));
		int size = 80;

		return featurePos.getX() >= size || featurePos.getZ() >= size;
	}

	// 1.20.1 has no applyInteractionsWithOtherStructures hook; super.isStructureChunk runs the placement, frequency and
	// exclusion zone checks in the same order, so the additional structure sets are checked last as in 4.8
	@Override
	public boolean isStructureChunk(ChunkGeneratorStructureState structureState, int x, int z) {
		return super.isStructureChunk(structureState, x, z)
			&& (this.avoidOtherStructures.isEmpty() || !this.avoidOtherStructures.get().isPlacementForbidden(structureState, x, z));
	}

	private Optional<AvoidAdditionalStructures> otherStructuresToAvoid() {
		return this.avoidOtherStructures;
	}

	@Override
	public StructurePlacementType<?> type() {
		return TFStructurePlacementTypes.AVOID_GRID_LANDMARK_PLACEMENT_TYPE.get();
	}

	public record AvoidAdditionalStructures(Object2IntArrayMap<Holder<StructureSet>> avoidStructures) {
		public static final Codec<AvoidAdditionalStructures> CODEC = Codec.unboundedMap(StructureSet.CODEC, Codec.intRange(0, 16)).xmap(map -> new AvoidAdditionalStructures(new Object2IntArrayMap<>(map)), AvoidAdditionalStructures::avoidStructures);

		private boolean isPlacementForbidden(ChunkGeneratorStructureState structureState, int x, int z) {
			for (Object2IntArrayMap.Entry<Holder<StructureSet>> structureSetEntry : this.avoidStructures.object2IntEntrySet()) {
				if (structureState.hasStructureChunkInRange(structureSetEntry.getKey(), x, z, structureSetEntry.getIntValue())) {
					return true;
				}
			}

			return false;
		}
	}
}
