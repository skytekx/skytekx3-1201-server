#!/usr/bin/env python3
"""Generate Tinkers' Construct melting recipes for every item that holds something meltable.

The script reads a dump of a real test server boot (written by tools/melting_dump.js, with SkyTekx Molten in mods/)
and writes one melting recipe per item that holds metal, gem, obsidian, glass, ender, debris or Molten fluid but does
not melt yet. It uses the standard library only and gives the same output for the same dump and fixes pack.

Usage, from the repo root:

    python3 -I tools/gen_melting.py <dump folder>                 # Write the recipes and print the summary
    python3 -I tools/gen_melting.py <dump folder> --check         # Change nothing, exit 1 when the folder differs
    python3 -I tools/gen_melting.py <dump folder> --report <file> # Also write the full report as JSON

The dump folder is kubejs/ of the test server, where the dump script writes recipes, recipe_json, items, fluids, tags
and mods as melting_dump_<name>.json, or a copy of these files with or without the prefix. The output
folder is world/datapacks/skytekx3_fixes/data/skytekx3/recipes/melting/ (option --out). The script deletes and rewrites
only that folder. Its own recipes in a dump are ignored, and the melting overrides and item tags of the fixes pack
(option --pack) take the place of the dump's recipes and tags with the same ids, so a dump made before an override
was added gives the same result as one made after it.

Exit status 1 means that the audit found a new dupe, that the reader counts fewer products than a recipe gives, or
with --check that the folder differs. The files are written in that case too, and the summary names every new dupe. A dupe is closed with an override in the fixes pack at the
recipe's own id, or listed in KNOWN_DUPES when a machine loop makes it without melting.

The rules, in short (the full text is on okf/content/melting.md):

- The content of an item is a vector of fluids in mB. An item that already melts has the content of its melting recipe,
  the smallest one per fluid when several match. Recipes of one item that share no fluid are separate families (Twilight
  Construct melts ironwood into molten ironwood, Tinkers into iron and gold), and the family with the largest amount
  counts. An ore counts with its base amount without byproducts.
- Any other item has the content of its cheapest recipe: the sum of its ingredients divided by the result count, with
  the minimum per fluid over the alternatives of a slot and over the recipes of the item. A casting recipe is a recipe
  whose ingredient is the fluid. Crafting remainders are subtracted, except in a recipe that uses up the container
  whole. A product with a chance counts with its largest count, a product of the Ender IO SAG mill with what the best
  grinding ball gives, a product of a Thermal machine with the best catalyst and augments, and a sequenced assembly
  with the count of one result. A fluid product takes its content with it. Items no recipe makes are worth
  nothing, and so are items that only a loop of their own recipes makes.
- A water bucket that catches a fish, an axolotl or a tadpole counts as a recipe of the mob bucket, which no recipe
  shows. The mob bucket then holds the iron of its bucket and nothing more.
- An ingredient that gets a generated recipe counts with what that recipe gives, rounded down to whole mB, so no item
  melts into more than its ingredients melt into. Where a loop such as ingot and nuggets meets an amount that nine does
  not divide, the ingot drops to the next multiple of nine.
- Recipes whose ingredients the parsed recipe hides are read from their JSON. Inputs the reader cannot place count as
  nothing, and a recipe the reader cannot read at all gives its product nothing, so an unreadable recipe never leaves
  a cheaper path open. A recipe with several products gives a product that does not melt nothing.
- Inputs that the machine keeps or hands back count as nothing: the pedestal items of Ars Nouveau imbuement, the mould
  of the Mob Grinding Utils solidifier, the lens of the Industrial Foregoing laser drills, every rune on the runic altar
  of Botania, and the tool that a durability recipe of Cucumber, Bookshelf or RecipesAPI damages and hands back.
- A recipe with an ingredient that no item fills, such as an empty tag that Forge shows as a barrier, is left out,
  since nobody can make it. The dump leaves every barrier out of the item lists.
- Without SkyTekx Molten the same is computed again with its fluids and recipes removed. An item whose content differs
  gets two files, one per case, with forge:mod_loaded and forge:not conditions on skytekx_molten.
- The largest fluid is the result and the others are byproducts. Items with durability use damagable_melting. An item
  whose largest fluid is below 10 mB stays out, and so do Tinkers items, tools and parts.
- Gear that nothing makes melts by its repair material at the unit counts Tinkers uses for vanilla gear.
- Every recipe has forge:mod_loaded for the mod of its item (forge:item_exists when the item namespace is no mod id) and
  for the mod of every fluid outside Tinkers.

The audit compares every melting recipe, those of Tinkers and the other mods included, with every recipe of its item
and sorts the findings into dupes, trades, conversions and sources (see audit()). It runs a second time with the
generated recipes in place, where a dupe would be a fault of this script. Both use the counts of the reader, so a self
check compares every count the reader gives a product with the result count of the parsed recipe and with the counts
in its JSON (see reader_check()).
"""
import argparse
import collections
import json
import math
import os
import shutil
import sys

PACK_DEFAULT = 'world/datapacks/skytekx3_fixes'
OUT_DEFAULT = PACK_DEFAULT + '/data/skytekx3/recipes/melting'
OWN_PREFIX = 'skytekx3:melting/'
MOLTEN = 'skytekx_molten'
INF = None  # An item that no recipe reaches yet

# Fluids that count as content: everything that melting recipes give, except these organic, slime, food, clay, stone
# and prismarine fluids. The owner's list (2026-11-04 17:35) is metals, gems, obsidian, glass, ender and debris.
EXCLUDED_FLUIDS = {
    'minecraft:water', 'minecraft:lava', 'minecraft:milk',
    'tconstruct:earth_slime', 'tconstruct:sky_slime', 'tconstruct:ender_slime', 'tconstruct:blazing_blood',
    'tconstruct:magma', 'tconstruct:venom', 'tconstruct:honey', 'tconstruct:beetroot_soup',
    'tconstruct:mushroom_stew', 'tconstruct:rabbit_stew', 'tconstruct:meat_soup', 'tconstruct:liquid_soul',
    'tconstruct:molten_clay', 'tconstruct:seared_stone', 'tconstruct:scorched_stone', 'tconstruct:molten_porcelain',
    'tinkers_reforged:charred_prismarine',
}
GEM_FLUIDS = {'tconstruct:molten_diamond', 'tconstruct:molten_emerald', 'tconstruct:molten_quartz',
              'tconstruct:molten_amethyst'}
# Tinkers ore rates of config/tconstruct-common.toml: 12 nuggets per ore unit in the melter and smeltery, 8 shards per
# gem, the foundry 9 and 4 plus its byproducts. Only the dupe audit uses them.
ORE_RATE = {'METAL': 12 / 9, 'GEM': 8 / 4}
# Units of the repair material that Tinkers melts vanilla gear into (helmet 450 mB of iron and so on)
GEAR_UNITS = {'HELMET': 5, 'CHESTPLATE': 8, 'LEGGINGS': 7, 'BOOTS': 4,
              'sword': 2, 'pickaxe': 3, 'axe': 3, 'shovel': 1, 'hoe': 2}
GEAR_TAGS = [('minecraft:swords', 'sword'), ('forge:tools/swords', 'sword'),
             ('minecraft:pickaxes', 'pickaxe'), ('forge:tools/pickaxes', 'pickaxe'),
             ('minecraft:axes', 'axe'), ('forge:tools/axes', 'axe'),
             ('minecraft:shovels', 'shovel'), ('forge:tools/shovels', 'shovel'),
             ('minecraft:hoes', 'hoe'), ('forge:tools/hoes', 'hoe')]
EPS = 1e-6
# An item whose largest fluid is below one nugget stays out (Ender IO glass with a trace of a token and the like)
MIN_RESULT = 10
# Dupes that need no melting: the recipe makes more of its own input, or a loop of machines does. Melting only shows
# them, so the audit lists them as known and leaves the melting recipes alone. The keys are recipe ids, recipe types
# or the items that the loop makes.
KNOWN_DUPES = {
    'reliquary:alkahestry_crafting': 'The Alkahestry Tome doubles the item itself for redstone or glowstone charge',
    'botania:mana_infusion/quartz_dupe': 'Mana infusion on the conjuration catalyst doubles quartz itself',
    'thermal:machines/crystallizer/crystallizer_amethyst_cluster':
        'The crystallizer grows a cluster from one shard, and crushing gives seven shards back without melting',
    'create:crushing/amethyst_cluster':
        'The crystallizer grows a cluster from one shard, and crushing gives seven shards back without melting',
    'ad_astra:venus_sand':
        'Ad Astra makes four sandstone of four sand, and the crushers make two sand of one sandstone without melting',
    'enderio:sag_milling/quartz':
        'The SAG mill gives 1.1 powdered quartz per quartz, and Mekanism enriches the dust back into quartz',
    'enderio:sag_milling/obsidian':
        'With a grinding ball the SAG mill gives up to eight obsidian dust per obsidian, and the Mekanism combiner '
        'makes one obsidian of four dust and a deepslate cobblestone without melting',
    'create:crushing/compat/galosphere/amethyst':
        'Crushing a Galosphere amethyst slab gives more shards than the slab took, without melting',
    'cyclic:solidifier/gilded_blackstone':
        'The Cyclic solidifier makes gilded blackstone of two gold nuggets, and Create crushing gives 18 back',
}
for _color in ('white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
               'purple', 'blue', 'brown', 'green', 'red', 'black'):
    KNOWN_DUPES['cyclic:crusher/concrete_%s_powder' % _color] = (
        'The Cyclic crusher makes one sand of one concrete powder, and eight powder take four sand and four gravel, '
        'so the gravel becomes sand without melting')

