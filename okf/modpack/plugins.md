---
type: Reference
title: Plugins
description: The Bukkit plugin layer that Arclight actually runs, which is EssentialsX 2.22.0 with its spawn module and, on the Mac mini since 2026-10-31, LuckPerms 5.5.71, their untracked files, the permissions, /spawn, and the metrics and update checks they bring.
tags: [modpack, server, plugins, essentialsx, arclight]
timestamp: 2026-11-04
---

# Plugins

The server runs on Arclight, a hybrid that loads Forge mods and Bukkit plugins side by side (see
[Arclight](../server/arclight.md)). SkyTekx 2 ran EssentialsX, LuckPerms, Multiverse and WorldEdit.
The checkout holds only EssentialsX, and the README says so since its rewrite of 2026-10-31. The Mac mini
also runs LuckPerms since 2026-10-31 22:34, see [permissions](#permissions).

| Plugin | Version | State |
|---|---|---|
| EssentialsX | 2.22.0 | Installed, loaded at every boot since 2026-06-28 |
| EssentialsXSpawn | 2.22.0 | For `/spawn` and `/setspawn`, on the build machine since 2026-10-31 and on the Mac mini since 2026-11-01 00:58, see [spawn](#spawn) |
| LuckPerms | 5.5.71 (Bukkit) | On the Mac mini only, since 2026-10-31 22:34. Not in the checkout |
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
| Jar `plugins/EssentialsXSpawn-2.22.0.jar` | No | The spawn module, since 2026-10-31 on the build machine |
| Folder `plugins/Essentials/` | No | Default `config.yml` from 2026-06-28, `kits.yml` with the sample kits (tools, dtools, notch), the sample `motd.txt`, `items.json`, `worth.yml`, `tpr.yml`, one player file in `userdata/`, `usermap.bin` and `uuids.bin` |
| File `plugins/bStats/config.yml` | No | Metrics switch, `enabled: true` |
| File `plugins/PluginMetrics/config.yml` | Yes | The old MCStats metrics switch, `opt-out: false`, from 2026-06-27 |

Nothing in `plugins/` is in `.gitignore`, but only `plugins/PluginMetrics/config.yml` is tracked. A fresh
clone has no plugin at all. The Essentials folder holds a player file with personal data, so do not
commit it as is (see [secrets](../server/secrets.md)).

## Hybrid warning

At every boot Essentials logs one ERROR that begins "You are running a server that does not properly
support Bukkit plugins" (for example `logs/2026-07-12-1.log.gz:3185`). It is one of the two accepted
boot errors, three before 2026-11-04, see
[logs and crashes](../server/logs-and-crashes.md#boot-error-baseline). Essentials still
enables and works.

## Permissions

The config keeps `use-bukkit-permissions: true` (`plugins/Essentials/config.yml:354`). Without a
permissions plugin, Bukkit's defaults apply, so most Essentials commands, `/gm` included, only work for
operators. The list `player-commands` at line 269, which gives commands to every player, only takes
effect when that option is false (the config's own comments, lines 265 to 268 and 351 to 353). For a
server with players who are not operators, either set the option to false and review the list, or
install a permissions plugin.

The Mac mini got the second way. At 22:34 on 2026-10-31 LuckPerms-Bukkit 5.5.71 went into its `plugins/`
(Modrinth version `b0mk8uS6`, SHA-512 checked against the API), and the server restarted. LuckPerms keeps
its data in an H2 database in `plugins/LuckPerms/` on the Mac. The group `default`, which every player
is in, holds 39 nodes of EssentialsX, each with the prefix `essentials.`: `tpa`, `tpaccept`, `tpdeny`,
`tpahere`, `tpacancel`, `tpauto`, `home`, `home.bed`, `sethome`, `sethome.multiple`, `delhome`, `back`,
`back.ondeath`, `spawn`, `warp`, `warp.list`, `msg`, `mail`, `mail.send`, `ignore`, `me`, `afk`, `list`,
`seen`, `realname`, `motd`, `rules`, `help`, `helpop`, `ping`, `getpos`, `compass`, `depth`, `suicide`,
`balance`, `balancetop`, `pay`, `worth` and `signs.use.*`. The admin commands stay with operators. `home.bed` came on 2026-11-02: without it `/home` of a player with no saved home ignores the bed and sends the player to the spawn, because `spawn-if-no-home` is on.
`essentials.spawn` works since the spawn module came on 2026-11-01, see [spawn](#spawn). Change the
set over RCON with `lp group default permission set <node>` or `unset <node>`, or with `/lp editor` in
game as an operator.

Breaking blocks and hitting players have nothing to do with these permissions. When both failed on
2026-10-31, the causes were the spawn protection and a bug in Asgard Shield Reloaded, see the
[change log](../log.md#2026-10-31-the-mac-mini-and-the-halloween-world).

## Spawn

On 2026-10-31 at 23:10 the owner asked why there is no `/spawn`. EssentialsX has the node
`essentials.spawn`, but the command lives in the separate module EssentialsXSpawn. Version 2.22.0, the
same release as EssentialsX, came from the GitHub release `EssentialsX/Essentials` 2.22.0
(`EssentialsXSpawn-2.22.0.jar`, 18,661 bytes, SHA-256 `dd5377c4c921b9b6...`, checked against the
digest of the release asset). The jar of EssentialsX in `plugins/` matches the digest of the same
release.

The module also takes over two events by default. On death it sends every player to the spawn point and
ignores beds and respawn anchors, and on the first join it gives the sample kit `tools` and announces the
new player. Both listeners are off in `plugins/Essentials/config.yml` (`respawn-listener-priority: none`
and `spawn-join-listener-priority: none`), so respawns and joins stay vanilla and the module only adds
the two commands.

Until an operator stands somewhere and runs `/setspawn`, `/spawn` leads to the world spawn of the
Overworld (308 64 392). `/setspawn` needs `essentials.setspawn`, which only operators have.

The Mac mini got the module with the restart of 2026-11-01 00:58 (see
[Skylands worldgen](../content/skylands-worldgen.md#deploy-of-2026-11-01)). Its boot logged "Enabling
EssentialsSpawn v2.22.0", and at 01:20 `bukkit:help spawn` over RCON showed the command with the alias
`espawn`. Nobody had used it yet.

## Metrics and update checks

- The bStats switch in `plugins/bStats/config.yml` is `enabled: true`, so plugins that use bStats,
  EssentialsX among them, send usage statistics to bstats.org.
- EssentialsX looks for new versions at startup, `update-check: true` (`config.yml:793`).
- The MCStats file is a leftover. That service no longer exists.

The owner's standing rule is no telemetry (memory `feedback_no_telemetry.md`, written for dev tools).
The update checker of the Collective mod was already turned off for the same kind of reason (commit
d5e1449, see [boot fixes](../content/boot-fixes.md#config-fixes)). Setting `enabled: false` in the bStats
file and `update-check: false` in the Essentials config is a candidate that waits for the owner.

## Mac mini

Git only tracks `plugins/PluginMetrics/config.yml`, so the Mac mini gets the plugin jar and its folder
from this checkout through `macos/sync-to-mac.sh`. Unless it runs with `--with-world`, the script
copies only `config.yml` from `plugins/Essentials/` and leaves the rest alone, because Essentials keeps
player data and in-game changes there (`userdata/`, `usermap.bin`, `uuids.bin`, kits, warps). Without
the folder the server has no `/gm` and no other Essentials command. See [macOS host](../server/macos.md).

## Open points

- The plugin layer is not tracked. Committing the jar and a reviewed `config.yml` would make a fresh
  clone complete.
- LuckPerms and its database exist only on the Mac mini. A new host needs the jar and the 38 nodes
  again, see above.
- The bStats metrics and the update check are on, see above.

Related: [Arclight](../server/arclight.md), [deploy](../server/deploy.md),
[logs and crashes](../server/logs-and-crashes.md), [open items](../open-items.md).
