# SkyTekx DE Fixes

Bundled file: `skytekx_defixes-1.0.0.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/skytekx-defixes
- License: MIT for own code, portions LicenseRef-CoFH-Dont-Be-a-Jerk
- Explanation:

  SkyTekx DE Fixes (skytekx_defixes-1.0.0.jar) is a small Forge 1.20.1 addon written for this pack by its author (SkyTekx). It fixes bugs of the unmodified Draconic Evolution 3.1.2.621 and Brandon's Core 3.2.1.302 jars, which the pack downloads from Modrinth, through Mixin, after the fixes of their 1.21 branches. It contains no Draconic Evolution or Brandon's Core class and no data file of theirs. Its own code is MIT licensed. A few portions reproduce upstream statements (one block properties chain and the values or checks of nine small mixin handlers, 11 of its 31 Java statements), stay Copyright (c) 2013-2023 brandon3055 and are used under the "Copy portions of this code for use in other projects" and "Write your own code that uses this code as a dependency (addon mods!)" grants of the "Don't Be a Jerk" license of Draconic Evolution. The jar carries LICENSE_skytekx_defixes, which gives the MIT text and lists those portions as not MIT, and NOTICE_skytekx_defixes, which credits brandon3055 and the Draconic-Inc contributors, links each upstream commit and gives the count. Its full source is public as that license requires. As the mod author and the modpack author are the same, redistribution in the modpack is permitted.

## Reference

- Redistribution allowed: yes, once master carries its source (see Notes)
- On Modrinth: no
- Author: SkyTekx (Florian Kleber). The upstream fixes it follows come from the Draconic-Inc contributors, on the 1.21 branches of Draconic-Inc
- Notes: Built from `ports/skytekx-defixes/` in this repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/skytekx-defixes), `./gradlew build` with Java 17. The build fetches Draconic Evolution 3.1.2.621, Brandon's Core 3.2.1.302 and CodeChicken Lib 4.4.0.528 from the Modrinth CDN and checks their SHA-1 against the pack's index, and the jar is byte-reproducible: SHA-1 403e01aacc5b78e9d74ea961c4974ca68d60838b, 21305 bytes. Its mods.toml declares license="MIT, with portions under the Draconic Evolution Don't Be a Jerk license, see NOTICE_skytekx_defixes". It requires `draconicevolution` [3.1.2.621] and `brandonscore` [3.2.1.302] on both sides, and its network channel `skytekx_defixes:main`, which carries no messages, refuses a peer without the mod. The pack applies two more upstream fixes, the Chaotic Shield Capacity recipe and the vanilla tool tags, in its own KubeJS scripts, not in this jar. The license text also travels in `licenses/skytekx_defixes-1.0.0-LICENSE.txt`. The license of Draconic Evolution demands a visible repository of the source, so the jar may only be distributed once master on GitHub holds `ports/skytekx-defixes/` and this jar. `tools/mrpack_permissions.py` refuses it until then. Not published on Modrinth or CurseForge.