# Recipe types whose parsed ingredients are the whole cost: crafting, cooking and the stonecutter, and the modded
# crafting types that extend them. Every other type is read from its JSON.
CRAFTING_TYPES = {
    'minecraft:crafting_shaped', 'minecraft:crafting_shapeless', 'minecraft:smelting', 'minecraft:blasting',
    'minecraft:smoking', 'minecraft:campfire_cooking', 'minecraft:stonecutting', 'create:mechanical_crafting',
    'cucumber:shaped_no_mirror', 'cucumber:shaped_transfer_nbt', 'backpack:crafting_shapeless_keep_nbt',
    'backpack:crafting_shaped_keep_nbt', 'naturalist:crafting_shapeless_nbt', 'computercraft:impostor_shaped',
    'computercraft:impostor_shapeless', 'dimdoors:shaped_tesselating', 'immersiveengineering:shaped_fluid',
    'spartanweaponry:tag_blasting', 'oreberriesreplanted:blasting', 'railcraft:blasting',
    # The universal terminal of AE2 Wireless Terminals: two terminals combined, or one added to a universal terminal
    'ae2wtlib:combine', 'ae2wtlib:upgrade',
    # The spell book upgrades of Ars Nouveau: a ShapelessRecipe that only copies the spells. The JSON of the
    # apprentice book also has a pattern with an item that no longer exists, which the recipe never reads.
    'ars_nouveau:book_upgrade',
}
# Crafting types that hand a damageable ingredient back, so a tool in them costs nothing. RecipesAPI, bundled in Extra
# Utilities Reborn Again, hands every damageable stack back with one more point of damage (getRemainingItems of
# DurabilityShapelessRecipe and DurabilityShapedRecipe), such as the glass cutter of exura:ender_shard.
DAMAGE_TYPES = {'cucumber:shaped_transfer_damage', 'bookshelf:shapeless_durability',
                'recipesapi:durability_shapeless', 'recipesapi:durability_shaped'}
# Inputs that a machine keeps, per recipe type: the JSON keys (lower case) whose items stay where they are. Each one was
# read in the mod's code on 2026-11-04.
KEPT_KEYS = {
    # ImbuementTile puts the result into its own slot and takes source. The items on the pedestals stay.
    'ars_nouveau:imbuement': {'pedestalitems'},
    # The XP solidifier of Mob Grinding Utils drains XP fluid and leaves the mould in its slot.
    'mob_grinding_utils:solidify': {'ingredient'},
    # The laser drill of Industrial Foregoing only tests its lens to weight the products.
    'industrialforegoing:laser_drill_ore': {'catalyst'},
    'industrialforegoing:laser_drill_fluid': {'catalyst'},
}
# Items that a machine hands back after the craft, per recipe type, as an item kind of the dump and an item tag for dumps
# without that kind. The runic altar of Botania spawns every RuneItem it took again (RunicAltarBlockEntity).
RETURNED_ITEMS = {'botania:runic_altar': ('botania_rune', 'botania:runes')}
# Recipe types that use up a container whole, so the crafting remainder of an ingredient does not come back: the catch
# of a mob in a water bucket, and the Naturalist net recipe that shows the catch of a cod
WHOLE_TYPES = {'naturalist:net', 'skytekx3:catch'}
# Mobs that a water bucket catches in the world (MobBucketItem). No recipe shows these catches except the cod's, so the
# generator adds one recipe per mob from the water bucket to the mob bucket. Without them the L2 Complements recipes
# of the salmon and tropical fish buckets, which take totemic gold, would set what the free buckets melt into.
CAUGHT_MOBS = ('cod', 'salmon', 'pufferfish', 'tropical_fish', 'axolotl', 'tadpole')
# Forge shows an empty tag in an ingredient as a barrier named after the tag, and dumps made before 2026-11-04 22:00
# hold it in their item lists. No player can get a barrier, so the barrier counts as no item at all: an ingredient
# that matches only the barrier is one that no item fills, and the recipe is left out.
PLACEHOLDER = 'minecraft:barrier'
# Recipe types that make no item from other items, or only change NBT, or are Tinkers tool building
SKIP_TYPES = {
    'minecraft:smithing_trim', 'quark:smithing_rune', 'almostunified:client_recipe_tracker',
    'tconstruct:modifier', 'tconstruct:incremental_modifier', 'tconstruct:swappable_modifier',
    'tconstruct:multilevel_modifier', 'tconstruct:overslime_modifier', 'tconstruct:modifier_salvage',
    'tconstruct:modifier_set_worktable', 'tconstruct:material', 'tconstruct:material_fluid',
    'tconstruct:material_melting', 'tconstruct:melting_fuel', 'tconstruct:entity_melting', 'tconstruct:severing',
    'tconstruct:alloy', 'tconstruct:tool_building', 'tconstruct:part_builder', 'tconstruct:item_part_builder',
    'tconstruct:part_builder_recycling', 'tconstruct:part_builder_tool_recycling', 'tconstruct:tinker_station_damaging',
    'tconstruct:material_value_swapping', 'tconstruct:fixed_material_swapping', 'tconstruct:table_casting_material',
    'tconstruct:basin_casting_material', 'tconstruct:table_casting_composite', 'tconstruct:basin_casting_composite',
    'tconstruct:table_tool_casting', 'tconstruct:basin_tool_casting', 'tconstruct:casting_table_part_swapping',
    'tconstruct:basin_casting_part_swapping', 'tconstruct:table_filling', 'tconstruct:basin_filling',
    'tconstruct:casting_table_potion', 'tconstruct:table_duplication', 'tconstruct:basin_duplication',
    'tconstruct:molding_table', 'tconstruct:molding_basin',
    'mekanism:mek_data', 'mekanism:energy_conversion', 'mekanism:gas_conversion', 'mekanism:infusion_conversion',
    'mekanism:pigment_extracting', 'mekanism:pigment_mixing', 'mekanism:rotary', 'mekanism:oxidizing',
    'mekanism:dissolution', 'mekanism:separating', 'mekanism:evaporating', 'mekanism:activating',
    'mekanism:centrifuging', 'mekanism:washing', 'twilightforest:no_template_smithing',
    'galosphere:preserved_transform_recipe',
}
# JSON keys whose item references the recipe consumes
IN_KEYS = {'ingredient', 'ingredients', 'input', 'inputs', 'iteminput', 'maininput', 'extrainput', 'base', 'addition',
           'template', 'materials', 'pedestalitems', 'reagent', 'inputitems', 'input0', 'input1', 'input2', 'input3',
           'waste', 'additives', 'catalyst', 'center', 'items', 'core', 'components', 'baseinput', 'addedinput',
           'primary', 'secondary', 'projectile', 'essences'}
# JSON keys whose references are products, plus result1, result2 and so on (Statues)
OUT_KEYS = {'result', 'results', 'output', 'outputs', 'mainoutput', 'secondaryoutput', 'secondaryoutputs',
            'secondaries', 'itemoutput', 'outputitem', 'outputitems', 'product', 'products', 'byproduct', 'byproducts',
            'addedoutput', 'stripped', 'slag', 'ores', 'spoils', 'result1', 'result2', 'result3', 'result4'}
# The machines of Thermal Series and Thermal Extra (read 2026-11-05 in thermal_core 11.0.6, cofh_core 11.0.2 and
# ThermalExtra 3.3.3). RecipeJsonUtils.parseItemChance locks an output without a chance and one marked locked. Every
# other chance c of the item output at index i becomes max(c * catalyst mod * machine mod, min chance) in
# BaseMachineRecipe and CatalyzedMachineRecipe, with the primary mods at index 0 and the secondary mods after it, and
# MachineBlockEntity.resolveOutputs gives floor of that for sure and one more stack at the rest. The catalysts are the
# recipes of the type <machine type>_catalyst. No augment raises the primary mod. The four augment slots of
# ThermalCoreConfig.machineAugments with the Auxiliary Process Sieve of the Abyssal tier of Thermal Extra (MachineSec
# 0.15 plus 0.05 per tier, tier 5, additive and not unique) raise the secondary mod to 1 + 4 * 0.4.
THERMAL_NAMESPACES = {'thermal', 'thermal_extra'}
THERMAL_MACHINE_MODS = (1.0, 1.0 + 4 * 0.4)
THERMAL_OUT_KEYS = ('result', 'results', 'output', 'outputs')
# JSON keys whose fluid references the recipe consumes, with mix of the Cyclic solidifier
FLUID_IN_KEYS = {'fluid', 'fluidinput', 'inputfluid', 'fluids', 'fluid_input', 'mix'}
# Keys in an IN_KEYS list that hold one ingredient each. Under a singular key a list is a list of alternatives.
LIST_OF_SLOTS = {'ingredients', 'inputs', 'input', 'materials', 'pedestalitems', 'inputitems', 'additives', 'items',
                 'components', 'secondary', 'essences'}
# Keys of other data that hold no ingredient, so the reader does not count them as unknown
MELT_TYPES = {'tconstruct:melting', 'tconstruct:damagable_melting', 'tconstruct:ore_melting'}
# Keys of a recipe JSON that a recipe defined in code still has
CODE_KEYS = {'type', 'conditions', 'group', 'category', 'show_notification'}
QUIET_KEYS = {'type', 'conditions', 'fabric:load_conditions', 'energy', 'time', 'processingtime', 'experience',
              'group', 'category', 'show_notification', 'chemicalinput', 'gasinput', 'gasoutput', 'tool', 'tools',
              'render', 'soil', 'container', 'color', 'sound', 'spell', 'rarity', 'dimensions', 'entities', 'mainhand',
              'stat_changes', 'modifier_predicate', 'ammo', 'pattern', 'key', 'sequence', 'transitionalitem',
              'filter', 'texture', 'trunk', 'leaves', 'chemicaloutput', 'pigmentoutput', 'slurryoutput'}


def ns(rid):
    return rid.split(':', 1)[0]


def rl(rid):
    """An id of a recipe JSON as Minecraft reads it: one without a namespace is in minecraft."""
    return rid if ':' in rid else 'minecraft:' + rid


# ---------------------------------------------------------------------------------------------------------------------
# Loading the dump


class Dump:
    def __init__(self, folder):
        def path(name):
            # The plain names of a copied dump, or the names that the dump script gives them in kubejs/
            plain = os.path.join(folder, name)
            return plain if os.path.exists(plain) else os.path.join(folder, 'melting_dump_' + name)

        def load(name):
            with open(path(name), encoding='utf-8') as fh:
                return json.load(fh)
        rec = load('recipes.json')
        # The barrier leaves every item list. A list that held nothing else is an ingredient that no item fills.
        self.ing = [tuple(i for i in x if i != PLACEHOLDER) for x in rec['ing']]
        unfillable = {n for n, x in enumerate(rec['ing']) if x and not self.ing[n]}
        self.recipes = rec['recipes']
        self.json = load('recipe_json.json')
        self.items = {e['id']: e for e in load('items.json')['items']}
        for e in self.items.values():
            if e.get('repair'):
                e['repair'] = [i for i in e['repair'] if i != PLACEHOLDER]
        self.fluids = {e['id']: e for e in load('fluids.json')['fluids']}
        # The loaded mod ids. An item namespace is not always one (irons_lib registers irons_patreon_lib items), and a
        # dump made before the dump script wrote mods.json falls back to the namespaces
        self.modids = set(load('mods.json')['mods']) if os.path.exists(path('mods.json')) else None
        tags = load('tags.json')
        self.item_tags = {k: tuple(i for i in v if i != PLACEHOLDER) for k, v in tags['items'].items()}
        self.fluid_tags = {k: tuple(v) for k, v in tags['fluids'].items()}
        self.tags_of = collections.defaultdict(set)
        for t, ids in self.item_tags.items():
            for i in ids:
                self.tags_of[i].add(t)
        for e in self.recipes:
            for k in ('count',):
                if k in e:
                    e[k] = int(e[k])
            if 'ing' in e:
                if any(int(i) in unfillable for i in e['ing']):
                    e['unfillable'] = True
                e['ing'] = [self.ing[int(i)] for i in e['ing']]
            if 'melt' in e:
                m = e['melt']
                m['input'] = self.ing[int(m['input'])]
                m['outputs'] = [[(f, int(a)) for f, a in alts] for alts in m['outputs']]
            if 'cast' in e:
                c = e['cast']
                if int(c['cast']) in unfillable:
                    e['unfillable'] = True
                c['cast'] = self.ing[int(c['cast'])]
                c['fluids'] = [(f, int(a)) for f, a in c['fluids']]
                c['count'] = int(c['count'])
        if not any(f.startswith(MOLTEN + ':') for f in self.fluids):
            sys.exit('The dump has no SkyTekx Molten fluids. Make it on a test server with the Molten jar in mods/.')


