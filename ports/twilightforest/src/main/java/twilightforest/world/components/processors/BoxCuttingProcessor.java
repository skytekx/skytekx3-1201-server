package twilightforest.world.components.processors;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import twilightforest.TwilightForestMod;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.init.TFStructureProcessors;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// Despite the name, any facts about actual Box Cutters being involved are lies
public final class BoxCuttingProcessor extends StructureProcessor {
    // TF 4.8 writes the boxes under "boxes". The bare list 4.3 used here ends up under "value" in a processor_type entry, so that form is still read.
    public static final Codec<BoxCuttingProcessor> CODEC = Codec.mapEither(BoundingBox.CODEC.listOf().fieldOf("boxes"), BoundingBox.CODEC.listOf().fieldOf("value"))
            .<List<BoundingBox>>xmap(either -> either.map(Function.identity(), Function.identity()), Either::left)
            .xmap(BoxCuttingProcessor::new, p -> p.cutouts)
            .codec();

    public final List<BoundingBox> cutouts;

    public BoxCuttingProcessor(List<BoundingBox> cutouts) {
        this.cutouts = cutouts;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos origin, BlockPos centerBottom, StructureTemplate.StructureBlockInfo originalBlockInfo, StructureTemplate.StructureBlockInfo modifiedBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        for (BoundingBox cutout : this.cutouts)
            if (cutout.isInside(modifiedBlockInfo.pos()))
                return null;

        return modifiedBlockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TFStructureProcessors.BOX_CUTTING_PROCESSOR.get();
    }

    public static BoxCuttingProcessor fromNBT(ListTag tag) {
        List<BoundingBox> boxes = new ArrayList<>();

        for (Tag tagElement : tag) {
            if (!(tagElement instanceof CompoundTag boxCompound)) continue;

            try {
                boxes.add(BoundingBoxUtils.NBTToBoundingBox(boxCompound));
            } catch (Throwable e) {
                TwilightForestMod.LOGGER.error("Invalid BoundingBox found in list", e);
            }
        }

        return new BoxCuttingProcessor(boxes);
    }

    public static BoxCuttingProcessor forLichTower(Map<BlockPos, Direction> sideTowerStarts) {
        return new BoxCuttingProcessor(sideTowerStarts
                .entrySet()
                .stream()
                .map(e -> BoundingBox.fromCorners(e.getKey(), e.getKey().relative(e.getValue(), 1).relative(e.getValue().getClockWise(), 4).above(6)))
                .collect(Collectors.toList())
        );
    }
}
