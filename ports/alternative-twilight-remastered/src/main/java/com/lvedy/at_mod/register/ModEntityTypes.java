package com.lvedy.at_mod.register;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.special.ModEntity.entity.NagaVenomEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AlternativeTwilight.MODID);

    public static final RegistryObject<EntityType<NagaVenomEntity>> NAGA_VENOM =
            ENTITY_TYPES.register("naga_venom", () -> EntityType.Builder.<NagaVenomEntity>of(NagaVenomEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(AlternativeTwilight.prefix("naga_venom").toString()));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
