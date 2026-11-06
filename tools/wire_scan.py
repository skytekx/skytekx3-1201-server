#!/usr/bin/env python3
"""Read-only scan of the Create Crafts & Additions wire nodes of a world.

wire_scan.py <world dir> [--json out.json] [--compare earlier.json]
Example: tools/wire_scan.py world --json /tmp/wires-133.json
Standard library only. Run it against a stopped server or right after "save-all flush".

Reports, per dimension (overworld, the_nether, the_end and every dimensions/<ns>/<path>):
  1. Every connector, light connector, large connector and redstone relay block entity with its
     wires, counted, plus the wire ends whose far end does not point back (one sided). 1.3.3 deletes
     one end of a wire whose far end it cannot find, and the wire graph of the backport cuts the other
     end at its first check, so these are the wires a migration loses. Nodes still in the old
     node0..node7 format and nodes in chunks that are not full are counted too. Their old slots
     count as wires with absolute far ends, as WireVertex.fromBlockEntityTag reads them.
  2. With a saved wire graph (data/createaddition_wires.dat of the dimension), three lists:
     a. Differences: a position that both the graph and a block entity hold, with other wires.
     b. Gone: a graph vertex whose full chunk the scan read and that holds no wire node there.
     c. Not yet discovered: a block entity the graph does not know. The graph only learns a node
        when its chunk loads or a wire reaches it, so this list is no difference.
     Only a and b count as differences. Vertices in chunks the scan could not read or that are not
     full are counted apart as unchecked.
  3. With --compare, every wire node whose wires differ from an earlier --json of the same world.
Every chunk and region file it could not read is listed. Their nodes are missing from the counts.
"""
import gzip, json, os, struct, sys, zlib

IDS = {"createaddition:connector", "createaddition:small_light_connector",
       "createaddition:large_connector", "createaddition:redstone_relay"}
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
            n = u(">i", 4); pos += 8 * n; return None
        raise ValueError("unknown tag type %d" % t)
    t = u(">b", 1)
    string()
    return payload(t)

def unpack_pos(p):
    # BlockPos.asLong: 26 bits x, 26 bits z, 12 bits y
    x = p >> 38
    y = p & 0xFFF
    z = (p >> 12) & 0x3FFFFFF
    if y >= 1 << 11: y -= 1 << 12
    if z >= 1 << 25: z -= 1 << 26
    return (x, y, z)

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
            yield read_nbt(raw)
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

def is_full(status):
    return status in ("minecraft:full", "full")

def wires_of(be, pos):
    # As WireVertex.fromBlockEntityTag: the nodes list holds far ends relative to the node, the old
    # node0..node7 slots hold them absolute, and an old slot replaces a listed one with its index.
    slots = {}
    for n in be.get("nodes", []) or []:
        slots[n["id"]] = (n["id"], n["other"], n["type"], pos[0] + n["x"], pos[1] + n["y"], pos[2] + n["z"])
    for i in range(8):
        other = be.get("node%d" % i)
        if other is None or other == -1: continue
        slots[i] = (i, other, be.get("type%d" % i, 0), be.get("x%d" % i, 0), be.get("y%d" % i, 0), be.get("z%d" % i, 0))
    return sorted(slots.values())

def scan_dim(base):
    nodes, full = {}, set()
    region = os.path.join(base, "region")
    for f in sorted(os.listdir(region)):
        if not f.endswith(".mca"): continue
        for c in chunks(os.path.join(region, f)):
            if is_full(c.get("Status")) and "xPos" in c and "zPos" in c:
                full.add((c["xPos"], c["zPos"]))
            for be in c.get("block_entities", []) or []:
                if be.get("id") not in IDS: continue
                pos = (be["x"], be["y"], be["z"])
                legacy = sorted(k for k in be if k.startswith("node") and k != "nodes")
                nodes[pos] = {"id": be["id"], "wires": wires_of(be, pos), "legacy": legacy, "status": c.get("Status")}
    return nodes, full

def one_sided(nodes):
    res = []
    for pos, n in sorted(nodes.items()):
        for w in n["wires"]:
            other = nodes.get(w[3:6])
            if other is None or not any(o[0] == w[1] and o[1] == w[0] and o[3:6] == pos for o in other["wires"]):
                res.append((pos, w))
    return res

