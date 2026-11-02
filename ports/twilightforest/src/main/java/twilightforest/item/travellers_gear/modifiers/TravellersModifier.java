package twilightforest.item.travellers_gear.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.custom.TravellersModifierTypes;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;

import java.util.List;

/**
 * A Traveller's Gear modifier, an entry of the {@code twilight/travellers_modifiers} datapack registry. Backport of
 * 4.8's TravellersModifier: the dispatch type comes from the {@code twilight/travellers_modifier_type} registry, and
 * the modifier payload is a {@link twilightforest.components.item.TravellersComponentType} (stack NBT) instead of a
 * data component.
 */
public interface TravellersModifier {

	Codec<TravellersModifier> CODEC = TravellersModifierTypes.TYPE_CODEC.dispatch(TravellersModifier::getType, TravellersModifierType::codec);

	Codec<EquipmentSlotGroup> EQUIPMENT_SLOTS_CODEC = ExtraCodecs.validate(EquipmentSlotGroup.CODEC, TravellersModifier::validateEquipment);

	Codec<List<Component>> DESCRIPTION_CODEC = ExtraCodecs.COMPONENT.listOf();

	TravellersModifierType getType();

	EquipmentSlotGroup group();

	boolean hasModifier(ItemStack stack);

	boolean isAbility();

	default List<Component> getDescription() {
		return List.of();
	}

	default String getPrefix() {
		return "travellers_gear.modifier";
	}

	/**
	 * Whether the modifier works on {@code stack}: present, the wearer is no spectator, and the stack is not broken
	 * (Auto-Repair keeps working on broken gear).
	 */
	default boolean isActive(ItemStack stack, ResourceKey<TravellersModifier> modifier, boolean spectator) {
		return this.hasModifier(stack) && !spectator && (!TravellersArmorItem.isTravellersArmorAndBroken(stack) || TravellersModifiersManager.ALWAYS_ACTIVE.contains(modifier));
	}

	static DataResult<EquipmentSlotGroup> validateEquipment(EquipmentSlotGroup group) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (!slot.isArmor() && group.test(slot)) {
				return DataResult.error(() -> "EquipmentSlotGroup must only use armor slots");
			}
		}
		return DataResult.success(group);
	}
}
