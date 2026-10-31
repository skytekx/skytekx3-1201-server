#!/usr/bin/env python3
"""Read-only scan of a Skylands dimension for anything placed in the void between the islands.

void_scan.py <region_dir> [--band-min 24]
Example: tools/void_scan.py world/dimensions/skytekx3/skylands/region
Needs numpy. Run it against a stopped server or right after "save-all flush".

Reports
  1. Structure starts: id, start chunk, lowest piece Y, biome at the start, pieces below the island band.
  2. Every non-air block below the island band (Y < band-min), grouped by block, with sample positions.
  3. Non-air blocks inside sky_void columns at any height, grouped by block (island rims are sky_void
     by design, so terrain blocks there are expected; anything else is a leak or a floater).
"""
import io, struct, sys, zlib, gzip, os, math, collections
import numpy as np

AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
VOID = "skytekx3:sky_void"
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
        s = buf[pos:pos + ln].decode("utf-8", "replace")
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
            n = u(">i", 4); v = buf[pos:pos + n]; pos += n; return v
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
            n = u(">i", 4); v = np.frombuffer(buf, ">i4", n, pos); pos += 4 * n; return v
        if t == 12:
            n = u(">i", 4); v = np.frombuffer(buf, ">i8", n, pos); pos += 8 * n; return v
        raise ValueError(f"bad tag {t}")
    t = u(">b", 1)
    string()
    return payload(t)

def chunks(path):
    with open(path, "rb") as f:
        data = f.read()
    if len(data) < 8192:
        return
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], "big")
        if off == 0:
            continue
        p = off * 4096
        ln = int.from_bytes(data[p:p + 4], "big")
        comp = data[p + 4]
        raw = data[p + 5:p + 4 + ln]
        try:
            if comp == 2:
                raw = zlib.decompress(raw)
            elif comp == 1:
                raw = gzip.decompress(raw)
            elif comp == 3:
                pass
            else:
                continue  # external or lz4 chunk, not used by 1.20.1
            nbt = read_nbt(raw)
        except Exception as e:  # a chunk caught mid write, count it and go on
            BAD.append((os.path.basename(path), i, repr(e)[:80]))
            continue
        yield nbt

def unpack(data, bits, count):
    vpl = 64 // bits
    arr = data.astype(np.int64).view(np.uint64)
    mask = np.uint64((1 << bits) - 1)
    out = np.empty((arr.size, vpl), dtype=np.uint64)
    for k in range(vpl):
        out[:, k] = (arr >> np.uint64(k * bits)) & mask
    return out.reshape(-1)[:count].astype(np.int64)

def section_blocks(sec):
    bs = sec.get("block_states")
    if not bs:
        return None, None
    pal = [p["Name"] for p in bs["palette"]]
    if len(pal) == 1:
        return pal, np.zeros(4096, dtype=np.int64)
    bits = max(4, math.ceil(math.log2(len(pal))))
    return pal, unpack(bs["data"], bits, 4096)

def section_biomes(sec):
    b = sec.get("biomes")
    if not b:
        return None, None
    pal = list(b["palette"])
    if len(pal) == 1:
        return pal, np.zeros(64, dtype=np.int64)
    bits = math.ceil(math.log2(len(pal)))
    return pal, unpack(b["data"], bits, 64)

