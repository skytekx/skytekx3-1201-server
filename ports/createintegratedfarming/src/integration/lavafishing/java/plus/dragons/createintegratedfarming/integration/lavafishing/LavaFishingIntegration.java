/*
 * Copyright (C) 2025  DragonsPlus, Copyright (C) 2026  SkyTekx
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

package plus.dragons.createintegratedfarming.integration.lavafishing;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import plus.dragons.createintegratedfarming.common.fishing.net.FishingNetCatchProviders;
import plus.dragons.createintegratedfarming.common.fishing.net.FishingNetEntityCaptures;
import plus.dragons.createintegratedfarming.common.fishing.net.FishingNetMedium;
import plus.dragons.createintegratedfarming.integration.ModIntegration;
import plus.dragons.createintegratedfarming.integration.lavafishing.fishing.LavaFishingNetCatchProvider;
import plus.dragons.createintegratedfarming.integration.lavafishing.ponder.LavaFishingNetPonderExample;

/**
 * SkyTekx: lets Lava Fishing (mod id {@code lavafishing}, the lava fishing mod of the SkyTekx pack) provide
 * the catches of the Lava Fishing Net, in the way upstream's Nether Depths Upgrade integration does. It
 * references Lava Fishing by registry names only, so it needs no compile dependency on that mod.
 */
public class LavaFishingIntegration {
    public LavaFishingIntegration(IEventBus modBus) {
        if (ModIntegration.LAVA_FISHING.enabled()) {
            modBus.register(new Common());
            if (FMLLoader.getDist() == Dist.CLIENT)
                modBus.register(new Client());
        }
    }

    public static class Common {
        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            FishingNetCatchProviders.register(
                    ModIntegration.LAVA_FISHING.asResource("fishing_net"),
                    FishingNetMedium.LAVA,
                    new LavaFishingNetCatchProvider());
            FishingNetEntityCaptures.register(entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())
                    .getNamespace()
                    .equals(ModIntegration.LAVA_FISHING.id()));
        }
    }

    public static class Client {
        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            LavaFishingNetPonderExample.register();
        }
    }
}
