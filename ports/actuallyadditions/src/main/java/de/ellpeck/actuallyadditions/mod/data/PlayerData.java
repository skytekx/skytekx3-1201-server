/*
 * This file ("PlayerData.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerData {

    // SkyTekx: the client's own copy, which SYNC_PLAYER_DATA fills (upstream 2a4e9666). The base
    // cast every level to ServerLevel, so a call from client code threw a ClassCastException
    private static PlayerSave clientData = null;

    public static PlayerSave getDataFromPlayer(Player player) {
        if (!(player.getCommandSenderWorld() instanceof ServerLevel level)) {
            // SkyTekx: only getClientData, which the sync packet of the local player calls, keys
            // the client copy. Any other player of the client level, such as a remote player in a
            // tick event, gets a throwaway copy, so that it cannot replace the synced data of the
            // local player
            if (clientData != null && player.getUUID().equals(clientData.id)) {
                return clientData;
            }
            return new PlayerSave(player.getUUID());
        }
        WorldData worldData = WorldData.get(level);
        ConcurrentHashMap<UUID, PlayerSave> data = worldData.playerSaveData;
        UUID id = player.getUUID();

        if (data.containsKey(id)) {
            PlayerSave save = data.get(id);
            if (save != null && save.id != null && save.id.equals(id)) {
                return save;
            }
        }

        //Add Data if none is existant
        PlayerSave save = new PlayerSave(id);
        data.put(id, save);
        worldData.setDirty();
        return save;
    }

    /**
     * SkyTekx: the client copy for the local player with this UUID, a new one when another player
     * logs in on this client, so that no data carries over. Only the client handler of
     * SYNC_PLAYER_DATA calls it.
     */
    public static PlayerSave getClientData(UUID localPlayer) {
        if (clientData == null || !localPlayer.equals(clientData.id)) {
            clientData = new PlayerSave(localPlayer);
        }
        return clientData;
    }

    public static class PlayerSave {

        public UUID id;

        public boolean bookGottenAlready;
        public boolean hasBatWings;
        public boolean shouldDisableBatWings;
        public int batWingsFlyTime;

        // SkyTekx: the bookmarks, the trials and the tutorial flag of the 1.12 booklet are gone, as
        // upstream c8e90aef removes them. Nothing used them since the booklet went. An old save
        // still loads, the three keys Bookmarks, Trials and DidTutorial are just no longer read
        // or written

//        @OnlyIn(Dist.CLIENT)
//        public GuiBooklet lastOpenBooklet;

        public PlayerSave(UUID id) {
            this.id = id;
        }

        public void readFromNBT(CompoundTag compound, boolean savingToFile) {
            this.bookGottenAlready = compound.getBoolean("BookGotten");

            this.hasBatWings = compound.getBoolean("HasBatWings");
            this.batWingsFlyTime = compound.getInt("BatWingsFlyTime");

            if (!savingToFile) {
                this.shouldDisableBatWings = compound.getBoolean("ShouldDisableWings");
            }
        }

        public void writeToNBT(CompoundTag compound, boolean savingToFile) {
            compound.putBoolean("BookGotten", this.bookGottenAlready);

            compound.putBoolean("HasBatWings", this.hasBatWings);
            compound.putInt("BatWingsFlyTime", this.batWingsFlyTime);

            if (!savingToFile) {
                compound.putBoolean("ShouldDisableWings", this.shouldDisableBatWings);
            }
        }
    }

}
