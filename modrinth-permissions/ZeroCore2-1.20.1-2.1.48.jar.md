# ZeroCore 2

Bundled file: `ZeroCore2-1.20.1-2.1.48.jar`
Status: VERIFY, do not publish until the copy in git is resolved

## Paste into the Modrinth attribution form

- Permission reason: Special permission
- Link to work: https://www.curseforge.com/minecraft/mc-mods/zerocore
- License: All Rights Reserved
- Explanation:

  ZeroCore 2 (ZeroCore2-1.20.1-2.1.48.jar) by ZeroNoRyouki is All Rights Reserved (mods.toml license="All rights reserved", Modrinth LicenseRef-All-Rights-Reserved), except its multiblock API, whose MIT text the jar carries in LICENSE.txt. The author grants modpack use in the project description on CurseForge and Modrinth: "You can add Zero Core to your mod pack or use them in your let's play / live streams to your heart content, no need to ask for my permission: you got it." The Extreme Reactors 2 description repeats it for both mods: "You can add Extreme Reactors 2 (and Zero Core 2) to your mod pack ... no need to ask for my permission: you got it." The pack bundles the unmodified CurseForge file, because build 2.1.48, which Extreme Reactors 2.0.97 requires, is not on Modrinth.

## Reference

- Redistribution allowed: unmodified and inside the modpack, by the author's grant. Not as a copy of its own on another website, which the jar in the public git repository would be
- On Modrinth: older builds only (https://modrinth.com/mod/zerocore, project rHpb85Mf, newest 1.20.1 build 2.1.45)
- Author: ZeroNoRyouki
- Notes: CurseForge project 247921, file 7344725, uploaded 2025-12-17 by ZeroNoRyouki together with Extreme Reactors 2.0.97, 1,341,342 bytes as the CurseForge API reports, SHA-1 d60df80acecc27e20401065a252773f86b08e9de. The file from mediafilez.forgecdn.net and the one from the CurseForge site download are byte identical. The grant covers adding the mod to a modpack. The same description section adds a repost limit: "No one is authorized to repost my mods, or any part of them, on any other website then the ones I post them to" (Modrinth text, which names CurseForge, Modrinth and the FTB launcher as those sites, while the older CurseForge text says "other then Curse Forge"). The pack ships the jar inside its `.mrpack` on Modrinth, which the grant covers. Git also tracks the jar in `local-mods/` of the public GitHub repository skytekx/skytekx3-1201-server since commit ffc3aa3, because tools/build_mrpack.py reads `local-mods/` from git HEAD only. On 2026-11-05 no remote branch held that commit yet (origin/master and origin/claude/twilight-forest-port-mods-j2owbq do not contain it), but the release steps merge it into master and push it (okf/modpack/publish.md, step 2), and then github.com serves the jar as a download of its own. The repost clause names only CurseForge, Modrinth and the FTB launcher, so it does not cover that copy. The Status stays VERIFY, which makes the build tools refuse a public pack, until the author allows the copy in the repository or the jar leaves the public git history, see okf/open-items.md, owner decisions. The CurseForge allowModDistribution flag could not be read from CurseForge itself on 2026-11-04 and 2026-11-05: the CurseForge API needs a key, and the site API and api.curse.tools answered 403 or a Cloudflare challenge. The public API proxy cfproxy.bmpm.workers.dev reports allowModDistribution true for ZeroCore 2 and Extreme Reactors on 2026-11-05, but it reports true for Cupboard, Connectivity and Hexxit Additions as well, whose flag other records here call off, and the Curse Maven test answers 200 for this file just as for Cupboard and Hexxit Additions, so neither settles the flag. The grant in the description is the permission this record rests on, as for Fast Leaf Decay and Enchanting Plus. A modified ZeroCore would need the author's permission. This jar is unmodified.
