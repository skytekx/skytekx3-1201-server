"""Check that one item per material works everywhere, and write the EMI hide list.

unify_audit.py <dump.json> [--server DIR] [--hidden FILE] [--report FILE]

<dump.json> comes from tools/unify_audit_dump.js on a test server. A variant is an item that Almost
Unified swaps for another one, its preferred item. unify_inventory.js swaps variants in every player
inventory, so nobody holds one, and every check below asks whether the preferred item still does all
that a variant did:

1. Ingredients that take a variant but not its preferred item.
2. Recipes whose result is a variant.
3. Crafting grids that two recipes share with vanilla matching (offsets, mirror, shapeless). Polymorph
   offers both results, which is a bug when they are the same item in two counts ("count") or two
   variants of one material ("variants"). "different" pairs give two unrelated items.
4. Item tags that a variant has and its preferred item lacks. With --server DIR, the recipe and tag
   files in the mods, world/datapacks and kubejs/data of that server that use such a tag, and the
   recipes without readable ingredients whose files name a variant, and the armor trim materials
   whose item is a variant, which no player can then use. The files are read as the mods
   ship them, and Almost Unified often turns such an item into its tag while it loads, so each hit
   needs a look at the live recipe on the server.

--hidden FILE writes every variant into the tag c:hidden_from_recipe_viewers, which EMI leaves out of
its item list. Each entry is optional, so the file also loads with fewer mods.
"""
import argparse
import collections
import json
import os
import re
import sys
import zipfile

NO_MIRROR = {"cucumber:shaped_no_mirror", "cucumber:shaped_tag"}
# The hide list holds every variant and no preferred item on purpose.
HIDDEN = "c:hidden_from_recipe_viewers"

ap = argparse.ArgumentParser()
ap.add_argument("dump")
ap.add_argument("--server")
ap.add_argument("--hidden")
ap.add_argument("--report")
args = ap.parse_args()

dump = json.load(open(args.dump))
table = [frozenset(t) for t in dump["table"]]
recipes = dump["recipes"]
# KubeJS writes every number as a double.
for r in recipes:
    r["ings"] = [int(x) for x in r.get("ings") or []]
    for k in ("w", "h"):
        if k in r:
            r[k] = int(r[k])
    if r.get("out"):
        r["out"]["count"] = int(r["out"]["count"])

variants = dump["variants"]
swapped = {x: v["to"] for x, v in variants.items() if v["to"] and v["to"] != x}
tag_of = collections.defaultdict(set)
for u in dump["unify"]:
    for it in u["items"]:
        tag_of[it].add(u["tag"])

# 1. ingredients without the preferred item, 2. results that are a variant
missing, results = [], []
no_ings = collections.Counter()
for r in recipes:
    if not r["ings"]:
        no_ings[r.get("type", "")] += 1
    for k, ref in enumerate(r["ings"]):
        s = table[ref]
        lost = sorted({swapped[i] for i in s if i in swapped and swapped[i] not in s})
        if lost:
            missing.append({"id": r["id"], "type": r.get("type"), "slot": k, "lacks": lost, "items": sorted(s)[:6]})
    out = r.get("out")
    if out and out["item"] in swapped:
        results.append({"id": r["id"], "type": r.get("type"), "gives": out["item"], "preferred": swapped[out["item"]]})

# 3. grid conflicts among crafting recipes
crafting = [r for r in recipes if r.get("type") == "minecraft:crafting" and r["ings"]]


def shape_of(r):
    """('shaped', cells, offset or None, mirror) with trimmed cells, or ('shapeless', sets)."""
    if r.get("w") and r["w"] * r["h"] == len(r["ings"]):
        w, h = r["w"], r["h"]
        grid = [[table[r["ings"][y * w + x]] for x in range(w)] for y in range(h)]
        rows = [y for y in range(h) if any(grid[y][x] for x in range(w))]
        cols = [x for x in range(w) if any(grid[y][x] for y in range(h))]
        if not rows:
            return None
        cells = tuple(tuple(grid[y][x] for x in range(cols[0], cols[-1] + 1)) for y in range(rows[0], rows[-1] + 1))
        # Vanilla tries every offset that the stored width and height leave free, so a pattern that
        # kept its empty rows (Cucumber) only fits where it stands.
        fixed = (w, h) != (len(cells[0]), len(cells))
        return ("shaped", cells, (cols[0], rows[0]) if fixed else None, r.get("ser") not in NO_MIRROR)
    sets = [table[i] for i in r["ings"]]
    if not all(sets) or len(sets) > 9:
        return None
    return ("shapeless", sets)


def mirrored(cells):
    return tuple(tuple(reversed(row)) for row in cells)


def cells_meet(a, b):
    if len(a) != len(b) or len(a[0]) != len(b[0]):
        return None
    pick = []
    for ra, rb in zip(a, b):
        for ca, cb in zip(ra, rb):
            if bool(ca) != bool(cb):
                return None
            if ca:
                both = ca & cb
                if not both:
                    return None
                pick.append(min(both))
            else:
                pick.append(None)
    return pick


