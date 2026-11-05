# Mystical Automation (1.20.1 port by SkyTekx)

Bundled file: `MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://github.com/BlakeBr0/MysticalAutomation
- License: MIT
- Explanation:

  MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar is an unofficial, modified Minecraft 1.20.1 Forge port of Mystical Automation 1.0.8 by BlakeBr0, built from source in this repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/mysticalautomation). Upstream builds the mod only for NeoForge 1.21.1 and newer. The port starts from the branch 1.21 of https://github.com/BlakeBr0/MysticalAutomation at commit 4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d ("Release 1.0.8", the head of that branch, which matches the Modrinth file of 1.0.8). The repository ships an MIT LICENSE ("Copyright (c) 2025 BlakeBr0"), the mods.toml says license="MIT", and the Modrinth project declares MIT. MIT permits modifying and redistributing the software, modpacks included, as long as the copyright and permission notice are kept. The jar carries that notice as LICENSE_mysticalautomation and a NOTICE_mysticalautomation that says the build is unofficial and not endorsed by upstream, names the upstream commit and the changes and points to the source. The port copies or adapts a few classes of Cucumber 8 and takes three tooltip translations from Mystical Agriculture 8, both MIT ("Copyright (c) 2018 BlakeBr0"), and the jar ships their notices as LICENSE-Cucumber and LICENSE-MysticalAgriculture. The textures are upstream's and carry no separate license. The required Cucumber 7.0.16, Mystical Agriculture 7.0.24 and Mystical Agradditions 7.0.12 stay CDN entries of the index and are not bundled.

## Reference

- Redistribution allowed: yes, under MIT with the notices in the jar
- On Modrinth: yes for NeoForge 1.21.1 and newer only (https://modrinth.com/mod/mystical-automation, project wtZsRhqk), so the 1.20.1 build has to be bundled
- Author: BlakeBr0 (original mod), 1.20.1 port by SkyTekx
- Notes: SHA-1 585446d50038e4f254c2fb2b012b616495ff5ed0, 312817 bytes, rebuilt on 2026-11-05 after the integration review (before it d2891fe346f5f6878789385e45742c6583a8d7c7, 308953 bytes). Built with Java 17 from `ports/mysticalautomation/` (`./gradlew build`, ForgeGradle 6.0.54, Forge 47.4.0 userdev, Parchment 2023.06.26). The build takes Cucumber, Mystical Agriculture and Mystical Agradditions from maven.blakesmods.com and checks their SHA-1 against the builds of the index, and two clean builds gave the same jar. `ports/mysticalautomation/UPSTREAM.md` lists every change: the API port to Forge 47 and the Mystical Agriculture 7 API, the 1.20.1 data forms, the fixes of a client message that could set any slot, of the Enchanternator cost, of Crafter and Altarnator results built from ghost items (special recipes and NBT taken from the ghost grid), of a Farmer output duplicate and of two smaller bugs, and the CraftTweaker integration that stays out. The license texts also travel in `licenses/MysticalAutomation-1.20.1-1.0.8-skytekx.1-LICENSE.txt`. MIT has no source duty, the source link is the convention of this repository, and `tools/mrpack_permissions.py` refuses the jar until master on GitHub holds `ports/mysticalautomation/` and this jar. Not published on Modrinth or CurseForge.
