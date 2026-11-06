// Melting dump for tools/gen_melting.py. A KubeJS 6 server script for test servers only.
//
// It never belongs in kubejs/ of the repo or of the live server. Run it on a test server like this:
//
//   1. Copy it: cp tools/melting_dump.js <test server>/kubejs/server_scripts/zz_melting_dump.js
//   2. Boot the test server, or run minecraft:reload on a running one. The recipe event writes recipe_json.
//   3. Run kubejs custom_command melting_dump on the console or over RCON. It writes the other files and logs
//      "MELTDUMP done" to logs/kubejs/server.log.
//   4. Remove it: rm <test server>/kubejs/server_scripts/zz_melting_dump.js
//   5. Generate, from the repo root: python3 -I tools/gen_melting.py <test server>/kubejs
//
// The files land in <test server>/kubejs/ as melting_dump_<name>.json, since KubeJS writes no new folders. The
// generator reads them there, or from any folder that holds a copy of them with or without the prefix.
//
// What the files hold:
//   recipe_json.json  The JSON of every recipe that reached the KubeJS recipe event, keyed by recipe id, after
//                     AlmostUnified. It is the source for recipe types whose ingredients the parsed recipe hides.
//   recipes.json      Every recipe of the final recipe manager: type, class, id, result item and count, and the
//                     ingredients resolved to item lists (an index into the table "ing"). Tinkers melting recipes add
//                     the input, the fluid outputs with byproducts, temperature, time and ore rate, item casting
//                     recipes the cast, whether it is consumed, the fluid alternatives with amounts, the result and
//                     cooling time. A recipe with an ingredient that no item can fill, such as an empty tag, has
//                     "unfillable".
//   items.json        Every registered item with its max damage, crafting remainder, armor slot and material, tool
//                     tier and the repair ingredients, and a few item classes.
//   fluids.json       Every registered fluid with its temperature.
//   tags.json         Every item tag and every fluid tag of the final registries with their ids, read from the
//                     registries, so an empty tag is an empty list.
//   mods.json         The ids of the loaded mods. An item namespace is not always a mod id (irons_lib registers
//                     items as irons_patreon_lib), and a forge:mod_loaded condition needs the mod id.
//
// Forge shows an empty tag in an ingredient as a barrier named "Empty Tag: <tag>" (Ingredient$TagValue). No survival
// player can get a barrier, so the dump leaves every barrier out of every item list.
//
// The command also logs about 120 Rhino reflection ERROR lines for client classes of some item classes. They are
// harmless on a test server and one more reason to keep the script off the live server.

var mdmpBR = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
var mdmpIS = Java.loadClass('net.minecraft.world.item.ItemStack')
var mdmpMelt = Java.loadClass('slimeknights.tconstruct.library.recipe.melting.MeltingRecipe')
var mdmpDmg = Java.loadClass('slimeknights.tconstruct.library.recipe.melting.DamageableMeltingRecipe')
var mdmpOre = Java.loadClass('slimeknights.tconstruct.library.recipe.melting.OreMeltingRecipe')
var mdmpCast = Java.loadClass('slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipe')
var mdmpGsonObj = Java.loadClass('com.google.gson.JsonObject')
var mdmpClasses = [
  ['armor', 'net.minecraft.world.item.ArmorItem'],
  ['tiered', 'net.minecraft.world.item.TieredItem'],
  ['sword', 'net.minecraft.world.item.SwordItem'],
  ['digger', 'net.minecraft.world.item.DiggerItem'],
  ['projectile_weapon', 'net.minecraft.world.item.ProjectileWeaponItem'],
  ['shield', 'net.minecraft.world.item.ShieldItem'],
  ['trident', 'net.minecraft.world.item.TridentItem'],
  ['horse_armor', 'net.minecraft.world.item.HorseArmorItem'],
  ['elytra', 'net.minecraft.world.item.ElytraItem'],
  ['shears', 'net.minecraft.world.item.ShearsItem'],
  ['fishing_rod', 'net.minecraft.world.item.FishingRodItem'],
  ['block', 'net.minecraft.world.item.BlockItem']
]
// Item classes of mods: the runic altar of Botania hands back every RuneItem it takes
var mdmpModClasses = [
  ['botania_rune', 'vazkii.botania.common.item.material.RuneItem']
]
var mdmpClassObjs = []
for (var mdmpI = 0; mdmpI < mdmpClasses.length; mdmpI++) {
  mdmpClassObjs.push([mdmpClasses[mdmpI][0], Java.loadClass(mdmpClasses[mdmpI][1])])
}
for (var mdmpJ = 0; mdmpJ < mdmpModClasses.length; mdmpJ++) {
  try {
    mdmpClassObjs.push([mdmpModClasses[mdmpJ][0], Java.loadClass(mdmpModClasses[mdmpJ][1])])
  } catch (mdmpErr) {
    console.info('MELTDUMP no class ' + mdmpModClasses[mdmpJ][1])
  }
}

