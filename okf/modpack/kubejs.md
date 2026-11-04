---
type: Reference
title: KubeJS
description: The nine KubeJS server scripts and the data files of SkyTekx 3, what each one really does on 1.20.1, the recipe failures of the boot log, the unfinished port of the SkyTekx 2 CraftTweaker scripts and how to work with Rhino.
tags: [modpack, kubejs, recipes, scripting]
timestamp: 2026-11-04
---

# KubeJS

KubeJS is the scripting layer of the pack. It replaces the CraftTweaker scripts of SkyTekx 2, which are
kept read only in [scripts-legacy-reference/](../../scripts-legacy-reference) (7 files). The port is
tracked in [docs/RECIPE_PORT.md](../../docs/RECIPE_PORT.md).

| Part | Version |
|---|---|
| KubeJS | 2001.6.5-build.26 |
| Rhino (the JavaScript engine) | 2001.2.3-build.10 |
| LootJS | 2.13.1, installed but unused |
| KubeJS plugins loaded | Built in Forge plugin, LootJS, Cucumber, L2 Hostility (`logs/kubejs/server.log:2-6`) |

## Scripts

All scripts live in [kubejs/server_scripts/](../../kubejs/server_scripts). There are no startup or
client scripts, and the folders `startup_scripts/` and `assets/` are empty. The folder `data/` holds the
six uncrafting recipes of Twilight Tweaks, see
[Twilight Forest backport](twilightforest-backport.md), the wood fallbacks, see
[Wood fallbacks](#wood-fallbacks), and overrides for broken recipes and loot tables of other mods, see
[Recipe failures](#recipe-failures). The last boot on the build machine loaded 4 of
4 scripts with 0 errors and 0 warnings (`logs/kubejs/server.log:15`). The boot of the Mac mini on
2026-11-02 at 17:33 loaded 7 of 7, again with 0 errors and 0 warnings.

| Script | What it does |
|---|---|
| Recipe fixes, `recipe_fixes.js` | Adds two recipes, the rest is commented out |
| Tag fixes, `tag_fixes.js` | Mends tags of Ad Astra, Twilight's Flavors & Delight and the Twilight Forest backport, and keeps yellorium out of the uranium tags |
| Gameplay, `gameplay.js` | Nothing yet, every line is a comment |
| Combat, `combat_1_7.js` | Removes the attack cooldown, see [combat](../content/combat.md) |
| Apotheosis riders, `apotheosis_riders.js` | Puts the riders of Apotheosis bosses on their mounts, see [Apotheosis riders](#apotheosis-riders) |
| Skylands sleep, `skylands_sleep.js` | Turns the overworld to day when players sleep in the Skylands, see [Skylands worldgen](../content/skylands-worldgen.md#sleep-and-hostile-mobs) |
| Skylands portals, `skylands_portals.js` | Lets nether portals work in the Skylands, see [Skylands worldgen](../content/skylands-worldgen.md#nether-portals) |
| Unify inventory, `unify_inventory.js` | Swaps other variants of a material for the preferred item, see [AlmostUnified](almostunified.md#inventory-converter) |
| Recipe races, `recipe_races.js` | Builds three recipe types of Tinkers' Construct and Reliquary on the main thread, because they write into shared state of their mod while they are built, see [Recipe failures](#recipe-failures) |

The recipe summary of that boot reads "Added 2 recipes, removed 0 recipes, modified 14 recipes,
with 17 failed recipes" (`logs/kubejs/server.log:54`, 23:25:52). The 17 failures are third-party
recipes that name items which do not exist in this pack. They are not caused by these scripts. The
boot of the Mac mini on 2026-11-02 at 17:33 logged "Added 2 recipes, removed 0 recipes, modified 9
recipes, with 13 failed recipes". Those 13 failures are listed under [Recipe failures](#recipe-failures).

### Recipe fixes

The file [recipe_fixes.js](../../kubejs/server_scripts/recipe_fixes.js) has two live recipes:

- The recipe `skytekx3:flesh_to_leather` smelts rotten flesh into leather (line 9).
- The recipe `skytekx3:bookshelf_any_plank` crafts a bookshelf from any planks (lines 12 to 16). The
  script first removes `minecraft:bookshelf`, but that removal matches nothing: Quark's variant
  bookshelves are on (`config/quark-common.toml:137`), and Quark ships its own `minecraft:bookshelf`
  recipe that switches itself off when they are on. That is why the log says "removed 0 recipes". The
  added recipe overlaps Quark's per wood bookshelf recipes, and Polymorph lets the player choose.

The commented blocks wait for the right item ids:

- Angel Ring (lines 24 to 26): the SkyTekx 2 rule kept it loot only, now meant for Excessive Utilities.
- Hexical diamond (lines 28 to 32): the comment says Nyx's fallen star is gone. Nyx is back and
  registers `nyx:fallen_star` (see [Nyx](../content/nyx.md)). The commented code also uses the namespace
  `hexxitgear`, but the installed Hexxit Gear (`hexxit_gear-1.0.1.jar`) uses `hexxit_gear`.
- Fresh water for HarvestCraft 2 (lines 34 and 35).

### Wood fallbacks

Since 2026-11-03 every kind of planks makes the basic wooden items. The owner asked at 13:15 why maple
planks make no chest and ruled that all wood should make chests, doors and so on, with the default item
where a wood has no own one.

The cause is Quark. Its Variant Chests and Variant Ladders (`config/quark-common.toml:138` and `:140`)
switch off the vanilla recipes `minecraft:chest`, `minecraft:trapped_chest` and `minecraft:ladder` and
add one recipe per wood instead. Quark's recipe `quark:building/crafting/chests/mixed_chest` gives a
plain chest only when the eight planks are of at least two kinds. So eight planks of a wood without a
Quark variant matched no recipe at all. That hit every Biomes O' Plenty wood, maple included, and the
woods of Tinkers' Construct, Botania, Thermal, Ad Astra and a few more. Ladders worked for only 17
kinds of planks. Some modded woods also come without doors, signs, buttons or boats.

The fix is data, not a script, so it loads the same way on a fresh start and on `/reload`:

- [kubejs/data/skytekx3/recipes/wood_fallback/](../../kubejs/data/skytekx3/recipes/wood_fallback) holds 12
  recipes with the vanilla shapes. Each takes the Forge ingredient `forge:difference`, the tag
  `#minecraft:planks` minus a skip tag.
- [kubejs/data/skytekx3/tags/items/wood_fallback_skip/](../../kubejs/data/skytekx3/tags/items/wood_fallback_skip)
  holds one skip tag per item. It lists the planks that already make their own version, so a fallback
  never competes with a wood's own recipe. Every entry is optional (`"required": false`), so a removed
  mod cannot break a tag.

| Recipe | Result | Kinds of planks it takes on 2026-11-03 |
|---|---|---|
| `chest` | 1 chest | 481 |
| `ladder` | 4 ladders, Quark's shape of six sticks around one plank | 494 |
| `door` | 3 oak doors | 452 |
| `trapdoor` | 2 oak trapdoors | 451 |
| `fence` | 3 oak fences | 450 |
| `fence_gate` | 1 oak fence gate | 451 |
| `sign` | 3 oak signs | 460 |
| `slab` | 6 oak slabs | 448 |
| `stairs` | 4 oak stairs | 448 |
| `pressure_plate` | 1 oak pressure plate | 455 |
| `button` | 1 oak button | 441 |
| `boat` | 1 oak boat | 474 |

Most of these planks are the 423 decorative variants of Chipped, which never had own versions. The
skip tag of the button also holds Quark's 14 vertical planks, because one vertical plank alone already
crafts back into normal planks. Crimson and warped planks now make oak boats, since vanilla has no
nether boats. A plain chest still becomes a trapped chest through Quark's recipe
`quark:building/crafting/chests/chest_trapped`. Hanging signs are not covered, they come from stripped
logs.

The skip lists come from a live dump of the server's crafting recipes on 2026-11-03 (511 kinds of
planks, 2,823 recipes for wooden items). A second scan of all 18,498 crafting recipes found no clash
except the vertical planks above. One overlap is left and does no harm: mixed planks of two woods
without own chests match both Quark's mixed chest and the fallback, and both give the same chest. A
temporary check after the `/reload` at 13:32 found all 12 recipes on the live server, for example
maple planks make a chest and ladders while spruce planks still make Quark's spruce chest.

To refresh the skip lists after a mod change, run the dump again: a temporary script in
`kubejs/server_scripts/` that walks `getAllRecipesFor(RecipeType.CRAFTING)`, loaded with RCON
`kubejs reload server_scripts` and deleted right after. Without a refresh, a wood that later gains its
own item still works, Polymorph then offers both results.

### Tag fixes

The file [tag_fixes.js](../../kubejs/server_scripts/tag_fixes.js) adds `ad_astra:venus_sandstone` to
the item and block tag `forge:sandstone/venus_sandstone`. Ad Astra's own compat recipes for Mekanism,
Create, Immersive Engineering and Thermal use that tag but never fill it, so the crushing of Venus
sandstone failed without it. The log shows one object added to each tag type at boot.

The same file strips the ice creams, milkshakes and cake slices of Twilight's Flavors & Delight out of
`farmersdelight:sweets`, `farmersdelight:snacks` and `diet:sugars` while Neapolitan is missing. Those
items exist only with Neapolitan, and their ids made the tag loader drop the three tags whole. It adds
the nine edible berries of the Twilight Forest backport to `forge:berries`, which Farmer's Delight and
Medieval Embroidery read. Since 2026-11-02 it also takes yellorium out of the uranium tags, see
[AlmostUnified](almostunified.md#yellorium).

### Apotheosis riders

The file [apotheosis_riders.js](../../kubejs/server_scripts/apotheosis_riders.js) (2026-11-04) fixes
the mounted bosses of Apotheosis 7.4.8. `ApothBoss.createBoss` builds the rider from NBT without a
position and moves only the mount to the spawn point, so the rider joins the world at x 0, y 0, z 0. It
would move onto its mount in its first tick as a passenger, but it only ticks while the chunk at the
origin is entity ticking. In this world that chunk is no spawn chunk (world spawn 308 64 392, and Ksyxis
removes the spawn chunks), so the rider never ticked.

The only mounted boss is the overworld Stray on a spider. A mob rider switches off the walking goals of
its mount, so players met a spider that did not walk, while the Stray boss waited at the origin. When the
spider died or despawned, the Stray dropped to x 0, z 0 at the height of the spider. The debug log shows
3 such Strays on 2026-11-02 at 13:26 and 12 on 2026-11-03 at 21:04. Those were gone by 22:02, and since
then a new one stands at 0 63 0. The copy of `world/entities/r.0.0.mca` read on 2026-11-04 holds it with
`apoth.boss`, `Spigot.ticksLived` 0 and its glowing effect still at 3600 ticks, so it never ticked.

The script listens to the join event of `minecraft:stray`. When the Stray rides a vehicle more than 8
blocks away, the vehicle places it on itself with `positionRider`. Forge fires that event before the
entity goes into a chunk section, so the rider is filed under the chunk of its mount and ticks from then
on. Each placement logs "Apotheosis riders: placed" with both positions, at most 100 per boot. After five
errors the script stops until the next reload. A Stray that already stands at the origin is no passenger
any more and stays there.

The nether miniboss Withering Archer has a related fault that the script leaves alone. Apotheosis
attaches its wither cloud while the archer joins the level. By then the game has already taken the list
of passengers to add, so the cloud rides the archer without being part of the level and never ticks.
The archer only gets its aura once its chunk has been saved and loaded again.

### Gameplay

The file [gameplay.js](../../kubejs/server_scripts/gameplay.js) only holds commented plans: a Quark rope
nerf and Asgard Shield giant sword recipes with ore tags. The comment uses the namespace
`asgardshieldreloaded`, but the mod id is `asr`. The loot tweaks of SkyTekx 2 (two rolls for cows and the
Hexxit World arrow drop nerf) were deferred to a `loot_tiers.js` that was never written.

### Recipe failures

From 2026-10-31 to 2026-11-03 every recipe load on the Mac mini ended with "with 13 failed recipes", and
three boots counted 14. KubeJS logs a failed recipe as "Error parsing recipe" and leaves it out of the
game. The log audit of 2026-11-04 traced every one of them:

| Recipe | Cause | State |
|---|---|---|
| `soulshards:soulsteeler` | Soul Shards 1.3.6 writes its Ender IO enchanter recipe in an old format, with the input item and an `amount`. Ender IO 6.2.18 reads `ingredient` and `count` ("Item cannot be null") | Fixed by an override, the Enchanter applies Soul Stealer for one corrupted ingot per level |
| `forbidden_arcanus:aurum_chest_boat` and `edelwood_chest_boat` | The chest is an object with `fabric_value` and `forge_value`, which Forge cannot read ("An ingredient entry needs either a tag or an item") | Fixed by overrides that take the tag `forge:chests/wooden` |
| One `tconstruct:tools/modifiers/slotless/embellishment/...` recipe, on 3 boots | The type `tconstruct:swappable_modifier` fills a static HashMap of Tinkers' Construct while KubeJS builds the recipes on several threads. The log shows only "null", the empty message of the ConcurrentModificationException that HashMap throws then | Fixed by [recipe_races.js](../../kubejs/server_scripts/recipe_races.js), which builds them on the main thread during the recipe event |
| `reliquary:alkahestry/crafting/netherrack`, at the boot of 2026-11-04 10:33 | The types `reliquary:alkahestry_crafting` and `reliquary:alkahestry_charging` add each recipe to a plain ArrayList of Reliquary 2.0.66 (`AlkahestryRecipeRegistry`) while it is built, so two threads can collide the same way. It first showed on the boot after the Tinkers recipes had moved to the main thread | Fixed by the same script since the boot of 2026-11-04 |
| `ancient_aether:ancient_rune`, `lost_aether_content:gravitite_shield` and three `aether_treasure_reforging:*_upgrade_smithing_template` | The built in `gravitite_ingot` pack of Aether Redux holds recipes for these three mods, which are not installed | Harmless, nothing to fix |
| `pamhc2foodcore:caramelcupcakeitem_x4`, `melonpieitem` and `honeymuffinitem` | Pam's HarvestCraft 2 Food Core 1.0.5 ships recipes for items it does not register | Harmless, nothing to fix |
| `galosphere:block_comparator` | Galosphere 1.5.2 ships the recipe but does not register the block | Harmless, nothing to fix |
| `bibliocraft:enchanted_book_cloning_in_printing_table` | The XP cost of BiblioCraft 1.3.1 uses a codec that refers to itself and can never decode ("Failed to decode from JSON") | Open. An override without the cost would make the cloning free, a real fix needs a patched jar |

With these fixes a boot logs "with 10 failed recipes", never 11.

Two more broken files of other mods load without counting as failed, and `kubejs/data/` overrides them
too:

- The stonecutter recipe `cataclysm:stonecutting/azure_seastone_brick_stair_from_stonecutting` of
  L_Ender's Cataclysm 3.31 gives `cataclysm:azure_seastone_brick_stair`, an item that does not exist.
  KubeJS warned "ItemStack 'result' can't be empty", and the vanilla reader turned the recipe into an
  empty button in the stonecutter. A `forge:false` condition switches it off. The correct recipe
  `azure_seastone_brick_stairs_from_stonecutting` stays.
- The barrel loot table `tf_dnv:chests/dungeon_shroom_barrel` of Dungeons & Villages nests a table that
  does not exist, see [Twilight Forest backport](twilightforest-backport.md).

An override has the id of the mod's file. KubeJS adds `kubejs/data/` after the mods and the world
datapacks, so the override always wins. The recipe overrides for Forbidden Arcanus and Soul Shards carry
`forge:mod_loaded` conditions, so a removed mod leaves no broken recipe behind. When a mod update fixes
its own file, delete the override.

## Open rulings

On 2026-06-27 21:46 the owner asked for loot chances roughly like SkyTekx 2 and not three Hexxit
flowers next to each other (`~/.claude/history.jsonl`, session 5bd3e1de). The flowers are done since
3f5eb2f2 with a worldgen override in the fixes pack, see
[boot fixes](../content/boot-fixes.md#gameplay-overrides). No script, LootJS rule or config changes the
loot chances yet. See [open items](../open-items.md).

## Working with Rhino

Rhino maps Java methods to JavaScript, and that mapping can go wrong. The first Skylands fall script
called the no argument accessors `level()`, `dimension()` and `location()`, which Rhino resolved
ambiguously. It threw a `TypeError` ("is not a function, it is object") every tick a player was below
Y 0, 126 times in one session log, and never teleported anyone (commit bf898e6). The script was
deleted, see [Skylands travel](../content/aether-fall.md#history).

The combat script shows a pattern that works: load the classes with `Java.loadClass` at the top and
resolve every constant at load time, so a wrong name fails at boot and not later in an event
(`combat_1_7.js:20-29`). Check every new script on boot (0 errors in `logs/kubejs/server.log`) and then
in game, because event handlers only fail when the event fires.

Run `/reload` to reload server scripts. It does not undo effects that were already applied to players,
such as attribute modifiers.

## Stale references

- The Nyx rows at `docs/RECIPE_PORT.md:52` and `:55` say Nyx is not on 1.20.1.
- The file `docs/RECIPE_PORT.md:3` points at UniDict and other SkyTekx 2 configs that are not in this
  repo. They are in `~/git/skytekx2-1122-server/config/`.

Related: [AlmostUnified](almostunified.md), [combat](../content/combat.md),
[mod set](mod-set.md), [boot fixes](../content/boot-fixes.md).
