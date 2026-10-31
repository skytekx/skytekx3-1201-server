---
type: Runbook
title: Performance and worldgen profiling
description: Spark profiles of live chunk generation, measured heap and tick data, the open performance levers and the Skylands structure density history. Lag comes from vanilla terrain noise and garbage collection, not from a single mod.
tags: [performance, worldgen, profiling, spark, memory, server]
timestamp: 2026-10-31
---

# Performance and worldgen profiling

On 2026-06-29 at 22:11 the owner asked for a performance report, because chunk loading had lagged
badly, and at 22:30 asked for it to live in the OKF. At 23:41 the owner wanted to know which mod wastes
the most performance, ready to remove exactly that mod. The answer is none. Chunk generation costs
vanilla terrain noise plus JVM overhead, and no mod is worth removing for worldgen. A follow-up on
2026-06-30 found one structural lever, the density of structures in the Skylands.

The owner's standing rule since 2026-06-30 01:20 is to decide in favour of speed and reliability. Two
and a half hours later the owner also asked for chocobos and friendly structures to be common in the
Skylands. The structure density went up for that, and a pregen pays the extra cost once.

## Test setup

All numbers on this page come from the build machine, not from a server host.

| Item | Value |
|---|---|
| Machine | Apple M1 Max laptop, 10 CPU cores, 62 GB RAM (about 23 GB free), Asahi Linux on aarch64 with 16K pages |
| Swap | Compressed RAM (zram), not disk, so its used figure is harmless |
| JVM | Temurin 17.0.19 with `-Xms4G -Xmx8G` and nothing else, so the default G1 collector without Aikar's flags |
| Server | Arclight 1.20.1 1.0.6 (the hardened jar), Forge 47.4.18, 213 index mods at the time |
| Players | None or one |
| Profiles | 2026-06-29 at 23:49 and 23:57, 2026-06-30 at 01:28 |

## Heap and memory

These values were measured.

- At idle the memory value that AllTheLeaks logs (its C value, see
  [Logs and crashes](logs-and-crashes.md)) stays between about 2,550 and 2,900 MB. The run from
  2026-06-30 08:09 to 2026-07-03 21:01 logged 343 samples, usually 10 minutes apart with gaps of up to
  13 hours, and showed no growth (2,558 to 2,886 MB). The two runs of 2026-07-12, the second of which
  lasted into 2026-07-13, stayed between 2,641 and 2,879 MB.
- The run of 2026-06-29 that regenerated every dimension logged higher values, 3,173 to 4,187 MB
  between 17:27 and 19:37.
- When the watchdog fired in that run (19:41:20), the crash report showed 1,099 MiB free of 8,192 MiB.
  The used part includes garbage that was not yet collected.
- No log contains an OutOfMemoryError, a full code cache or a GC overhead message.
- TPS stayed at 20.0 in all eight Spark health reports. Only the 15-minute average dipped to 19.94
  around 01:31 on 2026-06-30. Idle MSPT on 2026-06-29 at 23:46 was 0.2, 0.3, 0.4 and 2.3 ms (minimum,
  median, 95th percentile, maximum over 10 seconds) and 0.2, 0.3, 1.5 and 8.8 ms over one minute.
  Across the seven reports between 2026-06-29 22:52 and 2026-06-30 01:34 no tick took longer than
  10.8 ms. One report on 2026-06-27 showed 12.0 ms.

These values were not measured.

- Only one player account ever joined, so the memory and tick cost of 20 players is unknown.
- No run had GC logging enabled.

The Linux launchers in the repo set an 11G heap. That is `ServerLinux.sh:10` and line 23 of the systemd
unit, both with `-Xms` equal to `-Xmx` and with `-XX:+AlwaysPreTouch`, which commits the whole heap at
start. The comment in `ServerLinux.sh` says the size is tuned for a 16 GB host such as the M4 Mac mini
and leaves about 5 GB for the OS. It goes back to 2026-06-27, when the owner asked whether an M4 Mac
mini with 8 GB could serve 20 players (12:07) and then noted that only the 16 GB model exists (12:21).
The macOS launcher `ServerMac.sh`, which has run the server on that Mac mini since 2026-10-31, sets 9G.

