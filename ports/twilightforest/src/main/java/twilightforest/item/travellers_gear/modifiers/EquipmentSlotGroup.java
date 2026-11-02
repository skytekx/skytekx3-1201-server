package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.function.Predicate;

/**
 * [VanillaCopy] of 1.21's EquipmentSlotGroup, which 1.20.1 does not have. Traveller's modifiers and their attribute
 * entries name the equipment slots they apply to with it, in the same JSON form as TF 4.8 ({@code "armor"},
 * {@code "head"}, ...). 1.21's {@code body} group is left out: 1.20.1 has no body slot.
 */
public enum EquipmentSlotGroup implements StringRepresentable {
	ANY("any", slot -> true),
	MAINHAND("mainhand", EquipmentSlot.MAINHAND),
	OFFHAND("offhand", EquipmentSlot.OFFHAND),
	HAND("hand", slot -> slot.getType() == EquipmentSlot.Type.HAND),
	FEET("feet", EquipmentSlot.FEET),
	LEGS("legs", EquipmentSlot.LEGS),
	CHEST("chest", EquipmentSlot.CHEST),
	HEAD("head", EquipmentSlot.HEAD),
	ARMOR("armor", EquipmentSlot::isArmor);

	public static final Codec<EquipmentSlotGroup> CODEC = StringRepresentable.fromEnum(EquipmentSlotGroup::values);

	private final String name;
	private final Predicate<EquipmentSlot> predicate;

	EquipmentSlotGroup(String name, Predicate<EquipmentSlot> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	EquipmentSlotGroup(String name, EquipmentSlot slot) {
		this(name, other -> other == slot);
	}

	public boolean test(EquipmentSlot slot) {
		return this.predicate.test(slot);
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static EquipmentSlotGroup bySlot(EquipmentSlot slot) {
		return switch (slot) {
			case MAINHAND -> MAINHAND;
			case OFFHAND -> OFFHAND;
			case FEET -> FEET;
			case LEGS -> LEGS;
			case CHEST -> CHEST;
			case HEAD -> HEAD;
		};
	}
}
