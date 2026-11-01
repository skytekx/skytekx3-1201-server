package twilightforest.components.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The result of a finished Ore Meter scan, stored on the stack under {@code ore_data} and shown by the Ore Meter HUD.
 * <p>
 * Backport of 4.8's OreScannerData (027499a15) as stack NBT. {@code counts} maps block translation keys to the number
 * found. 4.8's {@code universal_id} hash is not kept: the HUD caches by the tag itself, and the scanned range is stored
 * instead, so the HUD header shows the range the scan used rather than the meter's current setting.
 */
public record OreScannerData(Map<String, Integer> counts, ChunkPos scannedChunk, int totalScannedBlocks, int scannedRange) {

	public static OreScannerData create(Map<String, Integer> counts, ChunkPos scannedChunk, int totalScannedBlocks, int scannedRange) {
		return new OreScannerData(Collections.unmodifiableMap(new LinkedHashMap<>(counts)), scannedChunk, totalScannedBlocks, scannedRange);
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();

		CompoundTag countsTag = new CompoundTag();
		this.counts().forEach(countsTag::putInt);
		tag.put("counts", countsTag);

		tag.putLong("scanned_chunk", this.scannedChunk().toLong());
		tag.putInt("scanned_block_count", this.totalScannedBlocks());
		tag.putInt("scanned_range", this.scannedRange());
		return tag;
	}

	public static OreScannerData load(CompoundTag tag) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		CompoundTag countsTag = tag.getCompound("counts");
		for (String key : countsTag.getAllKeys()) {
			if (!key.isEmpty() && countsTag.contains(key, Tag.TAG_ANY_NUMERIC)) {
				counts.put(key, Math.max(countsTag.getInt(key), 0));
			}
		}

		return create(counts, new ChunkPos(tag.getLong("scanned_chunk")), Math.max(tag.getInt("scanned_block_count"), 0), Math.max(tag.getInt("scanned_range"), 0));
	}
}
