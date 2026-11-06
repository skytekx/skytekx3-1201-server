---
type: Reference
title: Melting
description: How every armor piece, weapon, tool and item that holds a meltable material melts in the Tinkers smeltery, with 9,717 generated recipe files, the rules behind them, the dupe audit, the self check of the reader and one command to run it again after SkyTekx Molten or the mod set changes.
tags: [content, recipes, tconstruct, datapack, tools]
timestamp: 2026-11-06
---

# Melting

On 2026-11-04 at 17:35 the owner asked that all armor, weapons and everything that holds something meltable
melt in the smeltery, including what the new SkyTekx Molten makes meltable. Before that an item melted only
when Tinkers, an addon or one of the 32 hand written overrides of the [boot fixes](boot-fixes.md) had a recipe
for it. Two tools now do it for the whole pack:

- The KubeJS script `tools/melting_dump.js` is for test servers only. It dumps every recipe, item, fluid, tag and
  mod of a booted server into `kubejs/` of that server.
- The Python script `tools/gen_melting.py` reads the dump and writes one Tinkers melting recipe per item into
  `world/datapacks/skytekx3_fixes/data/skytekx3/recipes/melting/`, the ids are `skytekx3:melting/<mod>/<item>`.
  It uses the standard library only, gives the same files for the same dump, and deletes and rewrites only that
  folder.

