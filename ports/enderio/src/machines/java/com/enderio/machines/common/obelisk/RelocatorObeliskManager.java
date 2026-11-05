package com.enderio.machines.common.obelisk;

import com.enderio.EnderIO;
import com.enderio.machines.common.blockentity.RelocatorObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

/**
 * SkyTekx: backported from 8.x (f216165c6, 87eb5293c).
 * Moves the natural spawns in the area of an active Relocator Obelisk next to the obelisk.
 */
@Mod.EventBusSubscriber(modid = EnderIO.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RelocatorObeliskManager extends ObeliskManager<RelocatorObeliskBlockEntity> implements IRelocatorObeliskManager {

    public static LazyOptional<IRelocatorObeliskManager> getManager(ServerLevel level) {
        return level.getCapability(ObeliskManagerAttacher.RELOCATOR_OBELISK_MANAGER_CAPABILITY);
    }

    // Low priority, so the handler runs after AversionObeliskManager at normal priority and sees its cancellation.
    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpawnEvent(MobSpawnEvent.FinalizeSpawn event) {
        // Only affects natural spawns. A spawn that an Aversion Obelisk already cancelled stays where it is.
        if (event.getSpawnType() != MobSpawnType.NATURAL || event.isSpawnCancelled()) {
            return;
        }

        // If there is no obelisk manager, there is nothing to do.
        ServerLevelAccessor levelAccessor = event.getLevel();
        ServerLevel level = levelAccessor.getLevel();
        getManager(level).ifPresent(obeliskManager -> {
            // Floor the position, a cast to int rounds toward zero and misses the chunk at negative coordinates.
            var pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

            Set<RelocatorObeliskBlockEntity> obelisks = obeliskManager.getObelisksFor(pos);
            if (obelisks == null || obelisks.isEmpty()) {
                return;
            }

            // A copy, the teleport event that a relocation posts runs other handlers.
            for (RelocatorObeliskBlockEntity obelisk : List.copyOf(obelisks)) {
                if (obelisk.handleSpawnEvent(event)) {
                    break;
                }
            }
        });
    }
}
