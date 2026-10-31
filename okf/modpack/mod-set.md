---
type: Reference
title: Mod set
description: What SkyTekx 3 is made of, how the Modrinth index, the bundled jars and the server's mods folder relate, which jars sit outside the pack, and how to change the set.
tags: [modpack, mods, modrinth]
timestamp: 2026-10-31
---

# Mod set

SkyTekx 3 is a Forge 1.20.1 pack. Its mods reach players and the server in two ways: as entries of
the Modrinth index, which launchers download, and as bundled jars that ride inside the `.mrpack`. The
server runs whatever is in its `mods/` folder, which is neither of the two exactly.

| Item | Value | Evidence |
|---|---|---|
| Pack version | 3.0.6 | Field `versionId` in `modrinth.index.json`, set in commit 3860591 |
| Minecraft | 1.20.1 | Index `dependencies` |
| Forge in the index | 47.4.6 | Index `dependencies`, `tools/build_mrpack.py:12` |
| Forge at runtime | 47.4.18, on the server (Arclight) and in the owner's client | Server crash reports, Prism `mmc-pack.json` |
| Index entries | 217 since commit 092140d, of which 216 download from `cdn.modrinth.com` and 1 from GitHub (Nyx). 216 before | File `modrinth.index.json` |
| Size of the index downloads | 952,759,925 bytes, 933,046,213 before commit 092140d | Sum of `fileSize` |
| Bundled jars | 33 in `local-mods/` | Tracked in git |
| Jars the server loads | 236 in `mods/` after the update, 235 before | Counted 2026-10-31 |

## Update of 2026-10-31

