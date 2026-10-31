#!/usr/bin/env python3
"""Read-only scan of a Skylands dimension for anything placed in the void between the islands.

void_scan.py <region_dir> [--band-min 24]
Example: tools/void_scan.py world/dimensions/skytekx3/skylands/region
Needs numpy. Run it against a stopped server or right after "save-all flush".

Reports
  0. Every chunk and region file it could not read: cut off, torn by a write during the read, or kept
     in a c.X.Z.mcc file that is missing. Their blocks and starts are missing from the counts below.
  1. Structure starts of all chunks, also of the chunks at the edge of the generated area that are not
     full yet: id, start chunk, lowest piece Y, biome at the start, pieces below the island band.
  2. Every non-air block below the island band (Y < band-min), grouped by block, with sample positions.
  3. Non-air blocks inside sky_void columns at any height, grouped by block (island rims are sky_void
     by design, so terrain blocks there are expected; anything else is a leak or a floater).
"""
import struct, sys, zlib, gzip, os, math, collections
import numpy as np

AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
VOID = "skytekx3:sky_void"
BAD = {}    # (region file, slot 0..1023 or None for the whole file) -> (chunk x, chunk z, reason)

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

# ---------- region files, read the way RegionFile of 1.20.1 reads them ----------
class Unreadable(Exception):
    pass

def slot_xz(path, i):
    """Chunk coordinates of slot i in the region file r.X.Z.mca, (None, None) for another file name."""
    p = os.path.basename(path).split(".")
    try:
        return int(p[1]) * 32 + (i & 31), int(p[2]) * 32 + (i >> 5)
    except (IndexError, ValueError):
        return None, None

def note_bad(path, i, why):
    cx, cz = slot_xz(path, i) if i is not None else (None, None)
    BAD.setdefault((os.path.basename(path), i), (cx, cz, why[:120]))

def unreadable(cx, cz):
    """True if chunk (cx, cz) or its whole region file is in BAD."""
    fn = f"r.{cx >> 5}.{cz >> 5}.mca"
    return (fn, (cx & 31) + (cz & 31) * 32) in BAD or (fn, None) in BAD

def header_ok(path, size):
    """True for a region file with its whole 8 KiB header. Minecraft leaves empty region files behind,
    those are fine. A file that ends inside its header has lost all its chunks and goes into BAD."""
    if 0 < size < 8192:
        note_bad(path, None, f"header cut off at {size} of 8192 bytes, every chunk in the file is lost")
    return size >= 8192

def decode(path, i, entry, sectors):
    """NBT of the chunk in slot i, None for an empty slot or an unreadable chunk (that one goes into BAD).
    entry is the slot's 4-byte header value, sectors the bytes of its sectors (fewer if the file ends early)."""
    if entry == 0:
        return None
    first, count = entry >> 8, entry & 255
    try:
        if first < 2 or count == 0:
            raise Unreadable(f"header entry points to sector {first} with {count} sectors")
        if len(sectors) < 5:
            raise Unreadable(f"sector {first} lies past the end of the file")
        ln, comp = int.from_bytes(sectors[:4], "big"), sectors[4]
        if ln == 0:
            raise Unreadable("length 0, the chunk data is missing")
        if comp & 128:
            # a chunk over 1 MiB: the slot keeps only the compression type plus 128, the data is in c.X.Z.mcc
            cx, cz = slot_xz(path, i)
            if cx is None:
                raise Unreadable("external chunk, but the region file name gives no position")
            mcc = f"c.{cx}.{cz}.mcc"
            try:
                with open(os.path.join(os.path.dirname(path), mcc), "rb") as f:
                    raw = f.read()
            except OSError as e:
                raise Unreadable(f"external chunk, {mcc}: {e.strerror}")
            comp &= 127
        elif ln - 1 > len(sectors) - 5:
            end = "the end of the file" if len(sectors) < count * 4096 else f"the end of its {count} sectors"
            raise Unreadable(f"{ln - 1} bytes of chunk data, {len(sectors) - 5} left before {end}")
        else:
            raw = sectors[5:4 + ln]
        if comp == 2:
            raw = zlib.decompress(raw)
        elif comp == 1:
            raw = gzip.decompress(raw)
        elif comp != 3:
            raise Unreadable(f"compression type {comp}, 1.20.1 writes only 1 to 3")
        c = read_nbt(raw)
        if not isinstance(c, dict) or "xPos" not in c or "zPos" not in c:
            raise Unreadable("no chunk position in the NBT")
        return c
    except Exception as e:  # also a chunk torn by a write during the read
        note_bad(path, i, str(e) if isinstance(e, Unreadable) else repr(e))
        return None

