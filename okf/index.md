---
type: Project
title: SkyTekx 3
description: Knowledge base for SkyTekx 3, the Minecraft 1.20.1 modpack and server on Forge and Arclight, with its state on 2026-10-31, the stack at a glance and a map of every page.
tags: [skytekx, minecraft, modpack, server, overview]
timestamp: 2026-11-05
---

# SkyTekx 3

SkyTekx 3 is the third generation of SkyTekx, a modpack by ts4.party, the TeamSpeak for party, formerly
known as ts3.party. It mixes the tech of Tekkit, the adventure of Hexxit and the sky islands of
SkyFactory on Minecraft 1.20.1. The server runs Forge mods and Bukkit plugins side by side on Arclight,
and the pack brings its own Skylands dimension of floating islands. This wiki is for the agents and
people who work on it. The lineage is on [History](history.md).

## State on 2026-10-31

| Area | State | Details |
|---|---|---|
| Repo | Local master holds all commits of 2026-10-31 on top of GitHub's master 940f07e of 2026-07-12. None of them is pushed | [Related repos and services](related.md#this-repo-on-github) |
| Work of 2026-10-31 | Mod update (216 index entries), Halloween worldgen and server icon, Nyx blood moons in the Skylands, Battle Towers, strongholds, the illager campsite, petrified roots and windmill fields kept off the islands, the move to the Mac mini, the public address `hex.skytekx.com` and packs 3.0.6 and 3.0.7 on Modrinth | [Mod set](modpack/mod-set.md#update-of-2026-10-31), [Skylands worldgen](content/skylands-worldgen.md#changes-of-2026-10-31), [Mac mini](server/macos.md), [Publish](modpack/publish.md#version-history) |
| Host | An Apple M4 Mac mini with 16 GB under launchd since 2026-10-31, reachable in the LAN and since 21:13 through a Cloudflare Tunnel. Restart, stop, start, backup and a reboot were tested that evening | [Mac mini](server/macos.md) |
| Last complete boot | 2026-10-31 19:35:14 on the Mac mini after the test reboot, `Done` after 35 s with 0 FATAL, 3 ERROR and 344 WARN lines. The Mac's logs are not in the repo | [Logs and crashes](server/logs-and-crashes.md#boot-error-baseline) |
| World | The Halloween world on the Mac mini, created on 2026-10-31 at 18:07, pregenerated to radius 1500 in the Overworld and 3000 in the Skylands and first backed up at 19:34. The build machine's old world was last saved on 2026-07-13 15:45 and stays as it is | [World reset and pregen](server/world-reset.md), [Mac mini](server/macos.md#the-halloween-world) |
| Pack | Version 3.0.7 was built from git HEAD on 2026-10-31 at 21:54 and uploaded to the Modrinth draft at 21:55. It is 3.0.6 plus Modflared and a server list with `hex.skytekx.com`. A client still needs BrassAmber BattleTowers added by hand to join, as the owner decided at 21:48, and a 12 GB heap, since the 3.0.6 test client froze after the join with 8 GB (JEI 15.74.0.233 against EMI 1.1.24) | [Publish](modpack/publish.md#version-history), [test](modpack/publish.md#test-of-2026-10-31) |
| Modrinth | The project `skytekx3` is a draft and not public. Six All Rights Reserved jars and the JEI failure block a release. BattleTowers (owner's decision of 21:48) and Falling Meteors Reimagined (server since 2026-11-01 06:10) stay outside the pack | [Publish](modpack/publish.md#what-blocks-a-release) |
| Public address | The name `hex.skytekx.com` through a Cloudflare Tunnel since 2026-10-31 21:13. Players need the client mod Modflared, which pack 3.0.7 brings and which has no cloudflared for Linux on ARM. Nobody has joined through it yet. Voice chat only works in the LAN, and the whitelist stays off. The uplink is carrier grade NAT, so a port forward cannot work | [Mac mini](server/macos.md#public-access) |
| Website | Live on skytekx.com, last changed on 2026-06-30. Some claims on it are wrong | [Related repos and services](related.md#the-website-skytekxcom) |
| Players | Only the owner's account ever joined. Load with 20 players is unmeasured | [Server](server/index.md#load-and-players) |

What is still open, from owner rulings that were never built to unverified claims, is on
[Open items](open-items.md).

## Stack at a glance

| Part | Value |
|---|---|
| Game | Minecraft 1.20.1 with Forge 47.4.18 inside Arclight 1.20.1 1.0.6. The pack index asks clients for the same Forge build since a36207c |
| Server jar | The symlink `arclight.jar` on the hardened `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar`, built from `patches/` |
| Java | Java 17. The Mac mini runs Temurin 17.0.21, and every boot on the build machine that reached `Done` ran Temurin 17.0.19 |
| Mods | 215 Modrinth entries in `modrinth.index.json`, the client only Modflared among them, and 35 bundled jars in `local-mods/`. The server's `mods/` holds 235 jars that git does not track |
| Plugins | EssentialsX 2.22.0 and nothing else |
| Recipe layers | AlmostUnified, Polymorph and KubeJS, the successors of UniDict, NoMoreRecipeConflict and CraftTweaker in SkyTekx 2 |
| Own content | The Skylands dimension as a pure datapack, travel between it and the Overworld, the Nyx port, 1.7 style combat, the boot fixes and the small SkyTekx Compat mod |
| Operation | Over RCON on port 65535, which must never be exposed. Stop with RCON `stop`, never with a signal |

## Pages

### Top level

| Page | What it covers |
|---|---|
| [SkyTekx 3](index.md) | This page, the state of the project and the page map |
| [History](history.md) | The SkyTekx lineage since 2019, why the third generation runs on 1.20.1, ts3.party and ts4.party |
| [Change log](log.md) | What happened from 2026-06-26 to 2026-10-31, oldest first |
| [Decisions](decisions.md) | Every owner ruling with its date, its status and where it lives |
| [Open items](open-items.md) | Rulings not yet built, owner decisions, defects, unverified claims and stale docs |
| [Related repos and services](related.md) | The GitHub organisation, the forks, the website, Modrinth and the SkyTekx 2 audits |

### Server

| Page | What it covers |
|---|---|
| [Server](server/index.md) | Facts at a glance and the rules that prevent the known failures |
| [Arclight runtime](server/arclight.md) | The hardened jar and its patch, the Java 17 rule, how mods and plugins load, the configs |
| [Mac mini](server/macos.md) | The production host with its launcher, launchd jobs, firewall and backups |
| [Deploy on Debian (systemd)](server/deploy.md) | The Linux path with the systemd unit, mcrcon, ports, firewall, DNS and the files git does not hold |
| [Secrets](server/secrets.md) | Where the RCON password, the Modrinth token and the vault password live, and what to rotate |
| [World reset and pregen](server/world-reset.md) | Wiping dimensions, Chunky pregen with measured rates, datapack order and fresh worlds |
| [Performance and worldgen profiling](server/performance.md) | Spark profiles, heap and tick data, the open levers and the Skylands structure density |
| [Logs and crashes](server/logs-and-crashes.md) | Where the logs are, every crash with its cause, the stop path and the boot error baseline |

### Modpack

| Page | What it covers |
|---|---|
| [Modpack](modpack/index.md) | What the pack is made of and the recipe layers |
| [Mod set](modpack/mod-set.md) | The index, the bundled jars, the server's mods folder, the jars outside the index and how to change the set |
| [Draconic Evolution fixes](modpack/draconic-fixes.md) | Why Draconic Evolution is not backported and what the addon SkyTekx DE Fixes changes in the stock jar |
| [NeoForge ports](modpack/neoforge-ports.md) | Mods that only exist for NeoForge 1.21.1 or Fabric and how each one reaches the Forge pack, Bygone Nether in place of Eternal Nether first, then the Mystical Automation, Mekanism: Extra Modules and Expanded Delight ports |
| [Mystical Automation port](modpack/mysticalautomation-port.md) | The Forge 1.20.1 port of Mystical Automation 1.0.8 in `ports/mysticalautomation/`, its fixes over upstream, its tests on Forge and Arclight and its deploy |
| [Mekanism Extra Modules port](modpack/mekanism-extra-modules.md) | The pack's Forge 1.20.1 port of the MekaSuit modules Phase Guard, Chaos Anchor and Emergency Revival |
| [Expanded Delight port](modpack/expandeddelight-port.md) | The Forge 1.20.1 port of the Farmer's Delight addon Expanded Delight 0.1.4 in `ports/expandeddelight/`, the Farmer's Respite files that stay out, its tags with Pam's Food Core and Mekanism, its tests on Forge and Arclight and its deploy |
| [AlmostUnified](modpack/almostunified.md) | One item per material across the tech mods |
| [KubeJS](modpack/kubejs.md) | The four server scripts and how to work with Rhino |
| [Plugins](modpack/plugins.md) | EssentialsX, its files, permissions, metrics and update checks |
| [Tools](modpack/tools.md) | The eleven scripts in `tools/` and what each one really does |
| [Client](modpack/client.md) | The owner's Prism instance, the client-only mods, the main menu, the client crashes and how a player joins from outside the LAN |
| [Publish](modpack/publish.md) | The Modrinth project, what blocks a release, the release steps and the version history |

### Content

| Page | What it covers |
|---|---|
| [Content](content/index.md) | The content built for SkyTekx 3 itself |
| [Skylands worldgen](content/skylands-worldgen.md) | The floating islands dimension, a vanilla datapack generator that also runs on Spigot |
| [Skylands travel](content/aether-fall.md) | Falling off an island into the Overworld and climbing above the Overworld into the Skylands |
| [Nyx](content/nyx.md) | The port of Ellpeck's moon mod to Forge 1.20.1 |
| [Combat](content/combat.md) | The 1.7 style melee without attack cooldown |
| [Boot fixes](content/boot-fixes.md) | How the boot went from 152 errors to 3 |
| [SkyTekx Compat](content/compat-mod.md) | The pack's own small Forge mod, which guards Bird's Nests |

### Outside the wiki

| Page | What it covers |
|---|---|
| [README](../README.md) | The public overview and the self-hosting guides for Debian and macOS |
| [AGENTS.md](../AGENTS.md) | Rules, conventions and pitfalls for anyone who edits the repo |

## Where to start

- To learn the project, read [History](history.md), the state above and [Decisions](decisions.md).
- To pick up work, read [Open items](open-items.md).
- To run or move the server, read [Server](server/index.md), then [Mac mini](server/macos.md) or
  [Deploy on Debian (systemd)](server/deploy.md), and keep [Logs and crashes](server/logs-and-crashes.md)
  at hand.
- To change the mods, read [Mod set](modpack/mod-set.md), [Tools](modpack/tools.md) and
  [Boot fixes](content/boot-fixes.md).
- To change the Skylands, read [Skylands worldgen](content/skylands-worldgen.md),
  [World reset and pregen](server/world-reset.md) and [Performance](server/performance.md).
- To release the pack, read [Publish](modpack/publish.md) and [Secrets](server/secrets.md).

## Sources

The build sessions of June and July 2026 (session 5bd3e1de) were deleted. The pages rest on what
survived: the owner's prompts in `~/.claude/history.jsonl`, the git history of this repo and its
siblings, the server logs, crash reports and Chunky and Spark output on the build machine, the
`.mrpack` files, file times, the assistant's memory notes and public API reads on 2026-10-31. Pages say
where a claim was not verified. Times are local time on the build machine, CEST until 2026-10-25 and CET
after.

## History

The first index of 2026-06-29 said that the pack was published on Modrinth as 3.0.4, that Java 21
crashes Arclight, that LuckPerms, Multiverse and WorldEdit ride along with EssentialsX, and that the
index plus `local-mods/` are the source of truth for the mods. The Modrinth project was never public,
the Java 21 claim rests on one failed boot that had another cause, EssentialsX is the only plugin, and
the server ran three jars that were in neither, two of which the pack bundles since 507a8f2. The wiki
was rebuilt from the evidence on 2026-10-31.
