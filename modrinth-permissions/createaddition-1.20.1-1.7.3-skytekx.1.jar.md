# Create Crafts & Additions (SkyTekx 1.20.1 fork, 1.7.3 backport)

Bundled file: `createaddition-1.20.1-1.7.3-skytekx.1.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://github.com/mrh0/createaddition
- License: MIT
- Explanation:

  createaddition-1.20.1-1.7.3-skytekx.1.jar is an unofficial, modified Minecraft 1.20.1 Forge build of Create Crafts & Additions by MRH0, built from source in this repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/createaddition). It starts from the branch 1.20.1 of https://github.com/mrh0/createaddition at commit 42b6f2d06f0412a7cee787d767449d5b1b3b81b8 (2026-11-02, version 1.3.3 plus the merged community fixes after that release) and backports fixes and content of the branch 1.21.1 at commit 658a390b8ff4daa17013e9447b29949330c8a42a (version 1.7.3, NeoForge 1.21.1). It replaces the Modrinth file createaddition-1.20.1-1.3.3.jar of the same project, which the pack downloaded from the Modrinth CDN before. The repository ships an MIT LICENSE ("Copyright (c) 2024 MRH"), the mods.toml says "MIT License, Copyright 2022 MRH0", and the Modrinth project declares MIT. MIT permits modifying and redistributing the software, modpacks included, as long as the copyright and permission notice are kept. Upstream jars carry no license file, so this jar adds the upstream LICENSE unchanged as LICENSE_createaddition and a NOTICE_createaddition that says the build is modified, unofficial and not endorsed by upstream, names both upstream commits, lists the changes and points to the source. The mods.toml keeps the upstream author and license and marks the build as an unofficial backport. Some code and assets of the mod come from Create, which was MIT as a whole before 2025-09-21. The NOTICE lists every such part with Create's MIT notice, both the notice of 2019 and the copyright line of Create's current code license, and the jar holds no Create asset that Create published on or after 2025-09-21, when Create's assets became All Rights Reserved. The sounds come from pixabay.com (Pixabay Content License) and freesound.org (CC0) and ship only inside the jar.

## Reference

- Redistribution allowed: yes, under MIT with the notices in the jar
- On Modrinth: yes for 1.3.3 (https://modrinth.com/mod/createaddition, project kU1G12Nn), but not this modified build, so it has to be bundled
- Author: MRH0 and contributors (original mod), 1.20.1 backport by SkyTekx
- Notes: SHA-1 b7355fab1f431ddf186a5c7be2e5a4d4c751983b, 1716104 bytes, the slim reobf jar of `./gradlew build` in `ports/createaddition/` (Java 17, ForgeGradle 6.0.6, Forge 47.4.0 userdev, Create 6.0.8). An incremental and a clean build gave the same jar. `ports/createaddition/UPSTREAM.md` lists every build and source change, and the plan, the tests and the rollout rules are on `okf/modpack/createaddition-backport.md`. The license texts, with both Create notices, also travel in `licenses/createaddition-1.20.1-1.7.3-skytekx.1-LICENSE.txt`. The Pixabay sounds stay inside the jar and are never copied out. The texture and model checks of the release ran against Create 6.0.3, 6.0.8 and 6.0.10 (`UPSTREAM.md`, License). MIT has no source duty, the source link is the convention of this repository, and `tools/mrpack_permissions.py` refuses the jar until master on GitHub holds `ports/createaddition/` and this jar. The jar's NOTICE names the repository root and the path `ports/createaddition`. Not published on Modrinth or CurseForge.
