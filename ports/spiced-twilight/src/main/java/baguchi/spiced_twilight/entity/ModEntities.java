package baguchi.spiced_twilight.entity;

import baguchi.spiced_twilight.SpicedTwilight;
import baguchi.spiced_twilight.entity.projectile.MazeSlimeBallProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = SpicedTwilight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SpicedTwilight.MODID);

    public static final RegistryObject<EntityType<MazeSlimeBallProjectile>> MAZE_SLIME_BALL = ENTITIES.register("maze_slime_ball", () -> EntityType.Builder.<MazeSlimeBallProjectile>of(MazeSlimeBallProjectile::new, MobCategory.MISC)
            .sized(0.35F, 0.35F).updateInterval(20).build(SpicedTwilight.MODID + ":maze_slime_ball"));
    public static final RegistryObject<EntityType<MazeSlimeBeetle>> MAZE_SLIME_BEETLE = ENTITIES.register("maze_slime_beetle", () -> EntityType.Builder.of(MazeSlimeBeetle::new, MobCategory.MONSTER).sized(0.9F, 0.5F).build(SpicedTwilight.MODID + ":maze_slime_beetle"));


    @SubscribeEvent
    public static void registerSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(MAZE_SLIME_BEETLE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void registerEntityAttribute(EntityAttributeCreationEvent event) {
        event.put(MAZE_SLIME_BEETLE.get(), MazeSlimeBeetle.registerAttributes().build());
    }
}
