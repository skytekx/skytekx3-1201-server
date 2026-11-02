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
