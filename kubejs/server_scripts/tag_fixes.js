// SkyTekx3 — tag fixes
// Ad Astra ships Mekanism/Create/IE/Thermal compat recipes for Venus sandstone that use
// the tag `forge:sandstone/venus_sandstone`, but never populates that tag -> Mekanism reports
// a broken/empty tag and the crushing recipe (venus_sandstone -> venus_sand) silently breaks.
// Populate it with the actual block so the recipes resolve and the warning clears.

ServerEvents.tags('item', event => {
  event.add('forge:sandstone/venus_sandstone', 'ad_astra:venus_sandstone')
})
ServerEvents.tags('block', event => {
  event.add('forge:sandstone/venus_sandstone', 'ad_astra:venus_sandstone')
})

// Twilight's Flavors & Delight (twilightdelight 2.2.x) registers its ice creams, milkshakes and
// cake slices only when Neapolitan is installed, but its farmersdelight:sweets,
// farmersdelight:snacks and diet:sugars tag files list them unconditionally. Without Neapolitan
// the vanilla TagLoader drops each of those three tags completely ("Couldn't load tag ... missing
// following references"), taking every other mod's entries with it. Strip the unregistered ids so
// the tags load again. Neapolitan is not in the pack; the guard keeps the entries if it is added.
if (!Platform.isLoaded('neapolitan')) {
  let tfdNeapolitanFoods = [
    'twilightdelight:aurora_ice_cream', 'twilightdelight:aurora_milkshake', 'twilightdelight:aurora_cake_slice',
    'twilightdelight:torchberry_ice_cream', 'twilightdelight:torchberry_milkshake', 'twilightdelight:torchberry_cake_slice',
    'twilightdelight:phytochemical_ice_cream', 'twilightdelight:phytochemical_milkshake', 'twilightdelight:phytochemical_cake_slice',
    'twilightdelight:glacier_ice_cream', 'twilightdelight:glacier_milkshake', 'twilightdelight:glacier_cake_slice',
    'twilightdelight:twilight_ice_cream', 'twilightdelight:rainbow_ice_cream', 'twilightdelight:refreshing_ice_cream'
  ]

  ServerEvents.tags('item', event => {
    ['farmersdelight:sweets', 'farmersdelight:snacks', 'diet:sugars'].forEach(tag => {
      tfdNeapolitanFoods.forEach(id => event.remove(tag, id))
    })
  })
}

// TF 4.8 backport: upstream tags its edible berries c:foods/berry (1.21). The 1.20.1 Forge tag that
// Farmer's Delight (fruit_salad, cooking/stuffed_pumpkin_block) and Medieval Embroidery
// (mixedberry_pie) read is forge:berries.
ServerEvents.tags('item', event => {
  event.add('forge:berries', [
    'twilightforest:torchberries', 'twilightforest:raspberry', 'twilightforest:blackberry',
    'twilightforest:blueberry', 'twilightforest:maloberry', 'twilightforest:duskberry',
    'twilightforest:skyberry', 'twilightforest:blightberry', 'twilightforest:stingberry'
  ])
})

// Yellorium is its own material, not uranium, but Extreme Reactors also lists its ingot, dust and ore in
// the uranium tags. The uranium recipes of other mods then took them and gave Mekanism uranium, and the
// furnace or the crafting grid could pick either recipe, for example yellorium ingot to nuggets or
// yellorite ore to an ingot. Out of the uranium tags, no recipe turns them into uranium any more.
// Extreme Reactors keeps working: each of its recipes that asks for the uranium tag has a twin that asks
// for yellorium, and its reactors take yellorium fuel through forge:ingots/yellorium as well. Almost
// Unified ignores bigreactors items since 2026-11-02 for the same reason, and
// kubejs/server_scripts/unify_inventory.js skips them.
// Extreme Reactors 2.0.97 adds its deepslate ore, raw yellorium, the raw yellorium block and the
// yellorium block to the uranium tags as well. Its own recipes name those items directly, so they stay
// out of the uranium tags too. Otherwise the uranium recipes of Mekanism, Immersive Engineering, Create,
// Tinkers' Construct, Thermal Extra, Blood Magic and Industrial Foregoing would turn them into uranium.
ServerEvents.tags('item', event => {
  event.remove('forge:ingots/uranium', 'bigreactors:yellorium_ingot')
  event.remove('forge:dusts/uranium', 'bigreactors:yellorium_dust')
  event.remove('forge:ores/uranium', ['bigreactors:yellorite_ore', 'bigreactors:deepslate_yellorite_ore'])
  event.remove('forge:raw_materials/uranium', 'bigreactors:raw_yellorium')
  event.remove('forge:storage_blocks/raw_uranium', 'bigreactors:raw_yellorium_block')
  event.remove('forge:storage_blocks/uranium', 'bigreactors:yellorium_block')
})
ServerEvents.tags('block', event => {
  event.remove('forge:ores/uranium', 'bigreactors:yellorite_ore')
})

