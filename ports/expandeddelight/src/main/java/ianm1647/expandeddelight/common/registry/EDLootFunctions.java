package ianm1647.expandeddelight.common.registry;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.loot.CopyDrinkFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Upstream's juicer loot table copies the drink and container data components (minecraft:copy_components).
 * Forge 1.20.1 has no data components, so the juicer's drink travels as BlockEntityTag NBT, which this
 * function writes, as farmersdelight:copy_meal does for the cooking pot.
 */
public class EDLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ExpandedDelight.MODID);

    public static final RegistryObject<LootItemFunctionType> COPY_DRINK = LOOT_FUNCTIONS.register("copy_drink",
            () -> new LootItemFunctionType(new CopyDrinkFunction.Serializer()));
}
