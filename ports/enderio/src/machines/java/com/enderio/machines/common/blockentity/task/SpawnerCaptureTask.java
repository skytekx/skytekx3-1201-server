package com.enderio.machines.common.blockentity.task;

import com.enderio.api.capability.StoredEntityData;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.item.tool.SoulVialItem;
import com.enderio.base.common.util.EntityCaptureUtils;
import com.enderio.machines.common.blockentity.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blockentity.PoweredSpawnerMode;
import com.enderio.machines.common.io.energy.IMachineEnergyStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * SkyTekx: the capture mode of the 8.x powered spawner (MobCaptureTask of a90e5c512, with the output check of
 * f8049645c). Instead of spawning the bound mob, the spawner pays the energy of one spawn and fills one empty soul vial
 * from its input slot with the bound soul.
 * <p>
 * Differences from upstream:
 * <ul>
 *     <li>The 6.x spawn task {@link SpawnerMachineTask} stays as it is, with its NBT key {@code EnergyConsumed}. This task
 *     sits beside it and saves the same key.</li>
 *     <li>The filled soul vial of 6.x stacks to 1 (the vial unification be47284da that lets 8.x stack the vials of
 *     1853ceeb5 is not ported), so the output takes one vial and the spawner waits until it is taken.</li>
 *     <li>The vial gets the soul through the 6.x entity storage capability, the setter of {@link SoulVialItem}.</li>
 * </ul>
 */
public class SpawnerCaptureTask implements IPoweredMachineTask {
    private final PoweredSpawnerBlockEntity blockEntity;
    private final IMachineEnergyStorage energyStorage;

    @Nullable
    private EntityType<? extends Entity> entityType;
    private int energyCost;
    private int energyConsumed = 0;
    private SpawnerMachineTask.SpawnType spawnType = SpawnerMachineTask.SpawnType.ENTITYTYPE;
    private boolean complete;

    public SpawnerCaptureTask(PoweredSpawnerBlockEntity blockEntity, IMachineEnergyStorage energyStorage, Optional<ResourceLocation> rl) {
        this.blockEntity = blockEntity;
        this.energyStorage = energyStorage;

        SpawnerMachineTask.SoulSpawnData data = SpawnerMachineTask.resolveSoulData(blockEntity, rl);
        if (data == null) {
            // Unknown or blacklisted, resolveSoulData set the reason. The spawner drops the task and tries again on its
            // next inventory or mode change.
            complete = true;
            return;
        }

        // The cast is what SoulVialItem does, a bound soul always comes from a soul vial.
        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) data.entityType();
        if (EntityCaptureUtils.getCapturableStatus(livingType, null) != EntityCaptureUtils.CapturableStatus.CAPTURABLE) {
            // A soul that a soul vial may not hold, by the soul vial blacklist.
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.DISABLED);
            complete = true;
            return;
        }

        this.entityType = data.entityType();
        this.energyCost = data.energyCost();
        this.spawnType = data.spawnType();

        // Do not start while the output cannot take the vial, so no energy is spent on a capture that has to wait.
        if (!canOutput(blockEntity, createFilledVial())) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.OUTPUT_FULL);
            complete = true;
        }
    }

    /**
     * Whether the input holds an empty soul vial.
     */
    public static boolean hasEmptyVial(PoweredSpawnerBlockEntity blockEntity) {
        return PoweredSpawnerBlockEntity.INPUT.getItemStack(blockEntity).is(EIOItems.EMPTY_SOUL_VIAL.get());
    }

    /**
     * Whether the output can take one more vial like {@code result} (f8049645c). It never takes more than the stack size.
     */
    public static boolean canOutput(PoweredSpawnerBlockEntity blockEntity, ItemStack result) {
        ItemStack output = PoweredSpawnerBlockEntity.OUTPUT.getItemStack(blockEntity);
        if (output.isEmpty()) {
            return true;
        }

        return ItemStack.isSameItemSameTags(output, result) && output.getCount() + 1 <= output.getMaxStackSize();
    }

    @Override
    public IMachineEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public void tick() {
        if (entityType == null) {
            complete = true;
            return;
        }

        if (energyConsumed < energyCost) {
            energyConsumed += energyStorage.consumeEnergy(energyCost - energyConsumed, false);
            return;
        }

        capture();
    }

    private void capture() {
        ItemStack input = PoweredSpawnerBlockEntity.INPUT.getItemStack(blockEntity);
        if (!input.is(EIOItems.EMPTY_SOUL_VIAL.get())) {
            complete = true;
            return;
        }

        ItemStack result = createFilledVial();

        // Do not overwrite or overfill the output. The task keeps its energy and waits until the vial is taken.
        if (!canOutput(blockEntity, result)) {
            blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.OUTPUT_FULL);
            return;
        }

        ItemStack output = PoweredSpawnerBlockEntity.OUTPUT.getItemStack(blockEntity);
        if (!output.isEmpty()) {
            result.setCount(output.getCount() + 1);
        }

        PoweredSpawnerBlockEntity.OUTPUT.setStackInSlot(blockEntity, result);
        PoweredSpawnerBlockEntity.INPUT.setStackInSlot(blockEntity, input.copyWithCount(input.getCount() - 1));

        blockEntity.setReason(PoweredSpawnerBlockEntity.SpawnerBlockedReason.NONE);
        complete = true;
    }

    /**
     * The vial for the bound soul. In ENTITYTYPE mode it holds only the entity type, like the vials of the creative tab,
     * so a soul bound from a mob with a sword, armour or villager trades never copies them. Only COPY mode copies the
     * whole stored entity, as MobCaptureTask does for MobSpawnMode.COPY.
     */
    private ItemStack createFilledVial() {
        ItemStack vial = EIOItems.FILLED_SOUL_VIAL.get().getDefaultInstance();
        switch (spawnType) {
            case ENTITYTYPE -> SoulVialItem.setEntityType(vial, ForgeRegistries.ENTITY_TYPES.getKey(entityType));
            case COPY -> {
                // A deep copy, so that the vial never shares a tag with the spawner.
                StoredEntityData copy = new StoredEntityData();
                copy.deserializeNBT(blockEntity.getEntityData().serializeNBT().copy());
                SoulVialItem.setEntityData(vial, copy);
            }
        }

        return vial;
    }

    @Override
    public float getProgress() {
        if (energyCost <= 0) {
            return 0;
        }

        return energyConsumed / ((float) energyCost);
    }

    @Override
    public boolean isCompleted() {
        // A spawner switched to another mode or without an empty vial drops the task, as MobCaptureTask does.
        return complete || blockEntity.getMode() != PoweredSpawnerMode.CAPTURE || !hasEmptyVial(blockEntity);
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
}
