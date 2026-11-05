package com.enderio.base.common.capability;

import com.enderio.api.capability.IEntityStorage;
import com.enderio.api.capability.StoredEntityData;
import com.enderio.base.EIONBTKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class EntityStorageItemStack implements IEntityStorage {

    private final ItemStack stack;

    public EntityStorageItemStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public StoredEntityData getStoredEntityData() {
        CompoundTag tag = stack.getOrCreateTag();
        StoredEntityData entity = StoredEntityData.empty();
        if (tag.contains(BlockItem.BLOCK_ENTITY_TAG)) {
            CompoundTag entityTag = tag.getCompound(BlockItem.BLOCK_ENTITY_TAG).getCompound(EIONBTKeys.ENTITY_STORAGE);
            entity.deserializeNBT(entityTag);
        }
        return entity;
    }

    @Override
    public void setStoredEntityData(StoredEntityData entity) {
        // Only replace the stored entity, and keep whatever else the block entity tag holds.
        CompoundTag blockEntityTag = stack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG);
        blockEntityTag.put(EIONBTKeys.ENTITY_STORAGE, entity.serializeNBT());
    }
}
