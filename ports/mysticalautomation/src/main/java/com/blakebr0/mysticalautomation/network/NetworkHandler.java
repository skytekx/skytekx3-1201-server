package com.blakebr0.mysticalautomation.network;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.network.payload.EnchanternatorSelectLevelPayload;
import com.blakebr0.mysticalautomation.network.payload.InfuserSelectIndexPayload;
import com.blakebr0.mysticalautomation.network.payload.ReloadIngredientCachePayload;
import com.blakebr0.mysticalautomation.network.payload.SetFakeRecipeSlotPayload;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * SkyTekx port: a Forge SimpleChannel for upstream's NeoForge payload registrar. Upstream registers its payloads
 * with protocol version "1", and this channel uses the same version and accepts only it on both sides.
 */
public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            MysticalAutomation.resource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkHandler() { }

    public static void register() {
        var id = 0;

        INSTANCE.messageBuilder(ReloadIngredientCachePayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ReloadIngredientCachePayload::write)
                .decoder(ReloadIngredientCachePayload::read)
                .consumerMainThread(ReloadIngredientCachePayload::handleClient)
                .add();
        INSTANCE.messageBuilder(InfuserSelectIndexPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(InfuserSelectIndexPayload::write)
                .decoder(InfuserSelectIndexPayload::read)
                .consumerMainThread(InfuserSelectIndexPayload::handleServer)
                .add();
        INSTANCE.messageBuilder(EnchanternatorSelectLevelPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EnchanternatorSelectLevelPayload::write)
                .decoder(EnchanternatorSelectLevelPayload::read)
                .consumerMainThread(EnchanternatorSelectLevelPayload::handleServer)
                .add();
        INSTANCE.messageBuilder(SetFakeRecipeSlotPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetFakeRecipeSlotPayload::write)
                .decoder(SetFakeRecipeSlotPayload::read)
                .consumerMainThread(SetFakeRecipeSlotPayload::handleServer)
                .add();
    }
}
