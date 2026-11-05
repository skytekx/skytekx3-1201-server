#!/usr/bin/env python3
"""Convert the 1.21.1 data of Expanded Delight 0.1.4 (upstream 1.21-neo, ea82640) to Minecraft 1.20.1 Forge.

Run once, from ports/expandeddelight, on the vendored snapshot:

    python3 -I tools/convert_data_to_1_20_1.py

It rewrites src/main/resources/data and src/generated/resources/data in place and prints what it did. The
commit that ran it holds the result, so the script documents the conversion and is not run again. Running
it a second time finds no 1.21 folder names and changes nothing.

What it does, in this order:
- Renames the 1.21 folders to their 1.20.1 names: recipe -> recipes, loot_table -> loot_tables,
  advancement -> advancements, tags/item -> tags/items, tags/block -> tags/blocks,
  data/<ns>/neoforge/biome_modifier -> data/<ns>/forge/biome_modifier and
  data/neoforge/loot_modifiers -> data/forge/loot_modifiers.
- Moves the c: tags to forge: tags (TAG_MAP) and drops c: tags without a 1.20.1 convention.
- Rewrites item stacks ({"id", "count"} -> {"item", "count"}), cooking results, cutting board results and
  tools, item predicates of advancements and loot tables, loot conditions and functions, biome
  modifiers and loot modifiers to their 1.20.1 forms.
- Drops the files listed in DROP, each with its reason.
- Points the pie models at the 1.20.1 names of Farmer's Delight's pie templates (MODEL_PARENT_MAP).
"""
import json
import os
import sys

ROOTS = ["src/main/resources/data", "src/generated/resources/data"]

# c: tag of upstream -> forge: tag of this port. None drops the tag. The names follow Farmer's Delight
# 1.20.1 (forge:crops/*, forge:vegetables, forge:salad_ingredients, forge:milk, forge:pasta, forge:grain,
# forge:raw_bacon, forge:cooked_chicken, forge:cooked_fishes/salmon, forge:berries, forge:tools/knives)
# and Pam's HarvestCraft 2 Food Core (forge:cheese, forge:cookies, forge:nuts/*, forge:juices).
TAG_MAP = {
    "c:buckets/milk": "forge:buckets/milk",
    "c:chests/wooden": "forge:chests/wooden",
    "c:crops": "forge:crops",
    "c:crops/asparagus": "forge:crops/asparagus",
    "c:crops/beetroot": "forge:crops/beetroot",
    "c:crops/chili_pepper": "forge:crops/chili_pepper",
    "c:crops/grain": "forge:grain",
    "c:crops/onion": "forge:crops/onion",
    "c:crops/peanut": "forge:crops/peanut",
    "c:crops/rice": "forge:crops/rice",
    "c:crops/sweet_potato": "forge:crops/sweet_potato",
    "c:crops/tomato": "forge:crops/tomato",
    "c:crops/wheat": "forge:crops/wheat",
    "c:dusts": "forge:dusts",
    "c:dusts/cinnamon": "forge:dusts/cinnamon",
    "c:dusts/salt": "forge:dusts/salt",
    "c:eggs": "forge:eggs",
    "c:fence_gates/wooden": "forge:fence_gates/wooden",
    "c:foods": None,
    "c:foods/asparagus": "forge:vegetables/asparagus",
    "c:foods/berry": "forge:berries",
    "c:foods/cheese": "forge:cheese",
    "c:foods/chili_pepper": "forge:vegetables/chili_pepper",
    "c:foods/cooked_chicken": "forge:cooked_chicken",
    "c:foods/cooked_salmon": "forge:cooked_fishes/salmon",
    "c:foods/cookie": "forge:cookies",
    "c:foods/dough": "forge:dough",
    "c:foods/edible_when_placed": None,
    "c:foods/goat_cheese": "forge:goat_cheese",
    "c:foods/leafy_green": "forge:salad_ingredients",
    "c:foods/milk": "forge:milk",
    "c:foods/pasta": "forge:pasta",
    "c:foods/peanut": "forge:nuts/peanut",
    "c:foods/raw_bacon": "forge:raw_bacon",
    "c:foods/soup": None,
    "c:foods/sweet_potato": "forge:vegetables/sweet_potato",
    "c:foods/vegetable": "forge:vegetables",
    "c:ingots/iron": "forge:ingots/iron",
    "c:jams": "forge:jams",
    "c:jams/cranberry": "forge:jams/cranberry",
    "c:jams/glow_berry": "forge:jams/glow_berry",
    "c:jams/sweet_berry": "forge:jams/sweet_berry",
    "c:juices": "forge:juices",
    "c:juices/apple": "forge:juices/apple",
    "c:juices/cranberry": "forge:juices/cranberry",
    "c:juices/glow_berry": "forge:juices/glow_berry",
    "c:juices/sweet_berry": "forge:juices/sweet_berry",
    "c:ores": "forge:ores",
    "c:ores/salt": "forge:ores/salt",
    "c:rods/wooden": "forge:rods/wooden",
    "c:seeds": "forge:seeds",
    "c:stripped_logs": "forge:stripped_logs",
    "c:stripped_woods": "forge:stripped_wood",
}

