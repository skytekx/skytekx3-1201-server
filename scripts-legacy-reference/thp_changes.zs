/*							===	thp_changes ===							*/						

#priority 10
// Imports
import loottweaker.LootTweaker;
import loottweaker.vanilla.loot.LootTable;
import loottweaker.vanilla.loot.LootPool;
import crafttweaker.oredict.IOreDict;
import crafttweaker.oredict.IOreDictEntry;

// Many recipes are removed through UniDict.cfg

// Vanilla Bookshelf compact with Chisel/Quark
<minecraft:bookshelf>.displayName = "Oak Bookshelf";
recipes.remove(<minecraft:bookshelf>);
recipes.addShaped(<minecraft:bookshelf>,
										[[<minecraft:oak_stairs>, null, <minecraft:oak_stairs>],
										[<minecraft:book>, <minecraft:book>, <minecraft:book>],
										[<minecraft:oak_stairs>, null, <minecraft:oak_stairs>]]);

// Improve drops from cows
val cowTable = LootTweaker.getTable("minecraft:entities/cow");
val cowPool = cowTable.getPool("main");
cowPool.setRolls(2, 2);

// Modify Bound Leather recipe
recipes.remove(<backpack:bound_leather>);
recipes.addShaped(<backpack:bound_leather>,
											[[null, <minecraft:string>, null],
											[<minecraft:leather>, <minecraft:string>, <minecraft:leather>],
											[null, <minecraft:string>, null]]);
											
// Modify Mega Torch recipe
recipes.remove(<torchmaster:mega_torch>);
recipes.addShaped(<torchmaster:mega_torch>,
											[[<minecraft:torch>, <minecraft:torch>, <minecraft:torch>],
											[<minecraft:diamond_block>, oreDict.logWood, <minecraft:diamond_block>],
											[<minecraft:gold_block>, oreDict.logWood, <minecraft:gold_block>]]);

// Add JEI item descriptions
mods.jei.JEI.addDescription(<cqrepoured:scale_turtle>, "Can be used for repairing, and trading in some Taverns");
mods.jei.JEI.addDescription(<cqrepoured:leather_bull>, "Can be used for repairing, and trading in some Taverns");
mods.jei.JEI.addDescription(<cqrepoured:ball_slime>, "Can be used for repairing, and trading in some Taverns");
mods.jei.JEI.addDescription(<cqrepoured:leather_spider>, "Can be used for repairing, and trading in some Taverns");
mods.jei.JEI.addDescription(<cqrepoured:king_crown>, "Can be attached to helmets");
mods.jei.JEI.addDescription(<cqrepoured:magic_bell>, "Sold by a strange looking Enderman found in some Taverns");

// Rename Builder's Wands
<betterbuilderswands:wandstone>.displayName = "Stone Builder's Wand";
<betterbuilderswands:wandiron>.displayName = "Iron Builder's Wand";
<betterbuilderswands:wanddiamond>.displayName = "Diamond Builder's Wand";

// Fix broken names

// For new players
recipes.addShapeless(<hexxitgear:hexical_essence>, [<hexxitgear:hexbiscus>]);

// Add Cyclops Eye info
mods.jei.JEI.addDescription(<cyclopstek:cyclops_eye>, "The cyclops lives on a small, remote island in the middle of the ocean");

// Modify Cloud Boots recipe
recipes.remove(<cqrepoured:boots_cloud>);
recipes.addShaped(<cqrepoured:boots_cloud>,
											[[null, null, null],
											[<cqrepoured:feather_golden>, <hexxitgear:hexical_diamond>, <cqrepoured:feather_golden>],
											[<minecraft:diamond>, null, <minecraft:diamond>]]);
											
// Lower arrow drops
val mobsPassiveTable = LootTweaker.getTable("hexxitworld:field_drops/mobs_passive");
val mobsHostileTable = LootTweaker.getTable("hexxitworld:field_drops/mobs_hostile");
val mobsPassivePool = mobsPassiveTable.getPool("main");
val mobsHostilePool = mobsHostileTable.getPool("main");
mobsPassivePool.removeEntry("minecraft:arrow");
mobsHostilePool.removeEntry("arrow_5");

// Add Lunar Water info
mods.jei.JEI.addDescription(<nyx:lunar_water_bottle>, "If you fill a cauldron with water and let it sit exposed to the night sky for a certain time, it will eventually bubble with white particles. Throw in a Lapis Lazuli and see what happens...");
											
// Add CQR bullet recipes
recipes.addShapeless(<cqrepoured:bullet_iron> * 4, [<minecraft:iron_nugget>, <minecraft:iron_nugget>, <minecraft:iron_nugget>, <minecraft:iron_nugget>]);
recipes.addShapeless(<cqrepoured:bullet_gold> * 4, [<minecraft:gold_nugget>, <minecraft:gold_nugget>, <minecraft:gold_nugget>, <minecraft:gold_nugget>]);
recipes.addShapeless(<cqrepoured:bullet_diamond> * 4, [<thermalfoundation:material:16>, <thermalfoundation:material:16>, <thermalfoundation:material:16>, <thermalfoundation:material:16>]);
recipes.addShaped(<cqrepoured:bullet_fire> * 8,
											[[<cqrepoured:bullet_diamond>, <cqrepoured:bullet_diamond>, <cqrepoured:bullet_diamond>],
											[<cqrepoured:bullet_diamond>, <minecraft:blaze_powder>, <cqrepoured:bullet_diamond>],
											[<cqrepoured:bullet_diamond>, <cqrepoured:bullet_diamond>, <cqrepoured:bullet_diamond>]]);
											
