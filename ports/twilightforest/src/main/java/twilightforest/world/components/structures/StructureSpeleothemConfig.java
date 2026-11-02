package twilightforest.world.components.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.data.custom.stalactites.entry.SpeleothemVariety;
import twilightforest.data.custom.stalactites.entry.SpeleothemVarietyConfig;
import twilightforest.data.custom.stalactites.entry.Stalactite;
import twilightforest.data.custom.stalactites.entry.StalactiteReloadListener;
import twilightforest.util.RectangleLatticeIterator;

import java.util.List;
import java.util.Locale;

/**
 * Entry of the datapack registry {@code twilight/structure_speleothem_settings}: where a structure places speleothems (the lattice)
 * and which stalactite config ({@code twilight/stalactites}, matched by {@code type}) it takes them from.
 */
public record StructureSpeleothemConfig(RectangleLatticeIterator.TriangularLatticeConfig latticeConfig, String speleothemVarietyType) {
	public static final Codec<StructureSpeleothemConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			RectangleLatticeIterator.TriangularLatticeConfig.CODEC.fieldOf("lattice").forGetter(StructureSpeleothemConfig::latticeConfig),
			Codec.STRING.xmap(StructureSpeleothemConfig::lowerCase, StructureSpeleothemConfig::lowerCase).fieldOf("type").forGetter(StructureSpeleothemConfig::speleothemVarietyType)
	).apply(inst, StructureSpeleothemConfig::new));

	private static String lowerCase(String string) {
		return string.toLowerCase(Locale.ROOT);
	}

	/**
	 * The compiled variety is looked up on every use instead of being memoized like TF 4.8 does, so it follows {@code /reload}.
	 */
	public SpeleothemVariety getVariety() {
		return StalactiteReloadListener.getVariety(this.speleothemVarietyType);
	}

	public SpeleothemVarietyConfig getVarietyConfig() {
		return this.getVariety().config();
	}

	public boolean shouldDoAStalactite(RandomSource rand) {
		return this.getVariety().shouldDoAStalactite(rand);
	}

	public boolean shouldDoAStalagmite(RandomSource rand) {
		return this.getVariety().shouldDoAStalagmite(rand);
	}

	@NotNull
	public Stalactite getStalactite(RandomSource rand) {
		return this.getVariety().getStalactite(rand);
	}

	@NotNull
	public Stalactite getStalagmite(RandomSource rand) {
		return this.getVariety().getStalagmite(rand);
	}

	@NotNull
	public Stalactite getSpeleothem(boolean hanging, RandomSource rand) {
		return hanging ? this.getStalactite(rand) : this.getStalagmite(rand);
	}

	@NotNull
	public Iterable<BlockPos.MutableBlockPos> latticeIterator(@Nullable BoundingBox bounds, int yLevel) {
		if (bounds == null)
			return List.of();

		return this.latticeConfig.boundedGrid(bounds, yLevel);
	}
}
