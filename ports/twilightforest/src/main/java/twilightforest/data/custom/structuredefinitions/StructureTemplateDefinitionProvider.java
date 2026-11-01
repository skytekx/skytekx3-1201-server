package twilightforest.data.custom.structuredefinitions;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.GravityProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.custom.TemplateMarkerHandlers;
import twilightforest.world.components.structures.util.StructureTemplateDefinition;
import twilightforest.world.components.structures.util.StructureTemplateDefinitions;
import twilightforest.world.components.structures.util.TemplateMarkerHandlerList;
import twilightforest.world.components.structures.util.TemplatePoolInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Writes {@code data/<modid>/twilight/template_definition/<template>.json}, one file per template, listing the pools that template joins.
 * Stands in for TF 4.8's NeoForge JsonCodecProvider subclass, with the same output (codec-encoded, stable key order, no conditions).
 */
public abstract class StructureTemplateDefinitionProvider implements DataProvider {
	private final Map<ResourceLocation, Map<ResourceLocation, TemplatePoolInstance>> poolsForTemplateWeights = new HashMap<>();

	private final PackOutput.PathProvider pathProvider;
	// Protected as in 4.8's JsonCodecProvider: subclasses resolve getMarkers() through it. Pass RegistryDataGenerator's getRegistryProvider(), since the
	// plain GatherDataEvent lookup lacks TF's datapack registries and lookupOrThrow(TEMPLATE_MARKER_HANDLER_LIST_KEY) would throw
	protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
	protected final String modid;
	private final String name;

	public StructureTemplateDefinitionProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper, String name) {
		this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, StructureTemplateDefinitions.DIRECTORY);
		this.lookupProvider = lookupProvider;
		this.modid = modId;
		this.name = name;
	}

	protected abstract void generatePools();

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return this.lookupProvider.thenCompose(provider -> {
			DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, provider);

			this.poolsForTemplateWeights.clear();
			this.generatePools();

			return CompletableFuture.allOf(this.poolsForTemplateWeights.entrySet().stream().map(poolWeightsForTemplate -> {
				ResourceLocation templateId = poolWeightsForTemplate.getKey();
				StructureTemplateDefinition definition = new StructureTemplateDefinition(poolWeightsForTemplate.getValue());
				JsonElement json = Util.getOrThrow(StructureTemplateDefinition.CODEC.encodeStart(dynamicOps, definition), message -> new IllegalStateException("Couldn't encode template definition " + templateId + ": " + message));

				return DataProvider.saveStable(cache, json, this.pathProvider.json(templateId));
			}).toArray(CompletableFuture[]::new));
		});
	}

	protected void addToAllPools(String roomId, int weight, ResourceLocation... poolIds) {
		for (ResourceLocation poolId : poolIds) {
			this.add(roomId, poolId, weight);
		}
	}

	protected void addToAllPools(String roomId, TemplatePoolInstance weight, ResourceLocation... poolIds) {
		for (ResourceLocation poolId : poolIds) {
			this.add(roomId, poolId, weight);
		}
	}

	protected void addAllTemplatesToPool(ResourceLocation poolId, int weight, String... roomIds) {
		for (String roomId : roomIds) {
			this.add(roomId, poolId, weight);
		}
	}

	protected void add(String roomId, ResourceLocation poolId, int weight) {
		this.add(TwilightForestMod.prefix(roomId), poolId, weight);
	}

	protected void addEmpty(ResourceLocation poolId, int weight) {
		this.add(TwilightForestMod.prefix("_empty"), poolId, weight);
	}

	protected void add(ResourceLocation templateId, ResourceLocation poolId, int weight) {
		this.add(templateId, poolId, TemplatePoolInstance.defaultsWithWeight(weight));
	}

	protected void add(String roomId, ResourceLocation poolId, TemplatePoolInstance poolData) {
		this.add(TwilightForestMod.prefix(roomId), poolId, poolData);
	}

	protected void add(ResourceLocation templateId, ResourceLocation poolId, TemplatePoolInstance poolData) {
		Map<ResourceLocation, TemplatePoolInstance> poolWeightsForTemplate = this.poolsForTemplateWeights.computeIfAbsent(templateId, k -> new HashMap<>());

		poolWeightsForTemplate.put(poolId, poolData);
	}

	protected TemplatePoolInstance weightedPathTemplate(int weight, @Nullable Holder<TemplateMarkerHandlerList> markerHandlers, @Nullable Map<String, String> poolAliases) {
		// TODO register and reference instead
		Holder<StructureProcessorList> projectionProcessorHolder = Holder.direct(new StructureProcessorList(List.of(new GravityProcessor(Heightmap.Types.WORLD_SURFACE_WG, -2))));
		return new TemplatePoolInstance(
			Weight.of(weight),
			Optional.of(projectionProcessorHolder),
			StructureTemplatePool.Projection.RIGID,
			TerrainAdjustment.NONE,
			Optional.of(new TemplatePoolInstance.HeightAdjustment(Heightmap.Types.WORLD_SURFACE_WG, 2, Optional.of(0))),
			false,
			Optional.ofNullable(markerHandlers),
			Optional.empty(),
			poolAliases == null ? Map.of() : poolAliases
		);
	}

	protected TemplatePoolInstance weightedRigidTemplate(int weight, int beardifierGroundDelta, @Nullable Integer groundJunctionDiffLimit, @Nullable Holder<TemplateMarkerHandlerList> markerHandlers, @Nullable TemplatePoolInstance.ChooseRandomProcessors randomizedProcessors, @Nullable Map<String, String> poolAliases) {
		return new TemplatePoolInstance(
			Weight.of(weight),
			Optional.empty(),
			StructureTemplatePool.Projection.RIGID,
			TerrainAdjustment.BEARD_BOX,
			Optional.of(new TemplatePoolInstance.HeightAdjustment(Heightmap.Types.WORLD_SURFACE_WG, beardifierGroundDelta, Optional.ofNullable(groundJunctionDiffLimit))),
			true,
			Optional.ofNullable(markerHandlers),
			Optional.ofNullable(randomizedProcessors),
			poolAliases == null ? Map.of() : poolAliases
		);
	}

	// A reference to a marker handler list of the datapack registry, so the definition names it instead of inlining it
	protected Holder.Reference<TemplateMarkerHandlerList> getMarkers(HolderLookup.Provider provider, ResourceKey<TemplateMarkerHandlerList> key) {
		return Holder.Reference.createStandAlone(provider.lookupOrThrow(TemplateMarkerHandlers.TEMPLATE_MARKER_HANDLER_LIST_KEY), key);
	}

	@Override
	public String getName() {
		return String.format("%s generator for %s in %s", StructureTemplateDefinitions.DIRECTORY, this.name, this.modid);
	}
}
