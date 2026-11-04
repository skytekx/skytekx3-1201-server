---
type: Reference
title: AlmostUnified
description: How AlmostUnified 0.11.0 collapses the duplicate metals, dusts and plates of the tech mods to one item per material, how an inventory converter and the ore overrides carry that into the world, and why yellorium stays apart.
tags: [modpack, recipes, unification]
timestamp: 2026-11-04
---

# AlmostUnified

Many tech mods in the pack each add their own copper ingot, tin dust or steel plate. AlmostUnified
picks one item per material and tag, rewrites recipe outputs to that item and drops the recipes that
become duplicates. It plays the role that UniDict played in SkyTekx 2.

On 2026-06-27 10:34 the owner asked that recipes never conflict, as in the old SkyTekx, and on
2026-06-29 13:59 they asked for one copper for everything (`~/.claude/history.jsonl`, session 5bd3e1de).
On 2026-11-02 16:17 they asked for only one item of each material, one that builds everything (session
c0e872d8), see [One item per material](#one-item-per-material).

## How the recipe layers fit together

| Layer | Mod | Job |
|---|---|---|
| Unification | AlmostUnified 0.11.0 | One canonical item per material, duplicate recipes removed |
| Inventories | KubeJS script `unify_inventory.js` | Swaps the other variants a player carries for the canonical item |
| Grid conflicts | Polymorph 0.49.11 (0.49.10 before the update of 2026-10-31) | Lets the player choose when two crafting recipes match the same grid |
| Explicit changes | KubeJS 2001.6.5 | Recipe and tag fixes by script, see [KubeJS](kubejs.md) |

EMI and JEI are both in the index as recipe viewers.

## Configuration

The config lives in [config/almostunified/](../../config/almostunified) (commit cb58258). The file
`unify.json` holds the rules.

- The list `modPriorities` decides whose item wins: minecraft, kubejs, crafttweaker, thermal, mekanism,
  immersiveengineering, create, enderio, industrialforegoing, railcraft, tconstruct and twilightforest
  (lines 2 to 15). Twilight Forest went last in 50fb5f0f. All of them are installed except CraftTweaker,
  whose entry does nothing.
- The list `stoneStrata` covers ores in stone, andesite, deepslate, diorite, granite, nether and end.
- The list `tags` names 12 tag patterns, all in the `forge` namespace: nuggets, dusts, gears, gems,
  ingots, raw materials, ores, plates, rods, wires, storage blocks and raw storage blocks, each as
  `forge:<kind>/{material}`.
- The list `materials` names 47 materials: aeternium, aluminum, amber, apatite, bitumen, brass, bronze,
  charcoal, chrome, cinnabar, coal, coal coke, cobalt, constantan, copper, diamond, electrum,
  elementium, emerald, enderium, fluorite, gold, graphite, invar, iridium, iron, lapis, lead, lumium,
  mithril, netherite, nickel, obsidian, osmium, peridot, platinum, potassium nitrate, ruby, sapphire,
  signalum, silver, steel, sulfur, tin, tungsten, uranium and zinc.
- The list `ignoredItems` holds `bigreactors:.*` since 2026-11-02 (6a73d7be), so no Extreme Reactors
  item takes part, see [Yellorium](#yellorium).
- Recipes of the type `cucumber:shaped_tag` are left alone, and the setting `itemsHidingJeiRei` hides the
  items that lost to the priority list in the recipe viewer.
- The file `duplicates.json` keeps the default rules: when two recipes only differ in cooking time,
  energy or experience, the higher value wins. The file `startup.json` sets `serverOnly` to false.

The priority overrides, custom tags and ignored tags are empty.

## What it does at boot

The last boot on the build machine logged "Recipe count: 40492" and "Recipe count afterwards: 39987
(done in 675.8 ms)" (`logs/2026-07-12-1.log.gz`, 23:25:50 to 23:25:51). So AlmostUnified removed 505
duplicate recipes. It also logs one WARN that lists tag and material pairs that no mod defines, such
as `forge:gems/aluminum`. The pattern list creates them on purpose, and the WARN is harmless.

On the Mac mini the boot of 2026-11-02 13:22 went from 41844 to 41338 recipes. Since Extreme Reactors
is ignored, the boots and reloads from 17:20 on go from 41844 to 41344.

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

KubeJS fires its chest event only for the vanilla chest menu. Iron Chests, shulker boxes, backpacks and
machines therefore keep their items until a player takes them out. The log names the first 50 swaps
after each script load, such as "Unify inventory: 6 railcraft:tin_raw became thermal:raw_tin in the
inventory of ...".

The script went live at 17:16:53 without a restart. Its first swaps turned a player's 6 Railcraft and 4
Mekanism raw tin into Thermal raw tin. Between 17:22 and 17:28 the chests the owner opened gave up raw
lead, nickel, silver, tin and zinc and the sulfur dust of Railcraft, the raw silver of Immersive
Engineering and the raw tin and lead of Mekanism.

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
- Railcraft's saltpeter and Thermal's niter sit in different tags and stay two items.
- Some rod tags, `forge:rods/*`, hold only items of Create Crafts & Additions and Thermal Extra.
  Neither mod is in `modPriorities`, so those rods stay apart.

## Sides

The index marks AlmostUnified as optional on the client and required on the server. The recipes are
unified on the server. The hiding in the recipe viewer runs on the client, so it needs the mod there,
and the client reads its own copy of `config/almostunified/`. The owner's client has the mod. Its copy
of `unify.json` was behind since 50fb5f0f and got the file of 6a73d7be on 2026-11-02 at 17:38.
AlmostUnified reads that copy again whenever the client receives the tags of the server, at each join
and after a `/reload`. Other clients get the file with the next pack. A client without the mod was not
tested.

## Open points

- The planned priority list in `docs/RECIPE_PORT.md:17` (minecraft, thermal, mekanism, create,
  immersiveengineering) differs from the live one, which puts Immersive Engineering before Create and
  adds seven more mods. The live file is the one that counts.
- The [limits](#limits) above that are still open and the 7,000 unread machine recipes are also on
  [Open items](../open-items.md).

Related: [KubeJS](kubejs.md), [mod set](mod-set.md), [boot fixes](../content/boot-fixes.md).
