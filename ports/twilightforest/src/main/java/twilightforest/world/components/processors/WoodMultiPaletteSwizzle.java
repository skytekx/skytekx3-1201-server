package twilightforest.world.components.processors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFStructureProcessors;
import twilightforest.init.custom.WoodPalettes;
import twilightforest.util.WoodPalette;

import java.util.List;

// Like WoodPaletteSwizzle, but with several (target, replacement) palette pairs. The first pair that changes a block wins, so two palettes can be swapped with each other.
public final class WoodMultiPaletteSwizzle extends StructureProcessor {
	// Each pair is written like a WoodPaletteSwizzle: {"target_palette": ..., "replacement_palette": ...}.
	// TF 4.8 uses Codec.pair(WoodPalettes.CODEC, WoodPalettes.CODEC) here, which cannot read two palette references back; 4.8 only builds this processor in code.
	private static final Codec<Pair<Holder<WoodPalette>, Holder<WoodPalette>>> PALETTE_PAIR_CODEC = Codec.mapPair(
		WoodPalettes.CODEC.fieldOf("target_palette"),
		WoodPalettes.CODEC.fieldOf("replacement_palette")
	).codec();

	public static final Codec<WoodMultiPaletteSwizzle> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
		PALETTE_PAIR_CODEC.listOf().fieldOf("palettes").forGetter(s -> s.palettes)
	).apply(instance, WoodMultiPaletteSwizzle::new));

	private final List<Pair<Holder<WoodPalette>, Holder<WoodPalette>>> palettes;

	public WoodMultiPaletteSwizzle(List<Pair<Holder<WoodPalette>, Holder<WoodPalette>>> palettes) {
		this.palettes = palettes;
	}

	@Override
	public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos piecePos, StructureTemplate.StructureBlockInfo originalInfo, StructureTemplate.StructureBlockInfo modifiedInfo, StructurePlaceSettings placeSettings, @Nullable StructureTemplate template) {
		for (Pair<Holder<WoodPalette>, Holder<WoodPalette>> palettePair : this.palettes) {
			StructureTemplate.StructureBlockInfo newInfo = palettePair.getSecond().value().modifyBlockWithType(palettePair.getFirst().value(), modifiedInfo);

			if (newInfo != modifiedInfo) {
				return newInfo;
			}
		}

		return modifiedInfo;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return TFStructureProcessors.PLANK_MULTISWIZZLE.get();
	}
}
