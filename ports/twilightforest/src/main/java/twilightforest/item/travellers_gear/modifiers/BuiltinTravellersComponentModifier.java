package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersComponentType;
import twilightforest.init.custom.TravellersModifierTypes;

/**
 * An ability a piece of gear has built in: present when the item carries the component as a default
 * ({@link twilightforest.item.travellers_gear.TravellersArmorItem#withDefaultComponent}). Backport of 4.8's
 * BuiltinTravellersComponentModifier ({@code twilightforest:builtin}).
 */
public record BuiltinTravellersComponentModifier(EquipmentSlotGroup group, TravellersComponentType<?> component) implements TravellersModifier {

	public static final Codec<BuiltinTravellersComponentModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		EQUIPMENT_SLOTS_CODEC.fieldOf("equipment_slots").forGetter(BuiltinTravellersComponentModifier::group),
		TravellersComponentType.CODEC.fieldOf("component").forGetter(BuiltinTravellersComponentModifier::component)
	).apply(instance, BuiltinTravellersComponentModifier::new));

	@Override
	public boolean hasModifier(ItemStack stack) {
		return this.component().has(stack);
	}

	@Override
	public boolean isAbility() {
		return true;
	}

	@Override
	public TravellersModifierType getType() {
		return TravellersModifierTypes.BUILTIN.get();
	}
}
