package com.enderio.machines.common.tag;

import com.enderio.EnderIO;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class MachineTags {

    public static void register() {
        EntityTypes.init();
        Items.init();
    }

    public static class EntityTypes {

        private static void init() {}

        public static TagKey<EntityType<?>> SPAWNER_BLACKLIST = create("spawner_blacklist");

        private static TagKey<EntityType<?>> create(String pName) {
            return TagKey.create(Registries.ENTITY_TYPE, EnderIO.loc(pName));
        }
    }

    // The reagents of the Vat recipes (6bc3ec343).
    public static class Items {

        private static void init() {}

        public static final TagKey<Item> SEEDS = create("seeds");
        public static final TagKey<Item> CROPS = create("crops");
        public static final TagKey<Item> CLOUD_COLD = create("cloud_cold");
        public static final TagKey<Item> EXPLOSIVES = create("explosives");
        public static final TagKey<Item> NATURAL_LIGHTS = create("natural_lights");
        public static final TagKey<Item> SUNFLOWER = create("sunflower");
        public static final TagKey<Item> MEAT = create("meat");
        public static final TagKey<Item> PRISMARINE = create("prismarine");
        public static final TagKey<Item> BLAZE_POWDER = create("blaze_powder");
        public static final TagKey<Item> LIGHTNING_ROD = create("lightning_rod");
        public static final TagKey<Item> WIND_CHARGES = create("wind_charges");

        private static TagKey<Item> create(String name) {
            return ItemTags.create(EnderIO.loc(name));
        }
    }
}
