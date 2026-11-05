package com.mrh0.createaddition.network;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import com.mrh0.createaddition.CreateAddition;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class ObservePacket {
	private BlockPos pos;
	private int node;
	
	public ObservePacket(BlockPos pos, int node) {
		this.pos = pos;
		this.node = node;
	}
	
	public static void encode(ObservePacket packet, FriendlyByteBuf tag) {
        tag.writeBlockPos(packet.pos);
        tag.writeInt(packet.node);
    }
	
	public static ObservePacket decode(FriendlyByteBuf buf) {
		ObservePacket scp = new ObservePacket(buf.readBlockPos(), buf.readInt());
        return scp;
    }
	
	public static void handle(ObservePacket pkt, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			try {
				ServerPlayer player = ctx.get().getSender();
				
				if (player != null) {
					sendUpdate(pkt, player);
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	// SkyTekx hardening, not upstream: the client chooses the position, so the server only answers
	// for a loaded block near the player and at most every MIN_INTERVAL_TICKS per player. The client
	// sends at most every 10 ticks (send below), and goggles observe the block in reach or, for the
	// Modular Accumulator, its controller, which stays within a few blocks. Without the checks a
	// modified client could make getBlockEntity load or generate chunks anywhere on the server thread.
	private static final double MAX_DISTANCE_SQR = 64d * 64d;
	private static final int MIN_INTERVAL_TICKS = 5;
	// Server thread only (handle runs sendUpdate through enqueueWork). Weak keys, so a player that
	// left or respawned as a new entity does not stay in the map.
	private static final Map<ServerPlayer, Integer> LAST_ACCEPTED_TICK = new WeakHashMap<>();

	private static boolean mayObserve(ObservePacket pkt, ServerPlayer player) {
		int now = player.server.getTickCount();
		Integer last = LAST_ACCEPTED_TICK.get(player);
		if (last != null && now - last < MIN_INTERVAL_TICKS) return false;
		Level level = player.level();
		if (!level.isLoaded(pkt.pos)) return false;
		if (player.distanceToSqr(Vec3.atCenterOf(pkt.pos)) > MAX_DISTANCE_SQR) return false;
		LAST_ACCEPTED_TICK.put(player, now);
		return true;
	}

	private static void sendUpdate(ObservePacket pkt, ServerPlayer player) {
		if (!mayObserve(pkt, player)) return;
		BlockEntity te = (BlockEntity) player.level().getBlockEntity(pkt.pos);
        if (te != null) {
        	if(te instanceof IObserveTileEntity) {
	        	IObserveTileEntity ote = (IObserveTileEntity) te;
	        	ote.onObserved(player, pkt);
	            Packet<ClientGamePacketListener> supdatetileentitypacket = te.getUpdatePacket();
	            if (supdatetileentitypacket != null)
	                player.connection.send(supdatetileentitypacket);
        	}
        }
    }
	
	private static int cooldown = 0;
	public static void tick() {
		cooldown--;
		if(cooldown < 0)
			cooldown = 0;
	}
	
	public static boolean send(BlockPos pos, int node) {
		if(cooldown > 0)
			return false;
		cooldown = 10;
		CreateAddition.Network.sendToServer(new ObservePacket(pos, node));
		return true;
	}
	
	public BlockPos getPos() {
		return pos;
	}
	
	public int getNode() {
		return node;
	}
}

