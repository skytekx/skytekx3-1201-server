package twilightforest.init.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import twilightforest.TwilightForestMod;
import twilightforest.components.item.TravellersGearData;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;
import twilightforest.item.travellers_gear.modifiers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * The Traveller's Gear modifiers: the {@code twilight:travellers_modifiers} datapack registry (read from
 * {@code data/<namespace>/twilight/travellers_modifiers/} and synced to clients), the keys of the stock modifiers and
 * the helpers every modifier feature uses to ask whether a modifier is active. Backport of 4.8's
 * TravellersModifiersManager.
 * <p>
 * The modifier payloads are {@link twilightforest.components.item.TravellersComponentType}s (stack NBT) where 4.8 uses
 * data components; 1.21-only attributes are replaced (step height and swim speed by Forge's attributes) or left to the
 * feature that emulates them (the Aquatic Agility oxygen and underwater mining attributes).
 */
public final class TravellersModifiersManager {
	public static final ResourceKey<Registry<TravellersModifier>> TRAVELLERS_MODIFIERS_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("travellers_modifiers"));

	// all
	public static final ResourceKey<TravellersModifier> AUTO_REPAIR_MODIFIER = makeKey("auto_repair");
	// goggles
	public static final ResourceKey<TravellersModifier> ZOOM_ABILITY = makeKey("zoom");
	public static final ResourceKey<TravellersModifier> AQUATIC_AGILITY_MODIFIER = makeKey("aquatic_agility");
	public static final ResourceKey<TravellersModifier> RED_THREAD_VISION_MODIFIER = makeKey("red_thread_vision");
	public static final ResourceKey<TravellersModifier> ALL_NIGHT_GOGGLES_MODIFIER = makeKey("all_night_goggles");
	public static final ResourceKey<TravellersModifier> ITEM_DISPLAY_MODIFIER = makeKey("item_display");
	// vest
	public static final ResourceKey<TravellersModifier> SWIFT_SWIM_ABILITY = makeKey("swift_swim");
	public static final ResourceKey<TravellersModifier> STEALTH_MODIFIER = makeKey("stealth");
	public static final ResourceKey<TravellersModifier> ARROW_MAGNETISM_MODIFIER = makeKey("arrow_magnetism");
	public static final ResourceKey<TravellersModifier> EFFICIENT_EATER_MODIFIER = makeKey("efficient_eater");
	public static final ResourceKey<TravellersModifier> PERFECT_DODGE_MODIFIER = makeKey("perfect_dodge");
	public static final ResourceKey<TravellersModifier> HASTE_MODIFIER = makeKey("haste");
	// belt
	public static final ResourceKey<TravellersModifier> SWAP_HOTBAR_ABILITY = makeKey("swap_hotbar_ability");
	public static final ResourceKey<TravellersModifier> SWAP_HOTBAR_MODIFIER = makeKey("swap_hotbar");
	// wings
	public static final ResourceKey<TravellersModifier> HIGH_JUMP_ABILITY = makeKey("high_jump");
	public static final ResourceKey<TravellersModifier> GRADUAL_GLIDE_MODIFIER = makeKey("gradual_glide");
	public static final ResourceKey<TravellersModifier> AGILE_RANGER_MODIFIER = makeKey("agile_ranger");
	public static final ResourceKey<TravellersModifier> DOUBLE_JUMP_MODIFIER = makeKey("double_jump");
	public static final ResourceKey<TravellersModifier> SIDESTEP_MODIFIER = makeKey("side_step");
	// boots
	public static final ResourceKey<TravellersModifier> STEP_UP_ABILITY = makeKey("step_up");
	public static final ResourceKey<TravellersModifier> STRAIGHT_AHEAD_MODIFIER = makeKey("straight_ahead");
	public static final ResourceKey<TravellersModifier> SLIMY_SOLES_MODIFIER = makeKey("slimy_soles");
	public static final ResourceKey<TravellersModifier> UNRESTRAINED_MODIFIER = makeKey("unrestrained");
	public static final ResourceKey<TravellersModifier> WATER_WALK_MODIFIER = makeKey("water_walk");

	public static final Set<ResourceKey<TravellersModifier>> ALWAYS_ACTIVE = Set.of(AUTO_REPAIR_MODIFIER);

	private static final Map<ResourceKey<TravellersModifier>, TravellersModifier> CACHED_MODIFIERS = new ConcurrentHashMap<>();
	private static final Set<ResourceKey<TravellersModifier>> MISSING_MODIFIERS = ConcurrentHashMap.newKeySet();

	private TravellersModifiersManager() {
	}

	/**
	 * The Step-Up ability of the boots, 4.8 TFAttributeModifiers.TRAVELLERS_HIGH_STEP on 1.21's step height attribute;
	 * Forge's step height addition here. A method, as the attribute only exists once registries are filled.
	 */
	public static TravellersAttributeEntry travellersHighStep() {
		return new TravellersAttributeEntry(ForgeMod.STEP_HEIGHT_ADDITION.get(), TwilightForestMod.prefix("travellers_gear.boots_high_step"), 0.5, AttributeModifier.Operation.ADDITION, EquipmentSlotGroup.FEET);
	}

	/**
	 * The Swift Swim ability of the vest, 4.8 TFAttributeModifiers.TRAVELLERS_SWIFT_SWIM on 1.21's water movement
	 * efficiency attribute; Forge's swim speed here.
	 */
	public static TravellersAttributeEntry travellersSwiftSwim() {
		return new TravellersAttributeEntry(ForgeMod.SWIM_SPEED.get(), TwilightForestMod.prefix("travellers_gear.vest_fast_swimming"), 1.0, AttributeModifier.Operation.ADDITION, EquipmentSlotGroup.CHEST);
	}

	private static ResourceKey<TravellersModifier> makeKey(String name) {
		return ResourceKey.create(TRAVELLERS_MODIFIERS_KEY, TwilightForestMod.prefix(name));
	}

	public static void bootstrap(BootstapContext<TravellersModifier> context) {
		context.register(AUTO_REPAIR_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.ARMOR, TravellersGearData.AUTO_REPAIR_PROBABILITY, 0.001F, componentText(AUTO_REPAIR_MODIFIER)));
		context.register(ZOOM_ABILITY, new BuiltinTravellersComponentModifier(EquipmentSlotGroup.HEAD, TravellersGearData.ZOOM_ABILITY_MODIFIER));
		// 4.8 adds 1.21's oxygen bonus (+3) and submerged mining speed (x4) attributes; 1.20.1 has neither, so the marker alone is inserted and travellers-goggles-modifiers emulates them
		context.register(AQUATIC_AGILITY_MODIFIER, new TravellersEntryModifier(EquipmentSlotGroup.HEAD, List.of(), TravellersGearData.AQUATIC_AGILITY, componentText(AQUATIC_AGILITY_MODIFIER), false));
		context.register(RED_THREAD_VISION_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.HEAD, TravellersGearData.RED_THREAD_VISION, Unit.INSTANCE, componentText(RED_THREAD_VISION_MODIFIER)));
		context.register(ALL_NIGHT_GOGGLES_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.HEAD, TravellersGearData.ALL_NIGHT_GOGGLES, Unit.INSTANCE, componentText(ALL_NIGHT_GOGGLES_MODIFIER)));
		context.register(ITEM_DISPLAY_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.HEAD, TravellersGearData.ITEM_DISPLAY, emptyItemDisplay(), componentText(ITEM_DISPLAY_MODIFIER)));

		context.register(SWIFT_SWIM_ABILITY, new TravellersEntryModifier(EquipmentSlotGroup.CHEST, List.of(travellersSwiftSwim()), TravellersGearData.SWIFT_SWIM, true));
		context.register(STEALTH_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.CHEST, TravellersGearData.STEALTH_CROUCHING, Unit.INSTANCE, componentText(STEALTH_MODIFIER)));
		context.register(ARROW_MAGNETISM_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.CHEST, TravellersGearData.ARROW_MAGNETISM, Unit.INSTANCE, componentText(ARROW_MAGNETISM_MODIFIER)));
		context.register(EFFICIENT_EATER_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.CHEST, TravellersGearData.EFFICIENT_EATER, 2F, componentText(EFFICIENT_EATER_MODIFIER)));
		context.register(PERFECT_DODGE_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.CHEST, TravellersGearData.PERFECT_DODGE_PROBABILITY, 0.3F, componentText(PERFECT_DODGE_MODIFIER)));
		context.register(HASTE_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.CHEST, TravellersGearData.HASTE_AMPLIFIER, 1, componentText(HASTE_MODIFIER)));

		context.register(SWAP_HOTBAR_ABILITY, new BuiltinTravellersComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.SWAP_HOTBAR_ABILITY));
		context.register(SWAP_HOTBAR_MODIFIER, new TransferableComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.SWAP_HOTBAR_MODIFIER, TravellersGearData.CONTAINER, TravellersArmorBeltItem.DEFAULT_EMPTY_BELT_CONTAINER, componentText(SWAP_HOTBAR_MODIFIER)));

		context.register(HIGH_JUMP_ABILITY, new BuiltinTravellersComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.HIGH_JUMP_AMPLIFIER));
		context.register(GRADUAL_GLIDE_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.GRADUALLY_GLIDING_MULTIPLIER, 1 - 1 / 6F, componentText(GRADUAL_GLIDE_MODIFIER)));
		context.register(AGILE_RANGER_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.AGILE_RANGER_MODIFIER, 5F, componentText(AGILE_RANGER_MODIFIER)));
		context.register(DOUBLE_JUMP_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.DOUBLE_JUMP, Unit.INSTANCE, componentText(DOUBLE_JUMP_MODIFIER)));
		context.register(SIDESTEP_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.LEGS, TravellersGearData.SIDESTEP_COOLDOWN, 2 * 20L, componentText(SIDESTEP_MODIFIER, Component.keybind("key.left"), Component.keybind("key.right"))));

		context.register(STEP_UP_ABILITY, new TravellersEntryModifier(EquipmentSlotGroup.FEET, List.of(travellersHighStep()), TravellersGearData.HIGH_STEP, true));
		context.register(STRAIGHT_AHEAD_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.FEET, TravellersGearData.STRAIGHT_AHEAD_MULTIPLIER, 1.4, componentText(STRAIGHT_AHEAD_MODIFIER)));
		context.register(SLIMY_SOLES_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.FEET, TravellersGearData.SLIMY_SOLES_COEFFICIENT, 0.5F, componentText(SLIMY_SOLES_MODIFIER)));
		context.register(UNRESTRAINED_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.FEET, TravellersGearData.UNRESTRAINED, Unit.INSTANCE, componentText(UNRESTRAINED_MODIFIER)));
		context.register(WATER_WALK_MODIFIER, new TravellersComponentModifier(EquipmentSlotGroup.FEET, TravellersGearData.WATER_WALK, Unit.INSTANCE, componentText(WATER_WALK_MODIFIER)));
	}

	// 4.8 ItemDisplayContents.EMPTY
	private static CompoundTag emptyItemDisplay() {
		CompoundTag tag = new CompoundTag();
		tag.put("chosen_map_slot", IntTag.valueOf(0));
		tag.put("slots", new ListTag());
		return tag;
	}

	private static List<Component> componentText(ResourceKey<TravellersModifier> modifier, Object... args) {
		return List.of(Component.translatable(modifier.location().toLanguageKey("travellers_gear.modifier", "description"), args));
	}

	public static boolean isModifierActive(RegistryAccess registries, ItemStack stack, ResourceKey<TravellersModifier> modifierKey, boolean spectator) {
		return getCachedModifier(registries, modifierKey).map(modifier -> modifier.isActive(stack, modifierKey, spectator)).orElse(false);
	}

	public static boolean isModifierActive(Entity entity, ItemStack stack, ResourceKey<TravellersModifier> modifierKey) {
		return isModifierActive(entity.level().registryAccess(), stack, modifierKey, entity.isSpectator());
	}

	public static boolean isModifierActive(Entity entity, ResourceKey<TravellersModifier> modifierKey) {
		return entity instanceof LivingEntity livingEntity && isModifierActive(livingEntity, modifierKey);
	}

	/**
	 * Whether the piece worn in the single armor slot the modifier is made for has it active. Modifiers made for several
	 * slots (Auto-Repair) are checked per stack instead.
	 */
	public static boolean isModifierActive(LivingEntity livingEntity, ResourceKey<TravellersModifier> modifierKey) {
		Optional<TravellersModifier> modifier = getCachedModifier(livingEntity.level().registryAccess(), modifierKey);
		if (modifier.isEmpty())
			return false;
		ItemStack equippedStack = getStackForGroup(livingEntity, modifier.get().group());
		return !equippedStack.isEmpty() && modifier.get().isActive(equippedStack, modifierKey, livingEntity.isSpectator());
	}

	public static boolean hasTravellersModifier(RegistryAccess registries, ItemStack stack, ResourceKey<TravellersModifier> modifierKey) {
		return getCachedModifier(registries, modifierKey).map(modifier -> modifier.hasModifier(stack)).orElse(false);
	}

	public static boolean addModifier(RegistryAccess registries, ItemStack stack, ResourceKey<TravellersModifier> modifierKey) {
		Optional<TravellersModifier> modifier = getCachedModifier(registries, modifierKey);
		if (modifier.isEmpty() || !(modifier.get() instanceof InsertableTravellersModifier insertableTravellersModifier))
			return false;
		return insertableTravellersModifier.addModifier(stack);
	}

	public static boolean transferModifier(RegistryAccess registries, ItemStack stack, List<Ingredient> ingredients, ResourceKey<TravellersModifier> modifierKey) {
		Optional<TravellersModifier> modifier = getCachedModifier(registries, modifierKey);
		if (modifier.isEmpty() || !(modifier.get() instanceof TransferableTravellersModifier transferableTravellersModifier))
			return false;
		return transferableTravellersModifier.transfer(stack, ingredients);
	}

	public static int getModifierDataComponentProviders(RegistryAccess registries, List<Ingredient> ingredients, ResourceKey<TravellersModifier> modifierKey) {
		Optional<TravellersModifier> modifier = getCachedModifier(registries, modifierKey);
		if (modifier.isEmpty() || !(modifier.get() instanceof TransferableComponentModifier transferableComponentModifier))
			return 0;
		return transferableComponentModifier.findDataComponentProviders(ingredients).size();
	}

	public static MutableComponent getModifierTooltipComponent(Holder.Reference<TravellersModifier> modifier) {
		return TooltipStringInterpolator.render(modifier.key().location().toLanguageKey(modifier.value().getPrefix()));
	}

	private static Stream<Holder.Reference<TravellersModifier>> listModifiers(RegistryAccess registries) {
		return registries.registry(TRAVELLERS_MODIFIERS_KEY).map(Registry::holders).orElseGet(Stream::empty);
	}

	/**
	 * The insertable (not built-in) modifiers on {@code stack}, in registry order.
	 */
	public static List<Holder.Reference<TravellersModifier>> findAllInsertableModifiers(RegistryAccess registries, ItemStack stack) {
		return listModifiers(registries).filter(travellersModifier -> travellersModifier.value() instanceof InsertableTravellersModifier && !travellersModifier.value().isAbility() && travellersModifier.value().hasModifier(stack)).toList();
	}

	public static List<Holder.Reference<TravellersModifier>> findAllInsertableModifiers(Entity entity, ItemStack stack) {
		return findAllInsertableModifiers(entity.level().registryAccess(), stack);
	}

	public static List<Holder.Reference<TravellersModifier>> findAllInsertableModifiers(Level level, ItemStack stack) {
		return findAllInsertableModifiers(level.registryAccess(), stack);
	}

	public static List<Holder.Reference<TravellersModifier>> findAllAbilityModifiers(RegistryAccess registries, ItemStack stack) {
		return listModifiers(registries).filter(travellersModifier -> travellersModifier.value().isAbility() && travellersModifier.value().hasModifier(stack)).toList();
	}

	public static long countInsertableModifiers(RegistryAccess registries, ItemStack stack) {
		return findAllInsertableModifiers(registries, stack).size();
	}

	public static boolean isModifierEnabled(RegistryAccess registries, ResourceKey<TravellersModifier> modifierKey) {
		return getCachedModifier(registries, modifierKey).isPresent();
	}

	private static ItemStack getStackForGroup(LivingEntity livingEntity, EquipmentSlotGroup group) {
		EquipmentSlot matchedSlot = null;
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (!slot.isArmor() || !group.test(slot))
				continue;
			if (matchedSlot != null)
				return ItemStack.EMPTY;
			matchedSlot = slot;
		}
		return matchedSlot == null ? ItemStack.EMPTY : livingEntity.getItemBySlot(matchedSlot);
	}

	public static void clearCache() {
		CACHED_MODIFIERS.clear();
		MISSING_MODIFIERS.clear();
	}

	private static Optional<TravellersModifier> getCachedModifier(RegistryAccess registries, ResourceKey<TravellersModifier> modifierKey) {
		TravellersModifier cached = CACHED_MODIFIERS.get(modifierKey);
		if (cached != null)
			return Optional.of(cached);
		if (MISSING_MODIFIERS.contains(modifierKey))
			return Optional.empty();

		Optional<Holder.Reference<TravellersModifier>> modifier = registries.registry(TRAVELLERS_MODIFIERS_KEY).flatMap(registry -> registry.getHolder(modifierKey));
		if (modifier.isPresent()) {
			CACHED_MODIFIERS.put(modifierKey, modifier.get().value());
			return Optional.of(modifier.get().value());
		}

		TwilightForestMod.LOGGER.warn("Travellers modifier {} is not present in the registry", modifierKey.location());
		MISSING_MODIFIERS.add(modifierKey);
		return Optional.empty();
	}

	/**
	 * Drops the cached modifier definitions whenever the server reloads its data, as in 4.8. The client also clears the
	 * cache when it joins a world (TravellersClientEvents), since a dedicated client can join servers with different
	 * modifier definitions.
	 */
	public static final class CacheInvalidationReloadListener extends SimplePreparableReloadListener<Unit> {
		public static final CacheInvalidationReloadListener INSTANCE = new CacheInvalidationReloadListener();

		private CacheInvalidationReloadListener() {
		}

		@Override
		protected Unit prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
			return Unit.INSTANCE;
		}

		@Override
		protected void apply(Unit object, ResourceManager resourceManager, ProfilerFiller profiler) {
			clearCache();
		}
	}
}
