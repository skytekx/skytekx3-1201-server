package twilightforest.components.item;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import twilightforest.TwilightForestMod;
import twilightforest.item.travellers_gear.modifiers.TravellersAttributeEntry;

import java.util.List;

/**
 * Stack NBT of the Traveller's Gear. TF 4.8 keeps this state in data components; 1.20.1 has none, so each component
 * becomes a stack NBT key named after the 4.8 component id. Keep every read and write of these keys in this class, so
 * the Traveller's Gear features ported later (the per-piece modifiers) add their keys here and the names stay stable
 * for existing items.
 * <p>
 * The parts a piece is made of ({@code has_travellers_chestplate}, {@code has_travellers_wings} and so on) are fixed per
 * item in 4.8 too, so they are item properties ({@link twilightforest.item.travellers_gear.TravellersArmorItem.Part})
 * rather than NBT. Only the gloves can be added to a stack: the vest gains them through the vest and gloves merge recipe.
 * <p>
 * The other keys are {@link TravellersComponentType}s: the components the Traveller's modifiers insert (see
 * {@code twilightforest.init.custom.TravellersModifiersManager}). Their values come from the modifier definitions in
 * {@code data/<namespace>/twilight/travellers_modifiers/}; the features that act on a modifier read them here.
 */
public final class TravellersGearData {

	/**
	 * 4.8 {@code twilightforest:has_travellers_gloves}: set on a Traveller's Vest that has been merged with a pair of
	 * Traveller's Gloves. The gloves item has the part built in.
	 */
	public static final String HAS_GLOVES = "twilightforest:has_travellers_gloves";

	// all pieces
	public static final TravellersComponentType<Float> AUTO_REPAIR_PROBABILITY = register("auto_repair_probability", ExtraCodecs.POSITIVE_FLOAT);
	// goggles
	public static final TravellersComponentType<Float> ZOOM_ABILITY_MODIFIER = register("zoom_ability_modifier", ExtraCodecs.POSITIVE_FLOAT);
	public static final TravellersComponentType<Unit> AQUATIC_AGILITY = register("aquatic_agility", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Unit> RED_THREAD_VISION = register("red_thread_vision", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Unit> ALL_NIGHT_GOGGLES = register("all_night_goggles", TravellersComponentType.UNIT_CODEC);
	/**
	 * What the goggles' Item Display holds, see {@link ItemDisplayContents}.
	 */
	public static final TravellersComponentType<ItemDisplayContents> ITEM_DISPLAY = register("item_display", ItemDisplayContents.CODEC);
	// vest
	public static final TravellersComponentType<Unit> SWIFT_SWIM = register("swift_swim", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Unit> STEALTH_CROUCHING = register("stealth_crouching", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Unit> ARROW_MAGNETISM = register("arrow_magnetism", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Float> EFFICIENT_EATER = register("efficient_eater", Codec.FLOAT);
	public static final TravellersComponentType<Float> PERFECT_DODGE_PROBABILITY = register("perfect_dodge_probability", ExtraCodecs.POSITIVE_FLOAT);
	public static final TravellersComponentType<Integer> HASTE_AMPLIFIER = register("haste_amplifier", Codec.intRange(0, 255));
	// belt and wings
	public static final TravellersComponentType<Unit> SWAP_HOTBAR_ABILITY = register("swap_hotbar_ability", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Unit> SWAP_HOTBAR_MODIFIER = register("swap_hotbar_modifier", TravellersComponentType.UNIT_CODEC);
	/**
	 * 4.8's vanilla {@code minecraft:container} component, the hotbar the belt (or Swap Hotbar wings) stores; see
	 * {@link twilightforest.item.travellers_gear.TravellersArmorBeltItem#travellersTrySwapHotbar}.
	 */
	public static final TravellersComponentType<TravellersContainerContents> CONTAINER = TravellersComponentType.register(new ResourceLocation("container"), TravellersContainerContents.CODEC);
	public static final TravellersComponentType<Integer> HIGH_JUMP_AMPLIFIER = register("high_jump_amplifier", Codec.intRange(0, 255));
	public static final TravellersComponentType<Float> GRADUALLY_GLIDING_MULTIPLIER = register("gradually_gliding_multiplier", ExtraCodecs.POSITIVE_FLOAT);
	public static final TravellersComponentType<Float> AGILE_RANGER_MODIFIER = register("agile_ranger_modifier", ExtraCodecs.POSITIVE_FLOAT);
	public static final TravellersComponentType<Unit> DOUBLE_JUMP = register("double_jump", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Long> SIDESTEP_COOLDOWN = register("sidestep_cooldown", Codec.LONG);
	// boots
	public static final TravellersComponentType<Unit> HIGH_STEP = register("high_step", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Double> STRAIGHT_AHEAD_MULTIPLIER = register("straight_ahead_multiplier", Codec.DOUBLE);
	public static final TravellersComponentType<Float> SLIMY_SOLES_COEFFICIENT = register("slimy_soles_coefficient", ExtraCodecs.POSITIVE_FLOAT);
	public static final TravellersComponentType<Unit> UNRESTRAINED = register("unrestrained", TravellersComponentType.UNIT_CODEC);
	public static final TravellersComponentType<Unit> WATER_WALK = register("water_walk", TravellersComponentType.UNIT_CODEC);

	/**
	 * The attribute modifiers inserted by {@code twilightforest:attribute} modifiers. 4.8 adds them to the stack's
	 * {@code minecraft:attribute_modifiers} component next to the defaults; in 1.20.1 vanilla's AttributeModifiers NBT
	 * would replace the item defaults, so they get their own key and TravellersArmorItem#getAttributeModifiers adds
	 * them on top of the defaults.
	 */
	public static final TravellersComponentType<List<TravellersAttributeEntry>> INSERTED_ATTRIBUTE_MODIFIERS = register("inserted_attribute_modifiers", TravellersAttributeEntry.CODEC.listOf());

	private TravellersGearData() {
	}

	private static <T> TravellersComponentType<T> register(String name, Codec<T> codec) {
		return TravellersComponentType.register(TwilightForestMod.prefix(name), codec);
	}

	/**
	 * Loads the component types, so a lookup by id finds them before anything else touched this class.
	 */
	static void bootstrap() {
	}

	public static boolean hasGlovesTag(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && tag.getBoolean(HAS_GLOVES);
	}

	public static void setGloves(ItemStack stack) {
		stack.getOrCreateTag().putBoolean(HAS_GLOVES, true);
	}
}
