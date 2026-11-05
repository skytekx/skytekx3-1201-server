// SkyTekx3 — recipe fixes (ported from scripts-legacy-reference/Recipe Fixes.zs)
// Layer 3 of the conflict system. Unification (ores/ingots) is handled by AlmostUnified,
// in-grid conflicts by Polymorph; this file is for explicit removes/adds.
// Status tags: [PORT] live · [VERIFY] needs real 1.20.1 id · [REAUTHOR] successor mod · [DROP] obsolete.

ServerEvents.recipes(event => {

  // [PORT] Rotten flesh -> leather (smelting)
  event.smelting('minecraft:leather', 'minecraft:rotten_flesh').id('skytekx3:flesh_to_leather')

  // [PORT] Bookshelf accepts ANY plank (was oak-only conflict w/ Chisel/Quark)
  event.remove({ id: 'minecraft:bookshelf' })
  event.shaped('minecraft:bookshelf', ['PPP', 'BBB', 'PPP'], {
    P: '#minecraft:planks',
    B: 'minecraft:book'
  }).id('skytekx3:bookshelf_any_plank')

  // Draconic Evolution 3.1.2.621 asks for a Chaotic Core in the Chaotic Shield Capacity module, where an
  // Awakened Core was meant. Upstream fixed the recipe on its 1.21 branch in commit 0c85f0d9, which never
  // reached 1.20.1. The pack swaps the one ingredient here instead of shipping a copy of the recipe file,
  // see okf/modpack/draconic-fixes.md.
  event.replaceInput({ id: 'draconicevolution:modules/item_chaotic_shield_capacity' },
    'draconicevolution:chaotic_core', 'draconicevolution:awakened_core')

  // One item per material (2026-11-04). Two iron ingots on top of each other made 4 Immersive
  // Engineering rods or 2 Thermal Extra rods, and Polymorph offered both. Almost Unified turns both
  // results into the same rod, so the Thermal Extra recipe goes and the grid gives 4.
  event.remove({ id: 'thermal_extra:crafting/iron_rod' })

  // Spartan Weaponry smelts copper and steel weapons into the first nugget of a tag, which is a
  // variant that Almost Unified swaps away. The same recipes give the preferred nugget instead.
  var spartanWeapons = ['dagger', 'parrying_dagger', 'longsword', 'katana', 'saber', 'rapier', 'greatsword',
    'battle_hammer', 'warhammer', 'spear', 'halberd', 'pike', 'lance', 'longbow', 'heavy_crossbow',
    'throwing_knife', 'tomahawk', 'javelin', 'boomerang', 'battleaxe', 'flanged_mace', 'glaive',
    'quarterstaff', 'scythe']
  var spartanNuggets = { copper: 'thermal:copper_nugget', steel: 'mekanism:nugget_steel' }
  Object.keys(spartanNuggets).forEach(metal => {
    var weapons = spartanWeapons.map(w => 'spartanweaponry:' + metal + '_' + w)
    var smelt = metal + '_nugget_from_smelting_' + metal + '_weapons'
    var blast = metal + '_nugget_from_blasting_' + metal + '_weapons'
    event.remove({ id: 'spartanweaponry:' + smelt })
    event.remove({ id: 'spartanweaponry:' + blast })
    event.smelting(spartanNuggets[metal], weapons).xp(0.1).cookingTime(200).id('skytekx3:' + smelt)
    event.blasting(spartanNuggets[metal], weapons).xp(0.1).cookingTime(100).id('skytekx3:' + blast)
  })

  // Reliquary's Alkahestry Tome doubles steel into the first ingot of the tag. With the preferred
  // ingot as its input it gives that ingot back.
  event.remove({ id: 'reliquary:alkahestry/crafting/steel_ingot' })
  event.custom({
    type: 'reliquary:alkahestry_crafting',
    charge: 32,
    ingredient: { item: 'mekanism:ingot_steel' },
    result_count: 2
  }).id('skytekx3:alkahestry/crafting/steel_ingot')

  // ---------------------------------------------------------------------------
  // The following ported the SkyTekx2 intent but reference mods whose ids change
  // on 1.20.1. They are left commented until the mods are in `mods/`; confirm the
  // exact item ids (EMI -> copy id) then uncomment. See docs/RECIPE_PORT.md.
  // ---------------------------------------------------------------------------

  // [REAUTHOR] Angel Ring: keep "loot-only, tiered upgrade" intent.
  //   ExtraUtils2 -> Excessive Utilities. Remove craft, keep upgrade chain.
  // event.remove({ output: 'excessiveutilities:angel_ring' })

  // [VERIFY] Hexical Diamond (Hexxit Gear R). Nyx 'fallen_star' is gone -> swap star item.
  // event.remove({ id: 'hexxitgear:hexical_diamond' })
  // event.shaped('hexxitgear:hexical_diamond', [' S ', 'EDE', ' E '], {
  //   S: 'minecraft:nether_star', E: 'hexxitgear:hexical_essence', D: 'minecraft:diamond'
  // })

  // [VERIFY] Fresh water — HarvestCraft -> HarvestCraft 2
  // event.shapeless('8x harvestcraft:freshwater', ['minecraft:water_bucket'])

  // [DROP] Steel fix, coal-coke mirror, IC2 energy producers:
  //   handled by AlmostUnified (forge:ingots/steel, forge:storage_blocks/coal_coke)
  //   or removed with IC2 Classic / EnergyConverters.
})
