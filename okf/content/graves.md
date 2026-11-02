---
type: Feature
title: Graves
description: Since 2026-11-02 a player's death leaves a grave with the items, made by the GraveStone Mod, and the mod's death history lets an op give an inventory back with one command.
tags: [content, death, graves, mods]
timestamp: 2026-11-02
---

# Graves

Since 2026-11-02 19:35 a player who dies leaves a grave at the place of death instead of loose drops.
The GraveStone Mod 1.0.35 by Max Henkel does this. It comes from Modrinth and is required on the server
and on every client. At 19:14 that day the owner asked for the grave mod that every SkyTekx had so far,
after four deaths in a row had cost items that no drop held any more.

## What SkyTekx 2 had

SkyTekx 2 on 1.12.2 made graves with OpenBlocks from its first commit on 2023-01-03, with `grave=true`
and `destructiveGraves=true` in its `config/openblocks.cfg`. In its first three days it also tried Tomb
Many Graves, then Corpse and then the GraveStone Mod 1.10.3, and its commit 66cb027 of 2023-01-05
removed the last of them. OpenBlocks has no build for 1.20.1. The GraveStone Mod has one, so SkyTekx 3
uses it.

## How it behaves

The server config is `world/serverconfig/gravestone-server.toml`. The first boot with the mod wrote it
with the defaults, and none of them was changed.

| Key | Value | Meaning, from the comments in the file |
|---|---|---|
| `enable_obituary` | true | A player gets an obituary after the death |
| `remove_obituary` | false | The obituary stays when the grave is broken |
| `break_pickup` | true | Breaking the grave sorts the items back into the inventory |
| `sneak_pickup` | false | Sneaking on the grave does nothing |
| `only_owners_can_break` | false | Any player can break a grave |
| `spawn_ghost` | false | No ghost appears when a grave is broken |
| `strict_placement` | false | A grave does not replace other blocks when there is no free space above |
| `replaceable_blocks` | `#gravestone:grave_replaceable` | The blocks a grave may replace |

A grave is a block, so it does not despawn. Drops vanish after five minutes in a loaded chunk. A fall
out of the Skylands ends in the overworld, where the player lands (see [Skylands travel](aether-fall.md)),
so a death after such a fall leaves its grave there.

## Giving an inventory back

The mod writes every death to a file in the world folder. An op can give the items of a death back with
`/restore <player> <death id> add`, which adds them to the inventory, or with `replace`, which empties
the inventory first. The obituary shows the death id and can copy it. Up to 2026-11-02 an inventory came
back only by editing the playerdata file while the player was kicked, see the
[change log](../log.md#2026-11-02-the-twilight-forest-backport-goes-live).

At 19:45 on 2026-11-02 no grave had formed in play yet, so neither the grave on Arclight nor `/restore`
had been seen working.

Related: [mod set](../modpack/mod-set.md), [Skylands travel](aether-fall.md),
[Arclight](../server/arclight.md).
