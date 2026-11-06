/*
 * This file ("LensEvenMoarDeath.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items.lens;

import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.util.AAFakePlayers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class LensKiller extends LensDeath {

    @Override
    protected void onAttacked(LivingEntity entity, IAtomicReconstructor tile) {
        if (!tile.getWorldObject().isClientSide) {
            // SkyTekx: the kill is the named fake player's, standing at the reconstructor, so protection
            // plugins see who attacks
            try (AAFakePlayers.Use use = AAFakePlayers.use((ServerLevel) tile.getWorldObject(), tile.getPosition())) {
                entity.hurt(entity.damageSources().playerAttack(use.player()), 20);
            }
        }
    }

    @Override
    protected int getUsePerEntity() {
        return 2500;
    }
}
