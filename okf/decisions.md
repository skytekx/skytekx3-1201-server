---
type: Decisions
title: Decisions
description: Every owner ruling on SkyTekx 3 from 2026-06-26 to 2026-10-31, grouped by area, with its status on 2026-10-31 and the page or file where it lives.
tags: [decisions, rulings, owner]
timestamp: 2026-11-03
---

# Decisions

This page lists what the owner asked for or ruled, when, and what became of it. The rulings come from
the owner's prompts in `~/.claude/history.jsonl`: session 5bd3e1de from 2026-06-26 to 2026-07-12,
session 3dcdf872 for one hosting question and session c0e872d8 on 2026-10-31. Prompts the owner wrote in
German are given in English. Times are local time on the build machine. The story around the rulings is
in the [Change log](log.md).

| Status | Meaning |
|---|---|
| Done | Built and present in the repo or on the server |
| Partial | Built in part. The row names what is missing |
| Open | Not built. The row is also on [Open items](open-items.md) |
| In progress | Work of 2026-10-31 that is not finished |
| Superseded | Replaced by a later ruling or dropped |
| Unknown | The outcome is not recorded |

## Standing rules

These rules apply to every later change.

- Decisions that improve speed and reliability win (2026-06-30 01:20). The memory note on this rule
  (`skytekx3-performance-priority.md`) adds that spacing and tag edits are safe to make, while deleting
  generated content or removing a whole mod needs the owner's confirmation.
- The pack should feel like SkyTekx, run on the most stable configuration, keep the old mods with a few
  new ones, and its recipes must never conflict (2026-06-27 10:34).
- Mods come from Modrinth first, then from GitHub, and from CurseForge only as a last resort. Players
  must not need CurseForge (2026-06-27 17:29, repeated 2026-06-29 12:26, 14:11 and 15:11).
- Before a feature is made optional, check whether a mod that provides it can be installed (2026-06-30
  05:49).
- No structure may stand on the Y 0 floor above the void (2026-06-29 08:26 to 09:12, again 2026-06-30
  04:06 and 2026-07-12 20:36).
- The Skylands generator must also work on an unmodded Spigot server (2026-07-12 20:39).
- SkyTekx is "a modpack by ts4.party, the TeamSpeak for party, formerly known as ts3.party", and the
  credits carry no real names (2026-06-29 10:00, 10:10 and 10:24).
- Commit messages carry no dashes and no semicolons (2026-06-29 22:14). The website follows the same
  rule in English and German (2026-06-30 04:24).
- AGENTS.md and the wiki do not name the owner as commit author, because other people may write code
  with an agent (2026-06-29 18:46 and 18:47).

