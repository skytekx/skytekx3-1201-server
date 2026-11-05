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