function mdmpWrite(name, obj) {
  JsonIO.write('kubejs/melting_dump_' + name, JsonIO.parseRaw(JSON.stringify(obj)))
}

var mdmpPlaceholders = 0

// The ids of the items an ingredient matches, without the barrier that stands for an empty tag. Rhino cannot call
// Ingredient.getItems on this server, so the placeholder is not told from a real barrier. No survival player can get
// a barrier, and no recipe of the pack names one, so every barrier leaves the list.
function mdmpIds(ingredient) {
  var out = []
  if (ingredient === null || ingredient.isEmpty()) {
    return out
  }
  ingredient.getItemIds().forEach(function (id) {
    if (String(id) === 'minecraft:barrier') {
      mdmpPlaceholders++
    } else {
      out.push(String(id))
    }
  })
  out.sort()
  return out
}

// An ingredient that is not the empty slot of a grid but matches no item, so no player can fill it. It takes the ids
// from mdmpIds, so every placeholder is counted once.
function mdmpUnfillable(ingredient, ids) {
  return ingredient !== null && !ingredient.isEmpty() && ids.length === 0
}

// Rhino cannot call the methods of the JDK's own immutable lists (List.of), so copy them into a Guava list first
var mdmpImmutableList = Java.loadClass('com.google.common.collect.ImmutableList')
function mdmpList(list) {
  return mdmpImmutableList.copyOf(list)
}

function mdmpFluid(stack) {
  return [String(mdmpBR.FLUID.getKey(stack.getFluid())), stack.getAmount()]
}

ServerEvents.recipes(function (event) {
  var all = new mdmpGsonObj()
  var n = 0
  event.forEachRecipe({}, function (r) {
    all.add(String(r.getId()), r.json)
    n++
  })
  JsonIO.write('kubejs/melting_dump_recipe_json.json', all)
  console.info('MELTDUMP recipe json ' + n)
})

// RecipeManager.getRecipes() gives every recipe as one collection. Create: Dragons Plus adds the accessor
// RecipeManagerAccessor.getRecipes() with the same name and no arguments, which returns the map of recipes by type
// (field recipes), and Rhino may call that one. Such a map is flattened by type and id, which is what the method of
// the manager returns as well.
var mdmpArrayList = Java.loadClass('java.util.ArrayList')
var mdmpRecipeSource = 'collection'
function mdmpAllRecipes(manager) {
  var all = manager.getRecipes()
  if (all.keySet === undefined) {
    return all
  }
  mdmpRecipeSource = 'map by type'
  var out = new mdmpArrayList()
  all.values().forEach(function (byId) {
    out.addAll(byId.values())
  })
  return out
}

