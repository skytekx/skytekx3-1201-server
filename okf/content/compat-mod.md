---
type: Mod
title: SkyTekx Compat
description: The pack's own small Forge mod, skytekx-compat 2.1.0. It guards Bird's Nests against short loot table paths and, since 2026-10-31, keeps Skylands structures out of the void. It began as the skytekx-worldgen mod.
tags: [content, mod, compat, mixin]
timestamp: 2026-11-01
---

# SkyTekx Compat

SkyTekx Compat is the only Forge mod written for this pack. It began as skytekx-worldgen, which carried
the Skylands structure placement and the Skylands travel handler. Since the vanilla rework of
2026-07-12 it held one mixin that keeps Bird's Nests from breaking loot tables. Version 2.1.0 of
2026-10-31 added three worldgen mixins that keep the structures of the Skylands off the void, see
[Skylands void floor](#skylands-void-floor).

| Property | Value |
|---|---|
| Jar | Bundled as [local-mods/skytekx-compat-2.1.0.jar](../../local-mods/skytekx-compat-2.1.0.jar), 12,330 bytes, SHA-256 `7eb835067e332cf1...` |
| Mod id | Still `skytekx`, display name "SkyTekx Compat", MIT, author "SkyTekx" |
| Needs | Forge 47 or later and Minecraft 1.20.1 up to 1.21. No declared dependency on Bird's Nests |
| Source | Local git repo `~/git/skytekx-worldgen` (no remote), branch `master`, head db56da3 |
| Build | Gradle 8.8, ForgeGradle 6.0, Parchment `2023.09.03-1.20.1`, MixinGradle 0.7, Mixin annotation processor 0.8.5, Java 17 (`~/jdk17/jdk-17.0.19+10`), Forge `1.20.1-47.4.6` |

The jar in `local-mods/` is byte identical to `build/libs/skytekx-compat-2.1.0.jar` in the source repo.
The server's `mods/` on the build machine holds it too, and the Mac mini runs it since 2026-11-01 00:58. Packs 3.0.5 to 3.0.7 and the owner's Prism instance carry 2.0.0
(3,728 bytes, SHA-256 `16805d83d52c7c5f...`), which stays fine on the client, see
[server list](#server-list). At boot 2.1.0 logs "SkyTekx compat: birdsnests loot-table guard and
Skylands void floor active", 2.0.0 logged "SkyTekx compat: birdsnests loot-table guard active"
(`logs/2026-07-12-1.log.gz:235`).

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

## Skylands void floor

Vanilla places structure parts at the first free height of a column. Over the void of the Skylands that
height is Y 0, the bottom of the world. After the June terrain came back on 2026-10-31, village streets
and houses, the merchant camps of Dungeons Arise and AE2 meteorites hung at Y 0 to 12 between the
islands. The camps brought rose bushes, the "flowers in the void" that the owner reported at 23:10 (see
[Skylands worldgen](skylands-worldgen.md#the-void-floor)). The island density closes at Y 12, so the
class `SkylandsFloor` treats a column whose first free height is below Y 13 as void. All three mixins
act only in the dimension `skytekx3:skylands`, which they recognize by the noise settings
`skytekx3:skylands` of its generator or by the dimension key.

| Mixin | Target | What it does |
|---|---|---|
| `JigsawPlacerSkylandsFloorMixin` | `JigsawPlacement$Placer.tryPlacingChildren`, redirect of both calls to `ChunkGenerator.getFirstFreeHeight` | A jigsaw child that is not a rigid piece on a rigid parent goes to the first free height under the parent's jigsaw block. Over the void the redirect answers 4096 blocks below the world, so every candidate lies outside the jigsaw bounds and the connector closes as it does at any dead end |
| `GravityProcessorSkylandsFloorMixin` | `GravityProcessor.processBlock`, at the head | A terrain matching piece moves each of its blocks onto the heightmap of the block's own column. Over the void the block is dropped instead, so a street that overhangs an island edge ends at the edge |
| `StructureSkylandsFloorMixin` | `Structure.generate`, at the return | Cancels a start whose first piece reaches below Y 13 or whose center column is void, and an AE2 meteorite with a void column within 15 blocks of its chunk. A structure whose biomes include `skytekx3:sky_void` is left alone, since it floats over the void by design. These are the 22 floaters of `#skytekx3:has_structure/sky_floaters` and the Keep Kayra and Mechanical Nest of When Dungeons Arise |

AE2 15.4.12 puts a meteorite at the mean surface height within twice its radius (at most 15 blocks) of a
random point in its chunk. When the heights vary, it sinks the meteorite by three quarters of the way
down to the lowest column. A single void column counts as height 0, so a meteorite near an island edge
ended at Y 7 to 12 under the island. The rule keeps meteorites inside the islands. The Overworld keeps
all of its meteorites.

Before 2.1.0 went to the server, no other jar touched the same code: of the 236 jars in `mods/`,
Waystones mixes into `tryPlacingChildren` at `Registry.getHolder`, Structure Gel into
`Structure.generate` at its head and with a `@ModifyVariable` on the pieces builder, and no jar into
`GravityProcessor`. Arclight has no mixin on any of the three. The refmap maps the targets to
`m_227264_`, `m_223221_`, `m_7382_` and `m_226596_`.

## Server list

Forge compares the version of each mod in the server list ping. Packs 3.0.5 to 3.0.7 carry 2.0.0 with
the default display test `MATCH_VERSION`, which marks a server as incompatible unless the server reports
exactly "2.0.0" for the mod id `skytekx`. Version 2.1.0 therefore reports "2.0.0" in the ping and accepts
any server version on the client (`SkytekxWorldgen.SERVER_LIST_VERSION`). Every fix in the mod acts on
the server, so a client never needs 2.1.0.

## Rebuild

```sh
cd ~/git/skytekx-worldgen
JAVA_HOME=~/jdk17/jdk-17.0.19+10 ./gradlew build -Djava.net.preferIPv4Stack=true
cp build/libs/skytekx-compat-2.1.0.jar ~/git/skytekx3-1201-server/local-mods/
```

The Gradle cache lacks MixinGradle 0.7.38 and two of its dependencies, so `--offline` fails and the build
needs the network.

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
- The folder `modrinth-permissions/` still has `skytekx-worldgen-1.0.0.jar.md` next to
  `skytekx-compat-2.1.0.jar.md` (see [publish](../modpack/publish.md)).
- The mod has no tests. The void floor was tested on a throwaway copy of the server, see
  [Skylands worldgen](skylands-worldgen.md#the-void-floor).
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
- 2026-10-31 23:49: version 2.1.0 added the [Skylands void floor](#skylands-void-floor) (commit 9636058)
  and the [server list](#server-list) version (66c0847, 3c3a6a0).
- 2026-11-01 00:44: commit db56da3 exempts structures whose biomes include `skytekx3:sky_void`. Without
  it the start check also cancelled floaters whose center hung over the void, and a test world of
  radius 1000 held 16 floater starts instead of 25.
- 2026-11-01 00:58: 2.1.0 went live on the Mac mini, and 2.0.0 went to `~/skytekx3-backups/jars-orig/`
  there. The Skylands generated again to radius 2000 held no block below Y 13 and 93 floaters, see
  [Skylands worldgen](skylands-worldgen.md#deploy-of-2026-11-01).

Related: [boot fixes](boot-fixes.md), [mod set](../modpack/mod-set.md),
[Skylands worldgen](skylands-worldgen.md), [content](index.md).
