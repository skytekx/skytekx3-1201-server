/*							===	loot\thp_vanilla_chests ===							*/						
					

// Imports
import loottweaker.LootTweaker;
import loottweaker.vanilla.loot.LootTable;

// Populate vanilla loot tables
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/simple_dungeon"), 2, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/abandoned_mineshaft"), 1, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/jungle_temple"), 2, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/desert_pyramid"), 2, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/village_blacksmith"), 2, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/end_city_treasure"), 3, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/stronghold_library"), 2, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/stronghold_crossing"), 1, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/stronghold_corridor"), 1, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/woodland_mansion"), 3, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/nether_bridge"), 2, false, false);
scripts.loot.thp.populate(LootTweaker.getTable("minecraft:chests/igloo_chest"), 2, false, false);

val simpleDungeon = LootTweaker.getTable("minecraft:chests/simple_dungeon");
val abandonedMineshaft = LootTweaker.getTable("minecraft:chests/abandoned_mineshaft");
