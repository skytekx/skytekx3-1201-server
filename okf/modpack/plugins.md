---
type: Reference
title: Plugins
description: The Bukkit plugin layer that Arclight actually runs, which is EssentialsX 2.22.0 alone, its untracked files, its permissions, and the metrics and update checks it brings.
tags: [modpack, server, plugins, essentialsx, arclight]
timestamp: 2026-10-31
---

# Plugins

The server runs on Arclight, a hybrid that loads Forge mods and Bukkit plugins side by side (see
[Arclight](../server/arclight.md)). SkyTekx 2 ran EssentialsX, LuckPerms, Multiverse and WorldEdit.
Only EssentialsX is installed, and the README says so since its rewrite of 2026-10-31.

| Plugin | Version | State |
|---|---|---|
| EssentialsX | 2.22.0 | Installed, loaded at every boot since 2026-06-28 |
| LuckPerms | None | Not installed |
| Multiverse | None | Not installed. The Skylands is a datapack dimension, see [Skylands worldgen](../content/skylands-worldgen.md) |
| WorldEdit | None | Not installed |

## Why EssentialsX is there

On 2026-06-27 21:41 the owner asked why short commands like `/gm` did not work
(`~/.claude/history.jsonl`, session 5bd3e1de). Nine minutes later `plugins/EssentialsX-2.22.0.jar`
(4,861,125 bytes) appeared, file time 21:50. The running server never saw it, because the host went to
sleep. The first boot that loaded it was on 2026-06-28 at 17:59:28 (`logs/2026-06-28-22.log.gz:2712`).

## Files

| Path | In git | Content |
|---|---|---|
| Jar `plugins/EssentialsX-2.22.0.jar` | No | The plugin |
| Folder `plugins/Essentials/` | No | Default `config.yml` from 2026-06-28, `kits.yml` with the sample kits (tools, dtools, notch), the sample `motd.txt`, `items.json`, `worth.yml`, `tpr.yml`, one player file in `userdata/`, `usermap.bin` and `uuids.bin` |
| File `plugins/bStats/config.yml` | No | Metrics switch, `enabled: true` |
| File `plugins/PluginMetrics/config.yml` | Yes | The old MCStats metrics switch, `opt-out: false`, from 2026-06-27 |

Nothing in `plugins/` is in `.gitignore`, but only `plugins/PluginMetrics/config.yml` is tracked. A fresh
clone has no plugin at all. The Essentials folder holds a player file with personal data, so do not
commit it as is (see [secrets](../server/secrets.md)).

## Hybrid warning

At every boot Essentials logs one ERROR that begins "You are running a server that does not properly
support Bukkit plugins" (for example `logs/2026-07-12-1.log.gz:3185`). It is one of the three accepted
boot errors, see [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline). Essentials still
enables and works.

## Permissions

The config keeps `use-bukkit-permissions: true` (`plugins/Essentials/config.yml:354`). Without a
permissions plugin, Bukkit's defaults apply, so most Essentials commands, `/gm` included, only work for
operators. The list `player-commands` at line 269, which gives commands to every player, only takes
effect when that option is false (the config's own comments, lines 265 to 268 and 351 to 353). For a
server with players who are not operators, either set the option to false and review the list, or
install a permissions plugin.

## Metrics and update checks

- The bStats switch in `plugins/bStats/config.yml` is `enabled: true`, so plugins that use bStats,
  EssentialsX among them, send usage statistics to bstats.org.
- EssentialsX looks for new versions at startup, `update-check: true` (`config.yml:793`).
- The MCStats file is a leftover. That service no longer exists.

The owner's standing rule is no telemetry (memory `feedback_no_telemetry.md`, written for dev tools).
The update checker of the Collective mod was already turned off for the same kind of reason (commit
52f4f19, see [boot fixes](../content/boot-fixes.md#config-fixes)). Setting `enabled: false` in the bStats
file and `update-check: false` in the Essentials config is a candidate that waits for the owner.

## Mac mini

Git only tracks `plugins/PluginMetrics/config.yml`, so the Mac mini gets the plugin jar and its folder
from this checkout through `macos/sync-to-mac.sh`. The script leaves the player files in
`plugins/Essentials/userdata/` alone unless it runs with `--with-world`. Without the folder the server
has no `/gm` and no other Essentials command. See [macOS host](../server/macos.md).

## Open points

- The plugin layer is not tracked. Committing the jar and a reviewed `config.yml` would make a fresh
  clone complete.
- The permissions setup above leaves every player who is not an operator with almost no Essentials
  commands.
- The bStats metrics and the update check are on, see above.

Related: [Arclight](../server/arclight.md), [deploy](../server/deploy.md),
[logs and crashes](../server/logs-and-crashes.md), [open items](../open-items.md).
