package twilightforest.init;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.world.components.TFCavesCarver;

import java.util.List;

//this was all put into 1 class because it seems like a waste to have it in 2
public class TFCaveCarvers {
	// The carvers take their wall block providers in the constructor, and the highland one needs Trollsteinn, so they can
	// no longer be built while this class loads; a DeferredRegister constructs them once blocks are registered.
	public static final DeferredRegister<WorldCarver<?>> CARVER_TYPES = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, TwilightForestMod.ID);

	public static final RegistryObject<TFCavesCarver> TF_CAVES = CARVER_TYPES.register("tf_caves", () -> new TFCavesCarver(
		CaveCarverConfiguration.CODEC,
		false,
		new NoiseProvider(
			6972119253061020355L,
			new NormalNoise.NoiseParameters(0, 1.0),
			0.5f,
			List.of(
				Blocks.DIRT.defaultBlockState(),
				Blocks.DIRT.defaultBlockState(),
				Blocks.ROOTED_DIRT.defaultBlockState(),
				Blocks.DIRT.defaultBlockState(),
				Blocks.DIRT.defaultBlockState(),
				Blocks.COARSE_DIRT.defaultBlockState(),
				Blocks.DIRT.defaultBlockState(),
				Blocks.DIRT.defaultBlockState()
			)
		)
	));
	public static final RegistryObject<TFCavesCarver> HIGHLAND_CAVES = CARVER_TYPES.register("highland_caves", () -> new TFCavesCarver(
		CaveCarverConfiguration.CODEC,
		true,
		new WeightedStateProvider(
			new SimpleWeightedRandomList.Builder<BlockState>()
				.add(TFBlocks.TROLLSTEINN.get().defaultBlockState(), 1)
				.add(Blocks.STONE.defaultBlockState(), 3)
		)
	));

	/**
	 * How much higher 4.8 places Troll Caves than 4.3 does (4.8 TrollCaveStructure.getFirstPiece passes y + 11). Height
	 * values that 4.8 tuned around the Troll Caves are lowered by this much.
	 */
	public static final int TROLL_CAVE_HEIGHT_OFFSET = 11;

	public static final ResourceKey<ConfiguredWorldCarver<?>> TFCAVES_CONFIGURED = registerKey("tf_caves");
	public static final ResourceKey<ConfiguredWorldCarver<?>> HIGHLANDCAVES_CONFIGURED = registerKey("highland_caves");

	private static ResourceKey<ConfiguredWorldCarver<?>> registerKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_CARVER, TwilightForestMod.prefix(name));
	}

	public static void bootstrap(BootstapContext<ConfiguredWorldCarver<?>> context) {
		HolderGetter<Block> blocks = context.lookup(Registries.BLOCK);
		context.register(TFCAVES_CONFIGURED, TF_CAVES.get().configured(new CaveCarverConfiguration(
			0.1F,
			UniformHeight.of(VerticalAnchor.aboveBottom(16), VerticalAnchor.absolute(-8)),
			ConstantFloat.of(0.6F),
			VerticalAnchor.bottom(),
			blocks.getOrThrow(BlockTagGenerator.CARVER_REPLACEABLES),
			ConstantFloat.of(1.05F),
			ConstantFloat.of(1.05F),
			ConstantFloat.of(-0.7F)
		)));

		// 4.8 carves these tunnels between absolute(8) and absolute(32), around Troll Caves that it places 11 blocks
		// above sea level (TrollCaveStructure: y + 11, 4a68b96e3). 4.3 still places Troll Caves at sea level, so the band
		// is shifted down by those 11 blocks to keep the tunnels wrapped around the caves instead of above them.
		context.register(HIGHLANDCAVES_CONFIGURED, HIGHLAND_CAVES.get().configured(new CaveCarverConfiguration(
			1f,
			BiasedToBottomHeight.of(VerticalAnchor.absolute(8 - TROLL_CAVE_HEIGHT_OFFSET), VerticalAnchor.absolute(32 - TROLL_CAVE_HEIGHT_OFFSET), 16),
			ConstantFloat.of(0.6f),
			VerticalAnchor.bottom(),
			blocks.getOrThrow(BlockTagGenerator.CARVER_REPLACEABLES),
			UniformFloat.of(1.1f, 1.3f),
			ConstantFloat.of(1.1f),
			UniformFloat.of(-0.9F, -0.65F)
		)));
	}
}
