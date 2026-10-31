#!/usr/bin/env python3
"""Print every piece of every start of one structure in a region folder: template, projection and box.

structure_pieces.py <region_dir> <structure_id>
Example: tools/structure_pieces.py world/dimensions/skytekx3/skylands/region skytekx3:sky_windmill
Shows where jigsaw pieces really ended up, for example terrain_matching pieces that left a floating
structure. Lists the chunks it could not read at the end, a start in one of them is missing.
Reuses the NBT and region reader of void_scan.py (same directory). Needs numpy.
"""
import os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from void_scan import chunks, print_bad

if len(sys.argv) != 3:
    sys.exit(__doc__)
region_dir, want = sys.argv[1], sys.argv[2]
n = 0
for fn in sorted(os.listdir(region_dir)):
    if not fn.endswith(".mca"):
        continue
    for c in chunks(os.path.join(region_dir, fn)):
        for sid, st in c.get("structures", {}).get("starts", {}).items():
            if sid != want or st.get("id") == "INVALID":
                continue
            n += 1
            kids = st.get("Children", [])
            print(f"== start {n} chunk {c['xPos']},{c['zPos']} pieces {len(kids)}")
            for k in kids:
                el = k.get("pool_element", {})
                loc = str(el.get("location", el.get("element_type", "?"))).split("/")[-1]
                bb = [int(v) for v in k.get("BB", [])]
                print(f"   {loc:34s} projection={str(el.get('projection', '?')):16s} box={bb}")
print("starts:", n)
print_bad()
