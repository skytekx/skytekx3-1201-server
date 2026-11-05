package com.enderio.machines.common.obelisk;

import com.enderio.machines.common.blockentity.InhibitorObeliskBlockEntity;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

/**
 * SkyTekx: the manager capability of the Inhibitor Obelisk. It is a non-generic type of its own because Forge would
 * erase a token of IObeliskManagerCapability&lt;InhibitorObeliskBlockEntity&gt; to the Aversion Obelisk's capability.
 */
@AutoRegisterCapability
public interface IInhibitorObeliskManager extends IObeliskManagerCapability<InhibitorObeliskBlockEntity> {
}
