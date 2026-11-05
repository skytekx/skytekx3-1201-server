# Extreme Reactors 2

Bundled file: `ExtremeReactors2-1.20.1-2.0.97.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://www.curseforge.com/minecraft/mc-mods/extreme-reactors
- License: MIT
- Explanation:

  Extreme Reactors 2 (ExtremeReactors2-1.20.1-2.0.97.jar) by ZeroNoRyouki declares license="MIT" in its META-INF/mods.toml, and its Modrinth project lists MIT. The MIT license permits redistribution in any form, modpacks included, as long as the copyright and permission notice travel with the copies. The author also grants modpack use in the project description on CurseForge and Modrinth: "You can add Extreme Reactors 2 (and Zero Core 2) to your mod pack or use them in your let's play / live streams to your heart content, no need to ask for my permission: you got it." Extreme Reactors 2 continues the 1.7.10 mod Big Reactors, which is MIT licensed as well (LICENSE.txt of github.com/erogenousbeef/BigReactors: Copyright (c) 2013 "Erogenous Beef"), and the jar includes code and textures of Big Reactors. The jar carries no license text, so the pack ships both MIT notices, that of Extreme Reactors 2 and that of Big Reactors, next to it in licenses/ExtremeReactors2-1.20.1-2.0.97-LICENSE.txt. Build 2.0.97 for 1.20.1 is only on CurseForge. Modrinth stops at 2.0.84.

## Reference

- Redistribution allowed: yes
- On Modrinth: older builds only (https://modrinth.com/mod/extreme-reactors, project idkvShUy, newest 1.20.1 build 2.0.84)
- Author: ZeroNoRyouki
- Notes: CurseForge project 250277, file 7344727, uploaded 2025-12-17 by ZeroNoRyouki, 2,578,824 bytes as the CurseForge API reports, SHA-1 0e7877f58d4f1fcec50742f30aaace2053e00bc6. The file from mediafilez.forgecdn.net and the one from the CurseForge site download are byte identical. The repository ZeroNoRyouki/ExtremeReactors2 has no LICENSE file. The project description also says "No one is authorized to repost my mods, or any part of them, on any other website other then Curse Forge", in the same words on CurseForge and on Modrinth (api.modrinth.com/v2/project/extreme-reactors, body, checked 2026-11-05). The wording "other website then the ones I post them to" is the one of the ZeroCore 2 description. The pack is published on Modrinth, and the MIT license of the jar permits redistribution anyway. It needs ZeroCore 2 1.20.1-2.1.48 or newer (mods.toml range `[1.20.1-2.1.48,1.21-2.2.0)`), which the pack bundles next to it. The mods.toml description calls the mod "a continuation of the 1.7.10 mod Big Reactors from ErogenousBeef", and line 3 of assets/bigreactors/lang/en_us.json names Erogenous Beef first among the authors. The license review of 2026-11-05 found the reactor and turbine simulation of Big Reactors (RadiationHelper, MultiblockTurbine and the heat transfer of MultiblockReactor) carried over nearly line for line into ReactorLogic, TurbineLogic and MultiblockReactor of the upstream 1.20 branch, with 38 comments of Big Reactors word for word, and the compiled ReactorLogic.class of this jar holds its constants (-15.0, -0.0025, -0.95, -10.0, -0.0012 and 0.0001, javap of 2026-11-05). Three textures decode to the same pixels as Big Reactors textures: both fuel column fluids, which are byte identical, and the transparent glass. MIT asks that the Big Reactors notice travel with those portions, so the licenses/ file carries it word for word after the notice of Extreme Reactors 2. The jar is unmodified.
