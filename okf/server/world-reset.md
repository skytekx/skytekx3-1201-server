---
type: Runbook
title: World reset and pregen
description: Wiping and regenerating dimensions while keeping the seed and datapacks, Chunky pregeneration with measured rates, datapack sources and pack order, and what a fresh world needs.
tags: [server, world, worldgen, pregen, datapacks]
timestamp: 2026-11-06
---

# World reset and pregen

Worldgen edits only reach chunks that do not exist yet. Tag, spacing and structure changes in the
datapacks therefore need a wipe of the affected dimension and a fresh pregen before players see them.
Worldgen registries only rebind on a restart, not on `/reload` (verified on 2026-06-30, see
[Performance](performance.md)). The worldgen design itself is on
[Skylands worldgen](../content/skylands-worldgen.md).

A reset keeps these parts of `world/`:

- The seed and the datapack order in `world/level.dat`.
- The datapacks in `world/datapacks/` and the server configs in `world/serverconfig/`.
- Player data, advancements, stats and `world/data/`, which holds the scoreboards of the
  [Skylands travel](../content/aether-fall.md) functions.

It only wipes chunk data, which means region, entity and POI files.

## Full regen of every dimension

Run these from the server directory, one step after the other. Wait until the java process has exited
before the backup starts.

```bash
# Stop over RCON, then wait until the java process has exited
mcrcon -H localhost -P 65535 -p "$RCON_PW" "save-all flush" stop
# Back up first
tar czf ~/skytekx3-world-$(date +%F).tgz world
# Wipe generated chunk data, keep level.dat, datapacks, serverconfig, playerdata and data
rm -rf world/region world/entities world/poi world/DIM-1 world/DIM1 world/dimensions
# Start again (systemd here, launchd on the Mac mini)
systemctl start skytekx3.service
# Pregenerate the Overworld first, then the Skylands, radius 1000 blocks each
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world minecraft:overworld" "chunky radius 1000" "chunky start"
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world skytekx3:skylands" "chunky radius 1000" "chunky start"
```

The folder `world/dimensions/` holds every modded dimension (ad_astra, ae2, aether, bloodmagic,
blue_skies, dimdoors, irons_spellbooks, minecraft, mocreatures, skytekx3, twilightforest), and all of
them regenerate. Dimensions without a pregen generate on first entry. The full regen of 2026-06-29 ran
the two pregens one after the other.

The script `tools/rcon.py` joins all its arguments into one command. With it, send each command in its
own call. The service commands for the Mac mini are on the
[Mac mini (macOS, launchd) deployment](macos.md) page.

## Skylands-only reset

After a change that only touches the Skylands there is no need to wipe every dimension.

```bash
mcrcon -H localhost -P 65535 -p "$RCON_PW" "save-all flush" stop
tar czf ~/skylands-prewipe-$(date +%F).tgz world/dimensions/skytekx3
rm -rf world/dimensions/skytekx3/skylands/{region,entities,poi,data}   # data/ also clears stale forceloads
systemctl start skytekx3.service
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world skytekx3:skylands" "chunky radius 1000" "chunky start"
```

Player data stays. A player whose saved position now hangs over the void falls, and the travel
functions drop them into the Overworld, so the world heals itself. On the build machine the last
Skylands-only reset ran on 2026-07-12 for the vanilla worldgen rework.

On the Mac mini the Skylands of the Halloween world were reset on 2026-10-31 at 18:54, after the
fixes 50824a3 and bcb113c. The server was stopped, the same four folders were deleted (`uid.dat`
stayed), `tools/sync_skylands_datapack.sh` brought the fixed packs into `world/datapacks/`, and the
start under launchd followed. On the Mac the stop and start go through `macos/skytekx3ctl`, and the
pregen commands through `macos/skytekx3ctl rcon`, one command per call.

## Pregen numbers

Radius 1000 blocks covers 127 by 127 chunks, which is 16,129 chunks per dimension. Chunky logged these
full runs on the build machine (10 cores, Temurin 17, an 8 GB heap on 2026-06-29 and 11 GB on
2026-07-12).

| When | Dimension | Chunks | Time | Median rate |
|---|---|---:|---:|---:|
| 2026-06-29 17:19 | Overworld, full regen | 16,129 | 6 min 41 s | 40.5 chunks/s |
| 2026-06-29 17:26 | Skylands, full regen | 16,129 | 2 min 3 s | 128.6 chunks/s |
| 2026-07-12 23:31 | Skylands, after the vanilla rework | 16,129 | 3 min 31 s | 83.2 chunks/s |

