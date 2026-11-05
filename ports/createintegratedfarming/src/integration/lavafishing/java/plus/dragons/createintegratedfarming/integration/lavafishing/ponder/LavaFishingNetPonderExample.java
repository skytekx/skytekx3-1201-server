/*
 * Copyright (C) 2026  SkyTekx
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

package plus.dragons.createintegratedfarming.integration.lavafishing.ponder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createintegratedfarming.client.ponder.FishingNetPonderExample;
import plus.dragons.createintegratedfarming.client.ponder.FishingNetPonderExamples;
import plus.dragons.createintegratedfarming.integration.ModIntegration;

/** SkyTekx: the Quartz Fish of Lava Fishing as the example catch of the Lava Fishing Net scene. */
public final class LavaFishingNetPonderExample {
    private LavaFishingNetPonderExample() {}

    public static void register() {
        ResourceLocation quartzFish = ModIntegration.LAVA_FISHING.asResource("quartz_fish");
        FishingNetPonderExamples.register(new FishingNetPonderExample(
                quartzFish,
                level -> BuiltInRegistries.ENTITY_TYPE.get(quartzFish).create(level),
                () -> new ItemStack(BuiltInRegistries.ITEM.get(quartzFish))));
    }
}
