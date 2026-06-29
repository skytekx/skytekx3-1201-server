/*							===	thp_craftable_starting_gear ===							*/						

#priority 10

import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;
import crafttweaker.oredict.IOreDict;
import crafttweaker.oredict.IOreDictEntry;

// Craftable (full) Akashic Tome
var startingTome = <akashictome:tome>.withTag(
	{"akashictome:data": {
		chococraftplus: {id: "chococraftplus:chocopedia", Count: 1 as byte, tag: {"akashictome:definedMod": "chococraftplus"}, Damage: 0 as short}, 
		conarm: {id: "conarm:book", Count: 1 as byte, tag: {"akashictome:definedMod": "conarm"}, Damage: 0 as short}, 
		openblocks: {id: "openblocks:info_book", Count: 1 as byte, tag: {"akashictome:definedMod": "openblocks"}, Damage: 0 as short},
		tconstruct: {id: "tconstruct:book", Count: 1 as byte, tag: {"akashictome:definedMod": "tconstruct"}, Damage: 0 as short}, 
		thermalfoundation: {id: "thermalfoundation:tome_lexicon", Count: 1 as byte, tag: {"akashictome:definedMod": "thermalfoundation"}, Damage: 0 as short}, 
		twilightforest: {id: "patchouli:guide_book", Count: 1 as byte, tag: {"akashictome:definedMod": "twilightforest", "patchouli:book": "twilightforest:guide"}, Damage: 0 as short}}});

startingTome.addTooltip(format.lightPurple("All guides included"));
mods.jei.JEI.addItem(startingTome);
recipes.addShapeless(startingTome, [<hexxitgear:hexical_essence>, oreDict.bookshelf]);

// Craftable Patchouli book
var startingBook = <patchouli:guide_book>.withTag({"patchouli:book": "patchouli:hexxit2-book"});
recipes.addShapeless(startingBook, [<hexxitgear:hexical_essence>, <minecraft:book>]);