def match_sets(xs, ys):
    """Perfect matching between two lists of item sets, one item per pair, or None."""
    n = len(xs)
    if n != len(ys):
        return None
    adj = [[j for j in range(n) if xs[i] & ys[j]] for i in range(n)]
    owner = [-1] * n

    def try_(i, seen):
        for j in adj[i]:
            if j in seen:
                continue
            seen.add(j)
            if owner[j] < 0 or try_(owner[j], seen):
                owner[j] = i
                return True
        return False

    for i in range(n):
        if not try_(i, set()):
            return None
    return [min(xs[owner[j]] & ys[j]) for j in range(n)]


def conflict(sa, sb):
    """A grid that both shapes accept, as {slot: item} on a 3x3 grid, or None."""
    if sa[0] == "shaped" and sb[0] == "shaped":
        a_cells, a_off, a_mir = sa[1], sa[2], sa[3]
        b_cells, b_off, b_mir = sb[1], sb[2], sb[3]
        if a_off and b_off and a_off != b_off:
            return None
        # A grid fits a shaped recipe as it stands or, if the recipe allows it, mirrored.
        pick, cells = cells_meet(a_cells, b_cells), a_cells
        if pick is None and b_mir:
            pick = cells_meet(a_cells, mirrored(b_cells))
        if pick is None and a_mir:
            pick, cells = cells_meet(mirrored(a_cells), b_cells), b_cells
        if pick is None:
            return None
        ox, oy = a_off or b_off or (0, 0)
        w = len(cells[0])
        return {(oy + k // w) * 3 + ox + k % w: it for k, it in enumerate(pick) if it}
    if sa[0] == "shapeless" and sb[0] == "shapeless":
        pick = match_sets(sa[1], sb[1])
        return None if pick is None else dict(enumerate(pick))
    shaped, shapeless = (sa, sb) if sa[0] == "shaped" else (sb, sa)
    flat = [(k, c) for k, c in enumerate(c for row in shaped[1] for c in row) if c]
    pick = match_sets([c for _, c in flat], shapeless[1])
    if pick is None:
        return None
    ox, oy = shaped[2] or (0, 0)
    w = len(shaped[1][0])
    return {(oy + k // w) * 3 + ox + k % w: it for (k, _), it in zip(flat, pick)}


shapes, index = {}, collections.defaultdict(set)
for n, r in enumerate(crafting):
    s = shape_of(r)
    if s is None:
        continue
    sets = [c for row in s[1] for c in row if c] if s[0] == "shaped" else s[1]
    shapes[n] = (s, sets)
    for c in sets:
        for it in c:
            index[it].add(n)

# Two recipes that share a grid meet in every ingredient, so the recipes that hold an item of the
# rarest ingredient of one are the only candidates for the other.
pairs = []
for n, (s, sets) in shapes.items():
    best = min(sets, key=lambda c: sum(len(index.get(i, ())) for i in c))
    cand = set()
    for it in best:
        cand |= index.get(it, set())
    for m in cand:
        if m <= n or len(shapes[m][1]) != len(sets):
            continue
        g = conflict(s, shapes[m][0])
        if g is None:
            continue
        a, b = crafting[n], crafting[m]
        oa, ob = a.get("out") or {}, b.get("out") or {}
        if not oa or not ob:
            continue
        same = oa["item"] == ob["item"] and oa.get("nbt") == ob.get("nbt")
        if same and oa["count"] == ob["count"]:
            kind = "identical"
        elif same:
            kind = "count"
        elif tag_of.get(oa["item"], set()) & tag_of.get(ob["item"], set()):
            kind = "variants"
        else:
            kind = "different"
        pairs.append({"kind": kind, "a": a["id"], "b": b["id"], "out_a": oa, "out_b": ob,
                      "grid": {str(k): v for k, v in sorted(g.items())}})
kinds = collections.Counter(p["kind"] for p in pairs)

# 4. tags that a variant has and its preferred item lacks
members = collections.defaultdict(set)
for x, v in variants.items():
    for t in v["tags"]:
        members[t].add(x)
lacking = {}
for t, ms in members.items():
    if t == HIDDEN:
        continue
    lost = sorted({swapped[x] for x in ms if x in swapped and swapped[x] not in ms})
    if lost:
        lacking[t] = lost

uses = collections.defaultdict(set)
named = []
trims, dead_trims = {}, []
if args.server:
    no_ing_ids = {r["id"] for r in recipes if not r["ings"]}
    OUT_KEYS = {"result", "results", "output", "outputs", "secondary", "secondaryOutput", "secondary_output",
                "byproducts", "byproduct", "mainOutput", "secondaryOutputs", "itemOutput", "item_output", "output_item"}

    def walk(node, path, ref, hits):
        if isinstance(node, dict):
            for k, v in node.items():
                if k == "tag" and isinstance(v, str) and v in lacking:
                    uses[v].add(ref)
                if k in ("item", "id") and isinstance(v, str) and v in swapped:
                    hits.append(path + [k])
                walk(v, path + [k], ref, hits)
        elif isinstance(node, list):
            for i, v in enumerate(node):
                walk(v, path + [str(i)], ref, hits)
        elif isinstance(node, str):
            if node.startswith("#") and node[1:] in lacking:
                uses[node[1:]].add(ref)
            elif node in swapped and path and path[-1] not in ("item", "id"):
                hits.append(path)

    def scan(name, read, src):
        if not (name.startswith("data/") and name.endswith(".json")):
            return
        if not any(d in name for d in ("/recipes/", "/tags/", "/loot_tables/", "/trim_material/")):
            return
        try:
            j = json.loads(read())
        except Exception:
            return
        parts = name.split("/", 2)
        ref = parts[1] + ":" + parts[2][:-5]
        if "/trim_material/" in name:
            # A trim takes exactly one item. Datapacks load after the mods, so their file wins.
            trims.setdefault(ref, {})[src] = j.get("ingredient")
            return
        hits = []
        walk(j, [], ref, hits)
        if "/recipes/" in name:
            rid = parts[1] + ":" + name.split("/recipes/", 1)[1][:-5]
            ins = [h for h in hits if not set(h) & OUT_KEYS]
            if rid in no_ing_ids and ins:
                named.append({"id": rid, "paths": ["/".join(h) for h in ins][:4]})

    mods = os.path.join(args.server, "mods")
    for fn in sorted(os.listdir(mods)):
        if fn.endswith(".jar"):
            try:
                z = zipfile.ZipFile(os.path.join(mods, fn))
            except Exception:
                continue
            for n in z.namelist():
                scan(n, lambda n=n, z=z: z.read(n), "mod")
    for root in (os.path.join(args.server, "world", "datapacks"), os.path.join(args.server, "kubejs", "data")):
        for dp, _, fs in os.walk(root):
            for f in fs:
                full = os.path.join(dp, f)
                rel = full.split("/data/", 1)
                if len(rel) == 2:
                    scan("data/" + rel[1], lambda full=full: open(full, "rb").read(), "datapack")
    for ref, by in sorted(trims.items()):
        ing = by.get("datapack", by.get("mod"))
        if ing in swapped:
            dead_trims.append({"trim": ref, "takes": ing, "preferred": swapped[ing]})

if args.hidden:
    vals = [{"id": x, "required": False} for x in sorted(swapped)]
    with open(args.hidden, "w") as fh:
        fh.write(json.dumps({"replace": False, "values": vals}, indent=2) + "\n")

report = {
    "variants": len(swapped),
    "ingredients_without_preferred": missing,
    "results_that_are_variants": results,
    "conflict_kinds": kinds,
    "conflicts": [p for p in pairs if p["kind"] != "identical"],
    "tags_lacking_preferred": lacking,
    "tag_uses": {t: sorted(v) for t, v in uses.items()},
    "no_ingredients_naming_variants": named,
    "trims_taking_variants": dead_trims,
    "no_ingredients_by_type": no_ings.most_common(),
}
if args.report:
    json.dump(report, open(args.report, "w"), indent=1)

print("recipes %d, crafting %d, variants %d" % (len(recipes), len(crafting), len(swapped)))
print("1. ingredients without the preferred item: %d" % len(missing))
for m in missing[:20]:
    print("   %s slot %d lacks %s" % (m["id"], m["slot"], ", ".join(m["lacks"])))
print("2. results that are a variant: %d" % len(results))
for m in results[:20]:
    print("   %s gives %s" % (m["id"], m["gives"]))
print("3. shared crafting grids: %s" % dict(kinds))
for p in pairs:
    if p["kind"] in ("count", "variants"):
        print("   %s: %s (%dx %s) and %s (%dx %s)" % (p["kind"], p["a"], p["out_a"]["count"], p["out_a"]["item"],
                                               p["b"], p["out_b"]["count"], p["out_b"]["item"]))
print("4. tags a variant has and its preferred item lacks: %d" % len(lacking))
for t in sorted(lacking):
    line = "   %s (%s)" % (t, ", ".join(lacking[t][:3]))
    if args.server:
        line += ", used by %d files" % len(uses.get(t, ()))
    print(line)
if args.server:
    print("   recipes without readable ingredients that name a variant: %d" % len(named))
    for m in named[:20]:
        print("   %s at %s" % (m["id"], ", ".join(m["paths"])))
    print("   trim materials that take a variant: %d" % len(dead_trims))
    for m in dead_trims:
        print("   %s takes %s, not %s" % (m["trim"], m["takes"], m["preferred"]))
if args.hidden:
    print("wrote %d variants to %s" % (len(swapped), args.hidden))
sys.exit(0)
