package twilightforest.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.chunkgenerators.AbsoluteDifferenceFunction;
import twilightforest.world.components.chunkgenerators.FocusedDensityFunction;
import twilightforest.world.components.chunkgenerators.HollowHillFunction;
import twilightforest.world.components.chunkgenerators.SqrtDensityFunction;

/**
 * Density function types of TF 4.8 that structure terraformers ({@link twilightforest.world.components.structures.CustomDensitySource}) build on.
 * TF 4.8's biome_driven_terrain and biome_driven_noise belong to its density-based chunk generator, which TF 4.3 does not use.
 */
@SuppressWarnings("unused")
public class TFDensityFunctions {
	public static final DeferredRegister<Codec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, TwilightForestMod.ID);

	public static final RegistryObject<Codec<FocusedDensityFunction>> FOCUSED = register("focused", FocusedDensityFunction.KEY_CODEC);
	public static final RegistryObject<Codec<HollowHillFunction>> HOLLOW_HILL = register("hollow_hill", HollowHillFunction.KEY_CODEC);
	public static final RegistryObject<Codec<AbsoluteDifferenceFunction.Min>> COORD_MIN = register("coord_min", AbsoluteDifferenceFunction.Min.KEY_CODEC);
	public static final RegistryObject<Codec<AbsoluteDifferenceFunction.Max>> COORD_MAX = register("coord_max", AbsoluteDifferenceFunction.Max.KEY_CODEC);
	public static final RegistryObject<Codec<SqrtDensityFunction>> SQRT = register("sqrt", SqrtDensityFunction.KEY_CODEC);

	// Registers the KeyDispatchDataCodec's own codec instance, which DensityFunction.DIRECT_CODEC looks up to encode the function
	private static <T extends DensityFunction> RegistryObject<Codec<T>> register(String name, KeyDispatchDataCodec<T> keyCodec) {
		return DENSITY_FUNCTION_TYPES.register(name, keyCodec::codec);
	}
}
