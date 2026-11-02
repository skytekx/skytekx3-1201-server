package twilightforest.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;

/**
 * Attributes of the Lich fight (backported from TF 4.8). They replace the hardcoded clone, shield and minion counts, so
 * datapacks and commands can tune a Lich. A Lich saved before they existed simply gets the defaults.
 */
public class TFAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TwilightForestMod.ID);

	public static final RegistryObject<Attribute> CLONE_COUNT = ATTRIBUTES.register("clone_count", () -> new RangedAttribute("attribute.name.lich.clone_count", 2, 0, 1024).setSyncable(true));
	public static final RegistryObject<Attribute> SHIELD_STRENGTH = ATTRIBUTES.register("max_shield_strength", () -> new RangedAttribute("attribute.name.lich.shield_strength", 6, 0, 1024).setSyncable(true));
	public static final RegistryObject<Attribute> MINION_COUNT = ATTRIBUTES.register("minion_count", () -> new RangedAttribute("attribute.name.lich.minion_count", 9, 0, 1024).setSyncable(true));
}
