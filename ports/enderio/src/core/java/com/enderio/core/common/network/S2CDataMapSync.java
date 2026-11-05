package com.enderio.core.common.network;

import com.enderio.core.EnderCore;
import com.enderio.core.common.data.JsonDataMap;
import com.enderio.core.common.data.JsonDataMaps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * SkyTekx: sends all resolved values of one {@link JsonDataMap} to a client, like NeoForge does for the synced data maps
 * of Ender IO 8.x. The client needs them for display, such as the reagent multipliers of the Vat in its screen and in JEI.
 */
public class S2CDataMapSync implements Packet {

    @Nullable
    private final Payload<?> payload;

    private S2CDataMapSync(@Nullable Payload<?> payload) {
        this.payload = payload;
    }

    public static S2CDataMapSync of(JsonDataMap<?, ?> dataMap) {
        return new S2CDataMapSync(Payload.of(dataMap));
    }

    public S2CDataMapSync(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        JsonDataMap<?, ?> dataMap = JsonDataMaps.get(id);
        if (dataMap == null) {
            // Both sides run the same jar (the protocol version), so this only happens with a broken install.
            EnderCore.LOGGER.warn("Ignoring the values of the unknown JSON data map {}", id);
            buf.skipBytes(buf.readableBytes());
            this.payload = null;
        } else {
            this.payload = Payload.read(dataMap, buf);
        }
    }

    protected void write(FriendlyByteBuf buf) {
        if (payload != null) {
            payload.write(buf);
        }
    }

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getDirection() == NetworkDirection.PLAY_TO_CLIENT && payload != null;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        if (payload != null) {
            payload.apply();
        }
    }

    private record Payload<V>(JsonDataMap<?, V> dataMap, Map<ResourceLocation, V> values) {

        private static <V> Payload<V> of(JsonDataMap<?, V> dataMap) {
            return new Payload<>(dataMap, dataMap.getServerValuesById());
        }

        private static <V> Payload<V> read(JsonDataMap<?, V> dataMap, FriendlyByteBuf buf) {
            JsonDataMap.ValueSerializer<V> serializer = dataMap.serializer();
            return new Payload<>(dataMap, buf.readMap(FriendlyByteBuf::readResourceLocation, serializer::fromNetwork));
        }

        private void write(FriendlyByteBuf buf) {
            JsonDataMap.ValueSerializer<V> serializer = dataMap.serializer();
            buf.writeResourceLocation(dataMap.id());
            buf.writeMap(values, FriendlyByteBuf::writeResourceLocation, serializer::toNetwork);
        }

        private void apply() {
            dataMap.setSyncedValues(values);
        }
    }

    public static class Handler extends PacketHandler<S2CDataMapSync> {

        @Override
        public S2CDataMapSync fromNetwork(FriendlyByteBuf buf) {
            return new S2CDataMapSync(buf);
        }

        @Override
        public void toNetwork(S2CDataMapSync packet, FriendlyByteBuf buf) {
            packet.write(buf);
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
