---
type: Runtime
title: Arclight runtime
description: Forge 1.20.1 + Bukkit hybrid loader; Java 17 only.
tags: [server, arclight, java]
timestamp: 2026-11-02
---

# Arclight runtime

The server runs on **Arclight** (a forge-1.20.1 build) — a Forge + Bukkit/Spigot hybrid. This lets the
Forge mod set run beside the Bukkit plugin layer (EssentialsX, LuckPerms, Multiverse, WorldEdit).
Documented fallback if Arclight proves unstable: Mohist 1.20.1, then pure Forge.

## Java 17 only
Arclight 1.20.1 **crashes on Java 21** with `IInventoryBridge` `NoClassDefFound`. Install
`openjdk-17`; never bump the JVM. This constraint is encoded in [`ServerLinux.sh`](../../ServerLinux.sh)
and the [systemd unit](deploy.md).

## The jar
`arclight.jar` is a symlink to the **tracked** `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`
(sha256 `c55ad477b92250ef31baf965aae3158ffa99239b9b44a60b3a9dd5206dc1557f`). Launch is
`-jar arclight.jar nogui`. Do not swap in a release or snapshot jar from upstream: it lacks the fix below.

It is the upstream Trials snapshot `1.0.6-SNAPSHOT-6de9fec` with one method replaced:
`DistanceManagerMixin.arclight$safeIter` iterated the pending chunk-holder set with an unguarded
`do { iter.next() } while (iter.hasNext())`, which crashes the tick loop with `NoSuchElementException`
when worker threads change the set. The hardened body
([`patches/arclight-trials-distancemanager-safeiter.patch`](../../patches/arclight-trials-distancemanager-safeiter.patch))
guards with `hasNext()` and re-acquires the iterator on `NoSuchElementException` /
`ConcurrentModificationException`.

### The extraction cache (why `safeiter2`)
On start Arclight extracts its embedded `common.jar` to `.arclight/mod_file/<Implementation-Version>.jar`
**only if that file is missing**, never checks its content, and deletes the other files there
(`-Darclight.alwaysExtract=true` forces it). The first patched jar (`…-safeiter`, 88552a9) was built from
source and its `common.jar` was not reobfuscated, so a fresh install died with a `MixinTransformerError`.
Production never noticed because that jar kept the upstream version string: the server reused the
cached upstream `common.jar`, and the fix was never live. `safeiter2` carries
`Implementation-Version: arclight-1.20.1-1.0.6-SNAPSHOT-6de9fec-safeiter2`, so the first boot replaces
the stale cache. **Any custom Arclight build must change the Implementation-Version.**

After a deploy, check:
- `ls .arclight/mod_file` shows only `arclight-1.20.1-1.0.6-SNAPSHOT-6de9fec-safeiter2.jar`, and the
  startup banner ends in `…-safeiter2` (Bukkit `/version` still reports `…-6de9fec`; that string comes from
  inside `common.jar`).
- Optional deep check: start once with `-Dmixin.debug.export=true
  -Dmixin.debug.export.filter=net.minecraft.server.level.DistanceManager -Dmixin.debug.export.decompile=false`,
  then `javap -c -p .mixin.out/class/net/minecraft/server/level/DistanceManager.class`: `m_140805_`
  (`runAllUpdates`) must call `redirect$…$arclight$safeIter` and contain no `Set.forEach`.

Verified on 2026-11-02 with the full pack (272 mods, fresh world, chunk generation in the overworld, the
Twilight Forest and the Skylands): `Done`, 0 FATAL / crash / watchdog, no error signature that plain Forge
47.4.18 does not log too, and an upgrade boot over a stale upstream `.arclight` cache re-extracted
`safeiter2`. Re-checked on the release tree of the TF 4.8 backport (79acbd2, 274 mods, same chunk
generation): `Done`, 0 FATAL / crash / watchdog with `max-tick-time=180000`; at the default 60000 the
test harness's synchronous 145-chunk `forceload` tripped the watchdog after `Done`, on this host with the
older pack too (details in the [TF backport](../modpack/twilightforest-backport.md#in-the-pack-2026-11-01)).
One known mod conflict, present before as well: Arclight's `arclight$mutePotion` redirect
wins over Apothic Attributes' `apoth_sunderingHasEffect`, so Apothic's Sundering bonus damage does not
apply on Arclight (it does on plain Forge).

### Rebuilding it
Do not rebuild Arclight from source for this (that is what broke `safeiter`). Run:

```bash
# upstream snapshot (official Arclight file host), sha256 578f5866…afeb1299:
#   https://files.hypoglycemia.icu/v1/files/arclight/branches/Trials/versions-snapshot/1.0.6-SNAPSHOT-6de9fec/forge
tools/arclight/rebuild_safeiter.sh /path/to/arclight-forge-1.20.1-1.0.6-SNAPSHOT-6de9fec.jar [libraries/org/ow2/asm]
```

It takes the hardened method from the jar in commit 88552a9, transplants it into the upstream class
(`tools/arclight/Transplant.java`, which rejects any donor code that references a Minecraft member and
would need remapping), swaps that class and the version string (`tools/arclight/repack.py`), repoints
`arclight.jar`, and checks that the result matches the tracked jar entry by entry. Needs JDK 17, python3,
unzip and an ASM 9.x install (any Arclight/Forge 47 server `libraries/` folder has one).

## Mods vs plugins
- Forge mods load only from `mods/` (gitignored). It is assembled from the [index](../modpack/mod-set.md)
  downloads plus a copy of the tracked `local-mods/*.jar`; nothing points Forge at `local-mods/` directly.
- Bukkit plugins load from `plugins/` (gitignored; EssentialsX et al.).

Related: [deploy](deploy.md), [mod set](../modpack/mod-set.md), [world reset](world-reset.md).