def read_region(path):
    """Bytes of one region file, None if it is empty or unreadable (the latter goes into BAD)."""
    try:
        with open(path, "rb") as f:
            data = f.read()
    except OSError as e:
        note_bad(path, None, f"cannot read the file: {e.strerror}")
        return None
    return data if header_ok(path, len(data)) else None

def chunks(path):
    """Yield the NBT of every readable chunk in one region file, the unreadable ones go into BAD."""
    data = read_region(path)
    if data is None:
        return
    for i in range(1024):
        entry = int.from_bytes(data[i * 4:i * 4 + 4], "big")
        p = (entry >> 8) * 4096
        c = decode(path, i, entry, data[p:p + (entry & 255) * 4096])
        if c is not None:
            yield c

def load_chunk(region_dir, cx, cz):
    """NBT of chunk (cx, cz), None if it was never generated or is unreadable (then it is in BAD)."""
    path = os.path.join(region_dir, f"r.{cx >> 5}.{cz >> 5}.mca")
    i = (cx & 31) + (cz & 31) * 32
    if not os.path.exists(path):
        return None
    try:
        with open(path, "rb") as f:
            head = f.read(8192)
            if not header_ok(path, len(head)):
                return None
            entry = int.from_bytes(head[i * 4:i * 4 + 4], "big")
            f.seek((entry >> 8) * 4096)
            sectors = f.read((entry & 255) * 4096)
    except OSError as e:
        note_bad(path, None, f"cannot read the file: {e.strerror}")
        return None
    return decode(path, i, entry, sectors)

def print_bad(limit=50):
    """Print how many chunks and region files could not be read, and which ones."""
    files = sum(1 for fn, i in BAD if i is None)
    print(f"unreadable chunks: {len(BAD) - files}" + (f", unreadable region files: {files}" if files else ""))
    for n, ((fn, i), (cx, cz, why)) in enumerate(BAD.items()):
        if n == limit:
            print(f"  ... and {len(BAD) - limit} more")
            break
        where = "" if i is None else f" slot {i}" if cx is None else f" chunk {cx},{cz}"
        print(f"  {fn}{where}: {why}")

def status(c):
    return str(c.get("Status", "")).split(":")[-1]

def is_full(c):
    return status(c) == "full"

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

def biome_at(c, x, y, z):
    """Biome stored for block (x, y, z) in chunk c. None without a chunk, or for a chunk saved before
    the biomes step, whose palette still holds a placeholder."""
    if c is None or status(c) in ("empty", "structure_starts", "structure_references"):
        return None
    for s in c.get("sections", []):
        if s["Y"] == y >> 4:
            pal, idx = section_biomes(s)
            if pal is not None:
                return pal[idx[(((y & 15) >> 2) * 4 + ((z & 15) >> 2)) * 4 + ((x & 15) >> 2)]]
    return None

