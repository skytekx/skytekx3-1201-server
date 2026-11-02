package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersComponentType;
import twilightforest.init.custom.TravellersModifierTypes;

import java.util.List;

/**
 * An insertable modifier that sets one component on the gear, with the value given in its definition. Backport of
 * 4.8's TravellersComponentModifier ({@code twilightforest:component}).
 */
public record TravellersComponentModifier(EquipmentSlotGroup group, TravellersComponentType.Value<?> component, List<Component> description) implements InsertableTravellersModifier {

	public static final Codec<TravellersComponentModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		EQUIPMENT_SLOTS_CODEC.fieldOf("equipment_slots").forGetter(TravellersComponentModifier::group),
		TravellersComponentType.Value.CODEC.fieldOf("component").forGetter(TravellersComponentModifier::component),
		DESCRIPTION_CODEC.optionalFieldOf("description", List.of()).forGetter(TravellersComponentModifier::description)
	).apply(instance, TravellersComponentModifier::new));

	public <T> TravellersComponentModifier(EquipmentSlotGroup group, TravellersComponentType<T> component, T defaultValue, List<Component> description) {
		this(group, new TravellersComponentType.Value<>(component, defaultValue), description);
	}

	@Override
	public boolean isAbility() {
		return false;
	}

	@Override
	public boolean addModifier(ItemStack stack) {
		this.component().applyTo(stack);
		return true;
	}

	@Override
	public boolean hasModifier(ItemStack stack) {
		return this.component().type().has(stack);
	}

	@Override
	public void removeModifier(ItemStack stack) {
		this.component().type().remove(stack);
	}

	@Override
	public TravellersModifierType getType() {
		return TravellersModifierTypes.COMPONENT.get();
	}

	@Override
	public List<Component> getDescription() {
		return this.description;
	}
}
