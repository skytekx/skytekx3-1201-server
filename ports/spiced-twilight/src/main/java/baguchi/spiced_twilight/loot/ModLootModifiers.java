package baguchi.spiced_twilight.loot;

import baguchi.spiced_twilight.SpicedTwilight;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SpicedTwilight.MODID);

    public static final RegistryObject<Codec<AddTableLootModifier>> ADD_TABLE = LOOT_MODIFIERS.register("add_table", () -> AddTableLootModifier.CODEC);
}
