package twilightforest.item.travellers_gear;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.client.TravellersClientEvents;
import twilightforest.client.renderer.armor.TravellersArmorRenderer;
import twilightforest.components.entity.TravellersGogglesZoomAttachment;
import twilightforest.components.item.TravellersComponentType;
import twilightforest.components.item.TravellersGearData;
import twilightforest.enums.TwilightArmorMaterial;
import twilightforest.init.TFItems;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.TravellersAttributeEntry;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A piece of Traveller's Gear: Goggles, Vest, Gloves, Wings, Belt or Boots, all made from Tanned Leather.
 * <p>
 * Backport of 4.8's TravellersArmorItem. 4.8 marks each piece with data components (is_travellers_gear,
 * has_travellers_chestplate/gloves/wings/belt/boots); here the class itself marks the gear and the parts are item
 * properties ({@link Part}), except the gloves a vest gains through the merge recipe ({@link TravellersGearData}).
 * <p>
 * Traveller's Gear never breaks: damage stops at one point of durability, where the piece is "broken". A broken piece
 * keeps its name with a " (Broken)" suffix, switches to its broken item texture, gives no attribute modifiers (see
 * {@code TravellersGearEvents}) and its modifiers stop working until it is repaired with Tanned Leather in an anvil.
 * <p>
 * Goggles, Vest, Wings and Boots take up to {@link #getModifierSlots} Traveller's modifiers
 * ({@link TravellersModifiersManager}). A built-in ability is a component the item carries as a default
 * ({@link #withDefaultComponent}), the 1.20.1 stand-in for 4.8's default item components, plus the attribute modifiers
 * of the ability ({@link #withBuiltinAttribute}).
 */
public class TravellersArmorItem extends ArmorItem implements TravellersModifiable {
	private static final MutableComponent GLOVES_TOOLTIP = Component.translatable("item.twilightforest.travellers_gloves.desc").withStyle(ChatFormatting.GRAY);
	private static final String OUTER_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_1.png").toString();
	private static final String OUTER_TEXTURE_ZOOMING = TwilightForestMod.prefix("textures/models/armor/travellers_layer_1_down.png").toString();
	private static final String INNER_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_2.png").toString();

	private final int insertableModifierSlots;
	private final Set<Part> parts;
	private final boolean cosmetic;
	private final Map<TravellersComponentType<?>, Object> defaultComponents = new IdentityHashMap<>();
	private final List<Supplier<TravellersAttributeEntry>> builtinAttributes = new ArrayList<>();

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

	@Override
	public int getModifierSlots() {
		return this.insertableModifierSlots;
	}

	/**
	 * Gives every stack of this item {@code value} for {@code component} unless the stack NBT holds its own, like a 4.8
	 * item property component. The built-in abilities ({@code twilightforest:builtin} modifiers and built-in
	 * {@code twilightforest:attribute} modifiers) are present on the items that carry their component this way.
	 */
	public <T> TravellersArmorItem withDefaultComponent(TravellersComponentType<T> component, T value) {
		this.defaultComponents.put(component, value);
		return this;
	}

	/**
	 * Gives every stack of this item the attribute modifier of {@code entry} in the entry's slots, like the attribute
	 * modifiers 4.8 puts into the item properties of a piece with a built-in attribute ability (the boots' Step-Up). Lazy
	 * and resolved once, as Forge registers its attributes after the items.
	 */
	public TravellersArmorItem withBuiltinAttribute(Supplier<TravellersAttributeEntry> entry) {
		this.builtinAttributes.add(Suppliers.memoize(entry::get));
		return this;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getDefaultComponent(TravellersComponentType<T> component) {
		return (T) this.defaultComponents.get(component);
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
	 * ({@code textures/item/travellers_modifiers/<piece>/<modifier path>.png}).
	 */
	public static List<ResourceLocation> getInsertedModifierIds(RegistryAccess registries, ItemStack stack) {
		return TravellersModifiersManager.findAllInsertableModifiers(registries, stack).stream().map(modifier -> modifier.key().location()).toList();
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

	/**
	 * The defaults, the built-in attribute modifiers ({@link #withBuiltinAttribute}) and the attribute modifiers inserted
	 * by {@code twilightforest:attribute} modifiers. 4.8 merges them all into the stack's attribute modifier component;
	 * 1.20.1's AttributeModifiers NBT would drop the item defaults, so the inserted ones are kept apart and added here.
	 * While the piece is broken TravellersGearEvents clears them all.
	 */
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> defaults = super.getAttributeModifiers(slot, stack);
		List<TravellersAttributeEntry> inserted = TravellersGearData.INSERTED_ATTRIBUTE_MODIFIERS.get(stack);
		boolean hasInserted = inserted != null && !inserted.isEmpty();
		if (!hasInserted && this.builtinAttributes.isEmpty())
			return defaults;

		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(defaults);
		for (Supplier<TravellersAttributeEntry> builtin : this.builtinAttributes) {
			TravellersAttributeEntry entry = builtin.get();
			if (entry.slot().test(slot))
				builder.put(entry.attribute(), entry.createModifier(slot));
		}
		if (hasInserted) {
			for (TravellersAttributeEntry entry : inserted) {
				if (entry.slot().test(slot))
					builder.put(entry.attribute(), entry.createModifier(slot));
			}
		}
		return builder.build();
	}

	/**
	 * The outer layer flips the goggles down over the eyes while the wearer zooms with them (4.8 98ad4b10b).
	 */
	@Override
	public String getArmorTexture(ItemStack stack, @Nullable Entity entity, EquipmentSlot slot, String type) {
		if (slot == EquipmentSlot.LEGS)
			return INNER_TEXTURE;
		return entity != null && TravellersGogglesZoomAttachment.isUsingZoom(entity) ? OUTER_TEXTURE_ZOOMING : OUTER_TEXTURE;
	}

	/**
	 * Lists the built-in abilities, the inserted modifiers (with their descriptions while Shift is held) and the empty
	 * modifier slots. Backport of 4.8's TravellersArmorItem#appendHoverText (613e64609, d3b7f702a).
	 */
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		// the modifiers come from the level's registries (4.8: the tooltip context's); without a level only the empty slots show
		RegistryAccess registries = level == null ? null : level.registryAccess();
		boolean shiftDown = isShiftDown();

		List<Holder.Reference<TravellersModifier>> abilityModifiers = registries == null ? List.of() : TravellersModifiersManager.findAllAbilityModifiers(registries, stack);
		for (Holder.Reference<TravellersModifier> travellersModifierReference : abilityModifiers) {
			tooltip.add(Component.translatable("travellers_gear.ability", TravellersModifiersManager.getModifierTooltipComponent(travellersModifierReference).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GOLD));
		}

		List<Holder.Reference<TravellersModifier>> insertableModifiers = registries == null ? List.of() : TravellersModifiersManager.findAllInsertableModifiers(registries, stack);
		for (Holder.Reference<TravellersModifier> modifier : insertableModifiers) {
			tooltip.add(Component.literal("- ").append(TravellersModifiersManager.getModifierTooltipComponent(modifier).withStyle(ChatFormatting.GRAY)));
			if (shiftDown) {
				for (Component description : modifier.value().getDescription()) {
					tooltip.add(Component.literal("").append(Component.translatable("travellers_gear.info_indent").withStyle(ChatFormatting.BOLD)).append(description));
				}
			}
		}

		for (int i = insertableModifiers.size(); i < this.getModifierSlots(); i++) {
			tooltip.add(Component.literal("- ").append(Component.translatable("travellers_gear.modifier.empty").withStyle(ChatFormatting.DARK_GRAY)));
		}

		if (this.hasPart(Part.GLOVES)) {
			tooltip.add(GLOVES_TOOLTIP);
		}

		if (!shiftDown) {
			boolean hasHiddenDescriptions = Stream.concat(abilityModifiers.stream(), insertableModifiers.stream()).map(Holder::value).map(TravellersModifier::getDescription).anyMatch(Predicate.not(List::isEmpty));
			if (hasHiddenDescriptions) {
				tooltip.add(Component.translatable("travellers_gear.shift_info", Component.literal("Shift").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.WHITE));
			}
		}
	}

	// 1.20.1's TooltipFlag has no hasShiftDown; ask the client's keyboard, a dedicated server has no Shift to hold
	private static boolean isShiftDown() {
		Boolean shiftDown = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> TravellersClientEvents::hasShiftDown);
		return shiftDown != null && shiftDown;
	}

	/**
	 * Clamps armor damage so the piece stops at one point of durability instead of breaking, and plays the item break
	 * sound to the wearer when it reaches that broken state. Replaces 4.8's ArmorHurtEvent listeners
	 * (TravellersGearEvents#stopDamagingTravellersGear and #setLastDamageArmorTime); unlike 4.8 the sound also plays when
	 * a single hit would have overshot the broken state.
	 * <p>
	 * Damage that gets through also pauses Auto-Repair. 4.8 pauses it when any armor piece of the wearer takes damage;
	 * Forge 47 has no armor damage event, so only damage to a Traveller's Gear piece counts. A hit on the armor damages
	 * every worn piece at once, so this only differs when all the pieces hit are broken Traveller's Gear or other armor
	 * (a falling block only hits the helmet).
	 */
	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		int remaining = stack.getMaxDamage() - 1 - stack.getDamageValue();
		if (remaining <= 0 || amount <= 0)
			return 0;

		int damage = Math.min(amount, remaining);
		TravellersGearLogic.setLastDamageArmorTime(entity);
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
