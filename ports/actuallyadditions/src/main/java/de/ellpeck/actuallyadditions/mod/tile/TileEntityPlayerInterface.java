/*
 * This file ("TileEntityPlayerInterface.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import java.util.UUID;

public class TileEntityPlayerInterface extends TileEntityBase implements IEnergyDisplay {

    public static final int DEFAULT_RANGE = 32;
    private final CustomEnergyStorage storage = new CustomEnergyStorage(30000, 50, 0);
    public final LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.of(() -> this.storage);
    public UUID connectedPlayer;
    public String playerName;
    private IItemHandler playerHandler;
    private LazyOptional<IItemHandler> lazyPlayerHandler;
    private Player oldPlayer;
    private int oldEnergy;
    private int range;

    public TileEntityPlayerInterface(BlockPos pos, BlockState state) {
        super(ActuallyBlocks.PLAYER_INTERFACE.getTileEntityType(), pos, state);
    }

    private Player getPlayer() {
        if (this.connectedPlayer != null && this.level != null) {
            Player player = this.level.getPlayerByUUID(this.connectedPlayer);
            if (player != null) {
                // SkyTekx: the range in blocks, as on 1.12. The base and main compare the squared
                // distance with the range, which cut the 32 blocks of the manual down to about 5.7
                if (player.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) <= (double) this.range * this.range) {
                    return player;
                }
            }
        }
        return null;
    }

    // SkyTekx: one LazyOptional per connected player, invalidated when the player changes, leaves the
    // range or the block goes away, as 44e05549 does with invalidateCapabilities. The base made a
    // new LazyOptional at every call, whose supplier returned null without a player, so asking it
    // for its handler threw
    @Override
    public LazyOptional<IItemHandler> getItemHandler(Direction facing) {
        Player player = this.getPlayer();

        if (this.oldPlayer != player || this.lazyPlayerHandler == null) {
            this.oldPlayer = player;

            this.playerHandler = player == null
                ? null
                : new PlayerInvWrapper(player.getInventory());

            this.invalidatePlayerHandler();
            IItemHandler handler = this.playerHandler;
            this.lazyPlayerHandler = handler == null
                ? LazyOptional.empty()
                : LazyOptional.of(() -> handler);
        }

        return this.lazyPlayerHandler;
    }

    private void invalidatePlayerHandler() {
        if (this.lazyPlayerHandler != null) {
            LazyOptional<IItemHandler> old = this.lazyPlayerHandler;
            this.lazyPlayerHandler = null;
            if (old.isPresent()) {
                old.invalidate();
            }
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.invalidatePlayerHandler();
        this.oldPlayer = null;
    }

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityPlayerInterface tile) {
            tile.clientTick();
        }
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityPlayerInterface tile) {
            tile.serverTick();

            boolean changed = false;

            tile.range = TileEntityPhantomface.upgradeRange(DEFAULT_RANGE, level, pos);

            Player player = tile.getPlayer();
            if (player != tile.oldPlayer) {
                // SkyTekx: drop the handler of a player who left the range, logged out or respawned
                tile.getItemHandler(null);
            }
            if (player != null) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (tile.storage.getEnergyStored() > 0) {
                        ItemStack slot = player.getInventory().getItem(i);
                        if (StackUtil.isValid(slot) && slot.getCount() == 1) {

                            int received = slot.getCapability(ForgeCapabilities.ENERGY).map(cap -> cap.receiveEnergy(tile.storage.getEnergyStored(), false)).orElse(0);
                            if (received > 0) {
                                tile.storage.extractEnergyInternal(received, false);
                            }
                        }
                    } else {
                        break;
                    }
                }
            }

            if (changed) {
                tile.setChanged();
                tile.sendUpdate();
            }

            if (tile.storage.getEnergyStored() != tile.oldEnergy && tile.sendUpdateWithInterval()) {
                tile.oldEnergy = tile.storage.getEnergyStored();
            }
        }
    }

    @Override
    public void writeSyncableNBT(CompoundTag compound, NBTType type) {
        super.writeSyncableNBT(compound, type);

        this.storage.writeToNBT(compound);
        if (this.connectedPlayer != null && type != NBTType.SAVE_BLOCK) {
            compound.putUUID("Player", this.connectedPlayer);
            compound.putString("PlayerName", this.playerName);
        }
    }

    @Override
    public void readSyncableNBT(CompoundTag compound, NBTType type) {
        super.readSyncableNBT(compound, type);

        this.storage.readFromNBT(compound);
        // SkyTekx: the key that writeSyncableNBT writes, as in d2d0afa6. The base looked for PlayerLeast,
        // which putUUID never writes, so the interface lost its player at every load. The long pair
        // PlayerMost and PlayerLeast is still read
        if (type != NBTType.SAVE_BLOCK) {
            if (compound.hasUUID("Player")) {
                this.connectedPlayer = compound.getUUID("Player");
                this.playerName = compound.getString("PlayerName");
            } else if (compound.contains("PlayerMost", Tag.TAG_LONG) && compound.contains("PlayerLeast", Tag.TAG_LONG)) {
                this.connectedPlayer = new UUID(compound.getLong("PlayerMost"), compound.getLong("PlayerLeast"));
                this.playerName = compound.getString("PlayerName");
            }
        }
    }

    @Override
    public CustomEnergyStorage getEnergyStorage() {
        return this.storage;
    }

    @Override
    public boolean needsHoldShift() {
        return false;
    }

    @Override
    public LazyOptional<IEnergyStorage> getEnergyStorage(Direction facing) {
        return this.lazyEnergy;
    }
}
