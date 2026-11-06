---
type: Runbook
title: Publish
description: The state of the SkyTekx3 Modrinth project, the install test of 2026-10-31, what blocks a public release, the release steps, and the history of the mrpack versions.
tags: [modpack, modrinth, release, mrpack]
timestamp: 2026-11-06
---

# Publish

SkyTekx 3 is meant to be installed from Modrinth as the modpack `skytekx3`. On 2026-10-31 the project
is still a draft, so only its team can see it on Modrinth, but the file of 3.0.7 loads from the
Modrinth CDN for anyone who has its link. Pack 3.0.6 installs cleanly and reaches the main menu, but in
the test of 2026-10-31 it only played on this server with BrassAmber BattleTowers added by hand and a
12 GB heap, see [test of 2026-10-31](#test-of-2026-10-31). Pack 3.0.7 followed that evening with the
client mod Modflared and `hex.skytekx.com` in the server list, see [version history](#version-history).

## State on 2026-10-31

| Item | State | Evidence |
|---|---|---|
| Modrinth project | Slug `skytekx3`, id `KqKLuzBj`, status draft, created 2026-06-27 | API with the token, 2026-10-31 |
| Public visibility | None. An anonymous GET of `api.modrinth.com/v2/project/skytekx3` returns 404 | Checked 2026-10-31 |
| Versions uploaded | 3.0.0 to 3.0.4 (2026-06-27 to 2026-06-29), 3.0.6 (version id `Gi0aBofN`, 2026-10-31 20:27) and 3.0.7 (version id `LRkF99Pz`, 2026-10-31 21:55, a featured beta). 3.0.5 never went up | API with the token, 2026-10-31 |
| Current build | File `SkyTekx3-3.0.7.mrpack`, 67,246,343 bytes, 809 entries, built 2026-10-31 at 21:54:18 from 1603723, not in git. Commit 465e625 holds the index it wrote. Modrinth reports the same SHA-512 for the uploaded file | Repo root, upload of 21:55 |
| Index in the build | 217 entries, all on `cdn.modrinth.com`, `versionId` 3.0.7, Forge 47.4.18. Identical to `modrinth.index.json` at 465e625 | Build of 21:54 |
| Server list in the build | Entry `client-overrides/servers.dat`, 58 bytes, with the one server SkyTekx at `hex.skytekx.com` | Build of 21:54 |
| Public file | The [CDN link of 3.0.7](https://cdn.modrinth.com/data/KqKLuzBj/versions/LRkF99Pz/SkyTekx3-3.0.7.mrpack) answers with all 67,246,343 bytes, although the project is a draft | Checked 2026-10-31 |
| Project texts | Replaced on 2026-10-31 at 21:25 with `docs/MODPACK_SUMMARY.txt` and `docs/MODPACK_DESCRIPTION.md`, and again at about 21:56 after fa17708 added the section "Joining the server" to the body. The API returns both unchanged | Project PATCH and GET with the token, 2026-10-31 |

The owner asked to publish on 2026-06-27 20:46 and again on 2026-06-29 17:04, and to make the project
public on 2026-06-29 10:11. On 2026-06-30 18:16 they noted that it was still not public because of the
permissions page on Modrinth, and at 18:20 asked for a folder with the needed files
(`~/.claude/history.jsonl`, session 5bd3e1de). That folder is
[modrinth-permissions/](../../modrinth-permissions) (commit 7995a0a). On 2026-10-31 at 20:09 the owner
linked the project page and asked to update the pack and test it. Version 3.0.6 went up at 20:27.
At 21:48 they chose a pack 3.0.7 with Modflared as the way in from the internet, and it went up at
21:55.

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

Two more runs of the same instance followed that evening.

| Run | Result |
|---|---|
| JEI 15.20.0.132, 8 GB | Forge refused to start. Tinkers' Construct needs JEI 15.59.0.210 or newer, Tinkers' Reforged 15.56.0.204 and Polymorph 15.57.0.207, so the old JEI cannot come back |
| JEI 15.74.0.233, 12 GB (`MaxMemAlloc` 12288) | Joined at 21:12:07. The JEI import took 3,253 ms and hit the same 7,243 `AbstractMethodError`s, but AllTheLeaks printed no 90% line, there was no full GC and no `OutOfMemoryError`, and the heap settled at about 9.3 GB |

So 3.0.6 needs a 12 GB heap, and EMI still lacks the Chipped recipes. No install test of 3.0.7 is
recorded. It differs from 3.0.6 only by Modflared and the server list and has the same JEI and EMI
builds.

## What blocks a release

1. Seven bundled jars are All Rights Reserved without a redistribution grant: Aether Genesis,
   Connectivity, Cupboard, Deadly World, Fireflies, Hexxit Additions and, since 2026-11-04, Mob
   Grinding Utils (`modrinth-permissions/README.md`). The first six are still in 3.0.7. Modrinth review
   rejects them, so they need written permission, a replacement or removal.
2. With JEI 15.74.0.233 and EMI 1.1.24 a client with an 8 GB heap runs out of memory and freezes after
   the first join, and EMI drops the Chipped recipes. A 12 GB heap avoids the freeze. JEI 15.20.0.132
   cannot come back, because three mods need newer builds. The dropped recipes need a Chipped build
   that does not call the new JEI method, or an EMI build that implements it, then an owner decision
   and a new test. Since 2026-11-03 git ships such an EMI build, the fork from github.com/skytekx/emi
   (eadc7a6d, as 1.1.24-skytekx.2 since 2026-11-04). The new test is still open.
3. ZeroCore 2.1.48 is All Rights Reserved, and the modpack grant of its author forbids reposts on
   any website other than CurseForge, Modrinth and the FTB launcher. Its first bundling put the jar
   into local commits in `local-mods/`, so the push of step 2 below would have served it from
   GitHub. Resolved for the pack on 2026-11-06 with option A of
   [open items](../open-items.md#owner-decisions): git tracks only the pointer
   `local-mods/ZeroCore2-1.20.1-2.1.48.jar.fetch.json`, the builder fetches CurseForge file 7344725
   and checks its size and SHA-1, and the record says OK
   (`modrinth-permissions/ZeroCore2-1.20.1-2.1.48.jar.md`). No pushed commit ever held the jar, and
   before the first push a history rewrite of 2026-11-06 put the pointer in its place in every local
   commit. The builder and the upload script refuse while any commit reachable from HEAD holds it,
   see [tools](tools.md#pointed-jars).

Decided on 2026-10-31 at 21:48: BrassAmber BattleTowers stays on the server and players add the jar by
hand. The server refuses a client without it, and it is All Rights Reserved and only on CurseForge, so
bundling it would need a grant from its author. Since about 21:56 the Modrinth body tells players to
get it from CurseForge. See [mod set](mod-set.md#jars-outside-the-index).

Fixed on 2026-10-31: the stale project texts (21:25, the new summary and body credit ts4.party,
name no person or handle and drop "kitchen-sink", Better Combat and Radium), the Nyx entry that downloaded from a GitHub release that does not exist (d3391bf,
the jar is bundled), the two jars the server ran outside the pack (782126f, AllTheLeaks and the
Cataclysm Spellbooks build), the out-of-date permission folder (955275c), and the configs that server
boots had rewritten on disk (3a398b1, the builder reads git HEAD).

## What a player gets

- Every index entry with its client env, from the Modrinth CDN, unpatched. The server runs eleven of
  them patched, which only matters on the server. Since 3.0.7 this includes Modflared, which opens the
  Cloudflare Tunnel to `hex.skytekx.com`, see [client](client.md#mac-mini).
- The bundled jars under `overrides/mods/`, 44 in 3.1.1 and 3.1.2 and 49 in 3.1.3. Since 3.1.3 they
  include Simply Jetpacks 2, patched as `SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`, EnderIO Legacy
  Machines, Mob Grinding Utils, Flux Networks and SkyTekx Molten, with the license texts under
  `overrides/licenses/`. From the next pack after 3.1.3 on they are 59, with SkyTekx DE Fixes,
  Extreme Reactors 2, ZeroCore 2, the LittleTiles build of c5694e16, the Mystical Automation port, the
  Mekanism: Extra Modules port, the Expanded Delight port, the Create: Integrated Farming port, the
  Ender IO backport build 6.3.0-skytekx.1 in place of 6.2.18-beta, the Create Crafts & Additions fork
  1.20.1-1.7.3-skytekx.1 in place of the Modrinth file 1.3.3 and the new Actually Additions fork
  1.20.1-1.1.52-skytekx.1 (`local-mods/`, 58 jars and the pointer file of ZeroCore 2 on 2026-11-06).
  The builder fetches the ZeroCore jar from its CurseForge file, so it reaches players only inside the
  `.mrpack`, as its author allows. Under `overrides/licenses/` travel the sources of the Backpack port,
  of the LittleTiles build and of Mekanism: Extra Modules, the license texts of Simply Jetpacks 2,
  EnderIO Legacy Machines, Flux Networks, SkyTekx Molten, Extreme Reactors 2, SkyTekx DE Fixes,
  Mystical Automation, Mekanism: Extra Modules and the boss data of Iron's Apothic Invaders, the
  license texts and the NOTICE of the Expanded Delight port, the LGPL, GPL and MIT texts and the NOTICE
  of the Create: Integrated Farming port, the license texts of the parts of the Ender IO build
  6.3.0-skytekx.1 with their NOTICE, the MIT text of the Create Crafts & Additions fork with Create's
  two MIT notices, `createaddition-1.20.1-1.7.3-skytekx.1-LICENSE.txt`, and the MIT text of the
  Actually Additions fork with the Mekanism notice,
  `ActuallyAdditions-1.20.1-1.1.52-skytekx.1-LICENSE.txt` (`licenses/` in git).
- The `config/` and `kubejs/` files of the commit the pack was built from, 534 config files and 6
  KubeJS files in 3.0.6 and 3.0.7 (3.0.5 shipped 550 config files from disk). They include the
  FancyMenu layout, but the FancyMenu jars are not in the pack, see [client](client.md#main-menu).
- The two Skylands datapacks and, since 3c2662e, the `skytekx3_fixes` datapack under
  `overrides/world/datapacks/`. That path fits a server folder whose world is called `world`. A
  singleplayer save keeps its datapacks in `saves/<world>/datapacks/`, so a singleplayer world gets
  none of them unless the player copies them by hand.
- Forge 47.4.18, the build the server runs inside Arclight (c70f699, 47.4.6 before).
- Since 3.0.7 a server list under `client-overrides/servers.dat` with the one entry SkyTekx at
  `hex.skytekx.com`. An instance that is updated to 3.0.7 gets this file in place of its own server
  list, so its own entries are lost. The changelog of 3.0.7 says so.
- No resource pack, none of the other jar patches and no BrassAmber BattleTowers, which players add by
  hand.

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
override comes from git HEAD through `git archive`, and the builds of 20:19 and 21:54 have none of the
files above.

## Release steps

On the build machine, after the blockers are fixed:

1. Set the new version in `VERSION` of `tools/build_mrpack.py` and of `tools/publish_modrinth.py`, and
   write the changelog in `docs/changelog/<VERSION>.md`, starting from `docs/changelog/next.md`. If
   the public address changes, change `SERVERS` in `build_mrpack.py` as well.
2. Commit everything the pack should carry, since the builder reads git HEAD, not the disk. Before
   the push, check that no commit holds a jar that a pointer file stands for, at any path:
   `git log --oneline --find-object=f94d426ce1c02a3d3cbb16ddaf09db424c5435ae HEAD` has to print
   nothing for ZeroCore 2, and for another pointed jar the id to look for is what `git hash-object`
   gives for its fetched copy. The builder and the upload script refuse while it prints anything,
   see [tools](tools.md#pointed-jars). Merge the branch that carries `ports/` into master, push it
   and run `git fetch`. Then build with `python3 tools/build_mrpack.py` and commit the rewritten
   `modrinth.index.json`. The builder refuses a public pack while a bundled jar points its source at
   `tree/master/ports/<dir>` on GitHub and the local `origin/master` lacks that directory or that
   build of the jar in `local-mods/`, and the upload script checks the jars inside the `.mrpack` the
   same way (`tools/mrpack_permissions.py`). Those source links are the source offers that the
   licenses of these builds demand. The builder also downloads every jar that `local-mods/` names by
   a pointer file, ZeroCore 2 from `mediafilez.forgecdn.net` among them, into
   `~/.cache/skytekx3/local-mods/` (or `$SKYTEKX3_FETCH_CACHE`), and refuses the build when a
   download fails or its size or SHA-1 differ from the pointer. The upload script refuses a
   `.mrpack` without that jar or with other bytes, see [tools](tools.md#pointed-jars). A local test
   build that is never uploaded, while other blockers remain, is
   `python3 tools/build_mrpack.py --allow-unpermitted`. It writes
   `SkyTekx3-<version>-UNPERMITTED.mrpack`, which the upload script never picks up.
3. Import the `.mrpack` into a fresh Prism instance with a 12 GB heap, add BrassAmber BattleTowers by
   hand as players do, and join the server with it. On Asahi Linux add the JVM flag from
   [client](client.md#java-and-the-asahi-flag) and join by the Mac mini's LAN address, since Modflared
   cannot open the tunnel on Linux on ARM. Stay in the world until EMI logs "Reloaded EMI" and the heap
   settles. The 90% message of AllTheLeaks at the join is normal. A heap that stays full is not.
4. Load the token from the vault (see [secrets](../server/secrets.md)) into `MODRINTH_TOKEN` and run
   the dry run: `python3 tools/publish_modrinth.py`. It prints the `monetization_status` that the
   Modrinth API reports for the project and warns when the project is missing or not demonetized. The
   Patchouli parts of Ender IO (CC BY-NC-SA 3.0) and the assets of the Twilight Forest fork (CC BY-NC-SA
   4.0) allow no commercial use, so the pack must earn nothing, Modrinth creator rewards included
   (`modrinth-permissions/EnderIO-1.20.1-6.3.0-skytekx.1-all.jar.md`). Modrinth creates every project as
   `monetized` (`project_creation.rs` of its server Labrinth, read in the license review of
   2026-11-05), and the public projects of Extreme Reactors, ZeroCore, LittleTiles, Iron's Apothic
   Invaders and Ender IO all reported `monetized` that day. The rewards reach every creator
   automatically (modrinth.com/legal/cmp-info), so the status of the project is the switch that
   counts. Record the status and the date in the row of the version. Since the pack bundles a
   modified Ender IO build (de0d78b), a CDN entry of the index can no longer drop the Patchouli
   condition, and the Twilight Forest fork sets the same limit, so the project stays demonetized.
5. Upload: `python3 tools/publish_modrinth.py --go`. It creates the project if it is missing, sets its
   `monetization_status` to `demonetized` through the API, reads it back and refuses the upload unless
   Modrinth reports `demonetized` or `force-demonetized`. Then the version goes up as a featured beta,
   and the script compares the SHA-512 of the uploaded file with the local build. Nobody has run `--go`
   with this check yet, so whether the token may set the field is not verified.
6. If `docs/MODPACK_SUMMARY.txt` or `docs/MODPACK_DESCRIPTION.md` changed, replace the project texts
   with `python3 tools/publish_modrinth.py --texts`. To make the project public, submit it for review in
   the Modrinth web UI. Only then does it become public (memory `project_skytekx2_modpack.md:31`). The
   project has an icon.

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
| 3.0.7 | 2026-10-31 21:54 | 67,246,343 B | 217 | 35 | Modflared 1.2.0+release.61 as a client only index entry and the server list `client-overrides/servers.dat` with `hex.skytekx.com`, for the Cloudflare Tunnel. Every other file as in 3.0.6. Uploaded at 21:55 as `LRkF99Pz` |
| 3.1.1 | 2026-11-03 17:37 | 73,371,625 B | 225 | 44 | Private build with `--allow-unpermitted`, sent straight to players and never uploaded, because five bundled jars lack permission. It carries everything since 3.0.7, among it the Twilight Forest backport, Nyx 1.0.1, the minimap at the top right (`config/xaerohud.txt`) and the single Hexbiscus |
| 3.1.2 | 2026-11-03 20:33 | 73,371,076 B | 224 | 44 | Private build like 3.1.1. Elenai Dodge 2 removed, Nyx allows enchanting by day |
| 3.1.3 | 2026-11-05 00:41 | 75,915,546 B | 232 | 49 | Private build like 3.1.2, sent straight to players. The nine mods of 2026-11-04 with FLib, Advanced Loot Info in the place of Just Enough Resources, Flux Networks, SkyTekx Molten, EMI 1.1.24-skytekx.2, the patched Simply Jetpacks 2, Starlight off the clients, 3D dropped items and no Read Me note of The One Probe |

Pack 3.1.3 is the first with Simply Jetpacks 2 bundled and the first that ships a patched jar (see
[boot fixes](../content/boot-fixes.md#jar-patches)). It is also the first that keeps Starlight off the
clients, that ships the config of Nostalgic Tweaks with 3D dropped items (see
[client](client.md#dropped-items)) and the common config of The One Probe with `spawnNote = false`. It
brings Advanced Loot Info (see [mod set](mod-set.md#advanced-loot-info)), Flux Networks (see
[mod set](mod-set.md#flux-networks)), SkyTekx Molten and the nine mods of 2026-11-04, five addons of
Applied Energistics 2 and Cyclic among them (see [mod set](mod-set.md#additions-of-2026-11-04)). No
client loaded it before it went to the players on 2026-11-05. A static check of its 268 client jars
found every mandatory dependency of the sides BOTH and CLIENT present in a matching version, apart from
a nested old copy of L2 Library whose newer standalone jar Forge picks. A fresh install, the update of a
3.1.2 instance in Prism Launcher and the Modrinth App, a join without Starlight, the loot tabs of
Advanced Loot Info in EMI and the machines of the new mods are not tested yet. Its deploy moved the
fixes pack to the end of the pack order, see [boot fixes](../content/boot-fixes.md#pack-order).

Pack 3.1.3 went live without the mods below. The next pack after 3.1.3 is the first with
SkyTekx DE Fixes (see [Draconic Evolution fixes](draconic-fixes.md)), with Extreme Reactors 2.0.97,
ZeroCore 2.1.48 and the LittleTiles build of c5694e16 bundled (see
[mod set](mod-set.md#bundled-jars)), with the Mystical Automation port (see
[Mystical Automation port](mysticalautomation-port.md)), with the Mekanism: Extra Modules port (see
[Mekanism Extra Modules port](mekanism-extra-modules.md)), with the Expanded Delight port and the salt
of the pack unified to the Mekanism salt (see [Expanded Delight port](expandeddelight-port.md)), with
the Create: Integrated Farming port and its library Create: Dragons Plus 1.11.9 (see
[Create Integrated Farming port](createintegratedfarming-port.md)), with the Ender IO backport
6.3.0-skytekx.1 (see [Ender IO backport](enderio-backport.md)), with the Create Crafts & Additions
backport 1.20.1-1.7.3-skytekx.1 (see [Create Crafts & Additions backport](createaddition-backport.md)),
with the new Actually Additions fork 1.20.1-1.1.52-skytekx.1 (see
[Actually Additions backport](actuallyadditions-backport.md)), with the Apotheosis bosses of
Iron's Apothic Invaders and three Twilight Delight recipes (see [KubeJS](kubejs.md)), and with Bygone
Nether, whose tag repair in the fixes pack also needs that pack at the end of the pack order (see
[mod set](mod-set.md#bygone-nether)). Its row should record whether a fresh install and the update of
a 3.1.3 instance were tested in Prism Launcher and the Modrinth App, whether the screens and JEI
categories of Mystical Automation work in a client, whether the module screen and HUD of Mekanism:
Extra Modules, the juicer screen and boats of Expanded Delight and the Ponder scenes and the JEI
category Roosting of Create: Integrated Farming work there, whether a client joins with the Ender IO
network protocol 1.0-skytekx.1 and the update of a 3.1.3 instance replaces the 6.2.18-beta jar, since
server and client need the same build, whether JEI and EMI show the Vat recipes, whether the screens
of the Vat, the obelisks and the wireless charger and the staff and magnet keys work, whether the
conduit facades render with the Embeddium of the pack, whether a client joins over the Create Crafts &
Additions channel `createaddition:main` with its protocol 2 and the update of a 3.1.3 instance drops
`createaddition-1.20.1-1.3.3.jar`, whether JEI and EMI show the charging steps of Sequenced Assembly,
whether the new ponder scenes of the pump, the connectors and the servo open, whether a client joins
with the Actually Additions protocol 5, whether its manual opens as a Patchouli book and JEI and EMI
show its categories, whether the Draconic Evolution fixes work in a client and whether the mobs and
structures of Bygone Nether render there. The server appends the pack of every mod it sees for the
first time after the fixes pack, so its deploy has to move the fixes pack to the end of the pack order
again, as the deploy of 3.1.3 did, see [boot fixes](../content/boot-fixes.md#load-order).

The upload has to go out together with the jar swap of Create Crafts & Additions in the server's
`mods/`, which still holds 1.3.3. The fork bumped the protocol of `createaddition:main` from 1 to 2
and accepts no other (`CreateAddition.java:73-77` in `ports/createaddition/`, 0611ece), so a 3.1.3
client cannot join the fork server and a client of the next pack cannot join the 1.3.3 server. Both
wait for the alternator scan and the live world test of the wires on a copy of the live world, the
rollout gates under [Release](createaddition-backport.md#release) of the backport page.

The Actually Additions fork is new and replaces no jar, but a client of the next pack carries it.
Its channel `actuallyadditions:main` accepts only its own protocol 5 and no absent mod
(`PacketHandler.java:169-175` in `ports/actuallyadditions/`), so that client cannot join a server
without the fork. The upload therefore also goes out together with copying
`ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar` into the server's `mods/` while the java process is
gone, see the [operator notes](actuallyadditions-backport.md#operator-notes) of the backport page.
No pack client has joined a server with this jar yet, so no public pack with it goes up before a
client from the `.mrpack` has logged in to the Arclight test server over protocol 5, at least the
login item of carryover `aa-client-test`, see [Release](actuallyadditions-backport.md#release) of
the backport page.

The full pack of 052a0e4 with the six mods that stand in for NeoForge 1.21.1 mods, Create: Dragons
Plus included, booted on Forge 47.4.18 and on Arclight on 2026-11-06 with the error baseline of the
test copies, see [NeoForge ports](neoforge-ports.md#status). That boot covers the server side only.
The player notes for these six mods are drafted in `docs/changelog/next.md`. Before the upload, add
the other changes of the next pack to it, and rename it to `docs/changelog/<VERSION>.md`, the file
that `tools/publish_modrinth.py` sends as the changelog of the version.

Versions 3.0.0 to 3.0.4, 3.0.6 and 3.0.7 are on Modrinth. 3.0.5 was built but never uploaded. None of the
`.mrpack` files is in git (`.gitignore:36`). The 3.0.5 file was committed on 2026-07-12 at 23:43
(1092a5b) and dropped again before the push, with the note that it ships via Modrinth (reflog, c1a199b
and the reset to fded6ce).

Related: [tools](tools.md), [mod set](mod-set.md), [client](client.md),
[secrets](../server/secrets.md), [open items](../open-items.md).
