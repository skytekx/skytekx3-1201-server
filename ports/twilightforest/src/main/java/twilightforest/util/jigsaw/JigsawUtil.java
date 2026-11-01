package twilightforest.util.jigsaw;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class JigsawUtil {
	// Raw jigsaw block entity NBT key, written by MC 1.20.3+ jigsaw blocks. 1.20.1's JigsawBlockEntity does not know it, but templates
	// saved by newer versions keep it inside StructureBlockInfo#nbt, so it is read straight from there
	public static final String SELECTION_PRIORITY = "selection_priority";

	public static Direction getAbsoluteHorizontal(FrontAndTop orientation) {
		if (orientation.front().getAxis() == Direction.Axis.Y) {
			return orientation.top();
		} else {
			return orientation.front();
		}
	}

	public static FrontAndTop process(FrontAndTop source, StructurePlaceSettings settings) {
		return settings.getRotation().rotation().rotate(source);
	}

	// Determines if other Jigsaw orientation can be rotated in order to match the source Jigsaw orientation
	public static boolean canRearrangeForConnection(FrontAndTop sourceOrientation, StructureTemplate.StructureBlockInfo otherJigsaw) {
		FrontAndTop otherOrientation = otherJigsaw.state().getValue(JigsawBlock.ORIENTATION);

		boolean frontFacesAlignable = canBeRotatedToAlign(sourceOrientation.front(), otherOrientation.front());
		boolean topFacesAlignable = canBeRotatedToAlign(sourceOrientation.top().getOpposite(), otherOrientation.top());

		return frontFacesAlignable && topFacesAlignable;
	}

	private static boolean canBeRotatedToAlign(Direction source, Direction target) {
		Direction.Plane sourcePlane = source.getAxis().getPlane();
		boolean planesMatch = sourcePlane == target.getAxis().getPlane();

		if (sourcePlane == Direction.Plane.VERTICAL) {
			return planesMatch && source.getOpposite() == target; // Cannot vertically flip templates
		} else {
			return planesMatch; // If both Jigsaws are horizontal, then they can simply be rotated
		}
	}

	public static List<StructureTemplate.StructureBlockInfo> readConnectableJigsaws(StructureTemplateManager manager, ResourceLocation templateLocation, StructurePlaceSettings settings, @Nullable RandomSource random) {
		return readConnectableJigsaws(manager.getOrCreate(templateLocation), settings, random);
	}

	public static List<StructureTemplate.StructureBlockInfo> readConnectableJigsaws(@Nullable StructureTemplate template, StructurePlaceSettings settings, @Nullable RandomSource random) {
		if (template == null || BlockPos.ZERO.equals(template.getSize())) {
			return List.of();
		}

		// The relative-position overload is what filterBlocks(BlockPos, StructurePlaceSettings, Block) calls; it returns the mutable list that 1.20.1's Util.shuffle() needs
		ObjectArrayList<StructureTemplate.StructureBlockInfo> returnables = template.filterBlocks(BlockPos.ZERO, settings, Blocks.JIGSAW, true);

		if (random != null) {
			Util.shuffle(returnables, random);
			// "Stable" sorting - preserves order of "equal" priorities, as arranged by prior shuffling.
			sortBySelectionPriority(returnables);
		}

		return returnables;
	}

	// Stand-in for SinglePoolElement.sortBySelectionPriority(), which only exists from MC 1.20.3 onwards. Same comparator: highest priority first, stable
	public static void sortBySelectionPriority(List<StructureTemplate.StructureBlockInfo> structureBlockInfos) {
		structureBlockInfos.sort(Comparator.<StructureTemplate.StructureBlockInfo>comparingInt(info -> getSelectionPriority(info.nbt())).reversed());
	}

	public static int getSelectionPriority(@Nullable CompoundTag jigsawNbt) {
		return Optionull.mapOrDefault(jigsawNbt, tag -> tag.getInt(SELECTION_PRIORITY), 0);
	}
}
