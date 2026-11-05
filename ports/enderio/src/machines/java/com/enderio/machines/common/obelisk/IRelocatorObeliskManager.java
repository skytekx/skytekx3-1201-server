package com.enderio.machines.common.obelisk;

import com.enderio.machines.common.blockentity.RelocatorObeliskBlockEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * SkyTekx: the manager capability of the Relocator Obelisk. It is a non-generic type of its own because Forge would
 * erase a token of IObeliskManagerCapability&lt;RelocatorObeliskBlockEntity&gt; to the Aversion Obelisk's capability.
 */
@AutoRegisterCapability
public interface IRelocatorObeliskManager extends IObeliskManagerCapability<RelocatorObeliskBlockEntity> {
}