def graph_diff(base, nodes, full):
    path = os.path.join(base, "data", "createaddition_wires.dat")
    if not os.path.isfile(path): return None
    tag = read_nbt(gzip.decompress(open(path, "rb").read()))
    data = tag.get("data", tag)
    graph = {}
    for v in data.get("Vertices", []):
        graph[unpack_pos(v["Pos"])] = sorted((s["Index"], s["OtherIndex"], s["Type"]) + unpack_pos(s["Other"])
                                             for s in v.get("Slots", []))
    differ = [(p, graph[p], nodes[p]["wires"]) for p in sorted(set(graph) & set(nodes))
              if graph[p] != nodes[p]["wires"]]
    gone = [(p, graph[p]) for p in sorted(set(graph) - set(nodes)) if (p[0] >> 4, p[2] >> 4) in full]
    unchecked = [p for p in sorted(set(graph) - set(nodes)) if (p[0] >> 4, p[2] >> 4) not in full]
    undiscovered = [(p, nodes[p]["wires"]) for p in sorted(set(nodes) - set(graph))]
    return {"vertices": len(graph), "buffers": len(data.get("Buffers", [])), "differ": differ,
            "gone": gone, "unchecked": unchecked, "undiscovered": undiscovered}

def main():
    args = sys.argv[1:]
    if not args or args[0].startswith("-"):
        print(__doc__); sys.exit(2)
    world, out, earlier = args[0], None, None
    if "--json" in args: out = args[args.index("--json") + 1]
    if "--compare" in args: earlier = json.load(open(args[args.index("--compare") + 1]))
    dump = {}
    for dim, base in dimensions(world):
        nodes, full = scan_dim(base)
        if not nodes and not os.path.isfile(os.path.join(base, "data", "createaddition_wires.dat")): continue
        ends = sum(len(n["wires"]) for n in nodes.values())
        print("%s: %d wire nodes, %d wire ends" % (dim, len(nodes), ends))
        lonely = one_sided(nodes)
        print("  one sided wire ends: %d" % len(lonely))
        for pos, w in lonely[:50]:
            print("    %s node %d -> %s node %d (type %d)" % (pos, w[0], w[3:6], w[1], w[2]))
        legacy = [p for p, n in nodes.items() if n["legacy"]]
        partial = [p for p, n in nodes.items() if not is_full(n["status"])]
        print("  in the node0..node7 format: %d, in chunks that are not full: %d" % (len(legacy), len(partial)))
        g = graph_diff(base, nodes, full)
        if g is None:
            print("  no saved wire graph")
        else:
            print("  saved wire graph: %d vertices, %d buffers, %d differences (%d differ, %d gone)"
                  % (g["vertices"], g["buffers"], len(g["differ"]) + len(g["gone"]), len(g["differ"]), len(g["gone"])))
            for pos, gv, wv in g["differ"][:50]:
                print("    differ %s graph %s world %s" % (pos, gv, wv))
            for pos, gv in g["gone"][:50]:
                print("    gone %s graph %s, no wire node in its full chunk" % (pos, gv))
            print("  not yet discovered by the graph: %d wire nodes, %d of them with wires"
                  % (len(g["undiscovered"]), sum(1 for _, wv in g["undiscovered"] if wv)))
            for pos, wv in g["undiscovered"][:50]:
                print("    not yet discovered %s world %s" % (pos, wv))
            print("  unchecked vertices (chunk not read or not full): %d" % len(g["unchecked"]))
            for pos in g["unchecked"][:50]:
                print("    unchecked %s" % (pos,))
        if earlier is not None:
            old = {tuple(n["pos"]): [tuple(w) for w in n["wires"]] for n in earlier.get(dim, [])}
            new = {p: n["wires"] for p, n in nodes.items()}
            changed = [p for p in sorted(set(old) | set(new)) if old.get(p) != new.get(p)]
            print("  differ from the earlier scan: %d" % len(changed))
            for p in changed[:50]:
                print("    %s before %s now %s" % (p, old.get(p), new.get(p)))
        dump[dim] = [{"pos": list(p), "id": n["id"], "wires": [list(w) for w in n["wires"]]} for p, n in sorted(nodes.items())]
    if BAD:
        print("unreadable: %d" % len(BAD))
        for path, slot, why in BAD[:50]:
            print("  %s slot %s: %s" % (path, slot, why))
    if out:
        json.dump(dump, open(out, "w"), indent=0)

if __name__ == "__main__":
    main()
