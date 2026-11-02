package twilightforest.data.custom.stalactites.entry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Locale;

/**
 * Read from {@code data/<namespace>/twilight/stalactites/<name>.json}. Several files may share a type: the Twilight Forest's own
 * file decides the chances, files from other namespaces add their speleothems to the lists unless one sets {@code replace}.
 *
 * @param type             A string key in which this configuration is assigned to. Makes effects of this config obtainable by StructureSpeleothemConfigs during piecewise Structure generation
 * @param baseStalactites  A list of references to speleothems (Stalactites) to hang from ceiling. Typically composed of stone or other common blocks.
 * @param oreStalactites   A list of references to special speleothems (Ore Stalactites) to hang from ceiling. Typically composed of ores.
 * @param stalagmites      A list of references to speleothems (Stalagmites) emerging from the ground.
 * @param oreChance        A floating-point decimal number representing the percent chance of a Stalactite being replaced with an Ore Stalactite. Effectively an interpolation between the two weighted lists.
 * @param stalactiteChance The chance for generating a Stalactite or Ore Stalactite at a given point in the lattice
 * @param stalagmiteChance The chance for generating a Stalagmite at all at a given point in the lattice
 * @param replace          Whether prior configurations of the same type (and the speleothems they listed) should be replaced
 *                         <p>
 *                         An example:
 *                         <pre>{@code
 *                         {
 *                           "type": "hydra_lair",
 *                           "stalactite_chance": 1.0,
 *                           "ore_chance": 0.1,
 *                           "base_stalactites": [
 *                             "twilightforest:entries/diamond_stalactite"
 *                           ],
 *                           "ore_stalactites": [
 *                             "twilightforest:entries/redstone_stalactite",
 *                             "twilightforest:entries/glowstone_stalactite"
 *                           ],
 *                           "stalagmite_chance": 0.75,
 *                           "stalagmites": [
 *                             "twilightforest:entries/copper_stalactite"
 *                           ],
 *                           "replace": false
 *                         }
 *                         }</pre>
 */
public record SpeleothemVarietyConfig(String type, List<ResourceLocation> baseStalactites, List<ResourceLocation> oreStalactites, List<ResourceLocation> stalagmites, float oreChance, float stalactiteChance, float stalagmiteChance, boolean replace) {
	public static final Codec<SpeleothemVarietyConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.xmap(SpeleothemVarietyConfig::lowerCase, SpeleothemVarietyConfig::lowerCase).fieldOf("type").forGetter(SpeleothemVarietyConfig::type),
			ResourceLocation.CODEC.listOf().fieldOf("base_stalactites").forGetter(SpeleothemVarietyConfig::baseStalactites),
			ResourceLocation.CODEC.listOf().fieldOf("ore_stalactites").forGetter(SpeleothemVarietyConfig::oreStalactites),
			ResourceLocation.CODEC.listOf().fieldOf("stalagmites").forGetter(SpeleothemVarietyConfig::stalagmites),
			Codec.floatRange(0.0F, 1.0F).fieldOf("ore_chance").forGetter(SpeleothemVarietyConfig::oreChance),
			Codec.floatRange(0.0F, 1.0F).fieldOf("stalactite_chance").forGetter(SpeleothemVarietyConfig::stalactiteChance),
			Codec.floatRange(0.0F, 1.0F).fieldOf("stalagmite_chance").forGetter(SpeleothemVarietyConfig::stalagmiteChance),
			Codec.BOOL.fieldOf("replace").orElse(false).forGetter(SpeleothemVarietyConfig::replace)
	).apply(instance, SpeleothemVarietyConfig::new));

	private static String lowerCase(String string) {
		return string.toLowerCase(Locale.ROOT);
	}

	public boolean shouldDoAStalactite(RandomSource rand) {
		return rand.nextFloat() < this.stalactiteChance();
	}

	public boolean shouldDoAStalagmite(RandomSource rand) {
		return rand.nextFloat() < this.stalagmiteChance();
	}
}
