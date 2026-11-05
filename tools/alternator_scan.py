#!/usr/bin/env python3
"""Read-only scan of the Create Crafts & Additions alternators of a world.

alternator_scan.py <world dir> [--json out.json] [--compare earlier.json]
Example: tools/alternator_scan.py world --json /tmp/alternators-133.json
Standard library only. Run it against a stopped server or right after "save-all flush".

The backport opens the back face of the alternator (AlternatorBlock.hasShaftTowards accepts both
faces of its axis). Create joins a placed alternator to whatever has a shaft behind it on the first
server tick after its chunk loads, and two networks that turn in opposite directions there break a
block. This scan lists, per dimension, every alternator whose back neighbour, at the position
opposite its facing, is a kinetic block (a block entity with Create's key Speed), with:
  - The neighbour's block id and block state, and whether it has a shaft towards the alternator:
    "shaft" and "no shaft" come from the shaft rules of Create 6.0.8 and CC&A for the common block
    shapes, "check" means the scan cannot tell and the spot needs a look in game.
  - The saved Speed of both and what Create does with it for a plain shaft link. Speed is the value
    of the last save, and a link through a gearbox or gearshift can reverse the direction.
With --compare it compares the alternator count of every dimension with an earlier --json of the
same world, for a run before and after a Chunky pass with the new jar. Every chunk and region file
it could not read is listed. Their alternators are missing from the counts.
"""
import gzip, json, os, struct, sys, zlib

ALTERNATOR = "createaddition:alternator"
DIRS = {"down": (0, -1, 0), "up": (0, 1, 0), "north": (0, 0, -1), "south": (0, 0, 1),
        "west": (-1, 0, 0), "east": (1, 0, 0)}
AXIS_OF = {"down": "y", "up": "y", "north": "z", "south": "z", "west": "x", "east": "x"}
# Blocks whose shaft faces are the faces off their axis (Create GearboxBlock.hasShaftTowards).
OFF_AXIS = {"create:gearbox"}
# Blocks with a shaft on their front face only (ElectricMotorBlock, Create CreativeMotorBlock).
FRONT_SHAFT = {"createaddition:electric_motor", "create:creative_motor"}
# Blocks with a shaft on both faces of their facing axis (the backported AlternatorBlock).
BOTH_FACES = {ALTERNATOR}
# Create blocks with a shaft on both faces of their axis and on no other face.
PILLAR = {"create:shaft", "create:powered_shaft", "create:cogwheel", "create:large_cogwheel",
          "create:andesite_encased_shaft", "create:brass_encased_shaft",
          "create:metal_girder_encased_shaft", "create:clutch", "create:gearshift",
          "create:encased_chain_drive", "create:adjustable_chain_gearshift"}
# Links that can change the direction of rotation, so the saved speeds do not compare directly.
REVERSING = {"create:gearbox", "create:gearshift", "create:sequenced_gearshift"}
BAD = []

# ---------- NBT ----------
def read_nbt(buf):
    pos = 0
    def u(fmt, n):
        nonlocal pos
        v = struct.unpack_from(fmt, buf, pos)
        pos += n
        return v[0]
    def string():
        nonlocal pos
        ln = u(">H", 2)
        s = bytes(buf[pos:pos + ln]).decode("utf-8", "replace")
        pos += ln
        return s
    def payload(t):
        nonlocal pos
        if t == 1: return u(">b", 1)
        if t == 2: return u(">h", 2)
        if t == 3: return u(">i", 4)
        if t == 4: return u(">q", 8)
        if t == 5: return u(">f", 4)
        if t == 6: return u(">d", 8)
        if t == 7:
            n = u(">i", 4); pos += n; return None
        if t == 8: return string()
        if t == 9:
            et = u(">b", 1); n = u(">i", 4)
            return [payload(et) for _ in range(n)]
        if t == 10:
            d = {}
            while True:
                tt = u(">b", 1)
                if tt == 0: return d
                name = string()
                d[name] = payload(tt)
        if t == 11:
            n = u(">i", 4); pos += 4 * n; return None
        if t == 12:
            # Long arrays stay raw bytes and are decoded on demand (block_states data).
            n = u(">i", 4); raw = buf[pos:pos + 8 * n]; pos += 8 * n; return raw
        raise ValueError("unknown tag type %d" % t)
    t = u(">b", 1)
    string()
    return payload(t)

