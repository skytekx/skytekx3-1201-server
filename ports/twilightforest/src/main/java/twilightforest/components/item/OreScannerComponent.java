package twilightforest.components.item;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An Ore Meter scan in progress. The scan walks the full height of a square of whole chunks around the player,
 * spread evenly over {@code scanDurationTicks} ticks, and counts every block it passes.
 * <p>
 * Backport of 4.8's OreScannerComponent (43024f484, 2b3737946, 1076dd0cc). 4.8 keeps it in a persistent but
 * not network-synced data component; here it lives in the stack NBT under {@code ore_scanner}, and
 * {@link twilightforest.item.OreMeterItem#getShareTag} keeps it off the network.
 */
public class OreScannerComponent {
	private final ResourceKey<Level> dimension;
	private final int originX, originZ;
	private final int xSpan, zSpan;
	private final int area;
	private final int range;
	private final int scanDurationTicks;
	private final Reference2IntOpenHashMap<Block> blockCounter;

	private int ticksProgressed;

	public static OreScannerComponent scanFromCenter(ResourceKey<Level> dimension, BlockPos center, int range, int scanDurationTicks) {
		int xChunkCenter = SectionPos.blockToSectionCoord(center.getX());
		int zChunkCenter = SectionPos.blockToSectionCoord(center.getZ());

		// Enforce alignment with chunk edges.
		// 4.8 measured the span to the last block of the far chunk, which left the last column and row of chunks out by one block.
		int originX = SectionPos.sectionToBlockCoord(xChunkCenter - range);
		int originZ = SectionPos.sectionToBlockCoord(zChunkCenter - range);
		int span = SectionPos.sectionToBlockCoord(range * 2 + 1);

		return new OreScannerComponent(dimension, originX, originZ, span, span, range, scanDurationTicks, new Reference2IntOpenHashMap<>(), 0);
	}

	private OreScannerComponent(ResourceKey<Level> dimension, int originX, int originZ, int xSpan, int zSpan, int range, int scanDurationTicks, Reference2IntOpenHashMap<Block> blockCounter, int ticksProgressed) {
		this.dimension = dimension;
		this.originX = originX;
		this.originZ = originZ;
		this.xSpan = xSpan;
		this.zSpan = zSpan;

		// Total horizontal coverage that the scanning region encompasses,
		//  to be later multiplied by building height for actual volume
		this.area = this.xSpan * this.zSpan;

		this.range = range;
		this.scanDurationTicks = scanDurationTicks;
		this.blockCounter = blockCounter;
		this.ticksProgressed = ticksProgressed;
	}

	/**
	 * Scans the next slice of the region. Chunks that are not loaded are skipped instead of being loaded or generated,
	 * so a player who runs away from an unfinished scan cannot make the server load chunks for it.
	 */
	public void tickScan(Level level) {
		int minY = level.getMinBuildHeight();
		int volume = this.getVolume(level);
		int march = (volume + this.scanDurationTicks - 1) / this.scanDurationTicks;
		int totalProgress = this.ticksProgressed * march;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		LevelChunk chunk = null;

		for (int scanSteps = 0; scanSteps < march && totalProgress + scanSteps < volume; scanSteps++) {
			int index = totalProgress + scanSteps;
			int x = this.originX + index % this.xSpan;
			int z = this.originZ + index % this.area / this.xSpan;
			int y = minY + index / this.area;

			int chunkX = SectionPos.blockToSectionCoord(x);
			int chunkZ = SectionPos.blockToSectionCoord(z);
			if (chunk == null || chunk.getPos().x != chunkX || chunk.getPos().z != chunkZ) {
				chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
				if (chunk == null) continue;
			}

			this.blockCounter.addTo(chunk.getBlockState(pos.set(x, y, z)).getBlock(), 1);
		}

		this.ticksProgressed++;
	}

	/**
	 * @return the counted blocks by translation key: only the filter block if one is set, otherwise every ore found.
	 */
	public Map<String, Integer> getResults(@Nullable Block assignedBlock) {
		if (assignedBlock != null) {
			return Map.of(assignedBlock.getDescriptionId(), this.blockCounter.getInt(assignedBlock));
		}

		Map<String, Integer> results = new LinkedHashMap<>();

		for (Reference2IntMap.Entry<Block> entry : this.blockCounter.reference2IntEntrySet()) {
			if (entry.getIntValue() > 0 && entry.getKey().defaultBlockState().is(Tags.Blocks.ORES)) {
				results.merge(entry.getKey().getDescriptionId(), entry.getIntValue(), Integer::sum);
			}
		}

		return results;
	}

	public int getVolume(LevelHeightAccessor level) {
		// 4.8 multiplies by getMaxBuildHeight() while starting at getMinBuildHeight(), which skips the top of worlds with a negative floor
		return this.area * level.getHeight();
	}

	public int getTickProgress() {
		return this.ticksProgressed;
	}

	public int getRange() {
		return this.range;
	}

	public ChunkPos centerChunkPos() {
		return new ChunkPos(SectionPos.blockToSectionCoord(this.originX + this.xSpan / 2), SectionPos.blockToSectionCoord(this.originZ + this.zSpan / 2));
	}

	public boolean isEmpty() {
		return this.area <= 0;
	}

	public boolean isFinished() {
		return this.isEmpty() || this.ticksProgressed >= this.scanDurationTicks;
	}

	/**
	 * A scan is bound to the dimension it was started in. 4.8 keeps scanning the same coordinates in whatever level the player is in.
	 */
	public boolean isIn(Level level) {
		return level.dimension() == this.dimension || level.dimension().location().equals(this.dimension.location());
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putString("dimension", this.dimension.location().toString());
		tag.putIntArray("origin", new int[]{this.originX, 0, this.originZ});
		tag.putInt("span_x", this.xSpan);
		tag.putInt("span_z", this.zSpan);
		tag.putInt("range", this.range);
		tag.putInt("duration", this.scanDurationTicks);
		tag.putInt("progression", this.ticksProgressed);

		CompoundTag counts = new CompoundTag();
		for (Reference2IntMap.Entry<Block> entry : this.blockCounter.reference2IntEntrySet()) {
			counts.putInt(BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString(), entry.getIntValue());
		}
		tag.put("counts", counts);

		return tag;
	}

	@Nullable
	public static OreScannerComponent load(CompoundTag tag) {
		ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
		int[] origin = tag.getIntArray("origin");
		int xSpan = tag.getInt("span_x");
		int zSpan = tag.getInt("span_z");
		int duration = tag.getInt("duration");

		if (dimension == null || origin.length != 3 || xSpan <= 0 || zSpan <= 0 || duration <= 0) {
			return null;
		}

		Reference2IntOpenHashMap<Block> blockCounter = new Reference2IntOpenHashMap<>();
		CompoundTag counts = tag.getCompound("counts");
		for (String key : counts.getAllKeys()) {
			ResourceLocation id = ResourceLocation.tryParse(key);
			if (id != null && counts.contains(key, Tag.TAG_ANY_NUMERIC)) {
				BuiltInRegistries.BLOCK.getOptional(id).ifPresent(block -> blockCounter.addTo(block, counts.getInt(key)));
			}
		}

		return new OreScannerComponent(ResourceKey.create(Registries.DIMENSION, dimension), origin[0], origin[2], xSpan, zSpan,
				tag.getInt("range"), duration, blockCounter, Math.max(tag.getInt("progression"), 0));
	}
}
