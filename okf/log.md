---
type: Log
title: Change log
description: Chronological change log of SkyTekx 3 from the audit of SkyTekx 2 on 2026-06-26 to the move to the Mac mini and the Halloween world of 2026-10-31.
tags: [log, history]
timestamp: 2026-11-03
---

# Change log

Oldest first. Times are the build machine's local time, CEST up to 2026-07-13 and CET from 2026-10-31.
The build sessions themselves were deleted, so this log rests on the owner's prompts in
`~/.claude/history.jsonl` (session 5bd3e1de, and 3dcdf872 for the hosting question of 2026-06-26
23:09), the git history, the server logs and crash reports, the `.mrpack` files and file times.
Rulings are collected on [Decisions](decisions.md).

The git history before 2026-06-29 19:28 was rebuilt on the owner's request. The first 15 commits of
master, bc9a935 to c52f244, all carry 2026-06-29 18:52:20, so their dates say nothing about when the work
was done. The times below come from the prompts and the logs. The lineage before 2026-06-26 is on
[History](history.md).

## 2026-06-26: Audit of SkyTekx 2 and the choice of 1.20.1

- At 15:21 the owner asked for an updated version of SkyTekx 2, of which they are the original author.
- The session audited the 209 mods of SkyTekx 2 and mapped them to 1.21 and to 1.20.1. On 1.20.1, 170
  of them have a path and 128 survive as the same mod or a direct successor, while 1.21 loses Tinkers'
  Construct. The reports sit untracked in the SkyTekx 2 checkout, see
  [Related repos and services](related.md#skytekx-2-audit-files).
- At 17:58 the owner objected that 1.21 loses Tinkers' Construct and that Silent Gear is no real
  alternative. At 22:14 and 22:18 they asked whether 1.20.1 or 1.21 suits them better and how Arclight
  compares with Mohist. At 23:09 they asked for a VPS with a very strong single core and 8 GB of RAM or
  more.

## 2026-06-27: First build on Arclight

- At 10:34 the owner set the goal: feel as much like SkyTekx as possible, the most stable mod
  configuration, the old mods plus a few new ones, and recipes that never conflict. At 10:55 they asked for
  a new repo, and at 11:04 for an ansible vault that holds the Modrinth token.
- At 12:01 the first boot ran on Red Hat OpenJDK 21.0.11 and failed because `irons_lib` was missing. At
  12:04 the server moved to Temurin 17.0.19, which every later run used (`boot.log`, `boot2.log`).
- At 12:05 Radium crashed the world load, because its merged `PortalForcer` method broke Arclight's
  mixin (`crash-reports/crash-2026-06-27_12.05.07-server.txt`). Radium was removed, and the run of 12:06
  reached `Done` after 35 seconds.
- At 12:07 the owner asked whether the server would carry 20 players on an Apple M4 Mac mini, and at
  12:21 they noted that the smallest model has 16 GB. The 11 GB heap of `ServerLinux.sh` was sized for that
  machine. Load with 20 players was never measured, see [Open items](open-items.md).
- At 12:11 the first pack, `SkyTekx3-3.0.0.mrpack`, held 109 Modrinth mods with the configs and the KubeJS
  scripts. Recipes run through AlmostUnified, Polymorph and KubeJS, the successors of UniDict,
  NoMoreRecipeConflict and CraftTweaker, see [AlmostUnified](modpack/almostunified.md).
- At 12:49 the owner said "yes do the skyblock generator". Pack 3.0.1 (15:08) added the first Skylands
  datapack with 8 files, the dimension and `/trigger` teleport functions.
- At 15:09 the owner set the project contact address to `skytekx@erebos.xyz`.
- Twilight Forest was built from source and became the first bundled jar (17:29 ask, pack 3.0.2 at
  19:57). Pack 3.0.2 also brought Iron's Spells with its hidden library `irons_lib`. At 18:23 the owner
  asked not to publish the Chocolate Quest version yet. At 20:46 they said "yes pls publish", and 3.0.3
  (20:51) was uploaded with the server icon as its Modrinth icon.
- At 20:43 and 21:16 AsgardShieldReloaded crashed the server twice when a creeper exploded next to the
  owner in the Skylands. It was never fixed, see [Logs and crashes](server/logs-and-crashes.md).
- At 21:02 the first commit was made, and at 21:11 it was pushed to the new GitHub repo
  `skytekx/skytekx3-1201-server`.
- Between 20:49 and 21:49 the owner asked for the old SkyTekx start menu with its logo, without a
  resource pack, for islands that are less high and stacked, for short commands like `/gm`, and for loot
  chances like SkyTekx 2. EssentialsX 2.22.0 arrived at 21:50 for the short commands.

## 2026-06-28: The Skylands generator takes shape

- The server ran 22 short times between 17:58 and 22:56 while the generator was tuned. Each boot took
  33 to 53 seconds.
- The owner's rulings of that evening shaped the islands: smaller and a little further apart so
  players can build bridges, no flat tops, a different biome per island (18:33), and never any lava or
  water falling into the void (19:58, which replaced the bedrock catch basins of 18:33 and 18:46).
  At 19:58 they also asked for fewer islands, a bit bigger and with much more distance between them.
  Contained liquids where they would naturally spawn (20:13) and small lakes that never leak (20:35)
  followed. They called the result "a lot better" at 20:35 and "looks great" at 22:29.
- The FancyMenu title screen layout with the SkyTekx logo was made at 18:10 to 18:12, see
  [Client](modpack/client.md#main-menu).
- At 22:30 the owner asked to add mod structures to the Skylands. At 22:55 they asked that all
  advancements show their real names. A resource pack with 66 Spartan Weaponry advancement names
  followed at 23:05. It was never committed or shipped.
- At 23:15 the owner asked for chocobos, and at 23:18 they called the chocobo mod a core mod of SkyTekx.
- The last run of the day stopped writing its log at 23:50 without a crash report. The cause is
  unknown.

## 2026-06-29 morning: More mods, a fork and the website

- From 05:09 to 06:16 the owner's client crashed three times on Cataclysm Spellbooks against stock
  Cataclysm 3.31. At 06:25 the owner ruled to patch Spellbooks, not Cataclysm, and to fork the archived
  Spellbooks repo. The fork `skytekx/Cataclysm_Spellbooks` got its port at 06:54, and the built jar went
  into the server's `mods/` only, see [Mod set](modpack/mod-set.md#jars-outside-the-index).
- At 06:42 the owner asked for a website on skytekx.com. Its repo `skytekx-web` started at 11:41, see
  [Related repos and services](related.md#the-website-skytekxcom).
- Between 08:26 and 09:12 the owner ruled that no structure may stand on the y=0 floor above the void.
  The structure placement got a surface height gate (`min_surface_y`).
- Between 10:00 and 10:24 the owner set the credit line "a modpack by ts4.party, the TeamSpeak for
  party, formerly known as ts3.party" and ruled out real names in the credits. At 10:11 they asked to make
  the Modrinth project public.
- At 11:40 they ruled out Battle Towers with the explosion golem in the Skylands and asked to turn off mob
  explosion damage to blocks there. At 11:52 they called Battle Towers essential. At 12:00 they named
  BrassAmber BattleTowers as its successor, and that jar went into `mods/`.
- At 12:10 they asked for a sword swing like 1.7. The KubeJS script `combat_1_7.js` removes the attack
  cooldown, see [Combat](content/combat.md).
- At 12:26 they asked to find as many old mods or their successors as possible, from Modrinth first, then
  GitHub, CurseForge only as a last resort. A batch of 69 jars landed at 12:47. At 12:49 the boot failed,
  because client-only mods (Forgematica, MaFgLib, WATERMeDIA) sat in the server's `mods/`. They moved to
  `client-only-mods/` and `disabled-mods/`, and the boot of 12:51 was clean.
- The Aether and Hexxit Additions arrived at 13:56, Aether Villages, AeroBlender and XP Tomes at 14:16.
- At 14:23 the owner asked to fork and port Nyx. The fork `skytekx/nyx` followed at 14:25, see
  [Nyx](content/nyx.md).
- At 15:02 they asked for the essentials of the Better Minecraft pack BMC4. A batch with AllTheLeaks,
  Biomes O' Plenty, Galosphere, Repurposed Structures and others came at 15:15. VanillaBackport then
  stopped the boot at 15:19 with a mixin failure (`logs/2026-06-29-8.log.gz`) and was parked in
  `disabled-mods/`.

## 2026-06-29 afternoon: Nyx, the Aether fall and a full regen (pack 3.0.4)

- Nyx was ported completely to Forge 1.20.1 and installed on server and client, with an index entry
  that is required on both sides (commit a265655).
- The Skylands got many more floating and ground structures. Structures used the custom placement
  `skytekx:surface_spread` of the skytekx-worldgen mod, and raising its `min_surface_y` from 16 to 36
  stopped most structures from reaching the y=0 floor.
- An Aether-style fall was added as the KubeJS script `skylands_fall.js`: drop off an island and land in
  the Overworld. It never worked, see the evening entry.
- At 17:04 the owner asked to regenerate every dimension, to commit and to publish again. The world
  was backed up at 17:15 (`~/skytekx3-world-backup-prewipe.tgz`), and the regen run of 17:16
  pregenerated 16,129 chunks of the Overworld in 6:41 and of the Skylands in 2:03, with the same seed.
- Pack 3.0.4 was built at 17:29 with 213 index entries and 32 bundled jars. The old change log
  recorded an upload while the project stayed a draft. That cannot be checked without the token.

## 2026-06-29 evening: Git history, docs and the hardened Arclight

- At 18:38 the owner asked to fix the email of the first commit, to rebuild the history as granular
  multi-line Commitizen commits, and to write a self-hosting README, an AGENTS.md and an OKF wiki. The
  rebuilt history was force-pushed at 18:53. At 18:46 and 18:47 the owner ruled that the author rule
  stays out of the wiki and AGENTS.md, because other people may code with an agent.
- At 19:19 the owner reported worse performance and that the fall was still not like the Aether. The
  Rhino fix of 19:28 (de6ec6e) did not help. Commit 241117c (20:10) states that the script threw a
  `TypeError` every tick and never teleported anyone, and it replaced the script with a Java handler in
  the skytekx-worldgen mod.
- At 20:10 chocobos became easier to find (spawn weight 10 to 20, 043c9be) and the Skylands got six
  Dungeons Arise floaters at spacing 44 (fafc7ca).
- From 19:35 an RCON sweep of `/locate` over every modded structure, looking for the structure at
  overworld x 0, y 110, z 451 that the owner wanted in the Skylands, stalled the server thread. The
  watchdog fired at 19:41:18. During the forced shutdown Arclight's DistanceManager threw
  `NoSuchElementException` in the tick loop (crash reports 19:41:20 and 19:41:29).
- After the owner asked at 21:22 to fix the underlying issue and at 21:32 to harden Arclight with
  backports, a patched Arclight 1.0.6 was built at 21:50 and ran from 21:55. Commit 8b1c231 (22:11) ships
  `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar`, the symlink `arclight.jar` and the patch in
  `patches/`, see [Arclight runtime](server/arclight.md#the-hardened-jar).
- At 22:14 the owner ruled that commit messages carry no dashes and no semicolons and asked to clean
  the whole history. The messages were rewritten with `git filter-branch` at 22:45 and force-pushed at
  22:51. Two spaced hyphens used as dashes survived, in bc9a935 and 573b71b.
- At 22:45 the hot air balloon of the Ruins mod and seven more structures were floated in the Skylands
  (b2b5861).
- At 22:30 the owner asked for the performance report in the OKF and not in `docs/`.
- Between 23:00 and 23:41 the owner looked at Sponge as a better maintained server. Sponge has no
  build for Forge 1.20.1, so the server stayed on the hardened Arclight.

## 2026-06-30 night: Profiles and structure density

- Spark profiled live chunk generation at 23:49 and 23:57 on 06-29. Commit 8a35bed (00:01) records the
  result on [Performance](server/performance.md). Vanilla terrain code takes 49.6 % of the samples in the
  Overworld and 61.7 % in the Skylands, the JVM itself 23.8 % and 17.9 %. The costliest mod is Starlight
  with 0.85 % and 3.0 %, itself a performance mod. No content mod is worth cutting, a wider pregen is.
- At 00:23 the owner judged that the Ruins mod adds little and weighs a lot at chunk loading. It left
  the pack at 01:13 (720b8a4), whose message also notes that its writer thread threw errors every few
  minutes. Eight more structures were floated in its place.
- At 01:20 the owner set a standing rule: decisions that improve speed and reliability win.
- At 01:34 a density audit (0d79972) found the Skylands crowded, about one structure per 210 blocks
  from four overlapping layers. Six raw Dungeons Arise structures duplicated a floater, and two heavy
  Cataclysm builds generated in the sky. Cataclysm's own code costs nothing measurable, but a saturated
  region generated at 79 chunks per second instead of 115 to 124, with almost three times the garbage
  collection.
- Commit 87f885f (03:37) took the lightest cut. The Skylands biome tags of the six duplicates and
  of the acropolis and the soul black smith were deleted, 16 files in both datapack copies. After a
  restart none of the eight was found in the Skylands and all still generated in their own dimensions,
  as the owner then ruled at 03:48.
- Between 03:55 and 04:01 the owner asked for common chocobos, common friendly structures and chocobos
  only in the Skylands. Commit 6c5b986 (04:18) set the floaters from spacing 44 to 32, the island ground
  set from 18 to 14 and villages from 34 to 24. Commit 0f1b5d3 added chocobos with weight 30 to the 14
  island biomes, set ChocoCraft's Overworld, mountain and Nether spawns to `forge:none` and moved
  gysahl greens to the islands. These overrides turned out to lose against the mod's own data in this
  world, see [Skylands worldgen](content/skylands-worldgen.md#known-defect-the-chococraft-overrides-lose).
- A keep stood at Y 0, so the Dungeons Arise major and minor sets also moved to `surface_spread` with
  `min_surface_y` 36 (29bc494).
- At 04:24 the owner asked that climbing high enough in the Overworld puts the player on a dirt block at
  y=0 in the Skylands, as the main way to switch, instead of the `/trigger` command. Commit 786ee08
  (04:37) built it, see [Skylands travel](content/aether-fall.md).

## 2026-06-30 morning: From 152 boot errors to 3

At 04:15 the owner asked to resolve every error and warning at server boot. The work is on
[Boot fixes](content/boot-fixes.md).

- Commit 52f4f19 (05:26) added the override datapack `skytekx3_fixes`. It replaces 14 broken
  advancements and 99 broken loot tables of other mods with valid empty versions. It also fixed the
  inverted power range of the Apotheosis enchantment `dimdoors:string_theory`, turned off Collective's
  update check and deleted a stray loot table from the Ad Astra jar that crashed Bird's Nests. Boot
  ERRORs fell from 152 to 27. The lesson: a world datapack in the middle of the pack order loses to the
  mod packs loaded after it, so overrides need a pack that loads after them.
- At 05:49 the owner ruled that before a feature is made optional, the mod that provides it gets
  installed. Commit 518aa19 (06:21) installed SlashBlade Resharped, Re:Avaritia and Medieval Embroidery,
  added the Bird's Nests guard mixin to the skytekx-worldgen mod and started `tools/patch_mods.py` for
  fixes inside mod jars. Boot ERRORs fell to 10.
- Commit af5860e (07:05) installed The One Probe and Aether Genesis, built from its GitHub branch
  `1.20.1-develop`, and made the TiCEX gem and gun tools load. Three accepted ERRORs remained: the
  Simply Jetpacks guide book, an AllTheLeaks reflection into Citadel, and EssentialsX's warning about the
  hybrid server. WARN lines stayed at about 300.
- At 08:05 the owner had Re:Avaritia removed again, because TiCEX 0.6.0 cannot use it (b6a1e20, 08:11).
- The run started at 08:09 became the longest on record and lasted until 2026-07-03.

## 2026-06-30 afternoon: The client and Modrinth

- At 13:54 the owner's client was refused at the Forge handshake because it lacked the mods that went
  onto the server that morning ("Missing required datapack registry: slashblade:named_blades"). At 13:55
  the owner wrote: "you did not fix the Minecraft client at all, you only fixed the errors in the server
  it seems." The client was updated by hand later and joined again on 2026-07-01 at 19:56.
- At 18:14 the website got its last commit, ef50cc2.
- At 18:16 the owner noted that the Modrinth project could not go public because of its permissions
  page, and at 18:20 asked for a folder with the needed files. Commit 7995a0a (18:39) added
  `modrinth-permissions/` with one file per bundled jar. Of 34 jars, 6 are All Rights Reserved without a
  grant, see [Publish](modpack/publish.md#what-blocks-a-release).

## 2026-07-03: The long run ends

The run of 2026-06-30 08:09 logged a server tick of 60.90 seconds at 21:09:57, right after an 83 second
"Can't keep up", and then nothing more (`logs/2026-07-03-2.log.gz`). There is no crash report. The host
most likely slept or the process was killed.

## 2026-07-12: The Skylands become a pure datapack (pack 3.0.5)

- At 18:04 the owner asked to start the server on the build machine. It ran from 18:11, and the owner
  joined at 20:31.
- At 20:36 they recalled the ruling against structures on the y=0 floor. At 20:39 they asked for a more
  elegant design and ruled that the same world generator must also work on an unmodded Spigot server.
- The skytekx-worldgen source went into git at 21:43 in `~/git/skytekx-worldgen`. Its structure placement
  and fall handler were deleted, and the mod became SkyTekx Compat 2.0.0, which only guards Bird's Nests,
  see [SkyTekx Compat](content/compat-mod.md).
- Commits 7670df4, 97ffad9 and b7dd95f (22:31 to 22:32) replaced the mod placement with vanilla
  void biome gating. A 2D island mask feeds the terrain, a core slab from Y 52 to 77 and the continents
  climate channel. Columns over the void get the featureless biome `skytekx3:sky_void`, which sits in no
  `has_structure` tag, so vanilla rejects every structure start there. The five `surface_spread` sets
  went back to `random_spread` with the same salts. The previously missing `village_snowy` tag was
  fixed on the way.
- The pack split in two. The shared `skylands-datapack/` registers only vanilla and `skytekx3` ids, and
  `skylands-datapack-forge/` carries the Dungeons Arise sets, 21 floaters from mod pools and the Botania
  features. The script `tools/sync_skylands_datapack.sh` copies both into the world. The session
  reported clean boots of the shared pack on vanilla and on BuildTools Spigot 1.20.1. Those test logs
  were not kept.
- Travel became plain datapack functions: below Y 0 in the Skylands drops the player to Y 300 in the
  Overworld, and above Y 319 in the Overworld lifts them onto a 3 by 3 dirt platform at Y 0 in the
  Skylands, see [Skylands travel](content/aether-fall.md).
- At 23:23 the Skylands were backed up (`~/skylands-prewipe-2026-07-12.tgz`) and wiped. The boot of
  23:25 reached `Done` with 0 FATAL and 3 ERROR lines, and the Skylands were pregenerated to radius 1000
  (16,129 chunks in 3:31).
- Pack 3.0.5 was built at 23:40 with 216 index entries and 33 bundled jars (65,487,506 bytes). It was
  committed at 23:43 and taken out of git again at 23:49, and 4c5e331 (23:59) set the index version to
  3.0.5.
- The run ended on 2026-07-13 at 15:45 with a signal stop that was cut off while saving, see
  [Logs and crashes](server/logs-and-crashes.md#stop-path).

The prompt history has no SkyTekx prompt between 2026-07-12 21:45 and 2026-10-31 16:12.

## 2026-10-31: The Mac mini and the Halloween world

The owner's prompts of this day are in session c0e872d8 of `~/.claude/history.jsonl`. The Mac mini's
logs stay on the Mac and are not in the repo.

- From 16:02 on the Mac mini was slimmed for headless use, and at 16:12 the owner made it the server
  for the time being. At 16:33 the owner asked to read all logs and code and to write this knowledge
  base, and at 16:33 and 16:34 to finalize, test and deploy on the Mac mini and to optimize everything
  for it.
- The Mac's system log stops at 16:35:35 without a panic report, and the next boot is at 16:52. That
  points to a power cut, and autorestart brought the Mac back.
- At 16:56 the owner asked for a new map with a light Halloween theme, at 16:57 that nothing be placed
  between the islands, and at 16:58 for a check of the installed mods for updates. At 17:47 the Mac
  mini became a compute host for the checks.
- 55 Modrinth updates, the new dependency MezzConfig and three bundled jars (0a20b8c, 1ba93f7), more
  blood moons and Nyx events in the Skylands (44fdf98), the Halloween server icon (064cc56), pumpkin
  patches on the islands (1cc9702) and the first Skylands fix (1c6f8d4, Battle Towers, strongholds and
  the illager campsite) were committed at 18:14 and 18:15. See
  [Mod set](modpack/mod-set.md#update-of-2026-10-31).
- The Halloween world was created on the Mac mini at 18:07:17. Chunky pregenerated the Skylands to
  radius 3000 from 18:13 to 18:32 and the Overworld to radius 1500 from 18:32 to 18:40.
- The region scans of that run found 307 petrified root blocks and two windmill field blocks below the
  islands. Commits 50824a3 and bcb113c fixed both at 18:52. The server stopped over RCON at 18:53:14,
  the Skylands were wiped, and the first start under launchd at 18:54:57 generated them again from
  18:55 to 19:15. See [Skylands worldgen](content/skylands-worldgen.md#changes-of-2026-10-31).
- At 18:57:56 a vanilla chunk save race logged a `ConcurrentModificationException` in `IOWorker`
  during that pregen. The chunk is complete in the region file, see
  [Logs and crashes](server/logs-and-crashes.md#chunk-save-race-during-pregen).
- Pack 3.0.6 got its version at 18:57 (b23a72f) and its publish script at 19:03 (222ee72). At 19:09 the
  builder was fixed to pack tracked files only (41d15c4) and the pack was built. That build was never
  uploaded.
- The scans of 19:20 found no structure piece below Y 24, no petrified roots and all 10 windmills
  whole, see [Mac mini](server/macos.md#pregen-and-worldgen-checks).
- Between 19:26 and 19:36 restart, stop, start, backup and a reboot were tested. The command
  `launchctl bootout` turned out to return before the job is gone, so `install.sh` and
  `skytekx3ctl stop` now wait for it.
  The first backup (688 MB) was written at 19:34. See [Mac mini](server/macos.md#tests-of-2026-10-31).
- At 20:00 a checksum dry run of the sync showed 15 config files that differ between the checkout and
  the Mac, see [Open items](open-items.md#defects-to-fix).
- At 20:09 the owner linked the Modrinth project and asked to update the pack and test it. With the
  owner's token the draft showed versions 3.0.0 to 3.0.4, so 3.0.5 had never gone up. Between 20:17 and
  20:20 the dead Nyx download left the index (d3391bf), AllTheLeaks and the Cataclysm Spellbooks build
  joined `local-mods/` (782126f), the permission files were brought up to date (955275c), and the
  builder started to read git HEAD (3a398b1), to ask for Forge 47.4.18 (c70f699), to use the website's
  tagline (5aeb24d) and to ship the fixes datapack (3c2662e). The publish script reads the token from
  the environment since 7f5ae03.
- Pack 3.0.6 was built again at 20:19:59 (67,245,989 bytes, index in 50d875e) and went up to the
  Modrinth draft at 20:27 as version `Gi0aBofN`. The CDN copy has the same SHA-512.
- The install test downloaded all 216 index files with matching hashes. Prism's Microsoft sign-in had
  expired, and the owner signed in again. The client reached the main menu, but its join at 20:38:51
  was refused because BrassAmber BattleTowers is in no pack. The owner sent a screenshot of that screen
  at 20:39. With BattleTowers added by hand the client joined at 20:41:31.
- After the join EMI dropped 7,246 JEI recipes, 7,243 of them Chipped recipes that hit an
  `AbstractMethodError` against JEI 15.74.0.233. AllTheLeaks reported 90% heap use at 20:42:05, and the
  owner asked about it at 20:43. The 8 GB heap stayed full, and EMI's reload died with an
  `OutOfMemoryError` at 20:46:33. The owner's sessions of June and July with JEI 15.20.0.132 had the
  90% message but neither failure. See [Publish](modpack/publish.md#test-of-2026-10-31).
- At 20:56 the owner asked to install Prism and the pack on their second MacBook as well, at 20:58
  for a public address over a Cloudflare Tunnel, and at 21:08 and 21:09 for `hex.skytekx.com` as fast
  as possible, because players were waiting. tracepath from the Mac mini showed carrier grade NAT on its
  mobile uplink, so a port forward was out.
- Two more runs of the test client narrowed the freeze down. JEI 15.20.0.132 cannot come back, because
  Forge refused to start with it: Tinkers' Construct needs JEI 15.59.0.210 or newer, Tinkers' Reforged
  15.56.0.204 and Polymorph 15.57.0.207. With JEI 15.74.0.233 and a 12 GB heap the client joined at
  21:12:07 without running out of memory. See [Publish](modpack/publish.md#test-of-2026-10-31).
- At 21:12:33 a BattleTowers obelisk 76 blocks from spawn stopped the server with a ticking entity
  crash, 26 seconds after a test client joined. launchd restarted it at 21:12:42. The fix
  `removeErroringEntities = true` was first written while the server ran and lost at the next stop,
  so it was set again with the server down at 21:35:54. The boot of 21:36:32 keeps it. See
  [Logs and crashes](server/logs-and-crashes.md#a-battletowers-obelisk-on-the-mac-mini-2026-10-31-211233).
- At 21:13 the Cloudflare Tunnel `skytekx3-mac-mini` went live. A server list ping through it from the
  build machine answered, and `hex.skytekx.com` carries the TXT record for the client mod Modflared.
  See [Mac mini](server/macos.md#public-access).
- At 21:17 the owner gave the address of the second MacBook. By 21:31 it had Prism, Java 17 and the
  pack with BattleTowers and Modflared, see [Client](modpack/client.md#second-mac).
- From 21:22:01 to 21:28:43 the Ethernet link of the Mac mini was down. The server kept running and the
  tunnel reconnected by itself. See [Mac mini](server/macos.md#link-drop-of-2026-10-31).
- At 21:25 the summary and the body of the Modrinth draft were replaced with the rewritten
  `docs/MODPACK_SUMMARY.txt` and `docs/MODPACK_DESCRIPTION.md`. They credit ts4.party, name no person
  or handle, drop "kitchen-sink" and ask for 12 GB of memory. The publish script got `--texts` for
  this. See [Publish](modpack/publish.md#state-on-2026-10-31).
- At 21:48 the owner answered three questions. Players get in with a pack 3.0.7 that brings Modflared
  and lists `hex.skytekx.com`, not through a relay on an owner server or playit.gg, and voice chat
  still does not reach the internet. BrassAmber BattleTowers stays on the server, and players add the
  jar by hand. The whitelist stays off with the public address. See [Decisions](decisions.md#hosting).
- At about 21:50 the owner asked for the BattleTowers jar to send it to players over Telegram, and it
  was copied for them. The official file on CurseForge is identical to the jar in `mods/`, see
  [Mod set](modpack/mod-set.md#jars-outside-the-index).
- From 21:53 to 21:54 Modflared 1.2.0+release.61 joined the index as a client only entry (44358a8),
  the builder started to write the server list `client-overrides/servers.dat` with `hex.skytekx.com`
  (cba636e), and the version went to 3.0.7 (1603723). The changelog of 3.0.7 in that commit says where
  Modflared runs, as read from its bytecode: on Windows, on Linux on x86 and on macOS, where Apple
  Silicon needs Rosetta 2, but not on Linux on ARM. See [Mac mini](server/macos.md#public-access).
- Pack 3.0.7 was built at 21:54:18 (67,246,343 bytes, index in 465e625). Entry by entry it differs from
  3.0.6 only by the new server list and by the version and the Modflared entry in its index. It went up
  to the Modrinth draft at 21:55 as version `LRkF99Pz` with the same SHA-512 as the local build, and
  3.0.6 stays up. See [Publish](modpack/publish.md#version-history).
- At about 21:56 the body of the Modrinth draft got the section "Joining the server" (fa17708). It names
  the address, Modflared and where it runs, says that voice chat stays in the LAN and sends players to
  CurseForge for BattleTowers. The publish script replaced the summary and the body with `--texts`.
  Nobody has joined through Modflared yet.
- At 22:08:49 `hex.skytekx.com` got the address 192.0.2.1 next to its TXT record. Minecraft resolves a
  server name before Modflared sees it, and without an address the clients stopped at "Unknown host".
  The address is a documentation address that answers nothing. `tools/cf_tunnel.py` sets it as step 6
  since f17a50a. At 22:15:02 `play.skytekx.com` got the same address and the same TXT route as a second
  name, which the setup script does not create yet.
- At 22:34 LuckPerms-Bukkit 5.5.71 joined EssentialsX on the Mac mini, and the group `default` got 38
  player nodes of EssentialsX. See [Plugins](modpack/plugins.md#permissions).
- At about 22:44 the owner reported that nobody could break blocks or hit anyone and suspected the
  permissions. Neither came from LuckPerms. Hits between players failed inside Asgard Shield Reloaded,
  whose attack handler reads the arm a player last swung. That arm is null until the player swings after
  a login or a respawn, so from 22:37 on 37 hits threw `Invalid hand null` and did no damage. Breaking
  failed at the spawn. With one op on the server, the spawn protection of 16 blocks kept everyone else
  from breaking or placing blocks there, and every new player lands within 10 blocks of the spawn.
  `tools/patch_mods.py` now makes the handler read the hand in use (8286e93, see
  [Boot fixes](content/boot-fixes.md#jar-patches)), `spawn-protection` went to 0 in the Mac's
  `server.properties`, and the server restarted at 22:49:28 after a warning in chat. It was up again at
  22:50:09 with the 3 accepted boot errors.
- At 22:18 and 22:22 the owner reported that the islands no longer looked like in June and ruled that a
  fix must not change their size, shape or spacing. Commits 1c7692d, d3e3407 and 77a0259 of 22:59
  bring back the June terrain with a biome gate that follows it. On the Mac mini the Skylands were
  wiped at 23:01 and pregenerated with Chunky. At 23:17:24, at 80.43% of radius 3000, the server ran
  out of its 9 GB heap, and launchd restarted it at 23:17:31. See
  [Skylands worldgen](content/skylands-worldgen.md#june-terrain-returns).
- At 23:10 the owner reported things between the islands, especially flowers in the void, and asked
  why there is no `/spawn`. A scan at 23:13 found 54,386 blocks below Y 13 from villages, merchant
  camps and AE2 meteorites that start on an island and reach into the void. skytekx-compat 2.1.0
  (source commits 9636058 to db56da3) keeps them out, and four test worlds on the build machine
  ended with no block below Y 13. The biome gate went from 1.1 to 0.8 (07ff194). See
  [Skylands worldgen](content/skylands-worldgen.md#the-void-floor). For `/spawn`, EssentialsXSpawn
  2.22.0 was prepared, see [Plugins](modpack/plugins.md#spawn).
- At 23:51 the owner reported that the test client on the build machine showed an X as pointer and
  could not pick up items in the inventory. Every click acted as a shift click, which moves a stack
  between inventory and hotbar. The X server held no key down, so the client itself had missed the
  release of Shift. A client restart at 00:24 cleared it. The X was the default root cursor of
  XWayland, which the compositor never set. The client of that evening, with a 12 GB heap, held about
  9 GB after a join, needed about 3 GB more while EMI reloaded its recipes, and kept about 250 MB more
  after each of its 12 reconnects (counts of AllTheLeaks), so it came close to running out of memory
  after three hours.
- At 00:58 the server on the Mac mini restarted after a warning in chat, with skytekx-compat 2.1.0, the
  gate of 0.8 and EssentialsXSpawn. Its Skylands were wiped again (backup
  `~/skytekx3-backups/skylands-gate-1.1-2026-11-01-0058.tgz`) and pregenerated to radius 2000 from
  00:59 to 01:06. The scans of 01:11 found 426 structure starts, 93 of them floaters, and no block
  below Y 13. See [Skylands worldgen](content/skylands-worldgen.md#deploy-of-2026-11-01).
- Around 01:25 the owner reported that torches sometimes landed as a "Placed Block" of Placeable
  Items, shown as the missing texture, and at 01:34 ruled to take the mod out. The server stopped at
  01:38:36 after a warning in chat and was up at 01:39:29 without it, with the 3 accepted boot errors
  and three about the mod's entries that the world still mapped. The owner's client runs with the jar
  disabled since 01:42, and at the owner's request the jar was deleted on the second MacBook at
  01:45. See [Mod set](modpack/mod-set.md#history).
- skytekx-compat 2.1.1 (built at 01:47, source commits f6c0c41 to 630fca4) fixes three findings of a
  review of 2.1.0. A swamp hut on an island rim could drop its stilts into the void, the sky stone of a
  meteorite reaches 8 blocks below its box, and `/place structure` skipped every check. A fifth test
  world from 01:53 to 01:57 held the same 135 starts as the fourth and no block below Y 13. See
  [SkyTekx Compat](content/compat-mod.md#history).
- A review of 2.1.1 from 01:55 to 02:23 found two more gaps, and skytekx-compat 2.1.2 (source commits
  7a1a308 to 26a493d of 02:31) closes them. A meteorite counts from 9 blocks below its center, since
  AE2's decay pass copies its lowest sky stone one block down, and a swamp hut stilt over a column
  without island ground under it is left out. A sixth test world from 02:41 to 02:45 matched the
  fifth. A hut over carved stilt columns dropped 44 oak logs below Y 13 with 2.1.1 and none with
  2.1.2. The server repo bundles 2.1.2 since 593e227. 2.1.1 never left the test server.
- At 02:49:59 a warning went out in chat, with only one player online. The server stopped at 02:51:09.
  skytekx-compat 2.1.2 replaced 2.1.0, `ServerMac.sh` got the fixes of 2026-10-31 evening (d5761d3,
  39f7c05), and `macos/install.sh` ran again, which loaded the pf rules that only drop new connections
  to RCON. The server was up at 02:52:03 with the 3 accepted boot errors. From the LAN 25565 answered
  and 65535 timed out. See [Mac mini](server/macos.md).
- At 04:32:47 the owner burned to death with keepInventory off and lost 110 of 138 items, and none
  were left on the ground. On the owner's ask the `.dat_old` of the 04:32:04 autosave replaced the
  player file at 04:40:04 with nobody online. The two newer files are kept in `~/skytekx3-backups/` on
  the Mac mini. Minecraft keeps only the previous autosave of a player, so such a copy has to happen
  before the player joins again. At 05:00 the backup job ran for the first time (742 MB in 20 s).
- At 05:52 the owner ruled that Get It Together Drops leaves the pack and that Falling Meteors
  Reimagined 2.5 joins the server and both MacBooks, with meteors only in the overworld and none on the
  sky islands. skytekx-compat 2.1.3 holds that rule in a mixin and also answers the modifier queries of
  Infernal Mobs on the server thread, the source of the `Async chunk entity get` warnings. On the test
  server a thrown summoner called a meteor in the overworld and none in the Skylands, the Nether or the
  End, and the 61 queries of a joining client all ran on the server thread. The server stopped at
  06:10:10 with nobody online. Get It Together Drops 1.3 and skytekx-compat 2.1.2 moved to
  `~/skytekx3-backups/jars-orig/`, Falling Meteors and skytekx-compat 2.1.3 came in (235 jars), and
  `merge-radius.item` in `spigot.yml` went from 2.5 to 0.5, since Arclight would otherwise have kept
  merging dropped items at 2.5 (2af8bb0). The server was up at 06:11:04 at 20 TPS with only the known
  boot errors. Both MacBooks got the jar, and a game that was already running has to restart before it
  joins. See [SkyTekx Compat](content/compat-mod.md#history).
- At 06:23 the build machine's `mods/` got the same swap, because `macos/sync-to-mac.sh` mirrors that
  folder with `--delete` and would otherwise have brought the old jars back. Its 235 jars match the Mac
  mini's by SHA-256. The two old jars sit in `~/skytekx3-backups/jars-orig/` on the build machine.
- At 03:54 the owner asked for the simple backpack mod of SkyTekx2 instead of Sophisticated Backpacks.
  No build or remake for 1.20.1 exists, so Eydamos Backpack 2.0.1 (Minecraft 1.7.10, LGPL-2.1) was
  ported to Forge 1.20.1 in the local repo `skytekx-backpack`, with 20 GameTests and one review. On the
  test server with the owner's client the normal, big, workbench and ender backpacks opened, items
  stayed in a backpack after reopening it, the held backpack stayed locked in its slot, and the
  workbench backpack crafted four planks from a log in its storage. At 08:08:58 a backup ran
  (`world-2026-11-01-0808.tgz`, 708 MB). The server stopped at 08:09 with nobody online. Sophisticated
  Backpacks 3.26.19.2239 and Sophisticated Core 1.5.21.2444 moved to `~/skytekx3-backups/jars-orig/`
  together with a copy of `world/data/sophisticatedbackpacks.dat`, and `backpack-1.20.1-2.0.1.1.jar`
  came in (234 jars). That file held four backpacks, all worn by mobs, with jukebox upgrades and music
  discs, and no player had stored anything. The server was up at 08:09:40 at 20 TPS with the known
  boot errors and the expected unidentified mappings of the two removed mods. A player's first join
  logs the Sophisticated recipes it drops from the recipe book. The owner's client `SkyTekx3-test`
  and the second MacBook got the same swap, and the build machine's `mods/` matches the Mac mini by
  SHA-256. See [Mod set](modpack/mod-set.md#backpack).
- In the evening the Mac mini moved to a new local network and got a new LAN address. On the
  owner's ask Elementary Staffs came back as our Forge 1.20.1 port of Kroko's MIT mod
  (`elementary-staffs-1.20.1-2.0.0.1.jar`, public repo `skytekx/elementary-staffs`, 25 GameTests). After
  a test with the owner's client on the test server and a chat warning, a backup ran at 19:56
  (`world-2026-11-01-1956.tgz`, which pushed out the backup of 2026-10-31 19:34), and the server was up
  with the jar at 19:57:09 (235 jars). Both MacBooks got the jar plus the client only shader loader
  Oculus 1.8.0 with the shader MakeUp Ultra Fast 9.6. Complementary Reimagined failed on the laptop,
  since Mesa on Apple silicon offers only 16 texture units per stage. BattleTowers and Falling Meteors
  are now unchanged releases in the public repos `skytekx/battletowers` and
  `skytekx/falling-meteors-reimagined`, for the next pack to download.

## 2026-11-02: The Twilight Forest backport goes live

- On the owner's ask the cloud branch `claude/twilight-forest-port-mods-j2owbq` was renamed to
  `feat/twilight-forest` and merged into master (160438ff). The pack index kept the fixes of the
  unreleased 3.0.8 (every mod required, the Backpack port, the GitHub hosted mods) and moved to 3.1.0.
- The seed gate of the fork passed at 12:50: the 4.3 hook and the level held the same seed, so the
  Twilight Forest dimension was not reset.
- At 12:51 the Mac mini started on the fork jar `4.3.2508-skytekx.1` with its seven bundled addons, the
  nine new index mods and the `safeiter2` Arclight jar, 251 jars in all. The boot reached `Done` with the
  three known errors. Only the files the merge changed went over, not a full sync, because the Mac
  holds newer configs than the checkout.
- Both MacBooks got the same Twilight Forest jars and the two Twilight configs.
- At 13:07 the owner asked why a diamond pickaxe no longer mines obsidian. Nyx 1.0.0 registered its
  meteor tool tier between diamond and netherite with the tag `minecraft:needs_diamond_tool`, so Forge's
  tier sorting had taken every block that needs diamond away from diamond tools since Nyx came in on
  2026-06-29. At 13:12 the owner ruled that whatever diamond mines in vanilla stays mineable with it, and
  that new blocks may ask for a higher tier. Nyx 1.0.1 gives the meteor tier its own empty tag
  `nyx:needs_meteor_tool` and credits the port to SkyTekx alone. The server stopped at 13:21:46 with
  nobody online and reached `Done` with the new jar at 13:22:30, with 251 jars and the three known boot
  errors. Two temporary KubeJS scripts, loaded with `/kubejs reload server_scripts` and removed right
  after, found the diamond tier right for obsidian, crying obsidian, ancient debris, the respawn anchor
  and the netherite block, iron still too weak for obsidian, and all 1003 vanilla blocks mineable at the
  diamond tier. Only 10 blocks of Iron's Spellbooks, Mekanism Extras, Tinkers' Construct and Tinkers
  Reforged ask for more than diamond. Both MacBooks got the jar at 13:22. A client with the old jar
  still gets the drops but mines these blocks at the speed of a wrong tool. See
  [Nyx](content/nyx.md#history).
- At 13:11 the owner asked that it never rains in the Skylands. Commit e55c731a turned precipitation off
  in the 11 Skylands biomes that still had it, so none of the 15 has rain or snow. The server restarted
  with it at 13:13 and reached `Done` at 13:13:48. See
  [Skylands worldgen](content/skylands-worldgen.md#biomes).
- A fall at 13:28 and several deaths at a Skylands bed between 13:34 and 13:48 cost the owner the
  inventory. On the owner's ask it came back at 13:43 and again at 13:52 from the playerdata snapshot of
  13:32. The file was rewritten while the player was kicked, with newer items kept and the duplicates
  picked up from the death drops left out. A death in lava at 14:18:45 burned it again, and at 14:20 it
  came back the same way from the save of 14:17:31, 21 stacks and XP level 2. A second death in lava,
  in the overworld at 14:37:10, followed. The player stayed on the death screen, and a respawn empties
  the inventory of a dead player's file, so the restore waited for the respawn at 14:39:18. At 14:39:20
  the save of 14:32:31 came back, 27 stacks with the XP level 4 the player had at death. A third death
  in lava, at 14:46:40 while fleeing Silverfish, took the obsidian the owner had farmed since. At the
  owner's ask of 14:47 the save of 14:42:31 came back at 14:50, 31 stacks with XP level 4, and its one
  obsidian became 12, the count the player statistics show as picked up and never placed. At 17:11:33
  a Crimson Mosquito killed the player, and a death in lava followed at 17:13:00. On the owner's ask of
  17:13 the save of 17:07:34 came back at 17:15:02 after the respawn, 33 stacks with XP level 4, and the
  rejoin at 17:15:09 showed them. At 17:45:19 a second player died in lava. On the owner's ask of
  17:46 that player's save of 17:43:20 came back at 17:47:46, after the respawn, with 38 stacks and XP
  level 17, and the rejoin at 17:49:35 showed the level. Between 19:12:38 and 19:13:25 the owner died
  four times in the overworld, to an explosion, twice to a Skeleton and to fire. On the owner's ask of
  19:14 the save of 19:08:22 came back at 19:15:46 while the player was kicked, 31 stacks with XP level
  2, and the rejoin at 19:17:43 showed them. Drops in unloaded chunks do not age, so at 19:19 a
  force-load of the death area looked for the rest. Only 4 raw uranium lay there, and the loaded chunk
  let them despawn by 19:24. At 19:25:41 commands gave back the 4 raw uranium and the ores the save of
  19:13:22 listed, and the owner had them all in the backpack by 19:28:22. No save, no drop and not the
  backpack held gold. Five more deaths at 19:21 and 19:22 cost nothing. The save of 19:23:22 holds every
  item again.
- At 13:39 the owner asked for weaker mobs in the Skylands, where everybody builds a home, and at 13:48
  for none at all. With the Skylands at noon but never at day, undead no longer burned there, and
  L2Hostility gave the mobs levels near 50. `kubejs/server_scripts/skylands_no_mobs.js` refuses every
  hostile mob that joins the Skylands and removed 70 when it first loaded at 13:51. At 13:51 the owner
  added that levels 1 to 5 would be fine, and at 14:16 chose weak mobs over none. Since 14:26 an
  L2Hostility difficulty file gives the Skylands base 0, variation 1 and no share of the player level,
  so mobs near the homes spawn at levels 0 to 3, and the no mobs script is gone. Since 14:28 Infernal
  Mobs elites are blacklisted there, and the two that had spawned in between went into the void.
  One-shot KubeJS scripts applied both to the running server, without a restart or `/reload`. See
  [Skylands worldgen](content/skylands-worldgen.md#sleep-and-hostile-mobs).
- At 14:17 the owner asked why it now gets night in the Skylands too. The dimension type on the server
  still had `fixed_time` 6000 and the overworld stood at noon, but it rained there, which greys and
  darkens the Skylands sky as well. What the client showed was not checked.
- At 13:31 and 13:45 the owner reported that sleeping in the Skylands does not make it day in the
  overworld. The fixed time of the dimension lets beds work there at any hour, but vanilla then sets the
  time of the Skylands only, which follows the overworld, and Arclight replaces the Forge sleeping time
  check when a player lies down. `kubejs/server_scripts/skylands_sleep.js` moves the overworld to the
  next morning once enough players in the Skylands sleep. At 14:03 sleeping still failed, because a
  second player was awake in the Skylands and `playersSleepingPercentage` stood at the default of 100.
  At 14:10 the rule went to 0, so one sleeping player is enough on the whole server. Nobody slept in the
  next two nights. At 15:11:03 one of two players in the Skylands slept, and six seconds later the
  overworld stood at time 126, early morning.
- At 14:54 the owner asked why a nether portal in the Skylands does not open with flint and steel. The
  frame was complete, but vanilla and Arclight light portals only in the overworld and the nether, and a
  portal in the nether always leads to the overworld. On the owner's ask of 14:59
  `kubejs/server_scripts/skylands_portals.js` went live at 15:05 (1d8a7396). It opens frames in the
  Skylands and sends a player who came from the Skylands back there from the nether. A portal opened in
  the Skylands at 15:06:26, and at 15:07:55 the owner went back from the nether to the Skylands. See
  [Skylands worldgen](content/skylands-worldgen.md#nether-portals).
- At 16:02 the owner asked why their copper was the wrong one for brass plates, at 16:14 whether bronze
  and brass are the same, and at 16:17 asked for one item per material that builds everything. A probe
  of all 36,990 recipes against the 138 groups of AlmostUnified found that every readable recipe takes
  every variant, apart from the yellow dye recipes and yellorium, which AlmostUnified counted as
  uranium. At 17:09 the owner approved an inventory converter, the yellorium fix and one ore per metal
  in new chunks. `kubejs/server_scripts/unify_inventory.js` swaps other variants for the preferred item
  since 17:16:53 (8f46a335). The server restarted at 17:20 with Extreme Reactors out of AlmostUnified
  (6a73d7be) and 16 duplicate ore features of Railcraft and Immersive Engineering off (7ad5fffd), and a
  datapack reload at 17:23 took yellorium out of the uranium tags (f8308011). The stop at 17:20 wrote
  Mekanism's ore switch back to `true`, so a second restart at 17:32 brought it in with the server down
  (7c7ef1cd). See [AlmostUnified](modpack/almostunified.md#one-item-per-material).
- At 18:23 the owner reported creepers of level 34 on the sky islands again. The Skylands difficulty file
  of 14:26 had never loaded, because L2Library skips config files in a namespace that is no loaded mod
  and does not start with `_`, and the check at 14:27 had only seen the one-shot script. The restart of
  17:20 dropped those values. Since 18:28 the file sits under `data/_skytekx3/`, a datapack reload
  loaded it, and 37 hostile mobs of level 6 to 59 left the Skylands. See
  [Skylands worldgen](content/skylands-worldgen.md#the-namespace-trap-of-l2hostility).
- At 19:14 the owner asked for the grave mod that every SkyTekx had so far. SkyTekx 2 made its graves
  with OpenBlocks, which has no 1.20.1 build, and had tried the GraveStone Mod of Max Henkel in January
  2023. Commit 1ce3dfc2 put its Forge 1.20.1 build 1.0.35 from Modrinth into the index, required on
  both sides. The jar went to the Mac mini and to both MacBooks at 19:33. After a warning in the game
  the server restarted at 19:35:00 and reached `Done` at 19:35:42 with the three known errors and a
  default `gravestone-server.toml`. A join at 19:37:37 from a game started before the jar arrived was
  refused for the missing grave registry entries, so each player restarts the game once. See
  [graves](content/graves.md).

## 2026-11-03: Items stay pickable and EMI bridges JEI 15.74

- At 10:54 the owner reported dropped items that showed on the ground but could not be picked up. On
  the server the item hung in the air where the block under it had been, because Arclight's activation
  range skips exactly the tick in which about one item in four checks its footing. See
  [Floating items](server/arclight.md#floating-items).
- At 11:05 the owner asked for the fix. Commit 00a97cfd sets `entity-activation-range.misc: 0` in
  `spigot.yml`. After a warning in the game at 11:12:04 the server restarted at 11:13:06 and reached
  `Done` at 11:13:48, with `Mi 0` in its activation range line.
- At 09:54 the owner asked to fork EMI, fix all of its errors and push the fork to the skytekx org.
  EMI 1.1.24 builds its JEI bridge against JEI 15.20. Against JEI 15.74 the methods it lacks threw
  `AbstractMethodError`, 21,934 times in one client session of that morning: the Chipped recipes did
  not load, the Tinkers' Construct pages showed "Error rendering" and the recipe fill failed for several
  mods. The fork github.com/skytekx/emi, branch `1.20.1-jei-bridge`, builds the bridge against JEI 15.74
  in eight commits up to e4ae7350 and draws the recipe extras these mods add. A static scan of all pack
  jars found no remaining call into a missing JEI method. Since about 11:08 the branch is the fork's
  default branch.
- At 11:05 the owner also asked to install it. Commit eadc7a6d moves EMI from the index to
  `local-mods/`. The jar went into the Mac mini's `mods/` right before the restart of 11:13, into the
  build machine's `mods/` that `sync-to-mac.sh` mirrors, and into both MacBooks while their games were
  closed at 11:13. Both games started again at 11:14. In the owner's game the JEI bridge loaded in 6.6 s
  without a single `AbstractMethodError`. Three entity melting recipes of Tinkers' Construct still
  failed, because the bridge passed the null entries that JEI allows in an ingredient list on to EMI.
  The fork fixes that in 1.1.24-skytekx.2 (3fd75e08), which is not deployed yet.

## 2026-11-03: Every kind of planks makes chests, ladders and the other wooden items

- At 13:15 the owner asked why maple planks make no chest and ruled that all wood should make chests,
  doors and so on, with the default item where a wood has no own one.
- Quark's Variant Chests and Variant Ladders switch off the vanilla chest, trapped chest and ladder
  recipes, and Quark's mixed chest recipe only takes planks of at least two kinds. Eight planks of a
  wood without a Quark variant, like every Biomes O' Plenty wood, matched nothing, and only 17 kinds of
  planks could make ladders.
- Commit 0f7fe7d4 adds 12 fallback recipes and 12 skip tags under `kubejs/data/skytekx3/`, see
  [KubeJS](modpack/kubejs.md#wood-fallbacks). Each recipe takes `#minecraft:planks` minus the planks
  that already make their own version and gives the default item: chest, ladder and the oak door,
  trapdoor, fence, fence gate, sign, slab, stairs, pressure plate, button and boat.
- Live since a datapack reload at 13:32 with both players online, without a restart. A live check
  found all 12 recipes with the expected planks. Both MacBooks rebuilt EMI in about 38 s with 289,201
  recipes and no errors.

Related: [OKF index](index.md), [History](history.md), [Decisions](decisions.md),
[Open items](open-items.md).
