package de.ellpeck.actuallyadditions.mod.network;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.data.PlayerData;
import de.ellpeck.actuallyadditions.mod.particle.ParticleLaserItem;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityBase;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

/**
 * SkyTekx: the client side of the clientbound packets, the 1.20.1 form of upstream's
 * PacketHandlerClient (844efe42) and ClientPayloadHandler. The handlers in PacketHandler call it
 * through DistExecutor.unsafeRunWhenOn(Dist.CLIENT), so a dedicated server never loads it.
 */
@OnlyIn(Dist.CLIENT)
public final class PacketHandlerClient {

    private PacketHandlerClient() {
    }

    public static void handleLaser(CompoundTag compound, NetworkEvent.Context context) {
        AssetUtil.spawnLaserWithTimeClient(compound.getDouble("StartX"), compound.getDouble("StartY"), compound.getDouble("StartZ"), compound.getDouble("EndX"), compound.getDouble("EndY"), compound.getDouble("EndZ"), compound.getInt("Color"), compound.getInt("MaxAge"), compound.getDouble("RotationTime"), compound.getFloat("Size"), compound.getFloat("Alpha"));
    }

    public static void handleTileUpdate(CompoundTag compound, NetworkEvent.Context context) {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            BlockEntity tile = world.getBlockEntity(new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z")));
            if (tile instanceof TileEntityBase) {
                ((TileEntityBase) tile).readSyncableNBT(compound.getCompound("Data"), TileEntityBase.NBTType.SYNC);
            }
        }
    }

    public static void handleLaserParticle(CompoundTag compound, NetworkEvent.Context context) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack stack = ItemStack.of(compound);

        double inX = compound.getDouble("InX") + 0.5;
        double inY = compound.getDouble("InY") + 0.78;
        double inZ = compound.getDouble("InZ") + 0.5;

        double outX = compound.getDouble("OutX") + 0.5;
        double outY = compound.getDouble("OutY") + 0.525;
        double outZ = compound.getDouble("OutZ") + 0.5;

        // SkyTekx: the item rises from the relay it leaves and falls into the relay it goes to, as in
        // 99e16633. TileEntityItemInterface.doItemParticle names the target In and the source Out
        mc.level.addParticle(ParticleLaserItem.Factory.createData(stack, inX, inY, inZ),
                outX, outY, outZ, 0, 0.025, 0);
    }

    public static void handlePlayerData(CompoundTag compound, NetworkEvent.Context context) {
        CompoundTag dataTag = compound.getCompound("Data");
        // SkyTekx: the local player, as upstream's ClientPayloadHandler.handleSyncPlayer takes the
        // player of the clientbound flow. The base read context.getSender(), which is null for a
        // clientbound packet, so the client never got its player data and logged an ERROR
        Player player = Minecraft.getInstance().player;

        if (player != null) {
            PlayerData.getClientData(player.getUUID()).readFromNBT(dataTag, false);

            if (compound.getBoolean("Log")) {
                ActuallyAdditions.LOGGER.info("Receiving (new or changed) Player Data for player " + player.getName() + ".");
            }
        } else {
            ActuallyAdditions.LOGGER.error("Tried to receive Player Data for the current player, but he doesn't seem to be present!");
        }
    }
}
