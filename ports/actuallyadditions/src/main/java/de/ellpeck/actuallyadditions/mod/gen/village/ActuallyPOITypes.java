package de.ellpeck.actuallyadditions.mod.gen.village;

import com.google.common.collect.ImmutableSet;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// SkyTekx: upstream 2a2f5c6c. Every state of the Coffee Maker is the job site of the Engineer. Forge 47 adds the
// states to the point of interest lookup when the type registers
public class ActuallyPOITypes {
	public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, ActuallyAdditions.MODID);

	public static final RegistryObject<PoiType> ENGINEER = POI_TYPES.register("engineer", () -> new PoiType(ImmutableSet.copyOf(ActuallyBlocks.COFFEE_MACHINE.get().getStateDefinition().getPossibleStates()), 1, 1));

	public static void init(IEventBus bus) {
		POI_TYPES.register(bus);
	}
}
