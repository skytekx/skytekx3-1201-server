---
type: Reference
title: NeoForge ports
description: The mods that only exist for NeoForge 1.21.1, or for 1.20.1 only on Fabric, and how each one reaches the Forge 1.20.1 pack, as an older Forge build in the index or as a vendored fork under ports/, with its source, what came in and what stayed out, its license conditions, its tests and the operator steps. Bygone Nether and the Mystical Automation port so far.
tags: [modpack, mods, ports, neoforge, index, mysticalautomation]
timestamp: 2026-11-05
---

# NeoForge ports

Some mods that suit the pack only exist for NeoForge 1.21.1, or for Minecraft 1.20.1 only on Fabric.
Each of them reaches the Forge 1.20.1 pack in one of two ways. Where an older Forge 1.20.1 build of
the same mod or of its predecessor is on Modrinth, the pack takes that jar unchanged as an entry of
`modrinth.index.json`. Otherwise a vendored fork under `ports/<slug>/` is ported down to Forge 47.4.18
like [Twilight Forest](twilightforest-backport.md) and [Ender IO](enderio-backport.md) before it: an
`UPSTREAM.md` with repository, ref, full commit and license summary, the upstream license files, a
reproducible Java 17 Gradle build, the version suffix `-skytekx.1`, a `NOTICE` in the jar and the jar in
`local-mods/` with its record in `modrinth-permissions/`. A fork keeps the mod id of its upstream, so
worlds and configs keep working if an upstream build for Forge 1.20.1 replaces it later.

