package baguchi.twilight_crossbow.init;

import baguchi.twilight_crossbow.TwilightCrossBow;
import baguchi.twilight_crossbow.entity.IceFireworkRocketEntity;
import baguchi.twilight_crossbow.entity.SeekerFireworkRocketEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TwilightCrossBow.MODID);

    public static final RegistryObject<EntityType<IceFireworkRocketEntity>> ICE_FIREWORK = ENTITIES.register("ice_firework", () -> EntityType.Builder.<IceFireworkRocketEntity>of(IceFireworkRocketEntity::new, MobCategory.MISC)
            .sized(0.35F, 0.35F).updateInterval(20).build("twilight_crossbow:ice_firework"));

    public static final RegistryObject<EntityType<SeekerFireworkRocketEntity>> SEEKER_FIREWORK = ENTITIES.register("seeker_firework", () -> EntityType.Builder.<SeekerFireworkRocketEntity>of(SeekerFireworkRocketEntity::new, MobCategory.MISC)
            .sized(0.35F, 0.35F).updateInterval(20).build("twilight_crossbow:seeker_firework"));

}
