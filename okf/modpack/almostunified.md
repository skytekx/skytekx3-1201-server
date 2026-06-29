---
type: Reference
title: AlmostUnified
description: Material/tag unification — the modern replacement for the 1.12.2 UniDict.
tags: [modpack, recipes, unification]
timestamp: 2026-06-29
---

# AlmostUnified

Collapses every mod's duplicate ores/ingots/dusts/etc. to **one canonical item per material**, so the
many tech mods (Thermal, Mekanism, Immersive Engineering, Create, EnderIO, Industrial Foregoing,
Railcraft, Tinkers') don't each contribute a different "copper". This is the modern UniDict.

## Config
[`config/almostunified/unify.json`](../../config/almostunified) — `modPriorities` decides which mod's
item wins for each unified tag:

```
["minecraft","kubejs","crafttweaker","thermal","mekanism","immersiveengineering",
 "create","enderio","industrialforegoing","railcraft","tconstruct"]
```

Unifies the `c:*_ores` / `_ingots` / `_nuggets` / `_dusts` / `_plates` / `_gears` / `_rods` /
`_raw_materials` / `_storage_blocks` tag families.

## Where it sits in the engine
One of three layers (see [modpack index](index.md)): AlmostUnified (collapse duplicates) → Polymorph
(in-grid conflict cycling) → [KubeJS](kubejs.md) (remove duplicate processing recipes, gate progression).

Related: [KubeJS](kubejs.md), [mod set](mod-set.md).
