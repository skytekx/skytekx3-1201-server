package ianm1647.expandeddelight.common.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * The common tags of the mod. Upstream uses the c: tags of 1.21.1, this port the forge: tags of 1.20.1 under
 * the names that Farmer's Delight 1.20.1 and Pam's HarvestCraft 2 use where they have one. The data files in
 * src/generated/resources follow the same table, see UPSTREAM.md. No code reads these keys, upstream's
 * datagen did.
 */
public class EDCommonTags {
    //ores
    public static final TagKey<Item> ORES_SALT = itemTag("ores/salt");

    //dusts
    public static final TagKey<Item> DUSTS_SALT = itemTag("dusts/salt");
    public static final TagKey<Item> DUSTS_CINNAMON = itemTag("dusts/cinnamon");

    //crops
    public static final TagKey<Item> CROPS_ASPARAGUS = itemTag("crops/asparagus");
    public static final TagKey<Item> CROPS_SWEET_POTATO = itemTag("crops/sweet_potato");
    public static final TagKey<Item> CROPS_CHILI_PEPPER = itemTag("crops/chili_pepper");
    public static final TagKey<Item> CROPS_PEANUT = itemTag("crops/peanut");

    //foods (c:foods/<crop> becomes forge:vegetables/<crop> as in Farmer's Delight, the peanut forge:nuts/peanut)
    public static final TagKey<Item> FOODS_ASPARAGUS = itemTag("vegetables/asparagus");
    public static final TagKey<Item> FOODS_SWEET_POTATO = itemTag("vegetables/sweet_potato");
    public static final TagKey<Item> FOODS_CHILI_PEPPER = itemTag("vegetables/chili_pepper");
    public static final TagKey<Item> FOODS_PEANUT = itemTag("nuts/peanut");
    public static final TagKey<Item> FOODS_CHEESE = itemTag("cheese");
    public static final TagKey<Item> FOODS_GOAT_CHEESE = itemTag("goat_cheese");

    //jams
    public static final TagKey<Item> JAMS = itemTag("jams");
    public static final TagKey<Item> JAMS_SWEET_BERRY = itemTag("jams/sweet_berry");
    public static final TagKey<Item> JAMS_GLOW_BERRY = itemTag("jams/glow_berry");
    public static final TagKey<Item> JAMS_CRANBERRY = itemTag("jams/cranberry");

    //juices
    public static final TagKey<Item> JUICES = itemTag("juices");
    public static final TagKey<Item> JUICES_APPLE = itemTag("juices/apple");
    public static final TagKey<Item> JUICES_SWEET_BERRY = itemTag("juices/sweet_berry");
    public static final TagKey<Item> JUICES_GLOW_BERRY = itemTag("juices/glow_berry");
    public static final TagKey<Item> JUICES_CRANBERRY = itemTag("juices/cranberry");

    public EDCommonTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }
}