## Scope and platform

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-26 15:21 | Build an updated version of SkyTekx 2, of which the owner is the original author | Done. Packs 3.0.0 to 3.0.5 came from this repo | [History](history.md) |
| 2026-06-26 17:58 | Keep Tinkers' Construct. Silent Gear is no real alternative | Done. Tinkers' Construct and its add-ons are in the index | [Mod set](modpack/mod-set.md) |
| 2026-06-26 22:14 to 2026-06-27 10:14 | Choose between 1.20.1 and 1.21 and between Arclight and Mohist | Done. Minecraft 1.20.1 with Forge 47 on Arclight | [History](history.md#why-skytekx-3-runs-on-1201) |
| 2026-06-27 10:34 | Feel like SkyTekx, take the most stable configuration, keep the old mods and add a few, never let recipes conflict, test performance with debug mods, check SB-Skylands | Partial. The recipe layers stand, but the port of the SkyTekx 2 CraftTweaker scripts is unfinished. SB-Skylands has no port and was replaced by an own dimension | [Modpack](modpack/index.md#recipe-layers), [KubeJS](modpack/kubejs.md) |
| 2026-06-27 10:55 | Start a new repo in `~/git` | Done. GitHub repo `skytekx/skytekx3-1201-server` | [Related repos and services](related.md#this-repo-on-github) |
| 2026-06-27 17:20 | Build for the newest 1.20.x instead of 1.20.1 | Superseded. The pack stayed on 1.20.1, and no later prompt asks again | File `modrinth.index.json` |
| 2026-06-27 17:25 | Consider NeoForge, which Arclight also supports | Superseded. The server stayed on Forge 47 | [Arclight runtime](server/arclight.md) |
| 2026-06-29 21:27 and 21:32 | Upgrade Arclight, or harden it with backports from newer versions | Done. The hardened Arclight 1.0.6 jar of commit 88552a9 | [Arclight runtime](server/arclight.md#the-hardened-jar) |
| 2026-06-29 23:08 | Try a migration to Sponge | Superseded. Sponge has no build for Forge 1.20.1 | [Change log](log.md#2026-06-29-evening-git-history-docs-and-the-hardened-arclight) |
| 2026-07-12 20:39 | Find a more elegant generator that also works on an unmodded Spigot server | Done. A pure datapack since commits 174fbf6, 4da4ee3 and dc8b470. The logs of the Spigot test were not kept | [Skylands worldgen](content/skylands-worldgen.md) |

## Identity and credits

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-27 15:09 | The project contact address is `skytekx@erebos.xyz`, not office@netsnek.com | Done. It is the contact in the user agent of `tools/resolve_all.py:9` and `tools/publish_modrinth.py:13` | [History](history.md#from-ts3party-to-ts4party) |
| 2026-06-29 10:00 and 10:10 | Credit SkyTekx as "a modpack by ts4.party, the TeamSpeak for party, formerly known as ts3.party", with netsnek.com as supporter (worded "Supported by" at 14:46) | Partial. The website footer and credits, the README and since 2026-10-31 21:25 the Modrinth texts follow it. Two lead texts on the website still name the ts3.party community | [Related repos and services](related.md#claims-on-the-site-that-are-wrong-on-2026-10-31) |
| 2026-06-29 10:24 | No real names in the credits | Done on 2026-11-02. Nyx 1.0.1 credits the port to SkyTekx, and `modrinth-permissions/nyx-1.20.1-1.0.1.jar.md` names no person | [Nyx](content/nyx.md#open-points) |
| 2026-06-29 18:46 and 18:47 | AGENTS.md and the wiki must not name the owner as commit author | Done | [AGENTS.md](../AGENTS.md) |
| 2026-06-30 04:54 | The website subtitle reads "Hexxit × Tekkit × SkyFactory" | Done. The README title follows it since 2026-10-31 | [History](history.md#names) |

## Mod set and sourcing

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-27 17:29 | Build Twilight Forest from source, and look for GitHub builds of mods that are only on CurseForge | Done. Twilight Forest is a bundled jar in `local-mods/` | [Mod set](modpack/mod-set.md#bundled-jars) |
| 2026-06-27 17:29, repeated 2026-06-29 | Take mods from Modrinth first, then GitHub, CurseForge only as a last resort | Partial. All index files come from the Modrinth CDN since 74faf22, 217 of them since ca131c3. BrassAmber BattleTowers runs from a CurseForge jar outside the pack, and since 2026-10-31 21:48 players add it by hand from CurseForge | [Mod set](modpack/mod-set.md) |
| 2026-06-28 23:15 and 23:18 | Bring back chocobos, a core mod of SkyTekx | Done. ChocoCraft is in the index | [Skylands worldgen](content/skylands-worldgen.md#chocobos) |
| 2026-06-29 00:33 | No Chance Cubes, although the original pack had them | Done | [Mod set](modpack/mod-set.md) |
| 2026-06-29 05:39 | Add Mekanism Generators | Done | File `modrinth.index.json` |
| 2026-06-29 06:25 | Patch Cataclysm Spellbooks instead of Cataclysm, in a fork of the archived repo in the skytekx organisation | Done. The fork `skytekx/Cataclysm_Spellbooks` holds the port, and the pack bundles its build since 507a8f2. The fork has no release | [Mod set](modpack/mod-set.md#jars-outside-the-index) |
| 2026-06-29 11:52 and 12:00 | Battle Towers are essential, and BrassAmber BattleTowers is the successor | Partial. The jar sits only in the server's `mods/`. It is All Rights Reserved and only on CurseForge, and in the install test of 2026-10-31 a client without it could not join. Since 21:48 that day players add it by hand | [Mod set](modpack/mod-set.md#jars-outside-the-index) |
| 2026-06-29 12:05 and 12:26 | Search as deep as possible for the old mods or their successors | Done as a best effort. A batch of 69 jars arrived at 12:47 | [Mod set](modpack/mod-set.md) |
| 2026-06-29 13:46 and 14:11 | Add the Aether, Hexxit Additions, the Aether add-ons of Better Minecraft and XP Tomes | Done. Hexxit Additions is one of the six permission blockers | [Publish](modpack/publish.md#what-blocks-a-release) |
| 2026-06-29 14:23 and 15:02 | Fork Nyx and port it completely, for client and server | Done. The fork has no release, so the pack bundles the jar and the index entry with the dead link is gone since 74faf22 | [Nyx](content/nyx.md) |
| 2026-06-29 15:02 | Take only the essentials of the Better Minecraft pack BMC4 for now | Done with the batch of 15:15. VanillaBackport stopped the boot at 15:19 and was parked | [Mod set](modpack/mod-set.md#parked-and-removed-mods) |
| 2026-06-30 00:23 to 00:46 | Drop the Ruins mod, salvage its flying structures first | Done in ac261f1. The Ruins balloon had become a self-contained vanilla structure in 8afbfe1 and stayed | [Performance](server/performance.md#skylands-structure-density) |
| 2026-06-30 05:49 | Install the mod that provides a feature before making the feature optional | Done. SlashBlade Resharped, Re:Avaritia, Medieval Embroidery, The One Probe and Aether Genesis came in | [Boot fixes](content/boot-fixes.md) |
| 2026-06-30 08:05 | Remove Re:Avaritia, it does nothing | Done in bd5f18b | [Mod set](modpack/mod-set.md#parked-and-removed-mods) |
| 2026-10-31 16:58 | Check the installed mods for updates | Done. 55 Modrinth updates, one new dependency and three bundled jars landed in 092140d and 4917ab8. The updated set boots on the Mac mini with the baseline of 3 ERRORs, and 12 of the new builds are betas. The JEI build 15.74.0.233 breaks EMI's JEI bridge, and a client with it ran out of an 8 GB heap in the install test | [Mod set](modpack/mod-set.md#update-of-2026-10-31) |
| 2026-10-31 21:48 | Keep BrassAmber BattleTowers on the server and let players add the jar by hand | Done. The Modrinth body says so since about 21:56. At about 21:50 the owner asked for the jar to send it to players over Telegram, and it was copied for them | [Mod set](modpack/mod-set.md#jars-outside-the-index) |
| 2026-11-03 09:54 and 11:05 | Fork EMI, fix all of its errors, push it to the skytekx org, make the fix the default branch and install it with restarts | Done. github.com/skytekx/emi, branch `1.20.1-jei-bridge` up to e4ae7350, the default branch since about 11:08. The jar ships from `local-mods/` since 1ea5bb29 and runs on the Mac mini and both MacBooks since 11:13 | [Mod set](modpack/mod-set.md#bundled-jars) |

## Skylands worldgen and travel

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-27 12:49 | Build the skyblock generator | Done. The dimension `skytekx3:skylands` | [Skylands worldgen](content/skylands-worldgen.md) |
| 2026-06-27 21:41 | Islands less high and less stacked | Superseded by the rulings of 2026-06-28 | [Skylands worldgen](content/skylands-worldgen.md#owner-requirements) |
| 2026-06-28 18:33 | Smaller islands a little further apart so players can build bridges, no flat tops, a different biome per island | Done | [Skylands worldgen](content/skylands-worldgen.md#owner-requirements) |
| 2026-06-28 19:58 | No lava or water falling into the void, and fewer, somewhat bigger islands with much more distance | Done on 2026-06-28. No look in game is recorded after the rework of 2026-07-12 | [Skylands worldgen](content/skylands-worldgen.md#terrain) |
| 2026-06-28 20:13 and 20:35 | Contained liquids where they would naturally spawn, and small lakes that never leak | Done on 2026-06-28, with the same caveat | [Skylands worldgen](content/skylands-worldgen.md#terrain) |
| 2026-06-28 22:30, repeated up to 2026-06-30 03:57 | Mod structures in the Skylands, floating ones, ground and underground ones on big islands, and common friendly ones | Done. Floaters at spacing 32, the island ground set at 14 and villages at 24 | [Skylands worldgen](content/skylands-worldgen.md#structure-sets) |
| 2026-06-29 08:26 to 09:12, again 2026-06-30 04:06 and 2026-07-12 20:36 | No structure on the Y 0 floor above the void | Done. Vanilla `sky_void` biome gating since 2026-07-12. Since 2026-10-31 (1f8f4ef) Battle Towers, strongholds and the illager campsite also stay off the islands | [Skylands worldgen](content/skylands-worldgen.md#no-structures-over-the-void) |
| 2026-06-29 11:40 | No Battle Towers with the explosion golem in the Skylands | Done for new chunks by 1f8f4ef on 2026-10-31. The world copies of the datapacks on the build machine are not synced | [Skylands worldgen](content/skylands-worldgen.md#changes-of-2026-10-31) |
| 2026-06-29 11:40 | No mob explosion damage to blocks in the Skylands, so buildings stay intact | Built on 2026-11-01 in skytekx-compat 2.1.4 (server only). It denies mob griefing and clears the block list of every explosion in the Skylands | [Skylands worldgen](content/skylands-worldgen.md#open-rulings) |
| 2026-06-29 16:05, 19:19 and 19:33 | Fall from the Skylands into the Overworld like in the Aether | Done. Datapack functions since 2026-07-12, not tested in game since then | [Skylands travel](content/aether-fall.md) |
| 2026-06-29 19:33 | The fall must also work in singleplayer | Partial. The functions are vanilla, but the `.mrpack` does not put the datapacks into singleplayer saves | [Skylands travel](content/aether-fall.md#open-points) |
| 2026-06-29 19:30 | Generate the structure at Overworld x 0, y 110, z 451 in the Skylands too, and more flying structures | Partial. More floaters came, the structure itself was never identified | [Skylands worldgen](content/skylands-worldgen.md#open-rulings) |
| 2026-06-29 17:04 | Regenerate every dimension with Nyx in place | Done. Full regen on 2026-06-29, Skylands wipe and pregen on 2026-07-12 | [World reset and pregen](server/world-reset.md) |
| 2026-06-30 03:48 | Structures cut from the Skylands keep generating in their own dimensions | Done | [Performance](server/performance.md#what-was-decided) |
| 2026-06-30 03:55 and 03:57 | Chocobos and friendly structures such as villages and adventure houses common in the Skylands | Partial. Spacing and spawn weight went up (801edb4, daba422), but in the current world ChocoCraft's own spawn data wins | [Skylands worldgen](content/skylands-worldgen.md#known-defect-the-chococraft-overrides-lose) |
| 2026-06-30 04:01 | Chocobos and everything else from the chocobo mod only in the Skylands | Partial. Same defect. A fresh world loads the overrides after the mod packs, which should fix it, but nobody checked in game | [Skylands worldgen](content/skylands-worldgen.md#known-defect-the-chococraft-overrides-lose) |
| 2026-06-30 04:24, restated 2026-10-31 16:58 | Climbing above the Overworld build limit puts the player on a dirt block at Y 0 in the Skylands, as the main way to switch instead of `/trigger` | Done (2eceeb1, datapack functions since 2026-07-12). Not tested in game since 2026-07-12 | [Skylands travel](content/aether-fall.md) |
| 2026-10-31 16:56 | A new map with a light Halloween theme instead of the generated one | Done. The Halloween world was created on the Mac mini at 18:07 with pumpkin and jack o'lantern patches (5ec6b72), a Halloween server icon (f92aee3) and Nyx blood moons in the Skylands at chance 0.5 (a00ecce). Its Skylands were generated again after 0cf350a and 7b51eb5 | [Skylands worldgen](content/skylands-worldgen.md#changes-of-2026-10-31), [Mac mini](server/macos.md) |
| 2026-10-31 16:57 | Nothing may be placed between the islands, check the code | Done. The code check and scans of the new world led to 1f8f4ef (Battle Towers, strongholds, illager campsite), 0cf350a (petrified roots) and 7b51eb5 (windmill fields). The June terrain of 23:01 brought villages, merchant camps and meteorites back into the void, which the owner reported at 23:10. skytekx-compat 2.1.0 and a biome gate that follows the terrain closely fixed that, see the row of 23:10 | [Skylands worldgen](content/skylands-worldgen.md#changes-of-2026-10-31) |
| 2026-10-31 22:18 and 22:22 | The islands of June back: the fix of July made them far bigger, farther apart and flatter, and a fix must not change size, shape or spacing | Done at 23:01 (9a4dc12, cb91870, 9af7fae) with a wipe of the Skylands of the Halloween world | [Skylands worldgen](content/skylands-worldgen.md#terrain) |
| 2026-10-31 23:10 | Nothing in the void, especially no flowers (reported with two screenshots) | Done. skytekx-compat 2.1.0 keeps jigsaw children, terrain matching blocks, starts and AE2 meteorites off the void, the biome gate follows the terrain. Live since 2026-11-01 00:58, when the Skylands were generated again to radius 2000 and a scan found no block below Y 13. Since 02:51 the server runs version 2.1.2 or later, which also keeps swamp hut stilts and the sky stone of meteorites out of the void | [Skylands worldgen](content/skylands-worldgen.md#the-void-floor), [SkyTekx Compat](content/compat-mod.md#skylands-void-floor) |

## Gameplay

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-27 20:58 | Operator rights for the owner on the local server | Done in `ops.json`, which git ignores | [Deploy on Debian](server/deploy.md#what-git-does-not-hold) |
| 2026-06-27 21:41 | Short commands like `/gm` | Done with EssentialsX 2.22.0, whose jar and folder git does not track | [Plugins](modpack/plugins.md) |
| 2026-06-27 21:46 | Loot chances roughly like SkyTekx 2, not three Hexxit flowers side by side | Open | [KubeJS](modpack/kubejs.md#open-rulings) |
| 2026-06-28 22:55 | Every advancement shows its real name | Partial. A resource pack with 66 Spartan Weaponry names exists, untracked, unshipped and not enabled | [Client](modpack/client.md#resource-packs) |
| 2026-06-29 12:10 | Sword swing like in 1.7 | Partial. The attack cooldown is gone on the server. The old swing animation depends on Nostalgic Tweaks, which each client may or may not have | [Combat](content/combat.md) |
| 2026-06-30 04:15 | Resolve every error and warning at server boot | Partial. Boot ERRORs went from 152 to 3 accepted ones. WARNs were not cleared, with 303 to 331 per boot on the build machine and 344 to 387 on the Mac mini after the first boot of its new world | [Boot fixes](content/boot-fixes.md) |
| 2026-10-31 23:10 | A `/spawn` command | Done with EssentialsXSpawn 2.22.0 on the Mac mini since 2026-11-01 00:58. Respawns and joins stay vanilla. `/spawn` leads to the world spawn until an operator runs `/setspawn` | [Plugins](modpack/plugins.md#spawn) |
| 2026-11-01 01:34 | Placeable Items out, after torches landed as "Placed Block" mobs and placed items rendered as the missing texture | Done. The Mac mini runs without it since 01:39, and 787cb1f drops it from the index. A client that still has it cannot join, so players disable it until a pack without it is out | [Mod set](modpack/mod-set.md#history) |
| 2026-11-01 03:54 | Sophisticated Backpacks out and the simple backpack mod of SkyTekx2 back, Eydamos Backpack with tanned leather, without a back slot, ported by us if no 1.20.1 build exists. The server, the owner's laptop and the second MacBook switch together | Done. No 1.20.1 build or remake exists, so Eydamos Backpack 2.0.1 was ported to Forge 1.20.1 as `backpack-1.20.1-2.0.1.1.jar` (LGPL-2.1, local repo `skytekx-backpack`). The Mac mini runs it instead of Sophisticated Backpacks and Sophisticated Core since 08:09:40. The jar is not in the pack yet | [Mod set](modpack/mod-set.md#backpack) |
| 2026-11-01 05:52 | Get It Together Drops out and Falling Meteors Reimagined in, on the server and both MacBooks, with meteors only in the main dimension and none on the sky islands | Done. The Mac mini runs Falling Meteors 2.5 without Get It Together Drops since 06:11, and skytekx-compat 2.1.3 lets meteors fall only in the overworld. The jar is not in the pack, so other players add it by hand like BattleTowers. The mod's Alien Creepers still spawn on the sky islands | [Mod set](modpack/mod-set.md#jars-outside-the-index), [SkyTekx Compat](content/compat-mod.md#falling-meteors-only-in-the-overworld) |
| 2026-11-01 19:19 | For Falling Meteors Reimagined the license in the jar counts, LGPL 2.1 or later from `META-INF/mods.toml`, not the "All Rights Reserved" of the CurseForge project | Noted. The jar is not on GitHub. Hosting it ourselves under that license is possible, but menoXD has published no source to pass on with it | [Mod set](modpack/mod-set.md#jars-outside-the-index) |
| 2026-11-01 19:28 | Load BattleTowers and Falling Meteors from GitHub instead of a manual download. The owner reports the BrassAmber team's permission for BattleTowers | Done for hosting. Unchanged jars as releases in the public repos `skytekx/battletowers` and `skytekx/falling-meteors-reimagined`, downloads checked by SHA-1. The pack picks them up with 3.0.8 | [Mod set](modpack/mod-set.md#jars-outside-the-index) |
| 2026-11-03 13:15 | All wood makes chests, doors and so on, with the default item where a wood has no own one | Done. 12 fallback recipes with skip tags in `kubejs/data/skytekx3/` since 02dd53bd, live since the datapack reload at 13:32 | [KubeJS](modpack/kubejs.md#wood-fallbacks) |

## Client and main menu

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-27 19:58 | Add the client to the owner's Prism Launcher | Done | [Client](modpack/client.md) |
| 2026-06-27 20:49 to 21:49 | The old SkyTekx start menu with its logo, from the old repo's assets, without a resource pack | Partial. A FancyMenu layout with logo and background ships in `config/`, but the FancyMenu jars are not in the pack and the old buttons were not rebuilt | [Client](modpack/client.md#main-menu) |
| 2026-06-27 21:15 | Start in the normal view a pack user should see, not in a modification mode | Unknown. FancyMenu's customization overlay is off in the repo, which is the likely answer | [Client](modpack/client.md#main-menu) |
| 2026-06-30 13:55 | Fix the client as well, not only the server | Partial. The owner's client was updated by hand and joined on 2026-07-01. The drift between pack and server remains | [Mod set](modpack/mod-set.md#jars-outside-the-index) |

## Publishing

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-27 11:04 | Keep the Modrinth token in an ansible vault | Done. The token was also pasted in clear text and needs rotation | [Secrets](server/secrets.md) |
| 2026-06-27 11:50 and 20:46 | Publish the pack on Modrinth with the token | Partial. Versions 3.0.0 to 3.0.4, 3.0.6 and 3.0.7 are uploaded, but the project is still a draft | [Publish](modpack/publish.md#state-on-2026-10-31) |
| 2026-06-27 18:23 | Do not publish the version with Chocolate Quest Repoured yet | Superseded by "yes pls publish" at 20:46 | [Publish](modpack/publish.md#version-history) |
| 2026-06-29 10:11 | Update the Modrinth description and make the project public | Open. Six All Rights Reserved jars block the review | [Publish](modpack/publish.md#what-blocks-a-release) |
| 2026-06-29 17:04 | Commit everything and publish again | Done. Pack 3.0.4 went up on 2026-06-29 at 17:33 | [Publish](modpack/publish.md#version-history) |
| 2026-06-30 18:20 | A folder with the files the Modrinth permissions page asks for | Done in 62ada38, brought up to date in f4c2093. Six blockers remain | [Publish](modpack/publish.md#what-blocks-a-release) |
| 2026-10-31 20:09 | Update the Modrinth pack and test it | Partial. Version 3.0.6 went up at 20:27 after five builder fixes. It installs, but it needs BrassAmber BattleTowers to join this server and froze with an 8 GB heap after the join. With 12 GB it plays, and the Modrinth texts ask for that since 21:25. Version 3.0.7 of 21:55 only adds Modflared and the server list | [Publish](modpack/publish.md#test-of-2026-10-31) |

## Git and docs

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-29 18:38 | Fix the author address of the first commit and rebuild the history as granular multi-line Commitizen commits | Done. Force-pushed at 18:53. The states before the rewrites are on two local backup branches | [Related repos and services](related.md#this-repo-on-github) |
| 2026-06-29 18:38 | A README with self-hosting steps like the older repos, an AGENTS.md and an OKF wiki for other agents | Done, rebuilt on 2026-10-31 | [README](../README.md), [AGENTS.md](../AGENTS.md), [OKF index](index.md) |
| 2026-06-29 22:11 and 22:30 | A performance report, kept in the OKF and not in `docs/` | Done | [Performance](server/performance.md) |
| 2026-06-29 22:14 | No dashes and no semicolons in commit messages, clean the whole history | Partial. Two spaced hyphens used as dashes remain in the bodies of 5ae7f82 and 573cc25 | [Open items](open-items.md#docs-and-housekeeping) |
| 2026-10-31 16:33 | Read all logs and code and write an OKF | Done with this rebuild | [OKF index](index.md) |

## Website

The website has its own repo, see [Related repos and services](related.md#the-website-skytekxcom). About
60 asks on 2026-06-29 and 2026-06-30 shaped it. These are the rulings that matter beyond the site.

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-29 06:42 | A website for SkyTekx on skytekx.com | Done. Last change on 2026-06-30 18:14 | [Related repos and services](related.md#the-website-skytekxcom) |
| 2026-06-29 09:54 | Say that a public server runs on skytekx.com, explain self-hosting like the old READMEs, link GitHub | Partial. The text is on the site, but skytekx.com has no Minecraft record and port 25565 is closed | [Related repos and services](related.md#claims-on-the-site-that-are-wrong-on-2026-10-31) |
| 2026-06-29 11:02 | Drop the "kitchen sink" phrase | Partial. Gone from the site, the README, the index summary and the scripts since b3ec68c, and from the Modrinth texts since 2026-10-31 21:25. The GitHub repo description still uses it | [Publish](modpack/publish.md#what-blocks-a-release) |
| 2026-06-30 10:13 | Do not ask, deploy straight to Cloudflare, then commit and push | Done for the website work of that day | [Related repos and services](related.md#the-website-skytekxcom) |

## Hosting

| Date | Ruling | Status | Where it lives |
|---|---|---|---|
| 2026-06-26 23:09 | A VPS with a very strong single core and 8 GB of RAM or more | Superseded. No later prompt follows it up, and since 2026-10-31 the Mac mini serves | [Change log](log.md#2026-06-26-audit-of-skytekx-2-and-the-choice-of-1201) |
| 2026-06-27 12:07 to 12:21 | Find out whether an M4 Mac mini with 16 GB carries 20 players | Partial. The 11 GB heap of `ServerLinux.sh` was sized for that machine. Since 2026-10-31 the server runs there with a 9G heap and stayed at about 10.6 GB resident without swap during the pregens. Load with 20 players was never measured | [Performance](server/performance.md#heap-and-memory) |
| 2026-07-12 18:04 | Start the server on the build machine | Done. Runs from 18:11 and 23:25 | [Logs and crashes](server/logs-and-crashes.md) |
| 2026-10-31 16:02 | Turn off and disable everything on the Mac mini that is not essential and uses CPU or RAM | Done. Third party agents and login items, Spotlight, Siri, media analysis, location services and Bluetooth are off | [Mac mini](server/macos.md#host) |
| 2026-10-31 16:12 | The Mac mini becomes a Minecraft server for the time being | Done. It runs the server under launchd since 2026-10-31 18:54 | [Mac mini](server/macos.md) |
| 2026-10-31 16:33 and 16:34 | Finalize, test and deploy on the Mac mini. A server version exists already, so optimize everything for the Mac mini | Done. Deployed under launchd with the RCON guard and a daily backup. Restart, stop, start, backup and a reboot were tested between 19:26 and 19:36. Public access came at 21:13 through a Cloudflare Tunnel, since the uplink allows no port forward | [Mac mini](server/macos.md#tests-of-2026-10-31) |
| 2026-10-31 20:58, 21:08 and 21:09 | A public address over a Cloudflare Tunnel, `hex.skytekx.com`, as fast as possible, because players are waiting | Done at 21:13. Players need the client mod Modflared, which pack 3.0.7 brings since 21:55, and voice chat stays in the LAN | [Mac mini](server/macos.md#public-access) |
| 2026-10-31 20:56 and 21:17 | Install Prism and the pack on the second MacBook as well | Done at 21:31 and set up to join through the tunnel. Its files already match 3.0.7 plus BattleTowers. The sign-in and the first launch are left to the person at that Mac, so nobody has joined from it yet | [Client](modpack/client.md#second-mac) |
| 2026-10-31 21:48 | Let players in with a pack 3.0.7 that brings Modflared and lists `hex.skytekx.com`, instead of a relay on an owner server or playit.gg. Voice chat stays off the internet | Done. Built at 21:54 and uploaded to the Modrinth draft at 21:55 as `LRkF99Pz`. Nobody has joined through Modflared yet | [Publish](modpack/publish.md#version-history), [Mac mini](server/macos.md#public-access) |
| 2026-10-31 21:48 | Keep the whitelist off now that the address is public | Done. The whitelist was already off and stays off | [Server](server/index.md#facts-at-a-glance) |
| 2026-10-31 17:47 | Use the Mac mini for computation to speed up the work | Done. The checks behind 1f8f4ef, both Skylands pregens and the region scans ran there | [Mac mini](server/macos.md) |
| 2026-11-01 21:15 | The Skylands stay at noon all the time, with no mob griefing and no explosion damage to blocks there, done on the server only | Built. Dimension type `fixed_time` 6000 and skytekx-compat 2.1.4 on the server, clients unchanged | [Skylands worldgen](content/skylands-worldgen.md) |
| 2026-11-02 13:12 | Whatever a diamond tool mines in vanilla stays mineable with it, and new blocks may ask for a higher tier | Done at 13:22. Nyx 1.0.1 gives the meteor tier its own empty tag, and all 1003 vanilla blocks passed the diamond tier check on the live server | [Nyx](content/nyx.md#history) |
| 2026-11-02 13:11 | It never rains in the Skylands | Done at 13:13. None of the 15 Skylands biomes has precipitation (425d6113) | [Skylands worldgen](content/skylands-worldgen.md#biomes) |
| 2026-11-02 13:39, 13:48, 13:51 and 14:16 | Mobs in the Skylands are weaker than in the overworld, because everybody builds a home there. At 13:48 no hostile mobs there at all, at 13:51 levels 1 to 5 would be fine but not level 50, at 14:16 weak mobs rather than none | Done. No hostile mobs from 13:51 to 14:26, then weak ones: an L2Hostility difficulty file keeps them at levels 0 to 3 near the homes, and since 14:28 no Infernal Mobs elites spawn there. The file itself never loaded until 18:28, so from the restart of 17:20 to then mobs there had the default levels again | [Skylands worldgen](content/skylands-worldgen.md#sleep-and-hostile-mobs) |
| 2026-11-02 13:42, 13:48, 14:19, 14:38, 14:47, 17:13, 17:46 and 19:14 | Give back the inventory lost in the deaths at a Skylands bed, three times in lava and once to a mosquito, at 14:47 with the obsidian farmed before the death. At 17:46 also that of a second player who died in lava. At 19:14 the items and the gold of four deaths in the overworld | Done at 13:43 and 13:52 from the playerdata save of 13:32, at 14:20 from the save of 14:17:31, at 14:39 from the save of 14:32:31, at 14:50 from the save of 14:42:31 with 12 obsidian, at 17:15 from the save of 17:07:34 and at 19:15 from the save of 19:08:22, each written while the player was kicked. The second player's inventory came back at 17:47 from the save of 17:43:20. At 19:25 commands gave the ores mined after 19:08:22. No save held gold. Checked after the rejoins at 13:53, 14:22, 14:40, 14:51, 17:15, 17:49 and 19:17 | [Change log](log.md#2026-11-02-the-twilight-forest-backport-goes-live) |
| 2026-11-01 20:11, 2026-11-02 13:31, 13:45, 14:04 and 14:07 | Sleeping in the Skylands turns night into day in the overworld | Done. Live at 14:10 and seen working at 15:11, when one of two players in the Skylands slept and the overworld went to the next morning. `kubejs/server_scripts/skylands_sleep.js` moves the overworld, and `playersSleepingPercentage` went from 100 to 0, so one sleeping player is enough | [Skylands worldgen](content/skylands-worldgen.md#sleep-and-hostile-mobs) |
| 2026-11-02 14:54 and 14:59 | Nether portals work in the Skylands | Live at 15:05 and used right away: a portal opened in the Skylands at 15:06, and at 15:07 the owner went back from the nether to the Skylands. `kubejs/server_scripts/skylands_portals.js` opens frames there, and a player who came from the Skylands goes back there from the nether | [Skylands worldgen](content/skylands-worldgen.md#nether-portals) |
| 2026-11-02 16:17 and 17:09 | One item per material, one that builds everything. At 17:09 the owner approved an inventory converter, the yellorium fix and one ore per metal in new chunks | Done. `kubejs/server_scripts/unify_inventory.js` swaps other variants for the preferred item since 17:16, yellorium is its own material again since 17:23, the duplicate ores of Railcraft and Immersive Engineering are off since 17:20 and those of Mekanism since 17:33. Chunks generated before keep their ores, and four limits remain | [AlmostUnified](modpack/almostunified.md#one-item-per-material) |
| 2026-11-02 19:14 | Install or port the grave mod that every SkyTekx had so far | Done at 19:35 with the GraveStone Mod 1.0.35 from Modrinth, required on server and clients, on the Mac mini and both MacBooks. SkyTekx 2 had made its graves with OpenBlocks, which has no 1.20.1 build | [Graves](content/graves.md) |
| 2026-11-03 11:05 | Fix the dropped items that show on the ground but cannot be picked up | Done. `spigot.yml` sets `entity-activation-range.misc: 0` since 3a671f08, live since the restart at 11:13 | [Arclight](server/arclight.md#floating-items) |

Related: [OKF index](index.md), [Open items](open-items.md), [Change log](log.md),
[History](history.md).
