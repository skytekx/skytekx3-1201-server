/*							===	loot\thp_modded_chests ===							*/						
					

// Imports
import loottweaker.LootTweaker;
import loottweaker.vanilla.loot.LootTable;

// Remove old Bird's Nests loot
val birdsNests = LootTweaker.getTable("birdsnests:nest_loot");
birdsNests.removePool("nest_loot");

// Remove excessive Elementary Staffs loot
val cqrFood = LootTweaker.getTable("cqrepoured:chests/food");
val cqrEquipment = LootTweaker.getTable("cqrepoured:chests/equipment");
val quarkPirates = LootTweaker.getTable("quark:chests/pirate_chest");
cqrFood.removePool("loot");
cqrEquipment.removePool("loot");
quarkPirates.removePool("loot");

// Populate modded loot tables
scripts.loot.thp.populate(birdsNests, 1, true, true);
scripts.loot.thp.populate(LootTweaker.getTable("quark:chests/pirate_chest"), 2, false, false);