# ingredient tags that become something else than a tag on 1.20.1
INGREDIENT_MAP = {
    # Forge 1.20.1 has no chains tag, the vanilla hanging sign recipes name the chain item
    "c:chains": {"item": "minecraft:chain"},
}

# cutting board tools in the form of Farmer's Delight 1.20.1: a tool action, then the tag
TOOL_MAP = {
    "c:tools/knife": [{"type": "farmersdelight:tool_action", "action": "knife_dig"}, {"tag": "forge:tools/knives"}],
    "minecraft:axes": [{"type": "farmersdelight:tool_action", "action": "axe_dig"}, {"tag": "minecraft:axes"}],
}

BIOME_MAP = {
    "#c:is_desert": "#forge:is_desert",
    "#c:is_jungle": "#minecraft:is_jungle",
    "#c:is_ocean": "#minecraft:is_ocean",
    "#c:is_plains": "#forge:is_plains",
    "#c:is_savanna": "#minecraft:is_savanna",
    "#c:is_swamp": "#forge:is_swamp",
}

BLOCK_RENAMES = {
    # renamed in Minecraft 1.20.3
    "minecraft:short_grass": "minecraft:grass",
}

# Farmer's Delight 1.21 renamed its pie templates, 1.20.1 has the old names
MODEL_PARENT_MAP = {
    "farmersdelight:block/pie": "farmersdelight:block/template_pie",
    "farmersdelight:block/pie_slice1": "farmersdelight:block/template_pie_slice1",
    "farmersdelight:block/pie_slice2": "farmersdelight:block/template_pie_slice2",
    "farmersdelight:block/pie_slice3": "farmersdelight:block/template_pie_slice3",
}

# paths relative to a data root, in their 1.21 form
DROP = {
    # Overrides of other mods' recipes. Upstream makes Farmer's Delight's wheat dough need salt and disables
    # Farmer's Delight's melon juice and a Farmer's Respite recipe. The SkyTekx pack edits no recipes of
    # other mods per mod (AGENTS.md), and on 1.20.1 Farmer's Delight's dough recipes have other ids
    # (wheat_dough_from_egg) and a special recipe type, so these would add recipes instead of replacing them.
    "farmersdelight/recipe/wheat_dough_from_water.json": "overrides a Farmer's Delight recipe",
    "farmersdelight/recipe/wheat_dough_from_eggs.json": "overrides a Farmer's Delight recipe",
    "farmersdelight/advancement/recipes/food/wheat_dough_from_water.json": "unlocks the dropped override",
    "farmersdelight/advancement/recipes/food/wheat_dough_from_eggs.json": "unlocks the dropped override",
    "farmersdelight/recipe/melon_juice.json": "disables a Farmer's Delight recipe",
    "farmersrespite/recipe/brewing/melon_juice_from_water.json": "disables a Farmer's Respite recipe",
    # vanilla item tags that only exist from Minecraft 1.20.5 on, the 1.20.1 animals read fixed lists
    "minecraft/tags/item/chicken_food.json": "no such tag before 1.20.5",
    "minecraft/tags/item/parrot_food.json": "no such tag before 1.20.5",
    "minecraft/tags/item/pig_food.json": "no such tag before 1.20.5",
    "minecraft/tags/item/fox_food.json": "no such tag before 1.20.5",
}

FOLDER_RENAMES = [
    ("/recipe/", "/recipes/"),
    ("/loot_table/", "/loot_tables/"),
    ("/advancement/", "/advancements/"),
    ("/tags/item/", "/tags/items/"),
    ("/tags/block/", "/tags/blocks/"),
    ("/neoforge/biome_modifier/", "/forge/biome_modifier/"),
]

log = []


def map_tag(tag):
    if tag in TAG_MAP:
        return TAG_MAP[tag]
    if tag.startswith("c:"):
        raise SystemExit(f"unmapped c: tag {tag}")
    return tag


def conv_stack(obj):
    """1.21 item stack {"id", "count", "components"?} -> 1.20.1 {"item", "count"}."""
    if not isinstance(obj, dict) or "id" not in obj:
        return obj
    if "components" in obj:
        raise SystemExit(f"stack with components {obj}")
    out = {"item": obj["id"]}
    if obj.get("count", 1) != 1:
        out["count"] = obj["count"]
    return out


