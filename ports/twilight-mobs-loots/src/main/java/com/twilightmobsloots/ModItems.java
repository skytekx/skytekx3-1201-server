package com.twilightmobsloots;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Item registry: one {@code <mob>_trophy} item per {@link MobHead} (Twilight Forest already has its boss
 * trophies). Fully data driven: a new {@link MobHead} constant registers its item and both blocks.
 * Drops are injected by {@link TFLootInjection} (10%, boosted by Looting).
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TwilightMobsLoots.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TwilightMobsLoots.MOD_ID);

    /** Trophy items in enum order, stack size 64 like ordinary block items. */
    private static final EnumMap<MobHead, RegistryObject<MobTrophyItem>> TROPHY_ITEMS = new EnumMap<>(MobHead.class);

    static {
        for (MobHead head : MobHead.values()) {
            TROPHY_ITEMS.put(head, ITEMS.register(head.getSerializedName() + "_trophy",
                    () -> new MobTrophyItem(head, new Item.Properties().stacksTo(64),
                            ModBlocks.floor(head).get(), ModBlocks.wall(head).get())));
        }
    }

    /** The trophy item of a mob. */
    public static RegistryObject<MobTrophyItem> trophy(MobHead head) {
        return TROPHY_ITEMS.get(head);
    }

    /** All trophy items in enum order (client renderers, Curios, creative tab). */
    public static final List<RegistryObject<MobTrophyItem>> TROPHIES =
            List.copyOf(new ArrayList<>(TROPHY_ITEMS.values()));

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + TwilightMobsLoots.MOD_ID))
                    .icon(() -> new ItemStack(trophy(MobHead.REDCAP).get()))
                    .displayItems((parameters, output) -> ITEMS.getEntries().forEach(holder -> output.accept(holder.get())))
                    .build());
}
