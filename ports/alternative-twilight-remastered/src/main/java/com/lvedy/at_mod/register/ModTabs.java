package com.lvedy.at_mod.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.lvedy.at_mod.AlternativeTwilight.MODID;

public class ModTabs {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "at_mod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // The tab keeps the upstream id "at_mod:example_tab"; it is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.at_mod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItem.WOODEN_SPEAR.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItem.WOODEN_SPEAR.get());
                output.accept(ModItem.STONE_SPEAR.get());
                output.accept(ModItem.COPPER_SPEAR.get());
                output.accept(ModItem.IRON_SPEAR.get());
                output.accept(ModItem.GOLDEN_SPEAR.get());
                output.accept(ModItem.DIAMOND_SPEAR.get());
                output.accept(ModItem.NETHERITE_SPEAR.get());
                output.accept(ModItem.IRONWOOD_SPEAR.get());
                output.accept(ModItem.KNIGHT_SPEAR.get());
            }).build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
