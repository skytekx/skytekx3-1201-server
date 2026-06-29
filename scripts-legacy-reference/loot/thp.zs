/*							===	loot\thp ===							*/						
					
#priority 10

// Imports
import loottweaker.LootTweaker;
import loottweaker.vanilla.loot.LootTable;
import loottweaker.vanilla.loot.LootPool;
import loottweaker.vanilla.loot.Functions;

/*
	Common chest:
		2-3 common items (2-4 extra mode, 1-1 single mode)
		0-2 uncommon items (0-1 single mode)
			total: 2-5 items (2-6 extra mode, 1-2 single mode)
	Uncommon chest:
		1-2 common items (1-3 extra mode)
		2-3 uncommon items (2-4 extra mode)
		0-1 rare items
			total: 3-6 items (3-8 extra mode)
	Rare chest:
		1-2 common items (1-3 extra mode)
		1-2 uncommon items (1-3 extra mode)
		2-3 rare items
			total: 4-7 items (4-9 extra mode)
*/

function populate(lootTable as LootTable, tier as int, extra as bool, single as bool){
	// Configure common tier rolls
	var thp1 as LootPool;
	if (tier == 1) {
		if (!single) {
			thp1 = lootTable.addPool("thp1", 2, !extra ? 3 : 4, 0, 0);
		} else {
			thp1 = lootTable.addPool("thp1", 1, 1, 0, 0);
		}
	} else {
		thp1 = lootTable.addPool("thp1", 1, !extra ? 2 : 3, 0, 0);
	}
	// Add common tier items
	thp1.addItemEntry(<minecraft:apple>, 40, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<tconstruct:nuggets:2>, 40, 1, [Functions.setCount(2, 5)], []);
	thp1.addItemEntry(<tconstruct:nuggets:4>, 40, 1, [Functions.setCount(2, 5)], []);
	thp1.addItemEntry(<tconstruct:slime_boots:2>, 12, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<quark:trowel>, 10, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<spartanshields:shield_basic_wood>, 6, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:arrow>, 40, 1, [Functions.setCount(2, 8)], []);
	thp1.addItemEntry(<thermalfoundation:material:129>, 40, 1, [Functions.setCount(1, 3)], []);
	thp1.addItemEntry(<thermalfoundation:material:128>, 40, 1, [Functions.setCount(1, 3)], []);
	thp1.addItemEntry(<minecraft:string>, 40, 1, [Functions.setCount(3, 9)], []);
	thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 38 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 30 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 29 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 26 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:pumpkin_pie>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<minecraft:golden_carrot>, 40, 1, [Functions.setCount(2, 6)], []);
	thp1.addItemEntry(<minecraft:cookie>, 40, 1, [Functions.setCount(3, 12)], []);
	thp1.addItemEntry(<minecraft:cake>, 40, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<quark:variant_sapling:1>, 40, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:experience_bottle>, 40, 1, [Functions.setCount(2, 7)], []);
	thp1.addItemEntry(<chisel:chisel_iron>, 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<cqrepoured:bullet_iron>, 40, 1, [Functions.setCount(3, 7)], []);
	thp1.addItemEntry(<cqrepoured:bullet_gold>, 40, 1, [Functions.setCount(4, 9)], []);
	thp1.addItemEntry(<cqrepoured:dagger_iron>, 40, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<mocreatures:sharksword>, 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<mocreatures:sharkteeth>, 40, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<thermalfoundation:material:132>, 40, 1, [Functions.setCount(1, 3)], []);
	thp1.addItemEntry(<cqrepoured:shield_pigman>, 2, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<cqrepoured:shield_mummy>, 2, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<cqrepoured:shield_goblin>, 2, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<comforts:rope>, 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<cqrepoured:shield_zombie>, 2, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<betterbuilderswands:wandstone>, 10, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<xreliquary:bullet:3>, 40, 1, [Functions.setCount(3, 7)], []);
	thp1.addItemEntry(<xreliquary:bullet:5>, 40, 1, [Functions.setCount(3, 7)], []);
	thp1.addItemEntry(<skewers:skewer_cookie>, 40, 1, [Functions.setCount(1, 3)], []);
	thp1.addItemEntry(<skewers:skewer_caramel_apple>, 40, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<skewers:skewer_cod_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_salmon_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_veggie_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_chicken_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_rabbit_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_mutton_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_beef_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<skewers:skewer_porkchop_raw>, 40, 1, [Functions.setCount(1, 2)], []);
	thp1.addItemEntry(<natura:sticks>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:1>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:2>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:2>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:3>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:4>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:5>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:6>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:7>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<natura:sticks:8>, 40, 1, [Functions.setCount(2, 17)], []);
	thp1.addItemEntry(<openblocks:path>, 40, 1, [Functions.setCount(3, 13)], []);
	thp1.addItemEntry(<openblocks:generic>, 30, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<minecraft:torch>, 50, 1, [Functions.setCount(12, 24)], []);
	thp1.addItemEntry(<minecraft:cobblestone>, 50, 1, [Functions.setCount(64, 64)], []);
	thp1.addItemEntry(<minecraft:dirt>, 50, 1, [Functions.setCount(64, 64)], []);
	thp1.addItemEntry(<minecraft:sand>, 50, 1, [Functions.setCount(64, 64)], []);
	thp1.addItemEntry(<minecraft:dye:0>, 50, 1, [Functions.setCount(2, 6)], []);
	thp1.addItemEntry(<morebows:stone_bow>, 20, 1, [Functions.setCount(1, 1)], []);
	thp1.addItemEntry(<morebows:iron_bow>, 20, 1, [Functions.setCount(1, 1)], []);
	if (extra) {
		thp1.addItemEntry(<minecraft:clay_ball>, 40, 1, [Functions.setCount(3, 8)], []);
		thp1.addItemEntry(<minecraft:redstone>, 40, 1, [Functions.setCount(3, 12)], []);
		thp1.addItemEntry(<minecraft:iron_ingot>, 40, 1, [Functions.setCount(3, 7)], []);
		thp1.addItemEntry(<minecraft:coal>, 40, 1, [Functions.setCount(2, 9)], []);
		thp1.addItemEntry(<minecraft:stick>, 40, 1, [Functions.setCount(3, 12)], []);
		thp1.addItemEntry(<minecraft:log:1>, 40, 1, [Functions.setCount(4, 12)], []);
		thp1.addItemEntry(<minecraft:log>, 40, 1, [Functions.setCount(4, 12)], []);
		thp1.addItemEntry(<minecraft:iron_pickaxe>, 20, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:iron_sword>, 20, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<asr:stone_giant_sword>, 20, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<traverse:orange_autumnal_sapling>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<traverse:yellow_autumnal_sapling>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:wheat_seeds>, 40, 1, [Functions.setCount(1, 3)], []);
		thp1.addItemEntry(<minecraft:pumpkin_seeds>, 40, 1, [Functions.setCount(1, 3)], []);
		thp1.addItemEntry(<minecraft:beetroot_seeds>, 40, 1, [Functions.setCount(1, 4)], []);
		thp1.addItemEntry(<minecraft:fermented_spider_eye>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:speckled_melon>, 40, 1, [Functions.setCount(1, 3)], []);
		thp1.addItemEntry(<backpack:stick>, 40, 1, [Functions.setCount(1, 3)], []);
		thp1.addItemEntry(<quark:colored_flowerpot_red>, 40, 1, [Functions.setCount(1, 4)], []);
		thp1.addItemEntry(<minecraft:sapling:3>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:poisonous_potato>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:potato>, 40, 1, [Functions.setCount(3, 11)], []);
		thp1.addItemEntry(<minecraft:fish>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:carrot>, 40, 1, [Functions.setCount(2, 8)], []);
		thp1.addItemEntry(<minecraft:beetroot>, 40, 1, [Functions.setCount(2, 6)], []);
		thp1.addItemEntry(<minecraft:beetroot_soup> , 40, 1, [Functions.setCount(1, 3)], []);
		thp1.addItemEntry(<minecraft:mushroom_stew>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:feather>, 40, 1, [Functions.setCount(1, 4)], []);
		thp1.addItemEntry(<minecraft:gunpowder>, 40, 1, [Functions.setCount(1, 5)], []);
		thp1.addItemEntry(<minecraft:iron_nugget>, 40, 1, [Functions.setCount(3, 12)], []);
		thp1.addItemEntry(<minecraft:gold_nugget>, 40, 1, [Functions.setCount(3, 12)], []);
		thp1.addItemEntry(<minecraft:wheat>, 40, 1, [Functions.setCount(2, 5)], []);
		thp1.addItemEntry(<minecraft:paper>, 40, 1, [Functions.setCount(2, 5)], []);
		thp1.addItemEntry(<minecraft:book>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 38 as short}]}), 20, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 25 as short}]}), 20, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<quark:colored_flowerpot_cyan>, 40, 1, [Functions.setCount(1, 4)], []);
		thp1.addItemEntry(<fairylights:pennant:4>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:light:66>, 40, 1, [Functions.setCount(4, 3)], []);
		thp1.addItemEntry(<fairylights:light:9>, 40, 1, [Functions.setCount(4, 3)], []);
		thp1.addItemEntry(<fairylights:pennant_bunting>.withTag({pattern: [{color: 11 as byte}], text: {styling: [] as byte[] as byte[], value: ""}}), 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:hanging_lights>.withTag({twinkle: 0 as byte, pattern: [{color: 13 as byte, light: 0}], tight: 0 as byte}), 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<chococraftplus:gysahl_seeds>, 40, 1, [Functions.setCount(1, 5)], []);
		thp1.addItemEntry(<natura:overworld_seeds>, 40, 1, [Functions.setCount(2, 7)], []);
		thp1.addItemEntry(<natura:overworld_seeds:1>, 40, 1, [Functions.setCount(2, 7)], []);
		thp1.addItemEntry(<microblockcbe:saw_iron>, 20, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<tconstruct:sharpening_kit>.withTag({Material: "paper"}), 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<quark:paper_lantern:1>, 40, 1, [Functions.setCount(3, 6)], []);
		thp1.addItemEntry(<quark:paper_lantern>, 40, 1, [Functions.setCount(3, 6)], []);
		thp1.addItemEntry(<quark:rope>, 40, 1, [Functions.setCount(2, 12)], []);
		thp1.addItemEntry(<quark:colored_flowerpot_purple>, 40, 1, [Functions.setCount(1, 4)], []);
		thp1.addItemEntry(<backpack:bound_leather>, 30, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<cqrepoured:spear_iron>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<mocreatures:woolball>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:egg>, 40, 1, [Functions.setCount(2, 7)], []);
		thp1.addItemEntry(<minecraft:bone>, 40, 1, [Functions.setCount(2, 11)], []);
		thp1.addItemEntry(<minecraft:slime_ball>, 40, 1, [Functions.setCount(2, 5)], []);
		thp1.addItemEntry(<minecraft:name_tag>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:lead>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:bucket>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:baked_potato>, 40, 1, [Functions.setCount(2, 5)], []);
		thp1.addItemEntry(<minecraft:porkchop>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:rotten_flesh>, 40, 1, [Functions.setCount(2, 6)], []);
		thp1.addItemEntry(<minecraft:brown_mushroom>, 40, 1, [Functions.setCount(2, 5)], []);
		thp1.addItemEntry(<minecraft:red_mushroom>, 40, 1, [Functions.setCount(2, 5)], []);
		thp1.addItemEntry(<minecraft:glass_bottle>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:bow>, 30, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:web>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:bucket>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:mossy_cobblestone>, 40, 1, [Functions.setCount(3, 12)], []);
		thp1.addItemEntry(<minecraft:carrot_on_a_stick>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:melon_block>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:pumpkin>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 50 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 21 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:obsidian>, 40, 1, [Functions.setCount(1, 4)], []);
		thp1.addItemEntry(<minecraft:leaves>, 40, 1, [Functions.setCount(7, 19)], []);
		thp1.addItemEntry(<minecraft:vine>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:cactus>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:waterlily>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<minecraft:double_plant:1>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:red_flower:8>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<minecraft:double_plant>, 40, 1, [Functions.setCount(2, 2)], []);
		thp1.addItemEntry(<minecraft:red_flower:2>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<minecraft:red_flower>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<minecraft:yellow_flower>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<minecraft:sapling:5>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:sapling:4>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:sapling:1>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:sapling>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:sapling:3>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:sapling:2>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:fish:2>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:fish:1>, 40, 1, [Functions.setCount(2, 4)], []);
		thp1.addItemEntry(<minecraft:fish>, 40, 1, [Functions.setCount(1, 1)], []);
		thp1.addItemEntry(<minecraft:melon>, 40, 1, [Functions.setCount(3, 7)], []);
		thp1.addItemEntry(<minecraft:bookshelf>, 40, 1, [Functions.setCount(1, 2)], []);
		thp1.addItemEntry(<minecraft:item_frame>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:light:28>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:light:61>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:light:84>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:light:135>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:light:209>, 40, 1, [Functions.setCount(2, 3)], []);
		thp1.addItemEntry(<fairylights:ladder>, 5, 1, [Functions.setCount(1, 1)], []);
	}
	
	// Configure uncommon tier rolls
	var thp2 as LootPool;
	if (tier == 1) {
		thp2 = lootTable.addPool("thp2", 0, !single ? 2 : 1, 0, 0);
	} else {
		if (tier == 2) {
			thp2 = lootTable.addPool("thp2", 2, !extra ? 3 : 4, 0, 0);
		} else {
			thp2 = lootTable.addPool("thp2", 1, !extra ? 2 : 3, 0, 0);
		}
	}
	// Add uncommon tier items
	thp2.addItemEntry(<tconstruct:materials:18>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<xreliquary:attraction_potion>, 40, 1, [Functions.setCount(2, 3)], []);
	thp2.addItemEntry(<xreliquary:fertile_potion>, 40, 1, [Functions.setCount(2, 3)], []);
	thp2.addItemEntry(<waystones:warp_stone>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<thermalfoundation:horse_armor_tin>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<thermalfoundation:horse_armor_bronze>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_basic_stone>, 5, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:ender_pearl>, 40, 1, [Functions.setCount(1, 4)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 37 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 30 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 29 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 28 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 26 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 9 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 70 as short}]}), 60, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:whip>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:fishnet>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<tconstruct:slimesling:2>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<thermalfoundation:material:16>, 30, 1, [Functions.setCount(4, 13)], []);
	thp2.addItemEntry(<cqrepoured:shield_monking>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_pirate>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_pirate2>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_rusted>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_skeleton_friends>, 4, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_specter>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_spider>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_sun>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_tomb>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_triton>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:shield_turtle>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<quark:glass_item_frame>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<testdummy:dummy>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<lostbooks:random_book>, 160, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<xreliquary:bullet>, 40, 1, [Functions.setCount(4, 13)], []);
	thp2.addItemEntry(<xreliquary:bullet:1>, 40, 1, [Functions.setCount(3, 9)], []);
	thp2.addItemEntry(<cqrepoured:revolver>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:musket>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:musket_dagger_iron>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:reptileplate>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:reptilelegs>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:reptileboots>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:tuskswood>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:tusksiron>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:elephantchest>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:mammothplatform>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:elephantharness>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:great_sword_iron>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:tnt>, 40, 1, [Functions.setCount(2, 6)], []);
	thp2.addItemEntry(<minecraft:golden_apple>, 40, 1, [Functions.setCount(1, 4)], []);
	thp2.addItemEntry(<backpack:tanned_leather>, 40, 1, [Functions.setCount(2, 4)], []);
	thp2.addItemEntry(<bibliocraft:tapemeasure>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<bibliocraft:biblioglasses:2>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<chococraftplus:chocobo_saddle>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<asr:iron_giant_sword>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:dagger_diamond>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:dagger_ninja>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:dagger_monking>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:sword_turtle>, 30, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:helmet_slime>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:boots_slime>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:helmet_turtle>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:boots_turtle>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:helmet_bull>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:boots_bull>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:helmet_spider>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:boots_spider>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:kittylitter>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<betterbuilderswands:wandiron>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<exchangers:exchanger_core_tier1>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<exchangers:wooden_exchanger>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:ratburger>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<mocreatures:furhelmet>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:furchest>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:furlegs>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:furboots>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:reptilehelmet>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:chestplate_iron_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:helmet_heavy_iron>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:chestplate_heavy_iron>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:leggings_heavy_iron>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:boots_heavy_iron>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:helmet_iron_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:hookshot>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:bullet_fire>, 40, 1, [Functions.setCount(3, 9)], []);
	thp2.addItemEntry(<cqrepoured:bullet_diamond>, 30, 1, [Functions.setCount(3, 10)], []);
	thp2.addItemEntry(<cqrepoured:boots_iron_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:leggings_iron_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:feather_golden>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:potion_healing>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:spiked_glove>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<cqrepoured:scale_turtle>, 25, 1, [Functions.setCount(1, 3)], []);
	thp2.addItemEntry(<cqrepoured:leather_bull>, 25, 1, [Functions.setCount(1, 3)], []);
	thp2.addItemEntry(<cqrepoured:ball_slime>, 25, 1, [Functions.setCount(1, 3)], []);
	thp2.addItemEntry(<cqrepoured:leather_spider>, 25, 1, [Functions.setCount(1, 3)], []);
	thp2.addItemEntry(<spartanshields:shield_basic_iron>, 3, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_basic_gold>, 3, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_basic_tin>, 3, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_basic_obsidian>, 2, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "cofhcore:resistance3"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:long_regeneration"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:tipped_arrow>.withTag({Potion: "cofhcore:harming3"}), 40, 1, [Functions.setCount(1, 4)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:strong_regeneration"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:long_swiftness"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:strong_swiftness"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:long_fire_resistance"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:long_strength"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:horsesaddle>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<mocreatures:silversword>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "cofhcore:healing3"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "cofhcore:haste2+"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_tower_gold>, 5, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_tower_diamond>, 3, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_tower_iron>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<openblocks:paintbrush>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<ironchest:iron_gold_chest_upgrade>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:fireworks>, 40, 1, [Functions.setCount(3, 8)], []);
	thp2.addItemEntry(<minecraft:armor_stand>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:bone_block>, 40, 1, [Functions.setCount(2, 3)], []);
	thp2.addItemEntry(<minecraft:hay_block>, 40, 1, [Functions.setCount(2, 3)], []);
	thp2.addItemEntry(<minecraft:glass>, 40, 1, [Functions.setCount(8, 28)], []);
	thp2.addItemEntry(<minecraft:tipped_arrow>.withTag({Potion: "minecraft:long_poison"}), 40, 1, [Functions.setCount(1, 5)], []);
	thp2.addItemEntry(<minecraft:tipped_arrow>.withTag({Potion: "cofhcore:levitation"}), 40, 1, [Functions.setCount(1, 5)], []);
	thp2.addItemEntry(<minecraft:tipped_arrow>.withTag({Potion: "minecraft:long_slowness"}), 40, 1, [Functions.setCount(1, 5)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:regeneration"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:night_vision"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:fire_resistance"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:sponge>, 40, 1, [Functions.setCount(1, 3)], []);
	thp2.addItemEntry(<natura:overworld_sapling>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling:1>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling:2>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling:3>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling2>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling2:1>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling2:2>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:overworld_sapling2:3>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<natura:redwood_sapling>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<quark:variant_sapling>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<mocreatures:sugarlump>, 40, 1, [Functions.setCount(2, 4)], []);
	thp2.addItemEntry(<mocreatures:haystack>, 40, 1, [Functions.setCount(1, 2)], []);
	thp2.addItemEntry(<ironchest:wood_iron_chest_upgrade>, 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_tower_wood>, 10, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<spartanshields:shield_tower_stone>, 6, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<openblocks:luggage>, 35, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:swiftness"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "cofhcore:leaping2+"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:strong_healing"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:strong_harming"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:luck"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:healing"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<minecraft:splash_potion>.withTag({Potion: "minecraft:long_water_breathing"}), 40, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<hexxitgear:refresher_shard>, 20, 1, [Functions.setCount(1, 3)], []);
	thp2.addItemEntry(<morebows:frost_bow>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<morebows:flame_bow>, 20, 1, [Functions.setCount(1, 1)], []);
	thp2.addItemEntry(<morebows:gold_bow>, 20, 1, [Functions.setCount(1, 1)], []);
	if (extra) {
		thp2.addItemEntry(<xreliquary:potion>.withTag({effects: [{duration: 3000, potency: 2, name: "minecraft:speed"}], hasPotion: 1 as byte, splash: 1 as byte}), 30, 1, [Functions.setCount(1, 3)], []);
		thp2.addItemEntry(<minecraft:gold_ingot>, 40, 1, [Functions.setCount(3, 15)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 2 as short, id: 25 as short}]}), 20, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:clock>.withTag({"quark:clock_calculated": 1 as byte}), 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:compass>.withTag({"quark:compass_calculated": 1 as byte, "quark:compass_in_nether": 0 as byte}), 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<quark:variant_sapling:1>, 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<backpack:stick:1>, 40, 1, [Functions.setCount(1, 3)], []);
		thp2.addItemEntry(<quark:paper_wall>, 40, 1, [Functions.setCount(3, 17)], []);
		thp2.addItemEntry(<quark:paper_wall_sakura>, 40, 1, [Functions.setCount(3, 17)], []);
		thp2.addItemEntry(<mocreatures:medallion>, 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<comforts:hammock>, 5, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<comforts:sleeping_bag>, 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<conarm:travel_belt_base>, 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<conarm:gauntlet_mat>, 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<conarm:travel_cloak>, 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<conarm:travel_goggles_base>, 10, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:blaze_rod>, 30, 1, [Functions.setCount(1, 3)], []);
		thp2.addItemEntry(<xreliquary:mob_ingredient:7>, 30, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<xreliquary:mob_ingredient:4>, 30, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<xreliquary:mob_ingredient:3>, 30, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 3 as short, id: 2 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 3 as short, id: 16 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 3 as short, id: 21 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 4 as short, id: 32 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 4 as short, id: 48 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 51 as short}]}), 40, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<xreliquary:mob_ingredient:11>, 30, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<xreliquary:mob_ingredient:10>, 30, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:diamond_sword>, 20, 1, [Functions.setCount(1, 1)], []);
		thp2.addItemEntry(<minecraft:diamond_pickaxe>, 20, 1, [Functions.setCount(1, 1)], []);
	}
	
	// Configure rare tier rolls
	if (tier >= 2) {
		var thp3 as LootPool;
		if (tier == 2) {
			thp3 = lootTable.addPool("thp3", 0, 1, 0, 0);
		} else {
			thp3 = lootTable.addPool("thp3", 2, 3, 0, 0);
		}
		// Add rare tier items
		thp3.addItemEntry(<xreliquary:fertile_lilypad>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:glowing_bread>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:emperor_chalice>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:harvest_rod>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:sojourner_staff>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:hero_medallion>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:infernal_claws>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:kraken_shell>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:diamond>, 40, 1, [Functions.setCount(3, 8)], []);
		thp3.addItemEntry(<minecraft:emerald>, 40, 1, [Functions.setCount(3, 8)], []);
		thp3.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 33 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 4 as short, id: 32 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 1 as short, id: 31 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 3 as short, id: 29 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 3 as short, id: 24 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:enchanted_book>.withTag({StoredEnchantments: [{lvl: 3 as short, id: 8 as short}]}), 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:handgun>, 20, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorphelmetcave>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorpplatecave>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorplegscave>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorpbootscave>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<betterbuilderswands:wanddiamond>, 8, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:twilight_cloak>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<twilightforest:magic_map_empty>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<spartanshields:shield_basic_bronze>, 3, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<spartanshields:shield_basic_diamond>, 3, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_rainbow>, 1, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_walker>, 2, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_reflective>, 2, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_moon>, 2, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_bull>, 4, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_warped>, 2, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_fire>, 4, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_dragonslayer>, 1, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_carl>, 4, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<naturescompass:naturescompass>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<asr:diamond_giant_sword>, 15, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:sword_moonlight>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:sword_sunshine>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:flamethrower>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:bubble_pistol>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:bubble_rifle>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:longshot>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:musket_dagger_diamond>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:musket_dagger_monking>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:captain_revolver>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<eplus:table_upgrade>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:boots_diamond_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:helmet_inquisition>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_inquisition>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_inquisition>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:boots_inquisition>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:helmet_heavy_diamond>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_heavy_diamond>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_heavy_diamond>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:boots_heavy_diamond>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<minecraft:iron_block>, 40, 1, [Functions.setCount(1, 4)], []);
		thp3.addItemEntry(<minecraft:gold_block>, 40, 1, [Functions.setCount(1, 4)], []);
		thp3.addItemEntry(<minecraft:diamond_block>, 40, 1, [Functions.setCount(1, 3)], []);
		thp3.addItemEntry(<minecraft:emerald_block>, 40, 1, [Functions.setCount(1, 3)], []);
		thp3.addItemEntry(<minecraft:golden_apple:1>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:boots_cloud>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:king_crown>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_slime>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_slime>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_turtle>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_turtle>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_bull>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_bull>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_spider>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_spider>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:battle_axe_bull>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:sword_walker>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:shield_walker_king>, 1, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:great_sword_diamond>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:great_sword_bull>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:great_sword_monking>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:spear_diamond>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:staff_vampiric>, 20, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:staff_thunder>, 20, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:helmet_diamond_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:chestplate_diamond_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:leggings_diamond_dyable>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<cqrepoured:teleport_stone>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<torchmaster:mega_torch>, 45, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:horsearmorcrystal>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:tusksdiamond>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<exchangers:golden_exchanger>, 40, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<jexclusives:crystal>, 5, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<openblocks:golden_egg>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<hexxitgear:hexical_essence>, 40, 1, [Functions.setCount(2, 3)], []);
		thp3.addItemEntry(<openblocks:technicolor_glasses>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorphelmetfrost>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorpplatefrost>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorplegsfrost>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:scorpbootsfrost>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:holy_hand_grenade>, 40, 1, [Functions.setCount(1, 2)], []);
		thp3.addItemEntry(<mocreatures:heartundead>, 8, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:heartfire>, 8, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<mocreatures:heartdarkness>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:harvest_rod>, 25, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<quark:redstone_randomizer>, 30, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<quark:pirate_hat>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<xreliquary:mob_ingredient:8>, 8, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<hexxitworld:material:2>, 10, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<morebows:multi_bow>, 20, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<morebows:diamond_bow>, 20, 1, [Functions.setCount(1, 1)], []);
		thp3.addItemEntry(<morebows:ender_bow>, 20, 1, [Functions.setCount(1, 1)], []);
	}
}

// Make custom minecraft namespace loot tables for mods which use config files
populate(LootTweaker.newTable("thp_loot_common"), 1, true, false);
populate(LootTweaker.newTable("thp_loot_uncommon"), 2, true, false);
populate(LootTweaker.newTable("thp_loot_rare"), 3, true, false);
