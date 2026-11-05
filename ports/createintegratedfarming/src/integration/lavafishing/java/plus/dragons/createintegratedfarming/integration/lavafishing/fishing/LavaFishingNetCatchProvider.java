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

package plus.dragons.createintegratedfarming.integration.lavafishing.fishing;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createintegratedfarming.common.fishing.net.FishingNetCatchContext;
import plus.dragons.createintegratedfarming.common.fishing.net.FishingNetCatchProvider;
import plus.dragons.createintegratedfarming.integration.ModIntegration;

/**
 * SkyTekx: rolls the lava fishing loot tables of Lava Fishing 1.2.1, the Nether table in the Nether and
 * the lava table elsewhere, with the fishing loot parameters of the net.
 */
public class LavaFishingNetCatchProvider implements FishingNetCatchProvider {
    private static final ResourceLocation NETHER_FISHING = ModIntegration.LAVA_FISHING.asResource("gameplay/fishing/nether/fishing");
    private static final ResourceLocation LAVA_FISHING = ModIntegration.LAVA_FISHING.asResource("gameplay/fishing/lava/fishing");

    @Override
    public List<ItemStack> getCatch(FishingNetCatchContext context) {
        var lootTable = context.level().dimension() == Level.NETHER ? NETHER_FISHING : LAVA_FISHING;
        return context.level()
                .getServer()
                .getLootData()
                .getLootTable(lootTable)
                .getRandomItems(context.lootParams());
    }
}
