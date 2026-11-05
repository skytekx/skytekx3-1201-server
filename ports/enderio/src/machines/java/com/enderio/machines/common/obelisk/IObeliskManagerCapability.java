package com.enderio.machines.common.obelisk;

import com.enderio.machines.common.blockentity.base.ObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The per level lookup of one kind of obelisk.
 * <p>
 * Forge names a capability after the erased type of its token, so a token of IObeliskManagerCapability&lt;X&gt; gives the
 * same capability for every X. Only the Aversion Obelisk uses this interface directly (enderio:aversion_obelisk_manager_capability).
 * SkyTekx: every other kind of obelisk gets a non-generic sub-interface with its own @AutoRegisterCapability, such as
 * IInhibitorObeliskManager, so that level.getCapability never hands one kind's manager to another.
 */
@AutoRegisterCapability
public interface IObeliskManagerCapability<T extends ObeliskBlockEntity<T>> {
    void register(T obelisk);

    void unregister(T obelisk);

    void update(T obelisk);

    @Nullable Set<T> getObelisksFor(BlockPos pos);
}
