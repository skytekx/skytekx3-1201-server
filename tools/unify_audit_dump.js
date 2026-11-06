// Dump for tools/unify_audit.py. It runs on a test server only and never ships: copy it into the
// kubejs/server_scripts/ of a test server, start it, send `unify_audit_dump` over RCON, wait for the
// line "Unify audit dump:" in logs/kubejs/server.log and copy kubejs/unify_audit_dump.json out. Then
// delete the script and the JSON from the test server again.
//
// The dump holds every item of an Almost Unified tag with the item that Almost Unified swaps it for
// and its item tags, and every loaded recipe with the item ids of its ingredients and its result. The
// recipes are spread over ticks (25 ms each), so the watchdog never fires.
var UnifyDumpBIR = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
var UnifyDumpList = Java.loadClass('java.util.ArrayList')
var UnifyDumpRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

function unifyDumpIds(set) {
  var out = []
  var arr = set.toArray()
  for (var i = 0; i < arr.length; i++) out.push(String(arr[i]))
  out.sort()
  return out
}

// KubeJS hides Ingredient.getItems(), so the ids come from getItemIds() or the stacks.
function unifyDumpIngredient(ing) {
  try {
    return unifyDumpIds(ing.getItemIds())
  } catch (e) {
    var out = []
    var arr = ing.getStacks().toArray()
    for (var i = 0; i < arr.length; i++) out.push(String(arr[i].id))
    out.sort()
    return out
  }
}

function unifyDumpTags(id) {
  var out = []
  var arr = Item.of(id).getItem().builtInRegistryHolder().tags().toArray()
  for (var i = 0; i < arr.length; i++) out.push(String(arr[i].location()))
  out.sort()
  return out
}

function unifyDumpRun(server) {
  var t0 = Date.now()
  var reg = server.registryAccess()
  var unify = []
  var variants = {}
  var tags = unifyDumpIds(AlmostUnified.getTags())
  for (var i = 0; i < tags.length; i++) {
    var rl = new UnifyDumpRL(tags[i])
    var pref = AlmostUnified.getPreferredItemForTag(rl)
    var ids = unifyDumpIds(AlmostUnified.getItemIds(rl))
    unify.push({ tag: tags[i], preferred: pref == null || pref.isEmpty() ? '' : String(pref.id), items: ids })
    for (var j = 0; j < ids.length; j++) {
      if (variants.hasOwnProperty(ids[j])) continue
      // The swap of an ore depends on its stone, so it comes from Almost Unified, not from the tag.
      var rep = AlmostUnified.getReplacementForItem(Item.of(ids[j]))
      variants[ids[j]] = { to: rep == null || rep.isEmpty() ? '' : String(rep.id), tags: unifyDumpTags(ids[j]) }
    }
  }
  var items = unifyDumpIds(UnifyDumpBIR.ITEM.keySet())
  var rm = server.getRecipeManager()
  // KubeJS answers getType() with the serializer id, so the recipe type comes from the manager itself.
  var typeOf = {}
  var typeKeys = UnifyDumpBIR.RECIPE_TYPE.keySet().toArray()
  for (var i = 0; i < typeKeys.length; i++) {
    var byType = new UnifyDumpList(rm.getAllRecipesFor(UnifyDumpBIR.RECIPE_TYPE.get(typeKeys[i])))
    for (var j = 0; j < byType.size(); j++) typeOf[String(byType.get(j).getId())] = String(typeKeys[i])
  }
  // Create: Dragons Plus adds the accessor RecipeManagerAccessor.getRecipes() with the same name and no arguments,
  // which returns the map of recipes by type, and Rhino may call that one. Such a map is flattened by type and id.
  var byName = rm.getRecipes()
  var all = new UnifyDumpList()
  if (byName.keySet === undefined) {
    all.addAll(byName)
  } else {
    byName.values().forEach(byId => all.addAll(byId.values()))
  }
  var table = []
  var index = {}
  var recipes = []
  var st = { i: 0, errors: 0 }
  var step = () => {
    var tick = Date.now()
    while (st.i < all.size() && Date.now() - tick < 25) {
      var r = all.get(st.i++)
      var rec = { id: String(r.getId()) }
      try {
        var ser = UnifyDumpBIR.RECIPE_SERIALIZER.getKey(r.getSerializer())
        rec.ser = ser == null ? '' : String(ser)
        rec.type = typeOf.hasOwnProperty(rec.id) ? typeOf[rec.id] : ''
        try {
          rec.w = r.getRecipeWidth()
          rec.h = r.getRecipeHeight()
        } catch (e) {}
        var ings = r.getIngredients()
        var refs = []
        for (var k = 0; k < ings.size(); k++) {
          var key = unifyDumpIngredient(ings.get(k))
          var joined = key.join(',')
          if (!index.hasOwnProperty(joined)) {
            index[joined] = table.length
            table.push(key)
          }
          refs.push(index[joined])
        }
        rec.ings = refs
        try {
          // KubeJS hides hasTag(), so the NBT comes from res.nbt.
          var res = r.getResultItem(reg)
          if (res != null && !res.isEmpty()) rec.out = { item: String(res.id), count: res.getCount(), nbt: res.nbt == null ? '' : String(res.nbt) }
        } catch (e) {}
      } catch (e) {
        rec.error = String(e)
        if (st.errors++ < 10) console.error('Unify audit dump: ' + rec.id + ': ' + e)
      }
      recipes.push(rec)
    }
    if (st.i < all.size()) {
      server.scheduleInTicks(1, step)
      return
    }
    JsonIO.write('kubejs/unify_audit_dump.json', { items: items, unify: unify, variants: variants, table: table, recipes: recipes })
    console.info('Unify audit dump: ' + recipes.length + ' recipes, ' + unify.length + ' unify tags, ' + Object.keys(variants).length + ' tagged items, ' + st.errors + ' errors, ' + Math.round((Date.now() - t0) / 1000) + ' s')
  }
  step()
}

ServerEvents.commandRegistry(event => {
  var Commands = event.commands
  event.register(Commands.literal('unify_audit_dump').requires(s => s.hasPermission(2)).executes(ctx => {
    unifyDumpRun(ctx.source.server)
    return 1
  }))
})
