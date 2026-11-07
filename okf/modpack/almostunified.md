---
type: Reference
title: AlmostUnified
description: How AlmostUnified 0.11.0 collapses the duplicate metals, dusts and plates of the tech mods to one item per material, how an inventory converter, the ore overrides and an EMI hide list of 285 variants carry that into the world, what the audit of the merged mod set found, and why yellorium stays apart.
tags: [modpack, recipes, unification]
timestamp: 2026-11-07
---

# AlmostUnified

Many tech mods in the pack each add their own copper ingot, tin dust or steel plate. AlmostUnified
picks one item per material and tag, rewrites recipe outputs to that item and drops the recipes that
become duplicates. It plays the role that UniDict played in SkyTekx 2.

On 2026-06-27 10:34 the owner asked that recipes never conflict, as in the old SkyTekx, and on
2026-06-29 13:59 they asked for one copper for everything (`~/.claude/history.jsonl`, session 5bd3e1de).
On 2026-11-02 16:17 they asked for only one item of each material, one that builds everything (session
c0e872d8), see [One item per material](#one-item-per-material). On 2026-11-04 at 22:34 they asked again
for one plate per material, with the variants that cannot be built out of sight, see
[EMI hide list](#emi-hide-list).

## How the recipe layers fit together

| Layer | Mod | Job |
|---|---|---|
| Unification | AlmostUnified 0.11.0 | One canonical item per material, duplicate recipes removed |
| Inventories | KubeJS script `unify_inventory.js` | Swaps the other variants a player carries for the canonical item |
| Recipe viewer | Tag `c:hidden_from_recipe_viewers` in `kubejs/data/` | Leaves the other variants out of the item list of EMI |
| Grid conflicts | Polymorph 0.49.11 (0.49.10 before the update of 2026-10-31) | Lets the player choose when two crafting recipes match the same grid |
| Explicit changes | KubeJS 2001.6.5 | Recipe and tag fixes by script, see [KubeJS](kubejs.md) |

EMI and JEI are both in the index as recipe viewers.

## Configuration

The config lives in [config/almostunified/](../../config/almostunified) (commit cb58258). The file
`unify.json` holds the rules.

- The list `modPriorities` decides whose item wins: minecraft, kubejs, crafttweaker, thermal,
  mekanism, immersiveengineering, create, enderio, industrialforegoing, railcraft, tconstruct,
  twilightforest, createaddition, thermal_extra and bigreactors (lines 2 to 18). Twilight Forest went
  last in 50fb5f0f. The last three joined on 2026-11-04: Create Crafts & Additions and Thermal Extra are
  the only mods with copper, electrum and gold rods, and Extreme Reactors brings its graphite. All of
  them are installed except CraftTweaker, whose entry does nothing.
- The list `stoneStrata` covers ores in stone, andesite, deepslate, diorite, granite, nether and end.
- The list `tags` names 12 tag patterns, all in the `forge` namespace: nuggets, dusts, gears, gems,
  ingots, raw materials, ores, plates, rods, wires, storage blocks and raw storage blocks, each as
  `forge:<kind>/{material}`. Since 2026-11-04 it also names `forge:coal_coke`, `forge:silicon` and
  `forge:slag`, tags without a kind that the patterns never reach. Thermal, Immersive Engineering and
  Railcraft each make their own coal coke and slag, and AE2 and Ender IO their own silicon.
- The list `materials` names 53 materials: aeternium, aluminum, amber, apatite, bitumen, brass,
  bronze, charcoal, chrome, cinnabar, coal, coal coke, cobalt, constantan, copper, diamond, electrum,
  elementium, emerald, ender pearl, enderium, fluorite, gold, graphite, invar, iridium, iron,
  knightmetal, lapis, lead, lumium, mithril, netherite, nickel, obsidian, osmium, peridot, platinum,
  potassium nitrate, quartz, ruby, salt, saltpeter, sapphire, signalum, silver, steel, sulfur, tin,
  tungsten, uranium, wood and zinc. Ender pearl, knightmetal, quartz, saltpeter and wood joined on
  2026-11-04 for the dusts of ender pearl, quartz, saltpeter and wood and the knightmetal ingots and
  blocks, which two or three mods made each. Salt joined on 2026-11-05 (4b3774b) with the Expanded
  Delight port, so the salt of that mod and of Pam's Food Core gives way to the Mekanism salt, see
  [Expanded Delight port](expandeddelight-port.md#pack-fit). Since 2026-11-06 `tag_fixes.js` also puts
  the salt of Medieval Embroidery into `forge:dusts/salt` (3891eb8), so it gives way as well, see
  [Audit of the merged mod set](#audit-of-the-merged-mod-set).
- The map `priorityOverrides` gives the knightmetal ingots and blocks to Twilight Forest, which would
  lose them to Tinkers' Construct, and silicon to AE2, whose recipes use it most.
- The map `itemTagInheritance` lets a preferred item take five tags that only its variants had and
  that recipes or the game ask for: `forge:plates` for every plate, `minecraft:coals` for coal coke,
  `minecraft:beacon_payment_items` for the ingots, `minecraft:piglin_loved` for gold dust and plates and
  `minecraft:trim_materials` for steel, see [EMI hide list](#emi-hide-list).
- The list `ignoredTags` holds `forge:storage_blocks/quartz`, which also holds seven quartz blocks of
  Botania that would otherwise turn into the quartz block of Minecraft.
- The list `ignoredItems` holds `bigreactors:(?!graphite_).*` since 2026-11-04, every Extreme Reactors
  item but its graphite ingot, dust and block. From 2026-11-02 (6a73d7be) it held all of them, see
  [Yellorium](#yellorium).
- Recipes of the type `cucumber:shaped_tag` are left alone, and the setting `itemsHidingJeiRei` hides
  the items that lost to the priority list in JEI and REI. EMI has its own list, see
  [EMI hide list](#emi-hide-list).
- The file `duplicates.json` keeps the default rules: when two recipes only differ in cooking time,
  energy or experience, the higher value wins. The file `startup.json` sets `serverOnly` to false.

The custom tags and tag ownerships are empty. AlmostUnified reads `unify.json` again at every `/reload`,
and a test server picked up a change of the inherited tags that way on 2026-11-04.

## What it does at boot

The last boot on the build machine logged "Recipe count: 40492" and "Recipe count afterwards: 39987
(done in 675.8 ms)" (`logs/2026-07-12-1.log.gz`, 23:25:50 to 23:25:51). So AlmostUnified removed 505
duplicate recipes. It also logs one WARN that lists tag and material pairs that no mod defines, such
as `forge:gems/aluminum`. The pattern list creates them on purpose, and the WARN is harmless.

On the Mac mini the boot of 2026-11-02 13:22 went from 41844 to 41338 recipes. Since Extreme Reactors is
ignored, the boots and reloads from 17:20 on go from 41844 to 41344. A test server with the mods of
3.1.3 and the config of 2026-11-04 went from 42986 to 42477 recipes. Forge test boots of the merged tree
of 2026-11-06 went from 54281 to 53762 on the boot of 20:57 and from 54473 to 53954 on the boot of 21:11
(`packboot-logs/regen-base-forge-latest.log` and `regen-r1-forge-latest.log` in the scratch folder of
the session). The second boot had 192 more generated melting files, which AlmostUnified counts before
their conditions, and it removed 519 recipes both times.

## One item per material

AlmostUnified only changes recipes. Ores, block drops, chest loot and items made earlier still come as
the item of the mod that made them, so two stacks of the same metal did not stack. On 2026-11-02 at
16:02 the owner asked why their copper was the wrong one for brass plates, and at 16:17 they asked for
one item per material. At 17:09 they approved the three steps below.

A probe at 16:20 checked all 36,990 recipes on the server against the 138 groups of AlmostUnified that
hold two items or more. Every recipe it could read, about 29,800, takes every variant of a material,
apart from the yellow dye recipes, fixed since 2026-11-04, and yellorium, see [Limits](#limits) and
[Yellorium](#yellorium).
About 7,000 machine recipes, from Mekanism and the Thermal press among others, do not show their
ingredients to such a probe and were not checked. The preferred items are Thermal's for bronze, tin and
the copper nuggets, plates and gears, Create's for brass and zinc and Mekanism's for uranium.

### Inventory converter

The script [unify_inventory.js](../../kubejs/server_scripts/unify_inventory.js) (8f46a335) swaps every
other variant of a unified material for the preferred item at the same count. It asks the replacement
table of AlmostUnified itself, so the result fits every recipe that the variant fitted. It acts one tick
after an item lands in a player's inventory, when a player logs in, for the inventory and the ender
chest, and when a player opens a container with the vanilla chest menu, such as a chest, a barrel or an
ender chest. Swapped items join the stacks of the preferred item that are already there. Items with NBT
stay as they are, and so does everything from Extreme Reactors.

Since 2026-11-04 the script also swaps the five old decoration blocks of Chocolate Quest for
cobblestone, the block of iron, diamond ore, glass and obsidian. Its own table `UnifyOldBlocks` answers
before AlmostUnified is asked, see [Old blocks](../content/old-blocks.md). It went live with the deploy
of 3.1.3 on 2026-11-05.

KubeJS fires its chest event only for the vanilla chest menu. Iron Chests, shulker boxes, backpacks and
machines therefore keep their items until a player takes them out. The log names the first 50 swaps
after each script load, such as "Unify inventory: 6 railcraft:tin_raw became thermal:raw_tin in the
inventory of ...".

The script went live at 17:16:53 without a restart. Its first swaps turned a player's 6 Railcraft and 4
Mekanism raw tin into Thermal raw tin. Between 17:22 and 17:28 the chests the owner opened gave up raw
lead, nickel, silver, tin and zinc and the sulfur dust of Railcraft, the raw silver of Immersive
Engineering and the raw tin and lead of Mekanism.

### EMI hide list

At 22:12 on 2026-11-04 the owner asked why two iron ingots on top of each other offer 2 or 4 iron rods,
at 22:27 why they could not make zinc sheets, and at 22:34 for one plate per material, with the other
variants out of sight. A dump of a test server with the mods of 3.1.3 by
[tools/unify_audit_dump.js](../../tools/unify_audit_dump.js), read by
[tools/unify_audit.py](../../tools/unify_audit.py), found this in its 38,071 recipes:

- The rods were the only crafting grid with one item in two counts. Immersive Engineering makes 4 rods
  and Thermal Extra 2, and AlmostUnified turns both into the rod of Immersive Engineering. Since then
  `recipe_fixes.js` removes the Thermal Extra recipe, see [KubeJS](kubejs.md#recipe-fixes).
- The zinc sheet of Create Crafts & Additions is a variant of the zinc plate of Railcraft. Every
  recipe that made a sheet makes the plate, so the sheet has no recipe left, and every recipe that takes
  a sheet takes the plate too, except the plate casts of Tinkers' Construct and a banner blueprint of
  Immersive Engineering. They ask for `forge:plates`, which only the sheet had.
- Five recipes gave a variant, because they pick their result from a tag while they run: the smelting
  and blasting of the copper and steel weapons of Spartan Weaponry and the steel of the Alkahestry Tome
  of Reliquary. `recipe_fixes.js` makes them again with the preferred item.
- The dusts of ender pearl, quartz, saltpeter and wood, knightmetal, the copper, electrum and gold
  rods, coal coke, silicon, slag and graphite still came from two or three mods each, see
  [Configuration](#configuration).
- The steel trim of Tinkers' Construct took only Tinkers' steel and the graphite trim of Tinkers'
  Reforged only its own graphite, both variants. The fixes pack names the preferred items, see
  [Boot fixes](../content/boot-fixes.md#gameplay-overrides). The knightmetal trim of Tinkers' Construct
  still takes its own ingot, but Twilight Forest has a knightmetal trim of its own.

After the changes the check found 282 variants, no crafting grid with one item in two counts, no result
that is a variant and one ingredient without its preferred item. That is the melting recipe of Tinkers'
Construct for copper ores of one ore rate, which takes the copper ores of Ad Astra and Basic Nether Ores
and not that of Minecraft, which has a melting recipe for dense ores. Of the 7,116 recipes in the first
dump that do not show their ingredients, 6,871 have a file in a mod or datapack, and the other 245 are
mostly AlmostUnified's own trackers, Ars Nouveau enchantments and smithing recipes. Two of the files
name a variant, the cutting fluids of Blood Magic with its own saltpeter. On the test server both take
all three saltpeters, because AlmostUnified swaps the item for its tag while it loads.

EMI does not use the hiding of AlmostUnified, which works in JEI and REI only. It leaves the items of
the tag `c:hidden_from_recipe_viewers` out of its item list, and tags come from the server, so the list
reaches every client without a new pack. The file
[kubejs/data/c/tags/items/hidden_from_recipe_viewers.json](../../kubejs/data/c/tags/items/hidden_from_recipe_viewers.json)
named the 282 variants, each one optional, so the mod set of 3.1.2 on the Mac mini loads it too. Other
mods put 15 items of their own into the same tag. Since the audit of the merged mod set it names 285,
see [Audit of the merged mod set](#audit-of-the-merged-mod-set). The audit writes the file, nobody edits it by hand.
After a change to the mods or to `unify.json`, run the dump on a test server and then
`python3 -I tools/unify_audit.py <dump> --server <test server> --hidden kubejs/data/c/tags/items/hidden_from_recipe_viewers.json`.

The check of 2026-11-04 also listed 1,206 pairs of crafting recipes that share a grid and give two different items,
where Polymorph lets the player choose. In 29 of them the two items have the same name: the emerald
tools and armor of Cyclic and Medieval Embroidery, the steel armor of Mekanism Tools and Railcraft, the
gold bars of Cyclic, Quark and Tinkers' Construct, the gold chains and lanterns of BiblioCraft and
Cyclic, the crop crates of Farmer's Delight and Quark and a few blocks. Each is an item of its own with
its own stats or looks, so they stay until the owner decides, see
[Open items](../open-items.md#owner-decisions).

### Audit of the merged mod set

The merge of master into the port branch on 2026-11-06 brought together the mods of 3.1.3 and
those of the branch: Actually Additions, Expanded Delight, Mystical Automation, Mekanism: Extra Modules,
Create: Integrated Farming with Create: Dragons Plus, Bygone Nether, LittleTiles, Extreme Reactors
2.0.97 and the forks of Ender IO and Create Crafts & Additions. Since the rebase of 2026-11-07 the branch sits on
master instead, and 5c2c6f3, the rebased copy of the first parent of the merge, has the merged tree. The dump
script needed the fix of
3bb9339 first, since Create: Dragons Plus adds an accessor `getRecipes()` to the recipe manager that
returns the map of recipes by type. On a Forge test boot of the merged tree that started at 20:57, the dump of
21:00 then held 48,197 recipes, 273 unify tags and 595 tagged items, and the audit found:

- 284 variants, the 282 of 3.1.3 and the salts of Expanded Delight and Pam's Food Core, which became
  variants of the Mekanism salt when salt joined the materials (4b3774b).
- No other new variant. The new mods put their own materials only into tags of their own: the black
  quartz gem, ore and block and the twelve crystal blocks of Actually Additions, the alloys of Ender
  IO, the metals of Extreme Reactors and the cinnamon of Expanded Delight. The electrum ingot, nugget,
  plate and block and the copper and electrum wires of the Create Crafts & Additions fork stay
  variants of the items of Thermal and Immersive Engineering, as with 1.3.3. Black quartz, canola and
  the crystals have no twin in a tag that AlmostUnified reads.
- Two rices. Actually Additions and Farmer's Delight each have their own, and they share only
  `forge:crops/rice`, a crop tag outside the patterns of `unify.json`. Of the 56 ingredients of the
  second boot that take rice, 23 take both, 26 only that of Farmer's Delight and 7 only that of
  Actually Additions. They stay apart until the owner decides, see
  [Open items](../open-items.md#owner-decisions).
- The salt of Medieval Embroidery sat in no tag at all, and its 14 recipes named it directly. Since
  3891eb8 it is a fourth variant of the Mekanism salt, so these recipes take every salt, and the salt
  ore block of the mod gives nine Mekanism salt.
- One ingredient without its preferred item, the copper ore melting of Tinkers' Construct, and no
  result that is a variant, as on 2026-11-04.
- 1,218 shared crafting grids with two different items, ten of them the cinnamon wood set of Expanded
  Delight against the [wood fallbacks](kubejs.md#wood-fallbacks). Since 506781f the cinnamon planks
  sit in ten skip tags. No grid gives one item in two counts or two variants, and no pair of items of
  the same name involves a mod of the branch.
- 15 tags that a variant has and its preferred item lacks. One is new, `forge:salt/salt` of Pam's salt.
  Only the tag file `forge:salt` of Pam's mod uses it, and that tag holds the Mekanism salt through
  `tag_fixes.js`. The other 14 come from mods of 3.1.3, such as `forge:wires/all_metal` of the wires
  of Create Crafts & Additions, which 1.3.3 shipped as well and no file uses.

A second boot at 21:11 ran with the salt of Medieval Embroidery, the skip tags and the new hide list.
Its dump of 21:14 held 48,380 recipes and 596 tagged items. The audit found 285 variants, the same single
ingredient, no result that is a variant and 1,208 grids with two different items, none of them a
cinnamon one, and wrote the hide list that the boot had loaded, 285 variants (859ecb7). The tag
`c:hidden_from_recipe_viewers` held 300 items on that boot, the 285 variants and the 15 items of other
mods. The outputs are in `regen/audit/` of the scratch folder of the session.

### One ore per metal

New chunks get one ore per metal. The fixes pack
[world/datapacks/skytekx3_fixes/](../../world/datapacks/skytekx3_fixes) replaces 16 biome modifiers
with `forge:none` since the restart of 17:20 (7ad5fffd). They are the lead, nickel (three), silver
(two), sulfur (two), tin (two) and zinc ores of Railcraft and the lead, nickel (two), silver and uranium
ores of Immersive Engineering. A probe of the overworld biomes after the restart found all 16 features
gone and none new.

Mekanism's tin and lead are off in `config/Mekanism/world.toml` (7c7ef1cd). That file is a server
config, which Forge writes back from memory at every stop. The copy of 17:19 was lost in the stop of
17:20, so the switch holds only since the restart of 17:32, see
[Logs and crashes](../server/logs-and-crashes.md#mekanisms-ore-switch-lost-at-a-stop-2026-11-02-172012).

The ores of Thermal, Create and Mekanism that AlmostUnified prefers stay, and so do the ores no other
mod has: the bauxite and mineral veins of Immersive Engineering and the firestone, quarried stone and
saltpeter of Railcraft. The nether ores of Basic Nether Ores stay too. Chunks generated before keep
their duplicate ores, and the converter swaps what they drop once a player picks it up.

### Yellorium

Extreme Reactors lists its yellorium ingot, dust and ore in the uranium tags. AlmostUnified took
yellorium for uranium and made the recipes give Mekanism uranium, so raw yellorium smelted to Mekanism
uranium. Since the restart of 17:20 `ignoredItems` holds `bigreactors:.*` (6a73d7be), and raw yellorium
smelts to yellorium again. The recipe count after unification rose from 41338 to 41344.

The uranium recipes of other mods still took yellorium through the tags, and for yellorite ore the
furnace could pick the recipe of Immersive Engineering, which gives Mekanism uranium. Since a datapack
reload at 17:23 [tag_fixes.js](../../kubejs/server_scripts/tag_fixes.js) takes the three items out of
the uranium tags (f8308011). The command `kubejs list_tag` then showed no yellorium in them. Outside
Extreme Reactors only the Tinkers' casts, the yellorium seeds of Mystical Agriculture and one Industrial
Foregoing recipe take yellorium, and none of them turns it into uranium.

Extreme Reactors keeps working. Each of its six recipes that ask for the uranium tag has a twin that
asks for yellorium, and its reactors take yellorium fuel through `forge:ingots/yellorium` as well.

Since 2026-11-04 the pattern is `bigreactors:(?!graphite_).*`. The graphite ingot, dust and block of
Extreme Reactors take part again and win over those of Tinkers' Reforged, and yellorium stays out.

Extreme Reactors 2.0.97 also lists its deepslate yellorite ore, raw yellorium, the raw yellorium block and
the yellorium block in the item tags of uranium. Since 2026-11-05 tag_fixes.js takes them out as well.
Its own recipes name these items directly. A Forge 47.4.18 test boot with Extreme Reactors, ZeroCore,
Mekanism, Patchouli and KubeJS then listed only Mekanism items in the six uranium tags.

### Limits

- The yellow dye recipes took only Mekanism's sulfur dust, and the converter turns that dust into
  Thermal's. Mekanism lists its dust in `forge:dyes/yellow`, Thermal does not. Since 2026-11-04
  `kubejs/server_scripts/tag_fixes.js` adds `#forge:dusts/sulfur` to that tag, and a scan on the test
  server found 0 recipes that take one sulfur dust and not the other, see
  [KubeJS](kubejs.md#tag-fixes).
- Epic Knights 10.15 registers its bronze, tin and silver ingots only on Fabric. The class `ModItems`
  checks `Platform.isFabric()` before it registers `bronze_ingot`, `tin_ingot` and `silver_ingot`, and a
  probe on the test server on 2026-11-04 found no item `magistuarmory:bronze_ingot`. Its 27 recipe files
  that need bronze take `#forge:ingots/bronze`, which holds the bronze ingots of Thermal, Mekanism and
  Railcraft, so they already take the preferred ingot. Nothing needs to go into the tag.
- Since 2026-11-04 the saltpeters of Railcraft and Blood Magic turn into that of Immersive
  Engineering. Thermal's niter sits in its own tag, `forge:dusts/niter`, and stays apart.
- The copper, electrum and gold rods came only from Create Crafts & Additions and Thermal Extra, and
  neither mod was in `modPriorities`, so those rods stayed apart until both joined the list on
  2026-11-04. The rods of Create Crafts & Additions win.
- Tags that only variants carry and that no recipe asks for stay as they are, such as seven `c:` tags
  of Ad Astra like `c:steel_ingots`, the ore rate and stone tags of some ores and
  `apotheosis:boon_drops`. The audit lists them in its fourth check.

## Sides

The index marks AlmostUnified as optional on the client and required on the server. The recipes are
unified on the server. The hiding in the recipe viewer runs on the client, so it needs the mod there,
and the client reads its own copy of `config/almostunified/`. The owner's client has the mod. Its copy
of `unify.json` was behind since 50fb5f0f and got the file of 6a73d7be on 2026-11-02 at 17:38.
AlmostUnified reads that copy again whenever the client receives the tags of the server, at each join
and after a `/reload`. Other clients get the file with the next pack. A client without the mod was not
tested. The EMI hide list is a tag of the server, so it needs no AlmostUnified on the client.

## Open points

- The planned priority list in `docs/RECIPE_PORT.md:17` (minecraft, thermal, mekanism, create,
  immersiveengineering) differs from the live one, which puts Immersive Engineering before Create and
  adds seven more mods. The live file is the one that counts.
- The [limits](#limits) above that are still open, the 245 recipes without a file to read and the 29
  pairs of same-named items are also on [Open items](../open-items.md).

Related: [KubeJS](kubejs.md), [mod set](mod-set.md), [boot fixes](../content/boot-fixes.md).
