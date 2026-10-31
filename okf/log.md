---
type: Log
title: Change log
description: Chronological change log of SkyTekx 3 from the audit of SkyTekx 2 on 2026-06-26 to the move to the Mac mini and the Halloween world of 2026-10-31.
tags: [log, history]
timestamp: 2026-10-31
---

# Change log

Oldest first. Times are the build machine's local time, CEST up to 2026-07-13 and CET on 2026-10-31.
The build sessions themselves were deleted, so this log rests on the owner's prompts in
`~/.claude/history.jsonl` (session 5bd3e1de, and 3dcdf872 for the hosting question of 2026-06-26
23:09), the git history, the server logs and crash reports, the `.mrpack` files and file times.
Rulings are collected on [Decisions](decisions.md).

The git history before 2026-06-29 19:28 was rebuilt on the owner's request. The first 15 commits of
master, 5ae7f82 to e94ed6c, all carry 2026-06-29 18:52:20, so their dates say nothing about when the work
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
  that is required on both sides (commit c4369dc).
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
  Rhino fix of 19:28 (20bc022) did not help. Commit bf898e6 (20:10) states that the script threw a
  `TypeError` every tick and never teleported anyone, and it replaced the script with a Java handler in
  the skytekx-worldgen mod.
- At 20:10 chocobos became easier to find (spawn weight 10 to 20, c2d3c5a) and the Skylands got six
  Dungeons Arise floaters at spacing 44 (63ec22f).
- From 19:35 an RCON sweep of `/locate` over every modded structure, looking for the structure at
  overworld x 0, y 110, z 451 that the owner wanted in the Skylands, stalled the server thread. The
  watchdog fired at 19:41:18. During the forced shutdown Arclight's DistanceManager threw
  `NoSuchElementException` in the tick loop (crash reports 19:41:20 and 19:41:29).
