---
type: Runtime
title: Arclight runtime
description: Forge 1.20.1 + Bukkit hybrid loader; Java 17 only.
tags: [server, arclight, java]
timestamp: 2026-06-29
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
`arclight.jar` is a symlink to the downloaded `arclight-forge-1.20.1-<ver>.jar` (the versioned jar is
gitignored — grab it from the Arclight releases). Launch is `-jar arclight.jar nogui`.

## Mods vs plugins
- Forge mods load from `mods/` (rebuilt from the [index](../modpack/mod-set.md)) + `local-mods/` (tracked).
- Bukkit plugins load from `plugins/` (gitignored; EssentialsX et al.).

Related: [deploy](deploy.md), [mod set](../modpack/mod-set.md), [world reset](world-reset.md).
