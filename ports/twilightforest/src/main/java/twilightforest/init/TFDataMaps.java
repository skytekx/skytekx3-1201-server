package twilightforest.init;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.network.SyncDataMapPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.datamaps.DataMapLoader;
import twilightforest.util.datamaps.DataMapType;
import twilightforest.util.datamaps.EntityTransformation;
import twilightforest.util.datamaps.MagicMapBiomeColor;
import twilightforest.util.datamaps.OreMapOreColor;

import java.util.List;

/**
 * TF 4.8's NeoForge data maps, loaded from datapacks by {@link DataMapLoader}. See {@link DataMapType} for the file format.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class TFDataMaps {

	public static final DataMapType<Biome, MagicMapBiomeColor> MAGIC_MAP_BIOME_COLOR = new DataMapType<>(
			TwilightForestMod.prefix("magic_map_color"), Registries.BIOME, MagicMapBiomeColor.CODEC);

	public static final DataMapType<Block, OreMapOreColor> ORE_MAP_ORE_COLOR = new DataMapType<>(
			TwilightForestMod.prefix("ore_map_color"), Registries.BLOCK, OreMapOreColor.CODEC);

	// entities killed by ominous fire turn into the given entity type
	public static final DataMapType<EntityType<?>, EntityTransformation> OMINOUS_FIRE = new DataMapType<>(
			TwilightForestMod.prefix("ominous_fire"), Registries.ENTITY_TYPE, EntityTransformation.CODEC);

	private static final List<DataMapType<?, ?>> DATA_MAPS = List.of(MAGIC_MAP_BIOME_COLOR, ORE_MAP_ORE_COLOR, OMINOUS_FIRE);

	// 4.8 registers every data map as synced. Only the ominous fire map has a client reader here (the recipe viewers),
	// so only it is sent; add a data map here once client code reads it through DataMapType#getSyncedValues
	private static final List<DataMapType<?, ?>> SYNCED_DATA_MAPS = List.of(OMINOUS_FIRE);

	@Nullable
	public static DataMapType<?, ?> getSyncedDataMap(ResourceLocation id) {
		for (DataMapType<?, ?> type : SYNCED_DATA_MAPS) {
			if (type.id().equals(id))
				return type;
		}
		return null;
	}

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener(new DataMapLoader(DATA_MAPS));
	}

	// Sent on join and after every /reload, before the recipe and tag packets that make the recipe viewers reload
	@SubscribeEvent
	public static void syncDataMaps(OnDatapackSyncEvent event) {
		RegistryAccess access = event.getPlayerList().getServer().registryAccess();
		List<ServerPlayer> players = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
		for (DataMapType<?, ?> type : SYNCED_DATA_MAPS) {
			SyncDataMapPacket packet = SyncDataMapPacket.create(type, access);
			for (ServerPlayer player : players) {
				if (!(player instanceof FakePlayer)) {
					TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
				}
			}
		}
	}

	// Tag entries are resolved lazily, so lookups made with the old tags have to be redone
	@SubscribeEvent
	public static void tagsUpdated(TagsUpdatedEvent event) {
		DATA_MAPS.forEach(DataMapType::invalidate);
	}
}
