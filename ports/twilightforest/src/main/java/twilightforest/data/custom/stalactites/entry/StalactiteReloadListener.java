package twilightforest.data.custom.stalactites.entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.structures.util.CodecResourceReloadListener;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the speleothem varieties ({@link SpeleothemVarietyConfig}) from {@code data/<namespace>/twilight/stalactites/<name>.json} and the
 * speleothems they list from {@code data/<namespace>/twilight/stalactites/entries/<name>.json}. Structures pick a variety by type through
 * their {@code twilight/structure_speleothem_settings} entry.
 * <p>
 * TF 4.3 read hollow hill ore lists from {@code data/<namespace>/stalactites/<small|medium|large>_hollow_hill.json} and their entries from
 * {@code data/<namespace>/stalactites/entries/}. Those files are still read as a fallback, see {@link #applyLegacyHillLists}.
 */
public class StalactiteReloadListener extends CodecResourceReloadListener<SpeleothemVarietyConfig> {
	public static final StalactiteReloadListener INSTANCE = new StalactiteReloadListener();

	public static final String STALACTITE_DIRECTORY = "twilight/stalactites";
	public static final String LEGACY_STALACTITE_DIRECTORY = "stalactites";
	// Hill sizes of the TF 4.3 lists, smallest first: a 4.3 list also applied to every larger hill
	private static final List<String> LEGACY_HILL_TYPES = List.of("small_hollow_hill", "medium_hollow_hill", "large_hollow_hill");

	// Working state of the reload in progress, only touched by the thread applying the reload
	private final Map<String, SpeleothemVarietyConfig> hillConfigs = new HashMap<>();
	private final Map<String, List<Stalactite>> stalactitesPerHill = new HashMap<>();
	private final Map<String, List<Stalactite>> oreStalactitesPerHill = new HashMap<>();
	private final Map<String, List<Stalactite>> stalagmitesPerHill = new HashMap<>();

	// Published at the end of each reload; worldgen threads read this immutable snapshot
	private static volatile Map<String, SpeleothemVariety> varieties = Map.of();
	private static final Map<String, SpeleothemVariety> MISSING_TYPES = new ConcurrentHashMap<>();

	public StalactiteReloadListener() {
		super(STALACTITE_DIRECTORY, SpeleothemVarietyConfig.CODEC);
	}

	/**
	 * @return The compiled variety of the given type, or an empty variety (no stalactites, no stalagmites) if no datapack defines that type
	 */
	public static SpeleothemVariety getVariety(String type) {
		SpeleothemVariety variety = varieties.get(type);

		if (variety == null) {
			return MISSING_TYPES.computeIfAbsent(type, StalactiteReloadListener::emptyVariety);
		}

		return variety;
	}

	private static SpeleothemVariety emptyVariety(String type) {
		TwilightForestMod.LOGGER.warn("No stalactite config of type '{}' is loaded, structures using it get no speleothems", type);
		return SpeleothemVariety.compile(new SpeleothemVarietyConfig(type, List.of(), List.of(), List.of(), 0, 0, 0, false), List.of(), List.of(), List.of());
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
		// TF 4.8 cleared HILL_CONFIGS twice here and never STALACTITES_PER_HILL, so base stalactites piled up with every /reload
		this.clearWorkingState();

		super.apply(map, manager, profiler);

		this.applyLegacyHillLists(manager);

		Map<String, SpeleothemVariety> compiled = new HashMap<>();
		for (SpeleothemVarietyConfig config : this.hillConfigs.values()) {
			String type = config.type();
			compiled.put(type, SpeleothemVariety.compile(config,
					this.stalactitesPerHill.getOrDefault(type, List.of()),
					this.oreStalactitesPerHill.getOrDefault(type, List.of()),
					this.stalagmitesPerHill.getOrDefault(type, List.of())));
		}

		varieties = Map.copyOf(compiled);
		MISSING_TYPES.clear();
		this.clearWorkingState();

		TwilightForestMod.LOGGER.info("Loaded {} Stalactite configs!", compiled.size());
	}

	private void clearWorkingState() {
		this.hillConfigs.clear();
		this.stalactitesPerHill.clear();
		this.oreStalactitesPerHill.clear();
		this.stalagmitesPerHill.clear();
	}

	@Override
	protected void forLocation(ResourceManager manager, ResourceLocation location, SpeleothemVarietyConfig config) {
		if (!this.hillConfigs.containsKey(config.type()) || config.replace()) {
			this.hillConfigs.put(config.type(), config);
			if (config.replace()) {
				TwilightForestMod.LOGGER.info("Stalactite Config {} wiped by {}", config.type(), location.getNamespace());
			}
		}

		this.populateList(manager, config, config.baseStalactites(), this.stalactitesPerHill);
		this.populateList(manager, config, config.oreStalactites(), this.oreStalactitesPerHill);
		this.populateList(manager, config, config.stalagmites(), this.stalagmitesPerHill);
	}

	private void populateList(ResourceManager manager, SpeleothemVarietyConfig config, List<ResourceLocation> rawEntries, Map<String, List<Stalactite>> stalactiteDict) {
		List<Stalactite> stalactitesForType = stalactiteDict.computeIfAbsent(config.type(), k -> new ArrayList<>());

		if (config.replace()) stalactitesForType.clear();

		for (ResourceLocation rl : rawEntries) {
			ResourceLocation fileLocation = new ResourceLocation(rl.getNamespace(), String.format("%s/%s.json", STALACTITE_DIRECTORY, rl.getPath()));
			this.readStalactite(manager, fileLocation).ifPresent(stalactite -> {
				stalactitesForType.add(stalactite);
				TwilightForestMod.LOGGER.debug("Loaded Stalactite {} for config {}", fileLocation, config.type());
			});
		}
	}

	/**
	 * Reads a speleothem entry in the TF 4.8 format, or in the TF 4.3 {@code "blocks"} format as a fallback.
	 */
	private Optional<Stalactite> readStalactite(ResourceManager manager, ResourceLocation fileLocation) {
		Optional<Resource> stalRes = manager.getResource(fileLocation);
		if (stalRes.isEmpty()) {
			TwilightForestMod.LOGGER.error("Could not find stalactite entry for {}", fileLocation);
			return Optional.empty();
		}

		try (Reader stalReader = stalRes.get().openAsReader()) {
			JsonObject stalObject = GsonHelper.fromJson(this.gson, stalReader, JsonObject.class);
			if (stalObject == null) {
				TwilightForestMod.LOGGER.error("Stalactite entry {} is empty", fileLocation);
				return Optional.empty();
			}

			if (!stalObject.has("ores") && stalObject.has("blocks"))
				return Optional.of(Stalactite.fromLegacyJson(stalObject));

			return Stalactite.CODEC.parse(JsonOps.INSTANCE, stalObject).resultOrPartial(error -> TwilightForestMod.LOGGER.error("Failed to parse stalactite entry {}: {}", fileLocation, error));
		} catch (RuntimeException | IOException e) {
			TwilightForestMod.LOGGER.error("Failed to parse stalactite entry {}", fileLocation, e);
			return Optional.empty();
		}
	}

	/**
	 * Reads the TF 4.3 hollow hill lists, {@code {"replace": false, "stalactites": ["<namespace>:<name>", ...]}} at
	 * {@code data/<namespace>/stalactites/<small|medium|large>_hollow_hill.json}, whose entries live at
	 * {@code data/<namespace>/stalactites/entries/<name>.json}. Twilight Forest itself no longer ships these; this keeps datapacks written
	 * for 4.3 working. As in 4.3 every listed entry is an ore stalactite of that hill size and of every larger one, and the entry weights
	 * now take part in the weighted pick. {@code "replace": true} first clears the ore stalactites that hill size already has.
	 */
	private void applyLegacyHillLists(ResourceManager manager) {
		List<String> namespaces = new ArrayList<>(manager.getNamespaces());
		namespaces.sort(Comparator.comparing((String namespace) -> !TwilightForestMod.ID.equals(namespace)).thenComparing(Comparator.naturalOrder()));

		Map<String, Set<ResourceLocation>> addedPerType = new HashMap<>();

		for (int hillSize = 0; hillSize < LEGACY_HILL_TYPES.size(); hillSize++) {
			String type = LEGACY_HILL_TYPES.get(hillSize);

			for (String namespace : namespaces) {
				ResourceLocation listLocation = new ResourceLocation(namespace, LEGACY_STALACTITE_DIRECTORY + "/" + type + ".json");

				for (Resource resource : manager.getResourceStack(listLocation)) {
					List<ResourceLocation> entries = new ArrayList<>();
					boolean replace;

					try (Reader reader = resource.openAsReader()) {
						JsonObject list = GsonHelper.fromJson(this.gson, reader, JsonObject.class);
						if (list == null) continue;

						replace = GsonHelper.getAsBoolean(list, "replace", false);
						for (JsonElement entry : GsonHelper.getAsJsonArray(list, "stalactites")) {
							ResourceLocation entryId = ResourceLocation.tryParse(GsonHelper.convertToString(entry, "stalactite"));
							if (entryId != null) entries.add(entryId);
						}
					} catch (RuntimeException | IOException e) {
						TwilightForestMod.LOGGER.error("Couldn't read legacy Hollow Hill list {} in data pack {}", listLocation, resource.sourcePackId(), e);
						continue;
					}

					TwilightForestMod.LOGGER.info("Reading legacy TF 4.3 Hollow Hill list {} from data pack {}, consider moving it to {}", listLocation, resource.sourcePackId(), STALACTITE_DIRECTORY);

					if (replace) {
						this.oreStalactitesPerHill.computeIfAbsent(type, k -> new ArrayList<>()).clear();
						addedPerType.computeIfAbsent(type, k -> new HashSet<>()).clear();
						TwilightForestMod.LOGGER.info("Ore stalactites of {} wiped by legacy list {}", type, listLocation);
					}

					for (ResourceLocation entry : entries) {
						ResourceLocation fileLocation = new ResourceLocation(entry.getNamespace(), LEGACY_STALACTITE_DIRECTORY + "/entries/" + entry.getPath() + ".json");
						Optional<Stalactite> stalactite = this.readStalactite(manager, fileLocation);
						if (stalactite.isEmpty()) continue;

						// A 4.3 list for a smaller hill also fed every larger hill
						for (String targetType : LEGACY_HILL_TYPES.subList(hillSize, LEGACY_HILL_TYPES.size())) {
							if (!this.hillConfigs.containsKey(targetType)) {
								TwilightForestMod.LOGGER.warn("No stalactite config of type '{}' is loaded, skipping legacy entry {}", targetType, fileLocation);
								continue;
							}

							if (addedPerType.computeIfAbsent(targetType, k -> new HashSet<>()).add(fileLocation)) {
								this.oreStalactitesPerHill.computeIfAbsent(targetType, k -> new ArrayList<>()).add(stalactite.get());
							}
						}
					}
				}
			}
		}
	}
}
