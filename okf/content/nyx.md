---
type: Mod
title: Nyx
description: Ellpeck's moon mod, forked and ported to Forge 1.20.1 for SkyTekx 3, with lunar events, meteors, lunar water and meteor gear.
tags: [content, mod, nyx]
timestamp: 2026-10-31
---

# Nyx

Nyx is a content mod about the moon by Ellpeck (MIT, [Ellpeck/Nyx](https://github.com/Ellpeck/Nyx)).
SkyTekx 2 shipped it as `Nyx-1.4.2.jar` (`~/git/skytekx2-1122-server/mods/`), but it never got a
1.20.1 release, so the owner had it forked and ported. It runs on
the server and on every client.

## Owner rulings

All from `~/.claude/history.jsonl`, session 5bd3e1de, 2026-06-29:

- 14:23: Nyx is MIT licensed, fork it to the skytekx GitHub organisation and port it to 1.20.
- 15:02: Nyx turned out bigger than expected, but it must still be ported completely and installed on
  the client and on the server.
- 16:05: install it now.
- 17:04: the owner asked whether Nyx was installed everywhere and all dimensions, the Skylands included,
  were reset, and asked to commit and publish again. Mrpack 3.0.4 was built at 17:29.

## What it adds

The jar's `mods.toml` describes it as lunar events (full, blood and harvest moons and star showers),
falling meteors and stars, lunar water and gleaning crystals, meteor tools and armour, moon scaled
enchantments and a darker, tinted night sky that follows the moon phase.

- Items (20): fallen star, lunar water bottle and bucket, the meteor tools and armour, meteor dust,
  ingot and shard, meteor finder, scythe and unrefined crystal.
- Blocks (13): the star block family, meteor rock and gleaning meteor rock, meteor block and glass,
  crystal, lunar water and its cauldron, and star air.
- Data in the jar: 24 recipes, 11 loot tables and 2 block tags in the `minecraft` namespace
  (`mineable/pickaxe` and `needs_diamond_tool`).

## The jar

| Property | Value |
|---|---|
| File | Tracked in git as [local-mods/nyx-1.20.1-1.0.0.jar](../../local-mods/nyx-1.20.1-1.0.0.jar) |
| Size and SHA-1 | 418,839 bytes, `6424939d6484ea17b5762ebdc7d178e34c23f2f6` |
| Built | 2026-06-29 15:50:11 +02:00 (`Implementation-Timestamp` in the manifest), version 1.20.1-1.0.0 |
| Mod id | Id `nyx`, package `de.ellpeck.nyx`, side BOTH, mixin config `nyx.mixins.json` |
| Needs | Forge 47 or later and Minecraft 1.20.1 |
| Source | Fork [skytekx/nyx](https://github.com/skytekx/nyx), branch `1.20.1`, local checkout `~/git/nyx` |

The port lives on the branch `1.20.1`, which is two commits ahead of upstream: 7e5714f (2026-06-29
14:49 local time, the client side night sky) and 5ef1fdb (15:50, the full content mod). The default
branch `master` still equals upstream from 2021 (995d3b4). In this repo the jar arrived with commit
c4369dc.

Copies of the same jar sit in the server's `mods/`, in the owner's Prism instance (see
[client](../modpack/client.md)) and in the `.mrpack` under `overrides/mods/`, because
`tools/build_mrpack.py` bundles every jar in `local-mods/`. Up to 74faf22 the index listed it as well,
with a download from the GitHub release `v1.20.1-1.0.0` of skytekx/nyx. That release does not exist
(HTTP 404 and no releases at all, checked 2026-10-31), so a launcher aborted the whole install on this
file. Commit 74faf22 dropped the entry.

## Configuration

The file [config/nyx-common.toml](../../config/nyx-common.toml) came in with commit 535c4f5 and kept
the defaults until 2026-10-31:

- The key `allowedDimensions` lists only `minecraft:overworld`. Lunar events and falling stars only run in
  listed dimensions (fork source `NyxWorld.java:112` and `Events.java:192`), so the Skylands get none,
  although they share the overworld sky.
- Harvest moon, star showers and blood moon each start with a chance of 0.05 per night. The harvest
  moon and the blood moon only happen on full moon nights. During a blood moon twice as many mobs spawn
  and players cannot sleep.
- Lunar water is on. A cauldron of water turns into lunar water after a set time under the night sky,
  for example 1200 ticks at full moon.
- Meteors are on and land up to 1000 blocks from the nearest player. Their chance rises once a player
  has entered the gate dimension, `minecraft:the_nether`.

Commit a00ecce ("more blood moons, Nyx events in the Skylands", 2026-10-31 18:14) changed two keys, in
the same minutes as the Halloween commits for the Skylands (see
[Skylands worldgen](skylands-worldgen.md#changes-of-2026-10-31)). The key `allowedDimensions` now also
lists `skytekx3:skylands`, and `bloodMoonChance` went from 0.05 to 0.5. So lunar events reach the
Skylands, and about every second full moon becomes a blood moon.

## Open points

- Whether lunar events should also run in the Skylands was never ruled. Commit a00ecce turned
  them on there on 2026-10-31, together with a blood moon chance of 0.5. Whether both should
  stay beyond the Halloween map is an owner decision.
- Two notes still say Nyx is gone: the comment at `kubejs/server_scripts/recipe_fixes.js:28` and the
  Nyx rows in `docs/RECIPE_PORT.md` (lines 52 and 55). The fallen star exists again as `nyx:fallen_star`,
  so the SkyTekx 2 hexical diamond recipe could use it (see [KubeJS](../modpack/kubejs.md)).
- The `authors` line in the jar's `mods.toml` and the file
  `modrinth-permissions/nyx-1.20.1-1.0.0.jar.md` (lines 13 and 19) name the owner in person. The owner
  ruled on 2026-06-29 10:24 that the credits carry no real names.

## Boot log

Nyx loads cleanly. One boot error mentions a reflection failure in AllTheLeaks. The log of 2026-06-30
records it as an AllTheLeaks access into Citadel that is harmless and not caused by Nyx
([log](../log.md), 2026-06-30 entry). Between 06:41 and 06:43 that morning the owner had asked to change
the code if Nyx caused it.

## History

The first port commit (7e5714f) only brought the client side night sky. The full content port
(5ef1fdb) followed the same afternoon. The earlier version of this page spoke of a client only stub of
8,974 bytes. That stub was never committed, so git cannot confirm its size.

On 2026-10-31 commit a00ecce let lunar events into the Skylands and raised the blood moon chance to 0.5,
see [configuration](#configuration).

Related: [mod set](../modpack/mod-set.md), [compat mod](compat-mod.md), [content](index.md).
