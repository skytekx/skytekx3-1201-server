package com.enderio.machines.common.blockentity.task;

import com.enderio.machines.common.blockentity.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blockentity.PoweredSpawnerMode;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.energy.IMachineEnergyStorage;
import com.enderio.machines.common.souldata.SpawnerSoul;
import com.enderio.machines.common.tag.MachineTags;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SpawnerMachineTask implements IPoweredMachineTask {
    private boolean complete;
    private int energyCost;
    private int energyConsumed = 0;
    private float efficiency = 1;
    private SpawnType spawnType = MachinesConfig.COMMON.SPAWN_TYPE.get();
    @Nullable
    private EntityType<? extends Entity> entityType;
    private static boolean reload = false;
    private boolean reloadCache = reload;
    private final PoweredSpawnerBlockEntity blockEntity;
    private final IMachineEnergyStorage energyStorage;

    /**
     * Create a new powered task.
     *
     * @param energyStorage The energy storage used to power the task.
     */
    public SpawnerMachineTask(PoweredSpawnerBlockEntity blockEntity, IMachineEnergyStorage energyStorage, Optional<ResourceLocation> rl) {
        this.blockEntity = blockEntity;
        this.energyStorage = energyStorage;
        loadSoulData(rl);
    }

    @Override
    public IMachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public void tick() {
        if (entityType == null) {
            if (reload != reloadCache) {
                reloadCache = reload;
                complete = true;
            }
            return;
        }
        if (energyConsumed >= energyCost) {
            if (isAreaClear()) {
                complete = trySpawnEntity(blockEntity.getBlockPos(), (ServerLevel) blockEntity.getLevel()); //ready to spawn but blocked for a reason.
            }
        } else {
            energyConsumed += energyStorage.consumeEnergy(energyCost - energyConsumed, false);
        }

    }

    @Override
    public float getProgress() {
        return energyConsumed / ((float)energyCost);
    }

    @Override
    public boolean isCompleted() {
        // SkyTekx: a spawner switched to another mode drops its spawn task, as the PoweredSpawnerTask of 8.x does (a90e5c512).
        return complete || blockEntity.getMode() != PoweredSpawnerMode.SPAWN;
    }

    /**
     * Check if the area has fewer spawners and mobs than the config.
     * @return
     */
    public boolean isAreaClear() {
        AABB range = new AABB(blockEntity.getBlockPos()).inflate(blockEntity.getRange());
        Optional<ResourceLocation> rl = blockEntity.getEntityType();
        if (rl.isEmpty()) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
            return false;
        }
        EntityType<?> entity = ForgeRegistries.ENTITY_TYPES.getValue(rl.get());
        if (entity == null || !ForgeRegistries.ENTITY_TYPES.getKey(entity).equals(rl.get())) { // check we don't get the default pig
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
            return false;
        }
        List<? extends Entity> entities = blockEntity.getLevel().getEntities(entity, range, p -> p instanceof LivingEntity);
        if (entities.size() >= MachinesConfig.COMMON.MAX_SPAWNER_ENTITIES.get()) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.TOO_MANY_MOB);
            return false;
        }
        long count = BlockPos.betweenClosedStream(range).filter(pos -> blockEntity.getLevel().getBlockEntity(pos) instanceof PoweredSpawnerBlockEntity).count();
        if (count >= MachinesConfig.COMMON.MAX_SPAWNERS.get()) {
            this.efficiency = MachinesConfig.COMMON.MAX_SPAWNERS.get()/(float)count;
        }
        return true;
    }
    
    private void loadSoulData(Optional<ResourceLocation> rl) {
        SoulSpawnData data = resolveSoulData(blockEntity, rl);
        if (data != null) {
            this.entityType = data.entityType();
            this.energyCost = data.energyCost();
            this.spawnType = data.spawnType();
        }
    }

    /**
     * SkyTekx: the soul lookup of the spawn task, shared with {@link SpawnerCaptureTask} so that a capture costs what a
     * spawn costs.
     *
     * @return the entity type, energy cost and spawn type of the bound soul, or null after setting the blocked reason
     * when the soul is unknown or blacklisted.
     */
    @Nullable
    static SoulSpawnData resolveSoulData(PoweredSpawnerBlockEntity blockEntity, Optional<ResourceLocation> rl) {
        if (rl.isEmpty()) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
            return null;
        }
        Optional<Holder.Reference<EntityType<?>>> optionalEntity = ForgeRegistries.ENTITY_TYPES.getDelegate(rl.get());
        if (optionalEntity.isEmpty() || ! ForgeRegistries.ENTITY_TYPES.getKey(optionalEntity.get().get()).equals(rl.get())) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
            return null;
        }
        if (optionalEntity.get().get().is(MachineTags.EntityTypes.SPAWNER_BLACKLIST)) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.DISABLED);
            return null;
        }
        EntityType<?> entityType = optionalEntity.get().get();
        Optional<SpawnerSoul.SoulData> opData = SpawnerSoul.SPAWNER.matches(rl.get());
        if (opData.isEmpty()) { //Fallback
            int energyCost = 50000;
            if (entityType.create(blockEntity.getLevel()) instanceof LivingEntity entity) { //Are we 100% guaranteed this is a living entity?
                energyCost += entity.getMaxHealth()*50; //TODO actually balance based on health
            }
            return new SoulSpawnData(entityType, energyCost, MachinesConfig.COMMON.SPAWN_TYPE.get());
        }
        SpawnerSoul.SoulData data = opData.get();
        return new SoulSpawnData(entityType, data.power(), data.spawnType());
    }

    /**
     * SkyTekx: what {@link #resolveSoulData} found for a bound soul.
     */
    record SoulSpawnData(EntityType<? extends Entity> entityType, int energyCost, SpawnType spawnType) {}

    public boolean trySpawnEntity(BlockPos pos, ServerLevel level) {
        boolean spawned = false;
        if (this.efficiency < level.random.nextFloat()) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.TOO_MANY_SPAWNER);
            return false;
        }
        for (int i = 0; i < MachinesConfig.COMMON.SPAWN_AMOUNT.get(); i++) {
            RandomSource randomsource = level.getRandom();
            double x = pos.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.blockEntity.getRange() + 0.5D;
            double y = pos.getY() + randomsource.nextInt(3) - 1;
            double z = pos.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.blockEntity.getRange() + 0.5D;

            Optional<ResourceLocation> rl = blockEntity.getEntityType();
            if (rl.isEmpty()) {
                blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
                return false;
            }
            EntityType<?> optionalEntity = ForgeRegistries.ENTITY_TYPES.getValue(rl.get());
            if (optionalEntity == null || !ForgeRegistries.ENTITY_TYPES.getKey(optionalEntity).equals(rl.get())) { // check we don't get the default pig
                blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
                return false;
            }
            if (level.noCollision(optionalEntity.getAABB(x, y, z))) {

                Entity entity = null;
                switch (spawnType) {
                    case COPY -> {
                        entity = EntityType.loadEntityRecursive(blockEntity.getEntityData().getEntityTag(), level, entity1 -> {
                            entity1.moveTo(x, y, z, entity1.getYRot(), entity1.getXRot());
                            // SkyTekx: a fresh UUID for each copy (84bfe2477). With the stored UUID only the first copy
                            // could join the level, tryAddFreshEntityWithPassengers refuses a UUID that is in use.
                            entity1.setUUID(UUID.randomUUID());
                            return entity1;
                        });
                    }
                    case ENTITYTYPE -> {
                        EntityType<?> id = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(blockEntity.getEntityData().getEntityTag().getString("id")));
                        if (id != null) {
                            entity = id.create(level);
                            entity.moveTo(x, y, z);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + spawnType);
                }

                if (entity == null) {
                    blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.UNKNOWN_MOB);
                    break;
                }

                if (entity instanceof Mob mob) { // based on vanilla spawner
                    // SkyTekx: the Mind Killer of 84bfe2477. The mob gets no AI, and the tag lets tickNoAIMobs move it.
                    if (blockEntity.hasMindKiller()) {
                        mob.setNoAi(true);
                        mob.getPersistentData().putBoolean(MOVABLE_KEY, true);
                    }
                    MobSpawnEvent.FinalizeSpawn event = ForgeEventFactory.onFinalizeSpawnSpawner(mob, level, level.getCurrentDifficultyAt(pos), null,  blockEntity.getEntityData().getEntityTag(), null);
                    if (event == null || event.isSpawnCancelled()) {
                        blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.OTHER_MOD);
                        continue;
                    } else if (spawnType != SpawnType.COPY) {
                        // SkyTekx: an exact copy keeps its equipment and data, so it skips finalizeSpawn (84bfe2477).
                        ForgeEventFactory.onFinalizeSpawn(mob, level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData(), event.getSpawnTag());
                    }
                }

                if (!level.tryAddFreshEntityWithPassengers(entity)) {
                    blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.OTHER_MOD);
                    continue;
                }

                level.levelEvent(2004, pos, 0);
                level.gameEvent(entity, GameEvent.ENTITY_PLACE, new BlockPos((int) x, (int) y, (int) z));
                if (entity instanceof Mob mob) {
                    mob.spawnAnim();
                }

                spawned = true;
                blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.NONE);
            }
        }

        if (spawned) {
            //Clear energy after spawn
            energyConsumed -= energyCost; //The same amount of energy is used for 1 or MachinesConfig.COMMON.SPAWN_AMOUNT.get() spawns, so make sure your spawner has enough valid spaces!
        }
        
        return spawned;
    }

    // region Serialization

    private static final String KEY_ENERGY_CONSUMED = "EnergyConsumed";

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(KEY_ENERGY_CONSUMED, energyConsumed);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        energyConsumed = nbt.getInt(KEY_ENERGY_CONSUMED);
    }

    // endregion

    @SubscribeEvent
    static void reloadTags(TagsUpdatedEvent event) {
        reload = !reload;
    }

    /**
     * SkyTekx: the persistent data key of a mob from a spawner with a Mind Killer (84bfe2477).
     */
    public static final String MOVABLE_KEY = "enderio:movable";

    /**
     * SkyTekx: a mob without AI does not move at all, so it would neither fall nor be pushed into a grinder. For the
     * mobs of a Mind Killer spawner this runs the movement step with the AI switched on for a moment. 84bfe2477 does
     * this on the NeoForge EntityTickEvent.Pre, the Forge 47 equivalent is LivingTickEvent at the start of
     * LivingEntity.tick.
     */
    //Credit to Shadows-of-Fire for providing this workaround https://github.com/Shadows-of-Fire/Apothic-Spawners/blob/1.21/src/main/java/dev/shadowsoffire/apothic_spawners/ASEvents.java#L120
    @SubscribeEvent
    static void tickNoAIMobs(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            if (!mob.level().isClientSide() && mob.isNoAi() && mob.getPersistentData().getBoolean(MOVABLE_KEY)) {
                mob.setNoAi(false);
                mob.travel(new Vec3(mob.xxa, mob.yya, mob.zza));
                mob.setNoAi(true);
            }
        }
    }

    // TODO: Might want to move this to its own file in future.
    public enum SpawnType {
        ENTITYTYPE("entitytype"),
        COPY("copy");

        private final String name;

        SpawnType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static DataResult<SpawnType> byName(String pTranslationKey) {
            for(SpawnType type : values()) {
                if (type.name.equals(pTranslationKey)) {
                    return DataResult.success(type);
                }
            }
            return DataResult.error(()->"unkown type");
        }
    }
}
