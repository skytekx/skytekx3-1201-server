package com.mrh0.createaddition.event;

import com.mrh0.createaddition.CreateAddition;

import com.mrh0.createaddition.energy.HeldWireBounds;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.item.WireSpool;
import com.mrh0.createaddition.sound.CASoundScapes;
import com.mrh0.createaddition.util.ClientMinecraftWrapper;
import com.mrh0.createaddition.util.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAddition.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    public static boolean clientRenderHeldWire = false;

    @SubscribeEvent
    public static void playerRendererEvent(TickEvent.ClientTickEvent evt) {
        LocalPlayer player = ClientMinecraftWrapper.getPlayer();
        if(player == null) {
            HeldWireBounds.clear();
            return;
        }
        ItemStack stack = player.getInventory().getSelected();
        if(stack.isEmpty() || WireSpool.isRemover(stack.getItem())) {
            HeldWireBounds.clear();
            return;
        }
        Util.Triple<BlockPos, Integer, WireType> wireNode = Util.getWireNodeOfSpools(stack);
        clientRenderHeldWire = wireNode != null;
        // The start node's render box reaches the player while the wire is placed (a tick of slack).
        HeldWireBounds.set(wireNode == null ? null : wireNode.a, player.getBoundingBox().inflate(1));
    }

    @SubscribeEvent
    public static void tickSoundscapes(TickEvent.ClientTickEvent event) {
        CASoundScapes.tick();
    }

	// Fluid Fog TODO: update!
	/*@SubscribeEvent
	public static void getFogDensity(EntityViewRenderEvent.FogDensity event) {
		Camera info = event.getInfo();
		FluidState fluidState = info.getFluidInCamera();
		if (fluidState.isEmpty())
			return;
		Fluid fluid = fluidState.getType();

		if (fluid.isSame(CAFluids.SEED_OIL.get())) {
			event.setDensity(3.5f);
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public static void getFogColor(EntityViewRenderEvent.FogColors event) {
		Camera info = event.getInfo();
		FluidState fluidState = info.getFluidInCamera();
		if (fluidState.isEmpty())
			return;
		Fluid fluid = fluidState.getType();

		if (fluid.isSame(CAFluids.SEED_OIL.get())) {
			event.setRed(70 / 256f);
			event.setGreen(74 / 256f);
			event.setBlue(52 / 256f);
		}
	}*/

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new ResourceReloadListener());
        }
    }
}