Only the two runs of 2026-07-12 used an 11 GiB heap. Netty's `maxDirectMemory` of 11,811,160,064 bytes
in their debug logs shows it. Whether Aikar's flags were active in those runs cannot be told from the
logs. Metaspace for 236 mod jars, the code cache, thread stacks and direct buffers all come on top of
the heap. On a 16 GB host an 11 GB heap leaves little room for them and the OS. With the 9G heap of
`ServerMac.sh` the JVM on the Mac mini stayed at 10.1 to 10.7 GB resident without swap during and
after the pregens of 2026-10-31, see [Mac mini (macOS, launchd) deployment](macos.md#measured-numbers).

## How the worldgen profile was taken

Spark ran `spark profiler start --thread *` with the async engine and a 4 ms interval, so it captured
all threads. Meanwhile Chunky generated fresh chunks far away from the pregenerated core, which covers
radius 1000 around spawn.

- In the Overworld it generated a square at 30000,30000 (radius 160) and most of one at 60000,60000
  (radius 480, stopped at 3,296 of 3,721 chunks) at about 33 chunks per second (Chunky's median running
  rate). Spark report `jI5VCGQTd6` (about 156 s, 60 threads, 203,957 nodes).
- In the Skylands (`skytekx3:skylands`, the dimension players explore) it generated 40000,40000 (radius
  480) and 80000,80000 (radius 640) at 115 to 124 chunks per second on average. Most of it is void, so
  each chunk is cheap. Spark report `1JnrEfYpST` (about 115 s, 34 threads, 29,752 nodes).

Self time was charged to the mod that owns the running class. Each Spark report embeds a
`class_sources` map for that, which its Sources view shows.

Spark keeps uploaded reports only for a limited time. On 2026-10-31 all three reports of this page
returned HTTP 404 from the Spark upload store, so the figures here are the only record left. A new
profile needs a new Spark run.

## Where the time goes

Both dimensions tell the same story. Worldgen runs on the `Worker-Main` pool, which takes 67 to 69
percent of all samples. The rest is garbage collection, JIT compilation and chunk IO.

| Source | Overworld | Skylands | What it is |
|---|---:|---:|---|
| Minecraft (vanilla) | 49.6 % | 61.7 % | Terrain noise (Simplex, Perlin, Improved), aquifers, density functions, the biome climate R-tree, ore placement |
| JVM native code (libjvm.so) | 23.8 % | 17.9 % | The JVM itself with the G1 collector, C2 JIT compilation and native glue |
| JDK standard library | 11.5 % | 8.3 % | Calls into `java.util` and `java.lang.Math` from the generation above |
| Other native code (libzip, libc) | About 4 % | About 5 % | Jar and zip reads, C runtime |
| Starlight | 0.85 % | 3.02 % | Lighting engine, a performance mod that is faster than vanilla light |
| ModernFix | 0.82 % | 0.52 % | Performance mod |
| Biolith | 0.63 % | 0.05 % | Biome placement library behind Biomes O' Plenty and TerraBlender |
| Zeta and Quark | About 0.97 % | Not seen | Quark and its library |
| YUNG's API | 0.23 % | 0.35 % | Structure framework of YUNG's Better Dungeons, Mineshafts and Strongholds |
| TerraBlender | 0.19 % | Not seen | Biome region manager |
| SkyTekx worldgen mod, retired 2026-07-12 | Under 0.01 % | Under 0.01 % | Its `surface_spread` placement and the floaters of that time |

The top classes by self time are nearly all vanilla noise. They are `Aquifer$NoiseBasedAquifer` (8.4 %
in the Overworld), `SimplexNoise` (4.9 % and 17.9 %), `ImprovedNoise`, `PerlinNoise`, `Mth`, the
`Climate$RTree` biome lookup (about 5.5 % in the Overworld), `NoiseChunk`, `DensityFunctions` and
`OreFeature`.

## Verdict: no villain mod

- The biggest cost charged to a mod is Starlight with 0.85 % in the Overworld and 3.0 % in the Skylands.
  It is a performance mod, so removing it would make lighting slower. ModernFix and FerriteCore come
  next, also performance mods. Everything else on the mod side stays below 1 %.
- Removing any single content mod would buy less than 1 % of worldgen time and cost real content.
- Mods inflate one vanilla cost indirectly. Biomes O' Plenty, TerraBlender and Biolith register many
  biomes, which enlarges the climate R-tree search that vanilla runs per column (about 5.5 % of the
  Overworld pass). That cost is small, and Biomes O' Plenty is core to the pack, so it stays.
- Structure mods (When Dungeons Arise, L_Ender's Cataclysm, YUNG's, Repurposed Structures) barely
  register on average, below 0.25 %, because structures are sparse. Each one places in a single burst,
  though. If players report a stutter right when a new structure appears, suspect those.

This is the inherent cost of a pack this size doing vanilla noise terrain. The fix is operational, not
subtractive.

## Levers

The worldgen accelerators are already installed, which is why generation is as fast as it is. Noisium
2.3.0, Saturn 0.1.3 and ChunkOpt 1.0.2 speed up the noise and feature generation that dominates.
FerriteCore 6.0.1, ModernFix 5.27.51 and Starlight 1.1.2 cover memory and lighting. Keep all of them.
No server-side port of Lithium is installed. Radium crashed Arclight on 2026-06-27 (see
[Logs and crashes](logs-and-crashes.md)), and neither Canary nor ServerCore is in the pack.

The remaining levers are operational. Their state on 2026-10-31 is in the table.

| Lever | State |
|---|---|
| Pregenerate wider, radius 5000 or more per dimension, so players rarely hit live worldgen | Not done. The Halloween world on the Mac mini covers radius 1500 in the Overworld and 3000 in the Skylands, the old world on the build machine radius 1000 in both, see [World reset](world-reset.md#pregen-numbers) |
| Lower `view-distance` and `simulation-distance` from 10 and 10 to 8 and 6, which cuts the chunk fan-out each player drags around | Not done (`server.properties.example:50` and `:59`) |
| Keep `/locate` sweeps and big pregens off the live server | A sweep tripped the watchdog on 2026-06-29 at 19:41 |
| Do not cut content mods for worldgen | Holds, the profile shows no payoff |
| Remove the Ruins mod, whose flush thread threw on `RuinsPositionsFile.txt` every few minutes | Done in `720b8a4` on 2026-06-30. It was an IO bug, not a CPU cost |

## Skylands structure density

### The audit of 2026-06-30

After the first profile the Ruins mod was removed, and the Skylands floater set grew from 14 to 22
entries (7 new When Dungeons Arise floaters and the Cataclysm `abandoned_temple`). A follow-up then
checked how crowded the Skylands had become and whether one structure spikes chunk generation, with
L_Ender's Cataclysm as the suspect (commit `0d79972`, 2026-06-30 01:34).

A `/locate` hit means the structure really generates there, because the command runs each structure's
generation checks. Sweeps of `execute in skytekx3:skylands positioned ... run locate structure ...` from
four distant centres showed four overlapping layers. The table shows that night's state, and every row
has changed since.

| Layer at the time | Placement | Spacing in chunks | Density found with `/locate` |
|---|---|---|---|
| Floaters, 22 custom `sky_*` structures in `skytekx3:skylands_floaters` | Random spread | 44, separation 12 | Any floater about every 335 blocks |
| Dungeons Arise ground structures, 10 in `skytekx3:skylands_ground` | Surface spread from the SkyTekx mod | 18, separation 6 | One about every 288 blocks, the densest layer |
| Raw Dungeons Arise majors, 8 in `dungeons_arise:major_structures` | Random spread | 60, separation 50 | Present, and 6 of the 8 duplicate a floater |
| Villages and swamp huts | Surface spread from the SkyTekx mod | 34 for villages, 32 for swamp huts | Plains, savanna, taiga and swamp variants present |
| Cataclysm acropolis and soul black smith | Cataclysm's own sets | 80 and 60 | Both present, a smith as close as 57 blocks |

From four random points the nearest structure was 32, 48, 128 and 248 blocks away. The theoretical
combined density, the sum of one over spacing squared across the active sets, came to about one
structure per 210 blocks. That is about 2.5 times denser than the vanilla Overworld village grid with
one village per about 544 blocks.

The audit named two problems.

- The raw Dungeons Arise layer was redundant. Its `has_structure/*_biomes` tags had been extended with
  the 14 Skylands biomes (`replace: false`), so the raw majors also placed on island surfaces. Six
  existed both as a `sky_*` floater and as a raw surface build: aviary, bandit_towers,
  thornborn_towers, heavenly_conqueror, heavenly_challenger and heavenly_rider.
- The heavy Cataclysm builds generated in the sky. Their biome tags included the Skylands biomes,
  although the plan was that the big non-jigsaw Cataclysm builds would not float.

### The per-structure spike, measured

Spark profiled fresh generation of a dense Skylands region at 120000,120000 (radius 480, 3,721 chunks)
in report `fwMefVlnFM`, which has expired as well. The comparison is the sparse Skylands pass above.

| Metric | Sparse Skylands | Dense Skylands |
|---|---:|---:|
| Chunky throughput, average | 115 to 124 chunks/s | 79 chunks/s |
| Garbage collection (GC threads, G1 concurrent, refinement) | About 14 % | About 39 % |
| JVM native code, mostly GC and JIT | 17.9 % | 41.4 % |
| Vanilla structure, jigsaw and template assembly | 0.44 % | 1.40 % |
| Cataclysm's own code | 0.01 % | 0.00 % |
| Dungeons Arise's own code | 0.00 % | 0.00 % |

Cataclysm is not guilty on CPU. Cataclysm and Dungeons Arise structures are data, assembled by vanilla
jigsaw and template code, so the mods' own code costs about nothing. Even all structure assembly
together stays at 1.4 % in a saturated region. No single structure is a CPU bomb.

The saturated region still generated more slowly, and its garbage collection samples nearly tripled.
The dense run took 47 seconds for 3,721 chunks, and Chunky's running rate dipped to 38 chunks per second
on the way. The cost of density is allocation churn from jigsaw template block lists, palettes and block
state writes, plus the block volume of big builds. More heavy placements at once mean more GC pauses and
slower chunk generation, and that is how chunk loading stalls under load.

### What was decided

- The audit proposed to widen the ground set from 18 to 32 chunks (separation 6 to 10), to take the
  raw Dungeons Arise duplicates and the two Cataclysm builds out of the Skylands, and optionally to
  widen the floaters from 44 to 50. It projected about one structure per 280 blocks.
- The owner chose the lowest-regret slice (`87f885f`, 2026-06-30 03:37). It deleted the Skylands biome
  tags of the six duplicates and of the acropolis and the soul black smith, 16 files across the two
  datapack copies of that time. Worldgen registries only rebind on a restart, not on `/reload`. After a
  restart none of the eight located in the Skylands any more. All eight still located in their native
  dimensions: aviary in the End midlands and highlands, bandit_towers in Overworld badlands,
  thornborn_towers in the Overworld, heavenly_conqueror and heavenly_challenger in the Overworld and the
  End, heavenly_rider in Overworld jungles, forests and savannas and the End, the acropolis in warm
  oceans and the soul black smith in the Nether. The boot was clean with 0 FATAL. No pregen measured
  the effect of this change. At 03:48 the owner confirmed that removed structures must keep generating
  in their native dimensions.
- At 03:55 and 03:57 the owner asked for chocobos and friendly structures (villagers, adventure houses)
  to be common in the Skylands. Commit `6c5b986` (04:18) then made the Skylands denser for traversal.
  Floaters went from 44 to 32 chunks, the ground set from 18 to 14 and villages from 34 to 24. The
  audit's widening steps were never applied.
- On 2026-07-12 the Skylands moved to pure vanilla placement. Every set went back to random spread, and
  biome gating keeps structures off the void, see [Skylands worldgen](../content/skylands-worldgen.md).

### Current structure sets

Since 2026-07-12 these sets drive the Skylands. All of them use vanilla random spread (checked in
`skylands-datapack/` and `skylands-datapack-forge/` on 2026-10-31).

| Set | Pack | Spacing and separation | Entries |
|---|---|---|---|
| Skylands floaters (`skytekx3:skylands_floaters`) | Forge pack, which overrides the shared one | 32 and 8 | 22, the 21 mod floaters plus the balloon |
| Skylands floaters, vanilla build | Shared pack | 32 and 8 | 1, the sky balloon |
| Skylands ground (`skytekx3:skylands_ground`) | Forge pack | 14 and 5 | 10 Dungeons Arise surface structures |
| Villages (`minecraft:villages`) | Shared pack | 24 and 8, vanilla is 34 and 8 | 5 village types |
| Swamp huts (`minecraft:swamp_huts`) | Shared pack | 32 and 8, the vanilla values | 1 |
| Dungeons Arise majors (`dungeons_arise:major_structures`) | Forge pack | 60 and 50, as in the jar | 27 of the jar's 31 |
| Dungeons Arise minors (`dungeons_arise:minor_structures`) | Forge pack | 35 and 25, as in the jar | 1 of 6, the abandoned temple |

The Skylands pregen rate tracks this density. Chunky's median rate was 128.6 chunks per second for the
Skylands regen of 2026-06-29, before the density changes, and 83.2 after the rework of 2026-07-12 (see
[World reset](world-reset.md#pregen-numbers)). The two runs covered different terrain, so this is an
observation, not a controlled test.

### Side effect in the Overworld

Structure sets are global. A set places its structures in every dimension whose biomes match the
structures' own biome tags. The shared pack adds the Skylands biomes to the tags of the ten ground
structures with `replace: false`, so the tags keep their Overworld biomes from the Dungeons Arise jar
(plains, meadows, forests, beaches, jungles, taigas, savannas and hills). The Overworld therefore gets
these structures on the 14 chunk grid of the ground set. In the jar nine of them sit in the Dungeons
Arise sets, 35 or 60 chunks apart, and the small prairie house does not generate at all, because no set
in the jar lists it. The village set at 24 also applies to the Overworld, which gives about twice as
many villages as vanilla. Both effects hold at least since the move to random spread on 2026-07-12.
Nobody has measured what they cost in Overworld generation. Whether the Overworld should keep this
density is an open question for the owner.

## History

- The old version of this page gave the dense Skylands throughput as about 47 chunks per second and
  called the region about 2.4 times slower. The log shows 3,721 chunks in 47 seconds, which is 79 chunks
  per second on average. The dense region was about 1.5 times slower than the sparse pass. The GC
  figures come from the Spark reports and are unchanged.
- The old page attributed worldgen to the skytekx-worldgen mod and its `surface_spread` placement. Both
  were retired on 2026-07-12. The mod shrank to skytekx-compat, see
  [Compat mod](../content/compat-mod.md).
- The old heap table showed only the build machine's `-Xms4G -Xmx8G` without saying that the launchers
  use 11G.

Related: [Arclight runtime](arclight.md), [World reset](world-reset.md), [Deploy](deploy.md),
[Logs and crashes](logs-and-crashes.md), [Skylands worldgen](../content/skylands-worldgen.md).
