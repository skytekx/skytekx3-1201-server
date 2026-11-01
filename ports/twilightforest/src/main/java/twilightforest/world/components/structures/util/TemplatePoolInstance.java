package twilightforest.world.components.structures.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import twilightforest.util.WorldUtil;
import twilightforest.util.jigsaw.JigsawPlaceContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public record TemplatePoolInstance(Weight weight, Optional<Holder<StructureProcessorList>> processors, StructureTemplatePool.Projection projection, TerrainAdjustment terrainAdjustment, Optional<HeightAdjustment> beardifierGroundDelta, boolean ignoreWorldWaterlog, Optional<Holder<TemplateMarkerHandlerList>> markerHandlers, Optional<ChooseRandomProcessors> randomizedProcessors, Map<String, String> poolAliases) implements WeightedEntry {
	private static final Codec<TemplatePoolInstance> CODEC_DIRECT = withAlternative(RecordCodecBuilder.create(instance -> instance.group(
		Weight.CODEC.fieldOf("weight").forGetter(TemplatePoolInstance::weight),
		StructureProcessorType.LIST_CODEC.optionalFieldOf("processors").forGetter(TemplatePoolInstance::processors),
		StructureTemplatePool.Projection.CODEC.optionalFieldOf("projection", StructureTemplatePool.Projection.RIGID).forGetter(TemplatePoolInstance::projection),
		TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(TemplatePoolInstance::terrainAdjustment),
		HeightAdjustment.CODEC.optionalFieldOf("height_adjustment").forGetter(TemplatePoolInstance::beardifierGroundDelta),
		Codec.BOOL.optionalFieldOf("ignore_world_waterlog", false).forGetter(TemplatePoolInstance::ignoreWorldWaterlog),
		strictOptional(TemplateMarkerHandlerList.HOLDER_CODEC, "marker_handlers").forGetter(TemplatePoolInstance::markerHandlers),
		ChooseRandomProcessors.CODEC.optionalFieldOf("randomized_processors").forGetter(TemplatePoolInstance::randomizedProcessors),
		Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("pool_aliases", Map.of()).forGetter(TemplatePoolInstance::poolAliases)
	).apply(instance, TemplatePoolInstance::new)), Codec.INT, TemplatePoolInstance::defaultsWithWeight);

	public static final Codec<TemplatePoolInstance> CODEC = new TemplatePoolInstanceCodec();

	public static TemplatePoolInstance defaultsWithWeight(int weight) {
		return new TemplatePoolInstance(
			Weight.of(weight),
			Optional.empty(),
			StructureTemplatePool.Projection.RIGID,
			TerrainAdjustment.NONE,
			Optional.empty(),
			false,
			Optional.empty(),
			Optional.empty(),
			Map.of()
		);
	}

	// Stand-in for Codec.withAlternative() (DFU 7+, MC 1.20.5+): decodes with the primary codec, falls back to the alternative, always encodes with the primary
	private static <T, U> Codec<T> withAlternative(Codec<T> primary, Codec<U> alternative, Function<U, T> converter) {
		return Codec.either(primary, alternative).xmap(either -> either.map(Function.identity(), converter), Either::left);
	}

	// Stand-in for DFU 7+'s strict optionalFieldOf (MC 1.20.5+), which 4.8 gets for free: an absent field reads as empty, but a present field that fails
	// to parse (e.g. a marker_handlers list id missing from the registry) fails the whole entry instead of silently reading as empty like DFU 6's
	// optionalFieldOf. ExtraCodecs.strictOptionalField is 1.20.2+ and absent in 1.20.1
	private static <A> MapCodec<Optional<A>> strictOptional(Codec<A> codec, String name) {
		return new MapCodec<>() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return Stream.of(ops.createString(name));
			}

			@Override
			public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
				T value = input.get(name);
				if (value == null) {
					return DataResult.success(Optional.empty());
				}

				return codec.parse(ops, value).map(Optional::of);
			}

			@Override
			public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				if (input.isPresent()) {
					return prefix.add(name, codec.encodeStart(ops, input.get()));
				}

				return prefix;
			}

			@Override
			public String toString() {
				return "StrictOptionalFieldCodec[" + name + ": " + codec + "]";
			}
		};
	}

	@Override
	public Weight getWeight() {
		return this.weight;
	}

	@SuppressWarnings("OptionalIsPresent")
	public JigsawPlaceContext adjustContextForTerrain(JigsawPlaceContext placeContext, Structure.GenerationContext generationContext, boolean parentProjectsTerrain) {
		if (this.beardifierGroundDelta.isEmpty())
			return placeContext;

		return this.beardifierGroundDelta.get().adjustForTerrain(placeContext, generationContext, parentProjectsTerrain);
	}

	@SuppressWarnings("OptionalIsPresent")
	public StructureProcessorList chooseRandomProcessors(RandomSource randomSource) {
		if (this.randomizedProcessors.isEmpty()) {
			return new StructureProcessorList(List.of());
		}

		return this.randomizedProcessors.get().chooseRandomProcessors(randomSource);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || this.getClass() != o.getClass()) return false;
		TemplatePoolInstance that = (TemplatePoolInstance) o;
		return Objects.equals(this.weight(), that.weight())
			&& this.terrainAdjustment() == that.terrainAdjustment()
			&& ((this.processors.isEmpty() && that.processors.isEmpty()) || ((this.processors.isPresent() && that.processors.isPresent()) && Objects.equals(this.processors().get().value().list(), that.processors().get().value().list())))
			&& this.projection() == that.projection();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.weight(), this.processors().map(Holder::value).map(StructureProcessorList::list), this.projection(), this.terrainAdjustment());
	}

	public record HeightAdjustment(Heightmap.Types heightType, int beardifierGroundDelta, Optional<Integer> groundJunctionDiffLimit) {
		public static final Codec<HeightAdjustment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Heightmap.Types.CODEC.fieldOf("heightmap").forGetter(HeightAdjustment::heightType),
			Codec.INT.optionalFieldOf("y_offset", 0).forGetter(HeightAdjustment::beardifierGroundDelta),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("ground_junction_diff_clamp").forGetter(HeightAdjustment::groundJunctionDiffLimit)
		).apply(instance, HeightAdjustment::new));

		public JigsawPlaceContext adjustForTerrain(JigsawPlaceContext placeContext, Structure.GenerationContext generationContext, boolean parentProjectsTerrain) {
			BoundingBox box = placeContext.makeBoundingBox(generationContext.structureTemplateManager());

			int yAdjusted = parentProjectsTerrain
				? this.clampYLevelPos(generationContext, placeContext, placeContext.templatePos().offset(placeContext.seedJigsaw().pos()))
				: this.clampYLevelBox(generationContext, placeContext, box);

			return new JigsawPlaceContext(
				placeContext.templatePos().atY(yAdjusted),
				placeContext.placementSettings().copy(),
				placeContext.seedJigsaw(),
				List.copyOf(placeContext.spareJigsaws()),
				placeContext.templateLocation()
			);
		}

		private int clampYLevelBox(Structure.GenerationContext generationContext, JigsawPlaceContext placeContext, BoundingBox box) {
			if (this.groundJunctionDiffLimit.isEmpty()) {
				return this.beardifierGroundDelta + WorldUtil.adjustForTerrain(generationContext, box.minX(), box.minZ(), box.maxX(), box.maxZ(), 2, this.heightType);
			}

			int templateY = placeContext.templatePos().getY();
			int variantLimit = this.groundJunctionDiffLimit.get();
			if (variantLimit == 0) {
				return templateY;
			}

			int yTerrain = WorldUtil.adjustForTerrain(generationContext, box.minX(), box.minZ(), box.maxX(), box.maxZ(), 2, this.heightType);
			return Mth.clamp(this.beardifierGroundDelta + yTerrain, templateY - variantLimit, templateY + variantLimit);
		}

		private int clampYLevelPos(Structure.GenerationContext generationContext, JigsawPlaceContext placeContext, BlockPos pos) {
			if (this.groundJunctionDiffLimit.isEmpty()) {
				return this.beardifierGroundDelta + generationContext.chunkGenerator().getFirstOccupiedHeight(pos.getX(), pos.getZ(), this.heightType, generationContext.heightAccessor(), generationContext.randomState());
			}

			int templateY = placeContext.templatePos().getY();
			int variantLimit = this.groundJunctionDiffLimit.get();
			if (variantLimit == 0) {
				return templateY;
			}

			int yTerrain = generationContext.chunkGenerator().getFirstOccupiedHeight(pos.getX(), pos.getZ(), this.heightType, generationContext.heightAccessor(), generationContext.randomState());
			return Mth.clamp(this.beardifierGroundDelta + yTerrain, templateY - variantLimit, templateY + variantLimit);
		}
	}

	private static class TemplatePoolInstanceCodec implements Codec<TemplatePoolInstance> {
		@Override
		public <T> DataResult<T> encode(TemplatePoolInstance input, DynamicOps<T> ops, T prefix) {
			int weight = input.weight.asInt();
			if (input.equals(TemplatePoolInstance.defaultsWithWeight(weight))) {
				return DataResult.success(ops.createInt(weight));
			}

			return CODEC_DIRECT.encodeStart(ops, input);
		}

		@SuppressWarnings("OptionalIsPresent")
		@Override
		public <T> DataResult<Pair<TemplatePoolInstance, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<TemplatePoolInstance> parse = CODEC_DIRECT.parse(ops, input);
			Optional<TemplatePoolInstance> templatePoolInstance = parse.resultOrPartial(error -> {});
			if (templatePoolInstance.isEmpty()) {
				return DataResult.error(() -> "TemplatePoolInstance.CODEC deserialization problem:\n" + parse + "\n\n from data:\n" + input);
			}
			return DataResult.success(Pair.of(templatePoolInstance.get(), input));
		}
	}

	public record ChooseRandomProcessors(List<SimpleWeightedRandomList<StructureProcessor>> processors) {
		public static final Codec<ChooseRandomProcessors> CODEC = SimpleWeightedRandomList.wrappedCodec(StructureProcessorType.SINGLE_CODEC).listOf().xmap(ChooseRandomProcessors::new, ChooseRandomProcessors::processors);

		public StructureProcessorList chooseRandomProcessors(RandomSource random) {
			List<StructureProcessor> chosenProcessors = new ArrayList<>();

			for (SimpleWeightedRandomList<StructureProcessor> list : this.processors) {
				list.getRandomValue(random).ifPresent(chosenProcessors::add);
			}

			return new StructureProcessorList(Collections.unmodifiableList(chosenProcessors));
		}
	}
}
