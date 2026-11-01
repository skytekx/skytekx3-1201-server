---
type: Runtime
title: Arclight runtime
description: The hardened Arclight 1.20.1 jar built from patches/, the arclight.jar symlink, the Java 17 rule, how mods and plugins load and why the server must stop over RCON.
tags: [server, arclight, java, forge, bukkit]
timestamp: 2026-11-01
---

# Arclight runtime

The server runs Arclight, a hybrid that loads Forge mods and Bukkit plugins in one JVM. The build is
Arclight 1.20.1 version 1.0.6 from the Trials line at upstream commit 6de9fec, with one local patch. It
embeds Forge 47.4.18 for Minecraft 1.20.1. The SkyTekx launchers all run
`java ... -jar arclight.jar nogui`, see [Deploy](deploy.md) and
[Mac mini (macOS, launchd) deployment](macos.md).

## The hardened jar

Stock Arclight 1.0.6 crashed the server on 2026-06-29 at 19:41:29 with a `NoSuchElementException` in
`DistanceManagerMixin.arclight$safeIter`. That mixin walks the set of pending chunk holders in a
do-while loop that called `iter.next()` without a guard and only refreshed the iterator after an add.
When a second thread changed the set, `next()` threw and killed the tick loop. The full crash story is
on [Logs and crashes](logs-and-crashes.md).

The fix is `patches/arclight-trials-distancemanager-safeiter.patch`. It turns the loop into
`while (iter.hasNext())` and catches `NoSuchElementException` and `ConcurrentModificationException`. On
either exception it takes a fresh iterator and goes on instead of dying. Commit `88552a9`
(2026-06-29 22:11) added the patch, the rebuilt jar and the new symlink target. That evening the owner
had asked why the server crashed and for the underlying issue to be fixed (21:22), then whether a
backport from newer versions could harden it (21:32), and finally to commit the hardened jar together
with the symlink swap (22:09).

- The rebuilt jar is `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar` with 5,735,808 bytes. It is
  tracked in git.
- Its manifest says `Implementation-Version: arclight-1.20.1-1.0.6-SNAPSHOT-6de9fec` and
  `Implementation-Timestamp: 2026-06-29 21:50:26`. Compared with the upstream jar only the manifest and
  the nested common jar differ.
- At boot the Arclight banner shows `Build Date 2026-06-29 21:50:26`. The stock jar showed
  `Build Date 2026-05-07 12:21:25`. The first boot on the hardened jar was 2026-06-29 at 21:55, and every
  boot since has used it.

Check the jar after copying it to a new host. Use `sha256sum` on Linux or `shasum -a 256` on macOS.

```text
558a5f549ed5f1ecb1548b62e67ac28eed79c157cabd9a3899568b9ae2a5d6fa  arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar
```

### Rebuilding it

The body of `88552a9` gives the recipe. Apply the patch to IzzelAliz/Arclight on the Trials line at
commit 6de9fec and build with JDK 17. No Arclight source checkout survives on the build machine, and
nobody has repeated the build since 2026-06-29. The patch file is the only record of the change.

```bash
git clone https://github.com/IzzelAliz/Arclight.git
cd Arclight && git checkout 6de9fec
git apply ../skytekx3-1201-server/patches/arclight-trials-distancemanager-safeiter.patch
# Build with JDK 17 through the repo's Gradle wrapper, then take the forge 1.20.1 server jar
```

### What the patch does not fix

