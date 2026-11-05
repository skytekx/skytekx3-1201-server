package com.blakebr0.mysticalautomation.init;

import com.blakebr0.cucumber.item.BaseBlockItem;
import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.block.AwakeningAltarnatorBlock;
import com.blakebr0.mysticalautomation.block.CrafterBlock;
import com.blakebr0.mysticalautomation.block.EnchanternatorBlock;
import com.blakebr0.mysticalautomation.block.FarmerBlock;
import com.blakebr0.mysticalautomation.block.FertilizerBlock;
import com.blakebr0.mysticalautomation.block.InfuserBlock;
import com.blakebr0.mysticalautomation.block.InfusionAltarnatorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MysticalAutomation.MOD_ID);
    public static final Map<String, Supplier<BlockItem>> BLOCK_ITEMS = new LinkedHashMap<>();

    public static final RegistryObject<Block> INFUSER = register("infuser", InfuserBlock::new);
    public static final RegistryObject<Block> CRAFTER = register("crafter", CrafterBlock::new);
    public static final RegistryObject<Block> FARMER = register("farmer", FarmerBlock::new);
    public static final RegistryObject<Block> FERTILIZER = register("fertilizer", FertilizerBlock::new);
    public static final RegistryObject<Block> INFUSION_ALTARNATOR = register("infusion_altarnator", InfusionAltarnatorBlock::new);
    public static final RegistryObject<Block> AWAKENING_ALTARNATOR = register("awakening_altarnator", AwakeningAltarnatorBlock::new);
    public static final RegistryObject<Block> ENCHANTERNATOR = register("enchanternator", EnchanternatorBlock::new);

    private static RegistryObject<Block> register(String name, Supplier<Block> block) {
        return register(name, block, b -> () -> new BaseBlockItem(b.get()));
    }

    private static RegistryObject<Block> register(String name, Supplier<Block> block, Function<RegistryObject<Block>, Supplier<? extends BlockItem>> item) {
        var reg = REGISTRY.register(name, block);
        BLOCK_ITEMS.put(name, () -> item.apply(reg).get());
        return reg;
    }
}
