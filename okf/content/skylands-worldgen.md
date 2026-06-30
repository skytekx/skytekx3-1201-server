---
type: Feature
title: Skylands worldgen
description: Custom surface_spread structure placement for the floating-islands dimension.
tags: [content, worldgen, skylands, datapack]
timestamp: 2026-06-30
---

# Skylands worldgen

The custom `skytekx3:skylands` floating-islands dimension and its structure placement.

## Custom placement type
A **source-built worldgen mod** registers `skytekx:surface_spread`, a `StructurePlacementType` (Mixin +
a heightmap gate) that only places a structure where the column's `WORLD_SURFACE_WG` height ≥
`min_surface_y`. This keeps "ground" structures on real island terrain.

- **Floaters** (the 22 custom `sky_*` jigsaws in `skylands_floaters`): `random_spread`, fixed-Y — they
  hang in the air. This set intentionally stays `random_spread` because its jigsaws have rigid, fixed-Y
  start pools and never project to the heightmap.
- **Ground / houses / villages / DA dungeons**: `skytekx:surface_spread`, projected to the heightmap.

## The y0 leak (fixed)
`surface_spread` only checked the **start-chunk center** column, so a structure whose footprint reached
an adjacent void column could still project to the y0 void floor. Fix: raise **`min_surface_y` 16 → 36**
so only solid island cores qualify, and ensure every skylands-eligible structure is on `surface_spread`
(none on plain `random_spread`). Probes then landed structures at Y 48–136 — no more y0/y1.

**2026-06-30 follow-up.** The invariant "none on plain `random_spread`" had two holdouts: the Dungeons
Arise native sets `dungeons_arise:major_structures` and `minor_structures` were still `random_spread`, so
the DA structures that gate into the skylands (keep_kayra, mechanical_nest, abandoned_temple) projected to
the y0 void floor — a player found a keep sitting on Y0. Both sets were converted to
`skytekx:surface_spread` (`min_surface_y` 36). In the overworld, `surface_spread` still lands them on the
ground surface (Y > 36), so overworld DA generation is unchanged. RULE for future work: any structure set
whose structures can gate into a skylands biome MUST use `skytekx:surface_spread`, never `random_spread`,
unless its structures are rigid fixed-Y floaters (only `skylands_floaters` qualifies).

## Structure density and friendly content (2026-06-30)
Tuned denser for island-to-island traversal (more stepping-stones and landmarks make the early game
crossable):

- `skylands_floaters` spacing **44 → 32** — more air stepping-stones between islands.
- `skylands_ground` spacing **18 → 14** — denser DA adventure houses on the island surfaces.
- `minecraft:villages` spacing **34 → 24** — villager houses common.

**Chocobos (chococraft) are skylands-only.** Custom biomes do not inherit a mod's biome-modifier spawns,
so chocobos never spawned here. Fix: a `forge:add_spawns` biome modifier
(`skytekx3:add_skylands_chocobos`) adds `chococraft:chocobo` at weight 30 (the most common creature) to
the 14 skylands biomes, while chococraft's own `add_plains/mountain/nether_chocobos` modifiers are
overridden to `forge:none` (off) and `add_gysahl_green` is retargeted to the skylands biomes — so the
whole chocobo mod, mob plus its food plant, lives only in the sky.

## Two copies — keep in sync
- [`skylands-datapack/`](../../skylands-datapack) — the **source** (structure sets + biome tags);
  `tools/build_mrpack.py` ships this into the `.mrpack`.
- [`world/datapacks/`](../../world/datapacks) — the **runtime** copy the live server reads.
Edit both, or changes won't propagate through the pack.

After any change, [regenerate the world](../server/world-reset.md).

Related: [aether fall](aether-fall.md), [world reset](../server/world-reset.md), [mod set](../modpack/mod-set.md).