# ---------- region files ----------
def chunks(path):
    data = open(path, "rb").read()
    if len(data) < 8192:
        if data: BAD.append((path, None, "header cut off"))
        return
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
        if off == 0 or data[i * 4 + 3] == 0: continue
        start = off * 4096
        try:
            ln = struct.unpack(">i", data[start:start + 4])[0]
            comp = data[start + 4]
            raw = data[start + 5:start + 4 + ln]
            if comp & 128:
                rx, rz = (int(v) for v in os.path.basename(path).split(".")[1:3])
                ext = os.path.join(os.path.dirname(path), "c.%d.%d.mcc" % (rx * 32 + i % 32, rz * 32 + i // 32))
                raw = open(ext, "rb").read()
                comp &= 127
            if comp == 2: raw = zlib.decompress(raw)
            elif comp == 1: raw = gzip.decompress(raw)
            elif comp != 3: raise ValueError("compression %d" % comp)
            yield path, i, read_nbt(raw)
        except Exception as e:  # noqa: BLE001 - a torn chunk must not stop the scan
            BAD.append((path, i, repr(e)))

def dimensions(world):
    dims = [("minecraft:overworld", world), ("minecraft:the_nether", os.path.join(world, "DIM-1")),
            ("minecraft:the_end", os.path.join(world, "DIM1"))]
    root = os.path.join(world, "dimensions")
    if os.path.isdir(root):
        for ns in sorted(os.listdir(root)):
            for dirpath, dirnames, _ in os.walk(os.path.join(root, ns)):
                if "region" in dirnames:
                    dims.append((ns + ":" + os.path.relpath(dirpath, os.path.join(root, ns)).replace(os.sep, "/"), dirpath))
    return [(d, p) for d, p in dims if os.path.isdir(os.path.join(p, "region"))]

# ---------- block states ----------
def block_state(chunk, x, y, z):
    """The palette entry {Name, Properties} at a position of this chunk, or None."""
    for sec in chunk.get("sections", []) or []:
        if sec.get("Y") != y >> 4: continue
        states = sec.get("block_states") or {}
        palette = states.get("palette") or []
        if not palette: return None
        data = states.get("data")
        if len(palette) == 1 or not data: return palette[0]
        bits = max(4, (len(palette) - 1).bit_length())
        per = 64 // bits
        idx = ((y & 15) << 8) | ((z & 15) << 4) | (x & 15)
        li, sh = divmod(idx, per)
        word = int.from_bytes(bytes(data[li * 8:li * 8 + 8]), "big")
        v = (word >> (sh * bits)) & ((1 << bits) - 1)
        return palette[v] if v < len(palette) else None
    return None

def scan_dim(base):
    """Alternators and kinetic block entities of one dimension, by position."""
    alts, kinetic = {}, {}
    region = os.path.join(base, "region")
    for f in sorted(os.listdir(region)):
        if not f.endswith(".mca"): continue
        for path, slot, c in chunks(os.path.join(region, f)):
            if "Level" in c:
                BAD.append((path, slot, "chunk in the format before 1.18, not scanned"))
                continue
            for be in c.get("block_entities", []) or []:
                bid = be.get("id")
                if bid != ALTERNATOR and "Speed" not in be: continue
                pos = (be["x"], be["y"], be["z"])
                st = block_state(c, *pos) or {}
                entry = {"be": bid, "block": st.get("Name"), "props": st.get("Properties") or {},
                         "speed": be.get("Speed", 0.0)}
                kinetic[pos] = entry
                if bid == ALTERNATOR: alts[pos] = entry
    return alts, kinetic

def shaft_towards(n, face):
    """Does block n have a shaft on its face that points in direction face. Returns a hint."""
    block, props = n["block"] or "", n["props"]
    axis = AXIS_OF[face]
    facing = props.get("facing")
    if block in BOTH_FACES and facing in DIRS:
        return "shaft" if AXIS_OF[facing] == axis else "no shaft"
    if block in FRONT_SHAFT and facing in DIRS:
        return "shaft" if facing == face else "no shaft"
    if block in OFF_AXIS and "axis" in props:
        return "shaft" if props["axis"] != axis else "no shaft"
    if "top_shaft" in props and "axis" in props:
        # Create EncasedCogwheelBlock: a shaft end only where top_shaft or bottom_shaft is set
        if props["axis"] != axis: return "no shaft"
        key = "top_shaft" if face in ("up", "south", "east") else "bottom_shaft"
        return "shaft" if props.get(key) == "true" else "no shaft"
    if block in PILLAR and "axis" in props:
        return "shaft" if props["axis"] == axis else "no shaft"
    if "axis_along_first" in props and facing in DIRS:
        # Create DirectionalAxisKineticBlock.getRotationAxis
        first = props["axis_along_first"] == "true"
        rot = {"x": "y" if first else "z", "y": "x" if first else "z", "z": "x" if first else "y"}[AXIS_OF[facing]]
        return "shaft" if rot == axis else "no shaft"
    if "axis" in props:
        return "shaft" if props["axis"] == axis else "check"
    if "horizontal_axis" in props:
        return "shaft" if props["horizontal_axis"] == axis else "check"
    return "check"

def outcome(n, sa, sn):
    if (n["block"] or "") in REVERSING:
        return "the link can reverse the direction, compare in game"
    if sa == 0 and sn == 0: return "both stand still in the save"
    if sa == 0: return "the alternator joins the turning network behind it"
    if sn == 0: return "the alternator's network drives the one behind it"
    if (sa > 0) != (sn > 0): return "opposite directions, Create breaks a block"
    if abs(sa) != abs(sn): return "different speeds, the faster network overpowers the slower one"
    return "same speed and direction"

def main():
    args = sys.argv[1:]
    if not args or args[0].startswith("-"):
        print(__doc__); sys.exit(2)
    world, out, earlier = args[0], None, None
    if "--json" in args: out = args[args.index("--json") + 1]
    if "--compare" in args: earlier = json.load(open(args[args.index("--compare") + 1]))
    dump, total = {}, 0
    for dim, base in dimensions(world):
        alts, kinetic = scan_dim(base)
        rows = []
        for pos, a in sorted(alts.items()):
            facing = a["props"].get("facing")
            if facing not in DIRS:
                rows.append((pos, None, a, None, "check", "alternator without a facing in the save"))
                continue
            d = DIRS[facing]
            back = (pos[0] - d[0], pos[1] - d[1], pos[2] - d[2])
            n = kinetic.get(back)
            if n is None: continue
            hint = shaft_towards(n, facing)
            if hint == "no shaft": continue
            rows.append((pos, back, a, n, hint, outcome(n, a["speed"], n["speed"])))
        if not alts and (earlier is None or not earlier.get(dim)): continue
        total += len(rows)
        print("%s: %d alternators, %d with a kinetic block behind them" % (dim, len(alts), len(rows)))
        for pos, back, a, n, hint, what in rows:
            if n is None:
                print("  alternator at %s: %s" % (pos, what)); continue
            props = ",".join("%s=%s" % kv for kv in sorted(n["props"].items()))
            print("  alternator at %s facing %s, speed %g" % (pos, a["props"]["facing"], a["speed"]))
            print("    behind it at %s: %s[%s] speed %g, %s: %s" % (back, n["block"], props, n["speed"], hint, what))
        if earlier is not None:
            before = earlier.get(dim, {}).get("alternators", 0)
            print("  alternators in the earlier scan: %d, now: %d" % (before, len(alts)))
        dump[dim] = {"alternators": len(alts),
                     "listed": [{"pos": list(r[0]), "back": list(r[1]) if r[1] else None,
                                 "block": r[3]["block"] if r[3] else None, "hint": r[4], "outcome": r[5]}
                                for r in rows]}
    print("listed in all dimensions: %d" % total)
    if BAD:
        print("unreadable: %d" % len(BAD))
        for path, slot, why in BAD[:50]:
            print("  %s slot %s: %s" % (path, slot, why))
    if out:
        json.dump(dump, open(out, "w"), indent=0)

if __name__ == "__main__":
    main()
