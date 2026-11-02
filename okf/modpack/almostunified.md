---
type: Reference
title: AlmostUnified
description: How AlmostUnified 0.11.0 collapses the duplicate metals, dusts and plates of the tech mods to one item per material, and how it fits with Polymorph and KubeJS.
tags: [modpack, recipes, unification]
timestamp: 2026-11-01
---

# AlmostUnified

Many tech mods in the pack each add their own copper ingot, tin dust or steel plate. AlmostUnified
picks one item per material and tag, rewrites recipe outputs to that item and drops the recipes that
become duplicates. It plays the role that UniDict played in SkyTekx 2.

On 2026-06-27 10:34 the owner asked that recipes never conflict, as in the old SkyTekx, and on
2026-06-29 13:59 they asked for one copper for everything (`~/.claude/history.jsonl`, session 5bd3e1de).

## How the recipe layers fit together

| Layer | Mod | Job |
|---|---|---|
| Unification | AlmostUnified 0.11.0 | One canonical item per material, duplicate recipes removed |
| Grid conflicts | Polymorph 0.49.11 (0.49.10 before the update of 2026-10-31) | Lets the player choose when two crafting recipes match the same grid |
| Explicit changes | KubeJS 2001.6.5 | Recipe and tag fixes by script, see [KubeJS](kubejs.md) |

EMI and JEI are both in the index as recipe viewers.

## Configuration

The config lives in [config/almostunified/](../../config/almostunified) (commit cb58258). The file
`unify.json` holds the rules.

- The list `modPriorities` decides whose item wins: minecraft, kubejs, crafttweaker, thermal, mekanism,
  immersiveengineering, create, enderio, industrialforegoing, railcraft, tconstruct (lines 2 to 14). All
  of them are installed except CraftTweaker, whose entry does nothing.
- The list `stoneStrata` covers ores in stone, andesite, deepslate, diorite, granite, nether and end.
- The list `tags` names 12 tag patterns, all in the `forge` namespace: nuggets, dusts, gears, gems,
  ingots, raw materials, ores, plates, rods, wires, storage blocks and raw storage blocks, each as
  `forge:<kind>/{material}`.
- The list `materials` names 47 materials: aeternium, aluminum, amber, apatite, bitumen, brass, bronze,
  charcoal, chrome, cinnabar, coal, coal coke, cobalt, constantan, copper, diamond, electrum,
  elementium, emerald, enderium, fluorite, gold, graphite, invar, iridium, iron, lapis, lead, lumium,
  mithril, netherite, nickel, obsidian, osmium, peridot, platinum, potassium nitrate, ruby, sapphire,
  signalum, silver, steel, sulfur, tin, tungsten, uranium and zinc.
- Recipes of the type `cucumber:shaped_tag` are left alone, and the setting `itemsHidingJeiRei` hides the
  items that lost to the priority list in the recipe viewer.
- The file `duplicates.json` keeps the default rules: when two recipes only differ in cooking time,
  energy or experience, the higher value wins. The file `startup.json` sets `serverOnly` to false.

The priority overrides, custom tags, ignored tags and ignored items are all empty.

## What it does at boot

The last boot on the build machine logged "Recipe count: 40492" and "Recipe count afterwards: 39987
(done in 675.8 ms)" (`logs/2026-07-12-1.log.gz`, 23:25:50 to 23:25:51). So AlmostUnified removed 505
duplicate recipes. It also logs one WARN that lists tag and material pairs that no mod defines, such
as `forge:gems/aluminum`. The pattern list creates them on purpose, and the WARN is harmless.

## Sides

The index marks AlmostUnified as optional on the client and required on the server. The recipes are
unified on the server. The hiding in the recipe viewer runs on the client, so it needs the mod there.
The owner's client has it. A client without it was not tested.

## Open points

- The planned priority list in `docs/RECIPE_PORT.md:17` (minecraft, thermal, mekanism, create,
  immersiveengineering) differs from the live one, which puts Immersive Engineering before Create and
  adds six more mods. The live file is the one that counts.
- Whether all 47 materials have more than one source in this pack was not checked. A material with a
  single source costs nothing.

Related: [KubeJS](kubejs.md), [mod set](mod-set.md), [boot fixes](../content/boot-fixes.md).
