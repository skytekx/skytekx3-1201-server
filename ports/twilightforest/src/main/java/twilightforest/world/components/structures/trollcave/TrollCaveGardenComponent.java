package twilightforest.world.components.structures.trollcave;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFLandmark;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.init.TFConfiguredFeatures;
import twilightforest.world.components.feature.BlockSpikeFeature;

import java.util.function.Predicate;


public class TrollCaveGardenComponent extends TrollCaveMainComponent {

	public TrollCaveGardenComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFTCGard.get(), nbt);
	}

	public TrollCaveGardenComponent(int index, int x, int y, int z, int caveSize, int caveHeight, Direction direction) {
		super(TFStructurePieceTypes.TFTCGard.get(), index, x, y, z);
		this.size = caveSize;
		this.height = caveHeight;
		this.setOrientation(direction);
		this.boundingBox = TFLandmark.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, size - 1, height - 1, size - 1, direction, false);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
		// add a cloud
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);
		Predicate<Biome> highlands = biome -> biome == biomeRegistry.get(TFBiomes.HIGHLANDS) || biome == biomeRegistry.get(TFBiomes.HIGHLANDS_UNDERGROUND);
		// The structure pivot (blockPos) sits at the troll cave's minY (about y -20), in the underground layer of the
		// Highlands TerrainColumn. Upstream 4.8 (ecf49ebef) samples there and accepts HIGHLANDS_UNDERGROUND as well,
		// which the predicate mirrors. Sampling stays at sea level (0, quart 0), like 4.3's getBiome(x, 0, z), where the
		// Highlands column is HIGHLANDS whatever its underground layer is (a data pack may still map the old shared
		// twilightforest:underground layer there, which must keep rejecting gardens outside the Highlands).
		if (this.isBoundingBoxOutsideBiomes(world, highlands, blockPos.atY(generator.getSeaLevel()))) {
			return;
		}

		// clear inside
		hollowCaveMiddle(world, sbb, rand, 0, 0, 0, this.size - 1, this.height - 1, this.size - 1);
		RandomSource decoRNG = RandomSource.create(world.getSeed() + (this.boundingBox.minX() * 321534781L) ^ (this.boundingBox.minZ() * 756839L));

		// treasure!
		makeTreasureCrate(world, sbb);

		// dirt!
		for (int i = 0; i < 24; i++) {
			BlockPos dest = getCoordsInCave(decoRNG);
			generate(world, generator, TFConfiguredFeatures.TROLL_CAVE_DIRT, decoRNG, dest.getX(), 0, dest.getZ(), sbb);
		}

		// mycelium!
		for (int i = 0; i < 16; i++) {
			BlockPos dest = getCoordsInCave(decoRNG);
			generate(world, generator, TFConfiguredFeatures.TROLL_CAVE_MYCELIUM, decoRNG, dest.getX(), 0, dest.getZ(), sbb);
		}

		// uberous!
		for (int i = 0; i < 16; i++) {
			BlockPos dest = getCoordsInCave(decoRNG);
			generate(world, generator, TFConfiguredFeatures.UBEROUS_SOIL_PATCH_SMALL, decoRNG, dest.getX(), 0, dest.getZ(), sbb);

			generateAtSurface(world, generator, TFConfiguredFeatures.UBEROUS_SOIL_PATCH_SMALL, decoRNG, dest.getX(), dest.getZ(), sbb);
		}

		// mushglooms first
		for (int i = 0; i < 8; i++) {
			BlockPos.MutableBlockPos dest = getCoordsInCave(decoRNG);
			setBlockStateRotated(world, Blocks.MYCELIUM.defaultBlockState(), dest.getX(), dest.setY(0).getY(), dest.getZ(), this.rotation, sbb);
			generate(world, generator, TFConfiguredFeatures.TROLL_BIG_MUSHGLOOMS, decoRNG, dest.getX(), dest.setY(1).getY(), dest.getZ(), sbb);
		}

		// mushrooms!
		for (int i = 0; i < 16; i++) {
			BlockPos.MutableBlockPos dest = getCoordsInCave(decoRNG);
			setBlockStateRotated(world, Blocks.MYCELIUM.defaultBlockState(), dest.getX(), dest.setY(0).getY(), dest.getZ(), this.rotation, sbb);
			generate(world, generator, rand.nextBoolean() ? TFConfiguredFeatures.TROLL_HUGE_BROWN_MUSHROOMS : TFConfiguredFeatures.TROLL_HUGE_RED_MUSHROOMS, decoRNG, dest.getX(), dest.setY(1).getY(), dest.getZ(), sbb);
		}

		// stone stalactites!
		for (int i = 0; i < 128; i++) {
			BlockPos dest = getCoordsInCave(decoRNG);
			generateBlockSpike(world, BlockSpikeFeature.STONE_STALACTITE, dest.atY(this.height), sbb, true);
		}
	}

	protected void generate(WorldGenLevel world, ChunkGenerator generator, ResourceKey<ConfiguredFeature<?, ?>> feature, RandomSource rand, int x, int y, int z, BoundingBox sbb) {
		// are the coordinates in our bounding box?
		int dx = getWorldX(x, z);
		int dy = getWorldY(y);
		int dz = getWorldZ(x, z);

		BlockPos pos = new BlockPos(dx, dy, dz);
		if (sbb.isInside(pos)) {
			world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(feature).place(world, generator, rand, pos);
		}
	}
}
