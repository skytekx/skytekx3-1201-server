---
type: History
title: History of SkyTekx
description: The SkyTekx lineage from the 1.7.10 pack of 2019 over SkyTekx 2 on 1.12.2 to SkyTekx 3 on 1.20.1, why the third generation runs on 1.20.1, and how the ts3.party community became ts4.party.
tags: [history, skytekx, lineage, community]
timestamp: 2026-10-31
---

# History of SkyTekx

SkyTekx is a server first and a modpack second. Each generation mixed the tech of Tekkit, the
adventure of Hexxit and the sky islands of SkyFactory on a hybrid server that runs Forge mods and Bukkit
plugins side by side. The owner of this repo is the original author. On 2026-06-26 they asked to
"create an updated version of this modpack, I am the original author" (`~/.claude/history.jsonl`,
session 5bd3e1de).

| Generation | Minecraft | Server software | Years | Mods | Repo |
|---|---|---|---|---|---|
| SkyTekx | 1.7.10 | KCauldron, shipped through Technic | 2019 | 120 by the website count | Repo `skytekx/skytekx-1710-server` |
| SkyTekx 2 | 1.12.2 | Forge 14.23.5.2860 on Mohist 1.12.2-320 | 2023 to 2024 | 209 jars by the audit, 224 by the website badge | Repo `skytekx/skytekx2-1122-server` |
| SkyTekx 3 | 1.20.1 | Forge 47.4.18 on Arclight 1.20.1 1.0.6 | Since 2026 | 217 index entries and 35 bundled jars | Repo `skytekx/skytekx3-1201-server` |

The repo names carry the Minecraft version as a code: 1710, 1122 and 1201. The GitHub organisation
`skytekx` holds all three, see [Related repos and services](related.md).

## SkyTekx on 1.7.10

The website tells it this way: "The first pack lands on Minecraft 1.7.10 as the ts3.party ("ts3pp")
community build, a mix of Tekkit, Hexxit and SkyFactory on a Forge+Bukkit (KCauldron) hybrid. Shipped
through Technic, server-first and RCON-managed by the ts3.party crew" (`~/git/skytekx-web/index.html:461`).

- The repos `skytekx-1710` and `skytekx-1710-server` were created on 2019-02-15 and last pushed on
  2019-05-28. A backup of the community server, `skytekx-1710-server-ts3pp-backup`, followed on
  2019-04-13, and a private copy `skytekx-1710-server-pro` on 2020-04-30 (GitHub, read 2026-10-31).
- The server README described the pack as "a combination of Tekkit, Hexxit and SkyFactory2.5 in
  Minecraft 1.7.10" and installed it on Debian 8 without `screen`, managed over RCON behind ufw. The
  SkyTekx 2 repo still carries that README (`~/git/skytekx2-1122-server/README.md:1-12`), and the
  self-hosting guide of SkyTekx 3 follows the same pattern.
- The website credits six contributors for this generation by GitHub handle
  (`~/git/skytekx-web/index.html:499-530`).

## SkyTekx 2 on 1.12.2

- The repo `skytekx2-1122-server` was created on 2023-01-03. Its second commit on 2023-01-04 reads
  "WIP: switch from magma to mohist", and its last commit ee2cf4f on 2024-12-25 added a permission
  plugin. It has 70 commits. A private backup repo followed on 2024-12-25.
- The stack was Minecraft 1.12.2, Forge 14.23.5.2860 and Mohist 1.12.2-320 with 209 mod jars. The
  plugin layer was EssentialsX 2.19.7, LuckPerms 5.4.151, Multiverse 2.5, WorldEdit 6.1.9 and the
  skyblock plugin SB-Skylands 0.19 (memory `project_skytekx2_modpack.md:12`, audit
  `MODPACK_UPDATE_AUDIT.md`).
- The theme name was "Party Hexxit Deluxe", which survives in the SkyTekx 3 message of the day "A Party
  Hexxit Deluxe Server" (`server.properties.example`).