The Overworld generates at about 40 chunks per second. The Skylands rate dropped as its structures got
denser, see [Performance](performance.md).

The Mac mini (Apple M4, Temurin 17.0.21, a 9 GB heap) pregenerated the Halloween world on
2026-10-31 around its spawn at 308 392, one dimension at a time.

| When | Dimension | Radius | Chunks | Time | Median rate |
|---|---|---:|---:|---:|---:|
| 2026-10-31 18:13 | Skylands, first run before the fixes | 3000 | 142,129 | 19 min 15 s | 119.4 chunks/s |
| 2026-10-31 18:32 | Overworld | 1500 | 35,721 | 8 min 1 s | 74.4 chunks/s |
| 2026-10-31 18:55 | Skylands, again after the reset | 3000 | 142,129 | 19 min 50 s | 116.4 chunks/s |

The M4 generates the Overworld about 1.8 times as fast as the build machine did. The region folders
hold 618 MB for the Overworld and 766 MB for the Skylands.

The old world on the build machine is pregenerated to radius 1000 in both dimensions, the Overworld
since the full regen of 2026-06-29 and the Skylands since 2026-07-12. The Halloween world on the Mac
mini is pregenerated to radius 1500 in the Overworld and 3000 in the Skylands. No other dimension is
pregenerated anywhere. The performance profile recommends radius 5000 or more per dimension. Radius
5000 means 625 by 625 chunks, or 390,625 chunks per dimension. At the Mac mini's rates that takes about
1.5 hours for the Overworld and 55 minutes for the Skylands. The Overworld would need about 6.8 GB on
disk, and the Mac mini had 9.4 GiB free on 2026-10-31 at 21:05, so it needs more disk first. Run it
before players join.

Chunky keeps task progress in `config/chunky/tasks/`. Git tracks those two files by mistake, so every
pregen shows up as a config change. Chunky's `continueOnRestart` is false
(`config/chunky/config.json:4`), so an interrupted pregen does not resume after a restart.

## Datapacks and pack order

- The world loads three datapacks from `world/datapacks/`. These are `skytekx3_skylands` (shared and
  vanilla safe), `skytekx3_skylands_forge` (Forge only) and `skytekx3_fixes` (overrides that silence
  third-party boot errors). Git ignores `world/`, but these three folders were force-added, so new files
  in them need `git add -f`.
- The two Skylands packs have their sources in `skylands-datapack/` and `skylands-datapack-forge/`. Edit
  the sources, then run `tools/sync_skylands_datapack.sh`. It mirrors them with `rsync -a --delete`, so
  edits made directly under `world/datapacks/` are lost. The fixes pack has no source outside `world/`,
  see [Boot fixes](../content/boot-fixes.md).
- The pack order lives in `world/level.dat` and survives a chunk wipe. The server appends every pack it
  sees for the first time to the end of the list, for example "Found new data pack
  file/skytekx3_skylands_forge, loading it automatically" on 2026-07-12 at 23:25:47. Later packs win.
- In the old world on the build machine the shared Skylands pack sits at position 119 of 279, before
  many mod packs. The fixes pack sits at 270, and the Forge Skylands pack is last. As a result the four
  chococraft overrides in the shared pack lose to the chococraft mod pack there, see
  [Skylands worldgen](../content/skylands-worldgen.md).
