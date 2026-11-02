package twilightforest.item.travellers_gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.client.renderer.armor.TravellersArmorRenderer;
import twilightforest.components.item.TravellersGearData;
import twilightforest.enums.TwilightArmorMaterial;
import twilightforest.init.TFItems;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A piece of Traveller's Gear: Goggles, Vest, Gloves, Wings, Belt or Boots, all made from Tanned Leather.
 * <p>
 * Backport of 4.8's TravellersArmorItem. 4.8 marks each piece with data components (is_travellers_gear,
 * has_travellers_chestplate/gloves/wings/belt/boots); here the class itself marks the gear and the parts are item
 * properties ({@link Part}), except the gloves a vest gains through the merge recipe ({@link TravellersGearData}).
 * <p>
 * Traveller's Gear never breaks: damage stops at one point of durability, where the piece is "broken". A broken piece
 * keeps its name with a " (Broken)" suffix, switches to its broken item texture and gives no attribute modifiers (see
 * {@code TravellersGearEvents}) until it is repaired with Tanned Leather in an anvil.
 */
public class TravellersArmorItem extends ArmorItem {
	private static final MutableComponent GLOVES_TOOLTIP = Component.translatable("item.twilightforest.travellers_gloves.desc").withStyle(ChatFormatting.GRAY);
	private static final String OUTER_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_1.png").toString();
	private static final String INNER_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_2.png").toString();

	private final int insertableModifierSlots;
	private final Set<Part> parts;
	private final boolean cosmetic;

	/**
	 * @param durability the 4.8 durability multiplier, scaled by the vanilla armor health factor of the slot. 0 makes a
	 *                   cosmetic piece (the Gloves and the Belt) that never takes damage and gives no armor.
	 */
	public TravellersArmorItem(Type type, Properties properties, int insertableModifierSlots, int durability, Set<Part> parts) {
		super(TwilightArmorMaterial.ARMOR_TRAVELLERS, type, durability > 0 ? properties.durability(getDurability(type, durability)) : properties);
		this.insertableModifierSlots = insertableModifierSlots;
		this.parts = parts.isEmpty() ? EnumSet.noneOf(Part.class) : EnumSet.copyOf(parts);
		this.cosmetic = durability <= 0;
	}

	// [VanillaCopy] 1.21 ArmorItem.Type#getDurability, using the health factors of 1.20.1's ArmorMaterials
	private static int getDurability(Type type, int durability) {
		return durability * switch (type) {
			case HELMET -> 11;
			case CHESTPLATE -> 16;
			case LEGGINGS -> 15;
			case BOOTS -> 13;
		};
	}

	public int getModifierSlots() {
		return this.insertableModifierSlots;
	}

	public boolean hasPart(Part part) {
		return this.parts.contains(part);
	}

	public static boolean hasPart(ItemStack stack, Part part) {
		return stack.getItem() instanceof TravellersArmorItem armor && armor.hasPart(part);
	}

	/**
	 * Whether the stack shows Traveller's Gloves: the Gloves themselves, or a Vest merged with a pair.
	 */
	public static boolean hasGloves(ItemStack stack) {
		return stack.getItem() instanceof TravellersArmorItem armor && (armor.hasPart(Part.GLOVES) || TravellersGearData.hasGlovesTag(stack));
	}

	public static boolean isTravellersGear(ItemStack stack) {
		return stack.getItem() instanceof TravellersArmorItem;
	}

	public static boolean isTravellersArmorAndBroken(ItemStack stack) {
		return isTravellersGear(stack) && stack.isDamageableItem() && stack.getMaxDamage() - 1 <= stack.getDamageValue();
	}

	/**
	 * The ids of the modifiers inserted into this stack, in the order the item model layers their overlay sprites
	 * ({@code textures/item/travellers_modifiers/<piece>/<modifier path>.png}). The modifier system is not ported yet
	 * (feature travellers-modifier-framework), so no stack carries any.
	 */
	public static List<ResourceLocation> getInsertedModifierIds(ItemStack stack) {
		return List.of();
	}

	@Override
	public Component getName(ItemStack stack) {
		if (isTravellersArmorAndBroken(stack)) {
			return Component.translatable(this.getDescriptionId(stack)).append(Component.translatable("travellers_gear.broken").withStyle(ChatFormatting.GRAY));
		}
		return super.getName(stack);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		// 4.8 gives the Gloves and the Belt no attribute modifiers at all, so they are purely cosmetic
		return this.cosmetic ? ImmutableMultimap.of() : super.getDefaultAttributeModifiers(slot);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return slot == EquipmentSlot.LEGS ? INNER_TEXTURE : OUTER_TEXTURE;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);

		int insertedModifiers = getInsertedModifierIds(stack).size();
		for (int i = insertedModifiers; i < this.getModifierSlots(); i++) {
			tooltip.add(Component.literal("- ").append(Component.translatable("travellers_gear.modifier.empty").withStyle(ChatFormatting.DARK_GRAY)));
		}

		if (this.hasPart(Part.GLOVES)) {
			tooltip.add(GLOVES_TOOLTIP);
		}
	}

	/**
	 * Clamps armor damage so the piece stops at one point of durability instead of breaking, and plays the item break
	 * sound to the wearer when it reaches that broken state. Replaces 4.8's ArmorHurtEvent listener
	 * (TravellersGearEvents#stopDamagingTravellersGear); unlike 4.8 the sound also plays when a single hit would have
	 * overshot the broken state.
	 */
	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		int remaining = stack.getMaxDamage() - 1 - stack.getDamageValue();
		if (remaining <= 0 || amount <= 0)
			return 0;

		int damage = Math.min(amount, remaining);
		if (damage == remaining && entity instanceof ServerPlayer player)
			player.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, player.getVoicePitch());
		return damage;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		// 4.8 rejects every enchantment through supportsEnchantment; 1.20.1 gates books here
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean isRepairable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
		return stack.is(TFItems.TRAVELLERS_BOOTS.get());
	}

	@Override
	public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
		return this == TFItems.TRAVELLERS_GOGGLES.get() || this.hasPart(Part.WINGS);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(TravellersArmorRenderer.INSTANCE);
	}

	/**
	 * The parts a piece of Traveller's Gear draws on the player, 4.8's has_travellers_* components.
	 */
	public enum Part {
		CHESTPLATE,
		GLOVES,
		WINGS,
		BELT,
		BOOTS
	}
}
