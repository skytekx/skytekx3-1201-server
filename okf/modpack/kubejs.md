---
type: Reference
title: KubeJS
description: The nine KubeJS server scripts, the startup script and the data files of SkyTekx 3, what each one really does on 1.20.1, the recipe failures of the boot log, the unfinished port of the SkyTekx 2 CraftTweaker scripts and how to work with Rhino.
tags: [modpack, kubejs, recipes, scripting]
timestamp: 2026-11-05
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

The server scripts live in [kubejs/server_scripts/](../../kubejs/server_scripts). The only startup
script, [apotheosis_passengers.js](../../kubejs/startup_scripts/apotheosis_passengers.js) since
2026-11-04, holds the one Forge event of the Apotheosis riders fix, see
[Apotheosis riders](#apotheosis-riders). There are no client scripts, and the folder `assets/` is empty.
The folder `data/` holds the six uncrafting recipes of Twilight Tweaks, see
[Twilight Forest backport](twilightforest-backport.md), the wood fallbacks, see
[Wood fallbacks](#wood-fallbacks), the bosses and gear sets of
[Iron's Apothic Invaders](#irons-apothic-invaders), and overrides for broken recipes, loot tables and
loot modifiers of other mods, see [Recipe failures](#recipe-failures). The last boot on the build machine loaded 4 of
4 scripts with 0 errors and 0 warnings (`logs/kubejs/server.log:15`). The boot of the Mac mini on
[Wood fallbacks](#wood-fallbacks), overrides for broken recipes, loot tables and loot modifiers of other
mods, see [Recipe failures](#recipe-failures), and the EMI hide list, see
[AlmostUnified](almostunified.md#emi-hide-list). The last boot on the build machine loaded 4 of 4
scripts with 0 errors and 0 warnings (`logs/kubejs/server.log:15`). The boot of the Mac mini on
2026-11-02 at 17:33 loaded 7 of 7, again with 0 errors and 0 warnings.

| Script | What it does |
|---|---|
| Recipe fixes, `recipe_fixes.js` | Adds two recipes and gives the Chaotic Shield Capacity of Draconic Evolution its Awakened Core, the rest is commented out |
| Tag fixes, `tag_fixes.js` | Mends tags of Ad Astra, Twilight's Flavors & Delight and the Twilight Forest backport, keeps yellorium out of the uranium tags, lets every sulfur dust count as yellow dye and puts the Draconic Evolution tools into the vanilla tool tags |
| Recipe fixes, `recipe_fixes.js` | Adds two recipes, removes Thermal Extra's iron rod and makes five recipes give the preferred item, the rest is commented out |
| Tag fixes, `tag_fixes.js` | Mends tags of Ad Astra, Twilight's Flavors & Delight and the Twilight Forest backport, keeps yellorium out of the uranium tags and lets every sulfur dust count as yellow dye |
| Gameplay, `gameplay.js` | Nothing yet, every line is a comment |
| Combat, `combat_1_7.js` | Removes the attack cooldown, see [combat](../content/combat.md) |
| Apotheosis riders, `apotheosis_riders.js` | Puts the riders of Apotheosis bosses on their mounts and adds the wither cloud of the Withering Archer to the level, see [Apotheosis riders](#apotheosis-riders) |
| Apotheosis passengers, `startup_scripts/apotheosis_passengers.js` | Discards the wither cloud of a Withering Archer that despawns, see [Apotheosis riders](#apotheosis-riders) |
| Skylands sleep, `skylands_sleep.js` | Turns the overworld to day when players sleep in the Skylands, see [Skylands worldgen](../content/skylands-worldgen.md#sleep-and-hostile-mobs) |
| Skylands portals, `skylands_portals.js` | Lets nether portals work in the Skylands, see [Skylands worldgen](../content/skylands-worldgen.md#nether-portals) |
| Unify inventory, `unify_inventory.js` | Swaps other variants of a material for the preferred item, and the five old blocks of Chocolate Quest for the normal blocks, see [AlmostUnified](almostunified.md#inventory-converter) and [Old blocks](../content/old-blocks.md) |
| Recipe races, `recipe_races.js` | Builds three recipe types of Tinkers' Construct and Reliquary on the main thread, because they write into shared state of their mod while they are built, see [Recipe failures](#recipe-failures) |

The recipe summary of that boot reads "Added 2 recipes, removed 0 recipes, modified 14 recipes, with 17
failed recipes" (`logs/kubejs/server.log:54`, 23:25:52). The 17 failures are third-party recipes that
name items which do not exist in this pack. They are not caused by these scripts. The boot of the Mac
mini on 2026-11-02 at 17:33 logged "Added 2 recipes, removed 0 recipes, modified 9 recipes, with 13
failed recipes". Those 13 failures are listed under [Recipe failures](#recipe-failures). A test server
with the mods of 3.1.3 and the fixes of 2026-11-04 logged "Added 7 recipes, removed 6 recipes, modified
9 recipes, with 10 failed recipes" on 2026-11-04 at 22:55.

### Recipe fixes

The file [recipe_fixes.js](../../kubejs/server_scripts/recipe_fixes.js) has two live recipes and, since
2026-11-04, three fixes for one item per material:

- The recipe `skytekx3:flesh_to_leather` smelts rotten flesh into leather (line 9).
- The recipe `skytekx3:bookshelf_any_plank` crafts a bookshelf from any planks (lines 12 to 16). The
  script first removes `minecraft:bookshelf`, but that removal matches nothing: Quark's variant
  bookshelves are on (`config/quark-common.toml:137`), and Quark ships its own `minecraft:bookshelf`
  recipe that switches itself off when they are on. That is why the log says "removed 0 recipes". The
  added recipe overlaps Quark's per wood bookshelf recipes, and Polymorph lets the player choose.
- The recipe `thermal_extra:crafting/iron_rod` is removed (line 21). It took two iron ingots on top of
  each other like the rod of Immersive Engineering and made 2 rods where that one makes 4. After
  AlmostUnified both gave the same rod, so Polymorph offered 2 or 4 of it.
- Spartan Weaponry smelts and blasts its copper and steel weapons into the first nugget of a tag,
  which is a variant. Its four recipes are removed and made again, such as
  `skytekx3:copper_nugget_from_smelting_copper_weapons`, with the nuggets of Thermal and Mekanism and
  the same cooking times and experience (lines 23 to 38).
- The Alkahestry Tome of Reliquary doubled steel into the first ingot of the tag, the one of Immersive
  Engineering. The recipe `skytekx3:alkahestry/crafting/steel_ingot` takes and gives the steel of
  Mekanism instead (lines 40 to 48).

Since 2026-11-05 it also swaps the Chaotic Core in the recipe
`draconicevolution:modules/item_chaotic_shield_capacity` for an Awakened Core with a `replaceInput`, as
Draconic Evolution did on its 1.21 branch in commit 0c85f0d9. It stays out of the jar of SkyTekx DE
Fixes, so that no data file of Draconic Evolution ships, see
[Draconic Evolution fixes](draconic-fixes.md#license). A test server with Draconic
Evolution and KubeJS showed the Awakened Core in the bottom middle of that recipe on 2026-11-05.

The commented blocks wait for the right item ids:

- Angel Ring (lines 56 to 58): the SkyTekx 2 rule kept it loot only, now meant for Excessive Utilities.
- Hexical diamond (lines 60 to 64): the comment says Nyx's fallen star is gone. Nyx is back and
  registers `nyx:fallen_star` (see [Nyx](../content/nyx.md)). The commented code also uses the namespace
  `hexxitgear`, but the installed Hexxit Gear (`hexxit_gear-1.0.1.jar`) uses `hexxit_gear`.
- Fresh water for HarvestCraft 2 (lines 66 and 67).

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

Since 2026-11-04 it adds the tag `forge:dusts/sulfur` to `forge:dyes/yellow`. Mekanism lists its own
sulfur dust as a yellow dye, so the yellow dye recipes of other mods took that dust. AlmostUnified
prefers the sulfur dust of Thermal, and the [inventory converter](almostunified.md#inventory-converter)
turns the Mekanism dust into it, but Thermal does not list its dust as a dye. A recipe scan on the test
server on 2026-11-04 found 160 recipes that took the Mekanism dust and not the Thermal dust, all of them
through this tag and all of them next to yellow dye itself. A `replaceInput` would have swapped that
whole ingredient, so those recipes would have lost yellow dye, and it does not reach every machine
recipe. After the change and a `/reload` the tag held yellow dye and the sulfur dusts of Thermal,
Mekanism, Immersive Engineering, Railcraft and Blood Magic, and the same scan of 37,011 recipes found
0 recipes that take one dust and not the other, against 165 that take both. The recipe summary stayed at
"Added 2 recipes, removed 0 recipes, modified 9 recipes, with 10 failed recipes".

Since 2026-11-05 it puts the Wyvern, Draconic and Chaotic tools of Draconic Evolution into the vanilla
tags for pickaxes, axes, shovels, swords and hoes, and both staffs into the first four, the tag set of
commit 4d360c95 on the 1.21 branch of Draconic Evolution. Like the recipe above it stays out of the
jar of SkyTekx DE Fixes, see [Draconic Evolution fixes](draconic-fixes.md#license). The block runs only
with Draconic Evolution loaded. A test server with Draconic Evolution and KubeJS found every tool and
both staffs in their tags on 2026-11-05 and loaded the script with 0 errors.

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

The nether miniboss Withering Archer had a related fault. Its definition
`minibosses/the_nether/withering_archer.json` in Apotheosis gives a skeleton or wither skeleton that
spawns in the nether a chance of 0.02, spawners and spawn eggs excluded. Its miniboss NBT carries an
area effect cloud of wither with a radius of 2.5 blocks and a `Duration` of 200000 ticks as a passenger.
Apotheosis mounts that cloud in its own join event of the archer. The spawn adds the archer with
`addFreshEntityWithPassengers`, and `/summon` does the same, which has already read the empty
passenger list of the mob before that event runs. So the cloud rode the archer without being part of
the level and never ticked. A test archer on the test server on 2026-11-04 at 12:54 kept its cloud at
`Age` 0, the cloud's own position stayed at x 0, y 0, z 0, `execute if entity` found no cloud next to
the archer, and a zombie standing in the cloud got no effect. The archer only got its aura once its
chunk had been saved and loaded again.

Since 2026-11-04 the script also listens to the join events of `minecraft:skeleton` and
`minecraft:wither_skeleton`. One tick later it adds every passenger of the mob that is not part of the
level yet, placed on the seat of the mob, and logs "Apotheosis riders: added the
minecraft:area_effect_cloud of ...", at most 100 times per boot. It waits the tick because the KubeJS
join event fires before the one of Apotheosis. It cannot read the miniboss marker instead, because
`persistentData` in KubeJS is not the ForgeData of the entity. Passengers that a chunk loads from disk
are part of the level already and get no second copy.

A real cloud brings a new fault along. Apotheosis discards the cloud when its archer dies, but a mob that
despawns only drops its passengers, so the wither cloud would stay behind on its own for up to 200000
ticks. The startup script `apotheosis_passengers.js` listens to the Forge event `EntityMountEvent`. When
an area effect cloud stops riding a skeleton or wither skeleton, it discards the cloud, unless the
skeleton is only being unloaded with its chunk, which saves the cloud as its passenger. Each discard
logs "Apotheosis passengers: discarded the cloud of ...". KubeJS 6 registers Forge events only from
startup scripts, so a change to that file takes a restart.

The test on 2026-11-04 between 13:01 and 13:03 used the test server and its `logs/kubejs/server.log`.
A new wither skeleton archer at 698 100 646 in the nether got its cloud added. The cloud was part of the
level, rode the archer and counted `Age` 106, 127 and 534 over the next seconds. A zombie in the cloud
had wither (amplifier 0, about 900 ticks). It lost no health in about 20 seconds inside the cloud and 3
of its 20 health within 6 seconds after it stepped out. The cloud renews the effect every 20 ticks
(`ReapplicationDelay` 20), and wither at amplifier 0 only hurts when its remaining duration is a
multiple of 40 ticks, which a renewed effect never reaches. So the aura hurts once a mob leaves it. The
same code runs for players, but no player tested it. A skeleton archer got its cloud the same way. After
the nether chunks were unloaded, saved with `save-all flush` and loaded again, each of the two archers
had exactly one cloud, and the old archer from before the fix had its cloud ticking now as well. Killing
an archer removed its cloud. A Stray boss built with `ApothBoss.createBoss` was still placed from 0 0 0
onto its spider and ticked (`Spigot.ticksLived` 31 after 2 seconds).

After the restart at 13:26 the old archer still had exactly one cloud, and the startup script logged
"Apotheosis passengers: active". At 13:27 a probe discarded a new archer and, as a control, a zombie
that carried a plain area effect cloud, the way a despawn removes a mob. The cloud of the archer went
with it ("Apotheosis passengers: discarded the cloud of a minecraft:wither_skeleton that left the level
at 700 100 648"). The cloud of the zombie stayed in the level without a vehicle, which is what every
despawned archer would have left behind. Another unload and reload of the chunks kept the cloud of the
old archer, and the startup script discarded nothing during it.

### Iron's Apothic Invaders

Iron's Apothic Invaders 1.0.0 is a data mod for Minecraft 1.21.1 that makes mobs of Iron's Spells 'n
Spellbooks into invader bosses of Apotheosis 8. Its jar holds no classes, its Modrinth project lists
MIT and its mods.toml says Unlicense. Since 2026-11-05 the folder
[kubejs/data/irons_apothic_invaders/](../../kubejs/data/irons_apothic_invaders) holds its 10 bosses
and 23 gear sets, converted to the formats of Apotheosis 7.4.8 and Placebo 8.6.3, and the MIT text of
the jar travels in [licenses/](../../licenses/irons_apothic_invaders-1.0.0-LICENSE.txt). Every file
carries a `forge:mod_loaded` condition on `irons_spellbooks`. The 9 gear sets under `various/` of the
jar are byte identical copies of the top level ones and were left out.

- The overworld gets the Archevoker, Cryomancer, Cultist, Magehunter Vindicator, Necromancer, Priest
  and Pyromancer, the Nether a Citadel Keeper (upstream calls it the ancient knight) and a Necromancer,
  and the End a Magehunter Vindicator. Each takes the rarity range of the vanilla bosses of its
  dimension in Apotheosis 7.4.8: uncommon to rare, rare to epic and epic to mythic. Each keeps the
  upstream weight of 100, so Iron's mobs hold 700 of 1,140 weight points in the overworld, 200 of 580
  in the Nether and 100 of 295 in the End (`data/apotheosis/bosses/` of the Apotheosis jar).
- Apotheosis 8 picks the gear by world tier, and 7.4.8 has no tiers. Bosses with the same gear in every
  tier keep it. The Citadel Keeper takes the keeper sets of the tiers frontier and ascent. The keeper
  sets of haven, summit and pinnacle ship, but no boss names them. The Citadel Keeper keeps its upstream
  NBT `AngerTime` 99999999.
- Minecraft 1.20.1 has no scale attribute, so the size modifiers are left out. The projectile damage
  of Apothic Attributes 2 becomes the spell power of Iron's Spells, because these casters hurt with
  spells, and the projectile attributes of Apothic Attributes 1.3.7 cover arrows only. The uniform ranges become step
  functions with a step of 1 for whole numbers and 0.01 otherwise. The primary enchanting level goes to
  the affix item and the secondary one to the other gear, capped at 30 for a world without the
  enchanting module (`ApothBoss` reads the four values in that order).
- The item `cursed_doll_spellbook` of the 1.21 build is `cursed_doll_spell_book` in Iron's Spells
  3.16.3. The summit netherite keeper set lists the Legionnaire Flamberge as a helmet, and the
  conversion moves it to the main hand. The durability bonus component of Apotheosis 8 has no
  counterpart and is left out. The protection enchantments of the enchanted keeper sets stay as item
  NBT.
- A Forge 47.4.18 test server with Apotheosis 7.4.8, Apothic Attributes 1.3.7, Placebo 8.6.3, Iron's
  Spells 3.16.3, KubeJS and their libraries registered 27 bosses and 39 gear sets on 2026-11-05. That
  is the 17 bosses and 16 gear sets of Apotheosis that need no Twilight Forest, plus these 10 and 23. A
  test script then spawned each of the 10 at four rarities. Every one came with its entity, a generated
  name, raised health, its gear set and enchanted gear, and the casters with a spell power between 1.24
  and 2.13. The log held no error of the conversion. The Magehunter Vindicator joins the world as a
  `minecraft:vindicator` holding a Magehunter, because Iron's Spells builds it on the vanilla type.
  Nobody met one in a natural invasion yet.

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
| `bibliocraft:enchanted_book_cloning_in_printing_table` | The XP cost of BiblioCraft 1.3.1 uses a codec that refers to itself and can never decode ("Failed to decode from JSON") | Fixed by a class patch in `tools/patch_mods.py` since 2026-11-04, see [Boot fixes](../content/boot-fixes.md#bibliocraft-cloning-cost) |

With these fixes a boot logs "with 9 failed recipes", never 10. The test boots of 2026-11-04 at 13:15
and 15:18 logged "Added 2 recipes, removed 0 recipes, modified 9 recipes, with 9 failed recipes", and
so did the live boot of 16:06 that day.

Three more broken files of other mods do not count as failed recipes, and `kubejs/data/` overrides them
too:

- The stonecutter recipe `cataclysm:stonecutting/azure_seastone_brick_stair_from_stonecutting` of
  L_Ender's Cataclysm 3.31 gives `cataclysm:azure_seastone_brick_stair`, an item that does not exist.
  KubeJS warned "ItemStack 'result' can't be empty", and the vanilla reader turned the recipe into an
  empty button in the stonecutter. A `forge:false` condition switches it off. The correct recipe
  `azure_seastone_brick_stairs_from_stonecutting` stays.
- The barrel loot table `tf_dnv:chests/dungeon_shroom_barrel` of Dungeons & Villages nests a table that
  does not exist, see [Twilight Forest backport](twilightforest-backport.md).
- The loot modifier `aether_redux:genesis_golden_swet_ball_increase` in the built in `genesis_data`
  pack of Aether Redux 2.0.19 has the type `aether_redux:add_drops` but the fields `added_item` and
  `removed_item` of the type `aether_redux:replace`. Forge logged "Could not decode GlobalLootModifier
  ... No key item in MapLike" at every boot and left it out. The owner ruled on 2026-11-04 that golden
  swets drop more. The override keeps `add_drops` and names the ball under `item`, so a golden swet
  drops its usual golden swet ball plus 0 or 1 more and up to 1 more per level of Looting. On the test
  server on 2026-11-04 the log showed no decode error for it after a `/reload` and after a restart.
  Kills with a probe gave 1.00 balls per kill without Looting before (394 balls in 394 kills) and 1.50
  after (595 in 396), and 2.48 with Looting III before (985 in 397) and 4.39 after (1,715 in 391). The
  same probe after the restart gave 1.53 (604 in 396) and 4.44 (1,750 in 394). The type
  `aether_redux:replace` alone would have taken the usual ball away and given 0.50 balls per kill
  without Looting (198 in 394). The class `AddDrops` of Redux adds the item stack of the modifier
  itself to the drops, without a copy. Two golden swets killed within a moment could therefore change
  the count of an extra ball that still lies apart on the ground. That follows from the code, nobody saw
  it in game.

An override has the id of the mod's file. KubeJS adds `kubejs/data/` after the mods and the world
datapacks, so the override always wins. The recipe overrides for Forbidden Arcanus and Soul Shards carry
`forge:mod_loaded` conditions, so a removed mod leaves no broken recipe behind. When a mod update fixes
its own file, delete the override.

## Open rulings

On 2026-06-27 21:46 the owner asked for loot chances roughly like SkyTekx 2 and not three Hexxit
flowers next to each other (`~/.claude/history.jsonl`, session 5bd3e1de). The flowers are done since
9a784372 with a worldgen override in the fixes pack, see
[boot fixes](../content/boot-fixes.md#gameplay-overrides). No script, LootJS rule or config changes the
loot chances yet. See [open items](../open-items.md).

## Working with Rhino

Rhino maps Java methods to JavaScript, and that mapping can go wrong. The first Skylands fall script
called the no argument accessors `level()`, `dimension()` and `location()`, which Rhino resolved
ambiguously. It threw a `TypeError` ("is not a function, it is object") every tick a player was below
Y 0, 126 times in one session log, and never teleported anyone (commit 241117c). The script was
deleted, see [Skylands travel](../content/aether-fall.md#history).

The combat script shows a pattern that works: load the classes with `Java.loadClass` at the top and
resolve every constant at load time, so a wrong name fails at boot and not later in an event
(`combat_1_7.js:20-29`). Check every new script on boot (0 errors in `logs/kubejs/server.log`) and then
in game, because event handlers only fail when the event fires.

Run `/reload` to reload server scripts. It does not undo effects that were already applied to players,
such as attribute modifiers.

The probes of 2026-11-04 on the test server found more names that behave differently in this setup:

- The KubeJS property `persistentData` and the method `getPersistentData()` return the data that KubeJS
  keeps for the entity, not the Forge data that mods such as Apotheosis write.
- The UUID of an entity comes from `getUuid()`. The method `getUUID()` is not found.
- The method `hurt` and fields such as `tickCount` are not reachable. The NBT of the entity (`entity.nbt`)
  gives `Age` or `Spigot.ticksLived`, and the vanilla command `damage` deals damage.
- The call `server.getLevel(key)` is ambiguous. Walk `server.getAllLevels()` instead and read the
  dimension of each level through a helper that takes `dimension` as a property when it is one, like
  `apothRidersCall` in `apotheosis_riders.js`.
- Inside `execute ... run` a command needs its plain name, for example `summon` and not
  `minecraft:summon`. At the top level of RCON, the prefix still keeps EssentialsX away from `give` and
  `clear`.
- The method `scheduleInTicks` of the server, `addFreshEntity`, `positionRider`, `getPassengers` and
  `getEncodeId` work as named.

## Stale references

- The Nyx rows at `docs/RECIPE_PORT.md:52` and `:55` say Nyx is not on 1.20.1.
- The file `docs/RECIPE_PORT.md:3` points at UniDict and other SkyTekx 2 configs that are not in this
  repo. They are in `~/git/skytekx2-1122-server/config/`.

Related: [AlmostUnified](almostunified.md), [combat](../content/combat.md),
[mod set](mod-set.md), [boot fixes](../content/boot-fixes.md).
