package twilightforest.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import twilightforest.TwilightForestMod;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.block.entity.bookshelf.ChiseledCanopyShelfBlockEntity;
import twilightforest.init.TFBlocks;

/**
 * Shows creative players which mob a trapped (spawner) Chiseled Canopy Bookshelf will spawn, like Jade's own mob spawner line.
 */
public enum ChiseledBookshelfSpawnProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockState().is(TFBlocks.CHISELED_CANOPY_BOOKSHELF.get()) && accessor.getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
			if (accessor.getPlayer().isCreative()) {
				BlockEntity te = accessor.getBlockEntity();
				if (te instanceof ChiseledCanopyShelfBlockEntity shelf) {
					// 4.8 07af38da2: the next spawn data is only rolled once the spawner first ticks
					SpawnData logic = shelf.getSpawner().getNextSpawnData();
					if (logic != null) {
						EntityType.by(logic.entityToSpawn()).ifPresent(type -> {
							// Jade 11 has no ITooltip#replace, so swap the object name line the way its own MobSpawnerProvider does
							tooltip.remove(Identifiers.CORE_OBJECT_NAME);
							tooltip.add(0, IThemeHelper.get().title(Component.translatable("jade.spawner", accessor.getBlock().getName().getString(), type.getDescription().getString())), Identifiers.CORE_OBJECT_NAME);
						});
					}
				}
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return TwilightForestMod.prefix("chiseled_bookshelf_spawner");
	}
}
