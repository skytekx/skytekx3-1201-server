---
type: Runbook
title: Publish
description: The state of the SkyTekx3 Modrinth project, the install test of 2026-10-31, what blocks a public release, the release steps, and the history of the mrpack versions.
tags: [modpack, modrinth, release, mrpack]
timestamp: 2026-10-31
---

# Publish

SkyTekx 3 is meant to be installed from Modrinth as the modpack `skytekx3`. On 2026-10-31 the project
is still a draft, so only its team can see and install it. Pack 3.0.6 is uploaded, installs cleanly and
reaches the main menu, but in the test of 2026-10-31 it could not play on this server, see
[test of 2026-10-31](#test-of-2026-10-31).

## State on 2026-10-31

| Item | State | Evidence |
|---|---|---|
| Modrinth project | Slug `skytekx3`, id `KqKLuzBj`, status draft, created 2026-06-27 | API with the token, 2026-10-31 |
| Public visibility | None. An anonymous GET of `api.modrinth.com/v2/project/skytekx3` returns 404 | Checked 2026-10-31 |
| Versions uploaded | 3.0.0 to 3.0.4 (2026-06-27 to 2026-06-29) and 3.0.6 (version id `Gi0aBofN`, 2026-10-31 20:27). 3.0.5 never went up | API with the token, 2026-10-31 |
| Current build | File `SkyTekx3-3.0.6.mrpack`, 67,245,989 bytes, 808 entries, built 2026-10-31 at 20:19:59 from 7f5ae03, not in git. Commit 50d875e holds the index it wrote. The file on the Modrinth CDN has the same SHA-512 | Repo root, upload of 20:27 |
| Index in the build | 216 entries, all on `cdn.modrinth.com`, `versionId` 3.0.6, Forge 47.4.18. Identical to `modrinth.index.json` at 50d875e | Build of 20:19 |
| Project texts | The description and the body on Modrinth are still those of 2026-06-27, see blocker 4 | API with the token, 2026-10-31 |

The owner asked to publish on 2026-06-27 20:46 and again on 2026-06-29 17:04, and to make the project
public on 2026-06-29 10:11. On 2026-06-30 18:16 they noted that it was still not public because of the
permissions page on Modrinth, and at 18:20 asked for a folder with the needed files
(`~/.claude/history.jsonl`, session 5bd3e1de). That folder is
[modrinth-permissions/](../../modrinth-permissions) (commit 7995a0a). On 2026-10-31 at 20:09 the owner
linked the project page and asked to update the pack and test it. Version 3.0.6 went up at 20:27.

## Test of 2026-10-31

The install was tested with the uploaded 3.0.6 file in a new Prism instance on the build machine
(Asahi Linux, Java 17.0.19, the JVM flag from [client](client.md#java-and-the-asahi-flag), 8 GB heap as
in the owner's own instance). It joined the Mac mini server.

| Step | Result |
|---|---|
| Downloads | All 216 index files came from the Modrinth CDN, 908 MiB in 168 s. Size, SHA-1 and SHA-512 match for every file |
| Start | The client started on Forge 47.4.18 with its 239 mod jars and reached the main menu |
| First join, 20:38:51 | Refused. The handshake found 177 registry entries the client lacked, all from BrassAmber BattleTowers, and the screen said "Your client is missing the following mods" and named Brass-Amber BattleTowers |
| Second join, 20:41:31, with BattleTowers added by hand | Joined. The server log shows no new error, only the `Async chunk entity get` warnings known from [Arclight](../server/arclight.md) |
| Recipe viewer after the join | Failed. EMI's import of the JEI plugins dropped 7,246 recipes. 7,243 of them are Chipped recipes that hit an `AbstractMethodError`: JEI 15.74.0.233 added `IRecipeSlotBuilder.setPosition` with alignment arguments, and the JEI bridge of EMI 1.1.24 does not implement it. The import took 22.7 s instead of about 6.5 s |
| Memory after the join | Failed. AllTheLeaks reported 90% heap use at 20:42:05. The heap stayed full after every full GC (253 of them by 20:48, 365 s of the client's 507). EMI's reload died with an `OutOfMemoryError` at 20:46:33 while it built its search index, then started again. The heap was full of recipe viewer data: about 9 million NBT compounds, 4.4 million EMI stacks and 1.9 million JEI stacks. The game froze |

The owner's own instance runs JEI 15.20.0.132, the build of 3.0.5. Its sessions of June and July show
the same 90% message from AllTheLeaks less than a minute after each join, but no `AbstractMethodError`,
an EMI reload of about 27 s and play for up to two and a half hours. JEI 15.74.0.233 came in with the
mod update of 0a20b8c. The server on the Mac mini runs it too.

## What blocks a release

1. Six bundled jars are All Rights Reserved without a redistribution grant: Aether Genesis,
   Connectivity, Cupboard, Deadly World, Fireflies and Hexxit Additions
   (`modrinth-permissions/README.md`). All six are still in 3.0.6. Modrinth review rejects them, so they
   need written permission, a replacement or removal.
2. BrassAmber BattleTowers is in no pack, and the server refuses a client without it. It is All Rights
   Reserved and only on CurseForge. Bundling it needs a grant from its author, otherwise each player
   adds it by hand, or the server drops it, which goes against the ruling of 2026-06-29 that Battle
   Towers are essential. See [mod set](mod-set.md#jars-outside-the-index).
3. With JEI 15.74.0.233 and EMI 1.1.24 a client with an 8 GB heap runs out of memory and freezes after
   the first join, and EMI drops the Chipped recipes. With JEI 15.20.0.132 neither happened. The
   dropped recipes are caused by the JEI update. The memory failure most likely is too, but a test with
   JEI 15.20.0.132 has to show it. Rolling JEI back in the index and on the server, or an EMI build that
   matches JEI 15.74, needs the owner's decision and a new test.
4. The texts are stale:
   - The project description on Modrinth still calls the pack a "kitchen-sink". The body (from
     `docs/MODPACK_DESCRIPTION.md`, uploaded on 2026-06-27) lists Better Combat (parked) and Radium
     (removed), says that Twilight Forest is not bundled (it is), and ends with "Built by kleberbaum".
     The owner ruled on 2026-06-29 10:00 and 10:10 that the credit reads "a modpack by ts4.party, the
     TeamSpeak for party, formerly known as ts3.party", and at 10:24 that the credits carry no real
     names. The script `publish_modrinth.py` only sets these texts when it creates a project, so they
     have to be changed in the web UI or with a project PATCH.
   - The index summary and the changelog of 3.0.6 are up to date since 5aeb24d and 7f5ae03.

Fixed on 2026-10-31: the Nyx entry that downloaded from a GitHub release that does not exist (d3391bf,
the jar is bundled), the two jars the server ran outside the pack (782126f, AllTheLeaks and the
Cataclysm Spellbooks build), the out-of-date permission folder (955275c), and the configs that server
boots had rewritten on disk (3a398b1, the builder reads git HEAD).

## What a player gets

- Every index entry with its client env, from the Modrinth CDN, unpatched. The server runs nine of
  them patched, which only matters on the server.
- The 35 bundled jars under `overrides/mods/`.
- The `config/` and `kubejs/` files of the commit the pack was built from, 534 config files and 6
  KubeJS files in 3.0.6 (3.0.5 shipped 550 config files from disk). They include the FancyMenu layout,
  but the FancyMenu jars are not in the pack, see [client](client.md#main-menu).
- The two Skylands datapacks and, since 3c2662e, the `skytekx3_fixes` datapack under
  `overrides/world/datapacks/`. That path fits a server folder whose world is called `world`. A
  singleplayer save keeps its datapacks in `saves/<world>/datapacks/`, so a singleplayer world gets
  none of them unless the player copies them by hand.
- Forge 47.4.18, the build the server runs inside Arclight (c70f699, 47.4.6 before).
- No resource pack, none of the jar patches and no BrassAmber BattleTowers.

## Junk in the build

Up to 3.0.5 the script `tools/build_mrpack.py` zipped the working tree, not git. The 3.0.5 file
therefore holds:

- 14 config files that are not in git, among them `config/avaritia-common.toml` of the removed
  Re:Avaritia, and configs that differ from HEAD. HEAD alone cannot rebuild 3.0.5.
- The Spark profiler's temporary files under `config/spark/tmp/`: three native Linux libraries
  (`*-libasyncProfiler.so.tmp`, 343,408 bytes each) and three profile dumps, plus
  `config/spark/activity.json`. A reviewer would flag the native libraries.
- The Chunky pregen state under `config/chunky/tasks/`.

Commit 41d15c4 limited the script to the files git tracks under `config/`, `kubejs/` and
`defaultconfigs/` and skipped `config/chunky/tasks/`, but it still read them from disk. A first 3.0.6
build at 19:09 therefore carried seven configs that server boots had rewritten. Since 3a398b1 every
override comes from git HEAD through `git archive`, and the build of 20:19 has none of the files above.

## Release steps

On the build machine, after the blockers are fixed:

1. Set the new version in `VERSION` of `tools/build_mrpack.py` and of `tools/publish_modrinth.py`, and
   write the changelog in `publish_modrinth.py`.
2. Commit everything the pack should carry, since the builder reads git HEAD, not the disk. Then build
   with `python3 tools/build_mrpack.py` and commit the rewritten `modrinth.index.json`.
3. Import the `.mrpack` into a fresh Prism instance and join the server with it. On Asahi Linux add the
   JVM flag from [client](client.md#java-and-the-asahi-flag). Stay in the world until EMI logs
   "Reloaded EMI" and the heap settles. The 90% message of AllTheLeaks at the join is normal. A heap
   that stays full is not.
4. Load the token from the vault (see [secrets](../server/secrets.md)) into `MODRINTH_TOKEN` and run
   the dry run: `python3 tools/publish_modrinth.py`.
5. Upload: `python3 tools/publish_modrinth.py --go`. The version goes up as a featured beta, and the
   script compares the SHA-512 of the uploaded file with the local build.
6. In the Modrinth web UI, update the description and the body and submit the project for review. Only
   then does it become public (memory `project_skytekx2_modpack.md:31`). The project has an icon.

The builder needs the git checkout, and the Mac mini's copy has no `.git` folder, so packs are built on
the build machine, see [tools](tools.md#mac-mini).

## Version history

| Version | Built | Size | Index entries | Bundled jars | Main change |
|---|---|---:|---:|---:|---|
| 3.0.0 | 2026-06-27 12:11 | 274,151 B | 109 | 0 | First pack: configs and two KubeJS scripts |
| 3.0.1 | 2026-06-27 15:08 | 432,303 B | 109 | 0 | First Skylands datapack, 8 files with `/trigger` travel |
| 3.0.2 | 2026-06-27 19:57 | 22,068,098 B | 117 | 1 | Eight more mods, Twilight Forest bundled |
| 3.0.3 | 2026-06-27 20:51 | 22,227,611 B | 118 | 1 | Amendments and `tag_fixes.js` |
| 3.0.4 | 2026-06-29 17:29 | 50,921,945 B | 213 | 32 | 97 more mods, among them Nyx, Aether, Cataclysm, Mekanism addons and Immersive Engineering. Its datapack needed the unbundled skytekx-worldgen mod |
| 3.0.5 | 2026-07-12 23:40 | 65,487,506 B | 216 | 33 | Vanilla Skylands rework with the Forge pack split, skytekx-compat, Aether Genesis, Medieval Embroidery, SlashBlade Resharped, The One Probe, Ruins removed |
| 3.0.6 | 2026-10-31 20:19 | 67,245,989 B | 216 | 35 | The mod update of 2026-10-31, Skylands fixes (no Battle Towers, strongholds, illager campsites, floating petrified roots or windmill fields under the islands), Halloween pumpkins and blood moons, the fixes datapack, Forge 47.4.18. Uploaded at 20:27 as `Gi0aBofN`. Freezes after the join with an 8 GB heap, see [test](#test-of-2026-10-31) |

Versions 3.0.0 to 3.0.4 and 3.0.6 are on Modrinth. 3.0.5 was built but never uploaded. None of the
`.mrpack` files is in git (`.gitignore:36`). The 3.0.5 file was committed on 2026-07-12 at 23:43
(1092a5b) and dropped again before the push, with the note that it ships via Modrinth (reflog, c1a199b
and the reset to fded6ce).

Related: [tools](tools.md), [mod set](mod-set.md), [client](client.md),
[secrets](../server/secrets.md), [open items](../open-items.md).
