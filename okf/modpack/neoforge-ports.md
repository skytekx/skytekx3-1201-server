---
type: Reference
title: NeoForge ports
description: The mods that only exist for NeoForge 1.21.1, or for 1.20.1 only on Fabric, and how each one reaches the Forge 1.20.1 pack, as an older Forge build in the index or as a vendored fork under ports/, with its source, what came in and what stayed out, its license conditions, its tests and the operator steps. Bygone Nether, the Mystical Automation port, the Mekanism: Extra Modules port, the Expanded Delight port and the Create: Integrated Farming port so far.
tags: [modpack, mods, ports, neoforge, index, mysticalautomation, mekanism, expandeddelight, farmersdelight, createintegratedfarming, create]
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
| [Bygone Nether](#bygone-nether) 1.3.2 | Eternal Nether, NeoForge 1.21.1 only | Index entry, no fork | Modrinth version `RA38ax2z`, built from upstream commit cb38a20 | 61adc700, 2026-11-05 |
| [Mystical Automation](#mystical-automation) 1.0.8-skytekx.1 | Itself, NeoForge 1.21.1 and newer only | Fork in `ports/mysticalautomation/`, jar in `local-mods/` | Branch `1.21` of the upstream repository at 4149601 ("Release 1.0.8") | Commits 3b9e2f7 and 3c92dcb, 2026-11-05 |
| [Mekanism: Extra Modules](#mekanism-extra-modules) 0.1.0-beta.3-skytekx.1 | Itself, NeoForge 1.21.1 with Mekanism 10.7 only | Fork in `ports/mekanism-extra-modules/`, jar in `local-mods/` | Tag `v1.21.1-0.1.0-beta.3` of the upstream repository at 1548dd6 | Commits 9813e0a to fa64b7f, 2026-11-05 |
| [Expanded Delight](#expanded-delight) 0.1.4-skytekx.1 | Itself, NeoForge 1.21.1 only, and on 1.20.1 an older Fabric line with 44 items | Fork in `ports/expandeddelight/`, jar in `local-mods/` | Branch `1.21-neo` of the upstream repository at ea82640, with branch `1.20` at 8d8fe44 as the reference for the 1.20.1 API | Commits eb734ad to 047ff2d, 2026-11-05 |
| [Create: Integrated Farming](#create-integrated-farming) 1.4.3-skytekx.1 | Itself, NeoForge 1.21.1 only | Fork in `ports/createintegratedfarming/`, jar in `local-mods/`, its library Create: Dragons Plus 1.11.9 as an index entry | Branch `1.21.1/6.0.0-dev` of the upstream repository at 1f63780 ("[version] Release 1.4.3") | Commits b4bea7f to 26fc840, 2026-11-05 |

## Bygone Nether

Eternal Nether, the continuation of Bygone Nether, only has builds for NeoForge 1.21.1. Its
predecessor Bygone Nether has exactly one build for Forge 1.20.1 on Modrinth, version `RA38ax2z`
("Bygone Nether v1.3.2", 2023-09-10) of the project `kt3BLgXB`. The pack takes that jar as it is
(61adc700). The details, the full test tables and the deploy steps are on
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
  pack repairs its two data defects that matter: the Quark tag `quark:wraith_spawnable` (b1aaaa99) and
  the basalt guard tag `bygonenether:no_basalt` (2ac9b02), see
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
`ports/mysticalautomation/` (6272974 to 38b38ec, fixes of the integration in 3e6717e, 77470b8 and
76936d8). The full change list is `ports/mysticalautomation/UPSTREAM.md`, and the page
[Mystical Automation port](mysticalautomation-port.md) has the test tables and the deploy.

### Source and ref

- The base is the branch `1.21` of the upstream Mystical Automation repository (the URL is in
  `UPSTREAM.md`) at commit 4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d ("Release 1.0.8", 2026-10-02).
  Upstream has no git tags, the commit is the head of that branch and it matches the Modrinth file of
  1.0.8 (version `HdlBF75n`, SHA-1 586be8e7f3fedc700e4bbf845a55c4758f12db7e) resource for resource.
  The 26.1 line of upstream was not used.
- The jar `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar` has SHA-1
  585446d50038e4f254c2fb2b012b616495ff5ed0 and 312,817 bytes since 3c92dcb, the same from two clean
  builds with `./gradlew build` on Java 17 (ForgeGradle 6.0.54, Forge 47.4.0 userdev). The first
  bundled build of 3b9e2f7, SHA-1 d2891fe346f5f6878789385e45742c6583a8d7c7, lacks the integration fixes.
- It runs on the Cucumber 7.0.16, Mystical Agriculture 7.0.24 and Mystical Agradditions 7.0.12 entries
  of the index. Upstream needs Cucumber 8.0.16 and Mystical Agriculture 8.0.19, so the port works
  against the 7.x APIs, and the build checks the SHA-1 of the three jars against the index.

### What came in and what stayed out

- The whole mod came in with its registry ids, NBT keys, slot layouts and recipe ids. The Java code
  moved to Forge 47 (registries, capabilities, a `SimpleChannel` with protocol version 1, menus and a
  COMMON config) and to the Machine Upgrades, altar and enchanter recipes of Mystical Agriculture 7.
  Container data travels as two 16 bit halves and slot counts above 127 in an extra message, because
  1.20.1 sends them as shorts and bytes (2aeb25b, 9dea4d3).
- The data moved to the 1.20.1 folders, and `c:ingots/iron` and `c:gems/diamond` became
  `forge:ingots/iron` and `forge:gems/diamond` (56f8285). The mod adds no material, so
  `config/almostunified/unify.json` needs no change, and no KubeJS script is needed.
- Fixes over upstream, all for a public server: ghost slot and machine mode messages only act on the
  open menu (af10144), the Enchanternator pays for the level that it enchants at (5dda687), the JEI
  transfer bound and the bone meal fuel charge (717b470), the Crafter refuses special recipes and
  recipes without inputs, which it built from ghost items alone (3e6717e, after upstream 7a481dc of the
  26.1 line), the Crafter and both Altarnators assemble their result from the items they take and not
  from the ghost grid, so a ghost item no longer lends its NBT to new output (77470b8), and the Farmer
  no longer duplicates part of a result and waits while its outputs are full (76936d8).
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
- The integration tests with the jar of 3c92dcb: both copies reached `Done` with 0 ERROR and 0 FATAL
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

## Mekanism: Extra Modules

Mekanism: Extra Modules adds three modules for the MekaSuit body armor: Phase Guard, which spends suit
energy every tick to block all damage and death, Chaos Anchor, which under an active Phase Guard also
stops the Chaos Guardian laser of Draconic Evolution, and Emergency Revival, which spends suit energy
to cancel one death like a Totem of Undying. Upstream builds it only for NeoForge 1.21.1 and Mekanism
10.7, so the pack bundles a port to Forge 1.20.1 and the pack's Mekanism 10.4.16 in
`ports/mekanism-extra-modules/` (9813e0a to 5739305, the review fixes 33745b0 to e2dabb9, the jar in
fa64b7f). The full change list and both test rounds are in `ports/mekanism-extra-modules/UPSTREAM.md`,
and the page [Mekanism Extra Modules port](mekanism-extra-modules.md) has the module costs, the pack
fit and the config.

### Source and ref

- The base is the tag `v1.21.1-0.1.0-beta.3` of the upstream repository
  https://github.com/Lumysia/mekanism-extra-modules (annotated tag object 774116e), commit
  1548dd6f2d41906e845c626895cb35769a33a61e ("chore(release): prepare 0.1.0-beta.3 (#19)", 2026-09-13).
  It matches the Modrinth version `WRuXXPxB` of the project `TfCrGUbp`. Upstream is a beta with 677
  downloads (Modrinth API of 2026-11-05).
- The jar `local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar` has SHA-1
  3c40952af5145c740fe6f8fc90ac191574bb2b2f and 659,044 bytes since fa64b7f, the same from two clean
  builds with `./gradlew build` on Java 17 (NeoForged ForgeGradle 6.0.21, Forge 47.1.70 userdev, the
  setup of SkyTekx DE Fixes). The first bundled build of 74bf989, SHA-1
  ca7f070e3f78405968a6fe93257fe235e4f3e925, lacks the review fixes.
- It compiles against the Mekanism 10.4.16.80 and Draconic Evolution 3.1.2.621 files of the index,
  which the build fetches from the Modrinth CDN and checks by SHA-1. It needs Mekanism and Mekanism
  Tools `[10.4.16,10.5)` and takes Draconic Evolution as an optional dependency pinned to
  `[3.1.2.621]`.

### What came in and what stayed out

- The whole mod came in with its mod id, registry ids, config keys and the 30 language files. The Java
  code moved to Forge 47 events, `DeferredRegister` and the module API of Mekanism 10.4, with a wrapper
  that turns each module's `RegistryObject` into the `IModuleDataProvider` of 10.4 (8284e90).
- Forge 47 has no `EntityInvulnerabilityCheckEvent`, so Phase Guard cancels `LivingAttackEvent` for
  `Player.hurt` and, since 33745b0, `LivingHurtEvent` for mods that call `actuallyHurt` directly, both
  at the highest priority. Before 33745b0 such a direct call still drew MekaSuit energy and used up
  absorption hearts.
- The Draconic Evolution mixin keeps upstream's two `@Redirect` injections into
  `LaserBeamPhase.serverTick` and copies no Draconic Evolution code. It sits next to the
  `@ModifyConstant` of SkyTekx DE Fixes in the same method. Since a7f22ef the version range pins
  Draconic Evolution 3.1.2.621, because a changed target would crash the server when the first Chaos
  Guardian loads the class, which no boot test sees.
- The recipes moved to `recipes/` and use the forge tags `forge:circuits/ultimate`,
  `forge:alloys/ultimate`, `forge:alloys/elite` and `forge:obsidian` that Mekanism 10.4 uses itself
  (1c8bc5a). The mod adds no material, so `config/almostunified/unify.json` needs no change and no
  KubeJS script is needed.
- From the later upstream commits the port takes the `/kill` bypass of db62401 (ec037bb). Two fixes are
  its own: the Phase Guard tick skips a player whose death went through (ff76194, keyed on the death
  since 0005853), and upstream's agent instruction files stay out (b685363).
- The port leaves out the config screen of upstream 576425a, which needs NeoForge's
  `ConfigurationScreen`, the refactors of db62401, which change nothing on 1.20.1, the narrower `/kill`
  bypass of upstream's branch `fix/phase-guard-kill` (9f51277), which needs MixinExtras and is not on
  upstream `main`, and the README banner, whose C2PA metadata names an image generation service.
- The Skylands rules are not touched. The mod has no world generation, no biome tags and no loot.

### License

The code and assets are LGPL-3.0-or-later (upstream `LICENSE`, unchanged since be44c67). The item
textures and HUD icons are edited copies of Mekanism textures, which are MIT. The conditions of the
license review of 2026-11-05:

- Keep the LGPL v3 unchanged and ship it with the GPL v3 that it incorporates, in the fork and in the
  jar (`LICENSE_mekanism_extra_modules`, `LICENSE_mekanism_extra_modules_GPL-3.0`), and keep
  `license="LGPL-3.0-or-later"` and the author entry in `mods.toml`. The whole modified work stays
  under LGPL-3.0-or-later, with no further restrictions.
- Ship Mekanism's MIT notice for the derived textures (`LICENSE_mekanism_extra_modules_Mekanism-MIT`)
  and name those files in the `NOTICE`.
- Ship a `NOTICE` that calls the build an unofficial modified build, names the upstream URL and
  commit, lists the changed files with the date and points to the Corresponding Source in
  `ports/mekanism-extra-modules` of this repository.
- Keep the complete source with its Gradle build in `ports/` for as long as the jar is offered. The
  record `modrinth-permissions/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar.md` (Status OK) links
  `tree/master/ports/mekanism-extra-modules`, so `tools/mrpack_permissions.py` refuses a public build
  until `origin/master` holds the fork and the same jar. The pack also carries
  `licenses/mekanism_extra_modules-0.1.0-beta.3-skytekx.1-sources.zip`, the git archive of e2dabb9.
- Copy no Draconic Evolution code into the fork and commit no Draconic Evolution jar, because of its
  CoFH "Don't Be a Jerk" license. Draconic Evolution stays an index entry.
- LGPL-3.0 and MIT have no non-commercial clause, so the mod adds no monetization limit. Not verified:
  a CurseForge listing (CurseForge answered 403) and whether the machine generated banner, which stays
  out, can carry copyright at all.

### Tests

Evidence of 2026-11-05 from test copies outside the repository, driven over RCON by a scratch harness
that ticks a server player without a client. The full tables are in
`ports/mekanism-extra-modules/UPSTREAM.md`.

- The first round, with the jar of 74bf989, booted plain Forge 47.4.18 with and without Draconic
  Evolution and SkyTekx DE Fixes with 0 ERROR and 0 FATAL lines. Its Arclight copy had no Draconic
  Evolution, no SkyTekx DE Fixes and no plugin.
- The second round, with the jar of fa64b7f, booted plain Forge 47.4.18 and the pack's hardened
  Arclight jar, both with Draconic Evolution 3.1.2.621 and SkyTekx DE Fixes, with 0 ERROR and 0 FATAL
  lines, and both stopped over RCON with exit code 0. Under Phase Guard a direct `actuallyHurt` left
  health, absorption hearts and suit energy unchanged, while the same call without it drew 240,000 J
  or took 6 absorption. A health of 0 without a death came back to 1.0, a `kill()` stayed a death
  without further energy draw, Emergency Revival fired at lethal damage, and on Arclight
  `CraftPlayer.setHealth(0)`, the call of EssentialsX's `/kill`, was canceled by Phase Guard or met by
  Emergency Revival. The exported `LaserBeamPhase` carries the injections of both mods on both
  servers.
- A test copy of the jar with the Draconic Evolution range `[3.1.2.620]` stopped the Forge boot at mod
  loading, so the pin works.
- Nobody tested a real client, the HUD icons, the module screen, a Chaos Guardian fight, the EssentialsX
  `/kill` command itself and a boot with EssentialsX or the full pack.

### Operator steps

The owner mirrors the server by hand, and the mod goes live with the next pack, because clients
without it cannot join (it registers items and Mekanism modules on both sides):

- Stop the server over RCON and wait until the java process is gone.
- Copy `local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar` into the server's `mods/` by
  hand and check its SHA-1 against the one above. Mekanism, Mekanism Tools and Draconic Evolution
  3.1.2.621 are there already. On the Mac mini `macos/sync-to-mac.sh` mirrors `mods/` from the build
  machine.
- Clients need the same jar, which the next pack brings them.
- Start the server and check the boot against the error baseline. The mod writes
  `world/serverconfig/mekanism_extra_modules-server.toml`, which Forge rewrites at every stop, so edit
  it only while the java process is gone.
- The mod has no world generation, so old and new chunks are the same to it.
- To remove a player under an active Phase Guard or a powered Emergency Revival, use
  `/minecraft:kill`. The plain `/kill` of EssentialsX does not get through.
- A Draconic Evolution update stops the boot until `de_range` in
  `ports/mekanism-extra-modules/gradle.properties` is widened and the jar rebuilt, after a test copy
  has loaded `LaserBeamPhase` with the new build.
- Before a public pack, merge and push the branch with `ports/mekanism-extra-modules/` to master and
  fetch, see [publish](publish.md).

## Expanded Delight

Expanded Delight is an addon for Farmer's Delight with a cinnamon tree and wood set, asparagus, sweet
potatoes, chili peppers, peanuts and cranberries, a juicer, casks that ferment milk into cheese
wheels, salt and many dishes. Upstream builds the full content set only for NeoForge 1.21.1, and its
Fabric line for 1.20.1 has 44 items against 99, so the pack bundles a port to Forge 1.20.1 in
`ports/expandeddelight/` (eb734ad to 2428130, the jar in d9eb6cc and rebuilt in 047ff2d). The full
change list is `ports/expandeddelight/UPSTREAM.md`, and the page
[Expanded Delight port](expandeddelight-port.md) has the content, the pack fit and the test tables.

### Source and ref

- The content base is the branch `1.21-neo` of https://github.com/ianm1647/expandeddelight at commit
  ea826400654702587e86c9e94544bfd74f065083 ("fix workspace", 2026-08-25, `mod_version=0.1.4`). The
  Modrinth release 0.1.4-neoforge (version `RTRYPIJp`, `expandeddelight-0.1.4.jar`) was built from
  28d9561714a176eb10ae0c425fcee18ad8e6b3b1 on the same branch.
- The branch `1.20` at 8d8fe44d9b4cba54c47f84c5836872a803388cb2 (2026-01-14, 0.4.0 for Fabric 1.20.1)
  served as the reference for the 1.20.1 game API, and the port took the Forge forms from Farmer's
  Delight 1.20.1-1.3.4. The branch `1.21-rework` at 5ef645b (0.2.0) is an emptied restart whose
  commits say "complete emptiness" and "content cut", so it was not used.
- The jar `local-mods/expandeddelight-0.1.4-skytekx.1.jar` has SHA-1
  45b288dd96173e620db5a6d1065250a96b584949 and 486,426 bytes since 047ff2d, the same from two clean
  builds with `./gradlew build` on Java 17 (NeoForged ForgeGradle 6.0.21, Forge 47.1.70 userdev, the
  setup of SkyTekx DE Fixes). The first bundled build of d9eb6cc, SHA-1
  3e156033dbf6a5a09d199960614776023e15e69a, lacks the review fixes.
- It needs Farmer's Delight 1.20.1-1.3.4 or newer, which the index has. The build fetches that file
  from the Modrinth CDN and checks its SHA-1 against the index, as it does for the compile only
  KubeJS, Rhino and Architectury.

### What came in and what stayed out

- The whole content set of 0.1.4 came in with its mod id, registry ids, block states and NBT keys:
  99 items and 45 blocks. The Java code moved to Forge 47 (b098e2b): item stack NBT for the juicer's
  drink instead of data components, a new loot function `expandeddelight:copy_drink`, the item
  handler through `getCapability`, `DeferredRegister`, a hand written recipe serializer and
  `IExtensibleEnum` for the recipe book.
- The data moved to the 1.20.1 folders and formats through `tools/convert_data_to_1_20_1.py` in the
  fork (36ce58e), and the c: tags became the forge: tags of Farmer's Delight 1.20.1 and Pam's
  HarvestCraft 2 Food Core: `forge:cheese`, `forge:milk`, `forge:vegetables`, `forge:cookies`,
  `forge:crops/*`, `forge:dusts/salt`, `forge:ores/salt`, `forge:juices/*`, `forge:jams/*` and, since
  2428130, `forge:nuts` with `forge:nuts/peanut`.
- The pack glue of d8f433e gives AlmostUnified salt as its 48th material, so `forge:dusts/salt` keeps the
  Mekanism salt by the mod priority list, and `kubejs/server_scripts/tag_fixes.js` links the salt,
  juice and jelly tags of Expanded Delight, Pam's Food Core and Mekanism, naming items only of loaded
  mods. No other pack mod adds asparagus, sweet potatoes, chili peppers, peanuts, cranberries or
  cinnamon, so those need no unification.
- Upstream's goat milk mixin into `Goat#mobInteract` is gone. The port cancels Forge's interaction
  event when it milks, so no mixin touches a class that Bukkit patches. The port has no mixin and no
  access transformer at all. Log stripping uses Forge's tool modification event, and cranberries plant
  through a vanilla `PlaceOnWaterBlockItem`, which Bukkit's `BlockPlaceEvent` covers.
- Fixes over upstream: crops that named themselves as seed, a juicer drink taken by hand that was
  neither saved nor synced, a chest boat that lost its items at every save, the crushing mallet that
  1.20.1 refuses (86f6702), and from the review the default state of the juicer, which was waterlogged
  for `/setblock` and every mod that places it (3393d44), seven language keys that showed raw
  (9306195) and the juicer's container slot, which took any item from a pipe (c99d6cf).
- The port leaves out the datagen classes and worldgen bootstrap classes, which only build upstream's JSON on
  NeoForge, the overrides of Farmer's Delight's dough and melon juice recipes and of a Farmer's
  Respite recipe (the pack edits no recipes of other mods, and the 1.20.1 ids differ), the 1.20.5
  animal food tags and the Fabric libraries of the 1.20 branch. The wild sweet potato gives Absorption
  in suspicious stew, because Oozing needs Minecraft 1.20.5.
- The Skylands rules hold. The biome modifiers add the cinnamon tree and wild crops to
  `#minecraft:is_jungle`, `#forge:is_plains`, `#forge:is_desert`, `#minecraft:is_savanna` and
  `#forge:is_swamp`, and the salt ore to `#minecraft:is_ocean`. None of these tags holds
  `skytekx3:sky_void` or another Skylands biome, and the wild crop and cranberry patches also keep
  the `farmersdelight:biome_tag` filter for `#minecraft:is_overworld`. The mod adds no structures and
  names no `skytekx3` biome. Whether the islands should grow these crops is open, see
  [open items](../open-items.md).

### License

Expanded Delight declares MIT (`mod_license=MIT` in `gradle.properties` of 1.21-neo, the mods.toml
of the 0.1.4 jar, Modrinth and CurseForge), and its branch `1.20` carries a CC0 1.0 LICENSE. The
license review of 2026-11-05 found material of Farmer's Respite, which is All Rights Reserved, in the
1.21-neo tree, so the upstream tree copied unchanged would not pass. Its conditions, all met:

- Three Farmer's Respite files never entered git and are not in the jar: the juicer GUI sheets
  `textures/gui/juicer.png` and `textures/gui/jei/juicer.png` (one file, SHA-1 2a2bffad, built on the
  kettle sheet) and the bottle slot icon `textures/item/empty_container_slot_bottle.png` (SHA-1
  64863cf1). The vendoring commit eb734ad left them out. The juicer draws the author's own sheet
  `juicer_gui.png` of the branch `1.20` (commit 50e9c10, 2022) instead.
- Upstream's `JuicerMenu.java`, decompiled from the KettleMenu of Farmer's Respite, stayed out. The
  port's menu is written from Farmer's Delight's CookingPotMenu (MIT) with the slots of the 1.20
  sheet.
- No copy of a vanilla texture ships: the unused `item/glass_bottle.png` and `item/sweet_berries.png`
  stayed out, and so did the byte copy `block/rice_bag_bottom.png` of Farmer's Delight, whose model
  already points at `farmersdelight:block/rice_bag_bottom`.
- The fork and the jar carry the author's MIT text with its copyright line from branch 1.19 (commit
  6c73c98) as `LICENSE-MIT_expandeddelight`, the CC0 LICENSE of branch 1.20 unchanged as
  `LICENSE_expandeddelight`, Farmer's Delight's MIT text as `LICENSE_farmersdelight`, because the mod
  derives art and code from it, and the NeoForged MDK template license. The same texts lie in
  `licenses/expandeddelight-0.1.4-skytekx.1/`, which the pack ships as `overrides/licenses/`.
- A `NOTICE_expandeddelight` calls the build unofficial and modified, names the upstream URL, branches
  and full commits, the source at `ports/expandeddelight` in this repository, the left out Farmer's
  Respite files with the reason, and the art and code taken from Farmer's Delight under MIT.
- No translation, tag or recipe of the branch `1.20` came in, so the material of its outside
  contributors stays out. Every commit reachable from 1.21-neo is by the author.
- The record `modrinth-permissions/expandeddelight-0.1.4-skytekx.1.jar.md` says Status OK, permission
  reason Licensed, license MIT, with the upstream link. It links
  `tree/master/ports/expandeddelight`, so `tools/mrpack_permissions.py` refuses a public build until
  `origin/master` holds the fork and the same jar. MIT and CC0 do not ask for the source, only this
  gate does.
- MIT, CC0 and Farmer's Delight's MIT have no clause against commercial use, so the mod adds no
  monetization limit. Not verified: the license that GitHub detects for the repository (the API
  answered 403), a texture comparison with Croptopia, which is not on Modrinth, and the code
  provenance outside the juicer, which the review checked only at member level against Brewin' and
  Chewin'. Nobody asked the Farmer's Respite authors to settle the MIT and All Rights Reserved
  conflict of their own listings.

### Tests

Evidence of 2026-11-05 from test copies outside the repository, driven over RCON. The full tables
are on [Expanded Delight port](expandeddelight-port.md#test-of-2026-11-05).

- The port's tests with the jar of d9eb6cc on plain Forge 47.4.18 with Farmer's Delight, JEI and
  KubeJS: after the crushing mallet fix 86f6702 the boot reached `Done` with 0 ERROR and 0 FATAL
  lines. All 99 recipe files loaded, every item went into a chest, every block stood after
  `setblock`, hoppers ran the juicer, bone meal grew the crops and the tree, the casks fermented, and
  1,209 chunks of jungle, plains, desert, savanna, swamp and ocean held the wild crops, the trees and
  the salt ore. The class load log listed no class of the `client` or `integration.jei` packages.
- With Mekanism 10.4.16.80, Pam's Food Core 1.0.5, AlmostUnified 0.11.0 and the pack's glue the merged
  tags held the items of all three mods, and AlmostUnified replaced the salt of Expanded Delight and
  of Pam's mod with the Mekanism salt.
- The integration test with the jar of 047ff2d on the same Forge server reached `Done` in 9.0 s with
  0 ERROR and 0 FATAL lines and stopped over RCON with exit code 0. A juicer from `/setblock` passed
  `execute if block ... expandeddelight:juicer[facing=north,waterlogged=false]`, a side hopper handed
  over three glass bottles and kept four apples, and `forge:nuts` held the peanut next to Pam's
  sunflower seeds.
- An Arclight copy with the repo's hardened `arclight.jar` and the same mods reached `Done` with
  0 ERROR and 0 FATAL lines and no failed mixin, ran the same juicer, hopper and tag checks with the
  same results, pressed an apple juice into the hopper below and stopped over RCON with exit code 0.
- Nobody tested a client (the juicer screen, its recipe book, JEI, the boats, signs and tooltips and
  the seven new names), the hand interactions that need a player (milking a goat, filling a cask,
  planting cranberries, stripping a log) and a boot of the full pack with the mod.

### Operator steps

The owner mirrors the server by hand, and the mod goes live with the next pack, because clients
without it cannot join (it registers blocks, items, two boats, a menu and a recipe type on both
sides):

- Stop the server over RCON and wait until the java process is gone.
- Copy `local-mods/expandeddelight-0.1.4-skytekx.1.jar` into the server's `mods/` by hand and check
  its SHA-1 against the one above. Farmer's Delight 1.20.1-1.3.4 is there already. On the Mac mini
  `macos/sync-to-mac.sh` mirrors `mods/` from the build machine.
- Clients need the same jar, which the next pack brings them.
- Start the server and check the boot against the error baseline. The first boot writes
  `config/expandeddelight-common.toml` with the recipe book switch of the juicer. Commit it before
  `tools/build_mrpack.py`, which takes overrides from git HEAD.
- The salt material of AlmostUnified needs this restart. Afterwards `unify_inventory.js` swaps the
  salt of Expanded Delight and of Pam's mod in player inventories for the Mekanism salt.
- The cinnamon trees, wild crops, cranberries and salt ore only generate in chunks that do not exist
  yet, so players find them beyond the explored area. A pregen with Chunky places them ahead of time.
- Before a public pack, merge and push the branch with `ports/expandeddelight/` to master and fetch,
  see [publish](publish.md).

## Create: Integrated Farming

Create: Integrated Farming is a Create addon of the DragonsPlus team with roosts that hold chickens and
lay eggs, fishing nets for water and lava that work on contraptions, and the Vacuum Harvester, a
kinetic block that harvests and replants crops around it. Upstream builds it only for NeoForge 1.21.1,
so the pack bundles a port to Forge 1.20.1 and Create 6.0.8 in `ports/createintegratedfarming/`
(b4bea7f to 7bff431, the jar in 32d35c9 and rebuilt in 26fc840). The full change list is
`ports/createintegratedfarming/UPSTREAM.md`, and the page
[Create Integrated Farming port](createintegratedfarming-port.md) has the content, the pack fit and
the test tables.

### Source and ref

- The base is the branch `1.21.1/6.0.0-dev` of https://github.com/DragonsPlusMinecraft/CreateIntegratedFarming
  at commit 1f63780108d1d51f3b2c9ebdf4bb0cd5cb3f5906 ("[version] Release 1.4.3", 2026-09-21). It is
  the only branch, the repository has no tags, and all 285 Java files of the sources jar of the
  Modrinth release 1.4.3 (version `EWfVwbHy`) are byte identical to it (b4bea7f).
- The jar `local-mods/create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar` has SHA-1
  74b2b560712ba3efe7992fb354cfef8b8d18275a and 488,836 bytes since 26fc840, the same from two clean
  builds of a git archive export with `./gradlew build` on Java 17 (ForgeGradle 6.0.6, Forge 47.4.0
  userdev, Create 6.0.8-289). The first bundled build of 32d35c9, SHA-1
  0d447e2c04a55b83a657a2f34f34fac192ea1288, lacks the review fixes.
- It needs Create 6.0.8 and Create: Dragons Plus 1.11.9 for Forge 1.20.1 (Modrinth version
  `eMalpJtP`, `CreateDragonsPlus-1.11.9.jar`, SHA-1 cb249347ad15914619a81371b4cfa35f74f575ed), which
  joined the index in c15417f. Upstream asks for Create 6.0.10 or newer, which does not exist for
  1.20.1, so the fork asks for `[6.0.8,6.1.0)`, the range of Create: Dragons Plus. The build fetches
  Create: Dragons Plus, Farmer's Delight, Supplementaries and Moonlight from the Modrinth Maven, so it
  needs network access.

### What came in and what stayed out

- The whole mod came in with its mod id, registry ids, block states, NBT keys and config keys: the
  Roost, the Chicken Roost, the Fishing Net, the Lava Fishing Net and the Vacuum Harvester, their
  Ponder scenes, the custom harvest rules for Create's Mechanical Harvester and saws, the leash rule of
  Create's seats, and the integrations of Farmer's Delight, Supplementaries and Twilight Delight. The
  Java code moved to Forge 47 (d256d09): one `@Mod` entry point, Forge capabilities for the roost,
  the harvester and the Farmer's Delight basket, a `SimpleChannel` `create_integrated_farming:main`
  with protocol 1, `Block#use`, the loot data manager and the 1.20.1 fishing forms. The five mixins
  into Create set `remap = false`, and no mixin targets a Minecraft class.
- The synced NeoForge data maps for chicken food became `ChickenFoodMap`, which reads the same files
  on every reload and sends them to clients. The data moved to the 1.20.1 folders with `forge:` tags
  (`forge:rods/wooden`, `forge:plantoil`) and a new item tag `create_integrated_farming:chicken_food`
  with the six chicken foods of 1.20.1 (e704709). The mod adds no material, so AlmostUnified and
  `config/almostunified/unify.json` need no change, and no KubeJS script was needed.
- An addition over upstream: the pack has none of the four mods that upstream's Lava Fishing Net
  needs, so a new integration lets Lava Fishing 1.2.1 fill it (16dc400).
- Fixes over upstream: the roost drops a copy of the chicken food remainder, so every feeding returns
  it (a706e83), the integrations are reobfuscated against their targets, which fixed a
  `NoSuchMethodError` crash next to Farmer's Delight tomatoes (69bc4e3), and from the review the
  remainder field takes the 1.21.1 object form again (042f279), the roost Ponder scene stores its egg as
  a 1.20.1 stack (dd98051) and the recipe advancements unlock the crafting recipes (179c3bc).
- The port leaves out the integrations of 26 mods that the pack does not carry, with their duck, goose,
  turkey and warm or cold chicken roosts (a8dc899). The warm and cold chickens are 1.21.5 content.
  The datagen classes compile but never ran, so the generated files are upstream's output converted by
  hand. Spotless is gone, because it rewrote the checkout on every build.
- The library Create: Dragons Plus brings content of its own into the pack: liquid dyes, dragon's
  breath as a fluid with a cauldron and a dripstone duplication, the Fluid Hatch, and the fan
  processing types Bulk Coloring, Bulk Ending, Bulk Freezing and Bulk Sanding with recipes such as
  end stone from cobblestone, phantom membrane from leather and blue ice from packed ice. The list
  with the jar paths and the config switches is on
  [Create Integrated Farming port](createintegratedfarming-port.md#create-dragons-plus). Nobody
  checked it against the progression of the pack, see [open items](../open-items.md).
- Neither mod has worldgen, so the Skylands rules are not touched.

### License

The code is LGPL 3.0 or later (`mod_license` in `gradle.properties`, `license` in mods.toml and every
source header), and Modrinth lists LGPL-3.0-only. The license review of 2026-11-05 allowed the public
fork and the bundled jar, monetization included, under twelve conditions that the record
`modrinth-permissions/create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar.md` lists one by one. All
are met:

- Upstream's `LICENSE.txt` (the LGPL 3.0 text followed by the GPL 3.0 text) stays unchanged in the fork
  and the jar, and so does `FARMERS_DELIGHT_LICENSE.txt` (MIT) next to the basket and safety net
  textures that come from Farmer's Delight.
- The jar adds Create's MIT notice as `LICENSE-CREATE.txt`, for a harvest method adapted from Create's
  `BlockHelper#destroyBlockAs` and pixel blocks of two Create textures that predate Create's commit
  9dcc21950 (2025-09-21), after which Create's assets are All Rights Reserved. No newer Create asset is
  copied.
- Every upstream header keeps "Copyright (C) 2025 DragonsPlus". The three Lava Fishing classes are
  modified copies of upstream's Nether Depths Upgrade integration and keep that line next to the
  SkyTekx line (c07c056). The fork adds no further terms.
- A `NOTICE` in the fork and the jar marks the build as unofficial and modified, names the upstream
  repository and commit, gives the date and sums up the changes (GPL 5(a)). The same license texts and
  the NOTICE lie in `licenses/create-integrated-farming-1.20.1-1.4.3-skytekx.1/`.
- The complete source is `ports/createintegratedfarming/` at the commit that last changed the jar in
  `local-mods/` (26fc840), and it must stay public as long as a pack that ships the jar is offered
  (GPL 6(d)). The record links `tree/master/ports/createintegratedfarming`, so
  `tools/mrpack_permissions.py` refuses a public build until `origin/master` holds the fork and the
  same jar.
- Create: Dragons Plus (LGPL-3.0-or-later) comes from the Modrinth CDN, so the pack does not
  redistribute it, and the port vendors none of its code. Not verified: whether the texture
  `textures/block/chicken.png` derives from the vanilla chicken, which nobody compared.

### Tests

Evidence of 2026-11-05 from test copies outside the repository, driven over RCON. The full tables
are on [Create Integrated Farming port](createintegratedfarming-port.md#tests).

- The port's tests with the first build on plain Forge 47.4.18 with Create, Create: Dragons Plus,
  Farmer's Delight, Supplementaries, Twilight Delight with the Twilight Forest fork, Lava Fishing and
  KubeJS: every boot reached `Done` with no FATAL line and no ERROR line from the mod. The Vacuum
  Harvester harvested and replanted seven kinds of crops, the roosts took food by hand, belt and Spout
  and laid eggs, a roost item caught a chicken, both fishing nets caught fish on a Mechanical Bearing,
  and inventories survived a restart. The class load log held no client class of the mod.
- The review's Arclight copy with the repo's hardened `arclight.jar` and the same mods reached
  `Done (3.666s)` with no FATAL line and no failed mixin. Create: Dragons Plus applied its mixins into
  `ServerPlayer`, `BottleItem`, `ConcretePowderBlock`, `PointedDripstoneBlock`, `RecipeManager` and
  `ReloadableServerResources`, and the only ERROR lines were the flat world line and the three tag
  lines that Twilight Delight 2.2.8 logs on any server.
- The integration test with the jar of 26fc840 on the same Forge copy reached `Done` with the same
  ERROR lines, loaded `using_converts_to` in both forms, dropped a bowl for each of two feedings of
  bread, showed the egg of the placed roost schematic and the `crafting/` rewards of the recipe
  advancements, and stopped over RCON with exit code 0 after a second boot.
- Nobody tested a client (the Ponder scenes, the JEI category Roosting, the renderers) or a boot of the
  full pack with the two mods, on Forge or on Arclight, and the Aether part of Create: Dragons Plus
  never ran.

### Operator steps

The owner mirrors the server by hand, and the mod goes live with the next pack, because clients
without it cannot join (the channel `create_integrated_farming:main` and the blocks of both mods are
needed on both sides):

- Stop the server over RCON and wait until the java process is gone.
- Copy `local-mods/create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar` into the server's `mods/` by
  hand and check its SHA-1 against the one above. Download `CreateDragonsPlus-1.11.9.jar` from its
  `cdn.modrinth.com` URL in `modrinth.index.json` into `mods/` too and check its SHA-1
  cb249347ad15914619a81371b4cfa35f74f575ed. Create 6.0.8, Farmer's Delight, Supplementaries, Twilight
  Delight and Lava Fishing are there already. On the Mac mini `macos/sync-to-mac.sh` mirrors `mods/`
  from the build machine.
- Clients need the same two jars, which the next pack brings them.
- Start the server and check the boot against the error baseline. The first boot writes
  `world/serverconfig/create_integrated_farming-server.toml`, `world/serverconfig/create_dragons_plus-server.toml`
  and `config/create_dragons_plus-common.toml`. Commit the last one before `tools/build_mrpack.py`,
  which takes overrides from git HEAD.
- Neither mod generates anything in the world, so old chunks need no change and no pregen.
- Before a public pack, merge and push the branch with `ports/createintegratedfarming/` to master and
  fetch, see [publish](publish.md).

Related: [Mod set](mod-set.md), [Boot fixes](../content/boot-fixes.md), [Publish](publish.md),
[Mystical Automation port](mysticalautomation-port.md),
[Mekanism Extra Modules port](mekanism-extra-modules.md),
[Expanded Delight port](expandeddelight-port.md),
[Create Integrated Farming port](createintegratedfarming-port.md), [Open items](../open-items.md).
