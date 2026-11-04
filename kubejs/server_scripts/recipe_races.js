// SkyTekx3: builds the recipes on one thread whose constructors write into shared state of their mod.
//
// After the recipe event, KubeJS turns the datapack recipes into game recipes on several threads at
// once. A few recipe types write into a plain HashMap or ArrayList of their mod while they are built.
// When two of them do that at the same moment, one fails with a ConcurrentModificationException and
// KubeJS drops it until the next start. The log then shows "Error parsing recipe <id>: null" and one
// failed recipe more than usual.
//
// - tconstruct:swappable_modifier (the embellishments and a few other Tinker Station upgrades) looks up
//   a material name in a cache of Tinkers' Construct that is a plain HashMap. That dropped a wood
//   embellishment on three boots on 2026-11-02 and 2026-11-03.
// - reliquary:alkahestry_crafting and reliquary:alkahestry_charging add themselves to a plain ArrayList
//   in Reliquary's AlkahestryRecipeRegistry. That dropped reliquary:alkahestry/crafting/netherrack at the
//   boot of 2026-11-04 10:33, the first boot after the Tinkers recipes had moved here.
//
// This script builds those recipes inside the recipe event, which runs on the main thread. KubeJS keeps a
// recipe that it built for a script and reuses it when nothing changed it, so the threaded pass no longer
// builds them. A recipe that fails here is named in a warning, because KubeJS then drops it without a
// message of its own.

ServerEvents.recipes(event => {
  var types = [
    'tconstruct:swappable_modifier',
    'reliquary:alkahestry_crafting',
    'reliquary:alkahestry_charging'
  ]
  var counts = []
  var failed = []
  for (var i = 0; i < types.length; i++) {
    var type = types[i]
    var built = 0
    try {
      event.forEachRecipe({ type: type }, recipe => {
        if (recipe.getOriginalRecipe() != null) {
          built++
        } else {
          failed.push(String(recipe.getId()))
        }
      })
    } catch (e) {
      console.error('Recipe races: building the ' + type + ' recipes failed: ' + e)
      continue
    }
    counts.push(built + ' ' + type)
  }
  console.info('Recipe races: built ' + counts.join(', ') + ' before the threaded pass, ' + failed.length +
    ' failed')
  if (failed.length > 0) console.warn('Recipe races: these recipes are missing: ' + failed.join(', '))
})
