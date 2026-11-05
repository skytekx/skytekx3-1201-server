---
type: Index
title: Modpack
description: What the SkyTekx 3 pack is made of and how it is kept up, from the mod set and the recipe layers to the scripts, the plugin layer, the owner's client and the Modrinth release.
tags: [modpack, index]
timestamp: 2026-11-05
---

# Modpack

SkyTekx 3 is a Forge 1.20.1 pack. Its versions 3.0.6 and 3.0.7 went up to the Modrinth project on
2026-10-31, the second with the client mod Modflared for the public address, and the project is still
a draft (see [publish](publish.md)). Its mods come from the Modrinth index and from 52 bundled jars,
and the server runs them on Arclight together with one Bukkit plugin (see
[server](../server/index.md)). What was built for the pack itself, such as the Skylands, is under
[content](../content/index.md).

| Page | What it covers |
|---|---|
| [Mod set](mod-set.md) | The Modrinth index, the bundled jars, the sides, the jars outside the index, the parked mods and the mod update of 2026-10-31 |
| [Ender IO backport](enderio-backport.md) | The plan to bring an additive subset of Ender IO 8.2.x onto the vendored 6.x fork in `ports/enderio/`, with its bundles, exclusions, license conditions and savegame rules |
| [Create Crafts & Additions backport](createaddition-backport.md) | The plan to bring the WireGraph, the fixes and the new content of the 1.7.x line onto the vendored 1.20.1 fork in `ports/createaddition/`, with its bundles, exclusions, license conditions, savegame and operator notes |
| [Actually Additions backport](actuallyadditions-backport.md) | The plan to make the unreleased 1.20.1 branch of Actually Additions in `ports/actuallyadditions/` release quality and to bring the fixes and content of the newer lines up to 1.3.26 onto it, with its bundles, exclusions, license conditions, savegame, Arclight and operator notes |
| [NeoForge ports](neoforge-ports.md) | The mods that only exist for NeoForge 1.21.1 or only on Fabric for 1.20.1 and how each one reaches the Forge pack, starting with Bygone Nether 1.3.2 in place of Eternal Nether, the Mystical Automation port and the Mekanism: Extra Modules port, with source, license conditions, tests and operator steps |
| [Mystical Automation port](mysticalautomation-port.md) | The Forge 1.20.1 port of the NeoForge only Mystical Automation 1.0.8 in `ports/mysticalautomation/`, its changes against upstream, its tests on Forge and Arclight and its deploy |
| [Expanded Delight port](expandeddelight-port.md) | The Forge 1.20.1 port of Expanded Delight 0.1.4 in `ports/expandeddelight/`, its content, the Farmer's Respite files that stay out, its tags with Pam's Food Core and Mekanism, its worldgen and its test |
| [Draconic Evolution fixes](draconic-fixes.md) | Why Draconic Evolution stays the stock 1.20.1 build and which upstream fixes the pack's addon SkyTekx DE Fixes applies to it |
| [Mekanism Extra Modules port](mekanism-extra-modules.md) | The Forge 1.20.1 port of a 1.21.1 addon with three MekaSuit body armor modules, its Mekanism 10.4 adaptation, tests, license and operator steps |
| [AlmostUnified](almostunified.md) | One item per material across the tech mods, the successor of UniDict |
| [KubeJS](kubejs.md) | The four server scripts, the unfinished port of the SkyTekx 2 CraftTweaker scripts and how to work with Rhino |
| [Plugins](plugins.md) | The Bukkit layer on Arclight, which is EssentialsX alone, with its permissions and its metrics |
| [Tools](tools.md) | The scripts in `tools/`, from datapack sync and mod updates through jar patches and the build to the Modrinth upload and RCON |
| [Client](client.md) | The owner's Prism instance on Asahi Linux, its Java flag, the custom main menu, the client crash history and how a player joins from outside the LAN |
| [Publish](publish.md) | The Modrinth project, what blocks a public release, the release steps and the version history |

## Recipe layers

SkyTekx 2 on 1.12.2 kept recipes free of conflicts with three mods, and on 2026-06-27 10:34 the owner
asked that recipes never conflict in SkyTekx 3 either (`~/.claude/history.jsonl`, session 5bd3e1de).
Each layer has a modern counterpart:

| Job | SkyTekx 2 | SkyTekx 3 |
|---|---|---|
| One item per material | UniDict 3.0.10 | [AlmostUnified](almostunified.md) 0.11.0 |
| Choice when two recipes match the same grid | NoMoreRecipeConflict 0.13 | Polymorph 0.49.11 |
| Recipe and tag changes by script | CraftTweaker 4.1.20 | [KubeJS](kubejs.md) 2001.6.5 |
| Recipe viewer | JEI 4.16.1 | EMI 1.1.24-skytekx.2 (fork with a JEI 15.74 bridge) and JEI 15.74.0.233 |

The SkyTekx 2 versions are those of the jars in `~/git/skytekx2-1122-server/mods/`. Polymorph and JEI
show the builds since the update of 2026-10-31 (see [mod set](mod-set.md#update-of-2026-10-31)).
How the three layers work together is on [AlmostUnified](almostunified.md#how-the-recipe-layers-fit-together).

## Mac mini

The pack needs nothing special on the Mac mini. The script `macos/sync-to-mac.sh` copies `mods/`, the
configs, the Essentials config, the datapack sources and the fixes pack from this checkout. The build and upload scripts
only run on the build machine, see [tools](tools.md#mac-mini). The details are on
[macOS host](../server/macos.md).

Related: [server](../server/index.md), [content](../content/index.md), [open items](../open-items.md),
[decisions](../decisions.md).
