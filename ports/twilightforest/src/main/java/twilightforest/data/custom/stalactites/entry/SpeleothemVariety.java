package twilightforest.data.custom.stalactites.entry;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.NotNull;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.feature.BlockSpikeFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A {@link SpeleothemVarietyConfig} with its speleothem lists compiled into weighted pickers. TF 4.8 compiles these lazily inside each
 * StructureSpeleothemConfig and memoizes them forever; here {@link StalactiteReloadListener} compiles them once per data reload, so a
 * {@code /reload} picks up changed stalactite files and worldgen threads only ever read a finished, immutable snapshot.
 */
public record SpeleothemVariety(SpeleothemVarietyConfig config, Function<RandomSource, Stalactite> stalactites, Function<RandomSource, Stalactite> stalagmites) {
	public static SpeleothemVariety compile(SpeleothemVarietyConfig varietyConfig, List<Stalactite> baseStalactites, List<Stalactite> oreStalactites, List<Stalactite> stalagmites) {
		TwilightForestMod.LOGGER.debug("Compiling Stalactite and Stalagmite configs for " + varietyConfig.type() + " type");

		return new SpeleothemVariety(varietyConfig, compileStalactites(varietyConfig, baseStalactites, oreStalactites), compileSpeleothemsSimple(stalagmites));
	}

	@NotNull
	private static Function<RandomSource, Stalactite> compileStalactites(SpeleothemVarietyConfig varietyConfig, List<Stalactite> stalactites, List<Stalactite> oreStalactites) {
		// Ore Chance represents an interpolation between two weighted lists of A (stones) and B (ores)
		float weightedListInterpolation = Mth.clamp(varietyConfig.oreChance(), 0, 1);

		int stoneWeightSum = stalactites.stream().mapToInt(Stalactite::weight).filter(w -> w > 0).sum();
		int oreWeightSum = oreStalactites.stream().mapToInt(Stalactite::weight).filter(w -> w > 0).sum();
		float totalWeight = stoneWeightSum + oreWeightSum;

		// Simplify underlying data structures for returned lambdas by only "compiling" the list if the chance for the alternative is zero
		// or if the alternate's list is empty
		if (totalWeight <= 0) {
			return BlockSpikeFeature::defaultRandom;
		} else if (stalactites.isEmpty() || stoneWeightSum <= 0) {
			return compileSpeleothemsSimple(oreStalactites);
		} else if (oreStalactites.isEmpty() || oreWeightSum <= 0) {
			return compileSpeleothemsSimple(stalactites);
		}

		// Since the weights are integers, this ensures some interpolation precision through float->integer casting
		// Stupid but simple way of transporting fractional precision up to ten-thousandths
		// Loss of precision is expected regardless, but insignificant
		// Yes, digits of precision are counted through String Length.
		// But this happens once per variety and data reload.
		final double quantizationFactor = Math.ceil(Math.pow(10, Mth.clamp((weightedListInterpolation + "" + totalWeight).length() - 4, 2, 6)));

		final double stoneCounterweight = quantizationFactor * (1 - weightedListInterpolation) / stoneWeightSum;
		final double oreCounterweight = quantizationFactor * weightedListInterpolation / oreWeightSum;

		// Simplify underlying data structures for returned lambdas by only "compiling" the list if
		// the chance for the alternative is zero or if the alternate list is empty
		if (stoneCounterweight <= 0) {
			return compileSpeleothemsSimple(oreStalactites);
		} else if (oreCounterweight <= 0) {
			return compileSpeleothemsSimple(stalactites);
		}

		// Rebuild individual weighted lists, multiply appropriate weights for interpolating between stones vs ores speleothem
		ArrayList<WeightedEntry.Wrapper<Stalactite>> unbakedRandomList = stalactites.stream().filter(s -> s.weight() > 0).map(s -> WeightedEntry.wrap(s, Mth.ceil(s.weight() * stoneCounterweight))).collect(Collectors.toCollection(ArrayList::new));
		// Add oreStalactites to the unbaked list
		oreStalactites.stream().filter(s -> s.weight() > 0).map(s -> WeightedEntry.wrap(s, Mth.ceil(s.weight() * oreCounterweight))).forEachOrdered(unbakedRandomList::add);

		if (TwilightForestMod.LOGGER.isDebugEnabled()) {
			StringJoiner joiner = new StringJoiner("\n");

			joiner.add("")
					.add("Ore interpolation factor: " + weightedListInterpolation)
					.add("Stone Counterweight: " + stoneCounterweight)
					.add("Ore Counterweight: " + oreCounterweight);

			for (WeightedEntry.Wrapper<Stalactite> e : unbakedRandomList)
				joiner.add(e.getData() + " - After counterweight: " + e.getWeight().asInt());

			joiner.add("Total weight after counterweights: " + unbakedRandomList.stream().mapToInt(e -> e.getWeight().asInt()).sum());

			TwilightForestMod.LOGGER.debug(joiner);
		}

		return compileSpeleothems(unbakedRandomList);
	}

	@NotNull
	private static Function<RandomSource, Stalactite> compileSpeleothemsSimple(List<Stalactite> stalactites) {
		return compileSpeleothems(stalactites.stream().filter(s -> s.weight() > 0).map(stalactite -> WeightedEntry.wrap(stalactite, stalactite.weight())).toList());
	}

	@NotNull
	private static Function<RandomSource, Stalactite> compileSpeleothems(List<WeightedEntry.Wrapper<Stalactite>> unbakedRandomList) {
		// Construct this once. Constructing it inside the lambda means it'll be constructed each time the lambda is invoked
		WeightedRandomList<WeightedEntry.Wrapper<Stalactite>> randomList = WeightedRandomList.create(unbakedRandomList);

		// Simplify underlying data structure for returned lambdas by simply returning a
		// Stone Stalactite getter whenever there's no random elements to pass.
		if (randomList.isEmpty() || randomList.unwrap().stream().mapToInt(w -> w.getWeight().asInt()).sum() <= 0) {
			return BlockSpikeFeature::defaultRandom;
		}

		// Return a function representing anonymous access to the randomList, by passing a RandomSource in which a Speleothem is returned.
		// This ensures the randomList is constructed only once.
		return random -> randomList.getRandom(random).map(WeightedEntry.Wrapper::getData).orElse(BlockSpikeFeature.STONE_STALACTITE);
	}

	public boolean shouldDoAStalactite(RandomSource rand) {
		return this.config.shouldDoAStalactite(rand);
	}

	public boolean shouldDoAStalagmite(RandomSource rand) {
		return this.config.shouldDoAStalagmite(rand);
	}

	@NotNull
	public Stalactite getStalactite(RandomSource rand) {
		return this.stalactites.apply(rand);
	}

	@NotNull
	public Stalactite getStalagmite(RandomSource rand) {
		return this.stalagmites.apply(rand);
	}
}
