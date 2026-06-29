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
