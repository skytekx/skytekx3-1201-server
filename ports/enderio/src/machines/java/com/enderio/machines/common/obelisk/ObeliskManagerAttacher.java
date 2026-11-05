package com.enderio.machines.common.obelisk;

import com.enderio.EnderIO;
import com.enderio.machines.common.blockentity.AversionObeliskBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attaches one obelisk manager per kind to every server level. The managers hold no saved data.
 */
@Mod.EventBusSubscriber(modid = EnderIO.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ObeliskManagerAttacher {
    public static final Capability<IObeliskManagerCapability<AversionObeliskBlockEntity>> AVERSION_OBELISK_MANAGER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IInhibitorObeliskManager> INHIBITOR_OBELISK_MANAGER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IRelocatorObeliskManager> RELOCATOR_OBELISK_MANAGER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Level> event) {
        if (!(event.getObject() instanceof ServerLevel)) return;

        attach(event, "aversion_obelisk_manager_capability", AVERSION_OBELISK_MANAGER_CAPABILITY, new AversionObeliskManager());
        attach(event, "inhibitor_obelisk_manager_capability", INHIBITOR_OBELISK_MANAGER_CAPABILITY, new InhibitorObeliskManager());
        attach(event, "relocator_obelisk_manager_capability", RELOCATOR_OBELISK_MANAGER_CAPABILITY, new RelocatorObeliskManager());
    }

    private static <C> void attach(AttachCapabilitiesEvent<Level> event, String name, Capability<C> capability, C manager) {
        LazyOptional<C> storage = LazyOptional.of(() -> manager);

        ICapabilityProvider provider = new ICapabilityProvider() {
            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
                return capability.orEmpty(cap, storage);
            }
        };

        event.addCapability(new ResourceLocation(EnderIO.MODID, name), provider);
    }
}