// Mekanism lists its sulfur dust in forge:dyes/yellow, so the yellow dye recipes of other mods take it as
// one of the yellow dyes. Almost Unified makes the sulfur dust of Thermal Foundation the one sulfur dust of
// the pack and kubejs/server_scripts/unify_inventory.js turns the Mekanism dust into it, but Thermal does
// not list its dust as a dye. Players never held the dust those recipes asked for. On the test server on
// 2026-11-04, 160 recipes took the Mekanism dust and not the Thermal dust, all of them through this tag.
// A replaceInput cannot fix them, because it swaps the whole yellow dye ingredient, so the recipes would
// stop taking yellow dye itself, and it does not reach the machine recipes of every mod. With every
// sulfur dust in the tag, all of them take the Thermal dust next to yellow dye.
ServerEvents.tags('item', event => {
  event.add('forge:dyes/yellow', '#forge:dusts/sulfur')
})

// Draconic Evolution 3.1.2.621 puts none of its tools into the vanilla tool tags, so mods that ask for a
// pickaxe, an axe, a shovel, a sword or a hoe by tag do not see them. Upstream added the tags on its 1.21
// branch in commit 4d360c95, which never reached 1.20.1. Both staffs count as the first four tools there,
// and so they do here, see okf/modpack/draconic-fixes.md.
if (Platform.isLoaded('draconicevolution')) {
  ServerEvents.tags('item', event => {
    let tiers = ['wyvern', 'draconic', 'chaotic']
    let tools = { pickaxes: 'pickaxe', axes: 'axe', shovels: 'shovel', swords: 'sword', hoes: 'hoe' }
    Object.keys(tools).forEach(tag => {
      event.add('minecraft:' + tag, tiers.map(tier => 'draconicevolution:' + tier + '_' + tools[tag]))
      if (tag != 'hoes') {
        event.add('minecraft:' + tag, ['draconicevolution:draconic_staff', 'draconicevolution:chaotic_staff'])
      }
    })
  })
}

// Expanded Delight (local-mods, built from ports/expandeddelight) brings salt, juices and jellies that
// Pam's HarvestCraft 2 Food Core and Mekanism also have. The recipes of Pam's mod ask for forge:salt,
// forge:juices/<fruit>juice and forge:jellies/<fruit>jelly, those of Expanded Delight and Mekanism for
// forge:dusts/salt, and Expanded Delight's sandwiches for forge:jams/<fruit>. With these entries every
// salt fits every salt recipe and each juice and jelly fits the recipes of the other mod. Almost Unified
// then makes the Mekanism salt the one salt of the pack (config/almostunified/unify.json), see
// okf/modpack/expandeddelight-port.md. Each block names items only of mods that are loaded, because one
// unknown entry would drop the whole tag.
// Medieval Embroidery has a fourth salt in no tag at all, which its 14 recipes name directly. In the salt
// tag it becomes a variant of the Mekanism salt as well, so its recipes take every salt and give the
// Mekanism salt, see okf/modpack/almostunified.md.
ServerEvents.tags('item', event => {
  event.add('forge:salt', '#forge:dusts/salt')
  if (Platform.isLoaded('pamhc2foodcore')) {
    event.add('forge:dusts/salt', 'pamhc2foodcore:saltitem')
  }
  if (Platform.isLoaded('medievalembroidery')) {
    event.add('forge:dusts/salt', 'medievalembroidery:salt')
  }
  if (Platform.isLoaded('pamhc2foodcore') && Platform.isLoaded('expandeddelight')) {
    event.add('forge:juices/applejuice', 'expandeddelight:apple_juice')
    event.add('forge:juices/sweetberryjuice', 'expandeddelight:sweet_berry_juice')
    event.add('forge:juices/glowberryjuice', 'expandeddelight:glow_berry_juice')
    event.add('forge:juices/apple', 'pamhc2foodcore:applejuiceitem')
    event.add('forge:juices/sweet_berry', 'pamhc2foodcore:sweetberryjuiceitem')
    event.add('forge:juices/glow_berry', 'pamhc2foodcore:glowberryjuiceitem')
    event.add('forge:jellies/sweetberryjelly', 'expandeddelight:sweet_berry_jelly')
    event.add('forge:jellies/glowberryjelly', 'expandeddelight:glow_berry_jelly')
    event.add('forge:jams/sweet_berry', 'pamhc2foodcore:sweetberryjellyitem')
    event.add('forge:jams/glow_berry', 'pamhc2foodcore:glowberryjellyitem')
  }
})
