---
type: Runbook
title: Performance and worldgen profiling
description: Spark profile of live chunk generation — the lag is vanilla-noise-bound, not memory, no single mod is the villain, but the Skylands is over-saturated with structures.
tags: [performance, worldgen, profiling, spark]
timestamp: 2026-06-30
---

# Performance and worldgen profiling

Profiled the **running** server (Arclight 1.20.1, Forge 47.4.18) under forced fresh worldgen to find
what makes chunk loading lag. Short version: **the cost is vanilla terrain noise + JVM overhead, not a
mod.** There is no mod to delete for a worldgen win. Pregen wider instead. A later follow-up
([section 6](#6-skylands-structure-density-and-the-per-structure-spike-2026-06-30-follow-up)) found one
real, structure-side win: the **Skylands is over-saturated** and the densest/redundant/heaviest sets can
be thinned for a GC and chunk-gen reliability gain.

## 1. Memory is *not* the bottleneck (confirmed)

| Fact | Value |
|---|---|
| Physical RAM | 62 GB, ~23 GB available |
| Server heap | `-Xms4G -Xmx8G` (8 GB max) |
| "Swap" | zram — compressed RAM, not disk. Its `used` figure is normal and harmless. |
| Idle TPS | 20.0 across 5s/10s/1m/5m/15m |
| Idle MSPT | 0.2 / 0.3 / 0.4 / 2.3 ms (10s), 0.2 / 0.3 / 1.5 / 8.8 ms (1m) |

The heap is comfortable and there is no disk swapping. Lag during exploration is **worldgen-bound**: the
server is CPU-busy generating chunks, not starved for memory. During the profiled generation TPS held at
**20.0** the whole time — Chunky throttles itself to protect the tick, so worldgen cost shows up as a
backlog/latency for the player crossing fresh terrain, not as a TPS drop on the gauge.

## 2. How it was profiled

Spark (`spark profiler start --thread *`, async engine, 4 ms interval), capturing **all** threads while
[Chunky](world-reset.md) was forced to generate **fresh, un-pregenned** chunks far from the pregenned
core (which is only ±1000 from spawn):

- **Overworld** — squares at `30000,30000` (r160) then `60000,60000` (r480); ~31 chunks/s.
  Report: <https://spark.lucko.me/jI5VCGQTd6> (~156 s, 60 threads, 203,957 nodes).
- **Skylands** (`skytekx3:skylands`, the custom dim players explore) — `40000,40000` (r480) then
  `80000,80000` (r640); ~115 chunks/s (mostly void, so much cheaper per chunk).
  Report: <https://spark.lucko.me/1JnrEfYpST> (~115 s, 34 threads, 29,752 nodes).

Self-time was attributed per source from spark's embedded `class_sources` map (the "Sources" view): each
stack frame's exclusive time is charged to the mod that owns the running class.

## 3. Where the time goes (Spark Sources, self-time)

Both dimensions tell the same story. Worldgen runs on the `Worker-Main` pool (**67–69 %** of all
samples); the rest is GC + JIT + chunk IO. By *source*:

| Source | Overworld | Skylands | What it is |
|---|---:|---:|---|
| **Minecraft (vanilla)** | **49.6 %** | **61.7 %** | Terrain noise (Simplex/Perlin/Improved), aquifers, density functions, biome climate R-tree, ore placement |
| **libjvm.so (native)** | 23.8 % | 17.9 % | JVM itself: G1 GC, C2 JIT compilation, native glue |
| **JDK stdlib** | 11.5 % | 8.3 % | `java.util` / `java.lang.Math` called *from* the gen above |
| libzip / libc / native | ~4 % | ~5 % | Jar/zip reads, C runtime |
| **Starlight** | 0.85 % | 3.02 % | Lighting engine — a **performance mod** (faster than vanilla light) |
| ModernFix | 0.82 % | 0.52 % | Performance mod |
| Biolith | 0.63 % | 0.05 % | Biome-placement library (BoP / TerraBlender backend) |
| Zeta + Quark | ~0.97 % | — | Quark + its lib |
| YUNG's API | 0.23 % | 0.35 % | Structure framework (Better Dungeons/Mineshafts/Strongholds) |
| TerraBlender | 0.19 % | — | Biome region manager |
| **skytekx-worldgen (custom)** | <0.01 % | <0.01 % | Our `surface_spread` placement + the Skylands floaters (then ~14, now 22) — **invisible** |

Top *classes* by self-time are almost entirely vanilla noise: `Aquifer$NoiseBasedAquifer` (8.4 %,
overworld), `SimplexNoise` (4.9 % / 17.9 %), `ImprovedNoise`, `PerlinNoise`, `Mth`, the
`Climate$RTree` biome lookup (~5.5 %, overworld), `NoiseChunk` / `DensityFunctions`, `OreFeature`.

## 4. Worst offender — verdict

**There is no villain mod.** The single biggest mod-attributed cost is **Starlight** (0.85 % overworld,
3.0 % Skylands) and it is a *performance* mod — removing it would make lighting **slower**, not faster.
Next are ModernFix and FerriteCore, also perf mods. Everything else mod-side is noise (<1 %).

- **Removing any one content mod buys <1 % of worldgen time** while costing real content. Not worth it.
- The one place mods *indirectly* inflate a vanilla cost is the **biome climate R-tree** (~5.5 % of the
  overworld pass): Biomes O Plenty + TerraBlender + Biolith register a large biome list, which enlarges
  the multi-noise search vanilla runs per column. But that R-tree code is vanilla, the cost is small, and
  BoP is core pack identity — cutting it loses dozens of biomes for a few percent. **Keep it.**
- Structure mods (When Dungeons Arise, L_Ender's Cataclysm, YUNG's, Repurposed Structures) barely register
  on *average* (<0.25 %) because structures are sparse, but each places in one burst — so if a player
  reports a **stutter** specifically when a new structure spawns, those are the suspects, not the average.

Honest bottom line: this is the inherent cost of a 213-mod 1.20.1 pack doing **vanilla noise terrain**.
The fix is operational, not subtractive.

## 5. Concrete fixes

The worldgen-acceleration mods are **already installed** — this is why generation is as fast as it is:

- **Noisium 2.3.0**, **Saturn 0.1.3**, **ChunkOpt 1.0.2** — speed up the noise/feature gen that dominates.
- **FerriteCore 6.0.1**, **ModernFix 5.27.51**, **Starlight 1.1.2** — memory + lighting. Keep all of them.

So the levers that remain are operational:

1. **Pregen wider.** The pregenned core is only ±1000. Extend it so players rarely hit live worldgen —
   `chunky radius 5000`+ per dimension (overworld + `skytekx3:skylands`). See [world reset](world-reset.md).
2. **Lower `view-distance` / `simulation-distance`** in `server.properties` (currently both **10**). Each
   step down cuts the per-tick chunk-load fan-out a player drags around. 8/6 is a comfortable server pair.
3. **Don't cut content mods for worldgen** — the profile shows no payoff. If you *must* trim, the biome
   libs (BoP/TerraBlender/Biolith) are the only ones touching a measurable vanilla cost, and even they are
   ~5 % at most and load-bearing for the biome set.
4. **The `ruins` mod was removed** (commit `ac261f1`). It used to throw a `RuinsPositionsFile.txt` flush
   crash roughly every 5 min (an IO/threading bug, not worldgen CPU) that spammed the log — that noise is
   now gone.

Related: [Arclight runtime](arclight.md), [world reset / pregen](world-reset.md), [deploy](deploy.md).

## 6. Skylands structure density and the per-structure spike (2026-06-30 follow-up)

After the first pass, two things changed: the `ruins` mod was removed and the `skylands_floaters` set
grew from 14 to **22** entries (7 new When Dungeons Arise + 1 Cataclysm `abandoned_temple`). Re-audited
specifically for (a) how crowded the Skylands actually is and (b) whether any single structure — the fear
being **L_Ender's Cataclysm** — spikes chunk-gen.

### 6.1 What actually generates in the Skylands

`/locate` runs each structure's generation-point check, so a hit means it really generates there. Sweeping
`/locate` (via `execute in skytekx3:skylands positioned … run locate structure …`) from four far centers
shows the Skylands is fed by **four overlapping layers**, not just the 22 floaters:

| Layer | Set / placement | Spacing (chunks) | Lived density (empirical `/locate`) |
|---|---|---:|---|
| Floaters (22 custom `sky_*`) | `skytekx3:skylands_floaters` random_spread | 44 / 12 | any floater ~every **335 blocks** (avg) |
| Ground DA (10) | `skytekx3:skylands_ground` surface_spread | **18 / 6** | one ~every **288 blocks** — densest layer |
| Raw DA majors (8) | `dungeons_arise:major_structures` random_spread | 60 / 50 | present, **6 of 8 duplicate a floater** |
| Villages + swamp hut | `minecraft:villages` / `swamp_huts` surface_spread | 34 / 32 | plains/savanna/taiga + swamp present |
| Cataclysm (2) | `cataclysm:acropolis` / `soul_black_smith` | 80 / 60 | both present (smith located as close as 57 b) |

Empirical **nearest-any-structure from a random point: 32 / 48 / 128 / 248 blocks** across four bases.
Theoretical combined density (sum of `1/spacing²` over the active sets) ≈ **one structure per ~210 blocks**
— about 2.5× denser than the vanilla overworld village grid (one per ~544 blocks). The Skylands is
**over-saturated**: the floaters cannot feel special when there is a DA hut or a village every few hundred
blocks under them.

Two concrete problems:

- **Redundant raw-DA layer.** The DA `has_structure/*_biomes` tags were edited `replace:false` to add the
  14 Skylands biomes, so the raw `dungeons_arise:` majors place on island surfaces *in addition to* the
  custom floaters. Six exist BOTH as a `sky_*` floater AND as a raw surface build (`aviary`,
  `bandit_towers`, `thornborn_towers`, `heavenly_conqueror`, `heavenly_challenger`, `heavenly_rider`) —
  pure duplication.
- **Heavy Cataclysm builds are in the sky.** `cataclysm:acropolis` (spacing 80) and
  `cataclysm:soul_black_smith` (spacing 60) both generate in the Skylands — their `*_biomes` tags include
  the Skylands biomes — despite the intent that the big non-jigsaw Cataclysm builds would not float.

### 6.2 The per-structure spike — measured

Profiled fresh generation of a structure-dense Skylands region (`120000,120000`, r480, ~3,700 chunks):
<https://spark.lucko.me/fwMefVlnFM>, compared to the earlier sparse Skylands pass.

| Metric | Sparse Skylands | Dense Skylands |
|---|---:|---:|
| Worldgen throughput (Chunky) | ~115 chunks/s | **~47 chunks/s** |
| GC (GC Thread + G1 Conc + Refine) | ~14 % | **~39 %** |
| `libjvm.so` (native, mostly GC/JIT) | 17.9 % | 41.4 % |
| Vanilla structure/jigsaw/template assembly | 0.44 % | 1.40 % |
| **Cataclysm own code** | 0.01 % | **0.00 %** |
| **DungeonsArise own code** | 0.00 % | **0.00 %** |

**Cataclysm verdict: not guilty on CPU.** Cataclysm (and DA) structures are data-driven — assembled by
*vanilla* jigsaw/template code — so the mods' own code is ~0 %, and even total structure assembly is only
1.4 % in a saturated region. No single structure is a CPU bomb.

But the saturated region generates **~2.4× slower** and **triples GC pressure** (≈39 % of samples vs
≈14 %). The cost of over-saturation is not CPU in one method — it is allocation churn (jigsaw template
block-infos, palettes, block-state writes) feeding the garbage collector, plus the block-volume of big
builds. More concurrent heavy placements = more GC pauses + slower chunk gen = exactly the chunk-loading
stalls behind the earlier reliability incident.

### 6.3 Decisive fix — thin the Skylands (performance + reliability)

The Skylands does not need a structure every ~210 blocks. These are **spacing/tag-only** edits, so they
affect only **not-yet-generated** chunks — non-destructive and reversible. Apply in BOTH
`skylands-datapack/` and the runtime `world/datapacks/skytekx3_skylands/`, then
[regen/pregen](world-reset.md):

1. **Widen the densest layer.** `skylands_ground` spacing **18 → 32**, separation **6 → 10**. Cuts the
   dominant ground-structure density by ~68 % (one per ~288 → ~512 blocks, in line with villages).
2. **Drop the redundant raw-DA layer.** Remove the 14 Skylands biomes from the 8 DA `has_structure/*_biomes`
   tags (`aviary`, `bandit_towers`, `thornborn_towers`, `heavenly_conqueror`, `heavenly_challenger`,
   `heavenly_rider`, `mechanical_nest`, `keep_kayra`). The `sky_*` floaters already provide that content in
   the air; the raw surface copies are duplication.
3. **Pull the heavy Cataclysm builds out of the Skylands.** Remove the Skylands biomes from
   `cataclysm:acropolis_biomes` and `cataclysm:soul_black_smith_biomes`. These are the largest builds by
   block-volume and do not fit the floating-island theme. The intended Cataclysm presence, the
   `sky_abandoned_temple` floater, stays.
4. **Optional:** `skylands_floaters` spacing **44 → 50** to make the floaters rarer and more special.

Projected combined density after (1)+(2)+(3): ~one structure per **~280 blocks** (from ~210), with the two
heavy Cataclysm builds and the entire redundant raw-DA surface layer gone — which is where the GC and
throughput win comes from, not the raw count alone.

> Status: **light-touch subset applied 2026-06-30** (see [6.4](#64-applied-light-touch-subset-2026-06-30)).
> The owner chose the lowest-regret slice: pull the redundant raw-DA duplicates and the two heavy Cataclysm
> builds out of the Skylands, but keep the dense `skylands_ground` layer, the floater spacing, and the
> villages as-is. The spacing widen (step 1) and the optional floater widen (step 4) were **not** applied.

### 6.4 Applied: light-touch subset (2026-06-30)

Applied only steps 2 and 3, scoped to the structures that are pure duplication or the heaviest builds, and
left the rest of the density alone. The edit is **tag-deletion only** in both `skylands-datapack/` and the
runtime `world/datapacks/skytekx3_skylands/` (16 files), so it affects only **not-yet-generated** Skylands
chunks and is fully reversible.

**Removed from the Skylands** (deleted their `has_structure/*_biomes` Skylands tag in both copies):

| Structure | Why removed | Still generates in |
|---|---|---|
| `dungeons_arise:aviary` | duplicate of a `sky_*` floater | The End (`end_midlands/highlands`) |
| `dungeons_arise:bandit_towers` | duplicate of a floater | overworld badlands |
| `dungeons_arise:thornborn_towers` | duplicate of a floater | overworld (native tag) |
| `dungeons_arise:heavenly_conqueror` | duplicate of a floater | overworld + End |
| `dungeons_arise:heavenly_challenger` | duplicate of a floater | overworld + End |
| `dungeons_arise:heavenly_rider` | duplicate of a floater | overworld jungle/forest/savanna + End |
| `cataclysm:acropolis` | heavy build, off-theme in the sky | overworld `warm_ocean` |
| `cataclysm:soul_black_smith` | heavy build, off-theme in the sky | the Nether |

**Kept** (unchanged): the 22 `sky_*` floaters, the `skylands_ground` DA layer (spacing 18), the raw-only DA
majors `keep_kayra` + `mechanical_nest`, all minor DA huts, the villages + swamp hut, and the
`sky_abandoned_temple` Cataclysm floater.

**Verified** after a server restart (worldgen registries rebind only on restart, **not** on `/reload`):
all 8 removed structures now return *"Could not find ... nearby"* when `/locate`-d in `skytekx3:skylands`,
while every kept structure (incl. `sky_balloon`, `keep_kayra`, `mechanical_nest`, `wishing_well`) still
locates there, and all 8 removed structures still locate in their **native** dimensions (so nothing was
deleted from the game, only un-duplicated out of the sky). Boot clean: `Done (5.475s)`, 0 FATAL.

**Effect:** removes the entire redundant raw-DA surface layer plus the two largest-block-volume Cataclysm
builds from the Skylands. That is exactly the over-saturation that [6.2](#62-the-per-structure-spike--measured)
tied to GC pressure (jigsaw template allocation) and slower chunk gen, so the win is on the
allocation/throughput axis rather than CPU. A full before/after pregen was **not** re-run for this small
subset (the dense-vs-sparse profile in 6.2 already quantifies the mechanism, and the remaining ground DA +
village layers keep the lived density only modestly below the ~1 per 210 blocks baseline). The bigger
levers (the `skylands_ground` 18→32 widen) remain available in 6.3 if a future pass wants more headroom.
