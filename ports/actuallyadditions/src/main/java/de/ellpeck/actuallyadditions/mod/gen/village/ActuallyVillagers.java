package de.ellpeck.actuallyadditions.mod.gen.village;

import com.google.common.collect.ImmutableSet;
import de.ellpeck.actuallyadditions.mod.AASounds;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// SkyTekx: upstream 2a2f5c6c, the Engineer villager. Its texture is textures/entity/villager/profession/engineer.png
public class ActuallyVillagers {
	public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, ActuallyAdditions.MODID);
	public static final RegistryObject<VillagerProfession> ENGINEER = VILLAGER_PROFESSIONS.register("engineer", () -> new VillagerProfession("engineer", holder -> holder.value().equals(ActuallyPOITypes.ENGINEER.get()), holder -> holder.value().equals(ActuallyPOITypes.ENGINEER.get()), ImmutableSet.of(), ImmutableSet.of(), AASounds.VILLAGER_WORK_ENGINEER.get()));

	public static void init(IEventBus bus) {
		VILLAGER_PROFESSIONS.register(bus);
	}
}
