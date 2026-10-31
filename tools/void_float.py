#!/usr/bin/env python3
"""Which blocks below the island band float free, and which hang from an island?

void_float.py <region_dir> [--band-min 24] [--attach-y 40]
Example: tools/void_float.py world/dimensions/skytekx3/skylands/region

Finds every non-air block below band-min, then flood fills (6 neighbours) through non-air blocks.
A component that reaches attach-y is part of an island body (an island bottom, a geode or root
hanging out of it). A component that never reaches it floats in the void and is a leak.
Chunks it cannot read count as not generated, they are listed at the end.
Reuses the NBT and region reader of void_scan.py (same directory). Needs numpy.
"""
import collections, os, sys
import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from void_scan import AIR, chunks, is_full, load_chunk, print_bad, section_blocks, unreadable

region_dir = sys.argv[1]
band_min = int(sys.argv[sys.argv.index("--band-min") + 1]) if "--band-min" in sys.argv else 24
attach_y = int(sys.argv[sys.argv.index("--attach-y") + 1]) if "--attach-y" in sys.argv else 40
TOP = 256                                   # the Skylands height (min_y 0), so any --attach-y works
CAP = 200000                                # blocks per fill, so that one fill cannot run away

grid_cache = {}
def grid(cx, cz):
    """names array [TOP][16][16] (z, x) of block names, None for a missing, unfinished or unreadable chunk."""
    k = (cx, cz)
    if k in grid_cache:
        return grid_cache[k]
    c = load_chunk(region_dir, cx, cz)
    g = None
    if c is not None and is_full(c):
        g = np.full((TOP, 16, 16), "minecraft:air", dtype=object)
        for s in c.get("sections", []):
            sy = s["Y"]
            if sy < 0 or sy * 16 >= TOP:
                continue
            pal, idx = section_blocks(s)
            if pal is None:
                continue
            g[sy * 16:sy * 16 + 16] = np.array(pal, dtype=object)[idx].reshape(16, 16, 16)
    grid_cache[k] = g
    return g

def block(x, y, z):
    if y < 0 or y >= TOP:
        return "minecraft:air"
    g = grid(x >> 4, z >> 4)
    if g is None:
        return None                         # unknown, outside the generated area
    return g[y, z & 15, x & 15]

# pass 1: every non-air block below the band
low = []
for fn in sorted(os.listdir(region_dir)):
    if not fn.endswith(".mca"):
        continue
    for c in chunks(os.path.join(region_dir, fn)):
        if not is_full(c):
            continue
        cx, cz = c["xPos"], c["zPos"]
        for s in c.get("sections", []):
            sy = s["Y"]
            if sy < 0 or sy * 16 >= band_min:
                continue
            pal, idx = section_blocks(s)
            if pal is None or all(p in AIR for p in pal):
                continue
            arr = np.array(pal, dtype=object)[idx].reshape(16, 16, 16)
            ys, zs, xs = np.nonzero(~np.isin(arr, list(AIR)))
            for y, z, x in zip(ys, zs, xs):
                wy = sy * 16 + int(y)
                if wy < band_min:
                    low.append((cx * 16 + int(x), wy, cz * 16 + int(z)))
print(f"blocks below Y {band_min}: {len(low)}")

# pass 2: flood fill from each low block. A fill stops as soon as it reaches attach_y and leaves blocks
# it claimed but never expanded, so a later fill from the same body can run into them. owner keeps the
# fill of every claimed block, fills that meet are joined (union find), and a group counts as attached
# when any of its fills reached attach_y.
owner, parent, fills = {}, [], []           # fills[i] = (size, lo, hi, names, edge, reached, capped)

def root(i):
    while parent[i] != i:
        parent[i] = parent[parent[i]]
        i = parent[i]
    return i

def edge_kind(x, z):
    return "unreadable" if unreadable(x >> 4, z >> 4) else "ungenerated"

for start in low:
    if start in owner:
        continue
    me = len(parent)
    parent.append(me)
    owner[start] = me
    comp, q, names, edge, met = [], collections.deque([start]), collections.Counter(), set(), set()
    reached = capped = False
    while q:
        x, y, z = q.popleft()
        b = block(x, y, z)
        if b is None:                       # its chunk changed after pass 1 and cannot be read now
            edge.add(edge_kind(x, z))
            continue
        comp.append((x, y, z))
        names[b] += 1
        if y >= attach_y:
            reached = True
            break
        if len(comp) > CAP:
            capped = True
            break
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            n = (x + dx, y + dy, z + dz)
            o = owner.get(n)
            if o is not None:
                if o != me:
                    met.add(o)
                continue
            nb = block(*n)
            if nb is None:
                edge.add(edge_kind(n[0], n[2]))
                continue
            if nb in AIR:
                continue
            owner[n] = me
            q.append(n)
    pts = comp or [start]
    fills.append((len(comp), tuple(min(p[k] for p in pts) for k in range(3)),
                  tuple(max(p[k] for p in pts) for k in range(3)), names, edge, reached, capped))
    for o in met:
        ra, rb = root(me), root(o)
        if ra != rb:
            parent[max(ra, rb)] = min(ra, rb)

groups = {}
for i, f in enumerate(fills):
    groups.setdefault(root(i), []).append(f)
attached, floating = [], []
for fs in groups.values():
    names = collections.Counter()
    for f in fs:
        names.update(f[3])
    info = (sum(f[0] for f in fs), tuple(min(f[1][k] for f in fs) for k in range(3)),
            tuple(max(f[2][k] for f in fs) for k in range(3)), names.most_common(4),
            set().union(*(f[4] for f in fs)), any(f[6] for f in fs))
    (attached if any(f[5] for f in fs) else floating).append(info)

print(f"components reaching Y {attach_y} (hang from an island): {len(attached)}")
for n, lo, hi, nm, edge, capped in attached[:30]:
    print(f"  size>={n:6d} from {lo} to {hi} {nm}")
print(f"FLOATING components (never reach Y {attach_y}): {len(floating)}")
for n, lo, hi, nm, edge, capped in sorted(floating, key=lambda f: -f[0]):
    note = (" (touches an unreadable chunk)" if "unreadable" in edge else "") + \
           (" (touches ungenerated area)" if "ungenerated" in edge else "") + \
           (f" (fill stopped at {CAP} blocks)" if capped else "")
    print(f"  size={n:6d} from {lo} to {hi} {nm}{note}")
print_bad()