def conv_ingredient(ing):
    if isinstance(ing, list):
        return [conv_ingredient(i) for i in ing]
    if isinstance(ing, dict) and "tag" in ing:
        tag = ing["tag"]
        if tag in INGREDIENT_MAP:
            return dict(INGREDIENT_MAP[tag])
        mapped = map_tag(tag)
        if mapped is None:
            raise SystemExit(f"ingredient with dropped tag {tag}")
        return {**ing, "tag": mapped}
    return ing


def conv_recipe(d):
    t = d.get("type")
    for key in ("ingredients",):
        if key in d:
            d[key] = [conv_ingredient(i) for i in d[key]]
    if "ingredient" in d:
        d["ingredient"] = conv_ingredient(d["ingredient"])
    if "key" in d:
        d["key"] = {k: conv_ingredient(v) for k, v in d["key"].items()}
    if t in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"):
        res = conv_stack(d["result"])
        d["result"] = res["item"] if isinstance(res, dict) and set(res) == {"item"} else res
    elif t == "farmersdelight:cutting":
        results = []
        for r in d["result"]:
            if isinstance(r["item"], str):
                results.append(r)
                continue
            stack = conv_stack(r["item"])
            entry = dict(stack)
            if "chance" in r:
                entry["chance"] = r["chance"]
            results.append(entry)
        d["result"] = results
        tool = d["tool"]
        if isinstance(tool, dict) and "tag" in tool and tool["tag"] in TOOL_MAP:
            d["tool"] = TOOL_MAP[tool["tag"]]
        else:
            d["tool"] = conv_ingredient(tool)
    else:
        if "result" in d:
            d["result"] = conv_stack(d["result"])
        if "container" in d:
            d["container"] = conv_stack(d["container"])
    return d


def conv_item_predicate(pred):
    """1.21 item predicate -> 1.20.1 item predicate."""
    out = {}
    for k, v in pred.items():
        if k == "items":
            if isinstance(v, str) and v.startswith("#"):
                mapped = map_tag(v[1:])
                out["tag"] = mapped
            elif isinstance(v, str):
                out["items"] = [v]
            else:
                out["items"] = v
        elif k == "predicates":
            for pk, pv in v.items():
                if pk == "minecraft:enchantments":
                    out["enchantments"] = [
                        {"enchantment": e["enchantments"], **({"levels": e["levels"]} if "levels" in e else {})}
                        for e in pv
                    ]
                else:
                    raise SystemExit(f"unknown item sub predicate {pk}")
        else:
            out[k] = v
    return out


def conv_loot(o):
    if isinstance(o, list):
        return [x for x in (conv_loot(i) for i in o) if x is not None]
    if not isinstance(o, dict):
        return o
    o = {k: conv_loot(v) for k, v in o.items()}
    if o.get("condition") == "minecraft:match_tool":
        o["predicate"] = conv_item_predicate(o["predicate"])
    if o.get("condition") == "neoforge:can_item_perform_ability":
        o = {"condition": "forge:can_tool_perform_action", "action": o["ability"]}
    if "functions" in o:
        funcs = []
        for f in o["functions"]:
            if f.get("function") == "minecraft:copy_components":
                inc = f.get("include", [])
                if "minecraft:custom_name" in inc:
                    funcs.append({"function": "minecraft:copy_name", "source": "block_entity"})
                if "expandeddelight:drink" in inc:
                    # the drink and its container travel as BlockEntityTag, see CopyDrinkFunction
                    funcs.append({"function": "expandeddelight:copy_drink"})
                rest = [c for c in inc if c not in ("minecraft:custom_name", "expandeddelight:drink", "farmersdelight:container")]
                if rest:
                    raise SystemExit(f"copy_components with {rest}")
            else:
                funcs.append(f)
        o["functions"] = funcs
    return o


def conv_advancement(d):
    for crit in d.get("criteria", {}).values():
        cond = crit.get("conditions")
        if not cond:
            continue
        if crit["trigger"] == "minecraft:inventory_changed" and "items" in cond:
            cond["items"] = [conv_item_predicate(p) for p in cond["items"]]
    disp = d.get("display")
    if disp and "icon" in disp:
        disp["icon"] = conv_stack(disp["icon"])
    return d


def conv_tag(d):
    vals = []
    for v in d["values"]:
        if isinstance(v, dict):
            ref = v["id"]
            if ref.startswith("#"):
                mapped = map_tag(ref[1:])
                if mapped is None:
                    continue
                v = {**v, "id": "#" + mapped}
            vals.append(v)
        elif v.startswith("#"):
            mapped = map_tag(v[1:])
            if mapped is not None:
                vals.append("#" + mapped)
        else:
            vals.append(v)
    d["values"] = vals
    return d


def conv_worldgen(o):
    if isinstance(o, list):
        return [conv_worldgen(i) for i in o]
    if isinstance(o, dict):
        o = {k: conv_worldgen(v) for k, v in o.items()}
        if "Name" in o and o["Name"] in BLOCK_RENAMES:
            o["Name"] = BLOCK_RENAMES[o["Name"]]
        return o
    return o


