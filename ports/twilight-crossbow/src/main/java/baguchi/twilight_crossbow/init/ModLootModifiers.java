package baguchi.twilight_crossbow.init;

import baguchi.twilight_crossbow.TwilightCrossBow;
import baguchi.twilight_crossbow.loot.AddTableLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 1.20.1 stand-in for NeoForge's built-in {@code neoforge:add_table} loot modifier, which upstream uses to put the
 * Crossbow Upgrade Smithing Template into Aurora Palace chests.
 */
public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TwilightCrossBow.MODID);

    public static final RegistryObject<Codec<AddTableLootModifier>> ADD_TABLE = LOOT_MODIFIERS.register("add_table", () -> AddTableLootModifier.CODEC);
}
