package twilightforest.world.components.structures.markerhandler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import twilightforest.init.custom.TemplateMarkerHandlers;
import twilightforest.util.EntityUtil;

// Hangs a random 1x1 painting from a painting variant tag (e.g. minecraft:placeable), facing north turned by the marker rotation
public record PaintingMarkerHandler(TagKey<PaintingVariant> paintings) implements TemplateMarkerHandler {
	public static final Codec<PaintingMarkerHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		TagKey.codec(Registries.PAINTING_VARIANT).fieldOf("paintings").forGetter(PaintingMarkerHandler::paintings)
	).apply(instance, PaintingMarkerHandler::new));

	@Override
	public boolean handleDataMarker(String label, BlockPos pos, WorldGenLevel level, RandomSource random, BoundingBox chunkBounds, ChunkGenerator chunkGen, Rotation rotation) {
		return EntityUtil.placePainting(pos, random, 1, this.paintings, level, 1, 1, rotation.rotate(Direction.NORTH));
	}

	@Override
	public TemplateMarkerHandlerType getType() {
		return TemplateMarkerHandlers.PAINTING.get();
	}
}