def apply_pack_tags(dump, pack):
    """Merge the item tags of the fixes pack into the dump, so a tag added after the dump counts too."""
    root = os.path.join(pack, 'data')
    for nsp in sorted(os.listdir(root)) if os.path.isdir(root) else []:
        tdir = os.path.join(root, nsp, 'tags', 'items')
        for dirpath, dirnames, filenames in os.walk(tdir):
            dirnames.sort()
            for fn in sorted(filenames):
                if not fn.endswith('.json'):
                    continue
                tag = nsp + ':' + os.path.relpath(os.path.join(dirpath, fn), tdir)[:-5].replace(os.sep, '/')
                with open(os.path.join(dirpath, fn), encoding='utf-8') as fh:
                    js = json.load(fh)
                ids = set() if js.get('replace') else set(dump.item_tags.get(tag, ()))
                for v in js.get('values', []):
                    ref = v.get('id') if isinstance(v, dict) else v
                    if not isinstance(ref, str):
                        continue
                    if ref.startswith('#'):
                        ids.update(dump.item_tags.get(ref[1:], ()))
                    elif ref in dump.items:
                        ids.add(ref)
                dump.item_tags[tag] = tuple(sorted(ids))
                for i in ids:
                    dump.tags_of[i].add(tag)


def still(fid):
    a, b = fid.split(':', 1)
    if b.startswith('flowing_'):
        return a + ':' + b[len('flowing_'):]
    if b.endswith('_flowing'):
        return a + ':' + b[:-len('_flowing')]
    return fid


# ---------------------------------------------------------------------------------------------------------------------
# Fluids


class Fluids:
    """Groups fluids that share a forge:molten_* tag or forge:ender, so that the Tinkers and the addon copies of a metal
    are one, and so are molten ender and the resonant ender of Thermal (250 mB per pearl in both)."""

    def __init__(self, dump, recipes):
        parent = {}

        def find(x):
            while parent.setdefault(x, x) != x:
                parent[x] = parent[parent[x]]
                x = parent[x]
            return x

        for tag, ids in dump.fluid_tags.items():
            if not (tag.startswith('forge:molten_') or tag == 'forge:ender'):
                continue
            st = sorted({still(i) for i in ids})
            for o in st[1:]:
                parent[find(o)] = find(st[0])
        self.find = lambda f: find(still(f))
        # Fluids that melting recipes give, with their temperatures and the JSON form Tinkers uses for them
        self.temps = collections.defaultdict(collections.Counter)
        self.forms = collections.defaultdict(collections.Counter)
        self.units = collections.defaultdict(collections.Counter)
        self.melted = set()
        for e in recipes:
            m = e.get('melt')
            if not m:
                continue
            for alts in m['outputs']:
                for f, a in alts:
                    self.melted.add(self.find(f))
            self.temps[self.find(m['output'][0])][m['temperature']] += 1
            js = dump.json.get(e['id'])
            if js and isinstance(js.get('result'), dict):
                res = js['result']
                cls = self.find(m['output'][0])
                if 'tag' in res:
                    self.forms[cls][('tag', res['tag'])] += 1
                elif 'fluid' in res:
                    self.forms[cls][('fluid', res['fluid'])] += 1
                if 'unit_size' in res:
                    self.units[cls][int(res['unit_size'])] += 1
        self.tracked = {c for c in self.melted if c not in {self.find(x) for x in EXCLUDED_FLUIDS}}
        self.excluded_seen = sorted(c for c in self.melted if c not in self.tracked)

    def cls(self, fid):
        c = self.find(fid)
        return c if c in self.tracked else None

    def json_form(self, cls):
        if self.forms[cls]:
            (kind, val), _ = sorted(self.forms[cls].items(), key=lambda kv: (-kv[1], kv[0]))[0]
            return {kind: val}
        return {'fluid': cls}

    def temperature(self, cls):
        if self.temps[cls]:
            return sorted(self.temps[cls].items(), key=lambda kv: (-kv[1], kv[0]))[0][0]
        return 800

    def unit(self, cls):
        if self.units[cls]:
            return sorted(self.units[cls].items(), key=lambda kv: (-kv[1], kv[0]))[0][0]
        return 25 if cls in GEM_FLUIDS else 10


def melt_time(temperature, amount):
    # IMeltingRecipe.calcTimeForAmount of Tinkers 3.12: round((T + 300) ^ 0.585 * sqrt(amount / 90))
    factor = math.sqrt(amount / 90.0)
    return int(math.floor(math.pow(temperature + 300, 0.5849999785423279) * factor + 0.5))


# ---------------------------------------------------------------------------------------------------------------------
# Vectors of fluid amounts


def vadd(a, b, k=1.0):
    out = dict(a)
    for f, x in b.items():
        out[f] = out.get(f, 0.0) + x * k
    return out


def vmin(a, b):
    # Per fluid minimum, a fluid missing in one vector counts as zero there
    return {f: min(x, b[f]) for f, x in a.items() if f in b and min(x, b[f]) > EPS}


def vsub(a, b, k=1.0):
    out = {}
    for f, x in a.items():
        y = x - b.get(f, 0.0) * k
        if y > EPS:
            out[f] = y
    return out


def vscale(a, k):
    return {f: x * k for f, x in a.items() if x * k > EPS}


def vle(a, b):
    return all(x <= b.get(f, 0.0) + EPS for f, x in a.items())


def vstr(v):
    return ', '.join(f'{f} {x:g}' for f, x in sorted(v.items(), key=lambda kv: (-kv[1], kv[0]))) or 'nothing'


# ---------------------------------------------------------------------------------------------------------------------
# Reading recipe JSON for the types whose parsed ingredients are hidden


