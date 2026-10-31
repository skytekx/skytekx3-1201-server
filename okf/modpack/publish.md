---
type: Runbook
title: Publish
description: The state of the SkyTekx3 Modrinth project, what blocks a public release and a player install today, the release steps, and the history of the mrpack versions.
tags: [modpack, modrinth, release, mrpack]
timestamp: 2026-10-31
---

# Publish

SkyTekx 3 is meant to be installed from Modrinth as the modpack `skytekx3`. On 2026-10-31 that is not
possible yet: the project is not public, and the current `.mrpack` cannot be installed even from the
local file.

## State on 2026-10-31

| Item | State | Evidence |
|---|---|---|
| Modrinth project | Slug `skytekx3`, id `KqKLuzBj`, created as a draft | Memory `project_skytekx2_modpack.md:31` |
| Public visibility | None. An anonymous GET of `api.modrinth.com/v2/project/skytekx3` returns 404 | Checked 2026-10-31 |
| Versions uploaded | 3.0.0, 3.0.1 and 3.0.3 are recorded. Whether 3.0.4 or 3.0.5 went up cannot be seen without the token | Memory `project_skytekx2_modpack.md:31` and `:37` |
| Current build | File `SkyTekx3-3.0.6.mrpack`, 65,020,785 bytes, built 2026-10-31 at 19:09 and not uploaded, not in git | Repo root |
| Index in the build | 217 entries with `versionId` 3.0.6, identical to `modrinth.index.json` at commit b23a72f | Build of 2026-10-31 |
| Current index | 217 entries since the mod update of 2026-10-31 (0a20b8c), see [mod set](mod-set.md#update-of-2026-10-31) | File `modrinth.index.json` |

Commit b23a72f raised `versionId` to 3.0.6 after the mod update, and 222ee72 did the same in
`tools/publish_modrinth.py` with a new changelog. Pack 3.0.6 waits for the owner's go before any upload.

The owner asked to publish on 2026-06-27 20:46 and again on 2026-06-29 17:04, and to make the project
public on 2026-06-29 10:11. On 2026-06-30 18:16 they noted that it was still not public because of the
permissions page on Modrinth, and at 18:20 asked for a folder with the needed files
(`~/.claude/history.jsonl`, session 5bd3e1de). That folder is
[modrinth-permissions/](../../modrinth-permissions) (commit 7995a0a).

## What blocks a release

1. Six bundled jars are All Rights Reserved without a redistribution grant: Aether Genesis,
   Connectivity, Cupboard, Deadly World, Fireflies and Hexxit Additions
   (`modrinth-permissions/README.md`). All six are still in 3.0.5. Modrinth review rejects them, so they
   need written permission, a replacement or removal.
2. The Nyx entry downloads from a GitHub release that does not exist (HTTP 404, skytekx/nyx has no
   releases, checked 2026-10-31). Prism and the Modrinth App abort the whole install on a failed
   download. Publish the release or drop the index entry, see [Nyx](../content/nyx.md#open-points).
3. Three jars that the server runs are in no pack: AllTheLeaks, BrassAmber BattleTowers and the
   Cataclysm Spellbooks fork build. A player who installs 3.0.5 most likely cannot join this server.
   See [mod set](mod-set.md#three-jars-outside-the-pack).
4. The texts are stale:
   - The changelog at `tools/publish_modrinth.py:90` is the text of 3.0.4 (Nyx port, void fall, island
     floor fix) and says nothing about the vanilla rework.
   - The body `docs/MODPACK_DESCRIPTION.md` lists Better Combat (parked) and Radium (removed), says that
     Twilight Forest is not bundled (it is), and credits the owner's handle on line 22. The owner ruled
     on 2026-06-29 10:00 and 10:10 that the credit reads "a modpack by ts4.party, the TeamSpeak for party,
     formerly known as ts3.party", and on 10:24 that the credits carry no real names.
   - The index summary (`tools/build_mrpack.py:31`) and the project description
     (`tools/publish_modrinth.py:67`) still say "kitchen-sink". On 2026-06-29 the owner had the phrase
     dropped from the website.
5. The permission folder is out of date. It has files for Ruins (removed) and skytekx-worldgen (replaced)
   and none for skytekx-compat. Its README says 34 bundled mods, the pack bundles 33.
6. The 3.0.5 file carries junk from the working tree, see below. The 3.0.6 build has none, but it
   holds seven drifted configs as they are on disk. Commit or restore them and build again.

## What a player gets

- Every index entry with its client env, from the Modrinth CDN, unpatched. The server runs nine of
  them patched, which only matters on the server.
- The 33 bundled jars under `overrides/mods/`.
- The `config/` and `kubejs/` files that git tracks, 534 config files and 6 KubeJS files in 3.0.6 (3.0.5
  shipped 550 config files from disk). They include the FancyMenu layout, but the FancyMenu jars are
  not in the pack, see [client](client.md#main-menu).
- The two Skylands datapacks under `overrides/world/datapacks/`. That path fits a server folder whose
  world is called `world`. A singleplayer save keeps its datapacks in `saves/<world>/datapacks/`, so a
  singleplayer world gets no Skylands unless the player copies the packs by hand.
- No resource pack, no `skytekx3_fixes` datapack and none of the jar patches.

## Junk in the build

Up to 3.0.5 the script `tools/build_mrpack.py` zipped the working tree, not git. The 3.0.5 file
therefore holds:

- 14 config files that are not in git, among them `config/avaritia-common.toml` of the removed
  Re:Avaritia, and configs that differ from HEAD. HEAD alone cannot rebuild 3.0.5.
- The Spark profiler's temporary files under `config/spark/tmp/`: three native Linux libraries
  (`*-libasyncProfiler.so.tmp`, 343,408 bytes each) and three profile dumps, plus
  `config/spark/activity.json`. A reviewer would flag the native libraries.
- The Chunky pregen state under `config/chunky/tasks/`.

Since commit 41d15c4 the script packs only the files git tracks under `config/`, `kubejs/` and
`defaultconfigs/` and skips `config/chunky/tasks/`. Pack 3.0.6 has none of the files above. Tracked
configs still go in as they are on disk, so a runtime rewrite of a tracked config that is not committed
ends up in the pack too.

## Release steps

On the build machine, after the blockers are fixed:

1. Set the new version in `tools/build_mrpack.py:11` and `tools/publish_modrinth.py:10`, and write the
   changelog at `tools/publish_modrinth.py:90`.
2. Commit or restore every tracked file under `config/`, `kubejs/` and `defaultconfigs/` that differs
   from git (`git status`), since the build reads them from disk. Then build:
   `python3 tools/build_mrpack.py`.
3. Import the `.mrpack` into a fresh Prism instance and join the server with it. On Asahi Linux add
   the JVM flag from [client](client.md#java-and-the-asahi-flag).
4. Load the token from the vault (see [secrets](../server/secrets.md)) and run the dry run:
   `python3 tools/publish_modrinth.py "$PAT"`.
5. Upload: `python3 tools/publish_modrinth.py "$PAT" --go`. The version goes up as a featured beta.
6. In the Modrinth web UI, give the project an icon and submit it for review. Only then does it become
   public (memory `project_skytekx2_modpack.md:31`).

The scripts hard-code the build machine's repo path, so steps 2, 4 and 5 do not run on the Mac mini, see
[tools](tools.md#mac-mini).

## Version history

| Version | Built | Size | Index entries | Bundled jars | Main change |
|---|---|---:|---:|---:|---|
| 3.0.0 | 2026-06-27 12:11 | 274,151 B | 109 | 0 | First pack: configs and two KubeJS scripts |
| 3.0.1 | 2026-06-27 15:08 | 432,303 B | 109 | 0 | First Skylands datapack, 8 files with `/trigger` travel |
| 3.0.2 | 2026-06-27 19:57 | 22,068,098 B | 117 | 1 | Eight more mods, Twilight Forest bundled |
| 3.0.3 | 2026-06-27 20:51 | 22,227,611 B | 118 | 1 | Amendments and `tag_fixes.js` |
| 3.0.4 | 2026-06-29 17:29 | 50,921,945 B | 213 | 32 | 97 more mods, among them Nyx, Aether, Cataclysm, Mekanism addons and Immersive Engineering. Its datapack needed the unbundled skytekx-worldgen mod |
| 3.0.5 | 2026-07-12 23:40 | 65,487,506 B | 216 | 33 | Vanilla Skylands rework with the Forge pack split, skytekx-compat, Aether Genesis, Medieval Embroidery, SlashBlade Resharped, The One Probe, Ruins removed |
| 3.0.6 | 2026-10-31 19:09 | 65,020,785 B | 217 | 33 | The mod update of 2026-10-31, Skylands fixes (no Battle Towers, strongholds, illager campsites, floating petrified roots or windmill fields under the islands), Halloween pumpkins and blood moons. Not uploaded |

None of the `.mrpack` files is in git (`.gitignore:36`). The 3.0.5 file was committed on 2026-07-12 at
23:43 (1092a5b) and dropped again before the push, with the note that it ships via Modrinth (reflog,
c1a199b and the reset to fded6ce).

Related: [tools](tools.md), [mod set](mod-set.md), [client](client.md),
[secrets](../server/secrets.md), [open items](../open-items.md).
