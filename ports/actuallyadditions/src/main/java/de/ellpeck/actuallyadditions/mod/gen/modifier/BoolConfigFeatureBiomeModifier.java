package de.ellpeck.actuallyadditions.mod.gen.modifier;

import com.mojang.serialization.Codec;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.gen.ActuallyBiomeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

// SkyTekx: upstream 02422d65. Adds its features to the listed biomes when the named switch of the common
// config is on. Forge applies biome modifiers once when a server starts, after the common config loaded,
// so a changed switch takes effect at the next start and only in chunks generated after it
public record BoolConfigFeatureBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features, GenerationStep.Decoration step,
                                             String boolConfig) implements BiomeModifier {

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && checkConfig() && this.biomes.contains(biome)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			this.features.forEach(holder -> generationSettings.addFeature(this.step, holder));
		}
	}

	private boolean checkConfig() {
		switch (boolConfig) {
			case "generateQuartz":
				return CommonConfig.Worldgen.GENERATE_QUARTZ.get();
			case "generateCanola":
				return CommonConfig.Worldgen.GENERATE_CANOLA.get();
			case "generateFlax":
				return CommonConfig.Worldgen.GENERATE_FLAX.get();
			case "generateCoffee":
				return CommonConfig.Worldgen.GENERATE_COFFEE.get();
			// SkyTekx: main defines generateRice (2ffe78d6) but never reads it, so its rice patches ignore the
			// switch. Here it works like the other three
			case "generateRice":
				return CommonConfig.Worldgen.GENERATE_RICE.get();
			default:
				return true;
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return ActuallyBiomeModifiers.BOOL_CONFIG_MODIFIER.get();
	}
}