def conv_biome_modifier(d):
    if d["type"] == "forge:add_features":
        return d
    if d["type"] != "neoforge:add_features":
        raise SystemExit(f"biome modifier type {d['type']}")
    d["type"] = "forge:add_features"
    d["biomes"] = BIOME_MAP[d["biomes"]]
    return d


def conv_loot_modifier(d):
    for c in d.get("conditions", []):
        if c.get("condition") == "neoforge:loot_table_id":
            c["condition"] = "forge:loot_table_id"
    return d


def new_rel(rel):
    """Data root relative path, 1.21 form -> 1.20.1 form."""
    r = "/" + rel
    for a, b in FOLDER_RENAMES:
        r = r.replace(a, b)
    r = r[1:]
    if r.startswith("neoforge/loot_modifiers/"):
        r = "forge/" + r[len("neoforge/"):]
    if r.startswith("c/tags/"):
        kind, path = r[len("c/tags/"):].split("/", 1)
        tag = "c:" + path[:-len(".json")]
        mapped = map_tag(tag)
        if mapped is None:
            return None
        ns, p = mapped.split(":", 1)
        r = f"{ns}/tags/{kind}/{p}.json"
    return r


def dump(path, d):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(json.dumps(d, indent=2, ensure_ascii=False))


def convert_models():
    for root in ("src/main/resources/assets/expandeddelight/models", "src/generated/resources/assets/expandeddelight/models"):
        for dp, dn, fn in os.walk(root):
            for f in sorted(fn):
                path = os.path.join(dp, f)
                with open(path, encoding="utf-8") as fh:
                    raw = fh.read()
                d = json.loads(raw)
                if d.get("parent") in MODEL_PARENT_MAP:
                    old = d["parent"]
                    # keep the file's own formatting, only the parent changes
                    raw = raw.replace(json.dumps(old), json.dumps(MODEL_PARENT_MAP[old]), 1)
                    with open(path, "w", encoding="utf-8") as fh:
                        fh.write(raw)
                    log.append(f"model parent {path}: {old} -> {MODEL_PARENT_MAP[old]}")


def main():
    convert_models()
    for root in ROOTS:
        files = []
        for dp, dn, fn in os.walk(root):
            for f in fn:
                files.append(os.path.relpath(os.path.join(dp, f), root))
        for rel in sorted(files):
            src = os.path.join(root, rel)
            if rel in DROP:
                os.remove(src)
                log.append(f"drop {root}/{rel}: {DROP[rel]}")
                continue
            if not rel.endswith(".json"):
                continue
            nrel = new_rel(rel)
            if nrel is None:
                os.remove(src)
                log.append(f"drop {root}/{rel}: c: tag without a 1.20.1 convention")
                continue
            with open(src, encoding="utf-8") as f:
                raw = f.read()
            d = json.loads(raw)
            original = json.loads(raw)
            parts = nrel.split("/")
            kind = parts[1] if len(parts) > 1 else ""
            if kind == "recipes":
                d = conv_recipe(d)
            elif kind == "loot_tables":
                d = conv_loot(d)
            elif kind == "advancements":
                d = conv_advancement(d)
            elif kind == "tags":
                d = conv_tag(d)
            elif kind == "worldgen":
                d = conv_worldgen(d)
            elif kind == "forge" and parts[2] == "biome_modifier":
                d = conv_biome_modifier(d)
            elif kind == "loot_modifiers":
                d = conv_loot_modifier(d)
                if parts[0] == "forge":
                    d["entries"] = [e for e in d["entries"] if e != "expandeddelight:add_loot_sniffer_digging"]
                    log.append("global_loot_modifiers: drop expandeddelight:add_loot_sniffer_digging, upstream has no such file")
            dst = os.path.join(root, nrel)
            if dst != src and os.path.exists(dst):
                raise SystemExit(f"{dst} exists")
            if d == original:
                # unchanged content keeps its bytes, a moved file is only renamed
                if dst != src:
                    os.makedirs(os.path.dirname(dst), exist_ok=True)
                    os.rename(src, dst)
                    log.append(f"move {root}/{rel} -> {nrel}")
                continue
            os.remove(src)
            dump(dst, d)
            log.append(f"{'convert' if dst == src else 'move and convert'} {root}/{rel}" + (f" -> {nrel}" if dst != src else ""))
        # remove empty folders left by the renames
        for dp, dn, fn in os.walk(root, topdown=False):
            if not os.listdir(dp):
                os.rmdir(dp)
    print("\n".join(log))


if __name__ == "__main__":
    if not os.path.isdir("src/generated/resources/data"):
        sys.exit("run from ports/expandeddelight")
    main()
