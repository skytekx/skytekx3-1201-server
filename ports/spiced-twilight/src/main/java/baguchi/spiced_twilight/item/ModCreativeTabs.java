package baguchi.spiced_twilight.item;

import baguchi.spiced_twilight.SpicedTwilight;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.init.TFCreativeTabs;

import java.util.stream.Stream;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpicedTwilight.MODID);

    public static final RegistryObject<CreativeModeTab> SPICED_TWILIGHT = CREATIVE_MODE_TABS.register("spiced_twilight", () -> CreativeModeTab.builder()
            .withTabsBefore(TFCreativeTabs.ITEMS.getKey())
            .title(Component.translatable("itemGroup." + SpicedTwilight.MODID))
            .icon(() -> ModItems.FIRE_BEETLE_POWDER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.acceptAll(Stream.of(
                        ModItems.FIRE_BEETLE_POWDER,
                        ModItems.HYDRA_HIDE,
                        ModItems.CINDER_POUCH,
                        ModItems.MAZE_SLIME_BEETLE_SPAWN_EGG
                ).map(sup -> {
                    return sup.get().getDefaultInstance();
                }).toList());
            }).build());
}
