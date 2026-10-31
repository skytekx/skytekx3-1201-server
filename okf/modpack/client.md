---
type: Reference
title: Client
description: The owner's Prism Launcher instance for SkyTekx 3 on Asahi Linux, with Java 17, the 16K page allocator flag, the mods only clients have, the custom main menu and the client crash history.
tags: [modpack, client, prism, asahi]
timestamp: 2026-10-31
---

# Client

The only SkyTekx 3 client that exists is the owner's Prism Launcher instance on this MacBook (Asahi
Linux, aarch64, 16K pages). On 2026-06-27 19:58 the owner asked for the client to be added to Prism so
they could join the server (`~/.claude/history.jsonl`, session 5bd3e1de).

| Setting | Value | Evidence |
|---|---|---|
| Instance | Folder `~/.local/share/PrismLauncher/instances/SkyTekx3`, game folder `minecraft/` | Prism |
| Minecraft, Forge, LWJGL | 1.20.1, 47.4.18, 3.3.1 | File `mmc-pack.json` |
| Java | Eclipse Temurin 17.0.19 for aarch64 at `~/jdk17/jdk-17.0.19+10/bin/java` | File `instance.cfg` and the JDK's `release` file |
| JVM arguments | Flag `-Dorg.lwjgl.system.allocator=system` | File `instance.cfg` |
| Memory | 4096 MB minimum, 8192 MB maximum | File `instance.cfg` |
| Managed pack | No. The instance is not linked to the `.mrpack` and is kept up to date by hand | Key `ManagedPack=false` in `instance.cfg` |
| Server list | One entry, `127.0.0.1` | File `servers.dat` |

## Java and the Asahi flag

Minecraft 1.20.1 with Forge 47 needs Java 17. The instance points at a Temurin 17 JDK in the home folder
instead of a system Java.

The Asahi kernel uses 16K memory pages. The aarch64 `libjemalloc.so` that LWJGL bundles assumes 4K
pages, so the game crashes at launch with "Unsupported system page size". The JVM argument
`-Dorg.lwjgl.system.allocator=system` makes LWJGL use the system allocator instead (memory
`minecraft-prism-asahi.md`). Every new instance on this machine needs it, including one made from the
`.mrpack`. A player on Windows, macOS or a 4K page Linux does not need it.

Audio goes through `~/.alsoftrc` with `drivers=pulse`, which avoids an OpenAL crash on PipeWire (same
memory note).

## Mods

The instance's `mods/` has 259 entries (2026-10-31). Next to the pack it holds:

