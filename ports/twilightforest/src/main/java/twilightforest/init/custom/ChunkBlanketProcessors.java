package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructures;
import twilightforest.world.components.chunkblanketing.CanopyBlanketProcessor;
import twilightforest.world.components.chunkblanketing.ChunkBlanketProcessor;
import twilightforest.world.components.chunkblanketing.ChunkBlanketType;
import twilightforest.world.components.chunkblanketing.GlacierBlanketProcessor;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Chunk blanketing adds blocks on top of the finished surface of a chunk, such as the Dark Forest canopy and the Glacier ice. The processor
 * types are a static registry (twilightforest:chunk_blanket_type); the processors are a datapack registry read from
 * {@code data/<namespace>/twilight/chunk_blanket_processors/}, so a datapack can retune or replace the stock canopy and glacier.
 * <p>
 * TF 4.8 runs the processors from a coremod after ChunkGenerator#buildSurface. TF 4.3's surface is built by ChunkGeneratorTwilight, which
 * calls {@link #chunkBlanketing} at the same point, so no coremod is needed.
 */
public final class ChunkBlanketProcessors {
	public static final ResourceKey<Registry<ChunkBlanketType>> CHUNK_BLANKET_TYPE_KEY = ResourceKey.createRegistryKey(TwilightForestMod.prefix("chunk_blanket_type"));
	public static final ResourceKey<Registry<ChunkBlanketProcessor>> CHUNK_BLANKET_PROCESSORS_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("chunk_blanket_processors"));

	public static final DeferredRegister<ChunkBlanketType> CHUNK_BLANKETING_TYPES = DeferredRegister.create(CHUNK_BLANKET_TYPE_KEY, TwilightForestMod.ID);
	// Only worldgen reads the types, so like NeoForge's default for TF 4.8 the registry is neither synced to clients nor saved with the world
	public static final Supplier<IForgeRegistry<ChunkBlanketType>> REGISTRY = CHUNK_BLANKETING_TYPES.makeRegistry(() -> new RegistryBuilder<ChunkBlanketType>().disableSaving().disableSync());

	public static final Codec<ChunkBlanketType> TYPE_CODEC = ExtraCodecs.lazyInitializedCodec(() -> REGISTRY.get().getCodec());
	public static final Codec<ChunkBlanketProcessor> DISPATCH_CODEC = TYPE_CODEC.dispatch("type", ChunkBlanketProcessor::getType, ChunkBlanketType::getCodec);

	public static final RegistryObject<ChunkBlanketType> CANOPY = registerType("canopy", CanopyBlanketProcessor.CODEC);
	public static final RegistryObject<ChunkBlanketType> GLACIER = registerType("glacier", GlacierBlanketProcessor.CODEC);

	public static final ResourceKey<ChunkBlanketProcessor> DARK_FOREST_CANOPY = ResourceKey.create(CHUNK_BLANKET_PROCESSORS_KEY, TwilightForestMod.prefix("dark_forest_canopy"));
	public static final ResourceKey<ChunkBlanketProcessor> SNOWY_FOREST_GLACIER = ResourceKey.create(CHUNK_BLANKET_PROCESSORS_KEY, TwilightForestMod.prefix("snowy_forest_glacier"));

	private ChunkBlanketProcessors() {
	}

	public static RegistryObject<ChunkBlanketType> registerType(String name, Codec<? extends ChunkBlanketProcessor> codec) {
		return CHUNK_BLANKETING_TYPES.register(name, () -> () -> codec);
	}

	public static void bootstrap(BootstapContext<ChunkBlanketProcessor> context) {
		HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
		HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

		context.register(DARK_FOREST_CANOPY, new CanopyBlanketProcessor(HolderSet.direct(biomes.getOrThrow(TFBiomes.DARK_FOREST), biomes.getOrThrow(TFBiomes.DARK_FOREST_CENTER)), BlockStateProvider.simple(TFBlocks.HARDENED_DARK_LEAVES.get()), 14, HolderSet.direct(structures.getOrThrow(TFStructures.DARK_TOWER))));
		context.register(SNOWY_FOREST_GLACIER, new GlacierBlanketProcessor(HolderSet.direct(biomes.getOrThrow(TFBiomes.GLACIER)), BlockStateProvider.simple(Blocks.PACKED_ICE), BlockStateProvider.simple(Blocks.ICE), 32));
	}

	/**
	 * Runs every processor whose biome mask matches a biome of the chunk. Called by ChunkGeneratorTwilight#buildSurface once the surface
	 * of the chunk is built.
	 */
	public static void chunkBlanketing(ChunkAccess chunkAccess, WorldGenRegion worldGenRegion) {
		Set<Holder<Biome>> biomesInChunk = new ObjectArraySet<>();

		for (LevelChunkSection levelchunksection : chunkAccess.getSections()) {
			levelchunksection.getBiomes().getAll(biomesInChunk::add);
		}

		Iterator<ChunkBlanketProcessor> modifierIterator = worldGenRegion.registryAccess()
				.registry(CHUNK_BLANKET_PROCESSORS_KEY)
				.map(Registry::stream)
				.orElseGet(Stream::empty)
				.filter(modifier -> modifier.biomesForApplication().stream().anyMatch(biomesInChunk::contains))
				.iterator();

		Function<BlockPos, Holder<Biome>> biomeGetter = worldGenRegion::getBiome;

		while (modifierIterator.hasNext()) {
			modifierIterator.next().processChunk(worldGenRegion.getRandom().fork(), biomeGetter, chunkAccess);
		}
	}
}
