---
type: Runbook
title: Logs and crashes
description: Where the server logs live, how to read a run, the crash history with root causes and fixes, the stop path, the boot error baseline and the error counts of each recent boot.
tags: [server, logs, crashes, debugging, arclight]
timestamp: 2026-11-04
---

# Logs and crashes

The runs from 2026-06-27 to 2026-07-13 happened on the build machine. That is an Apple M1 Max laptop
with 10 CPU cores under Asahi Linux (aarch64), and the server ran straight from this checkout on
Temurin 17.0.19. Since 2026-10-31 the server runs on the Mac mini on Temurin 17.0.21, which keeps its
own `logs/` in its own checkout (see [Mac mini (macOS, launchd) deployment](macos.md)). Most of this
page is about the build machine's logs, the Mac mini runs are in their own sections.

## Where the logs live

- The current or last run writes `logs/latest.log` at INFO level and `logs/debug.log` at DEBUG level.
  The debug log is about 12.7 MB, so search it instead of opening it.
- Rotated runs sit in `logs/` as `YYYY-MM-DD-N.log.gz` (73 files from 2026-06-27 to 2026-07-12).
  Log4j rotates with `fileIndex=min`, so `-1` is the newest file of a day and higher numbers are older.
  Files also roll at midnight, so one long run can span several files.
- Rotated debug logs are `logs/debug-1.log.gz` to `logs/debug-5.log.gz`. Only five are kept.
- KubeJS writes `logs/kubejs/server.log` and `logs/kubejs/startup.log` for the last boot.
- Crash reports go to `crash-reports/`. There are six, all from 2026-06-27 and 2026-06-29.
- Stdout captures from the build sessions sit in the repo root (`boot.log`, `boot2.log` to `boot7.log`,
  `boot9.log`, `boot-regen.log`, `srv.log`, `srv-boot.log`) and in `logs/` (`console-boot.log`,
  `console-restart.log`, `pre-aether-boot-1782734295.log`). They are the only record of seven early
  runs that have no rotated log. Git ignores all of these files.
- Under systemd, stdout goes to the journal (`journalctl -u skytekx3`).
- Under launchd on the Mac mini, stdout goes to `/dev/null`, since `logs/latest.log` holds the same
  lines, and stderr goes to `logs/launchd-stderr.log`, which stayed empty on 2026-10-31. The console
  output of the first manual run there is in `logs/manual-boot.out` on the Mac.

## How to read a run

Log lines look like `[12Jul2026 23:26:00.952] [Server thread/INFO] [logger/]: message`. Filter by level
with the strings `/ERROR]`, `/WARN]` and `/FATAL]`.

- A run starts with two ModLauncher lines. The first ends in
  `starting: java version 17.0.19 by Eclipse Adoptium` and names the JVM. The second,
  `ModLauncher running: args [--launchTarget, arclightserver, --fml.forgeVersion, 47.4.18, ...]`,
  shows an Arclight launch.
- The Arclight banner prints a build date. The hardened jar shows `Build Date 2026-06-29 21:50:26`
  (`logs/2026-07-12-1.log.gz:11`). The stock 1.0.6 jar showed `Build Date 2026-05-07 12:21:25`
  (`logs/2026-06-29-4.log.gz:11`). See [Arclight runtime](arclight.md).
- The `Done (5.073s)` line only times the last world load step. ModernFix logs the real boot time, for
  example `Dedicated server took 54.308 seconds to load` (2026-07-12 23:26:01). Real boot times grew
  from 34 to 36 seconds on 2026-06-27 and 2026-06-28 to 54 to 55 seconds on 2026-07-12 as mods were
  added.
- Crash reports print the JVM flags (`JVM Flags: 2 total; -Xms4G -Xmx8G`), normal logs do not. In
  `logs/debug.log` the Netty line `maxDirectMemory: 11811160064 bytes` gives away the maximum heap,
  because Netty sizes its direct memory limit from it. That is how the 11 GiB heap of the two
  2026-07-12 runs is known.
- The `Memory:` line of a crash report lists free memory first, then committed memory, then the
  maximum. For example `... (1099 MiB) / ... (8192 MiB) up to ... (8192 MiB)` means 1,099 MiB free.
