---
type: Feature
title: Skylands worldgen
description: Custom surface_spread structure placement for the floating-islands dimension.
tags: [content, worldgen, skylands, datapack]
timestamp: 2026-06-29
---

# Skylands worldgen

The custom `skytekx3:skylands` floating-islands dimension and its structure placement.

## Custom placement type
A **source-built worldgen mod** registers `skytekx:surface_spread`, a `StructurePlacementType` (Mixin +
a heightmap gate) that only places a structure where the column's `WORLD_SURFACE_WG` height ≥
`min_surface_y`. This keeps "ground" structures on real island terrain.

- **Floaters** (towers, nests, fortresses, `cataclysm:acropolis`, …): `random_spread`, fixed-Y — they
  hang in the air; added to each `has_structure/*_biomes` tag for the 14 skylands biomes.
- **Ground / houses / villages**: `skytekx:surface_spread`, projected to the heightmap.

## The y0 leak (fixed)
`surface_spread` only checked the **start-chunk center** column, so a structure whose footprint reached
an adjacent void column could still project to the y0 void floor. Fix: raise **`min_surface_y` 16 → 36**
so only solid island cores qualify, and ensure every skylands-eligible structure is on `surface_spread`
(none on plain `random_spread`). Probes then landed structures at Y 48–136 — no more y0/y1.

## Two copies — keep in sync
- [`skylands-datapack/`](../../skylands-datapack) — the **source** (structure sets + biome tags);
  `tools/build_mrpack.py` ships this into the `.mrpack`.
- [`world/datapacks/`](../../world/datapacks) — the **runtime** copy the live server reads.
Edit both, or changes won't propagate through the pack.

After any change, [regenerate the world](../server/world-reset.md).

Related: [aether fall](aether-fall.md), [world reset](../server/world-reset.md), [mod set](../modpack/mod-set.md).
