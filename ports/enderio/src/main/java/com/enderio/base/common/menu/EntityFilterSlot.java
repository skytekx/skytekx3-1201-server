package com.enderio.base.common.menu;

import com.enderio.api.capability.IEntityStorage;
import com.enderio.api.capability.StoredEntityData;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.core.common.menu.FilterSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.function.Consumer;

public class EntityFilterSlot extends FilterSlot<StoredEntityData> {

    public EntityFilterSlot(Consumer<StoredEntityData> consumer, int pSlot, int pX, int pY) {
        super(consumer, pSlot, pX, pY);
    }

    @Override
    public Optional<StoredEntityData> getResourceFrom(ItemStack itemStack) {

        LazyOptional<IEntityStorage> entityStorageOptional = itemStack.getCapability(EIOCapabilities.ENTITY_STORAGE);
        if (entityStorageOptional.isPresent()) {
            IEntityStorage storage = entityStorageOptional.resolve().get();
            return Optional.of(storage.getStoredEntityData());
        }

        // The menu also runs on the dedicated server, so only the entity type is stored. Creating the entity in the
        // client level made the server fail to load this class at all.
        if (itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            return Optional.of(StoredEntityData.of(EntityType.getKey(spawnEggItem.getType(itemStack.getTag()))));
        }

        return Optional.empty();
    }
}
