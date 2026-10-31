#!/usr/bin/env python3
"""Which blocks below the island band float free, and which hang from an island?

void_float.py <region_dir> [--band-min 24] [--attach-y 40]
Example: tools/void_float.py world/dimensions/skytekx3/skylands/region

Finds every non-air block below band-min, then flood fills (6 neighbours) through non-air blocks.
A component that reaches attach-y is part of an island body (an island bottom, a geode or root
hanging out of it). A component that never reaches it floats in the void and is a leak.
Reuses the NBT and region reader of void_scan.py (same directory). Needs numpy.
"""
import collections, gzip, os, sys, zlib
import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from void_scan import AIR, chunks, read_nbt, section_blocks

region_dir = sys.argv[1]
band_min = int(sys.argv[sys.argv.index("--band-min") + 1]) if "--band-min" in sys.argv else 24
attach_y = int(sys.argv[sys.argv.index("--attach-y") + 1]) if "--attach-y" in sys.argv else 40
TOP = 256                                   # the Skylands height (min_y 0), so any --attach-y works

region_cache = {}
def region_bytes(rx, rz):
    k = (rx, rz)
    if k not in region_cache:
        p = os.path.join(region_dir, f"r.{rx}.{rz}.mca")
        region_cache[k] = open(p, "rb").read() if os.path.exists(p) else b""
    return region_cache[k]

def load_chunk(cx, cz):
    data = region_bytes(cx >> 5, cz >> 5)
    if len(data) < 8192:
        return None
    i = (cx & 31) + (cz & 31) * 32
    off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
    if off == 0:
        return None
    p = off * 4096
    ln = int.from_bytes(data[p:p + 4], "big")
    comp = data[p + 4]
    raw = data[p + 5:p + 4 + ln]
    if comp == 2:
        raw = zlib.decompress(raw)
    elif comp == 1:
        raw = gzip.decompress(raw)
    return read_nbt(raw)

grid_cache = {}
def grid(cx, cz):
    """names array [TOP][16][16] (z, x) of block names, None for a missing chunk."""
    k = (cx, cz)
    if k in grid_cache:
        return grid_cache[k]
    c = load_chunk(cx, cz)
    g = None
    if c is not None and c.get("Status", "") in ("minecraft:full", "full"):
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
        if c.get("Status", "") not in ("minecraft:full", "full"):
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

# pass 2: flood fill from each low block
seen = set()
attached, floating = [], []
for start in low:
    if start in seen:
        continue
    comp, q, top, names, unknown = [], collections.deque([start]), start[1], collections.Counter(), False
    seen.add(start)
    while q:
        x, y, z = q.popleft()
        b = block(x, y, z)
        if b is None:
            unknown = True
            continue
        comp.append((x, y, z))
        names[b] += 1
        top = max(top, y)
        if top >= attach_y or len(comp) > 200000:
            break
        for dx, dy, dz in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            n = (x + dx, y + dy, z + dz)
            if n in seen:
                continue
            nb = block(*n)
            if nb is None:
                unknown = True
                continue
            if nb in AIR:
                continue
            seen.add(n)
            q.append(n)
    xs = [p[0] for p in comp]; ys = [p[1] for p in comp]; zs = [p[2] for p in comp]
    info = (len(comp), (min(xs), min(ys), min(zs)), (max(xs), max(ys), max(zs)), names.most_common(4), unknown)
    (attached if top >= attach_y else floating).append(info)

print(f"components reaching Y {attach_y} (hang from an island): {len(attached)}")
for n, lo, hi, nm, unk in attached[:30]:
    print(f"  size>={n:6d} from {lo} to {hi} {nm}")
print(f"FLOATING components (never reach Y {attach_y}): {len(floating)}")
for n, lo, hi, nm, unk in sorted(floating, key=lambda f: -f[0]):
    print(f"  size={n:6d} from {lo} to {hi} {nm}{' (touches ungenerated area)' if unk else ''}")