function mdmpRecipes() {
  var server = Utils.server
  var access = server.registryAccess()
  var ingIndex = {}
  var ingTable = []
  var recipes = []
  var failed = 0
  var ingRef = function (ids) {
    var key = ids.join(' ')
    if (ingIndex[key] === undefined) {
      ingIndex[key] = ingTable.length
      ingTable.push(ids)
    }
    return ingIndex[key]
  }
  mdmpAllRecipes(server.getRecipeManager()).forEach(function (r) {
    var e = { id: String(r.getId()), type: String(r.getType()) }
    var res, list, i, k, lists, fl, inner, ids
    try {
      // The class shows whether a crafting recipe hands items back through getRemainingItems
      e.cls = String(r.getClass().getName())
    } catch (err0) {
      // A recipe without its own toString prints as class@hash
      e.cls = String(r).split('@')[0]
      e.cls_error = String(err0)
    }
    try {
      res = r.getResultItem(access)
      if (res !== null && !res.isEmpty()) {
        e.result = String(mdmpBR.ITEM.getKey(res.getItem()))
        e.count = res.getCount()
        // Not hasTag() or getTag(): Rhino resolves them to KubeJS methods. A plain damageable item carries {Damage:0}
        if (res.hasNBT() && String(res.getNbt()) !== '{Damage:0}') {
          e.nbt = String(res.getNbt())
        }
      }
    } catch (err) {
      e.result_error = String(err)
    }
    try {
      list = r.getIngredients()
      e.ing = []
      for (i = 0; i < list.size(); i++) {
        ids = mdmpIds(list.get(i))
        e.ing.push(ingRef(ids))
        if (mdmpUnfillable(list.get(i), ids)) {
          e.unfillable = true
        }
      }
    } catch (err2) {
      e.ing_error = String(err2)
      failed++
    }
    try {
      if (r instanceof mdmpMelt) {
        e.melt = {
          input: ingRef(mdmpIds(r.getInput())),
          temperature: r.getTemperature(),
          time: r.getTime(),
          damageable: r instanceof mdmpDmg,
          ore: r instanceof mdmpOre ? String(r.getOreType()) : null,
          outputs: []
        }
        lists = mdmpList(r.getOutputWithByproducts())
        for (i = 0; i < lists.size(); i++) {
          fl = []
          inner = mdmpList(lists.get(i))
          for (k = 0; k < inner.size(); k++) {
            fl.push(mdmpFluid(inner.get(k)))
          }
          e.melt.outputs.push(fl)
        }
        e.melt.output = mdmpFluid(r.getOutput())
      } else if (r instanceof mdmpCast) {
        ids = mdmpIds(r.getCast())
        if (mdmpUnfillable(r.getCast(), ids)) {
          e.unfillable = true
        }
        e.cast = {
          cast: ingRef(ids),
          consumed: r.isConsumed(),
          switch_slots: r.switchSlots(),
          cooling: r.getCoolingTime(),
          fluids: []
        }
        lists = mdmpList(r.getFluid().getFluids())
        for (i = 0; i < lists.size(); i++) {
          e.cast.fluids.push(mdmpFluid(lists.get(i)))
        }
        res = r.getOutput()
        e.cast.result = String(mdmpBR.ITEM.getKey(res.getItem()))
        e.cast.count = res.getCount()
      }
    } catch (err3) {
      e.tinkers_error = String(err3)
    }
    recipes.push(e)
  })
  mdmpWrite('recipes.json', { ing: ingTable, recipes: recipes })
  return recipes.length + ' recipes from the ' + mdmpRecipeSource + ', ' + ingTable.length + ' distinct ingredients, ' +
    failed + ' unreadable, ' + recipes.filter(function (x) { return x.unfillable }).length + ' unfillable'
}