def main():
    region_dir = sys.argv[1]
    band_min = 24
    if "--band-min" in sys.argv:
        band_min = int(sys.argv[sys.argv.index("--band-min") + 1])

    starts = []                      # (id, cx, cz, minY, biome, low_pieces, piece_count)
    low_blocks = collections.Counter()
    low_samples = collections.defaultdict(list)
    void_blocks = collections.Counter()
    void_samples = collections.defaultdict(list)
    n_chunks = n_full = 0
    void_cols = island_cols = 0

    for fn in sorted(os.listdir(region_dir)):
        if not fn.endswith(".mca"):
            continue
        for c in chunks(os.path.join(region_dir, fn)):
            n_chunks += 1
            if c.get("Status", "") not in ("minecraft:full", "full"):
                continue
            n_full += 1
            cx, cz = c["xPos"], c["zPos"]
            secs = {s["Y"]: s for s in c.get("sections", [])}
            # biome per 4x4 column, taken from the section at Y 64..79 (the climate is 2D here)
            bpal, bidx = section_biomes(secs.get(4, {}))
            colvoid = np.zeros((4, 4), dtype=bool)
            if bpal is not None:
                grid = np.array([bpal[i] for i in bidx]).reshape(4, 4, 4)[0]   # [z][x] at the lowest y of the section
                colvoid = grid == VOID
            void_cols += int(colvoid.sum())
            island_cols += int((~colvoid).sum())
            # structure starts
            for sid, st in c.get("structures", {}).get("starts", {}).items():
                if st.get("id", sid) == "INVALID":
                    continue
                kids = st.get("Children", [])
                if not kids:
                    continue
                bbs = [k["BB"] for k in kids if "BB" in k]
                miny = min(int(b[1]) for b in bbs) if bbs else None
                low = sum(1 for b in bbs if int(b[1]) < band_min)
                b0 = bbs[0] if bbs else None
                biome = None
                if b0 is not None:
                    sy = int(b0[1]) // 16
                    p2, i2 = section_biomes(secs.get(sy, {}))
                    if p2 is not None:
                        lx = ((int(b0[0]) + int(b0[3])) // 2) & 15
                        lz = ((int(b0[2]) + int(b0[5])) // 2) & 15
                        ly = int(b0[1]) & 15
                        biome = p2[i2[((ly // 4) * 4 + lz // 4) * 4 + lx // 4]]
                starts.append((sid, cx, cz, miny, biome, low, len(kids)))
            # blocks
            for sy, sec in secs.items():
                if sy < 0:
                    continue
                pal, idx = section_blocks(sec)
                if pal is None or all(p in AIR for p in pal):
                    continue
                names = np.array(pal)
                nonair = ~np.isin(names, list(AIR))
                block_nonair = nonair[idx].reshape(16, 16, 16)          # [y][z][x]
                if sy * 16 < band_min:
                    ys, zs, xs = np.nonzero(block_nonair)
                    for y, z, x in zip(ys, zs, xs):
                        wy = sy * 16 + int(y)
                        if wy >= band_min:
                            continue
                        name = pal[idx[(int(y) * 16 + int(z)) * 16 + int(x)]]
                        low_blocks[name] += 1
                        if len(low_samples[name]) < 6:
                            low_samples[name].append((cx * 16 + int(x), wy, cz * 16 + int(z)))
                if colvoid.any():
                    vmask = np.repeat(np.repeat(colvoid, 4, axis=0), 4, axis=1)   # [z][x] 16x16
                    sel = block_nonair & vmask[None, :, :]
                    if sel.any():
                        ys, zs, xs = np.nonzero(sel)
                        flat = idx.reshape(16, 16, 16)[ys, zs, xs]
                        for k, cnt in zip(*np.unique(flat, return_counts=True)):
                            void_blocks[pal[k]] += int(cnt)
                            if len(void_samples[pal[k]]) < 3:
                                j = int(np.nonzero(flat == k)[0][0])
                                void_samples[pal[k]].append((cx * 16 + int(xs[j]), sy * 16 + int(ys[j]), cz * 16 + int(zs[j])))

    print(f"unreadable chunks: {len(BAD)} {BAD[:5]}")
    print(f"chunks={n_chunks} full={n_full} columns(4x4): island={island_cols} void={void_cols} ({void_cols * 100 // max(1, void_cols + island_cols)}% void)")
    print(f"\n== 1. Structure starts: {len(starts)}")
    agg = collections.defaultdict(lambda: [0, 10**9, 0, collections.Counter()])
    for sid, cx, cz, miny, biome, low, n in starts:
        a = agg[sid]; a[0] += 1; a[1] = min(a[1], miny if miny is not None else 10**9); a[2] += 1 if low else 0; a[3][biome] += 1
    for sid, (n, miny, nlow, biomes) in sorted(agg.items(), key=lambda kv: -kv[1][0]):
        vb = biomes.get(VOID, 0)
        print(f"  {sid:52s} n={n:4d} minY={miny:4d} starts_with_pieces_below_{band_min}={nlow:3d} start_in_sky_void={vb}")
    bad = [s for s in starts if s[5] > 0]
    print(f"\n   Starts with any piece below Y {band_min}: {len(bad)}")
    for s in bad[:40]:
        print(f"    {s[0]} chunk=({s[1]},{s[2]}) block~({s[1]*16},{s[2]*16}) minY={s[3]} low_pieces={s[5]}/{s[6]} start_biome={s[4]}")
    print(f"\n== 2. Non-air blocks below Y {band_min}: {sum(low_blocks.values())}")
    for name, n in low_blocks.most_common(40):
        print(f"  {name:45s} {n:7d}  e.g. {low_samples[name][:4]}")
    print(f"\n== 3. Non-air blocks in sky_void columns (any Y): {sum(void_blocks.values())}")
    for name, n in void_blocks.most_common(60):
        print(f"  {name:45s} {n:8d}  e.g. {void_samples[name][:2]}")

if __name__ == "__main__":
    main()
