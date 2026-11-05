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

import static plus.dragons.createintegratedfarming.common.CIFCommon.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import plus.dragons.createintegratedfarming.client.renderer.VacuumHarvesterRenderer;
import plus.dragons.createintegratedfarming.client.renderer.VacuumHarvesterVisual;
import plus.dragons.createintegratedfarming.common.farming.vacuum.VacuumHarvesterBlockEntity;
import plus.dragons.createintegratedfarming.common.ranching.roost.chicken.ChickenRoostBlockEntity;

public class CIFBlockEntities {
    public static final BlockEntityEntry<ChickenRoostBlockEntity> CHICKEN_ROOST = REGISTRATE
            .blockEntity("chicken_roost", ChickenRoostBlockEntity::new)
            .validBlock(CIFBlocks.CHICKEN_ROOST)
            .register();
    public static final BlockEntityEntry<VacuumHarvesterBlockEntity> VACUUM_HARVESTER = REGISTRATE
            .blockEntity("vacuum_harvester", VacuumHarvesterBlockEntity::new)
            .visual(() -> VacuumHarvesterVisual::new, false)
            .validBlock(CIFBlocks.VACUUM_HARVESTER)
            .renderer(() -> VacuumHarvesterRenderer::new)
            .register();

    // SkyTekx: Forge 47 has no RegisterCapabilitiesEvent. The roost and the harvester expose their item
    // handlers through getCapability, with the same sides as upstream's registerCapabilities.
    public static void register(IEventBus modBus) {}
}