function mdmpItems() {
  var items = []
  mdmpBR.ITEM.forEach(function (item) {
    var e = { id: String(mdmpBR.ITEM.getKey(item)) }
    var st, rem, i, mat
    try {
      st = new mdmpIS(item)
      e.max_damage = st.getMaxDamage()
      e.damageable = st.isDamageableItem()
      e.stack = st.getMaxStackSize()
      if (st.hasCraftingRemainingItem()) {
        rem = st.getCraftingRemainingItem()
        e.remainder = rem.isEmpty() ? 'minecraft:air' : String(mdmpBR.ITEM.getKey(rem.getItem()))
      }
      e.kinds = []
      for (i = 0; i < mdmpClassObjs.length; i++) {
        if (item instanceof mdmpClassObjs[i][1]) {
          e.kinds.push(mdmpClassObjs[i][0])
        }
      }
      if (item instanceof mdmpClassObjs[0][1]) {
        mat = item.getMaterial()
        e.armor_type = String(item.getType())
        // No getName(): the material of Simply Jetpacks 2 lacks it, and the AbstractMethodError escapes try and catch
        e.armor_material = String(mat)
        e.repair = mdmpIds(mat.getRepairIngredient())
      } else if (item instanceof mdmpClassObjs[1][1]) {
        e.tier = String(item.getTier())
        e.repair = mdmpIds(item.getTier().getRepairIngredient())
      }
      e.name = String(st.getHoverName().getString())
    } catch (err) {
      e.error = String(err)
    }
    items.push(e)
  })
  mdmpWrite('items.json', { items: items })
  return items.length + ' items'
}

// The ids of one tag of a registry, read from the registry itself, so an empty tag gives an empty list
function mdmpTagIds(registry, key) {
  var ids = []
  var opt = registry.getTag(key)
  var set, i, id
  if (opt.isPresent()) {
    set = opt.get()
    for (i = 0; i < set.size(); i++) {
      id = String(registry.getKey(set.get(i).value()))
      if (ids.indexOf(id) < 0) {
        ids.push(id)
      }
    }
  }
  ids.sort()
  return ids
}

function mdmpTags() {
  // The registries hand out their tag names only as a Stream, whose methods Rhino cannot call on this Java version.
  // Guava walks it next to a list that is longer than any tag list (36,073 items against 4,133 item tags in 2026-11).
  var Streams = Java.loadClass('com.google.common.collect.Streams')
  var pad = Item.getTypeList()
  var items = {}
  var fluids = {}
  var walked = [0, 0]
  Streams.forEachPair(mdmpBR.ITEM.getTagNames(), Streams.stream(pad), function (key, unused) {
    items[String(key.location())] = mdmpTagIds(mdmpBR.ITEM, key)
    walked[0]++
  })
  Streams.forEachPair(mdmpBR.FLUID.getTagNames(), Streams.stream(pad), function (key, unused) {
    fluids[String(key.location())] = mdmpTagIds(mdmpBR.FLUID, key)
    walked[1]++
  })
  // The walk stops at the shorter of the two, so a walk as long as the padding list may have lost tags
  if (walked[0] >= pad.size() || walked[1] >= pad.size()) {
    throw new Error('the padding list (' + pad.size() + ') is too short for the tags (' + walked + ')')
  }
  mdmpWrite('tags.json', { items: items, fluids: fluids })
  return Object.keys(items).length + ' item tags, ' + Object.keys(fluids).length + ' fluid tags'
}

function mdmpMods() {
  // The class filter of KubeJS blocks the Forge ModList, the Platform binding lists the loaded mods
  var ids = []
  Platform.getList().forEach(function (id) {
    ids.push(String(id))
  })
  ids.sort()
  mdmpWrite('mods.json', { mods: ids })
  return ids.length + ' mods'
}

function mdmpFluids() {
  var fluids = []
  mdmpBR.FLUID.forEach(function (fluid) {
    var e = { id: String(mdmpBR.FLUID.getKey(fluid)) }
    try {
      e.temperature = fluid.getFluidType().getTemperature()
    } catch (err) {
      e.error = String(err)
    }
    fluids.push(e)
  })
  mdmpWrite('fluids.json', { fluids: fluids })
  return fluids.length + ' fluids'
}

ServerEvents.customCommand('melting_dump', function (event) {
  var t0 = Date.now()
  var a = mdmpRecipes()
  var b = mdmpItems()
  var c = mdmpFluids()
  var d = mdmpTags()
  var m = mdmpMods()
  console.info('MELTDUMP done: ' + a + ', ' + b + ', ' + c + ', ' + d + ', ' + m + ', ' + mdmpPlaceholders +
    ' empty tag placeholders left out, in ' + (Date.now() - t0) + ' ms')
})
