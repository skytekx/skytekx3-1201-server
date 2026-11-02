package com.twilightmobsloots;

import com.twilightmobsloots.block.entity.MobTrophyBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Block entity registry: all 96 trophy blocks share one {@link BlockEntityType}. The block entity stores
 * no data; the head is drawn entirely by the client renderer.
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TwilightMobsLoots.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // the data fixer type may be null
    public static final RegistryObject<BlockEntityType<MobTrophyBlockEntity>> MOB_TROPHY =
            BLOCK_ENTITIES.register("mob_trophy", () ->
                    BlockEntityType.Builder.of(MobTrophyBlockEntity::new, ModBlocks.allBlocks()).build(null));
}
