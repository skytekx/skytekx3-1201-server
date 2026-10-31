---
type: Reference
title: Boot fixes
description: How the server boot went from 152 errors to 3, with the skytekx3_fixes override datapack, the jar patches of tools/patch_mods.py, two config fixes and a hand edited jar.
tags: [content, server, datapack, mods, boot]
timestamp: 2026-10-31
---

# Boot fixes

On 2026-06-30 04:15 the owner asked for every error and warning at server boot to be resolved
(`~/.claude/history.jsonl`, session 5bd3e1de). Until then a boot logged 152 ERRORs, almost all of them
third-party data files that do not parse on 1.20.1. Three commits brought that down to 3, and it has
stayed at 3 since: 52f4f19 (05:26, to about 27), 518aa19 (06:21, to 10) and af5860e (07:05, to 3). The
warnings were not cleared. The last boot on the build machine (2026-07-12 23:25) still logged 304 WARN
lines, and the boots on the Mac mini of 2026-10-31 logged 344 to 387, or 1,658 at the first boot of the
new world. The counts per boot and the three accepted errors are on
[logs and crashes](../server/logs-and-crashes.md#boot-error-baseline).

The fixes come in five kinds:

| Kind | Where | Survives a mod update |
|---|---|---|
| Override datapack `skytekx3_fixes` | Folder `world/datapacks/skytekx3_fixes/`, tracked in git | Yes, as long as the paths stay the same |
| Jar patches | Script `tools/patch_mods.py`, applied to jars in `mods/` | No, run the script again after any new download |
| Config fixes | Files `config/apotheosis/enchantments.cfg` and `config/collective.json5` | Yes |
| Bird's Nests crash guard | The [compat mod](compat-mod.md), `local-mods/skytekx-compat-2.0.0.jar` | Yes |
| Hand edited Aether Genesis jar | Jar `local-mods/aether_genesis-1.20.1-0.0.1-neoforge.jar` | No, a rebuild from source loses it |

Installing missing dependencies did the rest. SlashBlade Resharped, Re:Avaritia (removed again later)
and Medieval Embroidery came in with 518aa19, and The One Probe and a source build of Aether Genesis
with af5860e.

## The override datapack

The pack `skytekx3_fixes` exists only inside the world, at `world/datapacks/skytekx3_fixes/`. It has no
source folder, the sync script does not touch it, and `tools/build_mrpack.py` does not ship it. Git
ignores `world/`, but the pack was force added, so it is tracked: 126 files, 125 data files plus
`pack.mcmeta` (pack format 15).

| Override | Count | What it replaces |
|---|---:|---|
| Loot tables | 99 | Tables that name items or types missing on 1.20.1, replaced with valid tables with empty pools: mekanism_extras 60, decocraft 28, dragnlivestock 3, irons_spellbooks 2, and one each for aether_villages, ancient_aether, galosphere, lost_aether_content, mocreatures and naturalist |
| Advancements | 14 | Malformed advancements, replaced with one `minecraft:impossible` criterion: galosphere 9, dragnlivestock 3, aether_redux 1, industrialforegoing 1 |
| Tags | 8 | Tags with a missing required entry, rewritten with `replace: true` and every entry `required: false`: five `forge` tags (`ungrown_ender_lilly_states`, `pink_items`, `generator_eur`, `frosty_gen_tag`, `rabbit_hutches`), `galosphere:experimental_blocks`, `bibliocraft:potion_shelf_potions` and `aether_villages:collections/ancient_aether_biomes` |
| Recipes | 2 | The Create recipes `silver_ingot_compat_galosphere` for smelting and blasting, switched off with a `forge:false` condition |
| Farming for Blockheads compat | 1 | The Thermal Cultivation seeds entry, made silent |
| Patchouli book | 1 | The `simplyjetpacks` guide book, set to `use_resource_pack: true` |

### What the overrides change in game

Each override was checked on 2026-10-31 against the last boot before the pack existed
(`logs/2026-06-30-19.log.gz`, started 03:33 on 2026-06-30). That boot logged 152 ERRORs, 101 of them "Couldn't parse element". All 99
loot tables and all 14 advancements were among the parse failures, so the originals never loaded. An
empty loot table and an advancement that cannot be earned behave the same as a file that failed to
load. These 113 overrides only remove log noise.

The 8 tags are different. That same boot logged a "Couldn't load tag" error for each of them, and a tag with a missing required entry does not load at all. With the overrides they load and
hold the entries that exist. So these tags now work where they were empty before.

The Patchouli override does not work. The boot of 2026-07-12 still logs "Failed to load book
simplyjetpacks:guidebook" (23:25:43). Only the mod author can fix it, see
[logs and crashes](../server/logs-and-crashes.md#boot-error-baseline).

The last boot (`logs/2026-07-12-1.log.gz`, up to Done) has no "Couldn't parse element" and no
"Couldn't load tag" line left.

Since the mod update of 2026-10-31 (see [mod set](../modpack/mod-set.md#update-of-2026-10-31)) three
overridden mods run newer builds: DragNs Livestock Overhaul 3.9.4, Iron's Spellbooks 3.16.3 and
Naturalist 2.0.9. An override with empty pools also empties a table that a new build has repaired. The
next boot test should show whether their 6 loot tables and 3 advancements still need the overrides.

### Pack order

Commit 52f4f19 says the pack had to be enabled last, because the Skylands pack sat in the middle of the
order and lost to mod packs loaded after it. In the current `world/level.dat` (checked 2026-10-31) the
fixes pack is no longer last. It sits at index 270 of 279. After it come packs that the server appended
when their mods were installed later (SlashBlade, Medieval Embroidery, The One Probe, Aether Genesis and
its built in packs), and then `skytekx3_skylands_forge` at 278. None of these later packs is known to
ship a file at one of the 125 override paths, and the current boot shows no parse error, so this is
harmless today. A fresh world puts the three file packs after all mod packs, in the last three of 280
places (read from `level.dat` on the Mac mini. The message of 1c6f8d4 counts from 0 and says 277 to
279). The same order problem does break the ChocoCraft overrides, see
[Skylands worldgen](skylands-worldgen.md#known-defect-the-chococraft-overrides-lose). The commands to
move a pack to the end are on [world reset](../server/world-reset.md).

## Jar patches

The script [tools/patch_mods.py](../../tools/patch_mods.py) edits mod jars in `mods/` in place (218 lines). It finds
each jar by a glob next to the script, so the repo path does not matter, and it is idempotent: a second
run prints `noop` for every jar.

| Mod and jar | Patch | Why |
|---|---|---|
| Doom Angel Ring (`doomangelring*.jar`) | Adds `minVersion: 0.8` to `doomangelring.forge.mixins.json` | Mixin logs an ERROR for a config without `minVersion` |
| Aether Redux (`aether-redux*.jar`) | Drops `GreenAercloudBlockMixin`, `OrangeTreeMixin` and `PurpleAercloudBlockMixin` | Compat mixins whose target mods are not installed |
| Mekanism Extras (`mekanism_extras*.jar`) | Drops `integration.mekaf.MixinAdvancedFactory` and `integration.mekmm.MixinMoreMachineFactory` | Same |
| Deep Aether (`deep_aether*.jar`) | Drops `AerwhaleKingMixin` | Same |
| Quark (`Quark*.jar`) | Drops `lootr.ConfigManagerMixin` from `quark_integrations.mixins.json` | Lootr is not installed |
| Ad Astra (`ad_astra*.jar`) | Deletes `data/minecraft/loot_tables/loot.json` | Its short path made Bird's Nests throw, see [compat mod](compat-mod.md) |
| Traverse (`traverse-forge*.jar`) | Adds `minVersion: 0.8` to the mixin config of the nested terraform wood API jar | Mixin ERROR as above |
| TiCEX (`*[Tt]icex*.jar`) | Drops the `ticex:catalyst_*` part and station slot from `blitz_gun` and the four `singular_gem` armour pieces, and 7 catalyst stats from the reconstruction material | TaCz is not installed and Re:Avaritia did not satisfy the gem tools, so these tools failed to load |
| CoFH Core (`cofh_core*.jar`) | Moves `MultiPlayerGameModeMixin` from the common list to the client list | A client class does not exist on a dedicated server |
| Medieval Embroidery (`Medieval_Embroidery*.jar`) | Rewrites 5 entity type tags with optional entries | They name a griffin entity that is never registered |

Ten jars are patched. Nine of them come from the index, and exactly these nine no longer match their
SHA-1 in `modrinth.index.json` (checked 2026-10-31). The tenth, Traverse, is a bundled jar, and the copy in `mods/` differs from the
one in `local-mods/` because only `mods/` is patched.

Run it after anything that downloads jars into `mods/`:

```sh
python3 tools/patch_mods.py
```

The docstring says that `tools/resolve_all.py` rebuilds `mods/` from `modrinth.index.json`. It does not:
it resolves a hard coded list of slugs to the newest release. See [tools](../modpack/tools.md).

## Config fixes

Commit 52f4f19 also changed two config files:

- In `config/apotheosis/enchantments.cfg` the Dimensional Doors enchantment `dimdoors:string_theory` had
  empty power functions, which made it impossible to obtain. They are now `Min Power Function=15` and
  `Max Power Function=50`.
- In `config/collective.json5` the key `enableUpdateChecker` is now false.

## Hand edited Aether Genesis jar

Aether Genesis has no release on Modrinth, so commit af5860e built it from the GitHub branch
`1.20.1-develop` and ships the jar without embedded libraries in `local-mods/`. The commit says the
client only mixins were moved into the client list. In the shipped jar the accessor
`client.accessor.SoundEngineAccessor` sits in the client list of `aether_genesis.mixins.json`. In the
source checkout `~/git/aether-genesis` (commit 6343dd5, no source change) it is still in the common
list, and the boot of 2026-06-30 06:54 warned about it ("@Mixin target
net.minecraft.client.sounds.SoundEngine was not found"). The shipped jar's SHA-1 (`5fa6d537...`) matches
none of the jars in the checkout's `build/libs/`. So the jar was edited after the build, and
`patch_mods.py` has no rule for it. A rebuild from source brings the warning back.

## On a new host

The script `macos/sync-to-mac.sh` mirrors `mods/` to the Mac mini as it is, with the patched jars. The
fixes pack lives in the world, so it only travels with `--with-world`. For a new world, such as the
Halloween map the owner asked for on 2026-10-31 16:56, copy `world/datapacks/skytekx3_fixes/` into the
new world's `datapacks/` before its first start. The script `tools/sync_skylands_datapack.sh` only
writes the two Skylands packs. Run `patch_mods.py` only if jars are downloaded again, and never fill
`mods/` with `resolve_all.py`. See [macOS host](../server/macos.md),
[world reset](../server/world-reset.md) and [deploy](../server/deploy.md).

A server built from the `.mrpack` gets neither the fixes pack nor the jar patches. It boots with the
old errors until both are added by hand.

Related: [logs and crashes](../server/logs-and-crashes.md), [mod set](../modpack/mod-set.md),
[compat mod](compat-mod.md), [world reset](../server/world-reset.md).
