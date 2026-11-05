# LittleTiles (SkyTekx build of the upstream branch 1.20)

Bundled file: `LittleTiles_BETA_v1.6.0-pre161-skytekx.1_mc1.20.1.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://modrinth.com/mod/littletiles
- License: LGPL-3.0-only
- Explanation:

  LittleTiles_BETA_v1.6.0-pre161-skytekx.1_mc1.20.1.jar is a SkyTekx build of LittleTiles by CreativeMD and N247S from the unchanged sources of the head of the branch 1.20 of https://github.com/CreativeMD/LittleTiles (commit c5694e16070fd10460a093df817149128936ba86 of 2026-05-08). That commit is 43 commits past 1.6.0-pre161, the last release for 1.20.1, and holds backported 1.21.1 fixes and the fix of a hybrid server crash (32acae1c). The repository carries the GNU LGPL v3 as LICENSE, the mods.toml says license="LGPL-3.0-only", and the Modrinth project lists LGPL-3.0-only. The LGPL allows conveying the library in object code, modpacks included, under the LGPL with the license texts and the Corresponding Source. The jar ships the LGPL v3 as LICENSE_littletiles, the GPL v3 as LICENSE_littletiles_GPL-3.0 and a NOTICE_littletiles that names the upstream commit as the Corresponding Source, says that only the build differs and points to the build workspace in ports/littletiles of the SkyTekx 3 repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/littletiles). The version carries the suffix -skytekx.1, and the NOTICE says the build is not an official release.

## Reference

- Redistribution allowed: yes
- On Modrinth: older builds only (https://modrinth.com/mod/littletiles, newest 1.20.1 build 1.6.0-pre161, which this build replaces in the index)
- Author: CreativeMD and N247S (original mod), built by SkyTekx
- Notes: SHA-1 5f69d1c6ee932367c567f92539d7320a5c030250, 2,650,303 bytes. Built with Java 17 and Gradle 8.1.1 from `ports/littletiles/` (`LT_SRC` pointing to a checkout of c5694e16, `CC_SRC` to CreativeCore at 4138056a, `CC_JAR` to the CreativeCore 2.12.40 jar of the index and `EMBEDDIUM_JAR` to the Embeddium 0.3.20 release jar of Modrinth, then `gradle jarJar`). Two clean builds gave the same SHA-1. No build script of the upstream repository runs. The build compiles against the Embeddium release instead of the git build of maven.embeddedt.org, which the build machine cannot reach, and against two empty stand-ins for Fabric API interfaces that the Embeddium WorldSlice implements. No class of LittleTiles names those types. Against the upstream pre161 jar, 910 classes are byte identical, 143 changed with the upstream commits, one is new, the mixin refmap is identical, and the jar adds the zh_cn translation of upstream, the two license files and the NOTICE. It needs CreativeCore 2.12.30 or newer (the pack has 2.12.40), and MixinExtras 0.4.1 rides inside as upstream's jar has it. A Forge 47.4.18 test server with CreativeCore 2.12.40 booted it on 2026-11-05 with 0 ERROR lines and placed and saved its tile, rendered tile, signal converter, storage and colored blocks.
