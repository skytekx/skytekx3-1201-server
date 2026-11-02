package twilightforest.world.components.structures.finalcastle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.world.components.structures.TFStructureComponentOld;


public class FinalCastleMuralComponent extends TFStructureComponentOld {

	/**
	 * The mural is built once and only read afterwards. postProcess runs concurrently for the chunks this piece spans,
	 * so the field is volatile and only ever assigned a fully generated mural. Before this, postProcess wrote the
	 * mural array and its size into shared fields from every thread.
	 */
	@Nullable
	private volatile FinalCastleMural mural;

	public FinalCastleMuralComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFFCMur.get(), nbt);

		// Pieces saved before the mural was serialized carry no bytes. Those rebuild it on first use from the same seed as before.
		if (nbt.contains("muralBytes", Tag.TAG_BYTE_ARRAY)) {
			FinalCastleMural saved = new FinalCastleMural(nbt);
			if (saved.width > 0 && saved.height > 0 && saved.width * saved.height == nbt.getByteArray("muralBytes").length) {
				this.mural = saved;
			}
		}
	}

	public FinalCastleMuralComponent(int i, int x, int y, int z, int width, int height, Direction direction) {
		super(TFStructurePieceTypes.TFFCMur.get(), i, x, y, z);
		this.setOrientation(direction);
		this.boundingBox = TFStructureComponentOld.getComponentToAddBoundingBox2(x, y, z, 0, -height / 2, -width / 2, 1, height - 1, width - 1, direction);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tagCompound) {
		super.addAdditionalSaveData(ctx, tagCompound);

		FinalCastleMural mural = this.mural;
		if (mural != null) {
			mural.writeIntoTag(tagCompound);
		}
	}

	/**
	 * The piece constructors have no access to the world seed (4.8 reads it through WorldUtil.getOverworldSeed(), which 4.3
	 * lacks), so the mural is generated on the first postProcess from the same seed formula 4.3 used. This keeps murals of
	 * castles that are already partly generated consistent across chunk borders.
	 */
	private FinalCastleMural getOrCreateMural(long worldSeed) {
		FinalCastleMural mural = this.mural;
		if (mural == null) {
			synchronized (this) {
				mural = this.mural;
				if (mural == null) {
					mural = new FinalCastleMural(this.boundingBox, this.getOrientation(), worldSeed + (this.boundingBox.minX() * 321534781L) ^ (this.boundingBox.minZ() * 756839L));
					this.mural = mural;
				}
			}
		}
		return mural;
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		final FinalCastleMural mural = this.getOrCreateMural(world.getSeed());
		final BlockState castleMagic = TFBlocks.YELLOW_CASTLE_RUNE_BRICK.get().defaultBlockState();

		// copy mural to world
		for (int x = 0; x < mural.width; x++) {
			for (int y = 0; y < mural.height; y++) {
				if (mural.get(x, y) > 0) {
					this.placeBlock(world, castleMagic, 0, y, x, sbb);
				} else {
					//this.setBlockState(world, TFBlocks.forceField, 0, 0, y, x, sbb);
				}
			}
		}
	}
}
