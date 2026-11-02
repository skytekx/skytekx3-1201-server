package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersComponentType;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.custom.TravellersModifierTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A modifier that adds attribute modifiers to the gear, marked by a Unit component. Built-in ones only carry the marker:
 * their attributes come with the item. Backport of 4.8's TravellersEntryModifier ({@code twilightforest:attribute});
 * the inserted attributes live in {@link TravellersGearData#INSERTED_ATTRIBUTE_MODIFIERS} instead of the stack's
 * attribute modifier component.
 */
public record TravellersEntryModifier(EquipmentSlotGroup group, List<TravellersAttributeEntry> modifiers, TravellersComponentType<Unit> markerComponent, List<Component> description, boolean builtin) implements InsertableTravellersModifier {

	public static final Codec<TravellersEntryModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		EQUIPMENT_SLOTS_CODEC.fieldOf("equipment_slots").forGetter(TravellersEntryModifier::group),
		TravellersAttributeEntry.CODEC.listOf().fieldOf("attribute_modifiers").forGetter(TravellersEntryModifier::modifiers),
		TravellersComponentType.UNIT_TYPE_CODEC.fieldOf("component").forGetter(TravellersEntryModifier::markerComponent),
		DESCRIPTION_CODEC.optionalFieldOf("description", List.of()).forGetter(TravellersEntryModifier::description),
		Codec.BOOL.fieldOf("builtin_modifier").orElse(false).forGetter(TravellersEntryModifier::builtin)
	).apply(instance, TravellersEntryModifier::new));

	public TravellersEntryModifier(EquipmentSlotGroup group, List<TravellersAttributeEntry> modifiers, TravellersComponentType<Unit> markerComponent, boolean builtin) {
		this(group, modifiers, markerComponent, List.of(), builtin);
	}

	@Override
	public TravellersModifierType getType() {
		return TravellersModifierTypes.ATTRIBUTE_ENTRY.get();
	}

	@Override
	public boolean addModifier(ItemStack stack) {
		if (!this.builtin()) {
			List<TravellersAttributeEntry> entries = getInsertedEntries(stack);
			entries.addAll(this.modifiers());
			TravellersGearData.INSERTED_ATTRIBUTE_MODIFIERS.set(stack, entries);
		}
		this.markerComponent().set(stack, Unit.INSTANCE);
		return true;
	}

	@Override
	public void removeModifier(ItemStack stack) {
		if (!this.builtin()) {
			List<TravellersAttributeEntry> entries = getInsertedEntries(stack);
			entries.removeIf(entry -> this.modifiers().stream().anyMatch(entry::isSameEntry));
			if (entries.isEmpty())
				TravellersGearData.INSERTED_ATTRIBUTE_MODIFIERS.remove(stack);
			else
				TravellersGearData.INSERTED_ATTRIBUTE_MODIFIERS.set(stack, entries);
			this.markerComponent().remove(stack);
		}
	}

	private static List<TravellersAttributeEntry> getInsertedEntries(ItemStack stack) {
		List<TravellersAttributeEntry> entries = TravellersGearData.INSERTED_ATTRIBUTE_MODIFIERS.get(stack);
		return entries == null ? new ArrayList<>() : new ArrayList<>(entries);
	}

	@Override
	public boolean isAbility() {
		return this.builtin();
	}

	@Override
	public boolean hasModifier(ItemStack stack) {
		return this.markerComponent().has(stack);
	}

	@Override
	public List<Component> getDescription() {
		return this.description;
	}
}
