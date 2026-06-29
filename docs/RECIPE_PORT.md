# Recipe-conflict system: 1.12.2 → 1.20.1 port map

SkyTekx's "recipes never interfere" came from a **layered** system. We reproduce each layer with its 1.20.1 equivalent. Legacy sources are in `scripts-legacy-reference/` and the original `config/unidict/UniDict.cfg`, `config/instantunify.cfg`, `config/recipehandler.cfg`.

## Layer map
| 1.12.2 (SkyTekx2) | 1.20.1 (SkyTekx3) | Notes |
|---|---|---|
| UniDict — ore/ingot unification + removal list | **AlmostUnified** (`config/almostunified/`) | tag-based; carry the metal priority |
| InstantUnify — runtime drop/inventory unify | AlmostUnified runtime unification | folded in, no separate mod |
| NoMoreRecipeConflict — in-grid cycle | **Polymorph** | drop-in GUI resolver |
| CraftTweaker2 `.zs` | **KubeJS** `kubejs/server_scripts/*.js` | re-author per item |
| LootTweaker 3-tier chests | KubeJS `LootJS`/loot events | recreate tiers |
| TinkersOreDictCache | — drop (native tag system) | |

## AlmostUnified priority (ported from UniDict metal order)
Legacy order: `minecraft → thermalfoundation → ic2 → mekanism → immersiveengineering → techreborn`.
1.20.1 `modPriorities` (IC2 Classic gone → Mekanism takes its tech slot): **`minecraft → thermal → mekanism → create → immersiveengineering`**.
Unify the standard `forge:` metal tag families (ores, raw_materials, ingots, nuggets, dusts, plates, gears, rods, storage_blocks) for the metals shared across Thermal/Mekanism/Create/IE. Carry legacy customs: obsidian dust + stone dust unification; keep cosmetic mods (Chisel-equivalents) out of unification.
> AlmostUnified writes a default config on first boot — set `modPriorities` there, then re-confirm the tag list against installed mods.

## CraftTweaker `.zs` → KubeJS port table
Status: **PORT** = translate as-is · **REAUTHOR** = intent kept, items changed by successor mod · **VERIFY** = needs the real 1.20.1 item id once mods are in · **DROP** = obsolete on 1.20.1.

### `Recipe Fixes.zs`
| Legacy item | Action | 1.20.1 note |
|---|---|---|
| Railcraft electric locomotive recipe | REAUTHOR | Railcraft→**Railcraft Reborn** (new ids); re-add corrected recipe |
| Rotten flesh → leather (furnace) | PORT | `event.smelting('minecraft:leather','minecraft:rotten_flesh')` |
| Bookshelf accepts any plank | PORT | use `#minecraft:planks` tag |
| `backpack:bound_leather` recipe | DROP/REAUTHOR | Eydamos Backpacks gone → Sophisticated/Corail; drop unless re-added |
| Tinkers toolkit (`tinkersaddons`) alts | DROP | Tinkers' Addons not on 1.20.1; TiC3 has built-in modifiers |
| Skyroot bucket milk oredict removal | REAUTHOR | Aether 1.20.1 bucket id differs; remove from `#forge:buckets/milk` if it conflicts |
| Fresh water (HarvestCraft) | REAUTHOR | HarvestCraft→**HarvestCraft 2** id `harvestcraft:freshwater` |
| **Angel Ring** tiers (ExtraUtils2) | REAUTHOR | ExtraUtils2→**Excessive Utilities** angel ring id; keep "loot-only, tiered upgrade" intent |
| Coal coke block mirror | DROP | Thermal/Railcraft coke now share `forge:storage_blocks/coal_coke`; AlmostUnified handles |
| Steel fix (IC2→TF steel) | DROP | steel unified via `forge:ingots/steel` (AlmostUnified) |
| IC2 energy-producer recipes | DROP | IC2 Classic + EnergyConverters gone; Mekanism native power |