- AllTheLeaks logs a memory sample every 10 minutes, for example `B: 2662MB / C: 2662MB / Diff: +0MB`
  (`logs/latest.log:326`, 2026-07-13 07:13). B is its baseline and C the current value.
- Arclight's async catcher (`async-catcher` in `arclight.conf`, `warn=true`) logs
  `Async chunk entity get` as WARN from the `Netty Epoll Server IO` thread while players are online.
  There were 6,482 such lines in 6 runs, 858 in the run of 2026-07-12 18:11 alone. On 2026-11-01 the
  stack dumps in `debug.log` named Infernal Mobs' `MobModsPacket.handle` for all 6,130 lines since
  03:02. Since skytekx-compat 2.1.3 (06:10) these queries run on the server thread, see
  [SkyTekx Compat](../content/compat-mod.md#infernal-mobs-queries). It never crashed the server.

How a run ended shows in its last lines.

- A clean RCON stop logs `[Rcon: Stopping the server]`. It ends with Arclight's exit thread line
  `3 threads not shutting down correctly, force exiting`, 5 to 9 seconds after `Stopping server` in
  every logged RCON stop. Two of the RCON stops on the Mac mini counted 5 threads instead.
- Lines from the `Server Shutdown Thread` mean a signal stop (SIGTERM, SIGINT) or a watchdog halt. See
  [Stop path](#stop-path).
- A crash report with the same time stamp means a crash.
- A log that simply ends means the process died silently, or the host went down or slept.

These searches answer most questions without opening a large file.

```bash
# JVM and real boot time of every run in one rotated log
zgrep -n -e 'starting: java version' -e 'Dedicated server took' logs/2026-07-12-1.log.gz
# ERROR and FATAL lines
zgrep -n -e '/ERROR]' -e '/FATAL]' logs/2026-07-12-1.log.gz
# Tick lag and watchdog halts in all rotated logs
zgrep -n -e "Can't keep up" -e 'single server tick took' logs/*.log.gz
# How runs ended, per file
zgrep -c -e 'Rcon: Stopping the server' -e 'Server Shutdown Thread' logs/*.log.gz
```

## Run history in numbers

The logs record 76 boot attempts from 2026-06-27 to 2026-07-12. Of these, 70 reached `Done` and 6 never
did. Of the 70 runs, 62 ended with an RCON stop, 3 crashed, 2 ended by a signal and 3 ended without a
trace. Most were short test boots during the build sessions, 22 of them on 2026-06-28 alone while the
Skylands generator was tuned. The longest run started on 2026-06-30 at 08:09 and lasted three and a
half days. The last complete boot on the build machine started on 2026-07-12 at 23:25.

The Mac mini's `logs/` holds six runs of 2026-10-31, and all six reached `Done`. The first started by
hand at 18:07:17, created the Halloween world and ended with an RCON stop at 18:53:14. The second, the
first start under launchd at 18:54:57, ran the second Skylands pregen and stopped at 19:26:01. The next
three, from 19:26:53 to 19:34:54, were the stop tests on the [Mac mini](macos.md#tests-of-2026-10-31).
Each of these runs ended with an RCON stop. The sixth booted after the test reboot at 19:35:14 and was
still running at 20:00. Log4j on the Mac gives the newest rotated file the index 1 and shifts the older
ones up, so this page names the Mac's runs by their start times and not by file names.

## Crash history

Six crash reports exist. Together with the launch failures and the runs that ended without a report
they make up the history below. Each entry gives the root cause, the fix and whether it is still a
risk. All six reports show Java 17.0.19, `-Xms4G -Xmx8G` and Arclight 1.20.1-1.0.6 at upstream commit
6de9fec.

### Radium against Arclight (2026-06-27 12:05)

The report `crash-2026-06-27_12.05.07-server.txt` ("Exception in server tick loop") came during world
load. Arclight's `PortalForcerMixin` could not inject into `PortalForcer::m_192985_`, because Radium,
the Forge port of Lithium, had already merged its own `PortalForcerMixin` into that method (report
lines 29 to 34). Radium was removed before the next boot at 12:06:53. No commit records this, since
the git history was rebuilt later and starts after it. Radium is in neither the index, `mods/` nor
`disabled-mods/`.

It can come back. The script `tools/resolve_all.py` still lists `radium` on line 18, together with
`rubidium`, `memoryleakfix` and `better-combat`, and downloads whatever it resolves into `mods/`. Do not
run it, see [mod set](../modpack/mod-set.md).

### AsgardShieldReloaded and creeper explosions (2026-06-27 20:43 and 21:16)

Two reports, `crash-2026-06-27_20.43.25-server.txt` and `crash-2026-06-27_21.16.11-server.txt`, are
both "Ticking entity" with `IllegalArgumentException: Invalid hand null`. Each time a creeper exploded
next to a player near the Skylands spawn. The explosion fired `LivingAttackEvent`, and AsgardShieldReloaded
0.2 (mod id `asr`) asked for the item in a hand that its own helper returned as null
(`EventHandler.onBlockActive`, `EventHandler.java:109`). The whole server went down twice in 33
minutes. The second crash came 12 seconds after the owner joined, and the owner asked why the server
crashed on joining (2026-06-27 21:16).

This is fixed since 2026-10-31. The handlers read `Player.swingingArm`, which stays null until a
player swings after a login or a respawn. On that evening the same error made every hit on such a
player fail, from 22:24:45 to 22:46:31. Since 8286e93, `tools/patch_mods.py` makes both handlers read
`getUsedItemHand()` instead, which is never null, and the patched jar runs on the Mac mini since the
restart of 22:49:28. No log after that restart holds "Invalid hand null" (log audit of 2026-11-04). The
error comes back only if the jar is replaced without running `tools/patch_mods.py`.

### Client-only mods on the server (2026-06-29 12:49)

The report `crash-2026-06-29_12.49.02-fml.txt` ("Mod loading error has occurred") followed a
re-resolve of the mods at 12:47. Forgematica and MaFgLib failed to construct because they load the
client class `Screen` on a dedicated server. WATERMeDIA refused to start on a server (report line 118).

The next boot at 12:51 reached `Done`. The index marks Forgematica, MaFgLib and CrashAssistant as
unsupported on the server, so `tools/build_mrpack.py` moves them to `client-only-mods/`.
WATERMeDIA (`watermedia-2.1.36.jar`) and LittleFrames, which needs it, sit in `disabled-mods/`. Both
folders are gitignored. CrashAssistant left a harmless `local/crash_assistant/` behind.

### A structure sweep tripped the watchdog (2026-06-29 19:41:20)

The report `crash-2026-06-29_19.41.20-server.txt` ("Watching Server") is the vanilla watchdog. A single
tick took 60 seconds, the limit set by `max-tick-time=60000`. The server thread was parked in
`ChunkGenerator.findNearestMapStructure`, called from `LocateCommand` through Bukkit's
`dispatchServerCommand`.

Between about 19:35 and 19:41 an RCON loop sent `execute positioned 0 110 451 run locate structure <id>`
for every modded structure. It searched for the Overworld structure at x 0, y 110, z 451 that the owner
wanted in the Skylands (request of 2026-06-29 19:30). Single locates took up to 8,820 ms
(`dungeons_arise:shiraz_palace` at 19:37:16), and the queued commands kept the server thread busy until
the watchdog fired. The sweep also logged 22 errors of the form
`AsyncStructureSpawnEvent cannot be triggered asynchronously from primary server thread`. Arclight
fires a Bukkit async event when `/locate` generates chunks on the main thread.

The cause is not fixed in code, and the setting `max-tick-time` is still 60000. A later sweep of 212
locates (2026-06-30 00:58 to 03:32) peaked at 2,902 ms per locate and stayed below the limit. Run such
sweeps on a test copy, never on the live server. Under a service manager with automatic restart a
watchdog halt turns into a restart.

### Arclight DistanceManager race at shutdown (2026-06-29 19:41:29)

Nine seconds after the watchdog, the report `crash-2026-06-29_19.41.29-server.txt` ("Exception in server
tick loop") recorded `java.util.NoSuchElementException` in Arclight's
`DistanceManagerMixin.arclight$safeIter` (`DistanceManagerMixin.java:552`), called from
`DistanceManager.runAllUpdates` on the server thread. One millisecond earlier the
`Server Shutdown Thread` had hit `ConcurrentModificationException` in the same method from
`MinecraftServer.close`, and four chunks failed to save. After the watchdog fired, Arclight's shutdown
hook closed the server while the stuck server thread still ran chunk work, so two threads iterated the
same set of pending chunk holders.

The owner reported the server down at 21:13, asked at 21:22 for the underlying issue to be fixed, and
at 21:32 whether a backport from newer versions could harden it. Commit `8b1c231` (2026-06-29 22:11)
ships a rebuilt Arclight jar whose `safeIter` checks `hasNext()` and recovers from both exceptions. The
first boot on it was 21:55 that evening. Details are on [Arclight runtime](arclight.md). The patch
hides the symptom. A signal stop still runs the shutdown hook beside the server thread, see
[Stop path](#stop-path).

### A BattleTowers obelisk on the Mac mini (2026-10-31 21:12:33)

The report `crash-2026-10-31_21.12.33-server.txt` ("Ticking entity") came 26 seconds after a test
client joined at 21:12:07. A `ba_bt:land_obelisk` at 392.5 68 408.5, about 76 blocks from the world
spawn, threw `IndexOutOfBoundsException: Index 7 out of bounds for length 7` in
`BTAbstractObelisk.checkSpawners` (`BTAbstractObelisk.java:567`, report line 13). The bug is in
BrassAmber BattleTowers 3.0.0-beta3.2.1, and launchd started the server again at 21:12:42.

The fix is Forge's `removeErroringEntities = true` in `world/serverconfig/forge-server.toml` on the
Mac mini. An entity that throws in its tick is then removed instead of stopping the server, so this
obelisk goes the next time it throws, and its tower loses it. Arclight's mixins leave Forge's entity
tick guard alone, so the setting works on this server.

The first edit at 21:13 did not hold. Forge writes its in-memory server config back to the file at
every stop, and the file's time matched the `Finished unloading server configs` line of the stop at
21:35:48. The value was set again with the JVM gone at 21:35:54 and holds since the boot that reached
`Done` at 21:36:32. So edit files in `world/serverconfig/` only while the server is down. The setting
lives in the world, so a new world needs it again, and the old world on the build machine lacks it. The
same holds for the server configs that some mods keep in `config/`, see
[Mekanism's ore switch](#mekanisms-ore-switch-lost-at-a-stop-2026-11-02-172012).

### Mekanism's ore switch lost at a stop (2026-11-02 17:20:12)

On 2026-11-02 at 17:19:06 a `config/Mekanism/world.toml` with tin and lead ore turned off reached the
Mac mini while the server ran. After the restart of 17:20 the file held `true` again, and its time fell
in the minute of the stop at 17:20:12. Mekanism keeps its server configs in `config/` instead of
`world/serverconfig/`, and Forge loads them and writes them back like the others. Its file watcher
noticed neither that copy nor an edit in place at 17:29. The file copied at 17:32:44, after the old JVM
had exited, holds since the boot that reached `Done` at 17:33:19.

The debug log names every config that Forge loads with the server, in the lines "Built TOML config for"
of the server thread. Outside `world/serverconfig/` they are `config/Mekanism/` (11 files),
`config/MekanismExtras/` (5), `config/ars_nouveau/` (81 glyph files), `config/ars_nouveau-server.toml`,
`config/colytra-server.toml`, `config/comforts-server.toml`, `config/veinmining-server.toml` and
`config/veinmining-common.toml`. Edit them, like `world/serverconfig/`, only with the java process gone.

### Launch failures without a report

These runs died before Forge could write a crash report.

- On 2026-06-27 at 12:01 (Red Hat OpenJDK 21.0.11, `boot.log`) and at 19:47 (Temurin 17.0.19,
  `boot6.log`) the launch failed because `irons_lib` was missing for `irons_spellbooks`
  (`boot.log:22395-22396`, `boot6.log:27-28`). Both runs then died with
  `NoClassDefFoundError: io/izzel/arclight/common/bridge/core/inventory/IInventoryBridge` and
  "Fail to launch Arclight." Installing `irons_lib-1.20.1-1.1.0.jar` fixed it. Older docs blamed this
  error on Java 21, see [Java 17 only](arclight.md#java-17-only).
- On 2026-06-27 at 19:50 (`boot7.log`) a critical mixin failure in Enhanced Celestials (`MixinBlock`,
  `m_49840_`) stopped the launch. The mod was removed.
- On 2026-06-29 at 15:19 (`logs/2026-06-29-8.log.gz`) a critical injection failure in VanillaBackport
  (`ThrownEgg::m_6532_`) stopped the launch. VanillaBackport and vb-compat moved to `disabled-mods/`.

### Runs that ended without a report

- The run that started on 2026-06-28 at 22:56 ends at 23:50:51 with a `/storm` command as its last
  line. There is no stop line and no crash report. The owner wrote at 00:25 that the server had crashed
  again. The cause is unknown, because the system journal does not reach back that far and no
  `hs_err` file exists.
- The run that started on 2026-06-30 at 08:09 ends on 2026-07-03 at 21:09:57 with
  `A single server tick took 60.90 seconds` (`logs/2026-07-03-2.log.gz:895`). Two minutes earlier the
  server logged `Can't keep up` with 83,487 ms, and the debug log shows Netty clock jumps of up to 35
  seconds. No crash report and no shutdown lines follow. The laptop most likely slept, or the process
  was killed.
- The run that started on 2026-07-12 at 23:25 ends on 2026-07-13 at 15:45:05 in the middle of a signal
  shutdown (see [Stop path](#stop-path)). No owner prompt falls into that window, so it was most likely
  a host shutdown. This stop last wrote `world/`.

Client crashes from the Prism launcher are on [Client](../modpack/client.md).

## Stop path

A clean stop sends `stop` over RCON, with `mcrcon` or with
`python3 tools/rcon.py <host> <port> <password> stop`. The server thread then runs the Forge stopping
handlers (ChunkOpt flushes its deferred queue), saves players and worlds, unloads configs and closes
RCON. Arclight's exit thread ends the process 5 to 9 seconds after `Stopping server`. That holds for
every logged RCON stop from 2026-06-27 to 2026-07-12.

A signal stop takes another path. SIGTERM or SIGINT start Arclight's `Server Shutdown Thread`. It skips
the stopping handlers and closes the server while the server thread may still be ticking. All three
signal stops in the logs went wrong.

- On 2026-06-27 at 12:08:12 the shutdown save hit an `ArrayIndexOutOfBoundsException` inside
  `DistanceManager` (`boot3.log:4009-4010`).
- On 2026-06-29 at 19:41:29 the shutdown raced the stuck server thread (the crash above), and four
  chunks failed to save.
- On 2026-07-13 at 15:45:05 the log stops 0.3 seconds into the shutdown while chunks were still being
  saved. The final steps of a normal stop never appear.

The hardened jar only catches the iterator exceptions. So the rules are these.

- Stop the server with RCON `stop` and wait until the java process has exited before you touch
  `world/`. In a foreground run, type `stop` into the console instead of pressing Ctrl+C.
- Do not stop it with `kill` or a service stop that sends SIGTERM first. The systemd unit sends `stop`
  over RCON through `ExecStop`, see [Deploy](deploy.md). For launchd see the [Mac mini page](macos.md).
- Kill the process only if it is still alive after the exit thread line. By then the world is saved.

## Boot error baseline

A clean boot reaches `Done` with 0 FATAL lines and the 3 accepted ERROR lines listed below. That is
the baseline. WARN lines are not part of it, because their number moves with the mod set and jumps at
the first boot of a new world or of new mods, so each boot below has its own WARN count.

The last complete boot on the build machine started on 2026-07-12 at 23:25:09 and reached `Done` at
23:26:00 (`logs/2026-07-12-1.log.gz`). Counted up to the `Done` line it shows this.

| Level | Count | Notes |
|---|---:|---|
| FATAL | 0 | None |
| ERROR | 3 | All three are accepted, see below |
| WARN | 304 | Mostly third-party data and mixin noise |

The three accepted errors are these.

- Patchouli cannot load the `simplyjetpacks:guidebook` book, because the book uses a format that was
  removed in 1.20 (line 1450). Only the mod author can fix it.
- AllTheLeaks fails to instantiate a constructor after it cannot get a VarHandle for Citadel's
  `CitadelServerData.dataMap` (line 1599). Only its reflection into Citadel fails. Nyx has nothing to do
  with it.
- Essentials warns that it runs on a hybrid server ("You are running a server that does not properly
  support Bukkit plugins", line 3185). This is accepted, since EssentialsX is the plugin layer.

The 304 WARNs of that boot come mostly from LootDataManager (109, of them 108 loot table validation
problems in `cataclysm_spellbooks` entity tables and 1 in `deadlyworld`), Mantle (36, a deprecated JSON
key in Mystical Agradditions recipes for Tinkers), mixin (35, missing refmaps and overwrite conflicts),
KubeJS (31), Aether Redux (30, duplicate biome holders), Create (15) and Apotheosis Adventure (8), plus
a tail of smaller loggers.

KubeJS loaded 4 of 4 server scripts with 0 errors. Its summary line reads "Added 2 recipes, removed 0
recipes, modified 14 recipes, with 17 failed recipes" (`logs/kubejs/server.log`, 23:25:52). The 17
failures are third-party recipes whose items are missing. There are no startup scripts.

The baseline has been stable since 2026-06-30. Boot ERRORs stood at 152 from 2026-06-29 15:22 to
2026-06-30 04:11, after the owner asked at 04:15 for every boot error and warning to be resolved. The
override datapack and the jar patches brought them to 27 (05:23), 10 (06:17) and 3 (07:04). The boots
of 2026-06-30 08:09, 2026-07-12 18:11 and 2026-07-12 23:25 all show 3. The boot right after the
Re:Avaritia removal (2026-06-30 08:08) showed 9, the usual 3 plus 6 one-time `Unidentified mapping`
errors that cleared on the next boot. How the fixes work is on [Boot fixes](../content/boot-fixes.md).

On the build machine, boot WARNs settled at 303 to 331 per boot from 2026-06-29 15:36 on. One-time
spikes follow added mods, when registry IDs are remapped: 21,437 at 2026-06-29 00:22, 443 at 16:38 the
same day, 1,393 at 2026-06-30 06:06 and 548 at 06:54.

The six boots on the Mac mini of 2026-10-31 are the first with the mod update of that day, and all six
keep the baseline. Counted up to their `Done` lines:

| Boot | Done after | FATAL | ERROR | WARN |
|---|---:|---:|---:|---:|
| 18:07:17, first boot of the Halloween world, started by hand | 38 s | 0 | 3 | 1,658 |
| 18:54:57, first boot under launchd | 32 s | 0 | 3 | 387 |
| 19:26:53, 19:27:46 and 19:33:32, the stop tests | 32 s each | 0 | 3 | 344 each |
| 19:35:14, after the test reboot | 35 s | 0 | 3 | 344 |

The three ERRORs are the accepted ones above. In the first boot, 1,316 WARNs came from Forge's config
loader, which filled the new `world/serverconfig/` files with their defaults ("Incorrect key ... was
corrected from null to its default"). The second boot shows 45 of them. The rest of the WARNs follow
the same loggers as on the build machine, with 109 from LootDataManager, 39 each from Tinkers'
Construct and mixin, 37 from Mantle and 30 from Aether Redux.

### Runtime noise after boot

The last run, from 2026-07-12 23:26 to 2026-07-13 15:45, logged this after `Done`.

- During the Skylands pregen (23:31 to 23:34) vanilla logged 31 `SculkSensorBlockEntity` errors
  ("No key selector in MapLike"). This is codec noise on newly generated sculk sensors.
- Iron's Lib logged "Failed to handle Patreon Data response: java.net.ConnectException"
  (`logs/latest.log:453`). The mod calls out to the internet at runtime, and the host was offline.
- Essentials logged its "/reload" warning during the signal shutdown (`logs/latest.log:456`).
- Dungeons Arise logged 33 warnings "Empty or non-existent pool:
  dungeons_arise:mechanical_nest/mechanical_nest_decoration", and vanilla logged 5 "Empty height range"
  warnings and 2 tick lag lines.
- The LAN pinger warns whenever the host has no network ("LanServerPinger: Network is unreachable" or
  "Operation not permitted"). Forge advertises the dedicated server on the LAN because
  `world/serverconfig/forge-server.toml:19` sets `advertiseDedicatedServerToLan = true`. A public
  server does not need that.

The Mac mini runs of 2026-10-31 logged three kinds of ERROR after `Done`, all during the Chunky pregens.

- The same sculk codec noise, 131 `SculkSensorBlockEntity` and 5 `SculkShriekerBlockEntity` lines in
  the first run and 101 `SculkSensorBlockEntity` lines in the second.
- One "Failed to store chunk" from the `IOWorker` in each run, see the next section.

### Chunk save race during pregen

Each Skylands pregen on the Mac mini logged one `java.util.ConcurrentModificationException` while the
IO worker wrote a chunk: chunk [195, -171] at 18:30:52 in the first run and chunk [-14, -52] at
18:57:56 in the second. The stack runs from `IOWorker` through `RegionFileStorage.write` and `NbtIo`
into `CompoundTag.write`, which iterates the keys of the chunk's top level tag. The frames carry
Arclight's own mixin names (`RegionFileCacheMixin`, `CompoundTagMixin`), but the race is in vanilla
1.20.1. The bytecode of the server jar shows it.

- A chunk save hands its tag to the IO worker, which queues it and writes it later.
- When the same chunk is read again before the write, `IOWorker.loadAsync` returns the queued tag
  itself, not a copy.
- The loader, `ChunkMap.readChunk`, runs `ChunkStorage.upgradeChunkTag` on a background worker. That
  method puts a `__context` key into the top level tag and removes it again.
- If the IO worker writes the tag in that moment, its key iteration fails, and the queued write is
  dropped.

A pregen saves and reloads chunks at their intermediate generation steps all the time, so it hits the
race far more often than play does. Chunk [-14, -52] is in the Skylands region file with status
`full`, saved at 18:58:00, four seconds after the failure, so nothing was lost there. Chunk
[195, -171] lies just outside the radius 3000 square and only ever reaches an early generation step.
Its first run copy went with the wipe before the second run. The chunk that was read back holds the
full data in memory, and its next save writes it. A loss needs a chunk with changes that is saved,
read back during the write and then unloaded again without another change. The disk then keeps the
state before the dropped write. The build machine's logs never show the error, but its three logged
full pregens covered 48,387 chunks, about a seventh of the 319,979 that the Mac mini generated.

Steam 'n' Rails also mixes into `IOWorker.store`, where it adds its `Railways_DataVersion` key. That
happens before the tag is queued and is not part of the race. A fix would hand out a copy of the
queued tag in `IOWorker.loadAsync`, in the hardened Arclight jar or in the compat mod. Nothing is
built yet, see [Open items](../open-items.md#defects-to-fix).

## Tick lag and the watchdog

The line `Can't keep up` appears 29 times across all logs, in three kinds.

- A single spike of 2.1 to 2.5 seconds a few seconds after `Done` shows up in most runs. It is harmless.
- Host sleep shows up as one large gap on wake: 7,091 ms on 2026-06-28 at 17:56 after 20 hours without
  a line, 4,866 ms on 2026-07-02 at 08:53, and 83,487 ms on 2026-07-03 at 21:07, which the watchdog
  line that ended the run followed.
- Real load comes from the `/locate` sweeps (up to 9,441 ms on 2026-06-29 at 19:37) and from fresh
  Skylands terrain while the owner explored (5,705 ms on 2026-06-28 at 18:00, 6,082 ms at 22:41).

Outside sweeps, pregeneration and sleep the logs show no tick lag. Spark measured 20.0 TPS at idle, see
[Performance](performance.md). The watchdog (`max-tick-time=60000`, `server.properties.example:31`)
fired twice, on 2026-06-29 at 19:41:18 and on 2026-07-03 at 21:09:57. A server host must never sleep.

## Live risks for any host

- Asgard Shield Reloaded needs the patch of `tools/patch_mods.py` after every update of its jar, or hits
  on players fail and explosions can crash the server again.
- Signal stops race inside Arclight's shutdown hook. Stop over RCON.
- Host sleep stalls the server and ended its longest run. Keep the host awake.
- Bulk `/locate` sweeps trip the watchdog. Keep them off the live server.
- The script `tools/resolve_all.py` would bring Radium back. Do not use it.
- Only one player account ever joined in the logs, so load with 20 players is untested.
- A vanilla race drops a queued chunk write when the chunk is read back during it. It showed up once
  per Skylands pregen on the Mac mini and lost nothing so far, see
  [Chunk save race during pregen](#chunk-save-race-during-pregen).

Related: [Arclight runtime](arclight.md), [Deploy](deploy.md), [Performance](performance.md),
[Boot fixes](../content/boot-fixes.md), [Open items](../open-items.md).