The generator never melts an item into more than the recipes it reads and the catches it models take, and it
audits every other melting recipe of the pack for the same rule. A dupe is a blocking fault: the generator exits with status 1 until it is
closed. A self check compares the product count of every recipe it reads with the counts that the JSON of the
recipe declares, so a reader fault that would hide a dupe stops the run as well (see
[follow-up of 2026-11-05](#follow-up-of-2026-11-05)).

The counts are those of the merged tree of 2026-11-06: pack 3.1.3 with the nine mods of 2026-11-04, Flux Networks
and SkyTekx Molten (see [mod set](../modpack/mod-set.md#additions-of-2026-11-04)), and the mods of the port branch,
such as Actually Additions, Expanded Delight and the forks of Ender IO and Create Crafts & Additions. They follow
the fixes of the review, the sync with the unification, the follow-up of 2026-11-05 and its review (see
[fixes of the review](#fixes-of-the-review), [sync with the unification](#sync-with-the-unification),
[follow-up of 2026-11-05](#follow-up-of-2026-11-05) and [review of the follow-up](#review-of-the-follow-up)), and
the regeneration for the mods of the branch (see
[regeneration for the mods of the branch](#regeneration-for-the-mods-of-the-branch)).
The recheck of 2026-11-05 held the folder back from the deploy of 3.1.3 at 00:46 until its three faults were
fixed. It reached the Mac mini at 03:47 and went live with a `/reload` at 05:45, after which KubeJS found
46,980 recipes, as on the test server. The recipes are server side, so they need no new pack. Since the merge of
2026-11-06 the files of Create Crafts & Additions follow the recipes of its fork and the moss soup of the Twilight
Forest is gone (see [after the merge of 2026-11-06](#after-the-merge-of-2026-11-06)). The counts below come from
the dump of 21:13 on 2026-11-06 of a Forge test boot of the merged tree. The mods of the branch reach the live
server only with the next pack after 3.1.3.

| Count | Without Molten | With Molten |
|---|---:|---:|
| Recipes read from the dump | 39,272 | 39,881 |
| Items that melt already and keep their recipe | 1,375 | 1,658 |
| Items that get a generated recipe | 8,044 | 8,499 |
| Of them gear that melts by its repair material | 46 | 49 |
| Items that stay out below 10 mB | 81 | 282 |
| Recipes the reader cannot read | 617 | 617 |
| Recipes left out because an ingredient matches no item | 4 | 4 |
| Products of recipes that take only inputs the machine keeps | 33 | 33 |

The folder holds 9,717 files with 4.0 MB: 6,826 for both cases, 1,218 only without Molten and 1,673 only with
it. With Molten, 1,534 of the generated recipes give at least one of 63 Molten fluids, led by soularium 313,
manasteel 186, dark steel 163 and draconium 161, and 519 items melt only because Molten is there.

## The rules

The content of an item is a vector of fluids in mB.

- An item that already melts has the content of its melting recipe, the smallest one per fluid when several
  match. Recipes of one item that share no fluid are separate families, and the family with the largest amount
  counts. Twilight Construct melts ironwood into molten ironwood and Tinkers into iron and gold, so ironwood gear
  of other mods melts into molten ironwood. An ore counts with its base amount without byproducts.
- Any other item has the content of its cheapest recipe: the sum of its ingredients divided by the result count,
  with the minimum per fluid over the alternatives of a slot and over the recipes of the item. A casting recipe is a
  recipe whose ingredient is the fluid. Crafting remainders such as buckets are subtracted, except in the catches
  below, which use up the bucket whole. A product with a chance counts with its largest count, so a lucky craft
  never melts into more than it took. A count inside the item of a product, as the SAG mill of Ender IO writes it,
  and a count beside a result given as a plain id count as well.
- A product of the Ender IO SAG mill counts with the best grinding ball of the pack. `SagMillingRecipe.craft`
  rolls all products of a recipe once per round while the output multiplier of the ball is above zero, the last
  round with the chance of what is left of it, and multiplies the chance of each product by the bonus of the ball,
  unless the recipe has the bonus type `none`. The vibrant alloy ball has the largest multiplier of the 10 balls,
  1.75, so a product of a recipe with the bonus type `multiply_output`, or with no bonus type, counts with twice
  its count in the generator and with its expected count in the audit. With `chance_only` the mill rolls once with
  the bonus of the ball on the chance, and with `none` once without it, so these products keep their count.
- A product of a Thermal machine counts with the best catalyst and the best augments of the pack. Thermal reads a
  product without a chance, or one marked `locked`, as locked (`RecipeJsonUtils.parseItemChance`). Every other
  chance becomes the chance times the mod of the catalyst times the mod of the machine, and at least the smallest
  chance of the catalyst (`BaseMachineRecipe` and `CatalyzedMachineRecipe`). The first product takes the primary
  mods, the others the secondary ones, and the machine gives the whole part of the chance for sure and one stack
  more at the rest. No augment raises the primary mod. Four Auxiliary Process Sieves of the Abyssal tier of Thermal
  Extra in the four augment slots raise the secondary mod to 2.6. The catalysts come from the dump, the strongest
  are phytogro in the insolator (3.0 and 3.0), basalz powder in the pulverizer (1.25 and 3.0), cinnabar in the
  smelter (3.0 primary, 3.5 secondary as dust) and nuke TNT in the nitratic igniter of Thermal Extra (5.0 and 5.0).
  A raw osmium in the igniter therefore gives up to 15 osmium ore chunks, 6 locked and 9 from the chance of 0.65,
  and a raw iron in the smelter up to 10 nickel nuggets.
- The Cyclic crusher adds its bonus once at its percent, always at a percent of 1, with up to the count of its
  stack (`TileCrusher`). The fluid that the Ender IO tank empties out of an item is a product, not an input.
- A sequenced assembly of Create gives exactly one of its results per craft and draws it by the chances, which
  act as weights. Each result therefore counts with its own count, and in the audit at its share of the weights.
- Catching a fish, an axolotl or a tadpole with a water bucket is no recipe, so the generator adds a catch for
  each of the six mob buckets, which takes the water bucket whole. The catch recipe of Naturalist
  (`naturalist:net`) takes the bucket whole as well. Every mob bucket therefore melts into the 270 mB iron of its
  bucket and nothing more.
- An ingredient that gets a generated recipe counts with what that recipe gives, rounded down to whole mB, and
  for an item with durability to whole units. So no item melts into more than its ingredients melt into. Where a
  loop such as ingot and nuggets meets an amount that nine does not divide, the ingot drops to the next multiple of
  nine: without Molten the twinite ingot of Thermal Extra melts into 117 mB obsidian, not 125 mB.
- Items no recipe makes are worth nothing, and so are items that only a loop of their own recipes makes. That
  includes the variants that AlmostUnified swaps away, since it turns their recipes into recipes of the preferred
  item, so a slot that also takes a variant that does not melt by itself counts as nothing (see
  [sync with the unification](#sync-with-the-unification)).
- Recipes whose ingredients the parsed recipe hides are read from their JSON. Inputs the reader cannot place count
  as nothing, and a recipe the reader cannot read at all gives its product nothing. An unreadable recipe therefore
  never leaves a cheaper path open, its product gets no melting recipe from it. An id without a namespace, such as
  `gilded_blackstone` in a pulverizer recipe of Thermal, is read as a `minecraft` id, as the game reads it. The
  spell book upgrades of Ars Nouveau are read like crafting, since `BookUpgradeRecipe` is a shapeless recipe that
  only copies the spells, and the JSON of the apprentice book has a pattern with an item that no longer exists,
  which the recipe never reads. The 617 unreadable recipes are led
  by Immersive Engineering mineral mixes 227, Framed Blocks frames 135, Mystical Agriculture infusion 119, Ars
  Nouveau caster tomes 27, the farmer of Mystical Automation 18, Mob Grinding Utils beheading 13, Mekanism
  crystallizing 11 and Forbidden & Arcanus residues 10.
- Inputs that the machine keeps or hands back count as nothing. The code of each machine shows which inputs it
  takes. There are five such cases:
  - Ars Nouveau imbuement: `ImbuementTile` puts the result into its own slot and takes the source and its one
    input, the items on the pedestals stay.
  - The runic altar of Botania: `RunicAltarBlockEntity` hands back every rune it takes (every `RuneItem`), so a
    rune that goes into a higher rune comes back.
  - The solidifier of Mob Grinding Utils: `TileEntityXPSolidifier` takes only the experience fluid, the mould
    stays in its slot.
  - The durability recipes `recipesapi:durability_shapeless` and `recipesapi:durability_shaped`: their
    `getRemainingItems` hands back the tool with one more point of damage, like the Cucumber and Bookshelf types.
  - The laser drills of Industrial Foregoing: `OreLaserBaseTile` only tests the lens. The 32 ore products of its
    recipes therefore get nothing from them, and the ores melt by their own recipes as before. They were 31 before
    the merge of 2026-11-06, and the one more is most likely the deepslate yellorite ore, which Extreme Reactors
    2.0.97 adds to the tag `forge:ores/yellorite` that the yellorite recipe names. Nobody checked that in game.
- Inputs that a machine uses up under a key that names no ingredient elsewhere count as used up, the table
  `USED_KEYS` of the script (be014fc). The cooking pot of Farmer's Delight 1.3.4 and the juicer of Expanded Delight
  serve their result into the item under `container` and shrink that stack
  (`CookingPotBlockEntity.useStoredContainersOnMeal` and `useHeldItemOnMeal`, read with javap, and
  `JuicerBlockEntity` of the port), and the Empowerer of Actually Additions shrinks the item on each of its four
  display stands under `modifiers` (`TileEntityEmpowerer` of the fork). So a drink of the cooking pot or the juicer
  melts into the 1,000 mB glass of its bottle, as the crafted melon juice of Farmer's Delight does.
- The other machines of the pack that take items from pedestals or slots around them use them up, which their code
  shows as well: the enchanting apparatus and scribes table of Ars Nouveau, the altars of Mystical Agriculture, the
  fusion crafting of Draconic Evolution and the solidifier of Cyclic. Of the 43 crafting classes that the reader
  takes as they are, those that override `getRemainingItems` hand back crafting remainders, a damaged tool (handled
  above), the minecart of a Railcraft cart, the elytra of the Quark duplication recipe or the earmuffs and power
  pack that Immersive Engineering takes off a helmet or chestplate. None of them changes a generated recipe: the
  part of a cart keeps the value of its own recipe, the elytra does not melt, and the earmuffs and power pack get
  their value from their own crafting recipes.
- A recipe with an ingredient that no item fills is left out, since nobody can make it. Forge shows an empty tag,
  and a tag that no mod defines, in an ingredient as a barrier named "Empty Tag: <tag>", and no survival player can
  get a barrier. The dump therefore leaves every barrier out of every item list and reads the tags from the
  registries, where an empty tag is an empty list. Four recipes are left out: the platinum capsule of Capsule needs
  `forge:ingots/platinum`, which is empty, and the lunar panel and the triple compressed dirt of Extra Utilities
  Reborn Again and the prismarine banner pattern of Additional Banners name tags that no mod defines. The triple
  compressed dirt has another recipe, the lunar panel has none and cannot be crafted in the pack.
- Since the nine mods the reader also knows the alchemical reaction chamber (ARC) of Blood Magic, the reaction
  chamber of AdvancedAE, the circuit cutter of ExtendedAE and the combine and upgrade recipes of AE2 Wireless
  Terminals. The new reaction chamber and circuit cutter recipes make the processors of Applied Energistics 2,
  which without these readers would have lost their melting recipes. The code of the ARC shows that it always
  takes its input and that `consumeingredient` false only hands back the crafting remainder of that input. The
  generator had read such recipes as free, so the 16 concretes and the fragments of Blood Magic got no recipe
  before.
- A recipe with several products gives a product that does not melt nothing, since nobody knows how the cost
  splits. A fluid product takes its content with it, so it comes off the share of the item products. No recipe of
  the pack gives an item together with a fluid that counts, so this changes no file today.
- Without Molten the same is computed again with its fluids and recipes removed. An item whose content is the same
  in both cases gets one file, an item whose content differs gets `<item>.json` with
  `forge:not` of `forge:mod_loaded skytekx_molten` and `<item>_molten.json` with `forge:mod_loaded skytekx_molten`.
- The largest fluid is the result and the others are byproducts, which only the foundry gives out. Items with
  durability use `tconstruct:damagable_melting` with the unit size Tinkers uses for the fluid, so a worn piece
  returns less. The amounts are whole units, because Tinkers rounds the output down to them even at full
  durability.
- The temperature is the one Tinkers uses for the result fluid, the time follows the Tinkers formula.
- Gear that nothing makes, such as dungeon loot, melts by its repair material at the unit counts Tinkers uses for
  vanilla gear: helmet 5, chestplate 8, leggings 7, boots 4, sword 2, pickaxe 3, axe 3, shovel 1 and hoe 2. That
  covers 46 items without Molten and 49 with it, such as the rusted armor of Magistu Armory, the heavy iron armor of
  Chocolate Quest and the claymore of Iron's Spellbooks. Gear whose repair material is an empty tag has none and
  stays out (see [what stays out](#what-stays-out)).
- Every recipe has `forge:mod_loaded` for the mod of its item and for the mod of every fluid outside Tinkers, and
  the Molten condition above. Vanilla items need no mod condition, so 87 files of vanilla items with Tinkers fluids
  have none. The items of `irons_patreon_lib` are registered by Iron's Lib under a namespace that is no mod id, so
  their recipe has `forge:item_exists` instead.
- The fluids that count are metals, gems (diamond, emerald, quartz, amethyst), obsidian, glass, ender, debris and
  the Molten fluids. Left out are water, lava, milk, blazing blood, magma, venom, honey, liquid soul, the slimes,
  the soups and stews, clay, seared and scorched stone and the charred prismarine of Tinkers' Reforged. Fluids
  that share a `forge:molten_*` tag count as one, and so do molten ender and the resonant ender of Thermal, which
  share `forge:ender` and hold 250 mB per pearl each.
- Items of Tinkers itself, Tinkers tools and Tinkers parts stay out, Tinkers melts them by their materials.

## Fixes of the review

The review of the first folder of 9,653 files found five faults that let items melt into more than their recipes
take, or into a fluid they do not hold. All five are fixed in the generator or the dump:

| Fault | Effect on the first folder | Fix |
|---|---|---|
| The items on the pedestals of Ars Nouveau imbuement counted as used up | 61 files of `ars_nouveau` too high, among them the earth essence at 90 mB iron and the manipulation essence at 360 mB gold | They count as nothing |
| The runes on the runic altar of Botania counted as used up | With Molten 77 files of `botania` and 4 of `spartanshields` too high, such as the rune of summer at 90 mB manasteel | Every rune counts as nothing |
| The dump kept the barrier that stands for an empty tag | 25 ore melting recipes of Tinkers for metals the pack lacks have an empty tag as input, so the barrier looked like an item that melts into bendalloy. In all, 50 files melted into molten bendalloy: 28 of `aether`, 18 of `spartanweaponry`, 3 of `exura` and 1 of `additionalbanners`, mostly gear whose repair tag is empty | The dump leaves every barrier out, and a recipe with an ingredient that no item fills is left out |
| The durability recipes of RecipesAPI were not known to keep their tool | 8 files of `exura` counted the tool as used up, such as the ender shard with 33 mB iron of the tool on top of its 31 mB ender | They hand back the tool, like the types of Cucumber and Bookshelf |
| The mould of the Mob Grinding Utils solidifier counted as used up | The XP jelly baby melted into 80 mB gold, the solid XP block into 720 mB gold, and the entity spawner had 1,440 mB gold | The mould counts as nothing |

While fixing them, the new rule for recipes that no item can fill showed two recipes that do work in game: the
pulverizer recipe of Thermal that names `gilded_blackstone` without a namespace, and the apprentice spell book
upgrade of Ars Nouveau, whose JSON pattern names an item that no longer exists. The reader now handles both (see
[the rules](#the-rules)). The apprentice spell book melts into 1,000 mB obsidian, 810 mB iron, 800 mB quartz and
300 mB diamond, and the archmage spell book, which takes it, into the same plus 750 mB ender and 200 mB emerald.

Against the first folder, 114 files are gone and 89 are lower, 1 is new and 1 is higher, both from the spell books.
Another 23 files kept their content but lost the Molten condition, since both cases now give the same, such as the
loonium of Botania. No other file changed. The audit lost 39 sources without Molten and 45 with it. Of them, 25
were the placeholder as well, the platinum essence recipe of Mystical Agriculture made a "barrier" that the 25
Tinkers recipes melted. The others are laser drill recipes, which take only the kept lens now. The ores they make melt as
before.

## Sync with the unification

While the review was worked off, the repo moved on to 5e6e2d70. Two recipe fixes in
`kubejs/server_scripts/recipe_fixes.js` (5c242f6e, 568ddaed), the new config of AlmostUnified (a9fc66f4), the EMI
hide list `c:hidden_from_recipe_viewers` (7d1bb726) and the steel and graphite trims (fc1b921a, 5b0574e4) reach
the game. The test server also lacked the 53 essence recipes of 0fc5a549 and Flux Networks, which comes with 3.1.3.
At 23:11 it got all of them, and the dump of 23:12 gave 125 files that differ from the folder of the review
fixes:

| Change | Files | Why |
|---|---:|---|
| Lower | 107 | AlmostUnified now unifies the ender pearl dusts to the one of Thermal. No recipe makes the dusts of AE2 and EnderIO any more, so they are worth nothing, and a slot that takes any ender pearl dust counts as nothing. In all, 48 files of `advanced_ae`, 48 of `megacells`, 8 of `exchangers`, 2 of `aeinfinitybooster` and 1 of `ae2` lost all or part of their ender, such as the lava immunity card of AdvancedAE, which had 12,000 mB ender and keeps its 4,320 mB copper and 180 mB netherite. No other fluid changed |
| Gone | 6 | The ender dust of AE2 and the powdered ender pearl of EnderIO, which no recipe makes now, and the shattered singularity and the quantum infused dust of AdvancedAE, the quantum entangled singularity of AE2 and the enderios of EnderIO, whose ender came only from such a slot |
| New | 12 | The 9 items of Flux Networks that hold something, such as the Flux Core at 1,000 mB obsidian and 62 mB ender, and the copper, gold and electrum rods of Create Crafts & Additions at 45 mB, the preferred rods now. Before, the metal press of Immersive Engineering named a tag of two rods as its result, which the generator reads as a recipe with several products, so these rods got nothing |

The lower files undercount, which the rule allows. A rule that leaves the variants of AlmostUnified out of a slot
would give the ender back, but then a variant that a player gets without a recipe, such as from loot, would fill
the slot for less than it counts, so the rule stays as it is (see [open items](../open-items.md#owner-decisions)). In game the two
old dusts melt like the Thermal dust, because AlmostUnified turned the item of the generated recipe of the Thermal
dust into the tag `forge:dusts/ender_pearl`, which holds all three. In the folder of 9,546 files that was 250 mB
ender. Since the follow-up it is 27 mB, a ninth of a pearl, because the SAG mill grinds one pearl into nine dusts
(see [follow-up of 2026-11-05](#follow-up-of-2026-11-05)). The rods of Thermal Extra melt the same way into 45 mB,
like the rods of Create Crafts & Additions. None of these variants took less when it was made.

The two recipe fixes change no file. The iron rod of Thermal Extra, the nuggets of the Spartan Weaponry recipes and
the steel ingot of the Alkahestry Tome melt by recipes of Tinkers, so their content stays what those give. The audit
keeps its 35 known machine dupes, two of them now on the preferred item: the tome doubles the steel of Mekanism,
and the SAG mill loop sits on the quartz dust of Thermal. Knightmetal of the Twilight Forest is the preferred item
now, which leaves two fewer conversions in each case. The alloy smelter recipe of EnderIO that makes an ender pearl
from nine ender pearl dusts is a new source, since its slot counts as nothing. The dump of 23:22 on the new
folder gives the same 9,546 files.

## Follow-up of 2026-11-05

The recheck of the workflow of 17:58 found three dupes in the folder of 9,546 files. All three came from the
reader, which counted fewer products than their recipes give or missed a cheaper way to get them, so the products
looked dearer than they are. The folder stayed out of the deploy of 3.1.3 at 00:46 until they were fixed:

| Fault | Effect on the folder of 9,546 files | Fix |
|---|---|---|
| The reader took the count inside the item of a SAG mill product (`"item": {"item": ..., "count": 9}`) as 1 | The SAG mill grinds one ender pearl into nine Thermal ender pearl dusts, and the dust melted into the 250 mB ender of a whole pearl | The count inside the item counts. The dust melts into 27 mB ender |
| The reader took a sequenced assembly of Create as one product, whatever the count of its result | The phantom track of Railways, made 32 at a time from a phantom membrane and two iron ingots, melted into 180 mB iron, and the monorail track, made six at a time, into 175 mB | Each result counts with its own count. The monorail track melts into 29 mB iron, and the phantom track holds 5 mB and stays out |
| Catching a fish with a water bucket is no recipe | With Molten the salmon and tropical fish buckets melted into 270 mB iron and 180 mB totemic gold, the content of their recipes in the spawn egg group of L2 Complements, which take two totemic gold ingots. A water bucket catches the fish for nothing | The generator adds a catch for each mob bucket, which takes the water bucket whole. All six mob buckets melt into 270 mB iron in both cases |

Three more changes came with the fixes:

- A count beside a result given as a plain id, as in `"result": "<id>", "count": 4`, counts as well, and the SAG
  mill counts with the best grinding ball of the pack (see [the rules](#the-rules)).
- The new function `reader_check()` takes every recipe that the reader reads and compares the count it took for
  each product with the largest count that the recipe declares for it, at the product, inside its item or beside
  a plain id. A product below that count fails the run with exit status 1, like a dupe. Run on the reader of the
  folder of 9,546 files, it reports 81 findings in 47 recipes in each case: 32 slicing recipes of Refurbished
  Furniture, whose count stands beside a plain id, and the two tracks, each flagged in its parsed recipe and in
  its JSON, and 13 recipes of the SAG mill, the ender pearl among them. On the fixed reader it finds none.
- The obsidian items of Forbidden & Arcanus melt the same with and without Molten again. The commit b1b57633 had
  left the obsidian ingot and the Processed Obsidian Block to the recipes of SkyTekx Molten, which give no iron
  byproduct. The overrides now load in both cases, and two files switch the recipes of Molten off (see
  [dupes closed with overrides](#dupes-closed-with-overrides)).

The ball rule showed one more dupe, a machine loop: with a vibrant alloy ball the SAG mill grinds one obsidian
into up to eight Mekanism obsidian dusts, each of which melts into 250 mB obsidian by the recipe of Tinkers, and
the Mekanism combiner makes one obsidian of four dusts and a cobbled deepslate. The loop doubles obsidian without
any melting, so the audit lists it as known (see [known machine dupes](#known-machine-dupes)).

Against the folder of 9,546 files the new one has 9,530:

| Change | Files | Why |
|---|---:|---|
| Gone | 20 | The phantom track, and the files for the case with Molten of the salmon and tropical fish buckets and of 17 items of Forbidden & Arcanus that hold obsidian ingots, such as the draco arcanus gear, the dark rune, the dark nether star and the obsidian skull. These items now melt the same in both cases |
| One file for both cases | 19 | The same buckets and items, whose other file lost its condition `forge:not` of Molten |
| New | 4 | The cod, pufferfish, axolotl and tadpole buckets. No recipe made the last three, and the catch of Naturalist made the cod bucket worth nothing, since it counted the bucket of the water bucket as handed back |
| Lower | 2 | The Thermal ender pearl dust, from 250 to 27 mB ender, and the monorail track, from 175 to 29 mB iron |
| Higher | 1 | With Molten the mystical dagger of Forbidden & Arcanus gets 5 mB iron as well, from its dark rune |

No other file changed, since no product of the 32 slicing recipes and the other 12 SAG mill recipes gets a
generated recipe.

### Review of the follow-up

A review of the follow-up confirmed its counts, conditions and readers and found seven smaller faults. None was a
dupe in the folder, and all seven are fixed:

| Fault | Effect | Fix |
|---|---|---|
| The reader did not know the catalysts and augments of the Thermal machines | The nitratic igniter of Thermal Extra gives up to 15 ore chunks per raw osmium, uranium or zinc. The reader counted 7, so each chunk melted into 12 mB of its metal instead of 6 | Every unlocked chance counts with the best catalyst and augments (see [the rules](#the-rules)). The three chunks fall below 10 mB and stay out |
| Four ironwood overrides had the unit size 10 for amounts that ten does not divide | Tinkers rounds even a new piece down to whole units, so the ironwood axe melted into 130 mB iron and 10 mB gold instead of 135 and 15. The helmet and the leggings lost 5 mB of each fluid the same way, the shovel 5 mB iron | Unit size 5 for both fluids. The generator falls back to a unit that divides the amount, which changes none of its files |
| A full refund plus a gained fluid counted as a conversion | 16 findings without Molten and 15 with it, such as the beacon recipe of Tinkers | They are sources now, and the report notes the refund |
| The reader did not know the bonus of the Cyclic crusher, the fluid that the Ender IO tank empties out, the product under `byproduct` of the Iron's Spellbooks cauldron and the fluid under `mix` of the Cyclic solidifier, and it kept resonant ender apart from molten ender | No file changed. The audit lost three sources, the Tinkers casts of the ender pearl and the ender eye and the pearl of the Thermal chiller, which took resonant ender as nothing, and gained two, the ender pearl that the Cyclic crusher adds to end stone and the quartz it adds to diorite | The reader knows them all, and the summary lists the JSON keys it still does not know, 287 in each case, such as energy fuels, enchanting stats and gases |
| The owner question on Neptune's Bounty named one item of a larger group | Loot and gifts that hold metal melt as well, such as the Faraday armor of the Immersive Engineering hero gifts and the gun parts of its commandos | The question covers all loot that melts into metal now (see [open items](../open-items.md#owner-decisions)) |
| The text on the obsidian overrides said that the two switch-off files only add a condition | Wording | They replace the two conditions of the Molten jar (see [dupes closed with overrides](#dupes-closed-with-overrides)) |
| The SAG rule said that every recipe with a bonus counts twice | Wording, the code was right | It names `multiply_output` and the missing bonus type (see [the rules](#the-rules)) |

Against the folder of 9,530 files the new one has 9,527: the osmium, uranium and zinc ore chunks of Thermal Extra
are gone, and no other file changed. The audit has 196 conversions and 360 sources without Molten and 250 and 386
with it, and no new dupe. The generator gives the same 9,527 files from the dump of 23:22 and from the dump of
01:19. The runs of the review found one more wording fault: the text said that the smeltery gives out the
byproducts, which only the foundry does (see [runs of the follow-up](#runs-of-the-follow-up)).

## After the merge of 2026-11-06

The merge of master into the port branch on 2026-11-06 (f0d5096) put the fork 1.20.1-1.7.3-skytekx.1 of Create
Crafts & Additions in the place of 1.3.3 and brought the Twilight Delight recipes of the branch (e26fc06). The folder
still came from the dump of 01:19, which knew neither, and a review of the merge found files that melt into more
than the new recipes take. The fork changed three recipes, the alternator, the electrum amulet and the spool, and
added charging recipes that turn gold into electrum. Against 6.2.18 and 2.0.84, Ender IO 6.3.0-skytekx.1 adds 47
recipe files and Extreme Reactors 2.0.97 adds 2 and neither changes one, and the 24 recipes of the LittleTiles build
read the same as JSON.

A Forge test boot of the merged tree at 20:31 with a heap of 9 GB gave a new dump. The boot harness
starts a fresh world, where the fixes pack loaded after every mod pack, so the two commands of
[load order](boot-fixes.md#load-order) were not needed. The first boot, with a heap of 5 GB, had run out of memory
in the reload of the second command. The dump command failed at first with "Cannot find function getId in object
mekanism:reaction", because Create: Dragons Plus, which comes with the Create: Integrated Farming port, adds an
accessor `getRecipes()` to the recipe manager that returns the map of recipes by type, and Rhino called it. Since
30562cb the dump flattens that map. It read 48,199 recipes, 36,506 items, 737 fluids and 300 mods, and
`gen_melting.py` ran on it in a scratch copy of the tree. Only the files of two cases went into the folder:

| Item | Before | After | Why |
|---|---|---|---|
| Alternator | 630 mB copper, 618 mB iron, 90 mB zinc | 720 mB copper, 621 mB iron | The recipe of the fork has four copper spools and no capacitor, so it takes no zinc |
| Electrum amulet | 405 mB electrum, 100 mB emerald | 315 mB electrum, 100 mB diamond, 90 mB zinc | Two electrum ingots, three electrum wires, a zinc plate and diamond dust |
| Spool | 11 mB iron | None | Two iron plates and an iron rod make 24 spools, so a spool holds 9.375 mB, below the 10 mB floor |
| Copper, gold, electrum and festive spools | 11 mB iron beside their metal | 9 mB iron | The new spool |
| Electric motor, portable energy interface and tesla coil | 78, 78 and 123 mB iron | 72, 76 and 117 mB iron | The new spools |
| Electrum rod | 45 mB electrum | None | The charging recipe `createaddition:charging/electrify_gold_rod` makes it from a gold rod, which holds no electrum |
| Electric pump and servo motor | None | 517 mB copper, 90 mB zinc, 18 mB iron, and 630 mB copper, 540 mB brass, 162 mB iron, 100 mB quartz, 90 mB zinc | New items of the fork |
| Moss soup of the Twilight Forest | 1,000 mB glass | None | The Twilight Delight cooking recipe of the branch makes it from a moss patch and a bowl |

So `createaddition` keeps 24 files in each case, `twilightforest` has 20 without Molten and 29 with it, and the
folder holds 9,526 files. With the electrum rod the rod of Thermal Extra stops melting as well, since it melted only
through the tag `forge:rods/electrum` of the generated recipe. The audit of the generated recipes of the run finds
no dupe in either case. A second Forge boot at 20:47, of the tree with these files, reached Done with 0 FATAL lines,
no crash and the 3 accepted ERROR lines of a test copy, and no line of its log names a generated recipe. Its dump
held 8,317 generated recipes, one fewer than the 8,318 of the first, and gave the same 9,701 files again. Nobody has
melted the changed items in a melter yet.

The rest of the run stayed out of the folder until the full regeneration for the mods of the branch, which followed
the same evening (see [regeneration for the mods of the branch](#regeneration-for-the-mods-of-the-branch)). It gave
9,701 files. Against the folder that is 176 more, 129 of them for Actually Additions, and 15 changed
files of other mods that gain glass through dragon's breath, which Create: Dragons Plus fills into a glass bottle.
The run also drops the melon juice of Farmer's Delight, which the juicer of Expanded Delight makes with the glass
bottle in its `container` key, a key that the reader counts as nothing (`QUIET_KEYS` in `gen_melting.py`). On the
second dump, `gen_melting.py --check` names these 192 files. The audit of the run found three new dupes: the crusher
of Actually Additions grinds an iron, golden or diamond horse armor into six raw iron, raw gold or diamonds, and a
horse armor takes four ingots or diamonds in the pack. The run exited with status 1 for them.

## Regeneration for the mods of the branch

The regeneration of 2026-11-06 made the folder follow the merged tree as a whole. A Forge test boot at 20:57 of the
tree after a18f0ca, with the dump script and `tools/unify_audit_dump.js`, gave the dump of 20:59 (48,197 recipes).
The generator gave the same 9,701 files from it as the run of 20:32 and the same three dupes. Three changes
followed:

- The reader counts the container of the cooking pot of Farmer's Delight and of the juicer of Expanded Delight and
  the display stand items of the Empowerer of Actually Additions as used up (be014fc, see [the rules](#the-rules)).
  The melon juice keeps its 1,000 mB glass, and 15 more drinks, jellies and meals get a file. No empowered crystal
  changes, since its stand items hold nothing that melts.
- The three horse armor loops of the crusher of Actually Additions are known machine dupes (2a8509c, see
  [known machine dupes](#known-machine-dupes)), since they multiply raw metal and diamonds without any melting.
  Whether the crusher recipes stay is an owner decision (see [open items](../open-items.md#owner-decisions)).
- The unification gained the salt of Medieval Embroidery (6c14f16) and the hide list its three salts (7b982a3). No
  salt melts, so no file changes for them.

A second boot at 21:11 had the new folder of 9,717 files, the salt and the hide list in place. Its dump of 21:13 read
48,380 recipes, 182 generated ones more than the folder of 9,526 files gave the boot before, and the generator with
`--check` found 0 of 9,717 files that differ, so a dump with the folder in place gives the same folder. Against the
folder of 9,526 files:

| Change | Files | What |
|---|---:|---|
| New, Actually Additions | 129 | Every item of the mod that holds metal, gems, obsidian, ender or glass, such as the 16 drills at 630 mB iron, 300 mB diamond and 160 mB gold, the AIOTs, the crystals and crystal blocks, the machines, laser relays and phantom blocks. The same file serves both cases |
| New, Ender IO | 22 | The Vat, the three obelisks, the wireless charger and its two antennas, the Mind Killer, the big and big advanced item filters, the limited item filter and the two hardened conduit facades of the backport, 11 items without Molten and 13 with it |
| New, Expanded Delight | 12 | The cask, the juicer, the glass jar at 1,666 mB glass, the goat milk bottle, the cinnamon hanging sign, four juices and three jellies |
| New, Mystical Automation | 7 | All seven machines, such as the crafter at 720 mB iron and 180 mB soulium |
| New, Mekanism: Extra Modules | 3 | The three modules, the chaos anchor unit at 4,000 mB obsidian and 3,420 mB iron |
| New, Create: Integrated Farming | 2 | The lava fishing net and the Vacuum Harvester |
| New, Create: Dragons Plus | 1 | The Fluid Hatch, 180 mB copper and 30 mB iron |
| New, Extreme Reactors | 1 | The active FE energizer port of 2.0.97 |
| New, Twilight Delight | 8 | Six drinks of the cooking pot in a glass bottle, the delicious in maze, served in a phantom helmet, and the fiery blood curd, served in a bucket |
| New, dragon's breath | 6 | Create: Dragons Plus fills dragon's breath into a glass bottle, so the breath holds the 1,000 mB glass of the bottle. The breath itself and five items made of it: the dragon banner pattern, infused breath, the lingering alchemy flask, the fabric of finality and the draco arcanus arrow |
| Changed, dragon's breath | 15 | Items that hold dragon's breath gain its glass, such as 333 mB for each of the three endshelves of Apotheosis, 2,000 and 4,000 mB for the draconic and chaotic damage modules of Draconic Evolution and 1,000 mB for the dragon charge of Amendments |
| Changed, phytochemical juice | 1 | The juice of Twilight Delight keeps its 90 mB steeleaf and gains the glass of its bottle |
| Gone | 0 | The melon juice, which the run of 20:32 dropped, keeps its file |

So no file of the folder of 9,526 files is gone, and every generated item id still exists in the jars of the
branch. Some new ids get no file: the two plain conduit facades of Ender IO, the dye and dragon's breath buckets of
Create: Dragons Plus, which a bucket or a spout fills with no recipe, the roosts and the fishing net of Create:
Integrated Farming, whose recipes hold no metal or less than 10 mB of it, and the wooden and stone AIOTs and the
crops of Actually Additions. The spool and the electrum rod of Create Crafts & Additions stay out since the merge,
see [after the merge of 2026-11-06](#after-the-merge-of-2026-11-06). Bygone Nether gets no file at all. Its Gilded
Netherite Shield and its Netherite Bell are loot without a recipe, and a shield has no unit count of Tinkers. Its
netherite scrap nuggets and ingots come only from crushing withered stone and debris, so they count as made of
nothing, and its other blocks hold no metal. SkyTekx DE Fixes adds no item, and LittleTiles keeps its 4 files.

The audit of the new folder finds no new dupe in either case. It counts 39 known machine dupes in 18 melting
recipes, the 36 in 16 of before and the three horse armor loops on the raw iron and raw gold recipes of Tinkers and
on the diamond, whose recipe the Alkahestry Tome already had. Its other counts are 11 trades in each case, 201
conversions and 383 sources without Molten and 255 and 409 with it, and the generated recipes have no dupe and no
trade. The self check of the reader finds no product below the count of its recipe.

## Counts per mod

Generated recipes per item namespace, without and with Molten. Eight of the nine mods of 2026-11-04 and Flux
Networks have a row of their own, FLib gets none. The new readers added the 16 concretes to `minecraft`, the
fragments to `bloodmagic` and the universal terminal to `ae2wtlib`, and the crafting recipe of the Auto Executor
(01bd61e7) one file to `exura`. Since the golden carrot and the glistering melon slice melt into 5 mB gold (see
[dupes closed with overrides](#dupes-closed-with-overrides)), `farmersdelight` and `cyclic` lost two items each and
`farmingforblockheads` its only one. With the unification `ae2`, `advanced_ae` and `enderio` lost two items each
and `createaddition` gained three rods (see [sync with the unification](#sync-with-the-unification)). With the
follow-up `railways` lost the phantom track and `minecraft` gained four mob buckets (see
[follow-up of 2026-11-05](#follow-up-of-2026-11-05)), and with its review `thermal_extra` lost three ore chunks
(see [review of the follow-up](#review-of-the-follow-up)). After the merge of 2026-11-06 `twilightforest` lost the
moss soup, and `createaddition` lost the spool and the electrum rod and gained the electric pump and the servo
motor (see [after the merge of 2026-11-06](#after-the-merge-of-2026-11-06)). With the regeneration for the mods of
the branch six namespaces are new, `actuallyadditions`, `expandeddelight`, `mysticalautomation`,
`mekanism_extra_modules`, `create_integrated_farming` and `create_dragons_plus`, and `enderio`, `twilightdelight`,
`bigreactors` and six namespaces of items that hold dragon's breath gained items (see
[regeneration for the mods of the branch](#regeneration-for-the-mods-of-the-branch)).

| Namespace | Without Molten | With Molten |
|---|---:|---:|
| `railways` | 1,093 | 1,093 |
| `enderio` | 573 | 604 |
| `spartanweaponry` | 392 | 392 |
| `magistuarmory` | 359 | 359 |
| `botania` | 217 | 340 |
| `railcraft` | 321 | 321 |
| `medievalembroidery` | 239 | 239 |
| `thermal_extra` | 225 | 198 |
| `mysticalagriculture` | 215 | 209 |
| `cyclic` | 212 | 212 |
| `immersiveengineering` | 203 | 205 |
| `refurbished_furniture` | 204 | 204 |
| `ad_astra` | 138 | 165 |
| `mekanism` | 158 | 158 |
| `ars_nouveau` | 155 | 155 |
| `draconicevolution` | 151 | 153 |
| `dragnlivestock` | 153 | 153 |
| `create` | 148 | 148 |
| `bibliocraft` | 143 | 143 |
| `bloodmagic` | 131 | 140 |
| `actuallyadditions` | 129 | 129 |
| `thermal` | 122 | 122 |
| `ae2` | 121 | 121 |
| `industrialforegoing` | 118 | 117 |
| `quark` | 108 | 108 |
| `minecraft` | 88 | 101 |
| `spartantwilight` | 96 | 96 |
| `irons_spellbooks` | 75 | 93 |
| `bigreactors` | 77 | 74 |
| `mocreatures` | 71 | 75 |
| `megacells` | 74 | 74 |
| `reliquary` | 70 | 70 |
| `forbidden_arcanus` | 46 | 67 |
| `simplyjetpacks` | 64 | 64 |
| `l2complements` | 47 | 63 |
| `cataclysm` | 42 | 62 |
| `advanced_ae` | 60 | 60 |
| `spartanshields` | 57 | 57 |
| `supplementaries` | 57 | 57 |
| `mekanism_extras` | 50 | 51 |
| `exchangers` | 47 | 47 |
| `expatternprovider` | 47 | 47 |
| `biomesoplenty` | 44 | 44 |
| `apotheosis` | 42 | 42 |
| `aquaculture` | 29 | 42 |
| `cataclysm_spellbooks` | 24 | 42 |
| `storagedrawers` | 41 | 41 |
| `galosphere` | 17 | 37 |
| `aether_redux` | 0 | 36 |
| `exura` | 36 | 34 |
| `l2hostility` | 18 | 36 |
| `waystones` | 31 | 31 |
| `twilightforest` | 20 | 29 |
| `mekanismgenerators` | 28 | 28 |
| `framedblocks` | 27 | 27 |
| `ironchests` | 26 | 26 |
| `mob_grinding_utils` | 26 | 26 |
| `fairylights` | 25 | 25 |
| `meteors` | 6 | 25 |
| `aether` | 9 | 24 |
| `blue_skies` | 9 | 24 |
| `createaddition` | 24 | 24 |
| `redstone_arsenal` | 23 | 23 |
| `deep_aether` | 8 | 22 |
| `farmersdelight` | 22 | 22 |
| `twilightdelight` | 22 | 22 |
| `mysticalagradditions` | 21 | 18 |
| `chocolate_quest` | 20 | 20 |
| `mekanismadditions` | 20 | 20 |
| `ticex` | 20 | 20 |
| `elementary_staffs` | 17 | 17 |
| `hexxit_gear` | 17 | 17 |
| `alexsmobs` | 16 | 16 |
| `elevatorid` | 16 | 16 |
| `asr` | 15 | 15 |
| `nyx` | 1 | 15 |
| `dimdoors` | 13 | 13 |
| `wirelessredstone` | 13 | 13 |
| `appbot` | 11 | 12 |
| `expandeddelight` | 12 | 12 |
| `lavafishing` | 4 | 12 |
| `appmek` | 11 | 11 |
| `arsmagicalegacy` | 11 | 11 |
| `slashblade` | 3 | 10 |
| `fluxnetworks` | 9 | 9 |
| `nvlforcefields` | 8 | 5 |
| `aether_genesis` | 0 | 7 |
| `at_mod` | 7 | 7 |
| `computercraft` | 7 | 7 |
| `mekanismtools` | 7 | 7 |
| `morebows` | 7 | 7 |
| `mysticalautomation` | 7 | 7 |
| `secretroomsmod` | 6 | 6 |
| `ae2wtlib` | 5 | 5 |
| `chipped` | 5 | 5 |
| `chunkloaders` | 4 | 4 |
| `constructionwand` | 4 | 4 |
| `littletiles` | 4 | 4 |
| `pamhc2foodcore` | 4 | 4 |
| `soulshards` | 4 | 3 |
| `tf_dnv` | 4 | 4 |
| `tflostblocks` | 4 | 4 |
| `theoneprobe` | 4 | 4 |
| `torchmaster` | 4 | 4 |
| `additionalbanners` | 3 | 3 |
| `decocraft` | 3 | 3 |
| `mekanism_extra_modules` | 3 | 3 |
| `playerplates` | 3 | 3 |
| `twilight_crossbow` | 3 | 3 |
| `aeinfinitybooster` | 2 | 2 |
| `bountiful` | 2 | 2 |
| `create_integrated_farming` | 2 | 2 |
| `eio_legacy_machines` | 2 | 2 |
| `eplus` | 2 | 2 |
| `statues` | 2 | 2 |
| `amendments` | 1 | 1 |
| `backpack` | 1 | 1 |
| `capsule` | 1 | 1 |
| `chococraft` | 1 | 1 |
| `comforts` | 1 | 1 |
| `create_dragons_plus` | 1 | 1 |
| `doomangelring` | 1 | 1 |
| `hexxit_additions` | 1 | 1 |
| `irons_lib` | 1 | 1 |
| `irons_patreon_lib` | 1 | 1 |
| `naturalist` | 1 | 1 |
| `naturescompass` | 1 | 1 |
| `pipe_api` | 1 | 1 |
| `tinkers_reforged` | 1 | 1 |
| `traverse` | 1 | 1 |
| `trumpet_skeleton` | 1 | 1 |
| `twilightaether` | 1 | 1 |
| `voidtotem` | 1 | 1 |
| `xpbook` | 1 | 1 |

## What stays out

Of the 2,535 items of the pack whose item class makes them armor, a tool, a weapon, a shield, horse armor, an
elytra, shears or a fishing rod, counted with Molten, 172 melt already, 1,627 get a generated recipe, 49 are Tinkers
tools and 687 stay out. The nine of the branch are the six AIOTs and the two engineer's goggles of Actually
Additions and the Gilded Netherite Shield of Bygone Nether. The iron, gold, diamond and netherite AIOTs and both
goggles melt, the wooden and stone AIOTs and the shield stay out. The largest groups are Medieval Embroidery 91, Blue Skies 90, Spartan Weaponry 60, Aether
52, Magistu Armory 50 and Iron's Spellbooks 46. They hold no fluid that counts:

- Leather, cloth, wood, stone, bone or scales, such as the vanilla leather and stone gear, the gambesons and
  leather armor of Magistu Armory, and the hats of Alex's Mobs.
- Gems without a fluid, such as Aether zanite, Blue Skies aquite and the other Blue Skies gems, the Medieval
  Embroidery gemstones, lapis lazuli of Mekanism Tools, and certus and fluix quartz of Applied Energistics 2.
  Whether these gems get fluids of their own is an open owner question (see [open items](../open-items.md#owner-decisions)), so
  items made only of them stay unmeltable for now.
- Items no recipe makes and whose repair material does not melt, such as the bow, the trident and the elytra, or
  the platinum bows of Spartan Weaponry, whose recipes need platinum that the pack does not have.
- Gear whose repair tag is empty, so that it has no repair material at all. The Aether ships the repair tags of its
  dungeon gear empty, which leaves out 28 items: the valkyrie, neptune, obsidian and phoenix armor, the sentry
  boots, the valkyrie tools and lance, the flaming, holy, lightning and vampire swords, the pig slayer and the
  hammer of Kingbdogz. The 18 platinum weapons of Spartan Weaponry have the empty tag `forge:ingots/platinum`. Up to
  the fixes of the review these 46 items melted into molten bendalloy.

The leather horse armor and the Magistu Armory barding are the two horse armors that do not melt, the other 12
do. Items whose largest fluid is below 10 mB (81 without Molten, 282 with it) stay out, since the merge of 2026-11-06
the spool of Create Crafts & Additions among them. With Molten 204 of them are Ender IO clear glass variants: their glass can be made from Aether quicksoil glass, which holds nothing, so only
2 mB soularium or dark steel of the token is left. The others are Railcraft tracks, Reliquary bullets, the phantom
track of Railways and the like.
Since the golden carrot and the glistering melon slice melt into 5 mB gold, two carrots of Cyclic, the gleaming
salad and the horse feed of Farmer's Delight and the feeding trough of Farming for Blockheads fell below 10 mB as
well. The products of the 599 unreadable recipes get nothing from them.

In the dump of 01:19 AlmostUnified had replaced the item of 24 of the 8,321 generated recipes with its unified tag,
such as `forge:rods/lumium` for the lumium rod of Thermal Extra. Of these tags, 20 hold only that item. The other
four also hold variants that no recipe makes any more, `forge:dusts/ender_pearl` the dusts of AE2 and EnderIO and
`forge:rods/copper`, `forge:rods/gold` and `forge:rods/electrum` the rods of Thermal Extra, so these variants melt
like the preferred item (see [sync with the unification](#sync-with-the-unification)). AlmostUnified also adds its
tracker recipe `almostunified:skytekx3`, so KubeJS counts one recipe more than the files add.

## Dupe audit

The audit compares every melting recipe of the pack, those of Tinkers and the other mods included, with every
recipe that makes its item. A finding is a melting recipe that gives more of a fluid than one of these recipes
takes, with the ingredients at their content and expected counts for chance outputs:

- A dupe takes some of the fluid and the melting gives more of it back, with no other fluid lost on the way.
- A trade pays for the gain with another fluid that the melting does not give back.
- A conversion takes only other fluids that the melting does not give back in full. A source takes nothing that
  melts, such as an essence, or only fluids that the melting refunds in full. The beacon recipe of Tinkers is such
  a source: the melting refunds its obsidian and gains glass, since a glass that does not melt fills the recipe.

Ore processing by machines is left out, it is what the ore rates are for. The audit runs a second time with the
generated recipes in place, with ingredients at what they melt into. Both runs find no open dupe in either case,
and the self check of the reader finds no product below the count of its recipe:

| Finding | Without Molten | With Molten |
|---|---:|---:|
| New dupes | 0 | 0 |
| Dupes in the generated recipes | 0 | 0 |
| Products that the reader counts below their recipe | 0 | 0 |
| Known machine dupes, in 18 melting recipes | 39 | 39 |
| Trades | 11 | 11 |
| Conversions | 201 | 255 |
| Sources | 383 | 409 |

### Dupes closed with overrides

These melting recipes multiplied a fluid. Each is overridden at its own id in the fixes pack, with the amount that
the cheapest recipe of the item takes. With them the fixes pack holds 48 melting overrides of Tinkers, of which 47
apply without Molten and all 48 with it, since the sterling horse armor needs Molten. Two more files switch off
the obsidian recipes of SkyTekx Molten (see below).

| Recipe | Item | Before | After | Why |
|---|---|---|---|---|
| `tconstruct:smeltery/melting/ironwood/` raw, ingot, block, axes, sword, shovel and the four armor pieces | Ironwood of Twilight Forest | 90 mB iron and 10 mB gold per ingot | 45 mB iron and 5 mB gold per ingot | One raw iron, one gold nugget and a liveroot make two raw ironwood |
| `tconstruct:smeltery/melting/gloves/twilightaether_ironwood` | Ironwood gloves of Twilight Aether | 180 mB iron and 20 mB gold | 90 mB iron and 10 mB gold | The gloves take two ironwood ingots |
| `tconstruct:smeltery/melting/metal/gold/enchanted_apple` | Enchanted golden apple | 6,480 mB gold | 720 mB gold | Apotheosis crafts it from one golden apple and eight potions |
| `tconstruct:smeltery/melting/metal/gold/produce` | Golden carrot and glistering melon slice | 80 mB gold | 5 mB gold | The Cyclic solidifier makes two of them from two carrots or melon slices, one gold nugget and honey |
| `tconstruct:smeltery/melting/scorched/casting_table` | Scorched casting table | 40 mB gold | 30 mB gold | The table takes one gold bars item, which melts into 30 mB |
| `tconstruct:smeltery/melting/seared/glass_tinted` and `scorched/glass_tinted` | Seared and scorched tinted glass | 1,000 mB glass | 500 mB glass | One glass and four amethyst shards make two tinted glass |
| `tconstruct:smeltery/melting/diamond/smithing_template` | Stratus smithing template of Deep Aether | 500 mB diamond | Does not melt | One template, seven gravitite ingots and holystone make two templates |

The smithing template recipe now subtracts the item tag `skytekx3:melting/not_as_template`, which holds the
stratus template, from `minecraft:trim_templates`. The other templates still melt into 500 mB diamond.

The produce override lowers 19 generated recipes that take golden carrots or glistering melon slices, such as the
golden carrot crate of Quark from 720 to 45 mB gold and the third eye of Botania from 320 to 20 mB, and puts five
items below 10 mB. A run of the generator with the recipe of Tinkers in place of the override gave these numbers
and the Cyclic dupe again. The owner can
keep 80 mB instead by switching off the two honey recipes of the Cyclic solidifier,
`cyclic:solidifier/solidifier_honeycarrot` and `cyclic:solidifier/solidifier_honeymelon`, and removing the
override. The override has no Cyclic condition, so a server without Cyclic melts these items into 5 mB as well.

The Forbidden & Arcanus overrides `obsidian_ingot` and `processed_obsidian_block` load in both cases. SkyTekx
Molten melts both items itself, into the same molten obsidian but without the 40 mB iron per ingot that the
overrides give as a byproduct. Two files at the ids of its recipes,
`skytekx_molten:smeltery/melting/obsidian/ingot` and `skytekx_molten:smeltery/melting/obsidian/block`, therefore
switch them off with `forge:false`, so the overrides give the iron with Molten as well. Each file is the recipe of
the Molten jar with its two conditions, `forge:mod_loaded` of `forbidden_arcanus` and `forge:item_exists`, replaced
by `forge:false`. The override `obsidian_with_iron` keeps its recipe in both cases.

### Known machine dupes

These loops multiply an item without melting, so the melting recipe only shows them. The audit lists them as known
and leaves the melting alone:

| Melting recipe | Loop |
|---|---|
| Iron, gold, copper, silver, tin and steel ingots, diamond, emerald, obsidian and sand | The Alkahestry Tome of Reliquary doubles the item for redstone or glowstone charge |
| `quartz/gem` | Botania mana infusion on the conjuration catalyst doubles quartz |
| `quartz/dust` | The Ender IO SAG mill gives 1.1 powdered quartz per quartz, and Mekanism enriches the dust back into quartz |
| `obsidian/dust` | With a vibrant alloy grinding ball the Ender IO SAG mill gives up to eight obsidian dusts of Mekanism per obsidian, and the Mekanism combiner makes one obsidian of four dusts and a cobbled deepslate |
| `amethyst/cluster` | The Thermal crystallizer grows a cluster from one shard, and crushing gives seven shards back |
| `amethyst/gem` | Create crushing of a Galosphere amethyst slab gives more shards than the slab took |
| `glass/sand` | Ad Astra makes four venus sandstone of four venus sand, and the crushers of Immersive Engineering, Mekanism and Thermal make two sand of one sandstone |
| `glass/sand` | The Cyclic crusher makes one sand of one concrete powder of each of the 16 colors, and eight powder take four sand and four gravel, so gravel becomes sand |
| `metal/gold/gilded_blackstone` | The Cyclic solidifier makes gilded blackstone of two gold nuggets, a blackstone and lava, and Create crushing gives 18 gold nuggets back |
| `metal/iron/raw`, `metal/gold/raw` and `diamond/gem` | DragN Livestock crafts the iron, golden and diamond horse armor of four ingots or diamonds, a leather and a carpet, and the crusher of Actually Additions grinds each into six raw iron, raw gold or diamonds (`actuallyadditions:crushing/iron_horse_armor` and the two others), since 2026-11-06 |

The gilded blackstone loop multiplies gold ninefold without any melting. An override of the melting at 20 mB
would turn the block from an ore into a plain item and open a Blood Magic recipe that makes gold ore of it, so the
melting stays. Closing the loop itself, such as with `forge:false` on `cyclic:solidifier/gilded_blackstone`, is a
decision for the owner.

The horse armor loops of Actually Additions turn four diamonds into six with no melting at all, and four iron or
gold ingots into six raw metal, which the furnace smelts into six ingots. Upstream Actually Additions has the same
crusher recipes, which harm nothing in a pack where horse armor is loot only. Taking the three recipes out, such as
with `event.remove` in `recipe_fixes.js`, or lowering them to four raw metal or diamonds, is a decision for the
owner, see [open items](../open-items.md#owner-decisions).

### Trades for the owner

Eleven findings gain a fluid by paying with another one that the melting does not give back. They are left as they
are until the owner decides:

- The seared casting tank, fuel tank, ingot tank, fuel gauge and ingot gauge melt into glass blocks, and their
  recipes accept tinted glass through `forge:glass`. A tinted glass is worth 500 mB glass and 200 mB amethyst.
- The Ender IO SAG mill, the Immersive Engineering crusher and the Cyclic crusher turn glass, tinted glass
  included, into sand, which melts into 1,000 mB glass.
- The ARC recipe `bloodmagic:arc/wash_glass` turns any `forge:glass`, tinted glass included, into a glass block,
  which gains 500 mB glass for 200 mB amethyst.
- The Immersive Engineering recipe `crafting/tinted_glass_lead_wire` makes three tinted glass from one glass, four
  amethyst shards and four lead wires. Melting them gives 200 mB amethyst and 500 mB glass more than went in, paid
  with 180 mB lead. Of the eleven it is the one that comes closest to a dupe.
- The Mekanism nucleosynthesizer makes an end crystal from a beacon (five glass and three obsidian) and antimatter,
  and the end crystal melts into 7,000 mB glass and 250 mB ender.

## Proof on a test server

All runs used the test server `skytest-melting` on the build machine, a copy of the repo at 5a21cdc4 with the jar
of SkyTekx Molten (sha256 b83aa78c, repo edec978). The baseline lines are from the same server before the
generated files. From 19:52 its fixes pack also had the files of the commits up to 104745aa, among them the Auto
Executor recipe of 01bd61e7. From 20:08 its `mods/` also had the nine mods of 2026-11-04: the seven index jars,
downloaded from the URLs of `modrinth.index.json` and checked against their sha512, and the two jars of
`local-mods/`. A run is named by the time it started. FATAL and ERROR count the lines up to Done, or those of the
reload.

| Run | Molten | Melting recipes | Boot to Done | FATAL and ERROR | KubeJS found | Generated live |
|---|---|---|---:|---|---:|---:|
| Baseline 17:48 | No | None | 50.8 s | 0 and 3 | 37,023 | 0 |
| Baseline 18:01 | Yes | None, dump script | 50.2 s | 0 and 1 | 37,635 | 0 |
| 19:05 | Yes | JSON folder | 50.1 s | 0 and 1 | 45,576 | 7,942 |
| 19:21 | No | JSON folder | 51.3 s | 0 and 5 | 44,502 | 7,478 |
| 19:25 | Yes | One KubeJS table | 49.9 s | 0 and 1 | 37,633 and 7,945 added | 7,943 |
| 19:27 | Yes | JSON folder | 51.4 s | 0 and 1 | 45,577 | 7,943 |
| 19:36 | Yes | JSON folder of 9,181 files | 49.8 s | 0 and 1 | 45,577 | 7,943 |
| 19:52 | Yes | Same, pack files up to 104745aa, dump script | 49.3 s | 0 and 1 | 45,578 | 7,943 |
| 19:55 reload | Yes | JSON folder of 9,182 files | 4.2 s | 0 and 0 | 45,579 | 7,944 |
| 19:59 | No | Same | 50.3 s | 0 and 5 | 44,504 | 7,479 |
| 20:01 | Yes | Same | 49.7 s | 0 and 1 | 45,579 | 7,944 |
| 20:08 | Yes | Same, nine new mods, dump script | 51.3 s | 0 and 8 | 46,599 | 7,944 |
| 20:09 reload | Yes | Same, fixes pack moved last | Not timed | 0 and 0 | 46,595 | 7,944 |
| 20:25 | Yes | JSON folder of 9,653 files, final | 51.4 s | 0 and 2 | 47,053 | 8,402 |
| 20:28 | No | Same | 50.5 s | 0 and 6 | 45,976 | 7,935 |

Before the nine mods the one ERROR of the runs with Molten is AllTheLeaks, as in the baselines. The runs without
Molten add four "Unidentified mapping" lines, because the test world had been saved with the blocks, fluids and
items of Molten. After Done the dump command logs about 120 Rhino reflection ERROR lines, 114 of them at 19:52.
No log of the day has "Parsing error loading recipe", and only the probes of the tests name a generated recipe.
The first run missed one recipe, the transmog table of `irons_patreon_lib`, whose `forge:mod_loaded` condition
named a namespace that is no mod id. Since then it uses `forge:item_exists`, and every later run has all files.
KubeJS found 37,635 recipes with Molten before, minus the two Forbidden & Arcanus overrides that Molten replaces,
plus 7,943 generated recipes and the AlmostUnified tracker, which is 45,577. Without Molten it is 37,023 plus
7,478 plus the tracker, which is 44,502. A fresh dump of the server with the generated recipes gave the same 9,181
files.

From 19:52 the last column comes from the dump at 19:52 and 20:09, from a probe script at 20:25 and 20:28, and from
the count of KubeJS for the other runs:

- At 19:52 the Auto Executor recipe of 01bd61e7 added one recipe, and the dump gave one generated file more, which
  the reload of 19:55 added. Without Molten both together made 44,504.
- At 20:08 the nine mods added AdvancedAE's `ModelBakery` mixin line to the ERRORs, and since the server had put
  their packs after the fixes pack, also the two ProjectE loot tables of ExtendedAE and MEGA Cells ("Couldn't
  parse element") and four Cyclic recipes. At 20:09 the two commands of [load order](boot-fixes.md#load-order)
  moved the fixes pack last. While the pack was off between them, the reload logged 127 ERRORs, mostly loot tables
  that the pack repairs, and a warning that the tag `skytekx3:melting/not_as_template` was gone. The reload after
  the second command logged no ERROR, and KubeJS found 4 recipes fewer than at boot, as many as the Cyclic recipes
  that the fixes pack switches off.
- At 20:25 KubeJS found 46,595 minus the 7,944 old and plus the 8,402 new generated recipes, which is 47,053. The
  ERRORs were the `ModelBakery` line and AllTheLeaks. The probe found 8,402 generated recipes, 1,736 of them
  damagable, the produce override at 5 mB and the Cyclic blood slime recipe `compat/melter_blood_slimeballs`
  absent.
- At 20:28 without Molten the probe found 7,935 generated recipes, 1,582 of them damagable. Outside the folder the
  pack had 610 recipes fewer than with Molten (45,976 minus 7,935 against 47,053 minus 8,402), the same 610 as at
  19:59 and 20:01 before the nine mods.

The melts were done by commands only: a seared fuel tank with 4,000 mB lava (blazing blood for the template), a
seared melter on it and a hopper on top. The command `item replace` put the item into the hopper, since the
melter itself is no container for the command, and `data get block` read the melter tank. The melter keeps only
the result fluid. The byproducts come out of the foundry alone, since only `FoundryBlockEntity` uses the
`ByproductMeltingModuleInventory` of Tinkers 3.12.1. A smeltery gives only the result, like the melter.

On the folders of 9,181 and 9,182 files, before the nine mods:

| Item | Case | Recipe | Melter tank |
|---|---|---|---|
| Iron longsword of Spartan Weaponry | Weapon | 360 mB iron | 360 mB molten iron |
| Iron longsword at 125 of 250 damage | Damaged weapon | 360 mB iron, unit 10 | 180 mB molten iron |
| Lamellar chestplate of Magistu Armory | Armor | 330 mB steel | 330 mB molten steel |
| Lamellar chestplate at 125 of 250 damage | Damaged armor | 330 mB steel, unit 10 | 160 mB molten steel |
| Veridium pickaxe of Aether Redux at 375 of 750 damage | Damaged tool of a Molten material | 270 mB veridium | 130 mB molten veridium |
| Neptunium chestplate of Aquaculture | Molten material | 720 mB neptunium | 720 mB molten neptunium |
| Bee spawn egg, crafted with L2 Complements from totemic gold | Molten material | 180 mB totemic gold | 180 mB molten totemic gold |
| Light iron horse armor of DragN Livestock | Horse armor | 450 mB iron | 450 mB molten iron |
| Obsidian With Iron of Forbidden & Arcanus | Override | 1,000 mB obsidian, 40 mB iron | 1,000 mB molten obsidian |
| ME pattern provider of Applied Energistics 2 | Block of another mod | 360 mB iron, 90 mB gold, 50 mB quartz | 360 mB molten iron |
| Potato cannon of Create | Tool | 380 mB copper | 380 mB molten copper |
| Space suit of Ad Astra | Armor | 850 mB steel, 810 mB iron | 850 mB molten steel |
| Ironwood ingot of Twilight Forest | Closed dupe | 45 mB iron, 5 mB gold | 45 mB molten iron |
| Enchanted golden apple | Closed dupe | 720 mB gold | 720 mB molten gold |
| Netherite upgrade smithing template | Closed dupe | 500 mB diamond | 500 mB molten diamond |
| Auto Executor of Extra Utilities Reborn Again | Block of a recipe of the fixes pack | 450 mB iron, 300 mB diamond | 450 mB molten iron |

On the final folder of 9,653 files with the nine mods, at 20:27 with Molten:

| Item | Case | Recipe | Melter tank |
|---|---|---|---|
| Amethyst pickaxe of Cyclic | Tool of a new mod | 300 mB amethyst, unit 25 | 300 mB molten amethyst |
| Amethyst pickaxe at 128 of 255 damage | Damaged tool | 300 mB amethyst, unit 25 | 125 mB molten amethyst |
| Monocle of Mob Grinding Utils | Armor of a new mod | 450 mB iron, unit 10 | 450 mB molten iron |
| Assembler matrix frame of ExtendedAE | Block of a new mod | 360 mB iron | 360 mB molten iron |
| Advanced extended pattern provider of AdvancedAE | Block of a new mod | 630 mB iron, 360 mB gold, 250 mB ender, 100 mB diamond, 50 mB quartz | 630 mB molten iron |
| ME cell dock of MEGA Cells | Item of a new mod | 180 mB iron, 90 mB copper | 180 mB molten iron |
| Sky steel ingot of MEGA Cells | Reaction chamber reader | 22 mB iron | 22 mB molten iron |
| ME chemical cell housing of Applied Mekanistics | Item of a new mod | 270 mB osmium | 270 mB molten osmium |
| Infinity Range Booster of AE Infinity Booster | Item of a new mod | 875 mB ender, 405 mB iron, 270 mB netherite | 875 mB molten ender |
| Farming station of EnderIO Legacy Machines | Molten material | 940 mB soularium, 800 mB energetic alloy and six more fluids | 940 mB molten soularium |
| Wireless universal terminal of AE2 Wireless Terminals | Combine reader | 600 mB iron, 500 mB ender, 180 mB gold, 100 mB diamond, 100 mB quartz | 600 mB molten iron |
| Logic processor of Applied Energistics 2 | Reaction chamber and circuit cutter readers | 90 mB gold | 90 mB molten gold |
| White concrete | ARC reader | 500 mB glass | 500 mB molten glass |
| Golden carrot | Closed dupe | 5 mB gold | 5 mB molten gold |

Without Molten the meteorite pickaxe of Meteors melted into 180 mB iron and the twinite ingot into 117 mB
obsidian from their files for that case, the obsidian ingot of Forbidden & Arcanus into 1,000 mB obsidian from the
override, and the pattern provider again into 360 mB iron. The neptunium chestplate did not melt, as it should
not without Molten. At 20:00 the iron longsword melted into 360 mB iron and the Auto Executor into 450 mB iron. At
20:29, with the nine mods, the farming station melted into 810 mB iron from its file for that case and the
amethyst pickaxe into 300 mB amethyst.

### Runs of the review fixes

The fixes of the review were tested on the same server from 21:53 on. Its fixes pack had the files up to
104745aa and the overrides of the first folder, and its `mods/` the nine mods. It did not have the 53 essence
recipes of 0fc5a549, which replace recipes of Mystical Agriculture at their own ids with the same inputs and
results, nor Advanced Core Info, Advanced Loot Info and Flux Networks, which came later.

| Run | Molten | Melting recipes | Boot to Done | FATAL and ERROR | KubeJS found | Generated live |
|---|---|---|---:|---|---:|---:|
| Baseline 21:53 | Yes | None, dump script | 50.8 s | 0 and 2 | 38,650 | 0 |
| 22:18 | Yes | JSON folder of 9,539 files, dump script and probes | 51.6 s | 0 and 2 | 46,962 | 8,311 |
| 22:24 | No | Same, probe | 52.1 s | 0 and 6 | 45,899 | 7,858 |
| 22:52 | Yes | JSON folder of 9,540 files, final, probe | 52.1 s | 0 and 2 | 46,963 | 8,312 |
| 22:58 | No | Same | 49.8 s | 0 and 6 | 45,900 | 7,859 |

The ERRORs are the same as at 20:25 and 20:28: the `ModelBakery` line and AllTheLeaks, and without Molten the four
"Unidentified mapping" lines. No log has "Parsing error loading recipe" or a line that names a generated recipe.
At 22:18 KubeJS found the 38,650 recipes of the baseline plus 8,311 generated and the AlmostUnified tracker, and
without Molten the 38,040 of 20:28 outside the folder plus 7,858 and the tracker. The probe found 8,311 generated
recipes with Molten, 1,689 of them damagable, and 7,858 without, 1,535 damagable, the same as the files say for
each case. AlmostUnified counted 9,539 recipes more before and after its run, so it removed none of them. The
load average of the build machine was 7.0 at the end of the run without Molten.

The dump of 22:20 with the final dump script read 46,978 recipes, 49 of them with an ingredient that no item
fills, and left out 124 barriers. The generator gave the same 9,539 files from it as from the dump of 21:58
without the folder. The 116 ERROR lines after Done all fall into the five seconds of the dump command and are
Rhino reflection lines. After the two reader fixes the generator gave 9,540 files from both dumps and from the
dump of 20:09.

With Molten at 22:23, on the folder of 9,539 files:

| Item | Case | Recipe | Melter tank |
|---|---|---|---|
| Iron longsword of Spartan Weaponry | Weapon | 360 mB iron | 360 mB molten iron |
| Lamellar chestplate of Magistu Armory | Armor | 330 mB steel | 330 mB molten steel |
| Veridium pickaxe of Aether Redux at 375 of 750 damage | Damaged tool of a Molten material | 270 mB veridium | 130 mB molten veridium |
| Light iron horse armor of DragN Livestock | Horse armor, recipe of Tinkers | 450 mB iron | 450 mB molten iron |
| Obsidian With Iron of Forbidden & Arcanus | Override | 1,000 mB obsidian, 40 mB iron | 1,000 mB molten obsidian |
| ME pattern provider of Applied Energistics 2 | Block of another mod | 360 mB iron, 90 mB gold, 50 mB quartz | 360 mB molten iron |
| Neptunium chestplate of Aquaculture | Molten material | 720 mB neptunium | 720 mB molten neptunium |
| Farming station of EnderIO Legacy Machines | File for the case with Molten | 940 mB soularium and seven more fluids | 940 mB molten soularium |
| Amethyst pickaxe of Cyclic at 128 of 255 damage | Damaged tool | 300 mB amethyst, unit 25 | 125 mB molten amethyst |
| Earth essence of Ars Nouveau | Imbuement fix | None | Empty, the essence stays in the melter |
| Glyph of harm of Ars Nouveau | Imbuement fix | 540 mB iron, was 630 mB | 540 mB molten iron |
| Rune of summer of Botania | Runic altar fix | None, was 90 mB manasteel | Empty, the rune stays in the melter |
| Rune of earth of Botania | Runic altar fix, its manasteel ingot is used up | 45 mB manasteel | 45 mB molten manasteel |
| Rune of lust of Botania | Runic altar fix | 200 mB diamond, was 200 mB diamond and 135 mB manasteel | 200 mB molten diamond |
| Valkyrie chestplate of The Aether | Empty tag fix | None, was 720 mB bendalloy | Empty, the chestplate stays in the melter |
| Platinum longsword of Spartan Weaponry | Empty tag fix | None, was 180 mB bendalloy | Empty, the sword stays in the melter |
| Ender shard of Extra Utilities Reborn Again | RecipesAPI fix | 31 mB ender, was 31 mB ender and 33 mB iron | 31 mB molten ender |
| XP jelly baby of Mob Grinding Utils | Solidifier fix | None, was 80 mB gold | Empty, the jelly stays in the melter |

The probe of 22:21 also showed that the entity spawner of Mob Grinding Utils lost its 1,440 mB gold and keeps
1,710 mB iron and 750 mB ender, that the manipulation essence of Ars Nouveau has no recipe, and that with Molten the
obsidian ingot and the processed obsidian block of Forbidden & Arcanus melt by the recipes of SkyTekx Molten. Without
Molten at 22:26 the farming station melted into 810 mB iron and the twinite ingot into 117 mB obsidian from their
files for that case, the obsidian ingot into 1,000 mB obsidian from the override, the iron longsword into 360 mB
iron, the lamellar chestplate at 125 of 250 damage into 160 mB steel, the rune of lust into 200 mB diamond and the
ender shard into 31 mB ender. The neptunium chestplate, the earth essence, the valkyrie chestplate and the XP jelly
baby did not melt.

At 22:52 and 22:58 the final folder gave one recipe more than at 22:18 and 22:24, the apprentice spell book. The
probe found 8,312 generated recipes with Molten, 1,689 of them damagable, and 7,859 without, 1,535 damagable. In
both cases the apprentice spell book (Mage's Spell Book in game) and the archmage spell book melted into 1,000 mB
molten obsidian each. With Molten the iron longsword melted into 360 mB iron again, and without it the farming
station into 810 mB iron, while the neptunium chestplate stayed in the melter.

The first try of 22:22 showed old fluid in the melters, because `setblock` leaves a block that is already there
alone, block entity included, and the melters of the earlier tests still stood at the same places. Every rig was
then cleared with `fill` and built again, and each melter was read empty before its item went in.

### Runs after the sync

From 23:11 the same server ran with the files of 5e6e2d70 that reach the game, the 53 essence recipes and Flux
Networks (see [sync with the unification](#sync-with-the-unification)). Advanced Core Info and Advanced Loot Info,
which add no recipes, were still not on it.

| Run | Molten | Melting recipes | Boot to Done | FATAL and ERROR | KubeJS found | Generated live |
|---|---|---|---:|---|---:|---:|
| 23:11 | Yes | Folder of the review fixes, 9,540 files, dump script | 49.5 s | 0 and 2 | 46,972 | 8,310 |
| 23:20 | Yes | New folder of 9,546 files, dump script and probes | 49.3 s | 0 and 2 | 46,980 | 8,318 |
| 23:25 | No | Same, probe | 49.2 s | 0 and 6 | 45,917 | 7,865 |

The ERRORs before Done are the same lines as before, and no log has "Parsing error loading recipe". On the folder
of the review fixes the dump found 8,310 of the 8,312 generated recipes with Molten. AlmostUnified had turned the
three ender pearl dust recipes into the same recipe for `forge:dusts/ender_pearl` and dropped two of them as
duplicates, so all three dusts melted into 250 mB ender by the recipe of AE2. On the new folder the probe found all
8,318 with Molten, 1,689 of them damagable, and 7,865 without, 1,535 damagable, as the files say. AlmostUnified
counted 53,142 recipes before its run and 52,633 after it, 6 and 8 more than on the folder of the review fixes.
The dump of 23:22 read 46,995 recipes, and the generator gave the same 9,546 files from it. The 118 ERROR lines
after Done all fall into the five seconds of the dump command, as at 22:20.

With Molten at 23:24, on the folder of 9,546 files:

| Item | Case | Recipe | Melter tank |
|---|---|---|---|
| Flux Core of Flux Networks | New mod | 1,000 mB obsidian, 62 mB ender | 1,000 mB molten obsidian |
| Copper rod of Create Crafts & Additions | Preferred rod now | 45 mB copper | 45 mB molten copper |
| Wireless booster of Applied Energistics 2 | Lower | 135 mB iron, was 135 mB iron and 125 mB ender | 135 mB molten iron |
| Quantum alloy of AdvancedAE | Lower | 360 mB copper, was 360 mB copper and 1,000 mB ender | 360 mB molten copper |
| Copper rod of Thermal Extra | Variant, by the tag of the preferred rod | 45 mB copper | 45 mB molten copper |
| Ender dust of Applied Energistics 2 | Variant, by the tag of the Thermal dust | 250 mB ender | 250 mB molten ender |
| Shattered singularity of AdvancedAE | Gone | None, was 250 mB ender | Empty, the singularity stays in the melter |

The probe of 23:21 also showed the Flux Block at 4,000 mB obsidian and 248 mB ender, the lava immunity card at
4,320 mB copper and 180 mB netherite and the 4M cell component of MEGA Cells at 65,610 mB gold and 88 mB iron, and
no melting recipe for the quantum infused dust, the quantum entangled singularity and the enderios. Without Molten
at 23:26 the Flux Core melted into 1,000 mB obsidian, the copper rod of Create Crafts & Additions into 45 mB copper
and the wireless booster into 135 mB iron, while the neptunium chestplate stayed in the melter.

### Runs of the follow-up

The fixes of 2026-11-05 ran on a new test server, `skytest-melt5`, built from the repo of 3.1.3 with the 264 jars
of the live `mods/` and the template world of the test servers. The template world had three mod packs after the
fixes pack, so the first boot moved it last with the two commands of [load order](boot-fixes.md#load-order).

| Run | Molten | Melting recipes | Boot to Done | FATAL and ERROR | KubeJS found | Generated live |
|---|---|---|---:|---|---:|---:|
| 01:17 | Yes | Folder of 9,530 files, dump script and probes | 51.5 s | 0 and 2 | 46,983 | 8,321 |
| 01:21 | No | Same, probe | 49.4 s | 0 and 6 | 45,921 | 7,868 |
| 03:24 | Yes | Folder of 9,527 files of the review, probe and melters | 51.4 s | 0 and 2 | 46,982, then 46,980 | 8,318 |
| 03:32 | No | Same, probe | 49.2 s | 0 and 6 | 45,918 | 7,865 |

The ERRORs are the `ModelBakery` line and AllTheLeaks, and without Molten the four "Unidentified mapping" lines,
as before, and no log has "Parsing error loading recipe" or a line that names a generated recipe. The probe found
8,321 generated recipes with Molten, 1,689 of them damagable, and 7,868 without, 1,535 damagable, as the files say.
With Molten KubeJS found the 46,980 recipes of 23:20 plus the three files that the folder gained for that case,
and AlmostUnified counted 53,126 recipes before its run and 52,617 after it, 16 fewer than at 23:20, as many as
the folder has fewer files. Without Molten it counted 52,518 and 52,009. KubeJS found 45,921 recipes there, one
more than the 45,917 of 23:25 and the three files gained for that case. The one recipe is not in the folder: the
probe counted exactly the 7,868 generated recipes, and the Molten jar has no recipe outside its own namespace.

The dump of 01:19 read 46,998 recipes with 15,631 distinct ingredients, 36,084 items, 695 fluids, 4,133 item
tags and 417 fluid tags of 291 mods in 4,799 ms. No recipe was unreadable, 49 had an ingredient that no item
fills, and 124 barriers were left out. The generator of the follow-up gave the same 9,530 files from it as from
the dump of 23:22.

The probe of 01:18 with Molten and of 01:22 without it found these recipes:

| Item | Case | With Molten | Without Molten |
|---|---|---|---|
| Ender pearl dust of Thermal, ender dust of AE2 and powdered ender pearl of Ender IO | Count inside the item, by the generated recipe of the Thermal dust | 27 mB ender | 27 mB ender |
| Monorail track of Railways | Sequenced assembly | 29 mB iron | 29 mB iron |
| Phantom track of Railways | Sequenced assembly, below 10 mB | None | None |
| Water bucket and the six mob buckets | Catch | 270 mB iron | 270 mB iron |
| Obsidian Ingot of Forbidden & Arcanus | Override in both cases | 1,000 mB obsidian, 40 mB iron | 1,000 mB obsidian, 40 mB iron |
| Processed Obsidian Block | Override in both cases | 9,000 mB obsidian, 360 mB iron | 9,000 mB obsidian, 360 mB iron |
| Obsidian With Iron | Override | 1,000 mB obsidian, 40 mB iron | 1,000 mB obsidian, 40 mB iron |
| Draco arcanus sword | One file for both cases | 5,000 mB obsidian, 1,970 mB ender, 200 mB iron, 100 mB diamond | The same |
| Mystical dagger of Forbidden & Arcanus | Higher with Molten | 180 mB deorum, 120 mB obsidian, 7 mB ender, 5 mB iron | 120 mB obsidian, 7 mB ender, 5 mB iron |
| Obsidian dust of Mekanism | Known machine dupe, recipe of Tinkers | 250 mB obsidian | 250 mB obsidian |
| Precision mechanism of Create | Sequenced assembly with weighted results | None | None |
| Neptune's Bounty of Aquaculture | Molten material | 720 mB neptunium | None |
| Iron longsword of Spartan Weaponry | Weapon | 360 mB iron | 360 mB iron |
| Flux Core of Flux Networks | Item | 1,000 mB obsidian, 62 mB ender | 1,000 mB obsidian, 62 mB ender |

In real melters with Molten at 01:19 the Thermal ender pearl dust and the ender dust of AE2 melted into 27 mB
molten ender each, the monorail track into 29 mB molten iron, the salmon bucket into 270 mB molten iron and the
Obsidian Ingot into 1,000 mB molten obsidian. Without Molten at 01:27 the powdered ender pearl melted into 27 mB
molten ender, the monorail track into 29 mB and the tropical fish bucket into 270 mB molten iron. The Processed
Obsidian Block stayed in the melter, since its 9,000 mB do not fit the melter tank of 2,160 mB
(`MelterBlockEntity.TANK_CAPACITY`), so it needs a smeltery or foundry with room for them. Neptune's Bounty stayed
as well, since it has no recipe without Molten. Every melter was read empty before its item went in.

The fixes of the review ran at 03:24 with Molten and at 03:32 without it. The boot of 01:21 without the Molten jar
had dropped its mod pack, and the boot of 03:24 added it after the fixes pack again. So KubeJS found 46,982
recipes at first, two more than expected, since the two obsidian recipes of Molten came back over the two files
that switch them off. The two commands of [load order](boot-fixes.md#load-order) moved the fixes pack last at
03:29, and KubeJS found 46,980, three fewer than at 01:17, as many as the folder has fewer files. That reload
logged the same 13 advancement errors of other mods as the one at 01:15. Without Molten KubeJS found 45,918,
again three fewer than at 01:21. The live server has the fixes pack last, 308th of 309 packs with Molten at 299,
and needs no move.

The probe at 03:30 with Molten and at 03:33 without it found the recipes of the table above again, and these for
the review:

| Item | With Molten | Without Molten |
|---|---|---|
| Osmium, uranium and zinc ore chunks of Thermal Extra | None | None |
| Ironwood axe, override | 135 mB iron, 15 mB gold | The same |
| Ironwood helmet, override | 225 mB iron, 25 mB gold | The same |
| Ironwood leggings, override | 315 mB iron, 35 mB gold | The same |
| Ironwood shovel, override | 45 mB iron, 5 mB gold | The same |
| Faraday chestplate of Immersive Engineering | 720 mB aluminum | The same |

The ironwood pieces match the recipes of Twilight Construct as well, which give 90 mB molten ironwood per ingot.
In real melters with Molten at 03:30 the ironwood axe took the override and melted into 135 mB molten iron, not
the 130 mB of the unit size 10, and the 15 mB gold stayed out as a byproduct. The ironwood shovel took the recipe
of Twilight Construct and melted into 90 mB molten ironwood, its whole ingot. The iron longsword melted into
360 mB molten iron. The osmium ore chunk stayed in its melter, which did not heat up, since no recipe takes it.
The melters ran with Molten only, since the four items have the same recipes without it, and every spot of the
rig was air before and after it.

### Runs of the regeneration of 2026-11-06

The regeneration for the mods of the branch ran on the boot harness of the session, which builds a fresh run folder
of the tree with the jars of the index and `local-mods/`, a new world of the seed 12345, Forge 47.4.18, Temurin
17.0.21 and a heap of 9 GB, and stops the server with `save-all flush` and `stop` over RCON. The fixes pack was the
last pack in both runs, so the two commands of [load order](boot-fixes.md#load-order) were not needed. Each run also
ran `tools/unify_audit_dump.js`, see [AlmostUnified](../modpack/almostunified.md#audit-of-the-merged-mod-set).

| Run | Melting recipes | Boot to Done | FATAL and ERROR | Dump | Generated live |
|---|---|---:|---|---:|---:|
| 20:57 | Folder of 9,526 files, the tree after a18f0ca | 115 s | 0 and 3 | 48,197 recipes | 8,317 |
| 21:11 | New folder of 9,717 files, salt, skip tags and hide list | 112 s | 0 and 3 | 48,380 recipes | 8,499 |
| 21:32, Arclight | The committed tree of 1600c9e without the dump scripts | 121 s | 0 and 3 | None | Not dumped |

The ERRORs are the three of a test copy, AllTheLeaks, the `ModelBakery` line of AdvancedAE and the backup config line
of Nostalgic Tweaks, and the runs stopped clean 4 s or 6 s after `stop`. The Arclight run used the hardened jar
`arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar` of the session, and AlmostUnified counted the same 54,473 and
53,954 recipes there as at 21:11 on Forge. Against the Arclight boot of the branch before the merge
(`actuallyadditions-arclight`) the boot diff finds no new log signature. No log has "Parsing error loading recipe" or a
line that names a generated recipe, and the boot diff finds no new log signature. Generated live counts the
recipes `skytekx3:melting/` in the dump, 1,694 of them damagable at 21:11. AlmostUnified counted 192 recipes more
before its run at 21:11, the 191 new files and the melon juice, since it counts the files before their conditions.

At 21:14 the second run built 14 rigs above the spawn by commands, a seared fuel tank with 4,000 mB lava, a seared
melter on it and a hopper on top, put one item into each hopper with `item replace` and read each melter with
`data get block` 120 s later. Every melter had been empty before, and every hopper was empty after:

| Item | Case | Recipe | Melter tank |
|---|---|---|---|
| Vat of Ender IO | New block of the backport, file for the case with Molten | 1,200 mB iron and three alloys | 1,200 mB molten iron |
| Iron casing of Actually Additions | Item of a new mod | 360 mB iron | 360 mB molten iron |
| Drill core of Actually Additions | Item of a new mod | 360 mB iron | 360 mB molten iron |
| Iron AIOT of Actually Additions | Tool of a new mod | 990 mB iron, unit 10 | 990 mB molten iron |
| Iron AIOT at 500 of 1,000 damage | Damaged tool | 990 mB iron, unit 10 | 490 mB molten iron |
| Electric pump of Create Crafts & Additions | New block of the fork | 517 mB copper, 90 mB zinc, 18 mB iron | 517 mB molten copper |
| Electric servo motor of Create Crafts & Additions | New block of the fork | 630 mB copper and four more fluids | 630 mB molten copper |
| Lava fishing net of Create: Integrated Farming | Item of a new mod | 330 mB iron | 330 mB molten iron |
| Crafter of Mystical Automation | Block of a new mod | 720 mB iron, 180 mB soulium | 720 mB molten iron |
| Juicer of Expanded Delight | Block of a new mod | 540 mB iron | 540 mB molten iron |
| Fluid Hatch of Create: Dragons Plus | Block of a new mod | 180 mB copper, 30 mB iron | 180 mB molten copper |
| Melon juice of Farmer's Delight | Kept by the container reader | 1,000 mB glass | 1,000 mB molten glass |
| Apple juice of Expanded Delight | Juicer with its container | 1,000 mB glass | 1,000 mB molten glass |
| Torchberry juice of Twilight Delight | Cooking pot with its container | 1,000 mB glass | 1,000 mB molten glass |

The byproducts come out of the foundry only, so the melters show the result fluid alone. The test world is gone
with the run folder, the dumps, logs and the command output are kept under `regen/` and `packboot-logs/regen-*` of
the scratch folder of the session.

### Recipe sync to the clients

Every client gets the whole recipe list at login in one recipe packet. A probe at 22:02, 22:21 and 23:22 serialized it
with the splitter of Forge (`VanillaPacketSplitter`), which cuts every packet above 8 MiB into parts of about
1 MiB for Forge clients. The bytes were deflated offline at the level of the network compression.

| Recipes | Packet | Parts | Deflated |
|---|---:|---:|---:|
| 38,666 without the folder | 11,702,406 bytes | 12 | 2,284,189 bytes |
| 46,978 with the folder of 9,539 files | 12,509,385 bytes | 12 | 2,577,911 bytes |
| The 8,311 generated recipes alone | 805,702 bytes | 1 | 183,113 bytes |
| 46,995 at 23:22, after the sync, with the folder of 9,546 files | 12,527,084 bytes | 12 | 2,564,447 bytes |

The packet was above 8 MiB and split for every client before the folder already, so the folder adds 0.8 MB and
about 0.3 MB on the wire and no new kind of packet. The folders of the follow-up and its review differ from the
one of 9,546 files by at most three recipes with Molten, so they were not measured again. Packet Fixer of the
pack also lifts the size limits (`allSizesUnlimited=true`). No client joined a test server: the only clients on
the build machine are the owner's instances, which the tests leave alone. The login with JEI and the Tinkers
recipe pages is therefore part of the check after the deploy.

## One JSON folder or one KubeJS table

The recipes ship as 9,717 JSON files in the fixes pack, which is the default. For the comparison at 19:25 the 9,181
recipes of that time were turned into one KubeJS script of 2.7 MB that adds them with `event.custom`:

| Point | JSON folder | KubeJS table |
|---|---|---|
| Boot to Done | 49.2 s to 52.1 s, against 50.2 s and 50.8 s without it | 49.9 s |
| Cost at boot | Parsed with the other datapack recipes, no measurable time | 0.19 s to load the script and 95 ms for the table, the KubeJS recipe event took 717 ms against 488 ms to 648 ms with the folder |
| Molten and mod checks | Forge conditions in each file | Hand written checks in the script |
| AlmostUnified | Sees and unifies the recipes | Runs before the table and never sees it |
| Git | 9,717 files under `world/`, added with `git add -f` | One file in `kubejs/server_scripts/` |
| Live server | Copied with the datapack, `/reload` is enough | Needs KubeJS and a reload of the scripts |

Both forms gave all 7,943 recipes of the folder at that time. The JSON folder stays, it needs no script, keeps the
conditions next to each recipe and is what the generator checks with `--check`.

## Running it again

After SkyTekx Molten, the mod set or the config of AlmostUnified changes, the recipes are generated again from a
new dump:

1. Put the new Molten jar and any new mods into `mods/` of a test server with the current repo, copy
   `tools/melting_dump.js` to `kubejs/server_scripts/zz_melting_dump.js` there and boot it.
2. With new mods, move the fixes pack last with the two commands of [load order](boot-fixes.md#load-order), since
   the server puts the packs of new mods after it. After a save, `python3 -I tools/datapack_order.py <level.dat>`
   lists the order.
3. Run `kubejs custom_command melting_dump` on its console and wait for "MELTDUMP done" in
   `logs/kubejs/server.log`. A reload rewrites that log, so the command runs after the last reload. Then remove
   the script and stop the server.
4. From the repo root, run the one command, here for the test server `skytest-melting`:

```
python3 -I tools/gen_melting.py ~/skytest-melting/kubejs
```

It prints the counts per world and mod, the unreadable recipe types, the fluids left out, the JSON keys that the
reader does not know and the audit, and rewrites the folder. Exit status 1 means a new dupe or a product that the
reader counts below its recipe, both named in the summary. A dupe is closed with an override in the fixes pack at
the recipe's own id, or listed in `KNOWN_DUPES` of the script when a machine loop makes it without melting. A low
product needs a fix of the reader for its recipe type, as the SAG mill and the sequenced assembly did on
2026-11-05. Then the command runs again. With `--check` it changes nothing and exits 1 when the folder differs
from what the dump gives. A dump of a server that already has the generated recipes and overrides gives the same
files. The new folder then goes into git with `git add -f`, since `world/` is ignored, and to the live server
with the datapack.

New mods can bring recipe types that the reader does not know. The summary lists them among the unreadable
recipes, and their products lose their melting until the reader learns them, as the processors of Applied
Energistics 2 would have on 2026-11-04. Comparing the per mod counts with the previous run shows such losses.
A new machine that keeps some of its inputs, such as on pedestals or in a mould slot, needs an entry in
`KEPT_KEYS` or `RETURNED_ITEMS` of the script, or its products melt into more than they take. A machine that uses up
an input under a key that the reader counts as nothing, such as the container of a cooking pot, needs an entry in
`USED_KEYS`, or its products melt into less than they take. A recipe type that
uses up a container whole, such as a catch with a water bucket, needs an entry in `WHOLE_TYPES`, or the crafting
remainder of the container is subtracted. Its code shows which inputs it uses up. The summary also lists the
recipes left out because an ingredient matches no item, and a recipe there that works in game points to a reader
fault, as the apprentice spell book did. A JSON key in the list of unknown keys that names a product or an input
that the machine uses up needs a reader entry as well, as `byproduct` and `mix` did in the review of the
follow-up.

The dump leaves the barrier out of every item list, and the generator drops it from the dumps of older versions of
`melting_dump.js` as well. The final generator gave the same 9,540 files from three dumps: the one of 20:09 made
with the first script, the one of 21:58 without the folder and the one of 22:20 with it. On the repo of 5e6e2d70
with Flux Networks it gives 9,546 files, from the dump of 23:12 on the folder of the review fixes and from the
dump of 23:22 on the new one, while the dump of 22:20 still gives the folder of the review fixes. A new config of
AlmostUnified therefore needs a new dump as well, since the variants it swaps away lose their recipes. The
generator of the follow-up gave the same 9,530 files from the dump of 23:22 and from the dump of 01:19 on the
new folder, and after its review it gives 9,527 files from both. Both dumps are kept on the build machine under
`~/skytekx3-dumps/`, in `melting-2026-11-04-2322/` and `melting-2026-11-05-0119/`, about 45 MB each. The generator of
the regeneration of 2026-11-06 gives the same 9,717 files from the dump of 20:59 without the new folder and from the
dump of 21:13 with it. Those two dumps, about 46 MB each, stayed in the scratch folder of the session under
`regen/dumps/base/` and `regen/dumps/r1/` and were not copied to the build machine.
