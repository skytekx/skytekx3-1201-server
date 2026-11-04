// SkyTekx3: builds the swappable modifier recipes of Tinkers' Construct on one thread.
//
// After the recipe event, KubeJS turns the datapack recipes into game recipes on several threads at
// once. The recipe type tconstruct:swappable_modifier (the embellishments and a few other Tinker Station
// upgrades) looks up a material name while it is built, in a cache of Tinkers' Construct that is a plain
// HashMap. When two of these recipes fill the cache at the same moment, one of them fails with a
// ConcurrentModificationException and KubeJS drops it until the next start. The log then shows "Error
// parsing recipe tconstruct:tools/modifiers/slotless/embellishment/...: null" and 14 instead of 13 failed
// recipes. That happened on three boots on 2026-11-02 and 2026-11-03, each time to another wood
// embellishment.
//
// This script builds those recipes inside the recipe event, which runs on the main thread. KubeJS keeps a
// recipe that it built for a script and reuses it when nothing changed it, so the threaded pass no longer
// builds them, and the cache is full before any other thread reads it. Nothing on the server clears that
// cache, so a /reload never had the problem. A recipe that fails here is named in a warning, because
// KubeJS then drops it without a message of its own.

ServerEvents.recipes(event => {
  var built = 0
  var failed = []
  try {
    event.forEachRecipe({ type: 'tconstruct:swappable_modifier' }, recipe => {
      if (recipe.getOriginalRecipe() != null) {
        built++
      } else {
        failed.push(String(recipe.getId()))
      }
    })
  } catch (e) {
    console.error('Tinkers recipe race: building the swappable modifier recipes failed: ' + e)
    return
  }
  console.info('Tinkers recipe race: built ' + built + ' swappable modifier recipes before the threaded pass, ' +
    failed.length + ' failed')
  if (failed.length > 0) console.warn('Tinkers recipe race: these recipes are missing: ' + failed.join(', '))
})
