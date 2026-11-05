package com.enderio.machines.common.obelisk;

import com.enderio.core.common.util.ChunkBoundLookup;
import com.enderio.machines.common.blockentity.base.ObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Keeps the obelisks of one kind in a level by the chunks that their area touches. It lives only in memory: the
 * obelisks register again when their chunks load.
 */
public abstract class ObeliskManager<T extends ObeliskBlockEntity<T>> implements IObeliskManagerCapability<T> {
    private final ChunkBoundLookup<T> lookup = new ChunkBoundLookup<>();

    @Override
    public void register(T obelisk) {
        lookup.addForBlockRadius(obelisk.getBlockPos(), obelisk.getRange(), obelisk);
    }

    @Override
    public void unregister(T obelisk) {
        lookup.remove(obelisk);
    }

    @Override
    public void update(T obelisk) {
        lookup.updateForBlockRadius(obelisk.getBlockPos(), obelisk.getRange(), obelisk);
    }

    @Override
    @Nullable
    public Set<T> getObelisksFor(BlockPos pos) {
        return lookup.getForChunk(new ChunkPos(pos));
    }

    /**
     * SkyTekx: the obelisks whose chunks hold either position, as a copy that a handler may outlive.
     */
    public static <T extends ObeliskBlockEntity<T>> Set<T> getObelisksFor(IObeliskManagerCapability<T> manager, BlockPos first, BlockPos second) {
        Set<T> result = new LinkedHashSet<>();
        Set<T> atFirst = manager.getObelisksFor(first);
        if (atFirst != null) {
            result.addAll(atFirst);
        }

        Set<T> atSecond = manager.getObelisksFor(second);
        if (atSecond != null) {
            result.addAll(atSecond);
        }

        return result;
    }
}
