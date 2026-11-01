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
import twilightforest.block.JarBlock;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class JarEvents {

	private static final Predicate<BlockState> IS_JAR = state -> state.getBlock() instanceof JarBlock;

	/**
	 * Firefly and cicada jars had no block entity in 4.3, so chunks saved back then (including chunks only partly
	 * generated then) hold none for them. The jar, its lid and its wobble are drawn by the block entity renderer, so
	 * such a jar would be invisible. When a chunk loads, give every jar that has neither a block entity nor saved
	 * block entity data a fresh one (default lid) before the chunk is sent to players. The block state is left as it
	 * is. Jars from current worldgen carry queued block entity data and are skipped. Sections whose palette holds no
	 * jar are skipped without being scanned.
	 */
	@SubscribeEvent
	public static void addMissingJarBlockEntities(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel) || !(event.getChunk() instanceof LevelChunk chunk)) return;

		boolean added = false;
		LevelChunkSection[] sections = chunk.getSections();
		for (int index = 0; index < sections.length; index++) {
			LevelChunkSection section = sections[index];
			if (section.hasOnlyAir() || !section.maybeHas(IS_JAR)) continue;

			BlockPos origin = SectionPos.of(chunk.getPos(), chunk.getSectionYFromSectionIndex(index)).origin();
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					for (int x = 0; x < 16; x++) {
						if (IS_JAR.test(section.getBlockState(x, y, z))) {
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
