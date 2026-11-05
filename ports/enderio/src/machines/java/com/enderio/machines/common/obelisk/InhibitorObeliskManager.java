package com.enderio.machines.common.obelisk;

import com.enderio.EnderIO;
import com.enderio.machines.common.blockentity.InhibitorObeliskBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * SkyTekx: backported from 8.x (f216165c6, 87eb5293c, d8261275b, a0189250c).
 * Cancels every EntityTeleportEvent whose target or start lies in the area of an active Inhibitor Obelisk, except the
 * /tp and /spreadplayers commands. Ender pearls, chorus fruit, endermen, shulkers, the travel staff and anchors of
 * Ender IO and every mod that posts the event are covered.
 */
@Mod.EventBusSubscriber(modid = EnderIO.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InhibitorObeliskManager extends ObeliskManager<InhibitorObeliskBlockEntity> implements IInhibitorObeliskManager {

    public static LazyOptional<IInhibitorObeliskManager> getManager(ServerLevel level) {
        return level.getCapability(ObeliskManagerAttacher.INHIBITOR_OBELISK_MANAGER_CAPABILITY);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onTeleportEvent(EntityTeleportEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Don't work on server commands
        if (event instanceof EntityTeleportEvent.TeleportCommand || event instanceof EntityTeleportEvent.SpreadPlayersCommand) {
            return;
        }

        // If there is no obelisk manager, there is nothing to do.
        getManager(serverLevel).ifPresent(obeliskManager -> {
            // Floor the positions, a cast to int rounds toward zero and misses the chunk at negative coordinates.
            BlockPos target = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
            BlockPos prev = BlockPos.containing(event.getPrevX(), event.getPrevY(), event.getPrevZ());

            Set<InhibitorObeliskBlockEntity> atTarget = obeliskManager.getObelisksFor(target);
            Set<InhibitorObeliskBlockEntity> atPrev = obeliskManager.getObelisksFor(prev);
            if ((atTarget == null || atTarget.isEmpty()) && (atPrev == null || atPrev.isEmpty())) {
                return;
            }

            // Upstream only looks at the start when no obelisk covers the target chunk, this checks both chunks.
            for (InhibitorObeliskBlockEntity obelisk : ObeliskManager.getObelisksFor(obeliskManager, target, prev)) {
                if (obelisk.handleTeleportEvent(event)) {
                    break;
                }
            }
        });
    }
}
