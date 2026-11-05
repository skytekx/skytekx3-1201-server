package com.mrh0.createaddition.network;

import java.util.function.Supplier;

import com.mrh0.createaddition.CreateAddition;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * SkyTekx: the 1.20.1 form of the 1.21.1 TimeRemainingPacketPayload (upstream 54cf0c9). The server sends it
 * to a player who observes a Liquid Blaze Burner or a Tesla Coil through goggles, as the answer to an
 * {@link ObservePacket}. It carries the burn or charge time left in ticks, or -1 while a Tesla Coil gets no
 * energy. Message id {@link CreateAddition#PACKET_TIME_REMAINING}, server to client only.
 */
public class TimeRemainingPacket {
	private final int timeRemaining;

	public static int clientTimeRemaining = 0;

	public TimeRemainingPacket(int timeRemaining) {
		this.timeRemaining = timeRemaining;
	}

	public static void encode(TimeRemainingPacket packet, FriendlyByteBuf buf) {
		buf.writeVarInt(packet.timeRemaining);
	}

	public static TimeRemainingPacket decode(FriendlyByteBuf buf) {
		return new TimeRemainingPacket(buf.readVarInt());
	}

	public static void handle(TimeRemainingPacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> updateClientCache(pkt.timeRemaining)));
		ctx.get().setPacketHandled(true);
	}

	public static void updateClientCache(int timeRemaining) {
		clientTimeRemaining = timeRemaining;
	}

	public static void send(int timeRemaining, ServerPlayer player) {
		CreateAddition.Network.send(PacketDistributor.PLAYER.with(() -> player), new TimeRemainingPacket(timeRemaining));
	}

	public int getTimeRemaining() {
		return timeRemaining;
	}
}
