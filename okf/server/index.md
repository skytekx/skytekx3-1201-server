---
type: Index
title: Server
description: Running, deploying and operating the SkyTekx 3 server on Arclight, with the facts the other server pages build on.
tags: [server, ops]
timestamp: 2026-11-01
---

# Server

The SkyTekx 3 server runs Minecraft 1.20.1 on Arclight, a hybrid that loads Forge 47.4.18 mods and
Bukkit plugins in one JVM. It is operated over RCON. The production host is an Apple M4 Mac mini under
launchd. The Debian path with systemd stays documented for Linux hosts.

Up to 2026-07-13 every run happened on the build machine, an Asahi Linux laptop, where the server was
started by hand from the checkout. Since 2026-10-31 the server runs on the Mac mini, first by hand and
from 18:54 under launchd, with a new world. The systemd unit never ran on a logged host.

## Pages

| Page | What it covers |
|---|---|
| [Arclight runtime](arclight.md) | The hardened Arclight jar and its patch, the `arclight.jar` symlink, the Java 17 rule, how mods and plugins load, the stop path and the Arclight, Bukkit and Spigot configs |
| [Mac mini (macOS, launchd) deployment](macos.md) | The production host with its launcher, launchd jobs, firewall and backups |
| [Deploy on Debian (systemd)](deploy.md) | The Linux path with the systemd unit, `ServerLinux.sh`, mcrcon, ports, firewall, DNS and the files git does not hold |
| [Secrets](secrets.md) | Where the RCON password, the Modrinth token and the vault password live, and what must be rotated |
| [World reset and pregen](world-reset.md) | Wiping dimensions, Chunky pregen with measured rates, datapack order and what a fresh world needs |
| [Performance and worldgen profiling](performance.md) | Spark profiles, heap and tick data, the open levers and the Skylands structure density |
| [Logs and crashes](logs-and-crashes.md) | Where the logs are, how to read a run, every crash with its cause, the stop path and the boot error baseline |

The host setup from scratch (packages, user, mcrcon, clone) is in the README section
[Self-hosting the server](../../README.md#self-hosting-the-server).

## Facts at a glance

| Item | Value |
|---|---|
| Game | Minecraft 1.20.1 with Forge 47.4.18 inside Arclight 1.20.1 1.0.6 |
| Server jar | The symlink `arclight.jar`, which points at the hardened `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar` |
| Java | Java 17 only. Every boot that reached `Done` ran Temurin 17.0.19 on the build machine or 17.0.21 on the Mac mini |
| Heap | An 11G heap in `ServerLinux.sh` and the systemd unit. The Mac mini launcher `ServerMac.sh` sets 9G, since macOS needs about 3 GB of the 16 GB |
| Mods | 234 jars in `mods/`, which git does not hold, since the Backpack port replaced Sophisticated Backpacks and Sophisticated Core on 2026-11-01 at 08:09. Copy the folder as it is |
| Plugins | EssentialsX 2.22.0 and nothing else |
| Players | Up to 39 (`max-players=39`), online mode on, whitelist off. The owner kept the whitelist off for the public address on 2026-10-31 at 21:48 |
| Ports | Game on 25565/tcp, Simple Voice Chat on 24454/udp, RCON on 65535/tcp, which must never be exposed |
| Stopping | RCON `stop`, never a signal |
| World | The Halloween world of 2026-10-31 on the Mac mini, pregenerated to radius 1500 in the Overworld and 3000 in the Skylands. The old world stays on the build machine (518 MB, last saved on 2026-07-13 at 15:45) |
| Last complete boot | 2026-10-31 at 19:35:14 on the Mac mini under launchd after a test reboot, `Done` after 35 s with 0 FATAL, 3 ERROR and 344 WARN lines |
| Public address | The name `hex.skytekx.com` through the Cloudflare Tunnel `skytekx3-mac-mini` since 2026-10-31 21:13, joined with the client mod Modflared, which pack 3.0.7 brings, see [Mac mini](macos.md#public-access) |

## Rules that prevent the known failures

- Keep `arclight.jar` pointed at the hardened jar. A stock Arclight release brings back the tick loop
  crash of 2026-06-29, see [Arclight runtime](arclight.md#the-hardened-jar).
- Stop the server over RCON and wait for the java process to exit. Every logged signal stop went wrong,
  see [Logs and crashes](logs-and-crashes.md#stop-path).
- Move `mods/` to a new host as a copy. Do not rebuild it with `tools/resolve_all.py`, see
  [Deploy on Debian (systemd)](deploy.md#what-git-does-not-hold).
- Never open RCON port 65535 to the network. RCON binds every interface and sends its password in clear
  text, see [Secrets](secrets.md#rcon-exposure).
- Pregenerate after every world wipe, and keep `/locate` sweeps off the live server. A sweep tripped the
  watchdog on 2026-06-29, see [World reset and pregen](world-reset.md).
- Rotate the Modrinth token and the vault password before the next publish. Both were exposed, see
  [Secrets](secrets.md#exposed-values-to-rotate).

## Load and players

Only one player account ever joined in the logs. The median idle tick took 0.3 ms and TPS stayed at 20,
but the cost of 20 players is unmeasured. The open levers, a wider pregen and lower view and
simulation distances, are on [Performance and worldgen profiling](performance.md#levers).

## History

- The old index said the server is deployed under systemd. The unit never ran on a logged host, and the
  production host is now the Mac mini.
- The old index had no page for logs and crashes, and the old server pages mentioned the crashes only
  in passing. The crash history is now on [Logs and crashes](logs-and-crashes.md).

Related: [OKF index](../index.md), [Mod set](../modpack/mod-set.md), [Plugins](../modpack/plugins.md),
[Skylands worldgen](../content/skylands-worldgen.md), [Decisions](../decisions.md),
[Open items](../open-items.md).
