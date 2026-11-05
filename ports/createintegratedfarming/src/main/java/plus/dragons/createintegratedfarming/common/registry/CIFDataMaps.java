/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createintegratedfarming.common.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.PacketDistributor;
import plus.dragons.createintegratedfarming.common.CIFCommon;
import plus.dragons.createintegratedfarming.common.network.CIFPackets;
import plus.dragons.createintegratedfarming.common.network.ChickenFoodSyncPacket;
import plus.dragons.createintegratedfarming.common.ranching.roost.chicken.ChickenFoodFluid;
import plus.dragons.createintegratedfarming.common.ranching.roost.chicken.ChickenFoodItem;
import plus.dragons.createintegratedfarming.common.ranching.roost.chicken.ChickenFoodMap;

/**
 * SkyTekx: Forge 47 has no data maps. The two synced data maps of upstream become {@link ChickenFoodMap}s
 * that read the same files ({@code data/create_integrated_farming/data_maps/item/chicken_food.json} and
 * {@code .../fluid/chicken_food.json}) on every data pack reload and reach the clients with
 * {@link ChickenFoodSyncPacket} at each data pack sync. The defaults are data files, not datagen output:
 * {@code #create_integrated_farming:chicken_food} (the vanilla chicken food of 1.20.1) and
 * {@code #forge:plantoil}.
 */
public class CIFDataMaps {
    public static final ChickenFoodMap<Item, ChickenFoodItem> CHICKEN_FOOD_ITEMS = new ChickenFoodMap<>(
            CIFCommon.asResource("chicken_food"), "item", Registries.ITEM, BuiltInRegistries.ITEM, ChickenFoodItem.CODEC);
    public static final ChickenFoodMap<Fluid, ChickenFoodFluid> CHICKEN_FOOD_FLUIDS = new ChickenFoodMap<>(
            CIFCommon.asResource("chicken_food"), "fluid", Registries.FLUID, BuiltInRegistries.FLUID, ChickenFoodFluid.CODEC);

    public static void register(IEventBus modBus) {
        MinecraftForge.EVENT_BUS.addListener(CIFDataMaps::addReloadListener);
        MinecraftForge.EVENT_BUS.addListener(CIFDataMaps::onDatapackSync);
    }

    private static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) CIFDataMaps::reload);
    }

    private static void reload(ResourceManager manager) {
        CHICKEN_FOOD_ITEMS.reload(manager);
        CHICKEN_FOOD_FLUIDS.reload(manager);
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        var packet = ChickenFoodSyncPacket.create();
        if (event.getPlayer() != null)
            CIFPackets.CHANNEL.send(PacketDistributor.PLAYER.with(event::getPlayer), packet);
        else
            CIFPackets.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
