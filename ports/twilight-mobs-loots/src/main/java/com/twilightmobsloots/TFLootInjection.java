package com.twilightmobsloots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Injects the trophy drops into the Twilight Forest entity loot tables.
 *
 * <p>Every loot table with the id {@code twilightforest:entities/<mob>} of a {@link MobHead} gets one extra
 * pool:
 * <ul>
 *     <li>base chance <b>10%</b> (Armored Giant and Giant Miner <b>50%</b>);</li>
 *     <li><b>+5% per Looting level</b> ({@code randomChanceAndLootingBoost});</li>
 *     <li>only when killed by a player.</li>
 * </ul>
 * The Bighorn Sheep's 16 per-color sub tables ({@code entities/bighorn_sheep/<color>}) map to the same trophy.
 */
public class TFLootInjection {

    /** Base drop chance (10%). */
    private static final float BASE_CHANCE = 0.10f;

    /** Extra chance per Looting level (5%). */
    private static final float PER_LOOTING_LEVEL_BOOST = 0.05f;

    /** Forge requires unique pool names per table. */
    private static final String POOL_NAME = TwilightMobsLoots.MOD_ID + ":trophy";

    /** Twilight Forest entity loot table id -> trophy item. */
    private static final Map<ResourceLocation, RegistryObject<MobTrophyItem>> ENTITY_LOOT = new HashMap<>();

    /** Drop chance overrides (default {@link #BASE_CHANCE}). */
    private static final Map<ResourceLocation, Float> LOOT_CHANCE = new HashMap<>();

    static {
        for (MobHead head : MobHead.values()) {
            put(head.getSerializedName(), ModItems.trophy(head));
        }
        for (String color : List.of("black", "blue", "brown", "cyan", "gray", "green", "light_blue",
                "light_gray", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow")) {
            put("bighorn_sheep/" + color, ModItems.trophy(MobHead.BIGHORN_SHEEP));
        }
        setChance(MobHead.ARMORED_GIANT, 0.50f);
        setChance(MobHead.GIANT_MINER, 0.50f);
    }

    /** Called from the mod constructor: listens on the game event bus. */
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(TFLootInjection::onLootTableLoad);
    }

    private static void put(String entityPath, RegistryObject<MobTrophyItem> item) {
        ENTITY_LOOT.put(new ResourceLocation("twilightforest", "entities/" + entityPath), item);
    }

    private static void setChance(MobHead head, float chance) {
        LOOT_CHANCE.put(new ResourceLocation("twilightforest", "entities/" + head.getSerializedName()), chance);
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        RegistryObject<MobTrophyItem> loot = ENTITY_LOOT.get(event.getName());
        if (loot == null) {
            return;
        }

        float chance = LOOT_CHANCE.getOrDefault(event.getName(), BASE_CHANCE);

        event.getTable().addPool(
                LootPool.lootPool()
                        .name(POOL_NAME)
                        .add(LootItem.lootTableItem(loot.get())
                                .setWeight(1))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(
                                chance, PER_LOOTING_LEVEL_BOOST))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .build());
    }
}
