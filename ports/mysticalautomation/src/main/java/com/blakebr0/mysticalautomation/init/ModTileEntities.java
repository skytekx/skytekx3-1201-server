package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.tileentity.AwakeningAltarnatorTileEntity;
import com.blakebr0.mysticalautomation.tileentity.CrafterTileEntity;
import com.blakebr0.mysticalautomation.tileentity.EnchanternatorTileEntity;
import com.blakebr0.mysticalautomation.tileentity.FarmerTileEntity;
import com.blakebr0.mysticalautomation.tileentity.FertilizerTileEntity;
import com.blakebr0.mysticalautomation.tileentity.InfuserTileEntity;
import com.blakebr0.mysticalautomation.tileentity.InfusionAltarnatorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class ModTileEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MysticalAutomation.MOD_ID);

    public static final RegistryObject<BlockEntityType<InfuserTileEntity>> INFUSER = register("infuser", InfuserTileEntity::new, () -> new Block[] { ModBlocks.INFUSER.get() });
    public static final RegistryObject<BlockEntityType<CrafterTileEntity>> CRAFTER = register("crafter", CrafterTileEntity::new, () -> new Block[] { ModBlocks.CRAFTER.get() });
    public static final RegistryObject<BlockEntityType<FarmerTileEntity>> FARMER = register("farmer", FarmerTileEntity::new, () -> new Block[] { ModBlocks.FARMER.get() });
    public static final RegistryObject<BlockEntityType<FertilizerTileEntity>> FERTILIZER = register("fertilizer", FertilizerTileEntity::new, () -> new Block[] { ModBlocks.FERTILIZER.get() });
    public static final RegistryObject<BlockEntityType<InfusionAltarnatorTileEntity>> INFUSION_ALTARNATOR = register("infusion_altarnator", InfusionAltarnatorTileEntity::new, () -> new Block[] { ModBlocks.INFUSION_ALTARNATOR.get() });
    public static final RegistryObject<BlockEntityType<AwakeningAltarnatorTileEntity>> AWAKENING_ALTARNATOR = register("awakening_altarnator", AwakeningAltarnatorTileEntity::new,  () -> new Block[] { ModBlocks.AWAKENING_ALTARNATOR.get() });
    public static final RegistryObject<BlockEntityType<EnchanternatorTileEntity>> ENCHANTERNATOR = register("enchanternator", EnchanternatorTileEntity::new, () -> new Block[] { ModBlocks.ENCHANTERNATOR.get() });

    @SuppressWarnings("DataFlowIssue")
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> tile, Supplier<Block[]> blocks) {
        return REGISTRY.register(name, () -> BlockEntityType.Builder.of(tile, blocks.get()).build(null));
    }
}
