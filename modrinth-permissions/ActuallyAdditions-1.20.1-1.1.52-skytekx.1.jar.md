# Actually Additions (SkyTekx 1.20.1 fork, 1.3.26 backport)

Bundled file: `ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://github.com/Ellpeck/ActuallyAdditions
- License: MIT
- Explanation:

  ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar is an unofficial, modified Minecraft 1.20.1 Forge build of Actually Additions by Ellpeck and contributors, built from source in this repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/actuallyadditions). It starts from the branch 1.20.1 of https://github.com/Ellpeck/ActuallyAdditions at commit 226a439154a8f103424783ad7afc1e33fe59cbd7 (2024-03-04, version 1.1.52, which upstream never released) and backports fixes and content of the branches 1.20.4, 1.21 and main up to commit cd9d9c63254f471f966ee51b078afdf47af5f852 (release 1.3.26, NeoForge 1.21.1). The Modrinth project Actually Additions (actually-additions, id 4K7Q3nqd) has no file for Minecraft 1.20.1, its files cover 1.7.10 to 1.12.2, 1.20.4 and 1.21.1 (Modrinth API, checked on 2026-11-06), so the pack has to bundle this build. Upstream moved every active branch to the MIT License on 2024-03-04 (commit 33f554119218b40dc1047c3d5a34545e64c78fe3, issue #1391), the LICENSE on main is the same text ("Copyright (c) 2019 Mrbysco"), the mods.toml says MIT, and the Modrinth project declares MIT. MIT permits modifying and redistributing the software, modpacks included, as long as the copyright and permission notice are kept. Upstream jars carry no license file, so this jar adds the upstream LICENSE unchanged as LICENSE_actuallyadditions and a NOTICE_actuallyadditions that says the build is modified, unofficial and not endorsed by upstream, names both upstream commits, states the content level, lists the changes and points to the source. The mods.toml keeps the upstream authors and credits, marks the build as unofficial and sends bug reports to this repository. The NOTICE also carries the MIT notice of Mekanism for the rotation helpers that upstream took from it, and keeps upstream's credit for the experience formulas excerpted from Ender IO with permission.

## Reference

- Redistribution allowed: yes, under MIT with the notices in the jar
- On Modrinth: yes for 1.20.4 and 1.21.1 (https://modrinth.com/mod/actually-additions, project 4K7Q3nqd), but no 1.20.1 file and not this modified build, so it has to be bundled
- Author: Ellpeck and contributors (original mod), 1.20.1 backport by SkyTekx
- Notes: SHA-1 227073059543dd3d969cd53a15f1fd42a38985cb, 3009728 bytes, the reobf jar of `./gradlew build` in `ports/actuallyadditions/` (Java 17, ForgeGradle 6.0.54, Forge 47.1.3 userdev, Parchment 2023.09.03). Two clean builds gave the same jar. `ports/actuallyadditions/UPSTREAM.md` lists every build and source change, and the plan, the tests and the rollout rules are on `okf/modpack/actuallyadditions-backport.md`. The license texts, with the Mekanism notice, also travel in `licenses/ActuallyAdditions-1.20.1-1.1.52-skytekx.1-LICENSE.txt`. No separate asset license exists on either upstream ref, the textures of the 1.20+ retexture came into the MIT repository after the relicensing, and the Modrinth team lists their artist. Patchouli, Curios and JEI are compile time dependencies only and ship no class in the jar. MIT has no source duty, the source link is the convention of this repository, and `tools/mrpack_permissions.py` refuses the jar until master on GitHub holds `ports/actuallyadditions/` and this jar. The jar's NOTICE names the repository root and the path `ports/actuallyadditions`. Not published on Modrinth or CurseForge.
