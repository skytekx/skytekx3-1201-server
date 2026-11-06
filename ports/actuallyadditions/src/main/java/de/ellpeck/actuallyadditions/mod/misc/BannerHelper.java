/*
 * This file ("BannerHelper.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.misc;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// SkyTekx: upstream 659779b1, as on 1.20.4. The four patterns live in the banner pattern registry of 1.20.1 and
// replace the base's 1.12 helper, whose enum hack did nothing. Each texture sits where BannerPattern.location
// derives it from the pattern id, textures/entity/banner/<id>.png and textures/entity/shield/<id>.png of this
// namespace (7245af2a), which the banner and shield atlases of 1.20.1 scan in every namespace. The hash name is
// what a banner saves for the pattern
public final class BannerHelper {
    public static final DeferredRegister<BannerPattern> BANNER_PATTERNS = DeferredRegister.create(Registries.BANNER_PATTERN, ActuallyAdditions.MODID);

    public static final RegistryObject<BannerPattern> DRILL = BANNER_PATTERNS.register("drill", () -> new BannerPattern(ActuallyAdditions.MODID + ":drill"));
    public static final RegistryObject<BannerPattern> LEAF_BLO = BANNER_PATTERNS.register("leaf_blo", () -> new BannerPattern(ActuallyAdditions.MODID + ":leaf_blo"));
    public static final RegistryObject<BannerPattern> PHAN_CON = BANNER_PATTERNS.register("phan_con", () -> new BannerPattern(ActuallyAdditions.MODID + ":phan_con"));
    public static final RegistryObject<BannerPattern> BOOK = BANNER_PATTERNS.register("book", () -> new BannerPattern(ActuallyAdditions.MODID + ":book"));

    public static void init(IEventBus eventBus) {
        BANNER_PATTERNS.register(eventBus);
    }
}