class JsonReader:
    def __init__(self, dump):
        self.dump = dump
        self.unknown_keys = collections.Counter()
        # Set by read() when an item ingredient of the recipe matches no item, such as an empty tag, and to the fluid
        # products of the recipe
        self.unfillable = False
        self.fluid_outs = []
        # The grinding balls of the Ender IO SAG mill and the empty ball slot, as (output multiplier, chance
        # multiplier). In GrindingBallRecipe, grinding is getOutputMultiplier and chance is getBonusMultiplier.
        self.balls = [(1.0, 1.0)]
        for js in dump.json.values():
            if isinstance(js, dict) and js.get('type') == 'enderio:grinding_ball':
                g, c = js.get('grinding'), js.get('chance')
                if isinstance(g, (int, float)) and isinstance(c, (int, float)) and g > 0 and c > 0:
                    self.balls.append((float(g), float(c)))
        # The catalysts of the Thermal machines per machine type, as (primary mod, secondary mod, min chance) with
        # the defaults of MachineCatalystSerializer, and the empty catalyst slot first
        self.catalysts = collections.defaultdict(lambda: [(1.0, 1.0, 0.0)])
        for js in dump.json.values():
            t = js.get('type') if isinstance(js, dict) else None
            if not (isinstance(t, str) and t.endswith('_catalyst') and ns(t) in THERMAL_NAMESPACES):
                continue
            if not self.items_of(js.get('ingredient')):
                continue
            mods = []
            for k, default in (('primary_mod', 1.0), ('secondary_mod', 1.0), ('min_chance', 0.0)):
                v = js.get(k, default)
                mods.append(float(v) if isinstance(v, (int, float)) else default)
            self.catalysts[t[:-len('_catalyst')]].append(tuple(mods))

    def items_of(self, node):
        """Item ids an ingredient node matches, or None when it is no item ingredient. An empty tuple is an item
        ingredient that no item fills, such as an empty tag or an item that does not exist."""
        d = self.dump
        if isinstance(node, list):
            out = []
            seen = False
            for x in node:
                r = self.items_of(x)
                if r is not None:
                    seen = True
                    out.extend(r)
            return tuple(sorted(set(out))) if seen else None
        if not isinstance(node, dict):
            return None
        t = node.get('type')
        if t == 'forge:difference':
            base = self.items_of(node.get('base')) or ()
            sub = set(self.items_of(node.get('subtracted')) or ())
            return tuple(i for i in base if i not in sub)
        if t in ('forge:compound',):
            return self.items_of(node.get('children'))
        if t == 'forge:intersection':
            sets = [set(self.items_of(c) or ()) for c in node.get('children', [])]
            return tuple(sorted(set.intersection(*sets))) if sets else ()
        if isinstance(node.get('item'), str):
            iid = rl(node['item'])
            return (iid,) if iid in d.items and iid != PLACEHOLDER else ()
        if isinstance(node.get('item'), dict):
            return self.items_of(node['item'])
        if isinstance(node.get('tag'), str) and 'amount' not in node:
            # An item tag that is empty matches no item. A tag that is no item tag may be a block or fluid tag of an
            # in-world recipe, which the reader cannot place, so it is no item ingredient.
            return d.item_tags.get(rl(node['tag']))
        if isinstance(node.get('tag'), str) and rl(node['tag']) in d.item_tags and rl(node['tag']) not in d.fluid_tags:
            return d.item_tags[rl(node['tag'])]
        for k in ('ingredient', 'base_ingredient', 'value'):
            if k in node:
                return self.items_of(node[k])
        return None

    def count_of(self, node):
        if isinstance(node, dict):
            for k in ('count', 'amount'):
                v = node.get(k)
                if isinstance(v, (int, float)) and not ('fluid' in node or 'fluidTag' in node):
                    if k == 'amount' and isinstance(node.get('tag'), str) and node['tag'] in self.dump.fluid_tags:
                        continue
                    return max(1.0, float(v))
        return 1.0

    def fluid_of(self, node):
        """(fluid ids, amount) of a fluid ingredient node, or None."""
        if not isinstance(node, dict):
            return None
        d = self.dump
        amt = node.get('amount', node.get('count', 1000))
        if not isinstance(amt, (int, float)):
            amt = 1000
        if isinstance(node.get('fluid'), str):
            return (rl(node['fluid']),), float(amt)
        if isinstance(node.get('fluid'), dict):
            return self.fluid_of(node['fluid'])
        for k in ('fluidTag', 'fluid_tag'):
            if isinstance(node.get(k), str):
                return d.fluid_tags.get(rl(node[k]), ()), float(amt)
        if isinstance(node.get('tag'), str) and rl(node['tag']) in d.fluid_tags and rl(node['tag']) not in d.item_tags:
            return d.fluid_tags[rl(node['tag'])], float(amt)
        return None

    def outputs(self, node, out):
        """Collect (item ids, largest count, expected count) of product nodes."""
        if isinstance(node, list):
            for x in node:
                self.outputs(x, out)
            return
        if isinstance(node, str):
            if rl(node) in self.dump.items:
                out.append(((rl(node),), 1.0, 1.0))
            return
        if not isinstance(node, dict):
            return
        if 'output' in node and isinstance(node['output'], (dict, list)) and 'item' not in node:
            # IE secondaries and similar wrappers {chance, output}
            self.outputs(node['output'], out)
            return
        ids = self.items_of(node)
        if ids is None:
            return
        cnt = self.count_of(node)
        if not ('count' in node or 'amount' in node) and isinstance(node.get('item'), dict):
            # The count inside the item object, as {chance, item: {count, item}}
            cnt = self.count_of(node['item'])
        ch = node.get('chance')
        exp = cnt
        if isinstance(ch, (int, float)) and ch > 0:
            # Thermal and others give floor(chance) for sure and one more at the rest of the chance
            exp = cnt * ch
            cnt = cnt * max(1, math.ceil(ch))
        if ids:
            out.append((ids, cnt, exp))

    def thermal_chances(self, rtype, js):
        """The JSON of a Thermal machine recipe with every unlocked chance raised to what the best catalyst and the
        best augments give (see THERMAL_MACHINE_MODS). A locked chance stays, and an output that no setup gives goes."""
        js = dict(js)
        cats = self.catalysts[rtype]
        index = 0
        for key in THERMAL_OUT_KEYS:
            if key not in js:
                continue
            nodes = js[key] if isinstance(js[key], list) else [js[key]]
            out = []
            for x in nodes:
                if not isinstance(x, dict) or 'fluid' in x or not self.items_of(x):
                    # A fluid, or an empty stack, which parseOutputs leaves out of the item outputs
                    out.append(x)
                    continue
                i, index = index, index + 1
                ch = x.get('chance')
                if not isinstance(ch, (int, float)):
                    out.append(x)
                    continue
                y = {k: v for k, v in x.items() if k != 'locked'}
                if ch < 0 or (ch > 0 and x.get('locked') in (True, 'true')):
                    y['chance'] = abs(float(ch))
                else:
                    m = THERMAL_MACHINE_MODS[0 if i == 0 else 1]
                    y['chance'] = round(max(max(ch * (p if i == 0 else s) * m, mn) for p, s, mn in cats), 6)
                if y['chance'] > 0:
                    out.append(y)
            js[key] = out if isinstance(js[key], list) else (out[0] if out else [])
        return js

    def take(self, slots, node, count=None):
        """Add the item slot of an ingredient node, or mark the recipe unfillable when the node matches no item."""
        ids = self.items_of(node)
        if ids is None:
            return
        if not ids:
            self.unfillable = True
            return
        slots.append((ids, self.count_of(node) if count is None else count))

    def need(self, node):
        """An input that the machine keeps costs nothing, but the recipe still needs an item that fills it."""
        if self.items_of(node) == ():
            self.unfillable = True

    def returned(self, rtype, ids):
        """Whether a slot can take an item that the machine hands back after the craft, so the slot costs nothing."""
        kind, tag = RETURNED_ITEMS.get(rtype, (None, None))
        if not kind:
            return False
        tagged = set(self.dump.item_tags.get(tag, ()))
        return any(i in tagged or kind in self.dump.items.get(i, {}).get('kinds', ()) for i in ids)

    def read(self, rtype, js):
        """(products, item slots, fluid slots) of a recipe JSON. A slot is (item ids, count).

        It sets unfillable when an item ingredient matches no item, so that the recipe is left out, and fluid_outs to
        the fluid products as (fluid ids, amount).
        """
        self.unfillable = False
        self.fluid_outs = []
        outs, slots, fluids = [], [], []
        if rtype == 'minecraft:smithing_transform':
            # The template counts as nothing, as in the Tinkers melting of netherite gear and the lead's horse armor
            self.need(js.get('template'))
            for k in ('base', 'addition'):
                self.take(slots, js.get(k), 1.0)
            self.outputs(js.get('result'), outs)
            return outs, slots, fluids
        if rtype == 'create:sequenced_assembly':
            self.take(slots, js.get('ingredient'), 1.0)
            loops = js.get('loops', 1) or 1
            for step in js.get('sequence', []):
                ings = step.get('ingredients', [])
                keep = bool(step.get('keepHeldItem'))
                for x in ings[1:]:
                    f = self.fluid_of(x)
                    if f:
                        fluids.append((f[0], f[1] * loops))
                        continue
                    if keep:
                        self.need(x)
                        continue
                    self.take(slots, x, self.count_of(x) * loops)
            # One assembly gives one of the results with the count of its stack, picked with the chances as weights
            # (SequencedAssemblyRecipe.rollResult), so a weight is no chance and leaves the largest count alone
            results = js.get('results') or []
            if not isinstance(results, list):
                results = [results]
            weights = []
            for r in results:
                w = r.get('chance', 1) if isinstance(r, dict) else 1
                weights.append(float(w) if isinstance(w, (int, float)) else 1.0)
            total = sum(weights) or 1.0
            for r, w in zip(results, weights):
                part = []
                self.outputs({k: v for k, v in r.items() if k != 'chance'} if isinstance(r, dict) else r, part)
                outs.extend((ids, cnt, cnt * w / total) for ids, cnt, _ in part)
            return outs, slots, fluids
        if 'pattern' in js and isinstance(js.get('key'), dict):
            # Shaped JSON of other types (Railcraft rolling): count every key in the pattern
            counts = collections.Counter(ch for row in js['pattern'] for ch in row if ch != ' ')
            for ch, n in counts.items():
                self.take(slots, js['key'].get(ch), float(n))
            self.outputs(js.get('result'), outs)
            return outs, slots, fluids
        if isinstance(js.get('baseRecipe'), dict):
            # Immersive Engineering wraps a crafting recipe that keeps no container item
            inner = dict(js['baseRecipe'])
            return self.read(inner.get('type', ''), inner)
        if rtype == 'ae2:inscriber' and isinstance(js.get('ingredients'), dict):
            # The presses on top and bottom stay in the inscriber unless the mode is press
            parts = ['middle'] + (['top', 'bottom'] if js.get('mode') == 'press' else [])
            for k in ('middle', 'top', 'bottom'):
                if k in parts:
                    self.take(slots, js['ingredients'].get(k), 1.0)
                else:
                    self.need(js['ingredients'].get(k))
            self.outputs(js.get('result'), outs)
            return outs, slots, fluids
        if rtype == 'bloodmagic:arc':
            # The ARC of Blood Magic always takes inputsize of its input. With consumeingredient false it hands back
            # the crafting remainder of the input, which slot_value subtracts for every recipe. The tool stays.
            n = js.get('inputsize', 1)
            self.take(slots, js.get('input'), float(n) if isinstance(n, (int, float)) and n > 0 else 1.0)
            self.need(js.get('tool'))
            f = self.fluid_of(js.get('inputFluid'))
            if f:
                fluids.append(f)
            self.outputs(js.get('output'), outs)
            for x in js.get('addedoutput') or []:
                # Added products as {type, mainchance, chance}. The ARC adds each one at most once, with the chance
                # mainchance plus chance times the bonus of its tool, so the largest count is the count of the stack.
                if isinstance(x, dict) and isinstance(x.get('type'), dict):
                    p = sum(v for v in (x.get('mainchance'), x.get('chance')) if isinstance(v, (int, float)) and v > 0)
                    if p <= 0:
                        continue
                    node = dict(x['type'])
                    if p < 1:
                        node['chance'] = p
                    self.outputs(node, outs)
            return outs, slots, fluids
        if rtype == 'advanced_ae:reaction':
            # The reaction chamber of AdvancedAE: inputs as {amount, ingredient}, the fluid as a fluid stack and the
            # product as a generic stack of Applied Energistics 2, whose kind ae2:i is an item
            for x in js.get('input_items') or []:
                self.take(slots, x)
            stack = js['fluid'].get('fluidStack') if isinstance(js.get('fluid'), dict) else None
            if isinstance(stack, dict) and isinstance(stack.get('FluidName'), str):
                fluids.append(((stack['FluidName'],), float(stack.get('Amount', 1000))))
            out = js.get('output')
            if isinstance(out, dict) and out.get('#c') == 'ae2:i' and out.get('id') in self.dump.items:
                n = float(out.get('#', 1))
                outs.append(((out['id'],), n, n))
            return outs, slots, fluids
        if rtype == 'expatternprovider:circuit_cutter':
            # The circuit cutter of ExtendedAE: one item and one fluid, each as {amount, ingredient}
            self.take(slots, js.get('item_input'))
            node = js.get('fluid_input')
            if isinstance(node, dict):
                f = self.fluid_of(node.get('ingredient'))
                if f:
                    fluids.append((f[0], float(node.get('amount', f[1]))))
            self.outputs(js.get('output'), outs)
            return outs, slots, fluids
        if rtype == 'enderio:sag_milling':
            # The SAG mill of Ender IO (SagMillingRecipe.craft, read 2026-11-05). A grinding ball with the output
            # multiplier m rolls every output once per started unit of m, so a recipe that multiplies gives up to
            # ceil(m) stacks of each output, and the chance multiplier of the ball raises the chance of each roll. The
            # bonus none uses no ball, chance_only only its chance multiplier, and a recipe without bonus multiplies.
            bonus = str(js.get('bonus', 'multiply_output')).lower()
            multiply = bonus == 'multiply_output'
            chance = bonus in ('multiply_output', 'chance_only')
            self.take(slots, js.get('input'), 1.0)
            for node in js.get('outputs') or []:
                if not isinstance(node, dict):
                    continue
                stack = node['item'] if isinstance(node.get('item'), dict) else node
                ids = self.items_of(stack)
                ch = node.get('chance', 1.0)
                ch = float(ch) if isinstance(ch, (int, float)) else 1.0
                if not ids or ch <= 0:
                    continue
                n = self.count_of(stack)
                rolls = [(m if multiply else 1.0, min(1.0, ch * (c if chance else 1.0))) for m, c in self.balls]
                outs.append((ids, n * max(math.ceil(m - EPS) for m, _ in rolls), n * max(m * q for m, q in rolls)))
            return outs, slots, fluids
        if ns(rtype) in THERMAL_NAMESPACES:
            js = self.thermal_chances(rtype, js)
        if rtype == 'cyclic:crusher' and isinstance(js.get('bonus'), dict):
            # The Cyclic crusher adds its bonus once (TileCrusher, read 2026-11-05): always at percent 1 or from 100 on,
            # else when nextInt(100) is below percent, with 1 plus nextInt(count) items of a stack above one
            js = dict(js)
            bonus = js.pop('bonus')
            p = js.get('percent', 0)
            ids = self.items_of(bonus)
            if ids and isinstance(p, (int, float)) and p > 0:
                n = self.count_of(bonus)
                ch = 1.0 if p == 1 or p >= 100 else p / 100.0
                outs.append((ids, n, ch * (n + 1) / 2))
        if rtype == 'enderio:tank' and js.get('is_emptying') is True:
            # The tank of Ender IO empties the input into its tank, so the fluid is a product
            f = self.fluid_of(js.get('fluid'))
            if f:
                self.fluid_outs.append(f)
            js = {k: v for k, v in js.items() if k != 'fluid'}
        consume = js.get('consumeingredient', True) is not False
        keep_second = bool(js.get('keepHeldItem'))
        kept_keys = KEPT_KEYS.get(rtype, ())
        for key, val in js.items():
            lk = key.lower()
            if lk in OUT_KEYS:
                before = len(outs)
                self.outputs(val, outs)
                n = js.get('count')
                if isinstance(val, str) and isinstance(n, (int, float)) and n > 1:
                    # A plain result id with its count beside it (Refurbished Furniture slicing)
                    outs[before:] = [(ids, float(n), float(n)) for ids, _, _ in outs[before:]]
                for x in (val if isinstance(val, list) else [val]):
                    f = self.fluid_of(x) if isinstance(x, dict) else None
                    if f:
                        self.fluid_outs.append(f)
            elif lk in IN_KEYS:
                nodes = val if (isinstance(val, list) and lk in LIST_OF_SLOTS) else [val]
                for idx, x in enumerate(nodes):
                    if not consume or lk in kept_keys or (keep_second and idx >= 1):
                        # An input that stays in the machine (a mould, a lens, the items on the pedestals)
                        self.need(x)
                        continue
                    if isinstance(x, str):
                        if rl(x) in self.dump.items and rl(x) != PLACEHOLDER:
                            slots.append(((rl(x),), 1.0))
                        continue
                    f = self.fluid_of(x)
                    if f:
                        fluids.append(f)
                        continue
                    ids = self.items_of(x)
                    if ids is None:
                        continue
                    if not ids:
                        self.unfillable = True
                        continue
                    # Thermal dies and chiller casts, Tinkers casts: catalysts that stay in the machine
                    ids = tuple(i for i in ids if not (i.endswith('_die') or i.endswith('_cast')))
                    if ids and self.returned(rtype, ids):
                        # A slot that takes an item the machine gives back, such as a rune on the runic altar
                        continue
                    if ids:
                        slots.append((ids, self.count_of(x)))
            elif lk in FLUID_IN_KEYS:
                nodes = val if isinstance(val, list) else [val]
                for x in nodes:
                    f = self.fluid_of(x)
                    if f:
                        fluids.append(f)
            elif lk not in QUIET_KEYS and isinstance(val, (dict, list)):
                self.unknown_keys[lk] += 1
        return outs, slots, fluids


