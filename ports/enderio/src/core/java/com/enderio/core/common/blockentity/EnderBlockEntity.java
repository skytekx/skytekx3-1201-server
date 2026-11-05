package com.enderio.core.common.blockentity;

import com.enderio.api.UseOnly;
import com.enderio.api.capability.IEnderCapabilityProvider;
import com.enderio.core.common.network.C2SDataSlotChange;
import com.enderio.core.common.network.CoreNetwork;
import com.enderio.core.common.network.S2CDataSlotUpdate;
import com.enderio.core.common.network.slot.NetworkDataSlot;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base block entity class for EnderIO.
 * Handles data slot syncing and capability providers.
 */
public class EnderBlockEntity extends BlockEntity {

    public static final String DATA = "Data";
    public static final String INDEX = "Index";
    private boolean isChangedDeferred = true;
    private final List<NetworkDataSlot<?>> dataSlots = new ArrayList<>();

    // The data slots that a client may change, see addClientDataSlot.
    private final Set<NetworkDataSlot<?>> clientDataSlots = Collections.newSetFromMap(new IdentityHashMap<>());

    private final List<Runnable> afterDataSync = new ArrayList<>();

    private final Map<Capability<?>, IEnderCapabilityProvider<?>> capabilityProviders = new HashMap<>();