| Mod | Stands in for | Way into the pack | Source | Since |
|---|---|---|---|---|
| [Bygone Nether](#bygone-nether) 1.3.2 | Eternal Nether, NeoForge 1.21.1 only | Index entry, no fork | Modrinth version `RA38ax2z`, built from upstream commit cb38a20 | 3d7c5cc0, 2026-11-05 |
| [Mystical Automation](#mystical-automation) 1.0.8-skytekx.1 | Itself, NeoForge 1.21.1 and newer only | Fork in `ports/mysticalautomation/`, jar in `local-mods/` | Branch `1.21` of the upstream repository at 4149601 ("Release 1.0.8") | Commits c3bb6eb and e6b9182, 2026-11-05 |

## Bygone Nether

Eternal Nether, the continuation of Bygone Nether, only has builds for NeoForge 1.21.1. Its
predecessor Bygone Nether has exactly one build for Forge 1.20.1 on Modrinth, version `RA38ax2z`
("Bygone Nether v1.3.2", 2023-09-10) of the project `kt3BLgXB`. The pack takes that jar as it is
(3d7c5cc0). The details, the full test tables and the deploy steps are on
[Mod set](mod-set.md#bygone-nether).

### Source and ref

- The file is `bygonenether-1.3.2-1.20.x.jar` with 7,642,613 bytes and SHA-1
  529e31d2877b87a8bf006f590513a33ab6b73171, from its `cdn.modrinth.com` link in the index. The SHA-1,
  SHA-512 and size matched the Modrinth API on 2026-11-05.
- It was built from commit cb38a20bf1ceb2a374f60bdf6994b433b594f54c ("v1.3.2 (i)", 2023-09-10). That
  commit is the head of the branch `1.20.1` of the source repository that the Modrinth project links
  and of the branch `1.20.1-legacy-forge` of the Eternal Nether repository. The license review of
  2026-11-05 compared the 383 non-class files of the jar with that commit by git blob id: every png, ogg
  and nbt matched byte for byte, and the JSON files differed only in line endings.
- Its `mods.toml` asks for Forge 46 or newer and Minecraft 1.20 up to 1.21 and nothing else. Its mod
  id is `bygonenether`, while Eternal Nether uses `eternalnether` (`modId` in the `gradle.properties`
  of the branch `1.21.1`). So the blocks, items and mobs of a world made with Bygone Nether would not
  carry over if the pack ever moved to Eternal Nether.

### What came in and what stayed out

- The whole mod came in unchanged. It adds the Catacomb, the Piglin Manor and the Citadel in Nether
  biomes, eight mobs, 21 blocks, 35 items and 58 recipes, lets bastions generate in basalt deltas and
  turns mined blackstone into Cobbled Blackstone. It adds no biomes.
- No fork was needed. The jar runs unchanged on Forge 47.4.18 and on Arclight 1.0.6, and the fixes
  pack repairs its two data defects that matter: the Quark tag `quark:wraith_spawnable` (eb0619b5) and
  the basalt guard tag `bygonenether:no_basalt` (1e25cdf), see
  [boot fixes](../content/boot-fixes.md#the-override-datapack).
- Everything that Eternal Nether added after 1.3.2 for NeoForge 1.21.1 stayed out, among them the 12
  textures that its `LICENSE-ASSETS.md` marks All Rights Reserved: chiseled warped nether bricks, soul
  stone, warped nether bricks, the two withered debris faces, the three warped enderman layers, the
  cutlass, the warped ender pearl, the withered bone and the withered bone meal. Their license forbids
  reposting, and the 1.3.2 jar holds none of them. They must never go into the pack, a fork or a
  texture backport.
- The pack needs no unification change. The mod adds no `forge:` material tags, and its netherite scrap
  nugget is not the netherite nugget of Cyclic or Tinkers' Construct, so `config/almostunified/` stays
  as it is.
- The Skylands rules hold. Its structure tags name only Nether biomes (`soul_sand_valley`,
  `crimson_forest`, `warped_forest`, the Withered Abyss of Biomes O' Plenty and optional biomes of
  Incendium and BYG), and the bastion tag gets only `minecraft:basalt_deltas`. The structure sets are
  global, but a start only passes in a biome of its tag, so none generates in the Overworld, the End or
  the Skylands. The mod names no `skytekx3` biome and no `is_overworld` tag. The test servers found
  none of its structures in the Overworld or the End, and they had no Skylands, so that part rests on
  the tag files alone.

### License

Code and assets of cb38a20 are MIT (`LICENSE.txt` of that commit, `license=MIT` in
`gradle.properties`, `license="MIT"` in the jar's `mods.toml`, and MIT on Modrinth). The conditions of
the review of 2026-11-05:

- Ship it only as the index entry with its CDN link and hashes. An index entry needs no
  `modrinth-permissions/` record and no copy in `local-mods/`, because `tools/mrpack_permissions.py`
  only gates bundled jars.
- Do not bundle, rebuild or fork the jar under this review. The upstream jar has no license file, so a
  later bundle or fork has to add `LICENSE.txt` with the copyright line of the MIT license and a
  `NOTICE`. The docs credit the Bygone Nether project.
- Keep the 12 All Rights Reserved textures of Eternal Nether out, see above.
- Selling is allowed. MIT grants the rights to use, copy, modify, publish, distribute and sell, with no
  clause against commercial use.
- Nobody verified the two vanilla textures that the jar replaces for every client, `blackstone.png`
  and `cracked_nether_bricks.png`. They may be edits of Mojang art that the MIT license cannot cover,
  which is one more reason never to bundle the jar. Whether any texture of the mod comes from a third
  party is unverified too, and nothing in the source suggests one.

### Tests

Evidence of 2026-11-05, details on [Mod set](mod-set.md#tests):

- A plain Forge 47.4.18 server with the mod and the pack mods that it touches (Create, Quark, Biomes O'
  Plenty, Curios, Repurposed Structures, Cataclysm, YUNG's Better Dungeons, Saturn and Chunky) booted
  with 0 FATAL and, after the Quark tag repair, 0 ERROR lines. It generated all three structures with
  their mobs and blocks in the Nether and none in the Overworld or the End, loaded 58 recipes of the
  mod and loaded no class of its `client` package.
- An Arclight 1.0.6 server with the repo's `arclight.jar` and the same mods reached Done with 0 ERROR
  and 0 FATAL lines and no failed mixin. Its Nether pregen of 6,561 chunks ran without an error, and it
  stopped over RCON without a crash report or a watchdog.
- With the basalt guard tag, the bastion in a basalt delta of the test seed held 2,157 basalt, 0 magma
  blocks and 8 lava inside its pieces on Forge, against 9,800, 136 and 175 without it, and the pregen
  took 2:47 against 2:52. Arclight gave 2,176, 3 and 8.
- Nobody tested the natural spawns inside the structures, the rescue of a Piglin Prisoner, the Gilded
  Netherite Shield, the textures in a client and a boot of the full pack with the mod, see
  [open items](../open-items.md#not-verified).

### Operator steps

The owner mirrors the server by hand, and the mod goes live with pack 3.1.3, because clients without
it cannot join. The full list is on [Mod set](mod-set.md#deploy):

- Stop the server over RCON and wait until the java process is gone.
- Download the jar from its CDN link in `modrinth.index.json`, check the SHA-1 above and copy it into
  the server's `mods/` by hand. On the Mac mini `macos/sync-to-mac.sh` mirrors `mods/` from the build
  machine. Clients need the same jar, which pack 3.1.3 brings them.
- Deploy the fixes pack with its two new tag files, start the server and, with nobody online, run
  `/datapack disable "file/skytekx3_fixes"` and `/datapack enable "file/skytekx3_fixes" last` once,
  so that the Quark tag repair loads after the pack of the mod.
- Check the boot against the error baseline.
- New structures, bastions in basalt deltas and soul stone only generate in Nether chunks that do not
  exist yet, so players find them beyond the area that they explored. A Nether pregen with Chunky
  places them ahead of time.

## Mystical Automation

Mystical Automation adds seven machines to Mystical Agriculture: the Infuser, the Crafter, the Farmer,
the Fertilizer, the Infusion and Awakening Altarnators and the Enchanternator. Upstream builds it only
for NeoForge 1.21.1 and newer, and no Forge 1.20.1 build exists, so the pack bundles a port in
`ports/mysticalautomation/` (a63ea3b to ad7088c, fixes of the integration in 50aaaed, e80e47f and
b4fda97). The full change list is `ports/mysticalautomation/UPSTREAM.md`, and the page
[Mystical Automation port](mysticalautomation-port.md) has the test tables and the deploy.

### Source and ref

- The base is the branch `1.21` of the upstream Mystical Automation repository (the URL is in
  `UPSTREAM.md`) at commit 4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d ("Release 1.0.8", 2026-10-02).
  Upstream has no git tags, the commit is the head of that branch and it matches the Modrinth file of
  1.0.8 (version `HdlBF75n`, SHA-1 586be8e7f3fedc700e4bbf845a55c4758f12db7e) resource for resource.
  The 26.1 line of upstream was not used.
- The jar `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar` has SHA-1
  585446d50038e4f254c2fb2b012b616495ff5ed0 and 312,817 bytes since e6b9182, the same from two clean
  builds with `./gradlew build` on Java 17 (ForgeGradle 6.0.54, Forge 47.4.0 userdev). The first
  bundled build of c3bb6eb, SHA-1 d2891fe346f5f6878789385e45742c6583a8d7c7, lacks the integration fixes.
- It runs on the Cucumber 7.0.16, Mystical Agriculture 7.0.24 and Mystical Agradditions 7.0.12 entries
  of the index. Upstream needs Cucumber 8.0.16 and Mystical Agriculture 8.0.19, so the port works
  against the 7.x APIs, and the build checks the SHA-1 of the three jars against the index.

### What came in and what stayed out

- The whole mod came in with its registry ids, NBT keys, slot layouts and recipe ids. The Java code
  moved to Forge 47 (registries, capabilities, a `SimpleChannel` with protocol version 1, menus and a
  COMMON config) and to the Machine Upgrades, altar and enchanter recipes of Mystical Agriculture 7.
  Container data travels as two 16 bit halves and slot counts above 127 in an extra message, because
  1.20.1 sends them as shorts and bytes (bb7e135, 82878fa).
- The data moved to the 1.20.1 folders, and `c:ingots/iron` and `c:gems/diamond` became
  `forge:ingots/iron` and `forge:gems/diamond` (85cce3c). The mod adds no material, so
  `config/almostunified/unify.json` needs no change, and no KubeJS script is needed.
- Fixes over upstream, all for a public server: ghost slot and machine mode messages only act on the
  open menu (66b49ff), the Enchanternator pays for the level that it enchants at (298bf2c), the JEI
  transfer bound and the bone meal fuel charge (654dab4), the Crafter refuses special recipes and
  recipes without inputs, which it built from ghost items alone (50aaaed, after upstream 7a481dc of the
  26.1 line), the Crafter and both Altarnators assemble their result from the items they take and not
  from the ghost grid, so a ghost item no longer lends its NBT to new output (e80e47f), and the Farmer
  no longer duplicates part of a result and waits while its outputs are full (b4fda97).
- The CraftTweaker integration and its test scripts stayed out, because CraftTweaker is not in the
  pack. Datapacks and KubeJS add Farmer recipes with `event.custom`.
- The Skylands rules are not touched. The mod has no world generation, no biome tags and no mixins.

### License

The code and assets are MIT (the upstream `LICENSE` of 2025, kept with its copyright line), and the port
carries a few classes adapted from Cucumber 8 and three tooltip texts of Mystical Agriculture 8, both
MIT as well. The conditions of the license review of 2026-11-05:

- Keep the upstream `LICENSE` unchanged in the fork and in the jar (`LICENSE_mysticalautomation`), and
  ship `LICENSE-Cucumber` and `LICENSE-MysticalAgriculture` next to it, since the official 1.0.8 jar
  carries no license file.
- Ship a `NOTICE` in the jar that calls the build unofficial and not endorsed by upstream and names
  the upstream URL, branch `1.21` at 4149601 and the source at `ports/mysticalautomation` in this
  repository. The `mods.toml` marks the build unofficial, points `issueTrackerURL` at SkyTekx, drops
  upstream's update URL and keeps `license="MIT"` and the mod id.
- Keep the copies of Cucumber code in the packages of this mod, because Forge's module layer rejects
  packages split across two jars.
- Keep Cucumber, Mystical Agriculture and Mystical Agradditions as CDN entries of the index.
- Keep the record `modrinth-permissions/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar.md` with Status
  OK. It links `tree/master/ports/mysticalautomation`, so `tools/mrpack_permissions.py` refuses a public
  build until `origin/master` holds the fork and the same jar.
- MIT has no clause against commercial use and no source duty. Publishing the source is the convention
  of this repository. Not verified: the CurseForge license field (CurseForge answered 403) and a pixel
  comparison of the GUI backgrounds with vanilla.

### Tests

Evidence of 2026-11-05 from two test copies outside the repository, a plain Forge 47.4.18 server and
an Arclight server with the hardened `arclight.jar`, each with the mod, its three dependencies, JEI
and Patchouli and driven over RCON. The full tables are on
[Mystical Automation port](mysticalautomation-port.md#tests).

- The port's tests with the first jar: three Forge boots with 0 FATAL lines (one ERROR from the flat
  test world without generator settings, then 0), every machine running from NBT set inventories,
  hoppers, Machine Upgrades, Clearable blocks, a restart, a `/reload` and 78 dynamic Farmer recipes,
  and no class of the `client` or `compat.jei` packages in the class load log of the dedicated server.
- The integration tests with the jar of e6b9182: both copies reached `Done` with 0 ERROR and 0 FATAL
  lines and stopped over RCON with exit code 0. A Crafter with a ghost shulker box and dye gave
  nothing, an enchanted ghost over plain leggings or a plain sword gave nothing in the Crafter and the
  Awakening Altarnator, and matching inputs gave their results with or without enchantments. The
  Farmer merged a cocoa result of 3 into 62 as 64 and 1 and waited while its outputs were full.
- Nobody tested the screens, the JEI categories, the recipe transfer, the ghost slots in a client, the
  guide book entries and a boot of the full pack with the mod.

### Operator steps

The owner mirrors the server by hand, and the mod goes live with the next pack, because clients
without it cannot join (its network channel refuses a peer without the mod):

- Stop the server over RCON and wait until the java process is gone.
- Copy `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar` into the server's `mods/` by hand
  and check its SHA-1 against the one above. Cucumber, Mystical Agriculture and Mystical Agradditions
  are there already. On the Mac mini `macos/sync-to-mac.sh` mirrors `mods/` from the build machine.
- Clients need the same jar, which the next pack brings them.
- Start the server and check the boot against the error baseline. The mod writes
  `config/mysticalautomation-common.toml` with every machine enabled.
- The mod has no world generation, so old and new chunks are the same to it.
- Before a public pack, merge and push the branch with `ports/mysticalautomation/` to master and fetch,
  see [publish](publish.md).

Related: [Mod set](mod-set.md), [Boot fixes](../content/boot-fixes.md), [Publish](publish.md),
[Mystical Automation port](mysticalautomation-port.md), [Open items](../open-items.md).