# ---------------------------------------------------------------------------------------------------------------------
# One world: the pack with or without SkyTekx Molten


class Prod:
    """One product of one recipe: count is the largest number a craft can give, ecount the expected one. fouts is the
    content of the fluid products of the recipe, which leaves with them."""
    __slots__ = ('rid', 'rtype', 'out', 'count', 'slots', 'fluids', 'others', 'zero', 'source', 'ecount', 'eothers',
                 'fouts')

    def __init__(self, rid, rtype, out, count, slots, fluids, others, source):
        self.rid, self.rtype, self.out, self.count = rid, rtype, out, count
        self.slots, self.fluids, self.others, self.source = slots, fluids, others, source
        self.ecount, self.eothers = count, others
        self.zero = False
        self.fouts = {}


def conditions_hold(dump, mods, conds):
    """Whether the load conditions of a recipe JSON hold for a set of loaded mods."""
    for c in conds or []:
        if not isinstance(c, dict):
            continue
        t = c.get('type')
        if t == 'forge:mod_loaded':
            ok = c.get('modid') in mods
        elif t == 'forge:not':
            ok = not conditions_hold(dump, mods, [c.get('value')])
        elif t == 'forge:false':
            ok = False
        elif t == 'forge:and':
            ok = conditions_hold(dump, mods, c.get('values'))
        elif t == 'forge:or':
            ok = any(conditions_hold(dump, mods, [x]) for x in c.get('values') or [])
        elif t == 'forge:item_exists':
            ok = c.get('item') in dump.items and ns(c.get('item', '')) in mods
        elif t in ('forge:tag_empty', 'mantle:tag_empty'):
            ok = not dump.item_tags.get(c.get('tag'))
        elif t == 'mantle:tag_filled':
            ok = bool(dump.item_tags.get(c.get('tag')))
        else:
            ok = True
        if not ok:
            return False
    return True


def molten_only(dump, mods, e):
    """Whether a recipe of the dump needs SkyTekx Molten, so that the world without Molten leaves it out."""
    if ns(e['id']) == MOLTEN:
        return True
    js = dump.json.get(e['id'])
    if js and js.get('conditions') and not conditions_hold(dump, mods - {MOLTEN}, js['conditions']) \
            and conditions_hold(dump, mods | {MOLTEN}, js['conditions']):
        return True
    m = e.get('melt')
    if m and any(ns(f) == MOLTEN for alts in m['outputs'] for f, _ in alts):
        return True
    c = e.get('cast')
    if c and any(ns(f) == MOLTEN for f, _ in c['fluids']):
        return True
    return False


