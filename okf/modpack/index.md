---
type: Index
title: Modpack
description: What the SkyTekx 3 pack is made of and how it is kept up, from the mod set and the recipe layers to the scripts, the plugin layer, the owner's client and the Modrinth release.
tags: [modpack, index]
timestamp: 2026-10-31
---

# Modpack

SkyTekx 3 is a Forge 1.20.1 pack, version 3.0.5. Its mods come from the Modrinth index and from 33
bundled jars, and the server runs them on Arclight together with one Bukkit plugin (see
[server](../server/index.md)). What was built for the pack itself, such as the Skylands, is under
[content](../content/index.md).

| Page | What it covers |
|---|---|
| [Mod set](mod-set.md) | The Modrinth index, the bundled jars, the sides, the three jars outside the pack, the parked mods and the mod update of 2026-10-31 |
| [AlmostUnified](almostunified.md) | One item per material across the tech mods, the successor of UniDict |
| [KubeJS](kubejs.md) | The four server scripts, the unfinished port of the SkyTekx 2 CraftTweaker scripts and how to work with Rhino |
| [Plugins](plugins.md) | The Bukkit layer on Arclight, which is EssentialsX alone, with its permissions and its metrics |
| [Tools](tools.md) | The scripts in `tools/`, from datapack sync and mod updates through jar patches and the build to the Modrinth upload and RCON |
| [Client](client.md) | The owner's Prism instance on Asahi Linux, its Java flag, the custom main menu and the client crash history |
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
| Recipe viewer | JEI 4.16.1 | EMI 1.1.24 and JEI 15.74.0.233 |

The SkyTekx 2 versions are those of the jars in `~/git/skytekx2-1122-server/mods/`. Polymorph and JEI
show the builds since the update of 2026-10-31 (see [mod set](mod-set.md#update-of-2026-10-31)).
How the three layers work together is on [AlmostUnified](almostunified.md#how-the-recipe-layers-fit-together).

## Mac mini

The pack needs nothing special on the Mac mini. The script `macos/sync-to-mac.sh` copies `mods/`, the
configs, the plugin folder and the datapack sources from this checkout. The build and upload scripts
only run on the build machine, see [tools](tools.md#mac-mini). The details are on
[macOS host](../server/macos.md).

Related: [server](../server/index.md), [content](../content/index.md), [open items](../open-items.md),
[decisions](../decisions.md).