- After the owner asked at 21:22 to fix the underlying issue and at 21:32 to harden Arclight with
  backports, a patched Arclight 1.0.6 was built at 21:50 and ran from 21:55. Commit 88552a9 (22:11) ships
  `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar`, the symlink `arclight.jar` and the patch in
  `patches/`, see [Arclight runtime](server/arclight.md#the-hardened-jar).
- At 22:14 the owner ruled that commit messages carry no dashes and no semicolons and asked to clean
  the whole history. The messages were rewritten with `git filter-branch` at 22:45 and force-pushed at
  22:51. Two spaced hyphens used as dashes survived, in 5ae7f82 and 573cc25.
- At 22:45 the hot air balloon of the Ruins mod and seven more structures were floated in the Skylands
  (8afbfe1).
- At 22:30 the owner asked for the performance report in the OKF and not in `docs/`.
- Between 23:00 and 23:41 the owner looked at Sponge as a better maintained server. Sponge has no
  build for Forge 1.20.1, so the server stayed on the hardened Arclight.

## 2026-06-30 night: Profiles and structure density

- Spark profiled live chunk generation at 23:49 and 23:57 on 06-29. Commit a7cfd8e (00:01) records the
  result on [Performance](server/performance.md). Vanilla terrain code takes 49.6 % of the samples in the
  Overworld and 61.7 % in the Skylands, the JVM itself 23.8 % and 17.9 %. The costliest mod is Starlight
  with 0.85 % and 3.0 %, itself a performance mod. No content mod is worth cutting, a wider pregen is.
- At 00:23 the owner judged that the Ruins mod adds little and weighs a lot at chunk loading. It left
  the pack at 01:13 (ac261f1), whose message also notes that its writer thread threw errors every few
  minutes. Eight more structures were floated in its place.
- At 01:20 the owner set a standing rule: decisions that improve speed and reliability win.
- At 01:34 a density audit (a0edacd) found the Skylands crowded, about one structure per 210 blocks
  from four overlapping layers. Six raw Dungeons Arise structures duplicated a floater, and two heavy
  Cataclysm builds generated in the sky. Cataclysm's own code costs nothing measurable, but a saturated
  region generated at 79 chunks per second instead of 115 to 124, with almost three times the garbage
  collection.
- Commit b07466b (03:37) took the lightest cut. The Skylands biome tags of the six duplicates and
  of the acropolis and the soul black smith were deleted, 16 files in both datapack copies. After a
  restart none of the eight was found in the Skylands and all still generated in their own dimensions,
  as the owner then ruled at 03:48.
- Between 03:55 and 04:01 the owner asked for common chocobos, common friendly structures and chocobos
  only in the Skylands. Commit 801edb4 (04:18) set the floaters from spacing 44 to 32, the island ground
  set from 18 to 14 and villages from 34 to 24. Commit daba422 added chocobos with weight 30 to the 14
  island biomes, set ChocoCraft's Overworld, mountain and Nether spawns to `forge:none` and moved
  gysahl greens to the islands. These overrides turned out to lose against the mod's own data in this
  world, see [Skylands worldgen](content/skylands-worldgen.md#known-defect-the-chococraft-overrides-lose).
- A keep stood at Y 0, so the Dungeons Arise major and minor sets also moved to `surface_spread` with
  `min_surface_y` 36 (e375d22).
- At 04:24 the owner asked that climbing high enough in the Overworld puts the player on a dirt block at
  y=0 in the Skylands, as the main way to switch, instead of the `/trigger` command. Commit 2eceeb1
  (04:37) built it, see [Skylands travel](content/aether-fall.md).

## 2026-06-30 morning: From 152 boot errors to 3

At 04:15 the owner asked to resolve every error and warning at server boot. The work is on
[Boot fixes](content/boot-fixes.md).

- Commit d5e1449 (05:26) added the override datapack `skytekx3_fixes`. It replaces 14 broken
  advancements and 99 broken loot tables of other mods with valid empty versions. It also fixed the
  inverted power range of the Apotheosis enchantment `dimdoors:string_theory`, turned off Collective's
  update check and deleted a stray loot table from the Ad Astra jar that crashed Bird's Nests. Boot
  ERRORs fell from 152 to 27. The lesson: a world datapack in the middle of the pack order loses to the
  mod packs loaded after it, so overrides need a pack that loads after them.
- At 05:49 the owner ruled that before a feature is made optional, the mod that provides it gets
  installed. Commit d14ea2b (06:21) installed SlashBlade Resharped, Re:Avaritia and Medieval Embroidery,
  added the Bird's Nests guard mixin to the skytekx-worldgen mod and started `tools/patch_mods.py` for
  fixes inside mod jars. Boot ERRORs fell to 10.
- Commit 7cea753 (07:05) installed The One Probe and Aether Genesis, built from its GitHub branch
  `1.20.1-develop`, and made the TiCEX gem and gun tools load. Three accepted ERRORs remained: the
  Simply Jetpacks guide book, an AllTheLeaks reflection into Citadel, and EssentialsX's warning about the
  hybrid server. WARN lines stayed at about 300.
- At 08:05 the owner had Re:Avaritia removed again, because TiCEX 0.6.0 cannot use it (bd5f18b, 08:11).
- The run started at 08:09 became the longest on record and lasted until 2026-07-03.

## 2026-06-30 afternoon: The client and Modrinth

- At 13:54 the owner's client was refused at the Forge handshake because it lacked the mods that went
  onto the server that morning ("Missing required datapack registry: slashblade:named_blades"). At 13:55
  the owner wrote: "you did not fix the Minecraft client at all, you only fixed the errors in the server
  it seems." The client was updated by hand later and joined again on 2026-07-01 at 19:56.
- At 18:14 the website got its last commit, ef50cc2.
- At 18:16 the owner noted that the Modrinth project could not go public because of its permissions
  page, and at 18:20 asked for a folder with the needed files. Commit 62ada38 (18:39) added
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
- Commits 174fbf6, 4da4ee3 and dc8b470 (22:31 to 22:32) replaced the mod placement with vanilla
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
  committed at 23:43 and taken out of git again at 23:49, and 940f07e (23:59) set the index version to
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
- 55 Modrinth updates, the new dependency MezzConfig and three bundled jars (092140d, 4917ab8), more
  blood moons and Nyx events in the Skylands (a00ecce), the Halloween server icon (f92aee3), pumpkin
  patches on the islands (5ec6b72) and the first Skylands fix (1f8f4ef, Battle Towers, strongholds and
  the illager campsite) were committed at 18:14 and 18:15. See
  [Mod set](modpack/mod-set.md#update-of-2026-10-31).
- The Halloween world was created on the Mac mini at 18:07:17. Chunky pregenerated the Skylands to
  radius 3000 from 18:13 to 18:32 and the Overworld to radius 1500 from 18:32 to 18:40.
- The region scans of that run found 307 petrified root blocks and two windmill field blocks below the
  islands. Commits 0cf350a and 7b51eb5 fixed both at 18:52. The server stopped over RCON at 18:53:14,
  the Skylands were wiped, and the first start under launchd at 18:54:57 generated them again from
  18:55 to 19:15. See [Skylands worldgen](content/skylands-worldgen.md#changes-of-2026-10-31).
- At 18:57:56 a vanilla chunk save race logged a `ConcurrentModificationException` in `IOWorker`
  during that pregen. The chunk is complete in the region file, see
  [Logs and crashes](server/logs-and-crashes.md#chunk-save-race-during-pregen).
- Pack 3.0.6 got its version at 18:57 (3860591) and its publish script at 19:03 (5b30934). At 19:09 the
  builder was fixed to pack tracked files only (e641c18) and the pack was built. That build was never
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
  20:20 the dead Nyx download left the index (74faf22), AllTheLeaks and the Cataclysm Spellbooks build
  joined `local-mods/` (507a8f2), the permission files were brought up to date (f4c2093), and the
  builder started to read git HEAD (ce31f6f), to ask for Forge 47.4.18 (a36207c), to use the website's
  tagline (b3ec68c) and to ship the fixes datapack (2883e32). The publish script reads the token from
  the environment since 8e11bbc.
- Pack 3.0.6 was built again at 20:19:59 (67,245,989 bytes, index in 192084f) and went up to the
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

Related: [OKF index](index.md), [History](history.md), [Decisions](decisions.md),
[Open items](open-items.md).
