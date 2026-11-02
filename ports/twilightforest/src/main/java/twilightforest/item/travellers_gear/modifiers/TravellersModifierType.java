package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;

/**
 * A type of Traveller's modifier, registered in {@code twilightforest.init.custom.TravellersModifierTypes}. 4.8 registers
 * the MapCodec itself; Forge 47 registries want a plain value type, so the codec sits behind this interface like the
 * other TF type registries of the fork.
 */
@FunctionalInterface
public interface TravellersModifierType {
	Codec<? extends TravellersModifier> codec();
}
