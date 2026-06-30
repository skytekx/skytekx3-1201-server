---
type: Log
title: Change log
description: Chronological history of major SkyTekx 3 milestones.
tags: [log, history]
timestamp: 2026-06-29
---

# Change log

Oldest → newest.

## 2026-06-27 — scaffold
Arclight 1.20.1 / Forge 47 / Java 17 hybrid stood up. The 1.12.2 recipe-consistency strategy was
ported: UniDict → [AlmostUnified](modpack/almostunified.md), NoMoreRecipeConflict → Polymorph,
CraftTweaker → [KubeJS](modpack/kubejs.md). Mod set assembled from
[`modrinth.index.json`](modpack/mod-set.md).

## 2026-06-29 — Nyx + worldgen overhaul + all-dimension regen (v3.0.4)
- **[Nyx](content/nyx.md)** fully ported to 1.20.1 (server + client + index, env `client`+`server` required).
- **[Skylands worldgen](content/skylands-worldgen.md)**: fixed the y0 void-floor structure leak
  (`min_surface_y` 16 → 36) and wired many more floating + ground structures.
- **[Aether-style fall](content/aether-fall.md)**: dropping off a Skylands island teleports you to the overworld.
- **[All dimensions regenerated](server/world-reset.md)** (same seed) + Chunky pregen of overworld + skylands.
- [AlmostUnified](modpack/almostunified.md) mod priorities finalised.
- [Published](modpack/publish.md) Modrinth v3.0.4 (project itself still in `draft`).

## 2026-06-29 — worldgen performance profile
Spark-profiled live chunk generation (overworld + Skylands) on the running server. Finding: lag is
**vanilla-noise-bound**, not memory and not a single mod. Vanilla terrain (~50–62 %) + JVM/GC overhead
(~30 %) dominate; the worst mod is Starlight (<1–3 %, itself a perf mod) and the custom Skylands worldgen
is invisible. No content mod is worth cutting — pregen wider instead. Full writeup:
[server/performance.md](server/performance.md).

## 2026-06-30 — Skylands structure-density follow-up
`/locate` sweep + a dense-region Spark profile found the Skylands **over-saturated**: one structure per
~210 blocks, fed by four overlapping layers (22 floaters + dense `skylands_ground` at spacing 18 + a
**redundant** raw-DA surface layer that duplicates 6 floaters + villages/swamp + two heavy Cataclysm
builds that do generate there). Per-structure spike verdict: **Cataclysm is not a CPU cost** (its code is
~0 %, all vanilla jigsaw assembly), but saturated regions generate ~2.4× slower and **triple GC**. Decisive
thinning is specified (widen `skylands_ground` 18→32, drop the raw-DA + Cataclysm Skylands biome tags) in
[server/performance.md §6](server/performance.md); left for the pack owner to apply (gameplay edit).

## 2026-06-30 — Skylands thinned (light touch, applied)
Owner chose the lowest-regret slice and it is now live. Removed the 6 raw-DA structures that duplicate a
sky floater (`aviary`, `bandit_towers`, `thornborn_towers`, `heavenly_conqueror`, `heavenly_challenger`,
`heavenly_rider`) and the 2 heaviest Cataclysm builds (`acropolis`, `soul_black_smith`) from the Skylands
by deleting their `has_structure` biome tags in both datapack copies (16 files). Kept the 22 floaters, the
`skylands_ground` DA layer, the villages and `sky_abandoned_temple`. Tag-only edit, so only not-yet-generated
chunks change and it is fully reversible. Verified after a restart: all 8 no longer `/locate` in the
Skylands but still generate in their native dimensions (End, Nether, overworld ocean or badlands), so
nothing was deleted from the game, only un-duplicated out of the sky. The bigger `skylands_ground` spacing
widen stays available for a later pass. Details: [server/performance.md §6.4](server/performance.md).

## 2026-06-30 — Skylands denser + chocobos + y0 dungeon fix
Reversed course on thinning per the owner: a densely-populated skylands actually helps island-to-island
traversal (structures are stepping-stones and landmarks). Tuned `skylands_floaters` 44→32,
`skylands_ground` 18→14 and `minecraft:villages` 34→24 so friendly content (adventure houses + villager
houses) is common. Made the whole chocobo mod skylands-only: a `forge:add_spawns` modifier adds
`chococraft:chocobo` (weight 30) to the 14 skylands biomes, chococraft's overworld/mountain/nether
spawn modifiers are overridden to `forge:none`, and gysahl green is retargeted to the skylands. FIXED a
y0 leak: `dungeons_arise:major_structures` and `minor_structures` were still `minecraft:random_spread`
(projecting DA dungeons onto the y0 void floor — a player found a keep at Y0), now converted to
`skytekx:surface_spread` (min_surface_y 36) like every other skylands set. Details:
[content/skylands-worldgen.md](content/skylands-worldgen.md).
