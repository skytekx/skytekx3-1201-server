package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.TravellersComponentType;
import twilightforest.init.custom.TravellersModifierTypes;

import java.util.Arrays;
import java.util.List;

/**
 * An insertable modifier made of a marker component plus a component whose value is taken from the recipe input that
 * carries it (the hotbar stored in a Traveller's Belt), or the default value from the definition when no input has
 * one. Backport of 4.8's TransferableComponentModifier ({@code twilightforest:transferable_component}, 51781e15f).
 */
public record TransferableComponentModifier(
	EquipmentSlotGroup group,
	TravellersComponentType<Unit> markerComponent,
	TravellersComponentType.Value<?> transferableComponent,
	List<Component> description
) implements TransferableTravellersModifier {

	public static final Codec<TransferableComponentModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		EQUIPMENT_SLOTS_CODEC.fieldOf("equipment_slots").forGetter(TransferableComponentModifier::group),
		TravellersComponentType.UNIT_TYPE_CODEC.fieldOf("component").forGetter(TransferableComponentModifier::markerComponent),
		TravellersComponentType.Value.CODEC.fieldOf("transferable_components").forGetter(TransferableComponentModifier::transferableComponent),
		DESCRIPTION_CODEC.optionalFieldOf("description", List.of()).forGetter(TransferableComponentModifier::description)
	).apply(instance, TransferableComponentModifier::new));

	public <T> TransferableComponentModifier(EquipmentSlotGroup group, TravellersComponentType<Unit> component, TravellersComponentType<T> transferableComponent, T defaultTransferableComponentValue, List<Component> description) {
		this(group, component, new TravellersComponentType.Value<>(transferableComponent, defaultTransferableComponentValue), description);
	}

	@Override
	public TravellersModifierType getType() {
		return TravellersModifierTypes.TRANSFERABLE_COMPONENT.get();
	}

	@Override
	public boolean isAbility() {
		return false;
	}

	@Override
	public boolean addModifier(ItemStack stack) {
		this.markerComponent().set(stack, Unit.INSTANCE);
		this.transferableComponent().applyTo(stack);
		return true;
	}

	@Override
	public boolean hasModifier(ItemStack stack) {
		return this.markerComponent().has(stack) && this.transferableComponent().type().has(stack);
	}

	@Override
	public void removeModifier(ItemStack stack) {
		this.markerComponent().remove(stack);
		this.transferableComponent().type().remove(stack);
	}

	@Override
	public boolean transfer(ItemStack output, List<Ingredient> input) {
		List<ItemStack[]> dataComponentProviders = this.findDataComponentProviders(input);
		if (dataComponentProviders.isEmpty())
			return false;
		if (dataComponentProviders.size() > 1) {
			TwilightForestMod.LOGGER.error("A recipe with more than 2 dataComponentProviders was matched: {}. Please report to https://github.com/TeamTwilight/twilightforest/issues", input);
			return false;
		}
		ItemStack dataComponentProvider = dataComponentProviders.get(0)[0];
		this.markerComponent().set(output, Unit.INSTANCE);
		copy(this.transferableComponent().type(), dataComponentProvider, output);
		return true;
	}

	private static <T> void copy(TravellersComponentType<T> type, ItemStack from, ItemStack to) {
		T value = type.get(from);
		if (value != null)
			type.set(to, value);
	}

	public List<ItemStack[]> findDataComponentProviders(List<Ingredient> input) {
		return input.stream().map(Ingredient::getItems)
			.filter(itemStacks -> Arrays.stream(itemStacks)
				.anyMatch(itemStack -> this.transferableComponent().type().has(itemStack)))
			.toList();
	}

	@Override
	public List<Component> getDescription() {
		return this.description;
	}
}
