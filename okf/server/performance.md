---
type: Runbook
title: Performance and worldgen profiling
description: Spark profile of live chunk generation — the lag is vanilla-noise-bound, not memory, and no single mod is the villain.
tags: [performance, worldgen, profiling, spark]
timestamp: 2026-06-29
---

# Performance and worldgen profiling

Profiled the **running** server (Arclight 1.20.1, Forge 47.4.18) under forced fresh worldgen to find
what makes chunk loading lag. Short version: **the cost is vanilla terrain noise + JVM overhead, not a
mod.** There is no mod to delete for a worldgen win. Pregen wider instead.

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
| **skytekx-worldgen (custom)** | <0.01 % | <0.01 % | Our `surface_spread` placement + the ~14 Skylands floaters — **invisible** |

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
4. **Unrelated, but seen while profiling:** the `ruins` mod throws a `RuinsPositionsFile.txt` flush crash
   roughly every 5 min (an IO/threading bug, *not* worldgen CPU). It spams the log and risks the
   positions file — worth a separate look.

Related: [Arclight runtime](arclight.md), [world reset / pregen](world-reset.md), [deploy](deploy.md).
