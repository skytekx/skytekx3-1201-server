---
type: Feature
title: Combat
description: The 1.7 style melee of SkyTekx 3, a server side KubeJS script that removes the attack cooldown, plus the client and mod pieces around it.
tags: [content, combat, kubejs, gameplay]
timestamp: 2026-10-31
---

# Combat

On 2026-06-29 12:10 the owner said they dislike the sword swing animations of this pack and of newer
Minecraft and want combat to feel like 1.7 (`~/.claude/history.jsonl`, session 5bd3e1de). SkyTekx 1
ran on 1.7.10, where every click hit at full strength. The pack gets close to that with one server
script and a few mod choices.

| Piece | Where it runs | State |
|---|---|---|
| No attack cooldown, script `combat_1_7.js` | Server (KubeJS) | Live since commit 035cb18 |
| Old swing animation, Nostalgic Tweaks | Client, optional | Installed in the owner's client only, the pack ships no config for it |
| Better Combat removed | Both | Jar parked in `disabled-mods/` |
| Clean Swing Through Grass | Both | Bundled from `local-mods/` |
| Shield Parry | Both | Bundled from `local-mods/` |

## No attack cooldown

Since 1.9 the attack cooldown comes from the attribute `minecraft:generic.attack_speed`. Vanilla
computes the attack strength as the ticks since the last swing divided by `20 / attack_speed`. The
script [kubejs/server_scripts/combat_1_7.js](../../kubejs/server_scripts/combat_1_7.js) adds 1024 to
that attribute, so the recharge takes less than one tick and every click lands at full strength, for
every weapon including slow modded ones (`combat_1_7.js:3-9`).

- The modifier has the UUID `2f8c9d10-7b3a-4e5f-9a1b-c2d3e4f5a6b7`, the name
  `skytekx3_no_attack_cooldown`, the value 1024 and the operation ADDITION (lines 28 to 37).
- It is added on `PlayerEvents.loggedIn` and on `PlayerEvents.respawned`, because a respawn rebuilds the
  attribute map (lines 41 and 42). The function removes an old copy first, so it never stacks.
- It is transient, so it is never written to player data.
- The attribute is synced from the server to the client, so the cooldown bar follows on clients without
  any mod.
- The class names are resolved when the script loads, so a wrong name fails at boot and not silently at
  login (lines 25 to 29).

The script loads with 0 errors at every boot (`logs/kubejs/server.log:15`). The owner played on
2026-07-12 from 20:31 to 23:18 with it active, and the server log shows no script error at that login
(`logs/2026-07-12-2.log.gz:3515`). Nobody measured the cooldown in game.

Every hit now counts as fully charged, and vanilla's `Player.attack` allows sweep attacks, sprint
knockback and critical hits only for fully charged hits. So all three are available on every click.

### Tuning and revert

The constant `VALUE` on line 29 is the only setting. The script's comment suggests 20 to give the
slowest weapons a small cooldown back, or 8 as a gentler middle ground. To return to the vanilla
cooldown, delete the script and run `/reload`. Players who are online keep the modifier until they
relog or respawn, because it was already added to them.

## Swing animation

The swing animation is drawn by the client, so a server script cannot change it. Nostalgic Tweaks
2.0.0-beta.922 can bring back the old swing. It is in `modrinth.index.json` with the env optional on
both sides, and on the server it sits in `client-only-mods/` (moved by hand, see
[mod set](../modpack/mod-set.md)).

The owner's Prism instance has `oldSwing`, `oldSwingInterrupt` and `oldSwingDropping` set to true
(`config/nostalgic_tweaks/nostalgic_tweaks.json:635-637` in the instance, see
[client](../modpack/client.md)). The repo ships no Nostalgic Tweaks config, so other players get the
mod's defaults, if they install the optional mod at all. Whether those defaults include the old swing
was not checked.

## Mods around combat

- Better Combat (`bettercombat-forge-1.9.0+1.20.1.jar`) was in the index up to mrpack 3.0.3 and left
  with 3.0.4. The reason is not recorded. The jar is parked in `disabled-mods/`, a folder that git
  ignores.
- Clean Swing Through Grass (`cleanswing-1.20-1.8.jar`, LGPL-3.0, CurseForge only) lets a sword hit
  mobs through grass. It is bundled from `local-mods/` (commit 0d9bfc6).
- Shield Parry (`parry-2.5.0.jar`, LGPL-2.1, CurseForge only) lets a shield parry projectiles. It is
  bundled from `local-mods/` (commit 0d9bfc6).
- Spartan Weaponry 3.2.1 and Spartan Shields 3.1.1 come from the index. Their slow weapons lose their
  cooldown like every other weapon.

## Known crash: Asgard Shield Reloaded

Asgard Shield Reloaded 0.2 (`mods/AsgardShieldReloaded-1.20.1-0.2.jar`, env required on both sides)
crashes the whole server when a creeper explodes next to a player. Its `LivingAttackEvent` handler asks
for the item in a hand that its own helper returns as null, which ends in "Invalid hand null". It
crashed the server twice on 2026-06-27, at 20:43:25 and at 21:16:11
(`crash-reports/crash-2026-06-27_20.43.25-server.txt` and `crash-2026-06-27_21.16.11-server.txt`). No
fix was ever made, and the mod still loads at every boot. "Invalid hand" appears in no later log, most
likely because no explosion has hit a player since. On a public server this is a live crash bug. The
full analysis is in [logs and crashes](../server/logs-and-crashes.md).

## History

- 2026-06-29 12:10: the owner asked for the 1.7 feel.
- 2026-06-29: Better Combat left the index with mrpack 3.0.4 (17:29), and the script arrived. In the
  reconstructed history it is commit 035cb18, stamped 18:52 like the other reconstructed commits.

Related: [KubeJS](../modpack/kubejs.md), [mod set](../modpack/mod-set.md),
[client](../modpack/client.md), [open items](../open-items.md).
