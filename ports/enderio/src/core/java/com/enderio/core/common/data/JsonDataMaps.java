package com.enderio.core.common.data;

import com.enderio.core.EnderCore;
import com.enderio.core.common.network.CoreNetwork;
import com.enderio.core.common.network.S2CDataMapSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SkyTekx: the registered {@link JsonDataMap}s and the Forge events that load and sync them.
 */
@Mod.EventBusSubscriber(modid = EnderCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JsonDataMaps {

    private static final List<JsonDataMap<?, ?>> DATA_MAPS = new CopyOnWriteArrayList<>();
    private static final Map<ResourceLocation, JsonDataMap<?, ?>> BY_ID = new ConcurrentHashMap<>();

    private JsonDataMaps() {
    }

    static void register(JsonDataMap<?, ?> dataMap) {
        if (BY_ID.putIfAbsent(dataMap.id(), dataMap) != null) {
            throw new IllegalArgumentException("A JSON data map with the id " + dataMap.id() + " is already registered");
        }

        DATA_MAPS.add(dataMap);
    }

    public static List<JsonDataMap<?, ?>> all() {
        return List.copyOf(DATA_MAPS);
    }

    @Nullable
    public static JsonDataMap<?, ?> get(ResourceLocation id) {
        return BY_ID.get(id);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        // Self check for the backport: names the data maps that this server reads.
        EnderCore.LOGGER.debug("Adding reload listeners for {} JSON data maps: {}", DATA_MAPS.size(), DATA_MAPS.stream().map(JsonDataMap::file).toList());

        for (JsonDataMap<?, ?> dataMap : DATA_MAPS) {
            event.addListener(dataMap.createReloadListener());
        }
    }

    // Values are resolved lazily against the tags, so lookups made with the old tags are dropped.
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            DATA_MAPS.forEach(JsonDataMap::invalidate);
        }
    }

    // Fired for a player who joins (before the tags and recipes are sent) and with no player after /reload.
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerPlayer player = event.getPlayer();
        PacketDistributor.PacketTarget target = player == null ? PacketDistributor.ALL.noArg() : PacketDistributor.PLAYER.with(() -> player);

        for (JsonDataMap<?, ?> dataMap : DATA_MAPS) {
            CoreNetwork.send(target, S2CDataMapSync.of(dataMap));
        }
    }

    // On a physical client the next world may come from another datapack set.
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DATA_MAPS.forEach(JsonDataMap::clearServerValues);
    }

    /**
     * Client side: forgets the values of the server that the client left.
     */
    public static void clearSyncedValues() {
        DATA_MAPS.forEach(JsonDataMap::clearSyncedValues);
    }
}
