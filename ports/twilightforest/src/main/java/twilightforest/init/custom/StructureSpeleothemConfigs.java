package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFStructures;
import twilightforest.util.RectangleLatticeIterator;
import twilightforest.world.components.structures.StructureSpeleothemConfig;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Datapack registry {@code twilight/structure_speleothem_settings}, read from
 * {@code data/<namespace>/twilight/structure_speleothem_settings/<name>.json}. The Hollow Hills, the Hydra Lair, the Yeti Cave and the
 * Troll Cave reference an entry through the {@code speleothem_config} field of their structure JSON.
 * <p>
 * Structure pieces store the entry's id under {@code config_id} and look it up when they decorate a chunk. Pieces saved before this
 * existed have no id and fall back to the entry of their structure, see {@link #readKey}.
 */
public class StructureSpeleothemConfigs {
	public static final ResourceKey<Registry<StructureSpeleothemConfig>> REGISTRY_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("structure_speleothem_settings"));
	public static final Codec<Holder<StructureSpeleothemConfig>> CODEC = RegistryFileCodec.create(REGISTRY_KEY, StructureSpeleothemConfig.CODEC, false);

	public static final ResourceKey<StructureSpeleothemConfig> SMALL_HILL = makeKey(TFStructures.HOLLOW_HILL_SMALL.location());
	public static final ResourceKey<StructureSpeleothemConfig> MEDIUM_HILL = makeKey(TFStructures.HOLLOW_HILL_MEDIUM.location());
	public static final ResourceKey<StructureSpeleothemConfig> LARGE_HILL = makeKey(TFStructures.HOLLOW_HILL_LARGE.location());
	public static final ResourceKey<StructureSpeleothemConfig> HYDRA_LAIR = makeKey(TFStructures.HYDRA_LAIR.location());
	public static final ResourceKey<StructureSpeleothemConfig> YETI_CAVE = makeKey(TFStructures.YETI_CAVE.location());
	public static final ResourceKey<StructureSpeleothemConfig> TROLL_CAVE = makeKey(TFStructures.TROLL_CAVE.location());

	public static final String NBT_KEY = "config_id";

	private static final Set<ResourceKey<StructureSpeleothemConfig>> WARNED_MISSING_KEYS = ConcurrentHashMap.newKeySet();

	private static ResourceKey<StructureSpeleothemConfig> makeKey(ResourceLocation name) {
		return ResourceKey.create(REGISTRY_KEY, name);
	}

	public static void bootstrap(BootstapContext<StructureSpeleothemConfig> context) {
		bootstrapRegister(context, SMALL_HILL, RectangleLatticeIterator.TriangularLatticeConfig.DEFAULT);
		bootstrapRegister(context, MEDIUM_HILL, RectangleLatticeIterator.TriangularLatticeConfig.DEFAULT);
		bootstrapRegister(context, LARGE_HILL, RectangleLatticeIterator.TriangularLatticeConfig.DEFAULT);
		bootstrapRegister(context, HYDRA_LAIR, new RectangleLatticeIterator.TriangularLatticeConfig(4.5f));
		bootstrapRegister(context, YETI_CAVE, RectangleLatticeIterator.TriangularLatticeConfig.DEFAULT);
		bootstrapRegister(context, TROLL_CAVE, new RectangleLatticeIterator.TriangularLatticeConfig(4.5f));
	}

	private static void bootstrapRegister(BootstapContext<StructureSpeleothemConfig> context, ResourceKey<StructureSpeleothemConfig> configKey, RectangleLatticeIterator.TriangularLatticeConfig latticeConfig) {
		context.register(configKey, new StructureSpeleothemConfig(latticeConfig, configKey.location().getPath()));
	}

	public static ResourceKey<StructureSpeleothemConfig> getHollowHillKey(int hillSize) {
		return switch (hillSize) {
			case 1 -> SMALL_HILL;
			case 2 -> MEDIUM_HILL;
			default -> LARGE_HILL;
		};
	}

	/**
	 * @return The key of a structure's {@code speleothem_config}, or the fallback for structure JSONs written before the field existed
	 */
	public static ResourceKey<StructureSpeleothemConfig> keyOf(Optional<Holder<StructureSpeleothemConfig>> holder, ResourceKey<StructureSpeleothemConfig> fallback) {
		return holder.flatMap(Holder::unwrapKey).orElse(fallback);
	}

	public static ResourceKey<StructureSpeleothemConfig> readKey(CompoundTag tag, ResourceKey<StructureSpeleothemConfig> fallback) {
		if (tag.contains(NBT_KEY, Tag.TAG_STRING)) {
			ResourceLocation id = ResourceLocation.tryParse(tag.getString(NBT_KEY));

			if (id != null)
				return makeKey(id);
		}

		return fallback;
	}

	public static void writeKey(CompoundTag tag, ResourceKey<StructureSpeleothemConfig> key) {
		tag.putString(NBT_KEY, key.location().toString());
	}

	/**
	 * Resolves a speleothem config. A key that no loaded datapack defines falls back to the default lattice with a stalactite config of
	 * the same name, instead of failing chunk generation the way TF 4.8's {@code Optional.get()} does.
	 */
	public static StructureSpeleothemConfig resolve(RegistryAccess registryAccess, ResourceKey<StructureSpeleothemConfig> key) {
		Optional<StructureSpeleothemConfig> config = registryAccess.registry(REGISTRY_KEY).flatMap(registry -> registry.getOptional(key));

		if (config.isPresent())
			return config.get();

		if (WARNED_MISSING_KEYS.add(key))
			TwilightForestMod.LOGGER.warn("Structure speleothem config {} is not loaded, using the default lattice", key.location());

		return new StructureSpeleothemConfig(RectangleLatticeIterator.TriangularLatticeConfig.DEFAULT, key.location().getPath());
	}
}
