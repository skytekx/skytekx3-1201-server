package ianm1647.expandeddelight.common;

import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.world.level.block.ComposterBlock;

/**
 * The composter chances of upstream's data/neoforge/data_maps/item/compostables.json (ea82640), which
 * Forge 1.20.1 cannot read. Called from FMLCommonSetupEvent#enqueueWork, as Farmer's Delight does.
 */
public class EDCompostables {
    public static void register() {
        ComposterBlock.COMPOSTABLES.put(EDItems.ASPARAGUS.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.ASPARAGUS_SEEDS.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.SWEET_POTATO.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.CHILI_PEPPER.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.CHILI_PEPPER_SEEDS.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.PEANUT.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.CRANBERRIES.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.CHOCOLATE_COOKIE.get(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(EDItems.SUGAR_COOKIE.get(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(EDItems.SNICKERDOODLE.get(), 0.85F);
        ComposterBlock.COMPOSTABLES.put(EDItems.WILD_ASPARAGUS.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.WILD_SWEET_POTATO.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.WILD_CHILI_PEPPER.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(EDItems.WILD_PEANUTS.get(), 0.65F);
    }
}