At 16:58 on 2026-10-31 the owner asked whether the installed mods have updates
(`~/.claude/history.jsonl`, session c0e872d8). The script `tools/update_mods.py` appeared at 17:50 and
ran with `--apply` and `--add-deps` (see [tools](tools.md#the-new-update-script)), and its index change
was committed at 18:14 as 092140d. The run went as follows:

- From 17:50:55 to 17:52:54 it swapped 55 jars in `mods/` and `client-only-mods/` for newer builds, and
  at 17:53:01 it rewrote the index. Among them are Botania 453 to 456, Tinkers' Construct 3.11.2.166 to
  3.12.1.231, Iron's Spellbooks 3.16.1 to 3.16.3 with Iron's Lib 1.1.0 to 2.3.0, JEI 15.20.0.132 to
  15.74.0.233, Polymorph 0.49.10 to 0.49.11, Simple Voice Chat 2.6.20 to 2.6.27 and Chocolate Quest
  1.1.0 to 1.3.1.
- It added mezz_config 0.6.10, optional on both sides, because JEI 15.74 requires it (line 55 of the JEI
  jar's `mods.toml`).
- It took beta builds as well. Modrinth lists 12 of the 56 new builds as beta, among them Extra
  Utilities Reborn Again 1.2.0-rc10, JEI, Polymorph, Tinkers' Construct and Simple Voice Chat (API
  check, 2026-10-31). The owner asked for the most stable configuration on 2026-06-27 10:34.
- Cataclysm stayed at 3.31, which the Cataclysm Spellbooks fork build needs. That build accepts Iron's
  Spellbooks 3.15.0 and later, so 3.16.3 fits.
- The jar patches ran again from 17:53:03 to 17:53:06. The nine patched index jars differ from their
  index SHA-1 again, as expected.

At 18:06 three bundled jars followed in `local-mods/` and `mods/`: Connectivity 7.6 to 7.7, Cupboard 3.7
to 4.2 and Enchanting Plus 1.2.0 to 1.3.0, with their files in `modrinth-permissions/` renamed (commit 4917ab8).
Their license status did not change.

This server folder on the build machine has not booted with the new jars (its newest log ends on
2026-07-13). The Mac mini runs them since the first boot of the Halloween world at 18:07, and all six
of its boots that day kept the baseline of 3 ERRORs, see
[logs and crashes](../server/logs-and-crashes.md#boot-error-baseline). Those logs stay on the Mac. The
owner's client still holds the old builds (see [client](client.md#mods)).

## Owner rulings

From `~/.claude/history.jsonl`, session 5bd3e1de:

- 2026-06-27 10:34: the pack should feel as much like SkyTekx as possible, run on the most stable
  configuration, keep the old mods and add only a few new ones, and recipes must never conflict.
- 2026-06-27 17:29, repeated 2026-06-29 12:26, 14:11 and 15:11: take mods from Modrinth first, then
  from GitHub, and from CurseForge only as a last resort. Players must not need CurseForge.
- 2026-06-26 17:58: Tinkers' Construct is essential, and Silent Gear is no substitute.
- 2026-06-28 23:15: chocobos are a core mod. 2026-06-29 00:33: no Chance Cubes.
- 2026-06-29 12:05 and 12:26: search hard for successors of old mods ("I love oreberries").
- 2026-06-30 01:20: decisions that improve speed and reliability win.
- 2026-06-30 05:49: before making a feature optional, check whether another mod provides it. Every mod
  that is needed may be installed.

## Where the jars live

| Place | In git | Content |
|---|---|---|
| File [modrinth.index.json](../../modrinth.index.json) | Yes | The index entries with path, SHA-1, SHA-512, size, download URL and env per side |
| Folder [local-mods/](../../local-mods) | Yes | 33 jars that are not on Modrinth or were built from source. They ship as `overrides/mods/` |
| Folder `mods/` | No (`.gitignore:33`) | What the server loads: every index jar with a server side except Nostalgic Tweaks (201 after the update, 200 before), the other 32 `local-mods/` jars, and 3 jars that exist nowhere else in the repo. Also `ad_astra-forge-1.20.1-1.15.20.jar.bak`, the unpatched original, which Forge ignores |
| Folder `client-only-mods/` | No (`.gitignore:34`) | 16 jars kept off the server: the 15 index entries with server env `unsupported`, and Nostalgic Tweaks, moved by hand |
| Folder `disabled-mods/` | No (`.gitignore:56`) | 8 parked jars, listed below |

Forge only loads `mods/`. The `local-mods/` jars reach the server because they were copied into
`mods/`. Nyx is both an index entry and a bundled jar, which is why 33 bundled jars give only 32 extra
copies. All copies are byte identical, except Traverse, which `tools/patch_mods.py` patches in `mods/`
only. Nine index jars are patched as well, so their SHA-1 no longer matches the index. Players get the
unpatched jars. See [boot fixes](../content/boot-fixes.md).

The comment at `.gitignore:32` says that `mods/` is reproducible from the index and fetched on deploy.
No tool does that today, see [tools](tools.md).

## Sides

Each index entry says whether the client and the server need it (checked 2026-10-31 after the update,
which added one optional entry).

| Client | Server | Count | Examples |
|---|---|---:|---|
| Required | Required | 154 | Content mods |
| Optional | Optional | 25 | Nostalgic Tweaks, JEI, mezz_config and other tweaks and libraries |
| Required | Unsupported | 15 | BetterAdvancements, BetterF3, Controlling, CrashAssistant, Forgematica, MaFgLib, MouseTweaks, NekosEnchantedBooks, Prism, Searchables, athena, bwncr, embeddium, entityculling, jumpoverfences |
| Unsupported | Required | 12 | AI Improvements, Dungeon Crawl, Dungeons Arise, Ksyxis, three YUNG's Better mods, Get It Together Drops, LootJS, Nether Portal Fix, Noisium, Repurposed Structures |
| Required | Optional | 6 | CreativeCore, EMI, Enchantment Descriptions, GeckoLib, Xaero's Minimap and World Map |
| Optional | Required | 5 | AlmostUnified, Create, FallingTree, Kleeslabs, Special Mobs |

Two flags look wrong. Create is optional on the client, although it adds blocks and items that a
client needs to join. GeckoLib is optional on the server, although 10 other jars in `mods/` name it as a
dependency, among them Ars Nouveau and Iron's Lib. A launcher
that offers optional files can leave them out. JEI and EMI are both in the index.

## Bundled jars

The 33 jars in `local-mods/` (origins from each jar's manifest and from `modrinth-permissions/`):

- 26 come from CurseForge, among them Bibliocraft, Bird's Nests, Clean Swing Through Grass, Connectivity,
  Cupboard, Deadly World, Fireflies, Hexxit Additions, Infernal Mobs, Shield Parry, Traverse and
  Tumbleweed.
- 2 match a Modrinth file exactly and could move into the index: Elevator Mod (`elevatorid`) and
  EnderIO 6.2.18 beta.
- Offline Skins is listed as a Modrinth mod, but no Modrinth file has its SHA-1.
- 4 were built locally from source: Aether Genesis (2026-06-30), Nyx (2026-06-29, see
  [Nyx](../content/nyx.md)), SkyTekx Compat (2026-07-12, see [compat mod](../content/compat-mod.md)) and
  Twilight Forest (2026-06-27).

Six of them are All Rights Reserved without a redistribution grant: Aether Genesis, Connectivity,
Cupboard, Deadly World, Fireflies and Hexxit Additions (`modrinth-permissions/README.md`). They block a
public Modrinth listing, see [publish](publish.md).

## Three jars outside the pack

The server's `mods/` and the owner's client (same SHA-1) hold three jars that are in neither the index
nor `local-mods/`, so a fresh clone and the 3.0.5 `.mrpack` lack them:

| Jar | What it is | License | Source |
|---|---|---|---|
| Jar `alltheleaks-1.1.1+1.20.1-forge.jar` | Memory leak fixes (AllTheLeaks) | MIT | Not on Modrinth |
| Jar `ba_bt-1.20.1-3.0.0-beta3.2.1.jar` | BrassAmber BattleTowers, the successor the owner named on 2026-06-29 12:01 | All Rights Reserved | CurseForge only |
| Jar `cataclysm_spellbooks-1.2.9-1.20.1.jar` | Cataclysm Spellbooks, built 2026-06-29 06:46 from the fork skytekx/Cataclysm_Spellbooks, commit 36d1cee | MIT | The fork has no release. Modrinth has a different upstream file that does not carry the fix |

The owner ruled on 2026-06-29 06:25 to patch Spellbooks rather than Cataclysm, because the upstream
Spellbooks crashed the client against stock Cataclysm 3.31. BattleTowers and Spellbooks add content
that a client needs, so a player who installs 3.0.5 most likely cannot join this server. Moving
AllTheLeaks and the Spellbooks fork build into `local-mods/` is safe (both MIT). BattleTowers needs a
redistribution grant or an owner decision. BattleTowers is also tied to an open ruling, see
[Skylands worldgen](../content/skylands-worldgen.md#open-rulings).

## Parked and removed mods

The folder `disabled-mods/` holds 8 jars that are in neither the index nor `mods/`:

| Jar | Why it left |
|---|---|
| Better Combat, `bettercombat-forge-1.9.0+1.20.1.jar` | Left the index with mrpack 3.0.4. Reason not recorded, see [combat](../content/combat.md) |
| Memory Leak Fix, `memoryleakfix-forge-1.17+-1.1.5.jar` | Left the index with mrpack 3.0.4. Reason not recorded |
| WATERMeDIA, `watermedia-2.1.36.jar` | Refuses to run on a server and stopped the boot (`crash-reports/crash-2026-06-29_12.49.02-fml.txt:118`) |
| LittleFrames, `LittleFrames_FORGE_v1.4.3.jar` | Needs WATERMeDIA |
| VanillaBackport, `VanillaBackport-forge-1.20.1-1.1.7.10.jar`, and its compat jar `vb-compat-2.1-1.20.1.jar` | The owner allowed VanillaBackport only if materials stay consistent, one copper for everything (2026-06-29 13:59). Why it was parked is not recorded |
| Geophilic Backported, `GeophilicBackported-v1.3.3.jar` | Reason not recorded |
| Platform, `Platform-forge-1.20.1-1.3.4.jar` | Reason not recorded |

Mods removed for good:

- Radium broke the first boot on 2026-06-27. Its merged `PortalForcer` method made Arclight's
  `PortalForcerMixin` fail (`crash-reports/crash-2026-06-27_12.05.07-server.txt`). It predates git.
- Ruins left on 2026-06-30 01:13 (commit ac261f1), see [Skylands worldgen](../content/skylands-worldgen.md#history).
  The owner's client still has `ruins-1.20.1.2.jar`.
- Re:Avaritia was installed on 2026-06-30 06:21 and removed at 08:11 on the owner's word (commit bd5f18b).
- Skytekx-worldgen was replaced by SkyTekx Compat on 2026-07-12 (commit 174fbf6).
- Chocolate Quest Repoured was only tested. The index has Chocolate Quest Golden Apple Edition (1.1.0
  at commit 940f07e, 1.3.1 since the update), whose `mods.toml` license field says "Not specified".

## Changing the mod set

1. For a Modrinth mod, add an entry to `modrinth.index.json` with path, both hashes, size, env and the
   CDN URL, and put the same file into `mods/` (or into `client-only-mods/` if the server does not
   support it). Check its SHA-1 against the entry.
2. For a jar that is not on Modrinth, add it to `local-mods/`, copy it into `mods/`, and write a
   permission file in `modrinth-permissions/`.
3. If the jar is a target of `tools/patch_mods.py`, run the script.
4. Restart and compare the boot with the baseline of 3 ERRORs on
   [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline).
5. Rebuild the `.mrpack` as described on [publish](publish.md).

To move existing entries to newer builds, run `tools/update_mods.py` instead of step 1 and then follow
steps 3 to 5, see [tools](tools.md#the-new-update-script).

Do not use `tools/resolve_all.py` to fill `mods/`. It ignores the index, pulls the newest releases of a
hard coded list and would bring back Radium, Rubidium, memoryleakfix and Better Combat. See
[tools](tools.md).

## Mac mini

The script `macos/sync-to-mac.sh` mirrors `mods/` to the Mac mini as it is, with `rsync --delete`. The
folder already holds the patched jars and the three jars outside the pack, and since 2026-10-31 also the
new builds of the update above. Do not rebuild `mods/` from the index or from `resolve_all.py`. See [macOS host](../server/macos.md).

## History

The index held 213 entries in mrpack 3.0.4 (2026-06-29), 216 after commit d14ea2b (SlashBlade
Resharped, Re:Avaritia and Medieval Embroidery), 217 after 7cea753 (The One Probe) and 216 again after
bd5f18b (Re:Avaritia out). Commit 940f07e only raised `versionId` to 3.0.5. The `.mrpack` of 3.0.5 holds
an index that is byte identical to the one at commit 940f07e. On 2026-10-31 the update above moved 55
entries to newer builds and added mezz_config (092140d), 217 entries in all.

Related: [AlmostUnified](almostunified.md), [KubeJS](kubejs.md), [publish](publish.md),
[tools](tools.md), [client](client.md), [Arclight](../server/arclight.md).
