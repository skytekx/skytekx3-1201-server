---
type: Reference
title: KubeJS
description: The seven KubeJS server scripts of SkyTekx 3, what each one really does on 1.20.1, the unfinished port of the SkyTekx 2 CraftTweaker scripts and how to work with Rhino.
tags: [modpack, kubejs, recipes, scripting]
timestamp: 2026-11-02
---

# KubeJS

KubeJS is the scripting layer of the pack. It replaces the CraftTweaker scripts of SkyTekx 2, which are
kept read only in [scripts-legacy-reference/](../../scripts-legacy-reference) (7 files). The port is
tracked in [docs/RECIPE_PORT.md](../../docs/RECIPE_PORT.md).

| Part | Version |
|---|---|
| KubeJS | 2001.6.5-build.26 |
| Rhino (the JavaScript engine) | 2001.2.3-build.10 |
| LootJS | 2.13.1, installed but unused |
| KubeJS plugins loaded | Built in Forge plugin, LootJS, Cucumber, L2 Hostility (`logs/kubejs/server.log:2-6`) |

## Scripts

All scripts live in [kubejs/server_scripts/](../../kubejs/server_scripts). There are no startup or
client scripts, and the folders `startup_scripts/` and `assets/` are empty. The folder `data/` holds the
six uncrafting recipes of Twilight Tweaks, see
[Twilight Forest backport](twilightforest-backport.md). The last boot on the build machine loaded 4 of
4 scripts with 0 errors and 0 warnings (`logs/kubejs/server.log:15`). The boot of the Mac mini on
2026-11-02 at 17:33 loaded 7 of 7, again with 0 errors and 0 warnings.

| Script | What it does |
|---|---|
| Recipe fixes, `recipe_fixes.js` | Adds two recipes, the rest is commented out |
| Tag fixes, `tag_fixes.js` | Mends tags of Ad Astra, Twilight's Flavors & Delight and the Twilight Forest backport, and keeps yellorium out of the uranium tags |
| Gameplay, `gameplay.js` | Nothing yet, every line is a comment |
| Combat, `combat_1_7.js` | Removes the attack cooldown, see [combat](../content/combat.md) |
| Skylands sleep, `skylands_sleep.js` | Turns the overworld to day when players sleep in the Skylands, see [Skylands worldgen](../content/skylands-worldgen.md#sleep-and-hostile-mobs) |
| Skylands portals, `skylands_portals.js` | Lets nether portals work in the Skylands, see [Skylands worldgen](../content/skylands-worldgen.md#nether-portals) |
| Unify inventory, `unify_inventory.js` | Swaps other variants of a material for the preferred item, see [AlmostUnified](almostunified.md#inventory-converter) |

The recipe summary of that boot reads "Added 2 recipes, removed 0 recipes, modified 14 recipes,
with 17 failed recipes" (`logs/kubejs/server.log:54`, 23:25:52). The 17 failures are third-party
recipes that name items which do not exist in this pack. They are not caused by these scripts. The
boot of the Mac mini on 2026-11-02 at 17:33 logged "Added 2 recipes, removed 0 recipes, modified 9
recipes, with 13 failed recipes". Those 13 failures were not examined.

### Recipe fixes

The file [recipe_fixes.js](../../kubejs/server_scripts/recipe_fixes.js) has two live recipes:

- The recipe `skytekx3:flesh_to_leather` smelts rotten flesh into leather (line 9).
- The recipe `skytekx3:bookshelf_any_plank` crafts a bookshelf from any planks (lines 12 to 16). The
  script first removes `minecraft:bookshelf`, but that removal matches nothing: Quark's variant
  bookshelves are on (`config/quark-common.toml:137`), and Quark ships its own `minecraft:bookshelf`
  recipe that switches itself off when they are on. That is why the log says "removed 0 recipes". The
  added recipe overlaps Quark's per wood bookshelf recipes, and Polymorph lets the player choose.

The commented blocks wait for the right item ids:

- Angel Ring (lines 24 to 26): the SkyTekx 2 rule kept it loot only, now meant for Excessive Utilities.
- Hexical diamond (lines 28 to 32): the comment says Nyx's fallen star is gone. Nyx is back and
  registers `nyx:fallen_star` (see [Nyx](../content/nyx.md)). The commented code also uses the namespace
  `hexxitgear`, but the installed Hexxit Gear (`hexxit_gear-1.0.1.jar`) uses `hexxit_gear`.
- Fresh water for HarvestCraft 2 (lines 34 and 35).

### Tag fixes

The file [tag_fixes.js](../../kubejs/server_scripts/tag_fixes.js) adds `ad_astra:venus_sandstone` to
the item and block tag `forge:sandstone/venus_sandstone`. Ad Astra's own compat recipes for Mekanism,
Create, Immersive Engineering and Thermal use that tag but never fill it, so the crushing of Venus
sandstone failed without it. The log shows one object added to each tag type at boot.

The same file strips the ice creams, milkshakes and cake slices of Twilight's Flavors & Delight out of
`farmersdelight:sweets`, `farmersdelight:snacks` and `diet:sugars` while Neapolitan is missing. Those
items exist only with Neapolitan, and their ids made the tag loader drop the three tags whole. It adds
the nine edible berries of the Twilight Forest backport to `forge:berries`, which Farmer's Delight and
Medieval Embroidery read. Since 2026-11-02 it also takes yellorium out of the uranium tags, see
[AlmostUnified](almostunified.md#yellorium).

### Gameplay

The file [gameplay.js](../../kubejs/server_scripts/gameplay.js) only holds commented plans: a Quark rope
nerf and Asgard Shield giant sword recipes with ore tags. The comment uses the namespace
`asgardshieldreloaded`, but the mod id is `asr`. The loot tweaks of SkyTekx 2 (two rolls for cows and the
Hexxit World arrow drop nerf) were deferred to a `loot_tiers.js` that was never written.

## Open rulings

On 2026-06-27 21:46 the owner asked for loot chances roughly like SkyTekx 2 and not three Hexxit
flowers next to each other (`~/.claude/history.jsonl`, session 5bd3e1de). No script, LootJS rule or
config does this yet. See [open items](../open-items.md).

## Working with Rhino

Rhino maps Java methods to JavaScript, and that mapping can go wrong. The first Skylands fall script
called the no argument accessors `level()`, `dimension()` and `location()`, which Rhino resolved
ambiguously. It threw a `TypeError` ("is not a function, it is object") every tick a player was below
Y 0, 126 times in one session log, and never teleported anyone (commit 241117c). The script was
deleted, see [Skylands travel](../content/aether-fall.md#history).

The combat script shows a pattern that works: load the classes with `Java.loadClass` at the top and
resolve every constant at load time, so a wrong name fails at boot and not later in an event
(`combat_1_7.js:20-29`). Check every new script on boot (0 errors in `logs/kubejs/server.log`) and then
in game, because event handlers only fail when the event fires.

Run `/reload` to reload server scripts. It does not undo effects that were already applied to players,
such as attribute modifiers.

## Stale references

- The Nyx rows at `docs/RECIPE_PORT.md:52` and `:55` say Nyx is not on 1.20.1.
- The file `docs/RECIPE_PORT.md:3` points at UniDict and other SkyTekx 2 configs that are not in this
  repo. They are in `~/git/skytekx2-1122-server/config/`.

Related: [AlmostUnified](almostunified.md), [combat](../content/combat.md),
[mod set](mod-set.md), [boot fixes](../content/boot-fixes.md).
