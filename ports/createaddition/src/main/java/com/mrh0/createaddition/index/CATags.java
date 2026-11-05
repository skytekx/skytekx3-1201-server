package com.mrh0.createaddition.index;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * SkyTekx: the tag keys of the 1.21.1 line's CATagRegister that this backport reads in code.
 * The 1.21.1 line names them in the c: namespace, this build keeps the forge: namespace of the
 * 1.20.1 tags (data/forge/tags/), which the pack unifies through.
 */
public class CATags {
	public static class Fluids {
		/**
		 * Fluids that catch fire and carry it (1.21.1 c:ignites, upstream 0d13505 and f224bb4).
		 * Filled by data/forge/tags/fluids/ignites.json and read by the fire and fluid mixins
		 * through {@link #ignites(Fluid)}.
		 */
		public static final TagKey<Fluid> IGNITES = forge("ignites");

		/**
		 * SkyTekx: whether a fluid ignites. Upstream checks only the fluid itself. Many Forge 1.20.1
		 * mods put just the source fluid into their tags (Immersive Engineering 10.2.0 lists
		 * immersiveengineering:creosote in forge:creosote, not its flowing variant), so a flowing
		 * fluid also counts when its source fluid is in the tag.
		 */
		public static boolean ignites(Fluid fluid) {
			if (fluid.is(IGNITES)) {
				return true;
			}
			return fluid instanceof FlowingFluid flowing && flowing.getSource().is(IGNITES);
		}

		/** SkyTekx: {@link #ignites(Fluid)} for the fluid of a fluid state. */
		public static boolean ignites(FluidState state) {
			return !state.isEmpty() && ignites(state.getType());
		}

		private static TagKey<Fluid> forge(String name) {
			return TagKey.create(Registries.FLUID, new ResourceLocation("forge", name));
		}
	}
}
