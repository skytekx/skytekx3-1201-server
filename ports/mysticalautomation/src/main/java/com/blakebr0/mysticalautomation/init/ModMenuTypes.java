package com.blakebr0.mysticalautomation.init;

import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.container.AwakeningAltarnatorContainer;
import com.blakebr0.mysticalautomation.container.CrafterContainer;
import com.blakebr0.mysticalautomation.container.EnchanternatorContainer;
import com.blakebr0.mysticalautomation.container.FarmerContainer;
import com.blakebr0.mysticalautomation.container.FertilizerContainer;
import com.blakebr0.mysticalautomation.container.InfuserContainer;
import com.blakebr0.mysticalautomation.container.InfusionAltarnatorContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MysticalAutomation.MOD_ID);

    public static final RegistryObject<MenuType<InfuserContainer>> INFUSER = REGISTRY.register("infuser", () -> IForgeMenuType.create(InfuserContainer::new));
    public static final RegistryObject<MenuType<CrafterContainer>> CRAFTER = REGISTRY.register("crafter", () -> IForgeMenuType.create(CrafterContainer::new));
    public static final RegistryObject<MenuType<FarmerContainer>> FARMER = REGISTRY.register("farmer", () -> IForgeMenuType.create(FarmerContainer::new));
    public static final RegistryObject<MenuType<FertilizerContainer>> FERTILIZER = REGISTRY.register("fertilizer", () -> IForgeMenuType.create(FertilizerContainer::new));
    public static final RegistryObject<MenuType<InfusionAltarnatorContainer>> INFUSION_ALTARNATOR = REGISTRY.register("infusion_altarnator", () -> IForgeMenuType.create(InfusionAltarnatorContainer::new));
    public static final RegistryObject<MenuType<AwakeningAltarnatorContainer>> AWAKENING_ALTARNATOR = REGISTRY.register("awakening_altarnator", () -> IForgeMenuType.create(AwakeningAltarnatorContainer::new));
    public static final RegistryObject<MenuType<EnchanternatorContainer>> ENCHANTERNATOR = REGISTRY.register("enchanternator", () -> IForgeMenuType.create(EnchanternatorContainer::new));
}
