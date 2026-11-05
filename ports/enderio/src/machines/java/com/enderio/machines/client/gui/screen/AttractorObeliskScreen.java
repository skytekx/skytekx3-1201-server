package com.enderio.machines.client.gui.screen;

import com.enderio.EnderIO;
import com.enderio.machines.common.blockentity.AttractorObeliskBlockEntity;
import com.enderio.machines.common.menu.AttractorObeliskMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AttractorObeliskScreen extends ObeliskScreen<AttractorObeliskBlockEntity, AttractorObeliskMenu> {

    public static final ResourceLocation BG_TEXTURE = EnderIO.loc("textures/gui/attractor.png");

    public AttractorObeliskScreen(AttractorObeliskMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, BG_TEXTURE);
    }
}
