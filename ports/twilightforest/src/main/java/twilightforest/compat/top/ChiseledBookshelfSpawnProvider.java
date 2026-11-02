package twilightforest.compat.top;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.theoneprobe.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.TwilightForestMod;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.init.TFBlocks;

/**
 * Shows creative players (or anyone holding TOP's creative probe) which mob a trapped (spawner) Chiseled Canopy Bookshelf will spawn.
 */
public enum ChiseledBookshelfSpawnProvider implements IProbeInfoProvider {

	INSTANCE;

	@Override
	public ResourceLocation getID() {
		return TwilightForestMod.prefix("chiseled_bookshelf_spawner");
	}

	@Override
	public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, Player player, Level level, BlockState blockState, IProbeHitData data) {
		if (blockState.is(TFBlocks.CHISELED_CANOPY_BOOKSHELF.get()) && blockState.getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
			if (player.isHolding(ModItems.CREATIVE_PROBE) || player.isCreative()) {
				BlockEntity te = level.getBlockEntity(data.getPos());
				if (te instanceof ChiseledCanopyShelfBlockEntity shelf) {
					// same guard as the 4.8 Jade provider (07af38da2): the next spawn data is only rolled once the spawner first ticks
					SpawnData logic = shelf.getSpawner().getNextSpawnData();
					if (logic != null) {
						EntityType.by(logic.entityToSpawn()).ifPresent(type -> info.horizontal(info.defaultLayoutStyle()
								.alignment(ElementAlignment.ALIGN_CENTER))
							.text(CompoundText.create().style(TextStyleClass.LABEL).text("Mob: ").info(type.getDescriptionId())));
					}
				}
			}
		}
	}
}
