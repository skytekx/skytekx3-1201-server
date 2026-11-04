#!/usr/bin/env python3
"""Prints the datapack order of a world: datapack_order.py <level.dat> [name ...]

The server keeps the order of the enabled packs in level.dat and appends the pack of every mod it
sees for the first time to the end, so a new mod can load after the fixes pack and shadow its
overrides (okf/content/boot-fixes.md, section Load order). /datapack list enabled shows the order too,
but tools/rcon.py prints only the first 4 KB of its answer. Read a level.dat that the server has saved,
for example after save-all or a stop. The script only reads the file and needs the standard library
only, so the system Python 3.9 of the Mac mini runs it.

Without names it prints the file packs (file/...), with names every pack whose id contains one of
them, each with its place in the load order (1 loads first). It always says which mod packs load after
the fixes pack.
"""
import gzip
import struct
import sys

FIXES = "file/skytekx3_fixes"


def read(f, n):
    b = f.read(n)
    if len(b) != n:
        raise EOFError("level.dat ends early")
    return b


def payload(f, tag):
    """One NBT payload of type `tag` (Java edition, big endian)."""
    if tag in (1, 2, 3, 4, 5, 6):
        fmt, n = {1: (">b", 1), 2: (">h", 2), 3: (">i", 4), 4: (">q", 8), 5: (">f", 4), 6: (">d", 8)}[tag]
        return struct.unpack(fmt, read(f, n))[0]
    if tag == 7:
        return read(f, struct.unpack(">i", read(f, 4))[0])
    if tag == 8:
        return read(f, struct.unpack(">H", read(f, 2))[0]).decode("utf-8", "replace")
    if tag == 9:
        inner = read(f, 1)[0]
        return [payload(f, inner) for _ in range(struct.unpack(">i", read(f, 4))[0])]
    if tag == 10:
        out = {}
        while True:
            inner = read(f, 1)[0]
            if inner == 0:
                return out
            name = read(f, struct.unpack(">H", read(f, 2))[0]).decode("utf-8", "replace")
            out[name] = payload(f, inner)
    if tag in (11, 12):
        n = struct.unpack(">i", read(f, 4))[0]
        size = 4 if tag == 11 else 8
        return list(struct.unpack(">%d%s" % (n, "i" if tag == 11 else "q"), read(f, size * n)))
    raise ValueError("unknown NBT tag %d" % tag)


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__.splitlines()[0])
    with gzip.open(sys.argv[1], "rb") as f:
        tag = read(f, 1)[0]
        read(f, struct.unpack(">H", read(f, 2))[0])
        root = payload(f, tag)
    packs = root["Data"]["DataPacks"]
    enabled = packs.get("Enabled", [])
    names = sys.argv[2:] or ["file/"]
    print("enabled %d, disabled %d" % (len(enabled), len(packs.get("Disabled", []))))
    for i, p in enumerate(enabled):
        if any(n in p for n in names):
            print("%4d %s" % (i + 1, p))
    if FIXES in enabled:
        after = [p for p in enabled[enabled.index(FIXES) + 1:] if p.startswith("mod:")]
        print("mod packs after %s: %d%s" % (FIXES, len(after), (" (" + ", ".join(after) + ")") if after else ""))
    else:
        print("%s is not enabled" % FIXES)


if __name__ == "__main__":
    main()
