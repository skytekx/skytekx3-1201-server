---
type: Reference
title: Mod set
description: What SkyTekx 3 is made of, how the Modrinth index, the bundled jars and the server's mods folder relate, which jars sit outside the pack, and how to change the set.
tags: [modpack, mods, modrinth]
timestamp: 2026-11-05
---

# Mod set

SkyTekx 3 is a Forge 1.20.1 pack. Its mods reach players and the server in two ways: as entries of
the Modrinth index, which launchers download, and as bundled jars that ride inside the `.mrpack`. The
server runs whatever is in its `mods/` folder, which is neither of the two exactly.

| Item | Value | Evidence |
|---|---|---|
| Pack version | 3.1.3 since f34d7362, built privately and not on Modrinth, after 3.1.2 (551170bb), 3.1.1 (ccc9289e), 3.1.0 (aaf6a5e9) and 3.0.7 | Field `versionId` in `modrinth.index.json`. The build wrote 3.0.7 in 15430ce and 3.0.6 in 3860591 |
| Minecraft | 1.20.1 | Index `dependencies` |
| Forge in the index | 47.4.18 since a36207c, 47.4.6 before | Index `dependencies`, `FORGE` in `tools/build_mrpack.py` |
| Forge at runtime | 47.4.18, on the server (Arclight) and in the owner's client | Server crash reports, Prism `mmc-pack.json` |
| Index entries | 232 since 2026-11-04 (b36bc030 to 3b98076f), which put Advanced Loot Info and its library Advanced Core Info in the place of Just Enough Resources, see [Advanced Loot Info](#advanced-loot-info). 228 of them come from `cdn.modrinth.com` and 4 from GitHub. Before them, 231 since c482e040, after 394330db to c482e040 added five addons of Applied Energistics 2, Cyclic and FLib, see [additions](#additions-of-2026-11-04), 224 since d1cabf85, which added Just Enough Resources as a client only entry, and 223 since 56912155, which moved Simply Jetpacks 2 to `local-mods/`, and 224 since 551170bb, which removed Elenai Dodge 2. Before them, 225 since 1ea5bb29, which moved EMI to `local-mods/`, 222 of them from `cdn.modrinth.com` and 3 from GitHub. The index held 226 since 4eca94b1, which added the GraveStone Mod. The Twilight Forest merge 3bdc98d6 left 225. Before it, 215 since 3afc3bf, all from `cdn.modrinth.com`. Commit ca131c3 added Modflared, a client only mod for the Cloudflare Tunnel, for 217, 787cb1f took Placeable Items out for 216 and 3afc3bf Get It Together Drops for 215. Between 74faf22 and ca131c3 there were 216. From 092140d to 74faf22 there were 217 as well, the extra one Nyx from a GitHub release that does not exist | File `modrinth.index.json` |
| Size of the index downloads | 1,018,751,213 bytes since 3b98076f, 1,019,027,444 from 4e3c25b0 with both Advanced Loot Info and Just Enough Resources, 1,018,349,694 from b36bc030, 1,018,089,782 from c482e040, 1,007,463,556 from d1cabf85 with Just Enough Resources, 1,007,187,325 from 56912155, 1,007,808,005 from 551170bb, 1,008,110,498 from 1ea5bb29, 1,009,232,248 from 4eca94b1, 1,008,915,067 from 3bdc98d6, 952,346,929 from 3afc3bf, 952,354,792 from 787cb1f, 952,411,152 from ca131c3, 952,341,086 from 74faf22, 952,759,925 with Nyx, 933,046,213 before commit 092140d | Sum of `fileSize` |
| Bundled jars | 49 in `local-mods/` since 2026-11-05 (a50c6748, SkyTekx Molten), 48 since 19840824 (Flux Networks), 47 since 2026-11-04 (f315fa3d and ee238bea), 45 since 56912155, 44 since 1ea5bb29, 43 before it, 35 since 507a8f2, 33 before | Tracked in git |
| Jars the server loads | 264 in the Mac mini's `mods/` since the deploy of 3.1.3 on 2026-11-05 at 00:46, 251 before it, 252 since 2026-11-02 19:35, 236 in `mods/` after the update of 2026-10-31, 235 before | Counted 2026-10-31 and 2026-11-02 |

## Update of 2026-10-31

At 16:58 on 2026-10-31 the owner asked whether the installed mods have updates
(`~/.claude/history.jsonl`, session c0e872d8). The script `tools/update_mods.py` appeared at 17:50 and
ran with `--apply` and `--add-deps` (see [tools](tools.md#the-new-update-script)), and its index change
was committed at 18:14 as 092140d. The run went as follows:

- From 17:50:55 to 17:52:54 it swapped 55 jars in `mods/` and `client-only-mods/` for newer builds, and
  at 17:53:01 it rewrote the index. Among them are Botania 453 to 456, Tinkers' Construct 3.11.2.166 to
  3.12.1.231, Iron's Spellbooks 3.16.1 to 3.16.3 with Iron's Lib 1.1.0 to 2.3.0, JEI 15.20.0.132 to
  15.74.0.233, Polymorph 0.49.10 to 0.49.11, Simple Voice Chat 2.6.20 to 2.6.27 and Chocolate Quest
  1.1.0 to 1.3.1.
- It added mezz_config 0.6.10, optional on both sides, because JEI 15.74 requires it (line 55 of the JEI
  jar's `mods.toml`).
- It took beta builds as well. Modrinth lists 12 of the 56 new builds as beta, among them Extra
  Utilities Reborn Again 1.2.0-rc10, JEI, Polymorph, Tinkers' Construct and Simple Voice Chat (API
  check, 2026-10-31). The owner asked for the most stable configuration on 2026-06-27 10:34.
- Cataclysm stayed at 3.31, which the Cataclysm Spellbooks fork build needs. That build accepts Iron's
  Spellbooks 3.15.0 and later, so 3.16.3 fits.
- The jar patches ran again from 17:53:03 to 17:53:06. The nine patched index jars differ from their
  index SHA-1 again, as expected.

At 18:06 three bundled jars followed in `local-mods/` and `mods/`: Connectivity 7.6 to 7.7, Cupboard 3.7
to 4.2 and Enchanting Plus 1.2.0 to 1.3.0, with their files in `modrinth-permissions/` renamed (commit
4917ab8). Their license status did not change.

This server folder on the build machine has not booted with the new jars (its newest log ends on
2026-07-13). The Mac mini runs them since the first boot of the Halloween world at 18:07, and all six
of its boots that day kept the baseline of 3 ERRORs, see
[logs and crashes](../server/logs-and-crashes.md#boot-error-baseline). Those logs stay on the Mac. The
owner's client still holds the old builds (see [client](client.md#mods)).

## Additions of 2026-11-04

At 19:31 on 2026-11-04 the owner asked for Mob Grinding Utils and EnderIO Legacy Machines, a little
later for five addons of Applied Energistics 2, and at 19:41 for Cyclic from its GitHub repository.

| Mod | Version | Source |
|---|---|---|
| ExtendedAE | 1.4.21 | GitHub release of GlodBlock/ExtendedAE, because the mod is not on Modrinth |
| MEGA Cells | 2.4.6 | Modrinth |
| AE Infinity Booster | 2.1.0 | Modrinth |
| Applied Mekanistics | 1.4.3 | Modrinth |
| AdvancedAE | 1.3.6 | Modrinth |
| Cyclic | 1.13.7 | Modrinth, uploaded by Lothrazar, built from the branch `trunk/1.20.1` of Lothrazar/Cyclic |
| FLib | 0.0.16 | Modrinth, the library that Cyclic needs in 0.0.15 or newer |
| EnderIO Legacy Machines | 0.4.9 | CurseForge only, bundled in `local-mods/` |
| Mob Grinding Utils | 1.1.0 | CurseForge only, bundled in `local-mods/` |

- All nine are required on both sides. They reached the server with the deploy of 3.1.3 on
  2026-11-05, together with pack 3.1.3, because the clients need them too.
- The GitHub releases of Cyclic end at 1.12.2 from 2023. The branch `trunk/1.20.1` carries 1.13.7 and
  since then only a typo fix, so the pack takes the Modrinth file of the same author, as the owner's
  ruling of 2026-06-27 puts Modrinth first.
- A test server with the repo's mods and the new jars booted four times on 2026-11-04, the last two
  with Cyclic and FLib as well. The first boot stopped in Quark, an `ArrayIndexOutOfBoundsException` in the event bus of its library Zeta
  (`ForgeZetaEventBus.subscribeMethod`), which none of the new mods touches. The other three passed.
  16 blocks of the AE addons, EnderIO Legacy Machines and Mob Grinding Utils and 37 machines of Cyclic
  ran for half a minute without an error, and the items of AE Infinity Booster and Applied Mekanistics
  exist.
- The new mods log seven ERRORs on a boot. The ProjectE loot tables of ExtendedAE and MEGA Cells and
  four blood slime recipes of Cyclic now have overrides in the fixes pack, which work only once that
  pack loads after the new mods, see [boot fixes](../content/boot-fixes.md#load-order). AdvancedAE lists
  its client mixin for `ModelBakery` for both sides, so a dedicated server logs one harmless ERROR for
  it.
- Cyclic puts its copper nugget into `forge:nuggets/copper`, the tag through which AlmostUnified
  unifies copper nuggets to Thermal's.
  Its four flowers generate in new chunks of many overworld biomes, each kind in one of two chunks of
  its biomes (`rarity_filter` 2).
- Mob Grinding Utils is All Rights Reserved, so like six other bundled jars it blocks a public pack,
  see [bundled jars](#bundled-jars) and [publish](publish.md).

### Advanced Loot Info

At 20:48 the owner asked for loot tabs with the loot of the server, not the defaults of the mods.
Advanced Loot Info 2.5.0 and its library Advanced Core Info 1.5.0 took the place of Just Enough
Resources (b36bc030 to 3b98076f). Both are required on both sides and under the MIT license, and every
build of them for 1.20.1 is a beta.

- At the start of the server and after every `/reload` the mod reads the loot tables from the
  server's loot manager (`AbstractServer.readLootTables`) and applies the global loot modifiers that it
  knows. When EMI loads on a client, the client asks the server for that loot and gets it in chunks
  over the mod's own network channel.
- On this server the loot differs from the defaults of the mods mainly by the loot table overrides of
  the fixes pack. Loot from other datapacks and LootJS rules would show the same way, but the Skylands
  packs carry no loot tables, and the pack has no LootJS rules. The `loot_tiers.js` that
  `kubejs/server_scripts/gameplay.js` plans was never written.
- On a test boot on 2026-11-04 it read 6,623 loot tables, 16 fake loot tables and 34 trades into
  29,403 pages in 3.7 seconds on the server thread, right after the start. Each `/reload` runs the
  same reading again. It logged no error.
- It has no handler for 72 kinds of global loot modifiers of other mods, among them Apotheosis, Blood
  Magic, Farmer's Delight, the Aether mods and the Twilight Forest. Drops that only such a modifier
  adds do not show in its tabs.
- Its EMI plugin is native, so the JEI bridge of EMI skips its JEI categories (namespace `ali`) and
  each tab shows once.
- The loot that the server sends to each client comes to 27 chunks of together 874,514 bytes, the
  biggest 32,768 bytes, counted on the test server on 2026-11-04 from the chunks that the mod holds.
  How much longer that makes a join over the tunnel is not measured, see
  [open items](../open-items.md#not-verified).

### Flux Networks

At 21:45 the owner asked for Flux Networks from its GitHub repository, SonarSonic/Flux-Networks. Version
7.2.1.15 of 2024-04-04 is the newest build for 1.20.1, and the branch `1.20` of the repository carries
the same version. The repository has no releases, and the only Flux Networks on Modrinth is an unrelated
Fabric port. So the pack bundles the CurseForge file (project 248020, file 5234697, uploaded by
BloCamLimb, one of the two authors) in `local-mods/`. Its `mods.toml` declares MIT like the repository,
and the jar has no license text, so the LICENSE of the branch `1.20` travels in `licenses/`.

- It is required on both sides and needs only Forge. Modern UI and JEI are optional, and the jar has
  no mixins. It runs on the server since the deploy of 3.1.3 on 2026-11-05, like the
  [nine mods](#additions-of-2026-11-04) above.
- Flux Dust comes from redstone that lies on obsidian, when a player left-clicks the obsidian and the
  block below it is bedrock or a Flux Block.
- Its server config `world/serverconfig/fluxnetworks-server.toml` keeps the defaults. The Flux Dust
  recipe is on, every flux device may keep its chunk loaded once a player turns that on, and Super Admin
  needs permission level 1, which only the ops have here.
- A test server with the other new mods of 3.1.3 booted with it on 2026-11-04 at 22:04. It logged no
  ERROR of its own, only 19 warnings that its 7 blocks, 11 items and 1 sound did not get the registry
  ids they asked for, which Forge prints when a world saved without the mod loads it for the first time.
  The Flux Plug, Point and Controller, the three storages and the Flux Block, placed with `setblock`,
  ran for half a minute without an error. Nobody made Flux Dust or sent energy through a network, which
  needs a player.

## Owner rulings

From `~/.claude/history.jsonl`, session 5bd3e1de:

- 2026-06-27 10:34: the pack should feel as much like SkyTekx as possible, run on the most stable
  configuration, keep the old mods and add only a few new ones, and recipes must never conflict.
- 2026-06-27 17:29, repeated 2026-06-29 12:26, 14:11 and 15:11: take mods from Modrinth first, then
  from GitHub, and from CurseForge only as a last resort. Players must not need CurseForge.
- 2026-06-26 17:58: Tinkers' Construct is essential, and Silent Gear is no substitute.
- 2026-06-28 23:15: chocobos are a core mod. 2026-06-29 00:33: no Chance Cubes.
- 2026-06-29 12:05 and 12:26: search hard for successors of old mods ("I love oreberries").
- 2026-06-30 01:20: decisions that improve speed and reliability win.
- 2026-06-30 05:49: before making a feature optional, check whether another mod provides it. Every mod
  that is needed may be installed.

## Where the jars live

| Place | In git | Content |
|---|---|---|
| File [modrinth.index.json](../../modrinth.index.json) | Yes | The index entries with path, SHA-1, SHA-512, size, download URL and env per side |
| Folder [local-mods/](../../local-mods) | Yes | 49 jars that are not on Modrinth or were built from source. They ship as `overrides/mods/` |
| Folder `mods/` | No (`.gitignore:33`) | What the server loads: every index jar with a server side except Nostalgic Tweaks (206 on 2026-11-04), the `local-mods/` jars and BrassAmber BattleTowers, which exists nowhere else in the repo. The nine mods of 2026-11-04, Advanced Loot Info with its library, Flux Networks and SkyTekx Molten joined them with the deploy of 3.1.3 on 2026-11-05. Also `ad_astra-forge-1.20.1-1.15.20.jar.bak`, the unpatched original, which Forge ignores |
| Folder `client-only-mods/` | No (`.gitignore:34`) | 17 jars kept off the server: the 16 index entries with server env `unsupported`, Modflared among them since ca131c3, and Nostalgic Tweaks, moved by hand. Just Enough Resources was one of them from d1cabf85 to 3b98076f |
| Folder `disabled-mods/` | No (`.gitignore:56`) | 8 parked jars, listed below |

Forge only loads `mods/`. The `local-mods/` jars reach the server because they were copied into
`mods/`. Up to 74faf22 Nyx was both an index entry and a bundled jar. All copies are byte identical,
except Traverse, BiblioCraft and Simply Jetpacks 2, which `tools/patch_mods.py` patches in `mods/`.
Eleven index jars are patched as well, so their SHA-1 no longer matches the index (checked
2026-11-04). Players get the unpatched jars, except Simply Jetpacks 2, which the pack ships patched as
`SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`. See
[boot fixes](../content/boot-fixes.md).

The comment at `.gitignore:32` says that `mods/` is reproducible from the index and fetched on deploy.
No tool does that today, see [tools](tools.md).

## Sides

Each index entry says whether the client and the server need it (counted on 2026-11-04 in the index
for the next pack).

| Client | Server | Count | Examples |
|---|---|---:|---|
| Required | Required | 203 | Content mods and libraries, Create and GeckoLib among them |
| Required | Unsupported | 16 | BetterAdvancements, BetterF3, Controlling, CrashAssistant, Forgematica, MaFgLib, MouseTweaks, NekosEnchantedBooks, Prism, Searchables, athena, bwncr, embeddium, entityculling, jumpoverfences, modflared |
| Unsupported | Required | 13 | AI Improvements, Apothic Category Compat, Dungeon Crawl, Dungeons Arise, Ksyxis, three YUNG's Better mods, LootJS, Nether Portal Fix, Noisium, Repurposed Structures, Starlight |

No entry is optional. Commit 264f511d made every mod required, after the index of 3.0.7 (checked on
2026-10-31) had 25 entries optional on both sides, 6 only on the server and 5 only on the client.
Among them were Create, optional on the client although a client needs its blocks and items to join,
and GeckoLib, optional on the server although 10 other jars in `mods/` need it. JEI is in the index,
and EMI rides in `local-mods/` since 1ea5bb29.

Starlight runs only on the server since 2026-11-04. On a client it crashes the guide of Applied
Energistics 2, see [client](client.md#crash-history). Its 35 classes reference no `SimpleChannel`,
`NetworkRegistry` or `DeferredRegister`, so it opens no network channel and registers no content, and
a client without it passes the Forge handshake. Starlight 1.1.2 is the newest Forge build for 1.20.1
on Modrinth. The next build, 1.1.3, is for 1.20.2 only. The server keeps it for its light engine, see
[performance](../server/performance.md#levers).

Just Enough Resources 1.4.0.247 was a client only entry from d1cabf85 to 3b98076f and never reached a
pack. It is a JEI plugin without network code, so on a dedicated server it loads the loot tables from
the vanilla jar and the mod jars (`LootTableHelper.getLootDataManager`). Its mob drops and chest loot
therefore show the defaults of the mods, without the overrides of the fixes pack, and its ore tab knows
only the vanilla ore heights, because nobody profiled the worlds of SkyTekx 3. The owner wanted the
loot of the server, which [Advanced Loot Info](#advanced-loot-info) shows.

## Bundled jars

The 49 jars in `local-mods/` (origins from each jar's manifest and from `modrinth-permissions/`). The
counts in the list below are older than the last fourteen of them and were not recounted:

- 26 come from CurseForge, among them Bibliocraft, Bird's Nests, Clean Swing Through Grass, Connectivity,
  Cupboard, Deadly World, Fireflies, Hexxit Additions, Infernal Mobs, Shield Parry, Traverse and
  Tumbleweed.
- 2 match a Modrinth file exactly and could move into the index: Elevator Mod (`elevatorid`) and
  EnderIO 6.2.18 beta.
- Offline Skins is listed as a Modrinth mod, but no Modrinth file has its SHA-1.
- 4 were built locally from source: Aether Genesis (2026-06-30), Nyx (2026-06-29, see
  [Nyx](../content/nyx.md)), SkyTekx Compat (2026-07-12, see [compat mod](../content/compat-mod.md)) and
  Twilight Forest (2026-06-27).
- 2 joined on 2026-10-31 (507a8f2): AllTheLeaks, whose build is not on Modrinth, and the Cataclysm
  Spellbooks build from the fork skytekx/Cataclysm_Spellbooks, see below.
- EMI 1.1.24-skytekx.1 joined on 2026-11-03 (1ea5bb29), built from the fork skytekx/emi, branch
  `1.20.1-jei-bridge`. It replaces the Modrinth download of EMI 1.1.24, whose JEI bridge predates
  JEI 15.74 and failed on the recipes of Tinkers' Construct and Chipped. It needs JEI 15.74 or newer.
  Since 2026-11-04 git ships 1.1.24-skytekx.2 (3fd75e08), which treats the null entries that JEI allows
  in an ingredient list as empty, so the three entity melting recipes of Tinkers' Construct load too.
- Simply Jetpacks 2, version 2.11.0.1, joined on 2026-11-04 (56912155). It is the unchanged Modrinth
  file, MIT licensed, moved out of the index so that the pack can ship it with the guide book patch of
  `tools/patch_mods.py`, as `SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`. Its license text
  travels in `licenses/`.
- EnderIO Legacy Machines 0.4.9 (f315fa3d) and Mob Grinding Utils 1.1.0 (ee238bea) joined on
  2026-11-04 as CurseForge files, see [additions](#additions-of-2026-11-04). EnderIO Legacy Machines
  declares MIT and has no source repository, and the MIT text travels in `licenses/` because the jar
  has none.
- Flux Networks 7.2.1.15 joined on 2026-11-04 as a CurseForge file, see
  [Flux Networks](#flux-networks). It is MIT licensed, and the LICENSE of its repository travels in
  `licenses/` because the jar has none.
- SkyTekx Molten 1.0.0 joined on 2026-11-05 (a50c6748). The pack's own mod, built from the local repo
  `~/git/skytekx-molten` at edec978, adds 70 molten fluids with their melting and casting recipes for
  the ingot materials that Tinkers' Construct does not handle, such as palladium and yellorium. It
  registers fluids, blocks and buckets, so it is required on both sides. It is MIT licensed, and its
  license text travels in `licenses/` because the jar has none.

Seven of them are All Rights Reserved without a redistribution grant: Aether Genesis, Connectivity,
Cupboard, Deadly World, Fireflies, Hexxit Additions and Mob Grinding Utils
(`modrinth-permissions/README.md`). They block a public Modrinth listing, see [publish](publish.md).

## Jars outside the index

Up to 2026-10-31 the server's `mods/` and the owner's client (same SHA-1) held three jars that were in
neither the index nor `local-mods/`, so a fresh clone and the packs up to 3.0.5 lacked them:

| Jar | What it is | License | Source | Since 2026-10-31 |
|---|---|---|---|---|
| Jar `alltheleaks-1.1.1+1.20.1-forge.jar` | Memory leak fixes (AllTheLeaks) | MIT | Not on Modrinth | In `local-mods/` (507a8f2) |
| Jar `ba_bt-1.20.1-3.0.0-beta3.2.1.jar` | BrassAmber BattleTowers, the successor the owner named on 2026-06-29 12:01 | All Rights Reserved | CurseForge only | Still only in the server's `mods/`. Players add it by hand (21:48) |
| Jar `cataclysm_spellbooks-1.2.9-1.20.1.jar` | Cataclysm Spellbooks, built 2026-06-29 06:46 from the fork skytekx/Cataclysm_Spellbooks, commit 36d1cee | MIT | The fork has no release. Modrinth has a different upstream file that does not carry the fix | In `local-mods/` (507a8f2) |

The owner ruled on 2026-06-29 06:25 to patch Spellbooks rather than Cataclysm, because the upstream
Spellbooks crashed the client against stock Cataclysm 3.31. BattleTowers was then the last jar outside
the pack, and a client needs it. In the install test of 2026-10-31 a fresh install of 3.0.6 booted, but
the join failed with "Your client is missing the following mods" and named Brass-Amber BattleTowers.
All 177 registry entries the client lacked belong to it. Bundling it would need a redistribution grant.
On 2026-10-31 at 21:48 the owner decided to keep it on the server and to have players add the jar by
hand, and since about 21:56 the Modrinth body tells them to get it from CurseForge. The official file
is [ba_bt-1.20.1-3.0.0-beta3.2.1.jar](https://mediafilez.forgecdn.net/files/7453/586/ba_bt-1.20.1-3.0.0-beta3.2.1.jar)
(CurseForge project 542294, file 7453586, uploaded 2026-01-13, 54,453,322 bytes, SHA-1
fd9fc82fbdca5364992ec1b6518e3eb8d5bdc8ab), identical to the jar in `mods/`. At about 21:50 the owner
asked for the jar to send it to players over Telegram, and it was copied for them.
BattleTowers is also tied to an open ruling, see
[Skylands worldgen](../content/skylands-worldgen.md#open-rulings).

Since 2026-11-01 06:10 the server also runs Falling Meteors Reimagined 2.5 by menoXD, a Forge 1.20.1
fork of AlexDGr8r's Falling Meteors from Hexxit. The owner asked for it at 05:52 together with the
removal of Get It Together Drops, on the server and on both MacBooks, with meteors only in the
overworld. skytekx-compat 2.1.3 enforces that, see
[SkyTekx Compat](../content/compat-mod.md#falling-meteors-only-in-the-overworld). The jar
`meteors-2.5-forge.jar` (CurseForge project 1489468, file 8137833, uploaded 2026-05-24, 1,173,120
bytes, SHA-1 a9d3117be3bc39120ee93b08352a9e1c8f54efd1) declares `GNU LGPL v2.1 or later` in its
`mods.toml`, while the CurseForge project field says All Rights Reserved. It is not on Modrinth and
not in the pack. Like BattleTowers it sits only in the server's `mods/`, and players add it by hand.
A client without it cannot join, because it adds blocks, items and entities. Its code has no
mixins, no network or process calls and no class loading, and it touches the world only through
Forge events, its entities and its saved data.

A Modrinth pack may also download files from `github.com`, but neither jar is there. On 2026-11-01 at
06:30 the owner suggested loading both from GitHub. The GitHub API showed that the repo
`BrassAmber-Mods/Brass_Amber_BattleTowers` holds 1.20.1 branches, but its releases carry jars only for
1.18.2, and the repo names no license. menoXD's only public repo is a Spleef game, and no other repo
holds the Forge port of Falling Meteors. A copy of either jar under a SkyTekx account would be a
redistribution, so it needs the authors' permission first, as the bundled jars on
[publish](publish.md#what-blocks-a-release) do.

## Backpack

Since 2026-11-01 08:09 the server runs `backpack-1.20.1-2.0.1.1.jar` instead of Sophisticated
Backpacks and Sophisticated Core. It is our port of Eydamos Backpack 2.0.1, the backpack mod of
SkyTekx2, to Forge 1.20.1 (mod id `backpack`, 164,729 bytes, SHA-256
1205ec8790110cdc3f10ac558776da96ce06f8121873dc81c027994e01505d2d). The owner asked for it at 03:54,
see [Decisions](../decisions.md). Its source sits in the local repo `skytekx-backpack`, which has no
remote. It brings the normal and big backpacks in 16 colors, the workbench backpacks, the ender
backpack that opens the player's ender chest, bound and tanned leather, and the 2.0.1 recipes. There
is no back slot. Contents live in the world as `world/data/backpack_<uuid>.dat`, one file per
backpack.

The original is LGPL-2.1, and so is the port. The jar carries the license and a notice of the
changes. Whoever passes the jar on also has to offer its source, so the build writes
`backpack-1.20.1-2.0.1.1-sources.zip` next to it, which has to travel with the jar. Like Falling
Meteors it is not in the pack yet, and a client without it cannot join.

Sophisticated Backpacks and Sophisticated Core sit in `~/skytekx3-backups/jars-orig/` on the Mac mini
and on the build machine. Their entries are still in `modrinth.index.json` until the next pack.

## Parked and removed mods

The folder `disabled-mods/` holds 8 jars that are in neither the index nor `mods/`:

| Jar | Why it left |
|---|---|
| Better Combat, `bettercombat-forge-1.9.0+1.20.1.jar` | Left the index with mrpack 3.0.4. Reason not recorded, see [combat](../content/combat.md) |
| Memory Leak Fix, `memoryleakfix-forge-1.17+-1.1.5.jar` | Left the index with mrpack 3.0.4. Reason not recorded |
| WATERMeDIA, `watermedia-2.1.36.jar` | Refuses to run on a server and stopped the boot (`crash-reports/crash-2026-06-29_12.49.02-fml.txt:118`) |
| LittleFrames, `LittleFrames_FORGE_v1.4.3.jar` | Needs WATERMeDIA |
| VanillaBackport, `VanillaBackport-forge-1.20.1-1.1.7.10.jar`, and its compat jar `vb-compat-2.1-1.20.1.jar` | The owner allowed VanillaBackport only if materials stay consistent, one copper for everything (2026-06-29 13:59). Why it was parked is not recorded |
| Geophilic Backported, `GeophilicBackported-v1.3.3.jar` | Reason not recorded |
| Platform, `Platform-forge-1.20.1-1.3.4.jar` | Reason not recorded |

Mods removed for good:

- Radium broke the first boot on 2026-06-27. Its merged `PortalForcer` method made Arclight's
  `PortalForcerMixin` fail (`crash-reports/crash-2026-06-27_12.05.07-server.txt`). It predates git.
- Ruins left on 2026-06-30 01:13 (commit ac261f1), see
  [Skylands worldgen](../content/skylands-worldgen.md#history). The owner's client still has
  `ruins-1.20.1.2.jar`.
- Re:Avaritia was installed on 2026-06-30 06:21 and removed at 08:11 on the owner's word (commit bd5f18b).
- Skytekx-worldgen was replaced by SkyTekx Compat on 2026-07-12 (commit 174fbf6).
- Elenai Dodge 2 (`elenaidodge2-1.1.jar`, in the index since 2026-06-29) left the index on 2026-11-03
  on the owner's word at 20:16 (pack 3.1.2). Every dodge calls `FeathersHelper.spendFeathers`, the mod
  does not declare Feathers, and Feathers was never installed, so Left Alt crashed the client. Dodging
  never worked in this pack. Its network channel accepts a missing peer on both sides, so clients
  with and without it can join either server. The server keeps the jar until its next restart.
- Chocolate Quest Repoured was only tested. The index has Chocolate Quest Golden Apple Edition (1.1.0
  at commit 940f07e, 1.3.1 since the update), whose `mods.toml` license field says "Not specified".

## Changing the mod set

1. For a Modrinth mod, add an entry to `modrinth.index.json` with path, both hashes, size, env and the
   CDN URL, and put the same file into `mods/` (or into `client-only-mods/` if the server does not
   support it). Check its SHA-1 against the entry.
2. For a jar that is not on Modrinth, add it to `local-mods/`, copy it into `mods/`, and write a
   permission file in `modrinth-permissions/`.
3. If the jar is a target of `tools/patch_mods.py`, run the script.
4. Restart and compare the boot with the baseline of 3 ERRORs on
   [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline).
5. Rebuild the `.mrpack` as described on [publish](publish.md).

To move existing entries to newer builds, run `tools/update_mods.py` instead of step 1 and then follow
steps 3 to 5, see [tools](tools.md#the-new-update-script).

Do not use `tools/resolve_all.py` to fill `mods/`. It ignores the index, pulls the newest releases of a
hard coded list and would bring back Radium, Rubidium, memoryleakfix and Better Combat. See
[tools](tools.md).

## Mac mini

The script `macos/sync-to-mac.sh` mirrors `mods/` to the Mac mini as it is, with `rsync --delete`. The
folder already holds the patched jars and BattleTowers, since 2026-10-31 also the new builds of the
update above, since 2026-11-01 Falling Meteors Reimagined and the Backpack port, since 2026-11-03 the EMI fork, since 2026-11-05 the 13 new jars of 3.1.3 and EMI 1.1.24-skytekx.2. Do not rebuild `mods/` from the index or from `resolve_all.py`. See
[macOS host](../server/macos.md).

## History

The index held 213 entries in mrpack 3.0.4 (2026-06-29), 216 after commit d14ea2b (SlashBlade
Resharped, Re:Avaritia and Medieval Embroidery), 217 after 7cea753 (The One Probe) and 216 again after
bd5f18b (Re:Avaritia out). Commit 940f07e only raised `versionId` to 3.0.5. The `.mrpack` of 3.0.5 holds
an index that is byte identical to the one at commit 940f07e. On 2026-10-31 the update above moved 55
entries to newer builds and added mezz_config (092140d), 217 entries in all. Commit 74faf22 dropped
the Nyx entry, whose jar is bundled anyway, which leaves 216. Commit ca131c3 added the client only
Modflared for pack 3.0.7, 217 entries again. Commit 787cb1f dropped Placeable Items on 2026-11-01,
which leaves 216, and 3afc3bf dropped Get It Together Drops on the owner's ruling of 05:52 that day,
which leaves 215. Get It Together Drops ran only on the server, so clients lose nothing. The Twilight
Forest merge 3bdc98d6 of 2026-11-02 left 225. At 19:14 that day the owner asked for the grave mod that
every SkyTekx had so far, and commit 4eca94b1 added the GraveStone Mod 1.0.35, required on both sides,
for 226. The server runs it since 19:35, and both MacBooks got the jar, see [graves](../content/graves.md).

Placeable Items 1.0.0 turned a right click with any item without durability into a mob named "Placed
Block" whenever vanilla did nothing with the click, for example on a block just past the reach of 4.5
blocks, since the mod looks 5 blocks far. It stored only the item id, so a placed item came back
without enchantments, name, potion type, gem data or shulker box contents, and some items rendered as
the missing texture. The owner ruled at 01:34 to take it out. The mod's network channel accepts only
version 1 on both sides, so a client that still has it is refused by a server without it. Players
disable it in their instance until a pack without it is out.

Related: [AlmostUnified](almostunified.md), [KubeJS](kubejs.md), [publish](publish.md),
[tools](tools.md), [client](client.md), [Arclight](../server/arclight.md).
