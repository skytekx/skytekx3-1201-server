---
type: Mod
title: SkyTekx Compat
description: The pack's own small Forge mod, skytekx-compat 2.0.0, which now only guards Bird's Nests against short loot table paths. It began as the skytekx-worldgen mod.
tags: [content, mod, compat, mixin]
timestamp: 2026-10-31
---

# SkyTekx Compat

SkyTekx Compat is the only Forge mod written for this pack. It began as skytekx-worldgen, which carried
the Skylands structure placement and the Skylands travel handler. Since the vanilla rework of
2026-07-12 it holds one mixin that keeps Bird's Nests from breaking loot tables.

| Property | Value |
|---|---|
| Jar | Bundled as [local-mods/skytekx-compat-2.0.0.jar](../../local-mods/skytekx-compat-2.0.0.jar), 3,728 bytes, SHA-256 `16805d83d52c7c5f...` |
| Mod id | Still `skytekx`, display name "SkyTekx Compat", MIT, author "SkyTekx" |
| Needs | Forge 47 or later and Minecraft 1.20.1 up to 1.21. No declared dependency on Bird's Nests |
| Source | Local git repo `~/git/skytekx-worldgen` (no remote), branch `master`, head 68efb3f |
| Build | Gradle 8.8, ForgeGradle 6.0, Parchment `2023.09.03-1.20.1`, MixinGradle 0.7, Mixin annotation processor 0.8.5, Java 17, Forge `1.20.1-47.4.6` |

The jar in `local-mods/` is byte identical to `build/libs/skytekx-compat-2.0.0.jar` in the source repo
(SHA-256 compared on 2026-10-31). It is also in the server's `mods/` and in the owner's Prism instance.
At boot it logs "SkyTekx compat: birdsnests loot-table guard active" (`logs/2026-07-12-1.log.gz:235`).

## The Bird's Nests guard

Bird's Nests 2.1.5 (`local-mods/birdsnests-1.20.1-2.1.5.jar`) listens to every loot table load in
`panda.birdsnests.DecayLeafEventHandler.onLootTableLoad`. It cuts the `blocks/` prefix off the table's
path with `substring(7)` without checking it. A table with a path shorter than 7 characters, for
example `minecraft:loot` or `apotheosis:book`, throws `StringIndexOutOfBoundsException`, and Forge then
drops that table ("Couldn't parse element"). The boot of 2026-06-30 03:33 logged 4 such exceptions
(`logs/2026-06-30-19.log.gz`).

The mixin `BirdsnestsDecayLeafFixMixin` injects at the head of that method (cancellable, `remap = false`
because the target is a mod method) and cancels it for every table whose path does not start with
`blocks/`. The handler only cares about leaf loot, so its own behaviour stays the same. The mixin config
`skytekx.mixins.json` is required and sets `defaultRequire` 1.

The fix lives in this mod and not in the Bird's Nests jar, so it survives a fresh download of Bird's
Nests. The Ad Astra table `data/minecraft/loot_tables/loot.json`, one of the short paths, is also
deleted by `tools/patch_mods.py` (see [boot fixes](boot-fixes.md)).

## Rebuild

```sh
cd ~/git/skytekx-worldgen
./gradlew build
cp build/libs/skytekx-compat-2.0.0.jar ~/git/skytekx3-1201-server/local-mods/
```

Copy the new jar into the server's `mods/` as well and restart. The build needs Java 17 and a copy of
Bird's Nests in `libs/` (compile only, for the mixin processor), which the repo tracks.

## Open points

- The rename stopped at the artifact. The Gradle root project is still `skytekx-worldgen`, the package
  is `com.skytekx.worldgen`, the main class is `SkytekxWorldgen`, the logger is named `SkytekxWorldgen`
  and the jar's `pack.mcmeta` says "SkyTekx Worldgen".
- The old `build/libs/skytekx-worldgen-1.0.0.jar` (12,955 bytes, 2026-06-30 05:42) still lies next to
  the new jar.
- The repo has no remote, so the only copy of the source is on this machine. The Mac mini only needs the
  jar.
- The folder `modrinth-permissions/` still has `skytekx-worldgen-1.0.0.jar.md` and no file for
  `skytekx-compat-2.0.0.jar` (see [publish](../modpack/publish.md)).
- Nobody tested a boot without Bird's Nests. The mixin config is required and Bird's Nests is no
  declared dependency, so removing Bird's Nests should be tested before it is done.

## History

- 2026-06-29: the skytekx-worldgen mod registered the structure placement type `skytekx:surface_spread`
  for the first Skylands structures (see [Skylands worldgen](skylands-worldgen.md#history)). It ran on the
  server but was not bundled, so mrpack 3.0.4 shipped without it.
- 2026-06-29 20:10: the Forge tick handler `SkylandsFallHandler` moved into the mod after the KubeJS fall
  script failed, and the jar joined `local-mods/` (commit bf898e6). On 2026-06-30 04:37 the handler also
  got the way up (2eceeb1), see [Skylands travel](aether-fall.md).
- 2026-06-30 06:21: the Bird's Nests mixin arrived, and the jar grew from 11,986 to 12,955 bytes (commit
  d14ea2b).
- 2026-07-12: the source became a git repo. Commit 64d3126 (21:43) imported version 1.0.0 as it ran on
  the server. Commit 2b49bba (21:44) deleted `SkylandsFallHandler` (124 lines),
  `SurfaceSpreadStructurePlacement` (129), `SurfaceCheckContext` (54) and the `ChunkGeneratorMixin` (49),
  and trimmed the main class, 385 lines in total. Commit 68efb3f renamed the artifact to skytekx-compat
  and set version 2.0.0. In this repo the jar swap landed with commit 174fbf6, although the message of
  dc8b470 claims it.

Related: [boot fixes](boot-fixes.md), [mod set](../modpack/mod-set.md),
[Skylands worldgen](skylands-worldgen.md), [content](index.md).