The commit message blames worker threads. In the crash the second thread was Arclight's
`Server Shutdown Thread`, which ran after the watchdog had fired. The patch stops the exception. It
does not stop the shutdown hook and the server thread from working on chunks at the same time. That is
why the server must stop over RCON, see [Stop path](#stop-path).

## The arclight.jar symlink

- The file `arclight.jar` is a symlink tracked in git (mode 120000). Its relative target is
  `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar`. Until `88552a9` it pointed at the stock
  `arclight-forge-1.20.1-1.0.6-6de9fec.jar`.
- Both the link and the hardened jar were force-added. The ignore rule `arclight-*.jar`
  (`.gitignore:20`) still matches the hardened jar, so any future jar needs `git add -f`.
- The stock jar `arclight-forge-1.20.1-1.0.6-6de9fec.jar` is gitignored and only exists on the build
  machine. The untracked `arclight-forge-1.20.1-1.0.6-6de9fec.jar.orig` is a byte-identical copy of it.
  The server needs neither.
- Never point the link at a stock Arclight release, because that brings the tick loop crash back. The
  old README step that downloaded a release and relinked `arclight.jar` did exactly that.
- Git checkouts and `rsync -a` keep the relative link intact.

## Java 17 only

Run the server on Java 17. Every boot on the build machine that reached `Done` ran Temurin 17.0.19
(Eclipse Adoptium) on aarch64, as all six crash reports and every ModLauncher start line there show.
On the Mac mini every boot of 2026-10-31 ran Temurin 17.0.21, as the ModLauncher start lines show.

Older docs gave a different reason, a Java 21 crash with `IInventoryBridge` `NoClassDefFound`. The logs
do not support it. The only Java 21 boot (Red Hat OpenJDK 21.0.11, `boot.log`, 2026-06-27 12:01) failed
because the mod `irons_lib` was missing (`boot.log:22395-22396`). The `IInventoryBridge` error only
followed from that failed mod load. The same error hit a Java 17.0.19 boot at 19:47 the same day
(`boot6.log:27-28` and `boot6.log:61`) with the same missing `irons_lib`. Java 21 was never tried again
after `irons_lib` was installed.

So Java 17 stays the rule because it is the only version with a successful boot on record. Whether this
Arclight build and mod set run on Java 21 is unknown. Test a boot on a copy of the server before any
change. The Forge 47.4.18 installer itself ran fine on Java 21.0.11 (`installer_stripped.jar.log`), so
only the server runtime is pinned.

Both `ServerLinux.sh` and the systemd unit default to `/usr/lib/jvm/java-17-openjdk/bin/java`. That path
follows Fedora naming, and the build machine does not have it. There the server ran on a Temurin 17
unpacked in the user's home directory. Debian and Ubuntu install the package under
`/usr/lib/jvm/java-17-openjdk-<arch>`, see [Deploy](deploy.md). The macOS launcher is covered on the
[Mac mini page](macos.md).

## Mods and plugins

Forge loads jars from `mods/` only. The folder `local-mods/` is the tracked store for jars that are not
on Modrinth or were built here. It is not a mods folder. Its 35 jars reach the server because copies
sit in `mods/`, and the debug log lists mod candidates under `mods/` only.

Git ignores `mods/`. On the build machine it held 236 jars after the mod update of 2026-10-31 and holds
234 since 2026-11-01 08:09. That day Placeable Items, Get It Together Drops, Sophisticated Backpacks
and Sophisticated Core left, and Falling Meteors Reimagined and the Backpack port came in. The folder
matches the Mac mini's jar for jar:

- The server side of `modrinth.index.json`, without Sophisticated Backpacks and Sophisticated Core,
  whose entries stay in the index until the next pack.
- Copies of the `local-mods/` jars.
- Three jars that exist nowhere else in the repo, `ba_bt-1.20.1-3.0.0-beta3.2.1.jar` (BrassAmber
  BattleTowers), since 2026-11-01 `meteors-2.5-forge.jar` (Falling Meteors Reimagined) and
  `backpack-1.20.1-2.0.1.1.jar` (the Backpack port, built in the local repo `skytekx-backpack`). Up to 2026-10-31 `alltheleaks-1.1.1+1.20.1-forge.jar` and
  `cataclysm_spellbooks-1.2.9-1.20.1.jar` were in that position too. Since 507a8f2 they are in
  `local-mods/`.

Ten of these jars are patched in place by `tools/patch_mods.py`, so they differ from the published
files. Copy this folder as it is to a new host. Details are on [Mod set](../modpack/mod-set.md) and
[Boot fixes](../content/boot-fixes.md).

Arclight loads Bukkit plugins from `plugins/`. Only EssentialsX 2.22.0 is installed. It arrived on
2026-06-27 at 21:50, right after the owner asked why short commands like `/gm` did not work. The jar
and its data folder are untracked, and `plugins/` is not in `.gitignore`. Only
`plugins/PluginMetrics/config.yml` is tracked. LuckPerms, Multiverse and WorldEdit, which older docs
listed, are not installed. Essentials logs one ERROR at every boot because it runs on a hybrid, and that
error is accepted. Details are on [Plugins](../modpack/plugins.md).

## Stop path

Stop the server with RCON `stop`, never with a signal. An RCON stop runs on the server thread, fires the
Forge stopping handlers, saves everything and exits 5 to 9 seconds later. SIGTERM or SIGINT instead
start Arclight's `Server Shutdown Thread`, which closes the server while the server thread may still
tick. All three signal stops in the logs went wrong. The evidence is on
[Logs and crashes](logs-and-crashes.md#stop-path).

## Configuration files

Arclight, Bukkit and Spigot read `arclight.conf`, `bukkit.yml`, `spigot.yml` and `commands.yml` from the
repo root. All four were committed once in `5ae7f82`, so they are close to generated defaults. Only
`spigot.yml` changed since.

- The file `arclight.conf` sets the async catcher to `defaultOperation=block` with `warn=true`, the
  source of the `Async chunk entity get` warnings. With `dump=true` the stack of each one goes to
  `debug.log`. Infernal Mobs caused all of them until skytekx-compat 2.1.3 moved its queries to the
  server thread on 2026-11-01, see [SkyTekx Compat](../content/compat-mod.md#infernal-mobs-queries). It keeps the data fixer on
  (`disable-data-fixer=false`) and sets `goal-selector-update-interval=2` and
  `use-activation-and-tracking-range=true`.
- The file `bukkit.yml` sets the spawn limits (monsters 70, animals 10, water animals 5, water ambient
  20, ambient 15), an autosave every 6000 ticks and chunk garbage collection every 600 ticks.
- The file `spigot.yml` sets entity activation ranges of 32 for animals and monsters, 48 for raiders
  and 16 for the rest, a tracking range of 48 for players, animals and monsters, and 4 Netty threads.
  It also sets `restart-on-crash: true` with `restart-script: ./start.sh`, a script that does not
  exist. Restarts come from the service manager instead. Since 2026-11-01 06:10 it sets
  `merge-radius.item` to 0.5 instead of 2.5 (a4bcaf7). Arclight inflates the merge box of a dropped
  item by that radius in every direction, vanilla by 0.5 sideways only, and each merge hands the
  longer pickup delay to the pile. Get It Together Drops had replaced this code with its own radius of
  2 until it left the pack the same day.
- The live `server.properties` is gitignored because it holds the RCON password, see
  [Secrets](secrets.md).

The Forge installer also left `run.sh`, `run.bat` and `user_jvm_args.txt` behind. These stock launchers
start plain Forge 47.4.18 without Arclight, so they skip the Bukkit layer and the hardened jar. Do not
use them.

## History

- The pack moved to Minecraft 1.20.1 with Forge and Arclight after the owner compared 1.20.1 with 1.21
  and Arclight with Mohist on 2026-06-26, and asked on 2026-06-27 whether Arclight was a good option.
  See [Decisions](../decisions.md).
- The first docs named Mohist 1.20.1, then plain Forge, as fallbacks in case Arclight proved unstable.
- At the first crash on 2026-06-29 the owner asked for an Arclight upgrade (21:27) and then for a
  backport to harden it (21:32). The answer was the local patch above.
- Between 23:00 and 23:36 that night the owner looked at Sponge as a better maintained platform and
  asked why it does not exist for 1.20.1. The server stayed on the hardened Arclight, and at 23:41 the
  owner noted that it is hardened anyway and moved on to measuring mod performance.
- The old version of this page said that `arclight.jar` points at a gitignored download from the
  Arclight releases and that Forge also loads `local-mods/`. The first has been wrong since `88552a9`,
  the second was never true.

Related: [Deploy](deploy.md), [Logs and crashes](logs-and-crashes.md), [Mod set](../modpack/mod-set.md),
[Plugins](../modpack/plugins.md), [World reset](world-reset.md).