- Recipes stayed free of conflicts through three layers: UniDict 3.0.10 for one item per material,
  NoMoreRecipeConflict 0.13 for grid conflicts and CraftTweaker 4.1.20 scripts. SkyTekx 3 rebuilt the
  same three layers, see [Modpack](modpack/index.md#recipe-layers).
- The skyblock part was SB-Skylands, a Bukkit plugin for 1.12 with no port. SkyTekx 3 replaced it with
  its own dimension, see [Skylands worldgen](content/skylands-worldgen.md).
- The website credits four contributors for this generation by GitHub handle.

## Why SkyTekx 3 runs on 1.20.1

On 2026-06-26 the first build session audited SkyTekx 2 and wrote four reports into the SkyTekx 2
checkout, where they are still untracked (see [Related repos and services](related.md#skytekx-2-audit-files)).

- A refresh on 1.12.2 was possible: 45 of the 209 mods had a newer 1.12.2 build, and Mohist 1.12.2-347
  was a drop-in swap (`MODPACK_UPDATE_AUDIT.md`). The audit recommended that path (memory
  `project_skytekx2_modpack.md:21`).
- A port to 1.21 would keep 154 of 209 mods in some form, 97 as the same mod or a direct successor, and
  it would lose the whole Tinkers' Construct stack (`PORT_1_21_MAP.md`). The owner objected at 17:58
  that Tinkers' Construct would be lost and that Silent Gear is no real alternative.
- A port to 1.20.1 would keep 170 of 209 mods, 128 as the same mod or a direct successor. Tinkers'
  Construct, Botania, the Thermal mods, CoFH Core, Quark and Mantle all exist there directly
  (`PORT_1_20_1_MAP.md`).
- MohistMC's new hybrid Youer only supports 1.21.1 and later (memory `project_skytekx2_modpack.md:16`).
  For Forge 1.20.1 with Bukkit plugins the session chose Arclight, after the owner asked on 2026-06-26
  22:18 how Arclight compares with Mohist and on 2026-06-27 10:14 whether Arclight is a good option.

On 2026-06-27 at 10:22 a fourth report suggested 65 additions taken from Hexxit Revived and Tekxit 4,
and 42 of them are in pack 3.0.5. At 10:34 the owner set the goal for the new pack: it should feel as
much like SkyTekx as possible, run on the most stable mod configuration, keep the old mods and add a few
new ones, and never let recipes conflict. The build started the same day in this repo, and memory
records the decision as "chose the 1.20.1 port path" (`project_skytekx2_modpack.md:25`).

## SkyTekx 3 on 1.20.1

The day by day story from 2026-06-26 to 2026-07-12 is the [Change log](log.md). In short:

- From 2026-06-27 to 2026-06-30 the index grew from 109 Modrinth entries in pack 3.0.0 to 216 entries,
  joined by about 30 bundled jars. Those days brought the Skylands dimension, the Nyx port, the 1.7 style
  combat, the boot error cleanup and the hardened Arclight jar.
- The website skytekx.com went live on 2026-06-29 and got its last change on 2026-06-30.
- On 2026-07-12 the Skylands generator became a pure datapack that also runs on unmodded Spigot, and
  pack 3.0.5 was built.
- No session touched the project between 2026-07-13 and 2026-10-31 (scans of the prompt history and of
  the surviving transcripts). On 2026-10-31 the owner asked to
  move the server to an Apple M4 Mac mini with 16 GB, to update the mods and to start a new world with a
  light Halloween theme (`~/.claude/history.jsonl`, session c0e872d8, 16:12 to 16:58). The state of that
  work is on the [OKF index](index.md) and the [Mac mini](server/macos.md) page.

The Modrinth project has never been public, see [Publish](modpack/publish.md).

## From ts3.party to ts4.party

- SkyTekx began as the build of the ts3.party community, a TeamSpeak community. The owner's old
  community website was www.erebos.xyz, of which the Wayback Machine keeps a copy from 2020-11-06
  (owner, 2026-06-29 14:46). Its Nyan Cat and Rainbow Dash art and the developer pictures went onto the
  new website.
- The project contact address is on that old domain, `skytekx@erebos.xyz`, not office@netsnek.com
  (owner, 2026-06-27 15:09). The build tools send it in their HTTP user agent
  (`tools/resolve_all.py:9`, `tools/publish_modrinth.py:13`).
- The community is now ts4.party. On 2026-06-29 the owner ruled that SkyTekx is "a modpack by
  ts4.party, the TeamSpeak for party, formerly known as ts3.party", not a modpack by a person, and that
  the credits carry no real names (10:00, 10:10, 10:24). The website footer adds "Supported by
  netsnek.com" (14:46).
- The website timeline ends with "Today: Home on ts4.party".

## Names

| Name | Meaning |
|---|---|
| SkyTekx, SkyTekx 2, SkyTekx 3 | The three generations. The `.mrpack` files and the Modrinth project use "SkyTekx3" and `skytekx3` |
| Tagline "Hexxit × Tekkit × SkyFactory" | The website subtitle since the owner's ruling of 2026-06-30 04:54 |
| Message of the day "A Party Hexxit Deluxe Server" | Inherited from SkyTekx 2 |
| Dimension `skytekx3:skylands` | The floating islands that replaced SB-Skylands |
| Mod id `skytekx` | The pack's own Forge mod, first skytekx-worldgen, now SkyTekx Compat |
| Logo | The owner named the file `skytekx.svg` as the official logo on 2026-06-29 09:32. The main menu and the website show the same PNG logo, whose origin is not recorded |

Related: [OKF index](index.md), [Change log](log.md), [Decisions](decisions.md),
[Related repos and services](related.md).
