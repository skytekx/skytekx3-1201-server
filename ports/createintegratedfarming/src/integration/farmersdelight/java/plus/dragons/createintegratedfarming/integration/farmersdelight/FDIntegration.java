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

package plus.dragons.createintegratedfarming.integration.farmersdelight;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.data.event.GatherDataEvent;
import plus.dragons.createintegratedfarming.integration.ModIntegration;
import plus.dragons.createintegratedfarming.integration.farmersdelight.data.FDRecipeProvider;
import plus.dragons.createintegratedfarming.integration.farmersdelight.ponder.FDPonderPlugin;
import plus.dragons.createintegratedfarming.integration.farmersdelight.registry.FDArmInteractionPointTypes;
import plus.dragons.createintegratedfarming.integration.farmersdelight.registry.FDBlockEntities;
import plus.dragons.createintegratedfarming.integration.farmersdelight.registry.FDBlockSpoutingBehaviours;
import plus.dragons.createintegratedfarming.integration.farmersdelight.registry.FDHarvestBehaviors;

// SkyTekx: CIFCommon constructs this entry point, since Forge 47 loads one @Mod class per mod id
public class FDIntegration {
    public FDIntegration(IEventBus modBus) {
        if (ModIntegration.FARMERS_DELIGHT.enabled()) {
            modBus.register(new Common());
            MinecraftForge.EVENT_BUS.register(FDBlockEntities.class);
            if (FMLLoader.getDist() == Dist.CLIENT)
                modBus.register(new Client());
        }
    }

    public static class Common {
        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            FDArmInteractionPointTypes.register();
        }

        @SubscribeEvent
        public void commonSetup(final FMLCommonSetupEvent event) {
            FDHarvestBehaviors.register();
            FDBlockSpoutingBehaviours.register();
        }

        @SubscribeEvent
        public void generate(final GatherDataEvent event) {
            var generator = event.getGenerator();
            var output = generator.getPackOutput();
            var server = event.includeServer();
            generator.addProvider(server, new FDRecipeProvider(output));
        }
    }

    public static class Client {
        @SubscribeEvent
        public void construct(final FMLConstructModEvent event) {
            FDPonderPlugin.register();
        }
    }
}