- In the Halloween world on the Mac mini the fixes pack, the shared Skylands pack and the Forge
  Skylands pack held the last three of 280 places on 2026-10-31. By 2026-11-04 the 20 mods added since
  then sat after them, at places 276 to 295 of 295 (read from its `level.dat`). No override touches those
  mods, so all overrides still win. A new mod that an override touches needs the fixes pack moved to
  the end again, see [Boot fixes](../content/boot-fixes.md#load-order).
- The command `/datapack list enabled` shows the order in game.

## Fresh world

A new `world/level.dat` changes three things.

- The seed changes. The example `server.properties.example` leaves `level-seed` empty, so a fresh world
  gets a random seed. To keep the current terrain, read the seed with `/seed` before the old world goes
  and put it into `level-seed`.
- The pack order is rebuilt. The fixes pack and the Forge Skylands pack must load after the mod packs.
  The Halloween world of 2026-10-31 put all three file packs at the end by itself. If a world ever has
  them earlier, the vanilla commands below move a pack to the end. The first two moved the fixes pack
  on a test world on 2026-11-04, the other two have not run yet, so check the result with
  `/datapack list enabled`. Through `tools/rcon.py` its answer ends after the first 4 KB, about 210
  packs, so for a longer list run `tools/datapack_order.py world/level.dat` after a save.
- The new world has no fixes pack. The script `tools/sync_skylands_datapack.sh` writes only the two
  Skylands packs. On the Mac mini, run `macos/sync-to-mac.sh` before the first boot, which mirrors
  `world/datapacks/skytekx3_fixes/` into the new world since 7a47a22. On other hosts, copy that folder
  from the checkout into the new world by hand, as was done for the Halloween world.
- Forge writes the new world's `world/serverconfig/forge-server.toml` as a copy of
  `defaultconfigs/forge-server.toml` from the checkout, which holds `removeErroringEntities = true`.
  Without that copy the world gets Forge's default `false`, as the Halloween world did at its first
  boot on 2026-10-31 at 18:07, and a BattleTowers obelisk can stop the server again, see
  [Logs and crashes](logs-and-crashes.md#a-battletowers-obelisk-on-the-mac-mini-2026-10-31-211233).
  No fresh world has booted with the copy yet, so check the value after the first boot. A world that
  already has the file keeps its own, so the old world on the build machine still holds `false`. The
  pack ships `defaultconfigs/` too, so new singleplayer worlds get the same value.
- In the same way Forge copies `defaultconfigs/fluxnetworks-server.toml`, whose block blacklist names
  the Phantom Energyface of Actually Additions by its 1.20.1 id, where the default of Flux Networks
  names a 1.12 id that matches nothing (0a5bb1b). The pack boot `actuallyadditions-forge-flux` of
  2026-11-06 showed the copy in a fresh world, see
  [Actually Additions backport](../modpack/actuallyadditions-backport.md#pack-integration).

```text
/datapack disable "file/skytekx3_fixes"
/datapack enable "file/skytekx3_fixes" last
/datapack disable "file/skytekx3_skylands_forge"
/datapack enable "file/skytekx3_skylands_forge" last
```

The other server configs in `world/serverconfig/` (27 more `*-server.toml` files, untracked) come
back as mod defaults. None of them mentions SkyTekx or the Skylands. The copy in `defaultconfigs/`
keeps `advertiseDedicatedServerToLan = true` like the live world, which a public server does not
need.

## Stopping safely

- Stop over RCON and wait for the java process to exit. In every logged RCON stop the last line,
  Arclight's `force exiting` message, came 5 to 9 seconds after `Stopping server`. By then the worlds
  are saved.
- Kill the process only if it is still alive after that line. Never kill it while it saves, and never
  stop it with a plain signal, see [Stop path](logs-and-crashes.md#stop-path).
- Always pregenerate after a wipe. Otherwise the first players pay the full worldgen cost, which showed
  up as tick lag of up to 6 seconds while the owner explored fresh Skylands terrain on 2026-06-28.

## World state and backups

- The live `world/` on the build machine is 518 MB. The run that last wrote it ended in a signal
  shutdown on 2026-07-13 at 15:45, after every level had logged its chunk save. Boot a copy once and
  stop it over RCON before players join, and check that boot log for chunk errors.
- The archive `~/skytekx3-world-backup-prewipe.tgz` on the build machine (153 MB, 2026-06-29 17:15)
  holds the whole world before the full regen.
- The archive `~/skylands-prewipe-2026-07-12.tgz` on the build machine (127 MB, 2026-07-12 23:23) holds
  the Skylands before the vanilla rework.
- The Halloween world lives in `world/` on the Mac mini. The launchd job `com.skytekx.backup` archives
  it every day at 05:00 into `~/skytekx3-backups` and keeps the newest three. It skips the run unless
  the disk has room for an archive as large as the newest one plus 4 GB. See [Mac mini](macos.md).

## History

- The old version of this page said the shutdown can hang for about 60 seconds. No surviving log shows
  that.
- The README pregenerated with `chunky radius 1000` and `chunky start` alone. That only covers the world
  Chunky has selected, usually the Overworld. Select each world with `chunky world` as above.

Related: [Deploy](deploy.md), [Skylands worldgen](../content/skylands-worldgen.md),
[Skylands travel](../content/aether-fall.md), [Boot fixes](../content/boot-fixes.md),
[Performance](performance.md).
