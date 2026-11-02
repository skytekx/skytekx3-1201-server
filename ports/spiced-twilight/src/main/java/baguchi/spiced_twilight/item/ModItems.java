package baguchi.spiced_twilight.item;

import baguchi.spiced_twilight.SpicedTwilight;
import baguchi.spiced_twilight.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SpicedTwilight.MODID);

    public static final RegistryObject<Item> MAZE_SLIME_BEETLE_SPAWN_EGG = ITEMS.register("maze_slime_beetle_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.MAZE_SLIME_BEETLE, 10724259, 2767639, new Item.Properties()));
    // 1.21 puts the description in the item's LORE data component
    public static final RegistryObject<Item> FIRE_BEETLE_POWDER = ITEMS.register("fire_beetle_powder", () -> new LoreItem(new Item.Properties(), "item.spiced_twilight.fire_beetle_powder.desc"));
    public static final RegistryObject<Item> HYDRA_HIDE = ITEMS.register("hydra_hide", () -> new Item(new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CINDER_POUCH = ITEMS.register("cinder_pouch", () -> new CinderPouchItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.UNCOMMON)));
}
