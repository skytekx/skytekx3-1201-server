package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * One attribute modifier a Traveller's modifier adds to the gear: the 1.20.1 stand-in for 1.21's
 * ItemAttributeModifiers.Entry, with the same JSON form ({@code type}, {@code id}, {@code amount}, {@code operation},
 * {@code slot}).
 * <p>
 * 1.20.1 attribute modifiers are keyed by UUID instead of a resource location, and an attribute instance refuses a
 * second modifier with the same UUID. {@link #createModifier} therefore derives the UUID from the id and the equipment
 * slot, so the same entry worn in two slots never collides.
 */
public record TravellersAttributeEntry(Attribute attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {

	/**
	 * 1.21's operation names; 1.20.1's Operation enum has no serialized form.
	 */
	public static final Codec<AttributeModifier.Operation> OPERATION_CODEC = Codec.STRING.comapFlatMap(name -> switch (name) {
		case "add_value" -> DataResult.success(AttributeModifier.Operation.ADDITION);
		case "add_multiplied_base" -> DataResult.success(AttributeModifier.Operation.MULTIPLY_BASE);
		case "add_multiplied_total" -> DataResult.success(AttributeModifier.Operation.MULTIPLY_TOTAL);
		default -> DataResult.error(() -> "Unknown attribute modifier operation: " + name);
	}, operation -> switch (operation) {
		case ADDITION -> "add_value";
		case MULTIPLY_BASE -> "add_multiplied_base";
		case MULTIPLY_TOTAL -> "add_multiplied_total";
	});

	public static final Codec<TravellersAttributeEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("type").forGetter(TravellersAttributeEntry::attribute),
		ResourceLocation.CODEC.fieldOf("id").forGetter(TravellersAttributeEntry::id),
		Codec.DOUBLE.fieldOf("amount").forGetter(TravellersAttributeEntry::amount),
		OPERATION_CODEC.fieldOf("operation").forGetter(TravellersAttributeEntry::operation),
		EquipmentSlotGroup.CODEC.optionalFieldOf("slot", EquipmentSlotGroup.ANY).forGetter(TravellersAttributeEntry::slot)
	).apply(instance, TravellersAttributeEntry::new));

	/**
	 * The modifier this entry gives while its stack is worn in {@code slot}.
	 */
	public AttributeModifier createModifier(EquipmentSlot slot) {
		UUID uuid = UUID.nameUUIDFromBytes((this.id() + "/" + slot.getName()).getBytes(StandardCharsets.UTF_8));
		return new AttributeModifier(uuid, this.id().toString(), this.amount(), this.operation());
	}

	/**
	 * Whether {@code other} is the same entry, ignoring the amount and operation, so a stack keeps losing the modifier at
	 * the grindstone after a data pack retunes it.
	 */
	public boolean isSameEntry(TravellersAttributeEntry other) {
		return this.attribute() == other.attribute() && this.id().equals(other.id()) && this.slot() == other.slot();
	}
}