### `thp_changes.zs`
| Legacy item | Action | 1.20.1 note |
|---|---|---|
| Oak bookshelf rename + stairs recipe | PORT | |
| Cow loot rolls ×2 | PORT | LootJS modify `minecraft:entities/cow` |
| Bound leather recipe | DROP/REAUTHOR | see above |
| Mega torch (Torchmaster) | PORT/VERIFY | Torchmaster 1.20.1 id `torchmaster:megatorch` |
| JEI descriptions (CQR items) | REAUTHOR | EMI descriptions; CQR id `cqrepoured:*` (verify CQR on 1.20.1) |
| Builder's Wands renames | DROP/REAUTHOR | BetterBuildersWands gone → Construction Wand |
| `hexxitgear:hexbiscus → hexical_essence` shapeless | VERIFY | **Hexxit Gear R** ids (hexbiscus/hexical_essence/hexical_diamond) — confirm |
| Cyclops eye JEI desc | DROP | CyclopsTek not on 1.20.1 |
| Cloud boots (CQR) | VERIFY | CQR id |
| Hexxitworld arrow-drop nerf | REAUTHOR | Hexxit World mod gone; apply via LootJS on the replacement |
| Nyx lunar water JEI desc | DROP | Nyx not on 1.20.1 |
| CQR bullet recipes | VERIFY | CQR ids |
| Golden feather (CQR) | VERIFY | CQR id |
| Hexical diamond recipe | VERIFY | Hexxit Gear R + Nyx fallen_star (Nyx gone → swap star item) |
| Asgard Shield giant swords (oredict) | PORT/VERIFY | AsgardShieldReloaded **is on 1.20.1**; use `#forge:rods/wooden`, `#forge:stone_tool_materials` |
| Quark stone tools stick fix | DROP | 1.20.1 vanilla stone tools already accept `#minecraft:stone_tool_materials` + `#forge:rods/wooden` |
| Bird's nest tooltip | PORT/VERIFY | Birds Nests id |
| Quark rope nerf | PORT | `quark:rope` |
| Quark wool/shulker dedup removals | RE-EVALUATE | Quark 1.20.1 may not register conflicting wool recipes — only remove if the duplicate reappears |

### `thp_craftable_starting_gear.zs`
| Legacy item | Action | 1.20.1 note |
|---|---|---|
| Craftable pre-populated **Akashic Tome** (6 guides) | REAUTHOR | rebuild NBT for 1.20.1 guide books: Tinkers', Thermal (Patchouli), Twilight Forest, etc.; recipe `hexical_essence + #forge:bookshelves` |
| Craftable Patchouli starting book | REAUTHOR | point at the pack's own Patchouli/`hexxit3` book id |

### Loot (`scripts/loot/thp*.zs`)
3-tier common/uncommon/rare chest system → recreate with **LootJS** (KubeJS) or a datapack: map vanilla + modded chest tables to the 3 tiers, same item pools where the mods survive.

## UniDict removal list → KubeJS `event.remove`
Carry the *intent* of UniDict's removals where the mods exist on 1.20.1:
- Block vanilla `*_block → 9× ingot/gem` shortcut recipes that re-introduce non-canonical items (iron, gold, diamond, emerald, coal, redstone) — only if a mod re-adds them; AlmostUnified + vanilla already keep these canonical.
- Remove duplicate toolkit / secret-rooms rotten-flesh recipes **only if** those mods are present (most are gone or replaced).
> Principle: on 1.20.1, **AlmostUnified removes most of what UniDict's manual list did** (duplicate ingots/dusts collapse to one tag-canonical item automatically). Keep the KubeJS removal block small and targeted — add entries only when the boot log / EMI shows a real duplicate.

## Validation checklist (post-boot)
1. AlmostUnified debug report: one canonical item per `forge:ingots/*`.
2. EMI: no duplicate ingots/dusts; key recipes (bookshelf, angel ring upgrade, hexical diamond, giant swords) resolve.
3. Polymorph: no unresolved same-output conflicts in-grid.
4. Log: no datapack recipe-load / missing-tag errors.
