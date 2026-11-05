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

package plus.dragons.createintegratedfarming.integration.farmersdelight.registry;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.behaviours.BehaviourProvider;
import plus.dragons.createdragonsplus.common.registry.CDPCapabilities;
import plus.dragons.createintegratedfarming.common.CIFCommon;
import plus.dragons.createintegratedfarming.integration.farmersdelight.logistics.basket.BasketBehaviourProvider;
import vectorwing.farmersdelight.common.block.entity.BasketBlockEntity;

// SkyTekx: Forge 47 attaches the behaviour provider capability of Create: Dragons Plus to each basket
// with AttachCapabilitiesEvent, where NeoForge registers it in RegisterCapabilitiesEvent. The provider
// is built once per basket, on the first query.
public class FDBlockEntities {
    private static final ResourceLocation BASKET_BEHAVIOURS = CIFCommon.asResource("basket_behaviours");

    @SubscribeEvent
    public static void attachCapabilities(final AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof BasketBlockEntity basket))
            return;
        LazyOptional<BehaviourProvider> provider = LazyOptional.of(() -> new BasketBehaviourProvider(basket));
        event.addCapability(BASKET_BEHAVIOURS, new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
                return capability == CDPCapabilities.BEHAVIOUR_PROVIDER ? provider.cast() : LazyOptional.empty();
            }
        });
        event.addListener(provider::invalidate);
    }
}
