package twilightforest.components.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.item.travellers_gear.TravellersArmorItem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A piece of Traveller's Gear state that TF 4.8 keeps in a data component. 1.20.1 has no data components, so each one
 * is a stack NBT key named after the 4.8 component id, holding the value encoded with the component's codec. The data
 * driven Traveller's modifiers name these types by id in their JSON ({@code "component": {"twilightforest:water_walk": {}}}),
 * the same way 4.8 names data component types.
 * <p>
 * Like a 1.21 item prototype, a piece of Traveller's Gear can carry default values
 * ({@link TravellersArmorItem#withDefaultComponent}); {@link #get} and {@link #has} fall back to them when the stack NBT
 * does not hold the key. {@link #remove} only removes the NBT value, so a default cannot be removed from a stack.
 * <p>
 * All types are declared in {@link TravellersGearData}.
 */
public final class TravellersComponentType<T> {
	private static final Map<ResourceLocation, TravellersComponentType<?>> BY_ID = new ConcurrentHashMap<>();

	/**
	 * Codec of the marker types: a present key with an empty compound, {@code {}} in JSON like 4.8's Unit components.
	 */
	public static final Codec<Unit> UNIT_CODEC = Codec.unit(Unit.INSTANCE);
	/**
	 * Codec of the types whose value is owned by a feature that is not ported yet: the NBT is kept as it is. The tag is
	 * copied both ways, so a stack never shares a mutable tag with a modifier definition or another stack.
	 */
	public static final Codec<Tag> RAW_NBT_CODEC = Codec.PASSTHROUGH.xmap(dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue().copy(), tag -> new Dynamic<>(NbtOps.INSTANCE, tag.copy()));

	public static final Codec<TravellersComponentType<?>> CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
		TravellersComponentType<?> type = byId(id);
		return type != null ? DataResult.success(type) : DataResult.error(() -> "Unknown Traveller's Gear component: " + id);
	}, TravellersComponentType::id);

	/**
	 * A marker type, for the modifiers whose component must be a Unit.
	 */
	@SuppressWarnings("unchecked")
	public static final Codec<TravellersComponentType<Unit>> UNIT_TYPE_CODEC = CODEC.comapFlatMap(type -> type.isUnit()
		? DataResult.success((TravellersComponentType<Unit>) type)
		: DataResult.error(() -> "Traveller's Gear component " + type.id() + " is not a marker"), type -> type);

	private final ResourceLocation id;
	private final String key;
	private final Codec<T> codec;

	private TravellersComponentType(ResourceLocation id, Codec<T> codec) {
		this.id = id;
		this.key = id.toString();
		this.codec = codec;
	}

	static <T> TravellersComponentType<T> register(ResourceLocation id, Codec<T> codec) {
		TravellersComponentType<T> type = new TravellersComponentType<>(id, codec);
		if (BY_ID.putIfAbsent(id, type) != null)
			throw new IllegalStateException("Duplicate Traveller's Gear component " + id);
		return type;
	}

	@Nullable
	public static TravellersComponentType<?> byId(ResourceLocation id) {
		TravellersGearData.bootstrap();
		return BY_ID.get(id);
	}

	public ResourceLocation id() {
		return this.id;
	}

	/**
	 * The stack NBT key of this component.
	 */
	public String key() {
		return this.key;
	}

	public Codec<T> codec() {
		return this.codec;
	}

	public boolean isUnit() {
		return this.codec == UNIT_CODEC;
	}

	/**
	 * The value on the stack, or the default of its item when the stack has none.
	 */
	@Nullable
	public T get(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(this.key)) {
			return this.codec.parse(NbtOps.INSTANCE, tag.get(this.key))
				.resultOrPartial(error -> TwilightForestMod.LOGGER.warn("Invalid Traveller's Gear component {} on {}: {}", this.id, stack, error))
				.orElse(null);
		}
		return stack.getItem() instanceof TravellersArmorItem armor ? armor.getDefaultComponent(this) : null;
	}

	public boolean has(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains(this.key) || stack.getItem() instanceof TravellersArmorItem armor && armor.getDefaultComponent(this) != null;
	}

	public void set(ItemStack stack, T value) {
		this.codec.encodeStart(NbtOps.INSTANCE, value)
			.resultOrPartial(error -> TwilightForestMod.LOGGER.error("Could not save Traveller's Gear component {} on {}: {}", this.id, stack, error))
			.ifPresent(tag -> stack.getOrCreateTag().put(this.key, tag));
	}

	public void remove(ItemStack stack) {
		stack.removeTagKey(this.key);
	}

	@Override
	public String toString() {
		return "TravellersComponentType[" + this.id + "]";
	}

	/**
	 * A component type with a value, 4.8's TypedDataComponent. {@link #CODEC} reads and writes it as a map with exactly
	 * one entry, the form of a 4.8 DataComponentMap holding one component.
	 */
	public record Value<T>(TravellersComponentType<T> type, T value) {
		public static final Codec<Value<?>> CODEC = new Codec<>() {
			@Override
			public <O> DataResult<Pair<Value<?>, O>> decode(DynamicOps<O> ops, O input) {
				return ops.getMapValues(input).flatMap(entries -> {
					List<Pair<O, O>> list = entries.toList();
					if (list.size() != 1)
						return DataResult.error(() -> "Expected exactly one entry in this component map: " + input);
					Pair<O, O> entry = list.get(0);
					return TravellersComponentType.CODEC.parse(ops, entry.getFirst())
						.flatMap(type -> decodeValue(ops, type, entry.getSecond()))
						.map(value -> Pair.of(value, ops.empty()));
				});
			}

			@Override
			public <O> DataResult<O> encode(Value<?> input, DynamicOps<O> ops, O prefix) {
				return encodeValue(input, ops, prefix);
			}

			@Override
			public String toString() {
				return "TravellersComponentType.Value";
			}
		};

		private static <O, T> DataResult<Value<?>> decodeValue(DynamicOps<O> ops, TravellersComponentType<T> type, O input) {
			return type.codec().parse(ops, input).map(value -> new Value<>(type, value));
		}

		private static <O, T> DataResult<O> encodeValue(Value<T> input, DynamicOps<O> ops, O prefix) {
			return input.type().codec().encodeStart(ops, input.value())
				.flatMap(encoded -> ops.mergeToMap(prefix, ops.createString(input.type().id().toString()), encoded));
		}

		public void applyTo(ItemStack stack) {
			this.type().set(stack, this.value());
		}
	}
}
