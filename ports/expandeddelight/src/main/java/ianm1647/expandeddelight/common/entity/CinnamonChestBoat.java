package ianm1647.expandeddelight.common.entity;

import ianm1647.expandeddelight.common.registry.EDEntityTypes;
import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CinnamonChestBoat extends ChestBoat {
    public static final EntityDataAccessor<Integer> DATA_ID_TYPE = SynchedEntityData.defineId(CinnamonChestBoat.class, EntityDataSerializers.INT);

    public CinnamonChestBoat(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }

    public CinnamonChestBoat(Level level, double x, double y, double z) {
        this(EDEntityTypes.CINNAMON_CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE, CinnamonBoat.Type.CINNAMON.ordinal());
    }

    // upstream overrides these two without calling super, which drops the chest's items on every save
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("Type", this.getModel().getSerializedName());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Type", 8)) {
            this.setModel(CinnamonBoat.Type.byName(compound.getString("Type")));
        }
    }

    @Override
    public Item getDropItem() {
        return switch (getModel()) {
            case CINNAMON -> EDItems.CINNAMON_CHEST_BOAT.get();
        };
    }

    public void setModel(CinnamonBoat.Type model) {
        this.entityData.set(DATA_ID_TYPE, model.ordinal());
    }

    public CinnamonBoat.Type getModel() {
        return CinnamonBoat.Type.byId(this.entityData.get(DATA_ID_TYPE));
    }
}