- The three jars that the server runs outside the pack: AllTheLeaks, BrassAmber BattleTowers and the
  Cataclysm Spellbooks fork build, with the same SHA-1 as on the server (see
  [mod set](mod-set.md#three-jars-outside-the-pack)).
- FancyMenu 3.9.6, Konkrete 1.8.0 and Melody 1.0.3 for the main menu (see below).
- Nostalgic Tweaks 2.0.0-beta.922 for the old swing (see [combat](../content/combat.md#swing-animation)).
- Simple Voice Chat 2.6.20.
- The Ruins jar `ruins-1.20.1.2.jar`, although Ruins left the pack and the server on 2026-06-30.
- Disabled copies: Better Combat, LittleFrames and WATERMeDIA 2.1.36 and 3.0.0.20, each renamed to
  `.disabled`.

The mod update of 2026-10-31 did not reach the instance (see
[mod set](mod-set.md#update-of-2026-10-31)). It still holds the old builds, for example Polymorph
0.49.10, Balm 7.3.38, Waystones 14.1.20 and Simple Voice Chat 2.6.20, while the server folder now has
0.49.11, 7.3.44, 14.1.21 and 2.6.27. Bring the instance to the same jars before the next join.

A client must carry every server mod that registers content. When it does not, Forge refuses the join
in the handshake: on 2026-06-29 05:09 the client lacked Ars Magica Legacy ("Missing required datapack
registry: arsmagicalegacy:obelisk_fuel", `logs/2026-06-29-11.log.gz` in the instance), and on
2026-06-30 13:54 it lacked SlashBlade ("Missing required datapack registry: slashblade:named_blades",
`logs/2026-06-30-2.log.gz`). Later joins worked, on 2026-07-01 at 19:55 and on 2026-07-12 at 20:31,
when the owner played until 23:18.

## Main menu

Between 2026-06-27 20:49 and 21:49 the owner asked four times for the old SkyTekx start menu with the
SkyTekx logo, built from the assets of the old repo and working without a resource pack. This is done
only in part.

The layout [config/fancymenu/customization/skytekx_title.txt](../../config/fancymenu/customization/skytekx_title.txt)
(tracked since commit 535c4f5, last edited 2026-06-28 18:10) replaces the title screen background with
`config/fancymenu/assets/skytekx_bg.png` (1920 by 1080) and draws `skytekx_logo.png` 420 by 160 pixels,
centred at the top (x -210, y 30). It hides the vanilla title logo and keeps the vanilla buttons. The
SkyTekx 2 menu (`~/git/skytekx2-1122-server/config/CustomMainMenu/mainmenu.json`) had its own buttons,
links, a greeting ("Thank you for playing #username#.") and a line with the Forge version ("The current
Forge version is #forgeversion#."). None of that was rebuilt.

On 2026-06-27 21:15 the owner asked why the game started in a modification mode instead of the normal
view a pack user should see. The FancyMenu option `show_customization_overlay` is `false` in the repo
and in the instance (`config/fancymenu/options.txt:11`, tracked since 535c4f5). It hides FancyMenu's
editing bar, which is the likely cause of that view. The link is not recorded.

The logo is the same file as the website logo (`~/git/skytekx-web/assets/logo.png`, equal SHA-1). Where
it came from before 2026-06-28 18:12 is not recorded. The folder `menu-assets/` still only has a README
that asks for the original CustomMainMenu images, and none are there.

The pack ships the layout with `config/`, but FancyMenu, Konkrete and Melody are not in
`modrinth.index.json`. So a player who installs the pack gets the files and a vanilla title screen. All
three jars match Modrinth files exactly and could go into the index as client only entries.

Two client crashes came from FancyMenu's dependencies. On 2026-06-27 21:05 it needed Melody, and on
2026-06-29 15:22 it refused WATERMeDIA 2.1.36 and wanted 3.0.0.19 or later
(`crash-reports/crash-2026-06-27_21.05.26-fml.txt` and `crash-2026-06-29_15.22.56-fml.txt` in the
instance). Today both WATERMeDIA jars are disabled and FancyMenu loads without them.

## Resource packs

The repo has an untracked resource pack, `resourcepacks/SkyTekxFixes/` (pack format 15, "SkyTekx
advancement name fixes"). It holds 66 lang keys for Spartan Weaponry advancements. A copy sits in the
instance's `resourcepacks/`, but it is not enabled: `options.txt:45` lists only vanilla, the mod
resources and Moonlight's dynamic assets. The `.mrpack` has no resource packs.

## Crash history

| Time | Report in the instance's `crash-reports/` | Cause | Fix |
|---|---|---|---|
| 2026-06-27 21:05 | File `crash-2026-06-27_21.05.26-fml.txt` | FancyMenu needs Melody | Melody added |
| 2026-06-27 21:24 | File `crash-2026-06-27_21.24.45-client.txt` | Asgard Shield Reloaded: "Registry Object not present: asr:diamond_giant_sword" | Not recorded, no repeat |
| 2026-06-29 05:09, 05:20 and 06:16 | Three client reports | Upstream Cataclysm Spellbooks against Cataclysm 3.31 (missing renderer class and method) | The fork build, see [mod set](mod-set.md#three-jars-outside-the-pack) |
| 2026-06-29 15:22 | File `crash-2026-06-29_15.22.56-fml.txt` | FancyMenu refuses WATERMeDIA 2.1.36 | WATERMeDIA disabled |

There is no client crash report after 2026-06-29.

## Other settings

- The optional telemetry of Minecraft is off, `telemetryOptInExtra:false` (`options.txt:85`).
- Render and simulation distance are 12, the frame cap is 120.
- The instance has no singleplayer world (`saves/` is empty).

## Mac mini

The client stays on the MacBook. To play on the Mac mini server, add its address to the server list and
open the ports there: 25565 over TCP for the game and 24454 over UDP for Simple Voice Chat
(`config/voicechat/voicechat-server.properties:9`). See [macOS host](../server/macos.md).

Related: [mod set](mod-set.md), [combat](../content/combat.md), [publish](publish.md),
[logs and crashes](../server/logs-and-crashes.md).
