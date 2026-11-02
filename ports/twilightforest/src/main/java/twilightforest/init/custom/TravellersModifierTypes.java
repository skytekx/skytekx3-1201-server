package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.item.travellers_gear.modifiers.BuiltinTravellersComponentModifier;
import twilightforest.item.travellers_gear.modifiers.TransferableComponentModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersComponentModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersEntryModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersModifierType;

import java.util.function.Supplier;

/**
 * The types of Traveller's modifiers, the static {@code twilight:travellers_modifier_type} registry that the
 * {@code type} field of a modifier definition names. Backport of 4.8's TravellersModifierTypes.
 */
public final class TravellersModifierTypes {
	public static final ResourceKey<Registry<TravellersModifierType>> TRAVELLERS_MODIFIER_TYPE_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("travellers_modifier_type"));

	public static final DeferredRegister<TravellersModifierType> TYPES = DeferredRegister.create(TRAVELLERS_MODIFIER_TYPE_KEY, TwilightForestMod.ID);
	// Synced like 4.8's registry, since clients decode the synced modifier definitions with it; no ids are saved
	public static final Supplier<IForgeRegistry<TravellersModifierType>> REGISTRY = TYPES.makeRegistry(() -> new RegistryBuilder<TravellersModifierType>().disableSaving());

	public static final Codec<TravellersModifierType> TYPE_CODEC = ExtraCodecs.lazyInitializedCodec(() -> REGISTRY.get().getCodec());

	public static final RegistryObject<TravellersModifierType> ATTRIBUTE_ENTRY = TYPES.register("attribute", () -> () -> TravellersEntryModifier.CODEC);
	public static final RegistryObject<TravellersModifierType> BUILTIN = TYPES.register("builtin", () -> () -> BuiltinTravellersComponentModifier.CODEC);
	public static final RegistryObject<TravellersModifierType> COMPONENT = TYPES.register("component", () -> () -> TravellersComponentModifier.CODEC);
	public static final RegistryObject<TravellersModifierType> TRANSFERABLE_COMPONENT = TYPES.register("transferable_component", () -> () -> TransferableComponentModifier.CODEC);

	private TravellersModifierTypes() {
	}
}