class World:
    def __init__(self, dump, molten, pack=None):
        self.dump = dump
        self.molten = molten
        self.mods = {'minecraft', 'forge'} | (dump.modids or ({ns(i) for i in dump.items} | {ns(f) for f in dump.fluids}))
        if not molten:
            self.mods.discard(MOLTEN)
        self.recipes = [e for e in dump.recipes if not e['id'].startswith(OWN_PREFIX)
                        and (molten or not molten_only(dump, self.mods, e))]
        self.fl = Fluids(dump, self.recipes)
        self.reader = JsonReader(dump)
        self.melts = collections.defaultdict(list)  # Item to [(recipe id, content vector, max vector, kind)]
        self._optimistic = False
        self.prods = []
        self.unreadable = collections.Counter()      # Recipe type to the count of recipes whose products got cost 0
        self.impossible = collections.Counter()      # Recipe type to the count of recipes that no item can fill
        self.free = collections.Counter()            # Recipe type to the count of recipes that take only kept inputs
        self._collect()
        for mob in CAUGHT_MOBS:
            item = 'minecraft:%s_bucket' % mob
            if item in dump.items and 'minecraft:water_bucket' in dump.items:
                self.prods.append(Prod('skytekx3:catch/' + mob, 'skytekx3:catch', item, 1.0,
                                       [(('minecraft:water_bucket',), 1.0)], [], [], 'world'))
        self.prods.sort(key=lambda p: (p.rid, p.out))
        self.overrides = []
        if pack:
            self._apply_pack(pack)

    def fvec(self, fid, amount):
        c = self.fl.cls(fid)
        return {c: float(amount)} if c and amount > 0 else {}

    def _collect(self):
        d = self.dump
        for e in self.recipes:
            m = e.get('melt')
            if m:
                main = m['outputs'][0] if m['outputs'] else [tuple(m['output'])]
                base = self.fvec(*main[0])
                full = dict(base)
                for alts in m['outputs'][1:]:
                    full = vadd(full, self.fvec(*alts[0]))
                if m['ore']:
                    rate = ORE_RATE.get(m['ore'], 1.0)
                    content = base
                    mx = vadd(vscale(base, rate), full, 0.0)
                    for alts in m['outputs'][1:]:
                        mx = vadd(mx, self.fvec(*alts[0]))
                    kind = 'ore'
                else:
                    content = full
                    mx = full
                    kind = 'damageable' if m['damageable'] else 'plain'
                for item in m['input']:
                    self.melts[item].append((e['id'], content, mx, kind))
                continue
            c = e.get('cast')
            if c and e.get('unfillable'):
                # A cast that no item fills (an empty tag): nobody can cast this
                self.impossible[e['type']] += 1
                continue
            if c:
                alts = [self.fvec(f, a) for f, a in c['fluids']]
                slots = []
                if c['consumed'] and c['cast']:
                    slots.append((c['cast'], 1.0))
                self.prods.append(Prod(e['id'], e['type'], c['result'], float(c['count']), slots,
                                       [alts] if alts else [], [], 'casting'))
                continue
            t = e['type']
            if t in SKIP_TYPES:
                continue
            js = d.json.get(e['id'])
            if t in CRAFTING_TYPES or t in DAMAGE_TYPES or (e.get('ing') and not js):
                if not e.get('result'):
                    continue
                if e.get('unfillable'):
                    # A slot that no item fills, such as an empty tag: nobody can craft this
                    self.impossible[t] += 1
                    continue
                slots = []
                for ids in e.get('ing', []):
                    if not ids:
                        continue
                    if t in DAMAGE_TYPES and all(d.items.get(i, {}).get('damageable') for i in ids):
                        continue
                    slots.append((ids, 1.0))
                self.prods.append(Prod(e['id'], t, e['result'], float(e['count']), slots, [], [], 'parsed'))
                continue
            if js is None:
                if e.get('result'):
                    # A recipe made in code: its product gets no content from it
                    p = Prod(e['id'], t, e['result'], float(e['count']), [], [], [], 'code')
                    p.zero = True
                    self.prods.append(p)
                    self.unreadable[t] += 1
                continue
            outs, slots, fluids = self.reader.read(t, js)
            if self.reader.unfillable:
                self.impossible[t] += 1
                continue
            if not outs and e.get('result'):
                outs = [((e['result'],), float(e['count']), float(e['count']))]
            if not outs:
                continue
            fslots = [[self.fvec(f, a) for f in ids] for ids, a in fluids if ids]
            # The fluid products take their content with them, the most that any of their fluids holds
            fouts = {}
            for ids, a in self.reader.fluid_outs:
                fouts = vadd(fouts, max((self.fvec(f, a) for f in ids), key=lambda v: sum(v.values()), default={}))
            if not (slots or fslots) and e.get('ing') and not (set(js) - CODE_KEYS):
                # A recipe defined in code (Blue Skies tools, Fairy Lights): its JSON holds only the type, and the
                # parsed ingredients are the crafting grid
                if e.get('unfillable'):
                    self.impossible[t] += 1
                    continue
                slots = [(ids, 1.0) for ids in e['ing'] if ids]
            known_inputs = bool(slots or fslots)
            # A product listed as several items (an output tag) counts as each of them
            merged = collections.OrderedDict()
            expected = collections.OrderedDict()
            for ids, cnt, exp in outs:
                for i in ids:
                    merged[i] = merged.get(i, 0.0) + cnt
                    expected[i] = expected.get(i, 0.0) + exp
            for i, cnt in merged.items():
                others = [(o, c2) for o, c2 in merged.items() if o != i]
                p = Prod(e['id'], t, i, cnt, slots, fslots, others, 'json')
                p.fouts = fouts
                p.ecount = expected[i]
                p.eothers = [(o, expected[o]) for o, _ in others]
                if not known_inputs:
                    # Nothing to count: an unreadable recipe, or one whose inputs all stay in the machine (the laser
                    # drill, the XP solidifier), which makes its product from nothing that melts
                    p.zero = True
                    if t in KEPT_KEYS:
                        self.free[t] += 1
                    else:
                        self.unreadable[t] += 1
                self.prods.append(p)

    def melt_entry(self, js):
        """(input items, content, max output, kind) of a melting recipe JSON."""
        def vec(node):
            if not isinstance(node, dict):
                return {}
            if isinstance(node.get('fluid'), str):
                fid = node['fluid']
            elif isinstance(node.get('tag'), str) and self.dump.fluid_tags.get(node['tag']):
                fid = self.dump.fluid_tags[node['tag']][0]
            else:
                return {}
            return self.fvec(fid, float(node.get('amount', 0)))
        items = self.reader.items_of(js.get('ingredient')) or ()
        base = vec(js.get('result'))
        full = dict(base)
        for b in js.get('byproducts') or []:
            full = vadd(full, vec(b))
        if js.get('type') == 'tconstruct:ore_melting':
            rate = ORE_RATE.get(str(js.get('rate', 'metal')).upper(), 1.0)
            return items, base, vadd(vscale(base, rate), vsub(full, base)), 'ore'
        kind = 'damageable' if js.get('type') == 'tconstruct:damagable_melting' else 'plain'
        return items, full, full, kind

    def _apply_pack(self, pack):
        """Use the melting recipes of the fixes pack in place of the dump's recipes with the same id.

        A dump made before an override was added then gives the same result as one made after it.
        """
        root = os.path.join(pack, 'data')
        for nsp in sorted(os.listdir(root)) if os.path.isdir(root) else []:
            rdir = os.path.join(root, nsp, 'recipes')
            for dirpath, dirnames, filenames in os.walk(rdir):
                dirnames.sort()
                for fn in sorted(filenames):
                    if not fn.endswith('.json'):
                        continue
                    rel = os.path.relpath(os.path.join(dirpath, fn), rdir)[:-5].replace(os.sep, '/')
                    rid = nsp + ':' + rel
                    if rid.startswith(OWN_PREFIX):
                        continue
                    with open(os.path.join(dirpath, fn), encoding='utf-8') as fh:
                        js = json.load(fh)
                    if js.get('type') not in MELT_TYPES:
                        continue
                    for item in [i for i, ms in self.melts.items() if any(m[0] == rid for m in ms)]:
                        self.melts[item] = [m for m in self.melts[item] if m[0] != rid]
                        if not self.melts[item]:
                            del self.melts[item]
                    if not conditions_hold(self.dump, self.mods, js.get('conditions')):
                        continue
                    items, content, mx, kind = self.melt_entry(js)
                    for item in items:
                        self.melts[item].append((rid, content, mx, kind))
                    self.overrides.append(rid)

    # -----------------------------------------------------------------------------------------------------------------

    def known(self, item):
        """The content of an item that melts: the smallest result per fluid over its melting recipes.

        Two mods can melt the same item into different fluids (Twilight Construct melts ironwood into molten ironwood,
        Tinkers into iron and gold), and Tinkers uses only one of the recipes. Recipes that share no fluid form
        separate families, and the family with the largest amount counts, with the smallest result per fluid in it.
        """
        ms = self.melts.get(item)
        if not ms:
            return None
        families = []
        for rid, c, _, _ in sorted(ms, key=lambda m: m[0]):
            if not c:
                return {}
            for fam in families:
                if set(fam[1]) & set(c):
                    fam[1] = vmin(fam[1], c)
                    break
            else:
                families.append([rid, dict(c)])
        families.sort(key=lambda fam: (-sum(fam[1].values()), fam[0]))
        return families[0][1]

    def known_max(self, item):
        ms = self.melts.get(item)
        if not ms:
            return None
        v = {}
        for _, _, mx, _ in ms:
            for f, x in mx.items():
                v[f] = max(v.get(f, 0.0), x)
        return v

    def is_ore(self, item):
        ms = self.melts.get(item)
        return bool(ms) and all(k == 'ore' for _, _, _, k in ms)

    def solve(self):
        """Fixed point of the content of every item.

        An item that melts is fixed at its smallest melting result and an item that no recipe makes is worth nothing.
        The first round reaches every item that some chain of recipes makes from fixed items. Items that only a loop
        of recipes reaches are still open after it: the second round counts their open inputs as nothing, so a loop
        adds no content, and the content of a recipe never rises above what the first evaluation gave it.
        """
        d = self.dump
        fixed = {item: self.known(item) for item in self.melts}
        produced = collections.defaultdict(list)
        users = collections.defaultdict(list)
        for p in self.prods:
            produced[p.out].append(p)
            for ids, _ in p.slots:
                for i in ids:
                    users[i].append(p)
        rem = {i: e.get('remainder') for i, e in d.items.items() if e.get('remainder')}
        rem_users = collections.defaultdict(list)
        for i, r in rem.items():
            rem_users[r].extend(users.get(i, ()))
        val = {}
        for item in d.items:
            if item in fixed:
                val[item] = fixed[item]
            elif produced.get(item):
                val[item] = INF
            else:
                val[item] = {}
        self._val = val
        self._rem = rem
        best = {}
        index = {id(p): n for n, p in enumerate(self.prods)}
        for rnd in (0, 1):
            self._optimistic = rnd == 1
            queue = collections.deque(self.prods)
            queued = set(range(len(self.prods)))
            steps = 0
            limit = 80 * len(self.prods)
            while queue and steps < limit:
                p = queue.popleft()
                queued.discard(index[id(p)])
                steps += 1
                if p.out in fixed:
                    continue
                v = self.evaluate(p)
                if v is INF:
                    continue
                cur = val.get(p.out, {})
                new = v if cur is INF else vmin(cur, v)
                if cur is not INF and set(new) == set(cur) and all(
                        abs(new[f] - cur[f]) <= 1e-9 * max(1.0, cur[f]) for f in new):
                    continue
                val[p.out] = new
                best[p.out] = p.rid
                for q in list(users.get(p.out, [])) + list(rem_users.get(p.out, [])):
                    n = index[id(q)]
                    if n not in queued:
                        queue.append(q)
                        queued.add(n)
            if not any(v is INF for v in val.values()):
                break
        for i, v in val.items():
            if v is INF:
                val[i] = {}
        self._optimistic = False
        return val, best, produced

    def melt_value(self, item, v):
        """What a generated recipe of the item gives for the content v: whole mB, and for an item with durability whole
        units, since Tinkers rounds damagable_melting down to them even at full durability."""
        v = floor_vec(v)
        e = self.dump.items.get(item, {})
        if e.get('damageable') and e.get('max_damage', 0) > 0:
            v = {f: a - a % min(self.fl.unit(f), a) for f, a in v.items()}
        return v

    def input_value(self, i):
        """What one item is worth as an ingredient: its melting result when it melts, else what its own generated
        recipe gives. So no item melts into more than its ingredients do."""
        v = self._val.get(i, {})
        if v is INF or i in self.melts:
            return v
        return self.melt_value(i, v)

    def slot_value(self, i, whole=False):
        """What one item is worth in a slot: its value less the crafting remainder that comes back, unless the recipe
        uses up the container whole."""
        v = self.input_value(i)
        if v is INF:
            return {} if self._optimistic else INF
        r = None if whole else self._rem.get(i)
        if r:
            if r == i:
                return {}
            rv = self.input_value(r)
            if rv:
                v = vsub(v, rv)
        return v

    def evaluate(self, p, expected=False):
        """Content that one recipe gives one of its products, INF while an ingredient is still unreachable.

        The largest count of a product is used, so a lucky craft never melts into more than it took. With expected the
        audit uses the expected counts instead, which is what a machine gives over many crafts.
        """
        if p.zero:
            return {}
        total = {}
        whole = p.rtype in WHOLE_TYPES
        for ids, cnt in p.slots:
            sv = INF
            for i in ids:
                av = self.slot_value(i, whole)
                if av is INF:
                    continue
                sv = av if sv is INF else vmin(sv, av)
            if sv is INF:
                return INF
            total = vadd(total, sv, cnt)
        for alts in p.fluids:
            fv = alts[0]
            for a in alts[1:]:
                fv = vmin(fv, a)
            total = vadd(total, fv)
        return self._share(p, total, expected)

    def _share(self, p, total, expected):
        count = p.ecount if expected else p.count
        total = vsub(total, p.fouts)
        for o, cnt in (p.eothers if expected else p.others):
            if o in self.melts:
                total = vsub(total, self.known_max(o), cnt)
            elif p.out not in self.melts:
                # Two products that do not melt share the cost in a way nobody knows: neither gets anything
                return {}
        return vscale(total, 1.0 / count) if count > 0 else {}

    def variants(self, i, whole=False):
        """What one item is worth as an input: one vector per melting recipe, since Tinkers uses one of them, or the
        content that its recipes give it."""
        if i in self.melts:
            out = [m[1] for m in self.melts[i]]
        else:
            v = self._val.get(i, {})
            out = [{} if v is INF else v]
        r = None if whole else self._rem.get(i)
        if r and r != i:
            rv = self._val.get(r) or {}
            out = [vsub(v, rv) for v in out]
        return out

    def carrier_inputs(self, p):
        """Per fluid f, the inputs of the choice that takes the least f while every slot that can hold f does.

        Every slot uses its alternative (and melting recipe) with the smallest positive amount of f, and a slot that
        cannot hold f uses its cheapest alternative. The vector is per product, with expected counts, so a melting
        result above it in f multiplies f even when only real carriers of f go in.
        """
        if p.zero:
            return {}
        slots = []
        whole = p.rtype in WHOLE_TYPES
        for ids, cnt in p.slots:
            vs = []
            for i in ids:
                vs.extend(self.variants(i, whole))
            slots.append((cnt, vs))
        for alts in p.fluids:
            slots.append((1.0, list(alts)))
        cheap = []
        fluids = set()
        for cnt, vs in slots:
            if not vs:
                return {}
            m = vs[0]
            for v in vs[1:]:
                m = vmin(m, v)
            cheap.append(m)
            for v in vs:
                fluids.update(f for f, x in v.items() if x > EPS)
        out = {}
        for f in sorted(fluids):
            total = {}
            for (cnt, vs), m in zip(slots, cheap):
                cand = [v for v in vs if v.get(f, 0.0) > EPS]
                v = min(cand, key=lambda v: (v[f], sum(v.values()))) if cand else m
                total = vadd(total, v, cnt)
            out[f] = self._share(p, total, True)
        return out