def main():
    region_dir = sys.argv[1]
    band_min = 24
    if "--band-min" in sys.argv:
        band_min = int(sys.argv[sys.argv.index("--band-min") + 1])

    starts = []                      # (id, cx, cz, minY, biome, low_pieces, piece_count, start chunk full)
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
            cx, cz = c["xPos"], c["zPos"]
            full = is_full(c)
            # structure starts of every chunk: at the edge of the generated area a start can sit in a
            # chunk that is not full yet while its pieces already stand in full chunks next to it
            for sid, st in c.get("structures", {}).get("starts", {}).items():
                if st.get("id", sid) == "INVALID":
                    continue
                kids = st.get("Children", [])
                if not kids:
                    continue
                bbs = [[int(v) for v in k["BB"]] for k in kids if "BB" in k]
                miny = min(b[1] for b in bbs) if bbs else None
                low = sum(1 for b in bbs if b[1] < band_min)
                biome = None
                if bbs:
                    # the biome check of a jigsaw start looks at the center of its first piece, which can
                    # lie in a neighbouring chunk (Java divides toward zero)
                    b0 = bbs[0]
                    x, z = int((b0[0] + b0[3]) / 2), int((b0[2] + b0[5]) / 2)
                    home = (x >> 4, z >> 4) == (cx, cz)
                    biome = biome_at(c if home else load_chunk(region_dir, x >> 4, z >> 4), x, b0[1], z)
                starts.append((sid, cx, cz, miny, biome, low, len(kids), full))
            if not full:
                continue
            n_full += 1
            secs = {s["Y"]: s for s in c.get("sections", [])}
            # biome per 4x4 column, taken from the section at Y 64..79 (the climate is 2D here)
            bpal, bidx = section_biomes(secs.get(4, {}))
            colvoid = np.zeros((4, 4), dtype=bool)
            if bpal is not None:
                grid = np.array([bpal[i] for i in bidx]).reshape(4, 4, 4)[0]   # [z][x] at the lowest y of the section
                colvoid = grid == VOID
            void_cols += int(colvoid.sum())
            island_cols += int((~colvoid).sum())
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

    print_bad()
    print(f"chunks={n_chunks} full={n_full} columns(4x4): island={island_cols} void={void_cols} ({void_cols * 100 // max(1, void_cols + island_cols)}% void)")
    print(f"\n== 1. Structure starts: {len(starts)}, {sum(1 for s in starts if not s[7])} of them in chunks that are not full yet")
    agg = collections.defaultdict(lambda: [0, 10**9, 0, collections.Counter()])
    for sid, cx, cz, miny, biome, low, n, full in starts:
        a = agg[sid]; a[0] += 1; a[1] = min(a[1], miny if miny is not None else 10**9); a[2] += 1 if low else 0; a[3][biome] += 1
    for sid, (n, miny, nlow, biomes) in sorted(agg.items(), key=lambda kv: -kv[1][0]):
        vb = biomes.get(VOID, 0)
        print(f"  {sid:52s} n={n:4d} minY={miny:4d} starts_with_pieces_below_{band_min}={nlow:3d} start_in_sky_void={vb} start_biome_unknown={biomes.get(None, 0)}")
    bad = [s for s in starts if s[5] > 0]
    print(f"\n   Starts with any piece below Y {band_min}: {len(bad)}")
    for s in bad[:40]:
        print(f"    {s[0]} chunk=({s[1]},{s[2]}) block~({s[1]*16},{s[2]*16}) minY={s[3]} low_pieces={s[5]}/{s[6]} start_biome={s[4]}{'' if s[7] else ' (start chunk not full yet)'}")
    print(f"\n== 2. Non-air blocks below Y {band_min}: {sum(low_blocks.values())}")
    for name, n in low_blocks.most_common(40):
        print(f"  {name:45s} {n:7d}  e.g. {low_samples[name][:4]}")
    print(f"\n== 3. Non-air blocks in sky_void columns (any Y): {sum(void_blocks.values())}")
    for name, n in void_blocks.most_common(60):
        print(f"  {name:45s} {n:8d}  e.g. {void_samples[name][:2]}")

if __name__ == "__main__":
    main()