// Modify Golden Feather recipe
recipes.remove(<cqrepoured:feather_golden>);
recipes.addShaped(<cqrepoured:feather_golden>,
											[[<minecraft:gold_ingot>, <minecraft:gold_ingot>, <minecraft:gold_ingot>],
											[<minecraft:gold_ingot>, <minecraft:feather>, <minecraft:gold_ingot>],
											[<minecraft:gold_ingot>, <minecraft:gold_ingot>, <minecraft:gold_ingot>]]);
											
// Modify Hexical Diamond recipe
recipes.remove(<hexxitgear:hexical_diamond>);
recipes.addShaped(<hexxitgear:hexical_diamond>,
											[[null, <nyx:fallen_star>, null],
											[<hexxitgear:hexical_essence>, <minecraft:diamond>, <hexxitgear:hexical_essence>],
											[null, <hexxitgear:hexical_essence>, null]]);

// Fix Asgard Shield Reloaded ore dictionary compatibility
recipes.remove(<asr:diamond_giant_sword>);
recipes.addShaped(<asr:diamond_giant_sword>,
											[[null, <minecraft:diamond>, <minecraft:diamond>],
											[<minecraft:diamond>, <minecraft:diamond>, <minecraft:diamond>],
											[oreDict.stickWood, <minecraft:diamond>, null]]);
											
recipes.remove(<asr:golden_giant_sword>);
recipes.addShaped(<asr:golden_giant_sword>,
											[[null, <minecraft:gold_ingot>, <minecraft:gold_ingot>],
											[<minecraft:gold_ingot>, <minecraft:gold_ingot>, <minecraft:gold_ingot>],
											[oreDict.stickWood, <minecraft:gold_ingot>, null]]);
recipes.remove(<asr:iron_giant_sword>);
recipes.addShaped(<asr:iron_giant_sword>,
											[[null, <minecraft:iron_ingot>, <minecraft:iron_ingot>],
											[<minecraft:iron_ingot>, <minecraft:iron_ingot>, <minecraft:iron_ingot>],
											[oreDict.stickWood, <minecraft:iron_ingot>, null]]);
recipes.remove(<asr:stone_giant_sword>);
recipes.addShaped(<asr:stone_giant_sword>,
											[[null, oreDict.materialStoneTool, oreDict.materialStoneTool],
											[oreDict.materialStoneTool, oreDict.materialStoneTool, oreDict.materialStoneTool],
											[oreDict.stickWood, oreDict.materialStoneTool, null]]);
recipes.remove(<asr:wooden_giant_sword>);
recipes.addShaped(<asr:wooden_giant_sword>,
											[[null, oreDict.plankWood, oreDict.plankWood],
											[oreDict.plankWood, oreDict.plankWood, oreDict.plankWood],
											[oreDict.stickWood, oreDict.plankWood, null]]);
											
// Fix Quark stone tools stick ore dictionary compatibility
recipes.remove(<minecraft:stone_sword>);
recipes.addShaped(<minecraft:stone_sword>,
											[[null, oreDict.materialStoneTool, null],
											[null, oreDict.materialStoneTool, null],
											[null, oreDict.stickWood, null]]);
recipes.remove(<minecraft:stone_shovel>);
recipes.addShaped(<minecraft:stone_shovel>,
											[[null, oreDict.materialStoneTool, null],
											[null, oreDict.stickWood, null],
											[null, oreDict.stickWood, null]]);
recipes.remove(<minecraft:stone_pickaxe>);
recipes.addShaped(<minecraft:stone_pickaxe>,
											[[oreDict.materialStoneTool, oreDict.materialStoneTool, oreDict.materialStoneTool],
											[null, oreDict.stickWood, null],
											[null, oreDict.stickWood, null]]);
recipes.remove(<minecraft:stone_axe>);
recipes.addShaped(<minecraft:stone_axe>,
											[[oreDict.materialStoneTool, oreDict.materialStoneTool, null],
											[oreDict.materialStoneTool, oreDict.stickWood, null],
											[null, oreDict.stickWood, null]]);
recipes.remove(<minecraft:stone_hoe>);
recipes.addShaped(<minecraft:stone_hoe>,
											[[oreDict.materialStoneTool, oreDict.materialStoneTool, null],
											[null, oreDict.stickWood, null],
											[null, oreDict.stickWood, null]]);

// Add Bird's Nest tooltip
<birdsnests:birdsnest>.addTooltip(format.gray("Right click to examine"));

// Nerf Rope coil recipe
recipes.remove(<quark:rope>);
recipes.addShaped(<quark:rope> * 4,
									[[<minecraft:string>, <minecraft:string>, <minecraft:string>],
									[<minecraft:string>, null, <minecraft:string>],
									[<minecraft:string>, <minecraft:string>, <minecraft:string>]]);
									
// Remove duplicate recipes due to Quark
recipes.removeByRecipeName("minecraft:orange_wool");
recipes.removeByRecipeName("minecraft:magenta_wool");
recipes.removeByRecipeName("minecraft:light_blue_wool");
recipes.removeByRecipeName("minecraft:yellow_wool");
recipes.removeByRecipeName("minecraft:lime_wool");
recipes.removeByRecipeName("minecraft:pink_wool");
recipes.removeByRecipeName("minecraft:gray_wool");
recipes.removeByRecipeName("minecraft:light_gray_wool");
recipes.removeByRecipeName("minecraft:cyan_wool");
recipes.removeByRecipeName("minecraft:purple_wool");
recipes.removeByRecipeName("minecraft:blue_wool");
recipes.removeByRecipeName("minecraft:brown_wool");
recipes.removeByRecipeName("minecraft:green_wool");
recipes.removeByRecipeName("minecraft:red_wool");
recipes.removeByRecipeName("minecraft:black_wool");
recipes.removeByRecipeName("minecraft:purple_shulker_box");