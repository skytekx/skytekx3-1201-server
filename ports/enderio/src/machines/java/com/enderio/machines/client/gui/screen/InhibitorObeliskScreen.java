package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.machines.common.blockentity.InhibitorObeliskBlockEntity;
import com.enderio.machines.common.menu.InhibitorObeliskMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InhibitorObeliskScreen extends ObeliskScreen<InhibitorObeliskBlockEntity, InhibitorObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/inhibitor.png");

    public InhibitorObeliskScreen(InhibitorObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BG_TEXTURE);
    }
}