# ---------------------------------------------------------------------------------------------------------------------
# Generation


def excluded_item(dump, item):
    if ns(item) == 'tconstruct':
        return 'Tinkers item'
    tags = dump.tags_of.get(item, ())
    if 'tconstruct:modifiable' in tags or 'tconstruct:parts' in tags:
        return 'Tinkers tool or part'
    if item == 'minecraft:air':
        return 'air'
    return None


def gear_units(dump, item):
    e = dump.items.get(item, {})
    if e.get('armor_type') in GEAR_UNITS:
        return GEAR_UNITS[e['armor_type']], e['armor_type'].lower()
    tags = dump.tags_of.get(item, ())
    for tag, kind in GEAR_TAGS:
        if tag in tags:
            return GEAR_UNITS[kind], kind
    if 'sword' in e.get('kinds', []):
        return GEAR_UNITS['sword'], 'sword'
    return None, None


def floor_vec(v):
    return {f: int(math.floor(x + 1e-6)) for f, x in v.items() if math.floor(x + 1e-6) >= 1}


def compute(world):
    """Content of every item that does not melt yet, with the reason it gets it."""
    dump = world.dump
    val, best, produced = world.solve()
    out = {}
    loot = {}
    world.small = 0
    for item, e in sorted(dump.items.items()):
        if item in world.melts or excluded_item(dump, item):
            continue
        v = world.melt_value(item, val.get(item) or {})
        if v and max(v.values()) < MIN_RESULT:
            world.small += 1
            continue
        if produced.get(item):
            if v:
                out[item] = (v, best.get(item))
            continue
        # Gear that no recipe and no casting makes: its repair material at the Tinkers unit count
        if not e.get('damageable') or not e.get('repair'):
            continue
        units, kind = gear_units(dump, item)
        if not units:
            continue
        rv = None
        for r in e['repair']:
            x = world.input_value(r) or {}
            rv = x if rv is None else vmin(rv, x)
        rv = world.melt_value(item, vscale(rv or {}, units))
        if rv and max(rv.values()) >= MIN_RESULT:
            out[item] = (rv, 'repair material, %d units (%s)' % (units, kind))
            loot[item] = kind
    return out, loot, val


def fluid_mods(world, vec):
    mods = set()
    for f in vec:
        form = world.fl.json_form(f)
        fid = form.get('fluid') or f
        if ns(fid) not in ('minecraft', 'tconstruct', 'forge') and (ns(fid) in world.mods or ns(fid) == MOLTEN):
            mods.add(ns(fid))
    return mods


def recipe_json(world, item, vec, molten):
    """The melting recipe of one item. molten is True or False for a file of one case, None for both."""
    dump, fl = world.dump, world.fl
    e = dump.items[item]
    damageable = bool(e.get('damageable')) and e.get('max_damage', 0) > 0
    order = sorted(vec.items(), key=lambda kv: (-kv[1], kv[0]))
    main, amount = order[0]
    conds = []
    mods = []
    if ns(item) in world.mods or ns(item) == MOLTEN:
        if ns(item) != 'minecraft':
            mods.append(ns(item))
    else:
        # A namespace that is no mod id: the item itself is the condition
        conds.append({'type': 'forge:item_exists', 'item': item})
    mods += sorted(m for m in fluid_mods(world, vec) if m not in mods and m != MOLTEN)
    conds += [{'type': 'forge:mod_loaded', 'modid': m} for m in mods]
    if molten or MOLTEN in fluid_mods(world, vec):
        conds.append({'type': 'forge:mod_loaded', 'modid': MOLTEN})
    elif molten is False:
        conds.append({'type': 'forge:not', 'value': {'type': 'forge:mod_loaded', 'modid': MOLTEN}})
    r = collections.OrderedDict()
    if conds:
        r['conditions'] = conds
    r['type'] = 'tconstruct:damagable_melting' if damageable else 'tconstruct:melting'
    r['ingredient'] = {'item': item}

    def fluid(f, a):
        out = dict(fl.json_form(f))
        out['amount'] = a
        if damageable:
            # DamageableMeltingRecipe.scaleOutput rounds even an undamaged item down to a multiple of the unit size
            u = min(fl.unit(f), a)
            out['unit_size'] = u if a % u == 0 else math.gcd(u, a)
        return out
    r['result'] = fluid(main, amount)
    if len(order) > 1:
        r['byproducts'] = [fluid(f, a) for f, a in order[1:]]
    t = fl.temperature(main)
    r['temperature'] = t
    r['time'] = max(1, melt_time(t, amount))
    return r


# ---------------------------------------------------------------------------------------------------------------------
# Dupe audit


def audit(world):
    """Every melting recipe against every recipe that makes its item.

    A finding is a melting recipe that gives more of a fluid than one recipe of its item takes. It is a dupe when
    that recipe takes some of the fluid, the melting gives more of it back and no other fluid is lost on the way
    (melting multiplies it). It is a trade when the recipe pays for the gain with another fluid that the melting does
    not give back, such as tinted glass in a seared tank, which turns amethyst into glass. It is a conversion when the
    recipe takes only other fluids that the melting does not give back in full, and a source when it takes nothing
    that melts, such as an essence or a residue, or when the melting refunds every fluid it takes (refund).
    Ore processing by machines is left out, it is what the ore rates are for.
    """
    val, best, produced = world.solve()

    def grid(p):
        return any(k in p.rtype for k in ('crafting', 'shaped', 'shapeless'))
    # Ores, and items that do not melt and that only machines make from ores (dirty scrap, slurry crystals)
    ore_like = {i for i in world.melts if world.is_ore(i)}
    changed = True
    while changed:
        changed = False
        for item, ps in sorted(produced.items()):
            if item in ore_like or item in world.melts or not ps:
                continue
            if all(not grid(p) and any(i in ore_like for ids, _ in p.slots for i in ids) for p in ps):
                ore_like.add(item)
                changed = True
    findings = []
    for item in sorted(world.melts):
        for p in produced.get(item, []):
            if p.zero:
                continue
            if not grid(p) and any(i in ore_like for ids, _ in p.slots for i in ids):
                # Ore processing by a machine: ore doubling is what the ore rates and the machines are for
                continue
            carriers = world.carrier_inputs(p)
            for rid, content, mx, kind in world.melts[item]:
                # An ore compares its base amount, since its inputs count with their base amounts too
                out = content if kind == 'ore' else mx
                cls, takes, cap, refund = None, {}, {}, {}
                for f, x in sorted(out.items()):
                    inp = carriers.get(f)
                    if inp is None or x <= inp.get(f, 0.0) + 0.5:
                        continue
                    if any(y > out.get(g, 0.0) + 0.5 for g, y in inp.items()):
                        c = 'trade'
                    else:
                        c = 'dupe'
                        cap[f] = int(math.floor(inp[f] + 1e-6))
                    if cls != 'dupe':
                        cls, takes = c, inp
                if cls is None:
                    # No input holds the fluid: made from other fluids or from nothing that melts
                    cheap = world.evaluate(p, expected=True)
                    if cheap is INF or not any(x > cheap.get(f, 0.0) + 0.5 for f, x in out.items()):
                        continue
                    if cheap and all(out.get(g, 0.0) + 0.5 >= y for g, y in cheap.items()):
                        # The melting gives back every fluid the recipe takes, so nothing pays for the gain: a source
                        # with a refund, not a conversion
                        refund = cheap
                    cls, takes = ('conversion' if cheap and not refund else 'source'), cheap
                findings.append({
                    'melting': rid, 'item': item, 'kind': kind, 'recipe': p.rid, 'recipe_type': p.rtype,
                    'class': cls, 'melts': out, 'takes': takes,
                    'known': (KNOWN_DUPES.get(p.rid) or KNOWN_DUPES.get(p.rtype) or KNOWN_DUPES.get(item))
                    if cls == 'dupe' else None,
                    'cap': cap, 'refund': refund})
    return findings


# Keys under which an output node of a recipe JSON states a count
COUNT_KEYS = ('count', 'Count', 'amount', '#', 'quantity', 'size', 'stackSize')