    public EnderBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    // region Ticking

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos pos, BlockState state, EnderBlockEntity blockEntity) {
        if (level.isClientSide) {
            blockEntity.clientTick();
        } else {
            blockEntity.serverTick();
        }
        blockEntity.endTick();
    }

    /**
     * Perform server-side ticking
     */
    @UseOnly(LogicalSide.SERVER)
    protected void serverTick() {
        // Perform syncing.
        if (level != null) {
            sync();
        }
    }

    /**
     * Perform client side ticking.
     */
    @UseOnly(LogicalSide.CLIENT)
    protected void clientTick() {

    }

    /**
     * Perform tick on both client and server, on the end.
     */
    protected void endTick() {
        if (this.level == null) {
            return;
        }
        if (isChangedDeferred) {
            isChangedDeferred = false;
            setChanged(level, getBlockPos(), getBlockState());
        }
    }

    @Override
    public void setChanged() {
        this.isChangedDeferred = true;
    }

    // endregion

    // region Sync

    /**
     * This is the initial packet sent to a client loading the block (or when it is placed).
     */
    @Override
    public CompoundTag getUpdateTag() {
        ListTag dataList = new ListTag();
        for (int i = 0; i < dataSlots.size(); i++) {
            var slot = dataSlots.get(i);
            var nbt = slot.serializeNBT(true);
            if (nbt == null) {
                continue;
            }

            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt(INDEX, i);
            slotTag.put(DATA, nbt);

            dataList.add(slotTag);
        }

        CompoundTag data = new CompoundTag();
        data.put(DATA, dataList);
        return data;
    }

    /**
     * This is the client handling the tag above.
     * @param syncData The {@link CompoundTag} sent from {@link BlockEntity#getUpdateTag()}
     */
    @Override
    public void handleUpdateTag(CompoundTag syncData) {
        if (syncData.contains(DATA, Tag.TAG_LIST)) {
            ListTag dataList = syncData.getList(DATA, Tag.TAG_COMPOUND);

            for (Tag dataEntry : dataList) {
                if (dataEntry instanceof CompoundTag slotData) {
                    int slotIdx = slotData.getInt(INDEX);
                    dataSlots.get(slotIdx).fromNBT(slotData.get(DATA));
                }
            }

            for (Runnable task : afterDataSync) {
                task.run();
            }
        }
    }

    @Nullable
    private FriendlyByteBuf createBufferSlotUpdate() {
        List<Integer> needsUpdate = new ArrayList<>();
        for (int i = 0; i < dataSlots.size(); i++) {
            if (dataSlots.get(i).needsUpdate()) {
                needsUpdate.add(i);
            }
        }

        if (needsUpdate.isEmpty()) {
            return null;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(needsUpdate.size());
        needsUpdate.forEach(i -> {
            buf.writeInt(i);
            dataSlots.get(i).writeBuffer(buf);
        });
        return buf;
    }

    /**
     * Add a data slot that the server syncs to the client. The server ignores any change a client sends for it.
     */
    public void addDataSlot(NetworkDataSlot<?> slot) {
        dataSlots.add(slot);
    }

    /**
     * Add a data slot that the server syncs to the client and that the client may change through {@link #clientUpdateSlot},
     * such as a setting of the block's screen. The value comes from the client, so the slot's setter must check it.
     */
    public void addClientDataSlot(NetworkDataSlot<?> slot) {
        addDataSlot(slot);
        clientDataSlots.add(slot);
    }

    public void addAfterSyncRunnable(Runnable runnable) {
        afterDataSync.add(runnable);
    }

    /**
     * Fire this when you change the value of a {@link NetworkDataSlot} on the client side.
     */
    @UseOnly(LogicalSide.CLIENT)
    public <T> void clientUpdateSlot(@Nullable NetworkDataSlot<T> slot, T value) {
        if (slot == null) {
            return;
        }

        if (dataSlots.contains(slot)) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(dataSlots.indexOf(slot));
            slot.toBuffer(buf, value);
            CoreNetwork.sendToServer(new C2SDataSlotChange(getBlockPos(), buf));
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_NEIGHBORS);
        }
    }

    /**
     * Sync the BlockEntity to all tracking players. Don't call this if you don't know what you do
     */
    @UseOnly(LogicalSide.SERVER)
    public void sync() {
        var syncData = createBufferSlotUpdate();
        if (syncData != null) {
            setChanged();
            CoreNetwork.sendToTracking(level.getChunkAt(getBlockPos()), new S2CDataSlotUpdate(getBlockPos(), syncData));
        }
    }

    @UseOnly(LogicalSide.CLIENT)
    public void clientHandleBufferSync(FriendlyByteBuf buf) {
        for (int amount = buf.readInt(); amount > 0; amount--) {
            int index = buf.readInt();
            dataSlots.get(index).fromBuffer(buf);
        }

        for (Runnable task : afterDataSync) {
            task.run();
        }
    }

    /**
     * Apply a data slot change that a client sent.
     * @throws IllegalStateException if the buffer is malformed, the index is out of range or the slot does not take client changes.
     */
    @UseOnly(LogicalSide.SERVER)
    public void serverHandleBufferChange(FriendlyByteBuf buf) {
        int index = -1;
        try {
            index = buf.readInt();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid buffer was passed over the network to the server.");
        }

        if (index < 0 || index >= dataSlots.size()) {
            throw new IllegalStateException("Data slot index " + index + " does not exist.");
        }

        NetworkDataSlot<?> slot = dataSlots.get(index);
        if (!clientDataSlots.contains(slot)) {
            throw new IllegalStateException("Data slot " + index + " cannot be changed by the client.");
        }

        try {
            slot.fromBuffer(buf);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Invalid buffer for data slot " + index + " was passed over the network to the server.", e);
        }
        slot.updateServerCallback();
    }

    // endregion

    // region Capabilities

    /**
     * Get all capability providers
     */
    public Map<Capability<?>, IEnderCapabilityProvider<?>> getCapabilityProviders() {
        return capabilityProviders;
    }

    /**
     * Add a capability provider to the block entity.
     */
    public void addCapabilityProvider(IEnderCapabilityProvider<?> provider) {
        capabilityProviders.put(provider.getCapabilityType(), provider);
    }

    /**
     * Invalidate capabilities serving the given side.
     */
    public void invalidateCaps(@Nullable Direction side) {
        for (IEnderCapabilityProvider<?> capProvider : capabilityProviders.values()) {
            capProvider.invalidateSide(side);
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (capabilityProviders.containsKey(cap)) {
            return capabilityProviders.get(cap).getCapability(side).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (IEnderCapabilityProvider<?> provider : capabilityProviders.values()) {
            provider.invalidateCaps();
        }
    }

    // endregion
}
