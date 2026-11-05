# Create: Integrated Farming (1.20.1 Forge port by SkyTekx)

Bundled file: `create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://github.com/DragonsPlusMinecraft/CreateIntegratedFarming
- License: LGPL-3.0
- Explanation:

  create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar is an unofficial, modified port of Create: Integrated Farming 1.4.3 by DragonsPlus from NeoForge 1.21.1 to Forge 1.20.1, made for this pack. It is built from https://github.com/DragonsPlusMinecraft/CreateIntegratedFarming, branch 1.21.1/6.0.0-dev at commit 1f63780108d1d51f3b2c9ebdf4bb0cd5cb3f5906 (the 1.4.3 release). The mod is licensed under the GNU LGPL version 3 or later (mods.toml, gradle.properties and every source header say LGPL-3.0-or-later, Modrinth lists LGPL-3.0-only). The LGPL permits conveying a modified version, also inside a modpack, under the same license, with a notice of the changes and the complete source. The jar ships the LGPL and GPL texts as LICENSE.txt, Create's MIT notice as LICENSE-CREATE.txt (for one method and two texture details taken from Create's MIT era files), the MIT text of Farmer's Delight next to the textures copied from it, and a NOTICE that says it is a modified build, names the upstream commit and the date and lists every change. The complete source of the port is public in this pack's repository under ports/createintegratedfarming (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/createintegratedfarming), and the NOTICE in the jar points there. The mod id stays create_integrated_farming, and the version carries the suffix -skytekx.1.

## Reference

- Redistribution allowed: yes, under the LGPL, once master carries its source (see Notes)
- On Modrinth: NeoForge 1.21.1 only (https://modrinth.com/mod/create-integrated-farming, project 9k1pAsfR, 1.4.3 as version EWfVwbHy). This 1.20.1 build is not, so it must be bundled
- Author: DragonsPlus (Create: Integrated Farming), port by SkyTekx. Parts follow Create (MIT, before 2025-09-21) and Farmer's Delight (MIT)
- Notes: SHA-1 74b2b560712ba3efe7992fb354cfef8b8d18275a, 488,836 bytes, the build with the fixes of the integration review (it replaced the first build 0d447e2c of 32d35c9). Built with Java 17 from `ports/createintegratedfarming/` (`./gradlew build`), which fetches Create 6.0.8, Create: Dragons Plus 1.11.9, Farmer's Delight 1.20.1-1.3.4, Supplementaries 1.20-3.1.44 and Moonlight 1.20-2.16.38B. Two clean builds gave the same SHA-1. The source of this jar is `ports/createintegratedfarming/` at the commit that last changed the jar in `local-mods/`. The directory is unchanged there since 7bff431. A rebase changes these hashes, so the rule names the commit, and the hashes only help to find it. It requires Create [6.0.8,6.1.0) and Create: Dragons Plus 1.11.9 or newer on both sides, which the index carries. The license review of 2026-11-05 set these conditions, and the build meets each of them:
  1. Upstream's LICENSE.txt (the LGPL 3.0 text followed by the GPL 3.0 text, no added terms) stays unchanged in the port and in the jar.
  2. `textures/block/farmersdelight/FARMERS_DELIGHT_LICENSE.txt` (MIT, Copyright (c) 2020 vectorwing) stays next to the basket and safety net textures, in the source and in the jar.
  3. Create's MIT notice (the Create LICENSE before commit 9dcc21950, "MIT License, Copyright (c) 2019 simibubi") ships as LICENSE-CREATE.txt in the port and the jar, for `CustomHarvestBehaviour#harvestBlock` (marked as code reference to Create's `BlockHelper.destroyBlockAs`) and the pixel blocks of `cam_linkage.png` and `rotation_speed_controller.png` in `vacuum_harvester.png`.
  4. Every upstream file header ("Copyright (C) 2025 DragonsPlus", "SPDX-License-Identifier: LGPL-3.0-or-later") stays, as do `codeformat/HEADER.txt` and `license="LGPL-3.0-or-later"` in mods.toml. The files SkyTekx added carry the same header with a SkyTekx copyright line. The three classes of the Lava Fishing integration are modified copies of upstream's Nether Depths Upgrade integration and keep the DragonsPlus line next to the SkyTekx line (c07c056). The fork adds no further terms.
  5. The fork meets the GPL 3.0 terms for conveying a modified work, which covers the LGPL-3.0-only label of Modrinth too.
  6. The NOTICE in the port and in the jar says that this is an unofficial modified build, names the upstream repository and commit 1f63780108d1d51f3b2c9ebdf4bb0cd5cb3f5906, gives the date (November 2026) and sums up the changes. `ports/createintegratedfarming/UPSTREAM.md` records repository, ref, full commit, date, license and every change, and the git history keeps the upstream snapshot.
  7. The complete Corresponding Source (build files, wrapper, sources and generated resources) is public at `ports/createintegratedfarming` and must stay there as long as any pack version that ships this jar is offered. The jar's NOTICE and this record point at it, and the reproducible build lets anyone check that jar and source match.
  8. The jar is in `local-mods/`, with copies of the LGPL and GPL texts, the Farmer's Delight MIT text, Create's MIT text and the NOTICE in `licenses/create-integrated-farming-1.20.1-1.4.3-skytekx.1/`.
  9. No Create asset that Create added or changed on or after its commit 9dcc21950 (2025-09-21) is copied. The models refer to Create textures such as `create:block/sail/frame` by resource location only.
  10. Create: Dragons Plus is an index entry (Modrinth version eMalpJtP, `CreateDragonsPlus-1.11.9.jar`), so the pack does not redistribute it, and the port vendors none of its code.
  11. Charging for the pack is allowed under the LGPL and GPL. Other licenses in the pack decide whether the project may be monetized, see the publish page.
  12. Not verified: `textures/block/chicken.png` looks like pixel art derived from the vanilla chicken, a Mojang asset that the Minecraft usage guidelines govern like other mod textures. The references of upstream to the 1.21.5 textures `minecraft:entity/chicken/warm_chicken` and `cold_chicken` are gone with the Vanilla Backport integration.

  Publishing the source is a duty of the LGPL here. The pack's own gate also wants it: `tools/mrpack_permissions.py` refuses the jar until `origin/master` holds `ports/createintegratedfarming/` and this build in `local-mods/`. Not published on Modrinth or CurseForge by SkyTekx.