def declared_counts(js):
    """Item id to the largest count that the output nodes of a recipe JSON state: at the node, inside its item object,
    or beside a plain result id at the top."""
    found = {}

    def note(iid, nums):
        if isinstance(iid, str):
            found[rl(iid)] = max([found.get(rl(iid), 1.0)] + [float(x) for x in nums])

    def walk(node):
        if isinstance(node, list):
            for x in node:
                walk(x)
            return
        if not isinstance(node, dict) or 'fluid' in node or 'fluidTag' in node:
            return
        if 'output' in node and isinstance(node['output'], (dict, list)) and 'item' not in node:
            walk(node['output'])
            return
        inner = node.get('item') if isinstance(node.get('item'), dict) else {}
        nums = [node[k] for k in COUNT_KEYS if isinstance(node.get(k), (int, float))]
        nums += [inner[k] for k in COUNT_KEYS if isinstance(inner.get(k), (int, float))]
        note(node.get('item') if isinstance(node.get('item'), str) else inner.get('item') or node.get('id'), nums)

    for key, val in js.items() if isinstance(js, dict) else ():
        if key.lower() not in OUT_KEYS:
            continue
        if isinstance(val, str):
            n = js.get('count')
            note(val, [n] if isinstance(n, (int, float)) else [])
        else:
            walk(val)
    return found


def reader_check(world):
    """Products of JSON recipes whose count the reader puts below the result count of the parsed recipe or below a
    count in the JSON. The audits use the counts of the reader, so a count that is too low there would be a dupe that
    no audit sees. A count above them is fine: a chance above one, a grinding ball."""
    byid = {e['id']: e for e in world.recipes}
    low = []
    for p in world.prods:
        if p.source != 'json':
            continue
        e = byid.get(p.rid) or {}
        if e.get('result') == p.out and e.get('count', 1) > p.count + EPS:
            low.append({'recipe': p.rid, 'item': p.out, 'reader': p.count, 'count': e['count'], 'from': 'parsed'})
        n = declared_counts(world.dump.json.get(p.rid)).get(p.out, 1.0)
        if n > p.count + EPS:
            low.append({'recipe': p.rid, 'item': p.out, 'reader': p.count, 'count': n, 'from': 'json'})
    return low


def audit_generated(world, out):
    """The audit again with the generated recipes in place: dupes among them would be faults of the generator."""
    saved = world.melts
    world.melts = collections.defaultdict(list, {k: list(v) for k, v in saved.items()})
    for item, (vec, _) in out.items():
        world.melts[item].append((OWN_PREFIX + item.replace(':', '/', 1), dict(vec), dict(vec), 'generated'))
    try:
        return [f for f in audit(world) if f['melting'].startswith(OWN_PREFIX)]
    finally:
        world.melts = saved


# ---------------------------------------------------------------------------------------------------------------------
# Output


def plan(dump, pack):
    """Both worlds, their results and the files to write, keyed by path below the output folder."""
    worlds = {}
    for molten in (False, True):
        w = World(dump, molten, pack)
        out, loot, _ = compute(w)
        worlds[molten] = (w, out, loot)
    (w0, out0, _), (w1, out1, _) = worlds[False], worlds[True]
    files = {}
    for item in sorted(set(out0) | set(out1)):
        a, b = out0.get(item), out1.get(item)
        base = item.replace(':', '/', 1)
        if a and b and a[0] == b[0]:
            files[base + '.json'] = recipe_json(w1, item, a[0], None)
            continue
        if a:
            files[base + '.json'] = recipe_json(w0, item, a[0], False)
        if b:
            files[base + '_molten.json'] = recipe_json(w1, item, b[0], True)
    return worlds, files


def render(obj):
    return json.dumps(obj, indent=2, ensure_ascii=False) + '\n'


def read_folder(out):
    have = {}
    for dirpath, dirnames, filenames in os.walk(out):
        for fn in filenames:
            full = os.path.join(dirpath, fn)
            with open(full, encoding='utf-8') as fh:
                have[os.path.relpath(full, out).replace(os.sep, '/')] = fh.read()
    return have


def write_folder(out, files):
    if os.path.isdir(out):
        shutil.rmtree(out)
    for rel in sorted(files):
        full = os.path.join(out, *rel.split('/'))
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, 'w', encoding='utf-8', newline='\n') as fh:
            fh.write(render(files[rel]))


def summary(worlds, files, findings):
    lines = []
    for molten in (False, True):
        w, out, loot = worlds[molten]
        name = 'with SkyTekx Molten' if molten else 'without SkyTekx Molten'
        per_mod = collections.Counter(ns(i) for i in out)
        lines.append('World %s: %d recipes read, %d items melt already, %d items get a recipe (%d of them gear '
                     'that melts by its repair material), %d items stay out below %d mB, %d recipes are unreadable.'
                     % (name, len(w.recipes), len(w.melts), len(out), len(loot), w.small, MIN_RESULT,
                        sum(w.unreadable.values())))
        lines.append('  Per mod: ' + ', '.join('%s %d' % kv for kv in sorted(per_mod.items(),
                                                                              key=lambda kv: (-kv[1], kv[0]))))
        lines.append('  Unreadable recipe types (their products get nothing from them): ' + ', '.join(
            '%s %d' % kv for kv in w.unreadable.most_common(12)))
        lines.append('  Recipes left out because an ingredient matches no item, such as an empty tag: %d (%s)' % (
            sum(w.impossible.values()), ', '.join('%s %d' % kv for kv in w.impossible.most_common(8))))
        lines.append('  Products of recipes that take only inputs the machine keeps, which get nothing from them: %d (%s)'
                     % (sum(w.free.values()), ', '.join('%s %d' % kv for kv in w.free.most_common(8))))
        lines.append('  Fluids left out: ' + ', '.join(w.fl.excluded_seen))
        unknown = w.reader.unknown_keys
        lines.append('  JSON keys the reader does not know, which it counts as nothing: %d (%s)' % (
            sum(unknown.values()), ', '.join('%s %d' % kv for kv in unknown.most_common(12))))
        lines.append('  Overrides of the fixes pack in use: %d' % len(w.overrides))
    both = sum(1 for k in files if not k.endswith('_molten.json')
               and 'forge:not' not in json.dumps(files[k].get('conditions', [])))
    lines.append('Files: %d (%d for both cases, %d only without Molten, %d only with Molten).' % (
        len(files), both,
        sum(1 for k in files if 'forge:not' in json.dumps(files[k].get('conditions', []))),
        sum(1 for k in files if k.endswith('_molten.json'))))
    for molten in (False, True):
        fs = findings[molten]
        dupes = [f for f in fs if f['class'] == 'dupe']
        new = sorted({f['melting'] for f in dupes if not f['known']})
        lines.append('Audit %s: %d findings (%d dupes in %d melting recipes, %d of them known machine dupes), %d '
                     'trades, %d conversions, %d sources.' % (
                         'with Molten' if molten else 'without Molten', len(fs), len(dupes),
                         len({f['melting'] for f in dupes}), len({f['melting'] for f in dupes if f['known']}),
                         sum(1 for f in fs if f['class'] == 'trade'),
                         sum(1 for f in fs if f['class'] == 'conversion'),
                         sum(1 for f in fs if f['class'] == 'source')))
        for rid in new:
            f = [x for x in dupes if x['melting'] == rid and not x['known']][0]
            lines.append('  DUPE %s: %s melts into %s, %s (%s) takes %s' % (
                rid, f['item'], vstr(f['melts']), f['recipe'], f['recipe_type'], vstr(f['takes'])))
    return '\n'.join(lines)


def main(argv=None):
    ap = argparse.ArgumentParser(description='Generate Tinkers melting recipes from a melting dump.')
    ap.add_argument('dump', help='kubejs/ of the test server after the dump, or a folder with a copy of the dump files')
    ap.add_argument('--out', default=OUT_DEFAULT, help='output folder (deleted and rewritten)')
    ap.add_argument('--pack', default=PACK_DEFAULT, help='fixes pack whose melting overrides replace the dump\'s')
    ap.add_argument('--check', action='store_true', help='change nothing, exit 1 when the output folder differs')
    ap.add_argument('--report', help='also write the full report as JSON to this file')
    args = ap.parse_args(argv)
    dump = Dump(args.dump)
    apply_pack_tags(dump, args.pack)
    worlds, files = plan(dump, args.pack)
    findings = {m: audit(worlds[m][0]) for m in (False, True)}
    print(summary(worlds, files, findings))
    low = {m: reader_check(worlds[m][0]) for m in (False, True)}
    for m in (False, True):
        print('Reader check %s: %d products below the count of their recipe.' % (
            'with Molten' if m else 'without Molten', len(low[m])))
        for f in low[m][:10]:
            print('  LOW %s: %s counts %g, the %s recipe gives %g' % (f['recipe'], f['item'], f['reader'], f['from'],
                                                                    f['count']))
    own = {m: audit_generated(worlds[m][0], worlds[m][1]) for m in (False, True)}
    own_dupes = {m: [f for f in own[m] if f['class'] == 'dupe' and not f['known']] for m in own}
    for m in (False, True):
        print('Audit of the generated recipes %s: %d dupes, %d trades.' % (
            'with Molten' if m else 'without Molten', len(own_dupes[m]),
            sum(1 for f in own[m] if f['class'] == 'trade')))
        for f in own_dupes[m][:10]:
            print('  DUPE %s: melts into %s, %s takes %s' % (f['melting'], vstr(f['melts']), f['recipe'],
                                                            vstr(f['takes'])))
    if args.report:
        rep = {'files': len(files), 'worlds': {}}
        for molten in (False, True):
            w, out, loot = worlds[molten]
            rep['worlds']['molten' if molten else 'base'] = {
                'generated': {i: {'content': v, 'from': why} for i, (v, why) in sorted(out.items())},
                'loot_gear': dict(sorted(loot.items())),
                'unreadable': dict(w.unreadable.most_common()),
                'impossible': dict(w.impossible.most_common()),
                'free': dict(w.free.most_common()),
                'fluids_left_out': w.fl.excluded_seen,
                'overrides': w.overrides,
                'findings': findings[molten],
                'generated_findings': own[molten],
                'reader_low': low[molten],
            }
        with open(args.report, 'w', encoding='utf-8') as fh:
            json.dump(rep, fh, indent=1, sort_keys=True)
    new_dupes = any(f['class'] == 'dupe' and not f['known'] for m in findings for f in findings[m]) \
        or any(own_dupes.values()) or any(low.values())
    if args.check:
        have = read_folder(args.out) if os.path.isdir(args.out) else {}
        want = {k: render(v) for k, v in files.items()}
        diff = sorted(k for k in set(have) | set(want) if have.get(k) != want.get(k))
        print('Check: %d of %d files differ.' % (len(diff), len(want)))
        for k in diff[:20]:
            print('  ' + k)
        return 1 if diff or new_dupes else 0
    write_folder(args.out, files)
    print('Wrote %d files to %s.' % (len(files), args.out))
    return 1 if new_dupes else 0


if __name__ == '__main__':
    sys.exit(main())
