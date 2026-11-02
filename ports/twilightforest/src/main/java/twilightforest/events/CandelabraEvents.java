package twilightforest.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TwilightForestMod;
import twilightforest.block.CandelabraBlock;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class CandelabraEvents {

	private static final Predicate<BlockState> IS_CANDELABRA = state -> state.getBlock() instanceof CandelabraBlock;

	/**
	 * Candelabras had no block entity in 4.3, so chunks saved back then hold none for them. Their candles are drawn by
	 * the block entity renderer and dropped from the block entity, so such a candelabra would show and drop no candles.
	 * When a chunk loads, give every candelabra that has neither a block entity nor saved block entity data a fresh one
	 * before the chunk is sent to players; it starts out with the plain candles its block state shows (three, through
	 * the {@code has_candle_N} defaults). The block state is left as it is. Candelabras from current worldgen carry
	 * queued block entity data and are skipped. Sections whose palette holds no candelabra are skipped without being
	 * scanned. Same approach as {@link JarEvents}.
	 */
	@SubscribeEvent
	public static void addMissingCandelabraBlockEntities(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel) || !(event.getChunk() instanceof LevelChunk chunk)) return;

		boolean added = false;
		LevelChunkSection[] sections = chunk.getSections();
		for (int index = 0; index < sections.length; index++) {
			LevelChunkSection section = sections[index];
			if (section.hasOnlyAir() || !section.maybeHas(IS_CANDELABRA)) continue;

			BlockPos origin = SectionPos.of(chunk.getPos(), chunk.getSectionYFromSectionIndex(index)).origin();
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						if (IS_CANDELABRA.test(section.getBlockState(x, y, z))) {
							pos.setWithOffset(origin, x, y, z);
							// Promotes saved block entity data if there is any; creates and registers a new one otherwise
							if (chunk.getBlockEntities().get(pos) == null && chunk.getBlockEntityNbt(pos) == null) {
								added |= chunk.getBlockEntity(pos.immutable(), LevelChunk.EntityCreationType.IMMEDIATE) != null;
							}
						}
					}
				}
			}
		}

		if (added) chunk.setUnsaved(true);
	}
}
