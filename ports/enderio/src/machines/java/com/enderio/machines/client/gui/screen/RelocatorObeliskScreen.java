package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.machines.common.blockentity.RelocatorObeliskBlockEntity;
import com.enderio.machines.common.menu.RelocatorObeliskMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RelocatorObeliskScreen extends ObeliskScreen<RelocatorObeliskBlockEntity, RelocatorObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/relocator.png");

    public RelocatorObeliskScreen(RelocatorObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BG_TEXTURE);
    }
}
