#!/usr/bin/env python3
"""Replace the five old blocks of Chocolate Quest with the normal blocks in a stopped world.

Chocolate Quest Golden Apple Edition builds its Builder Village from five decoration blocks: old
cobblestone, old iron block, old diamond ore, old glass and old obsidian. They look like the vanilla
blocks, but no furnace, Fortune or smeltery takes them. The owner asked on 2026-11-04 at 17:33 to
replace them everywhere. This script rewrites the existing world. The fixes datapack covers new
villages (a rule processor in the template pool) and placed blocks that this script cannot reach
(loot tables), and kubejs/server_scripts/unify_inventory.js swaps the items that players still carry.

It walks the whole world folder, so every dimension is included, and rewrites:
  Region files (region/*.mca): The block palette of every chunk section, with the duplicate entries
    that a rename creates merged and the block data packed again, and every NBT string that equals
    an old id, such as items in chests and shulker boxes. A chunk where old glass became glass loses
    its isLightOn flag, so the server relights it when it loads, because old glass blocks light and
    glass does not. Every start of chocolate_quest:builder_village gets the rule processor of the
    fixes datapack, so the parts of a village that generate later come out with the normal blocks.
  Entity files (entities/*.mca): Every NBT string that equals an old id, which covers item
    entities, item frames, armor stands and minecarts.
  Other NBT files (.dat, .dat_old and .nbt): The same for player data with inventory, ender chest
    and curios, for saved data under data/ such as backpacks, and for the files of mods such as the
    GraveStone deaths. It leaves level.dat, level.dat_old, poi/ and datapacks/ alone.

The script refuses to run while a server holds world/session.lock and holds that lock itself while
it runs. Chunks it does not change keep their bytes and their place in the file. Every changed file
is read back and checked, written to a temporary file in the same folder and renamed over the old
one. A second run finds nothing to change. The dry run reads the same data and writes nothing.

Usage: python3 tools/replace_blocks.py [--dry-run] [--verbose] WORLD
Needs Python 3.9 or newer and the standard library only.
"""
import argparse
import errno
import fcntl
import os
import re
import stat
import struct
import sys
import time
import zlib

OLD_TO_NEW = {
    'chocolate_quest:old_cobblestone': 'minecraft:cobblestone',
    'chocolate_quest:old_iron_block': 'minecraft:iron_block',
    'chocolate_quest:old_diamond_ore': 'minecraft:diamond_ore',
    'chocolate_quest:old_glass': 'minecraft:glass',
    'chocolate_quest:old_obsidian': 'minecraft:obsidian',
}
OLD = dict((k.encode('ascii'), v.encode('ascii')) for k, v in OLD_TO_NEW.items())
NEW_IDS = set(OLD.values())
PREFIX = b'chocolate_quest:old_'
# Any old id, also inside a longer string. Chunks and files without a match are not parsed at all.
OLD_RE = re.compile(rb'chocolate_quest:old_(?:cobblestone|iron_block|diamond_ore|glass|obsidian)')
# OldGlassBlock.getLightBlock returns 15 like stone, while glass returns 0, so light changes where
# old glass was. The other four old blocks and their replacements are all full opaque cubes that
# block 15 and emit nothing.
LIGHT_CHANGES = set([b'chocolate_quest:old_glass'])
VILLAGE = b'chocolate_quest:builder_village'
# The uid.dat of every dimension is the world UUID of Bukkit (Arclight), 16 raw bytes and no NBT.
SKIP_FILES = set(['level.dat', 'level.dat_old', 'session.lock', 'uid.dat'])
SKIP_DIRS = set(['datapacks', 'poi'])
NBT_SUFFIXES = ('.dat', '.dat_old', '.nbt')
REGION_NAME = re.compile(r'^r\.(-?\d+)\.(-?\d+)\.mca$')

# NBT, kept as raw bytes wherever possible, so a parse and a dump give back the same bytes. A
# compound is a dict of name to (type, value) and a list is [element type, items]. Numbers and
# arrays stay raw bytes without their length prefix, and so do the items of a list of numbers.
END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY, LONG_ARRAY = range(13)
FIXED = {BYTE: 1, SHORT: 2, INT: 4, LONG: 8, FLOAT: 4, DOUBLE: 8}
ARRAYS = {BYTE_ARRAY: 1, INT_ARRAY: 4, LONG_ARRAY: 8}
MAX_DEPTH = 512


class NbtError(Exception):
    pass


def _need(buf, end):
    if end > len(buf):
        raise NbtError('truncated NBT')


def _payload(buf, pos, kind, depth):
    """Reads one payload of the given type and returns (value, next position)."""
    size = FIXED.get(kind)
    if size is not None:
        _need(buf, pos + size)
        return buf[pos:pos + size], pos + size
    if kind == STRING:
        _need(buf, pos + 2)
        end = pos + 2 + ((buf[pos] << 8) | buf[pos + 1])
        _need(buf, end)
        return buf[pos + 2:end], end
    if depth > MAX_DEPTH:
        raise NbtError('NBT nested deeper than %d' % MAX_DEPTH)
    if kind == COMPOUND:
        out = {}
        while True:
            _need(buf, pos + 1)
            tag = buf[pos]
            if tag == END:
                return out, pos + 1
            _need(buf, pos + 3)
            end = pos + 3 + ((buf[pos + 1] << 8) | buf[pos + 2])
            _need(buf, end)
            name = buf[pos + 3:end]
            if name in out:
                raise NbtError('duplicate key in a compound')
            value, pos = _payload(buf, end, tag, depth + 1)
            out[name] = (tag, value)
    if kind == LIST:
        _need(buf, pos + 5)
        tag = buf[pos]
        count = int.from_bytes(buf[pos + 1:pos + 5], 'big', signed=True)
        pos += 5
        if count < 0 or (count > 0 and tag == END):
            raise NbtError('bad list header')
        size = FIXED.get(tag)
        if size is not None:
            _need(buf, pos + size * count)
            return [tag, buf[pos:pos + size * count]], pos + size * count
        items = []
        for _ in range(count):
            value, pos = _payload(buf, pos, tag, depth + 1)
            items.append(value)
        return [tag, items], pos
    width = ARRAYS.get(kind)
    if width is not None:
        _need(buf, pos + 4)
        count = int.from_bytes(buf[pos:pos + 4], 'big', signed=True)
        if count < 0:
            raise NbtError('negative array length')
        _need(buf, pos + 4 + count * width)
        return buf[pos + 4:pos + 4 + count * width], pos + 4 + count * width
    raise NbtError('unknown tag type %d' % kind)


def nbt_load(buf):
    """Parses a named root compound and returns (name, compound)."""
    if len(buf) < 3 or buf[0] != COMPOUND:
        raise NbtError('the root is not a compound')
    end = 3 + ((buf[1] << 8) | buf[2])
    _need(buf, end)
    value, pos = _payload(buf, end, COMPOUND, 0)
    if pos != len(buf):
        raise NbtError('%d bytes after the root tag' % (len(buf) - pos))
    return buf[3:end], value


def _emit(out, kind, value):
    if kind in FIXED:
        out.append(value)
    elif kind == STRING:
        out.append(len(value).to_bytes(2, 'big'))
        out.append(value)
    elif kind == COMPOUND:
        for name, (tag, item) in value.items():
            out.append(bytes((tag,)) + len(name).to_bytes(2, 'big'))
            out.append(name)
            _emit(out, tag, item)
        out.append(b'\x00')
    elif kind == LIST:
        tag, items = value
        if isinstance(items, (bytes, bytearray)):
            out.append(bytes((tag,)) + (len(items) // FIXED[tag]).to_bytes(4, 'big', signed=True))
            out.append(items)
        else:
            out.append(bytes((tag,)) + len(items).to_bytes(4, 'big', signed=True))
            for item in items:
                _emit(out, tag, item)
    else:
        out.append((len(value) // ARRAYS[kind]).to_bytes(4, 'big', signed=True))
        out.append(value)


def nbt_dump(name, value):
    out = [bytes((COMPOUND,)) + len(name).to_bytes(2, 'big'), name]
    _emit(out, COMPOUND, value)
    return b''.join(out)


def s(text):
    return (STRING, text)


def rule_processor():
    """The rule processor of the fixes datapack pool, as the game stores it in a structure start."""
    rules = []
    for old, new in OLD.items():
        rules.append({
            b'input_predicate': (COMPOUND, {b'predicate_type': s(b'minecraft:block_match'), b'block': s(old)}),
            b'location_predicate': (COMPOUND, {b'predicate_type': s(b'minecraft:always_true')}),
            b'output_state': (COMPOUND, {b'Name': s(new)}),
        })
    return {b'processor_type': s(b'minecraft:rule'), b'rules': (LIST, [COMPOUND, rules])}


def short(name):
    return name.decode('ascii').split(':', 1)[1]


def signed(raw):
    return int.from_bytes(raw, 'big', signed=True)


class Stats(object):
    """Counts for one chunk, one file or the whole run."""

    def __init__(self, verbose=False):
        self.verbose = verbose
        self.blocks = {}      # Old id to the number of block positions in palettes.
        self.strings = {}     # Old id to the number of other strings replaced.
        self.items = {}       # Old id to the item count of the replaced item stacks.
        self.keys = {}        # Old id seen as a compound key, not changed.
        self.inner = {}       # Old id inside a longer string, not changed.
        self.starts = 0       # Village starts that got the rule processor.
        self.starts_done = 0  # Village starts that already had it.
        self.relit = 0
        self.merged = 0       # Palette entries merged away.
        self.chunks = 0
        self.chunks_changed = 0
        self.files = 0
        self.files_changed = 0
        self.bytes = 0
        self.dirty = False
        self.notes = []
        self.errors = []
        self.unread = []      # NBT files that are neither gzip nor raw NBT, or that do not inflate.

    def bump(self, table, key, n=1):
        table[key] = table.get(key, 0) + n

    def add(self, other):
        for name in ('blocks', 'strings', 'items', 'keys', 'inner'):
            mine = getattr(self, name)
            for key, n in getattr(other, name).items():
                mine[key] = mine.get(key, 0) + n
        for name in ('starts', 'starts_done', 'relit', 'merged', 'chunks', 'chunks_changed', 'files',
                     'files_changed', 'bytes'):
            setattr(self, name, getattr(self, name) + getattr(other, name))
        self.notes.extend(other.notes)
        self.errors.extend(other.errors)
        self.unread.extend(other.unread)

    def changes(self):
        return sum(self.blocks.values()) + sum(self.strings.values()) + self.starts + self.relit

    def describe(self):
        parts = []
        for title, table in (('blocks', self.blocks), ('strings', self.strings)):
            if table:
                text = ', '.join('%s %d' % (short(k), table[k]) for k in sorted(table))
                if title == 'strings' and self.items:
                    text += ' (item count %s)' % ', '.join('%s %d' % (short(k), self.items[k])
                                                          for k in sorted(self.items))
                parts.append('%s: %s' % (title, text))
        if self.starts:
            parts.append('village starts patched %d' % self.starts)
        if self.starts_done:
            parts.append('village starts already patched %d' % self.starts_done)
        if self.relit:
            parts.append('relight %d' % self.relit)
        if self.merged:
            parts.append('palette entries merged %d' % self.merged)
        for title, table in (('old id as a key, not changed', self.keys),
                             ('old id inside a longer string, not changed', self.inner)):
            if table:
                parts.append('%s: %s' % (title, ', '.join('%s %d' % (short(k), table[k]) for k in sorted(table))))
        return '  '.join(parts)


def _count_of(compound):
    for key in (b'Count', b'count'):
        entry = compound.get(key)
        if entry is not None and entry[0] in FIXED and entry[0] not in (FLOAT, DOUBLE):
            return signed(entry[1])
    return None


def _child(path, name):
    if path is None:
        return None
    part = name.decode('utf-8', 'replace')
    return path + '.' + part if path else part


def replace_strings(compound, stats, path, skip=None):
    """Replaces every string in a compound tree that equals an old id, in place."""
    for name, (tag, value) in compound.items():
        if name == skip:
            continue
        if name in OLD:
            stats.bump(stats.keys, name)
        if tag == STRING:
            new = OLD.get(value)
            if new is not None:
                compound[name] = (STRING, new)
                stats.bump(stats.strings, value)
                stats.dirty = True
                if name == b'id':
                    count = _count_of(compound)
                    if count is not None:
                        stats.bump(stats.items, value, count)
                if path is not None:
                    stats.notes.append('    %s: %s' % (_child(path, name), short(value)))
            elif PREFIX in value and OLD_RE.search(value):
                stats.bump(stats.inner, OLD_RE.search(value).group(0))
        elif tag == COMPOUND:
            replace_strings(value, stats, _child(path, name))
        elif tag == LIST:
            _replace_list(value, stats, _child(path, name))


def _replace_list(lst, stats, path):
    tag, items = lst
    if tag == STRING:
        for i, value in enumerate(items):
            new = OLD.get(value)
            if new is not None:
                items[i] = new
                stats.bump(stats.strings, value)
                stats.dirty = True
                if path is not None:
                    stats.notes.append('    %s[%d]: %s' % (path, i, short(value)))
            elif PREFIX in value and OLD_RE.search(value):
                stats.bump(stats.inner, OLD_RE.search(value).group(0))
    elif tag == COMPOUND:
        for i, item in enumerate(items):
            replace_strings(item, stats, None if path is None else '%s[%d]' % (path, i))
    elif tag == LIST:
        for i, item in enumerate(items):
            _replace_list(item, stats, None if path is None else '%s[%d]' % (path, i))


def _has_old(tag, value):
    if tag == STRING:
        return value in OLD
    if tag == COMPOUND:
        return any(_has_old(t, v) for t, v in value.values())
    if tag == LIST and not isinstance(value[1], (bytes, bytearray)):
        return any(_has_old(value[0], v) for v in value[1])
    return False


def storage_bits(size):
    """Bits per entry of a section palette of this size, as PalettedContainer packs and unpacks it."""
    if size <= 1:
        return 0
    bits = (size - 1).bit_length()
    return 4 if bits <= 4 else bits


def unpack_longs(raw, bits, count=4096):
    per = 64 // bits
    need = (count + per - 1) // per
    if len(raw) != need * 8:
        return None
    mask = (1 << bits) - 1
    out = []
    for word in struct.unpack('>%dQ' % need, raw):
        for _ in range(per):
            out.append(word & mask)
            word >>= bits
    del out[count:]
    return out


def pack_longs(values, bits):
    per = 64 // bits
    words = []
    for i in range(0, len(values), per):
        word = 0
        shift = 0
        for value in values[i:i + per]:
            word |= value << shift
            shift += bits
        words.append(word)
    return struct.pack('>%dQ' % len(words), *words)


def fix_section(section, stats):
    """Renames the old blocks in the palette of one section. Returns True if light may change."""
    entry = section.get(b'block_states')
    if entry is None or entry[0] != COMPOUND:
        return False
    states = entry[1]
    pal = states.get(b'palette')
    if pal is None or pal[0] != LIST or pal[1][0] != COMPOUND:
        return False
    entries = pal[1][1]
    old = {}
    for i, e in enumerate(entries):
        name = e.get(b'Name')
        if name is not None and name[0] == STRING and name[1] in OLD:
            old[i] = name[1]
    if not old:
        return False
    size = len(entries)
    bits = storage_bits(size)
    values = None
    if bits == 0:
        counts = {0: 4096}
    else:
        data = states.get(b'data')
        if data is not None and data[0] == LONG_ARRAY:
            values = unpack_longs(data[1], bits)
        if values is None or max(values) >= size:
            raise NbtError('block data of section %s does not fit its palette of %d'
                           % (signed(section.get(b'Y', (BYTE, b'\x00'))[1]), size))
        counts = {}
        for v in values:
            counts[v] = counts.get(v, 0) + 1
    light = False
    for i, oid in old.items():
        e = entries[i]
        e[b'Name'] = (STRING, OLD[oid])
        e.pop(b'Properties', None)
        n = counts.get(i, 0)
        stats.bump(stats.blocks, oid, n)
        if oid in LIGHT_CHANGES and n:
            light = True
    stats.dirty = True
    # A rename can leave the same plain block twice in the palette. Merge such entries and pack the
    # block data again with the bit width that the shorter palette needs.
    first = {}
    remap = []
    keep = []
    for e in entries:
        name = e.get(b'Name')
        key = None
        if b'Properties' not in e and name is not None and name[0] == STRING and name[1] in NEW_IDS:
            key = name[1]
        if key is not None and key in first:
            remap.append(first[key])
            continue
        if key is not None:
            first[key] = len(keep)
        remap.append(len(keep))
        keep.append(e)
    if len(keep) == size or values is None:
        return light
    new_values = [remap[v] for v in values]
    new_bits = storage_bits(len(keep))
    if new_bits == 0:
        states.pop(b'data', None)
    else:
        raw = pack_longs(new_values, new_bits)
        if unpack_longs(raw, new_bits) != new_values:
            raise NbtError('packed block data of a section does not read back')
        states[b'data'] = (LONG_ARRAY, raw)
    pal[1][1] = keep
    stats.merged += size - len(keep)
    return light


def patch_starts(structures, stats, label):
    """Adds the rule processor to every Builder Village piece of the structure starts in a chunk."""
    entry = structures.get(b'starts')
    if entry is None or entry[0] != COMPOUND:
        return
    for sid, (tag, start) in entry[1].items():
        if sid != VILLAGE or tag != COMPOUND:
            continue
        kids = start.get(b'Children')
        if kids is None or kids[0] != LIST or kids[1][0] != COMPOUND:
            continue
        for piece in kids[1][1]:
            element = piece.get(b'pool_element')
            if element is None or element[0] != COMPOUND:
                continue
            element = element[1]
            location = element.get(b'location')
            if location is None or location[1] != VILLAGE:
                continue
            procs = element.get(b'processors')
            lst = None
            if procs is not None and procs[0] == COMPOUND:
                inner = procs[1].get(b'processors')
                if inner is not None and inner[0] == LIST:
                    lst = inner[1]
            elif procs is not None and procs[0] == LIST:
                lst = procs[1]
            if lst is None or lst[0] not in (COMPOUND, END):
                stats.errors.append('%s: a village piece names its processors by id, not patched' % label)
                continue
            if _has_old(LIST, lst):
                stats.starts_done += 1
                continue
            if lst[0] == END:
                lst[0] = COMPOUND
            lst[1].append(rule_processor())
            stats.starts += 1
            stats.dirty = True


def fix_root(root, kind, stats, label):
    """Applies every replacement to one chunk or file. The kind is region, entities or nbt."""
    if kind != 'region':
        replace_strings(root, stats, '' if stats.verbose else None)
        return
    light = False
    sections = root.get(b'sections')
    if sections is not None and sections[0] == LIST and sections[1][0] == COMPOUND:
        for section in sections[1][1]:
            if fix_section(section, stats):
                light = True
    replace_strings(root, stats, '' if stats.verbose else None, skip=b'structures')
    structures = root.get(b'structures')
    if structures is not None and structures[0] == COMPOUND:
        patch_starts(structures[1], stats, label)
    if light and b'isLightOn' in root:
        # ChunkSerializer.write leaves the key out when light is not done. Starlight, which the
        # server runs, then drops the stored light of the chunk on load and lights it from scratch
        # (SaveUtil.loadLightHookReal). Vanilla keeps the stored light and runs
        # propagateLightSources for the chunk on top of it.
        del root[b'isLightOn']
        stats.relit += 1


def inflate(kind, payload):
    """Decompresses one stream. Returns the data and the number of bytes after the stream."""
    if kind == 3:
        return bytes(payload), 0
    if kind not in (1, 2):
        raise NbtError('unknown compression type %d' % kind)
    d = zlib.decompressobj(31 if kind == 1 else 15)
    out = d.decompress(payload)
    if not d.eof:
        raise NbtError('compressed stream is truncated')
    return out, len(d.unused_data)


def deflate(kind, raw):
    if kind == 3:
        return raw
    if kind == 2:
        return zlib.compress(raw, 6)
    c = zlib.compressobj(6, zlib.DEFLATED, 31)
    return c.compress(raw) + c.flush()


def fsync_dir(path):
    try:
        fd = os.open(path, os.O_RDONLY)
    except OSError:
        return
    try:
        os.fsync(fd)
    except OSError:
        pass
    finally:
        os.close(fd)


def tmp_name(path):
    return os.path.join(os.path.dirname(path), '.' + os.path.basename(path) + '.replace_blocks.tmp')


def write_tmp(path, data, like):
    """Writes data next to path with the mode and owner of the file like, and returns its name."""
    tmp = tmp_name(path)
    with open(tmp, 'wb') as f:
        f.write(data)
        f.flush()
        os.fsync(f.fileno())
    if like is not None:
        os.chmod(tmp, stat.S_IMODE(like.st_mode))
        if os.geteuid() == 0:
            os.chown(tmp, like.st_uid, like.st_gid)
    return tmp


def replace_file(path, data):
    like = os.stat(path)
    tmp = write_tmp(path, data, like)
    os.replace(tmp, path)
    fsync_dir(os.path.dirname(path))


def chunk_at(data, idx):
    """Returns (sector, sectors) of a chunk from a region header, or None if it has no data."""
    packed = int.from_bytes(data[idx * 4:idx * 4 + 4], 'big')
    if packed == 0:
        return None
    return packed >> 8, packed & 0xFF


def read_chunk(data, idx, folder, cx, cz, external_data=None):
    """Returns (compression, payload, external) of one chunk, as RegionFile reads it."""
    sector, count = chunk_at(data, idx)
    if sector < 2 or count == 0 or sector * 4096 > len(data):
        raise NbtError('invalid sector %d (%d sectors) in the header, the server drops it' % (sector, count))
    start = sector * 4096
    avail = min(count * 4096, len(data) - start)
    if avail < 5:
        raise NbtError('chunk header is truncated')
    length = int.from_bytes(data[start:start + 4], 'big', signed=True)
    kind = data[start + 4]
    if length == 0:
        raise NbtError('chunk is allocated but has no stream')
    if kind & 0x80:
        mcc = os.path.join(folder, 'c.%d.%d.mcc' % (cx, cz))
        if external_data is not None and mcc in external_data:
            payload = external_data[mcc]
        else:
            with open(mcc, 'rb') as f:
                payload = f.read()
        return kind & 0x7F, payload, True
    if length < 0 or length - 1 > avail - 5:
        raise NbtError('chunk stream is truncated')
    return kind, data[start + 5:start + 4 + length], False


def build_region(data, changes, folder, now):
    """Returns the new region bytes and the external chunk files to write and to delete."""
    new = bytearray(data)
    if len(new) % 4096:
        new.extend(bytes(4096 - len(new) % 4096))
    owner = {}
    for idx in range(1024):
        loc = chunk_at(data, idx)
        if loc is None:
            continue
        for sec in range(loc[0], loc[0] + loc[1]):
            owner.setdefault(sec, []).append(idx)
    writes = {}
    deletes = []
    for idx, kind, payload, external, cx, cz, _ in changes:
        sector, count = chunk_at(data, idx)
        mcc = os.path.join(folder, 'c.%d.%d.mcc' % (cx, cz))
        sectors = (len(payload) + 5 + 4095) // 4096
        if sectors >= 256:
            blob = (1).to_bytes(4, 'big') + bytes((kind | 0x80,))
            sectors = 1
            writes[mcc] = payload
        else:
            blob = (len(payload) + 1).to_bytes(4, 'big') + bytes((kind,)) + payload
            if external:
                deletes.append(mcc)
        blob += bytes(sectors * 4096 - len(blob))
        shared = any(owner.get(sec) != [idx] for sec in range(sector, sector + count))
        if sectors <= count and not shared:
            new[sector * 4096:(sector + count) * 4096] = blob + bytes((count - sectors) * 4096)
            at = sector
        else:
            at = len(new) // 4096
            new.extend(blob)
        new[idx * 4:idx * 4 + 4] = ((at << 8) | sectors).to_bytes(4, 'big')
        new[4096 + idx * 4:4096 + idx * 4 + 4] = now.to_bytes(4, 'big')
    return bytes(new), writes, deletes


def check_region(old, new, changes, folder, writes, rx, rz):
    """Reads the new region back: unchanged chunks byte for byte, changed ones by content."""
    want = dict((c[0], c) for c in changes)
    used = {}
    for idx in range(1024):
        cx, cz = rx * 32 + (idx & 31), rz * 32 + (idx >> 5)
        loc_old = chunk_at(old, idx)
        loc_new = chunk_at(new, idx)
        if idx not in want:
            if loc_old != loc_new:
                raise NbtError('unchanged chunk %d %d moved' % (cx, cz))
            if loc_old is None:
                continue
            a, b = loc_old[0] * 4096, (loc_old[0] + loc_old[1]) * 4096
            if old[a:b] != new[a:b][:len(old[a:b])]:
                raise NbtError('unchanged chunk %d %d differs' % (cx, cz))
        else:
            kind, payload, external = read_chunk(new, idx, folder, cx, cz, writes)
            if kind != want[idx][1] or payload != want[idx][2] or inflate(kind, payload)[0] != want[idx][6]:
                raise NbtError('changed chunk %d %d does not read back' % (cx, cz))
            if external and len(payload) + 5 < 256 * 4096:
                raise NbtError('chunk %d %d is external but small' % (cx, cz))
        if loc_new is not None:
            if loc_new[0] < 2 or loc_new[1] == 0 or (loc_new[0] + loc_new[1]) * 4096 > len(new):
                raise NbtError('chunk %d %d has an invalid sector' % (cx, cz))
            for sec in range(loc_new[0], loc_new[0] + loc_new[1]):
                if sec in used and (idx in want or used[sec] in want):
                    raise NbtError('chunk %d %d overlaps another chunk' % (cx, cz))
                used[sec] = idx
    if len(new) % 4096:
        raise NbtError('region file is not padded to a sector')


def process_region(path, rel, kind, args):
    fs = Stats(args.verbose)
    fs.files = 1
    m = REGION_NAME.match(os.path.basename(path))
    rx, rz = int(m.group(1)), int(m.group(2))
    folder = os.path.dirname(path)
    with open(path, 'rb') as f:
        data = f.read()
    fs.bytes = len(data)
    if not data:
        return fs
    if len(data) < 8192:
        fs.errors.append('%s: header is truncated, left as it is' % rel)
        return fs
    changes = []
    for idx in range(1024):
        if chunk_at(data, idx) is None:
            continue
        cx, cz = rx * 32 + (idx & 31), rz * 32 + (idx >> 5)
        label = '%s chunk %d %d' % (rel, cx, cz)
        try:
            comp, payload, external = read_chunk(data, idx, folder, cx, cz)
            raw, extra = inflate(comp, payload)
        except (NbtError, OSError, zlib.error) as e:
            fs.errors.append('%s: %s, left as it is' % (label, e))
            continue
        fs.chunks += 1
        if external:
            fs.bytes += len(payload)
        # A village start holds no old id, but it gets the rule processor.
        if not ((PREFIX in raw and OLD_RE.search(raw)) or (kind == 'region' and VILLAGE in raw)):
            continue
        try:
            if extra:
                raise NbtError('%d bytes after the compressed stream' % extra)
            name, root = nbt_load(raw)
            if nbt_dump(name, root) != raw:
                raise NbtError('NBT does not dump back to the same bytes')
            cs = Stats(args.verbose)
            fix_root(root, kind, cs, label)
        except (NbtError, RecursionError) as e:
            fs.errors.append('%s: %s, left as it is' % (label, e))
            continue
        if cs.dirty:
            cs.chunks_changed = 1
            new_raw = nbt_dump(name, root)
            changes.append((idx, comp, deflate(comp, new_raw), external, cx, cz, new_raw))
            if args.verbose:
                print('  %s: %s' % (label, cs.describe()))
                for note in cs.notes:
                    print(note)
            cs.notes = []
        fs.add(cs)
    if changes:
        fs.files_changed = 1
        if not args.dry_run:
            now = int(time.time())
            try:
                new, writes, deletes = build_region(data, changes, folder, now)
                check_region(data, new, changes, folder, writes, rx, rz)
            except NbtError as e:
                fs.errors.append('%s: %s, file left as it is' % (rel, e))
                fs.files_changed = 0
                return fs
            tmps = []
            for mcc, payload in writes.items():
                like = os.stat(path)
                tmps.append((write_tmp(mcc, payload, like), mcc))
            replace_file(path, new)
            for tmp, mcc in tmps:
                os.replace(tmp, mcc)
            for mcc in deletes:
                if mcc not in writes and os.path.exists(mcc):
                    os.remove(mcc)
            if tmps or deletes:
                fsync_dir(folder)
    return fs


def process_nbt_file(path, rel, args):
    fs = Stats(args.verbose)
    fs.files = 1
    with open(path, 'rb') as f:
        data = f.read()
    fs.bytes = len(data)
    if data[:2] == b'\x1f\x8b':
        comp = 1
    elif data[:1] == b'\x0a':
        comp = 3
    else:
        fs.unread.append('%s: neither gzip nor raw NBT' % rel)
        return fs
    try:
        raw, extra = inflate(comp, data)
    except (NbtError, zlib.error) as e:
        fs.unread.append('%s: does not inflate (%s)' % (rel, e))
        return fs
    if PREFIX not in raw or not OLD_RE.search(raw):
        return fs
    try:
        if extra:
            raise NbtError('%d bytes after the compressed stream' % extra)
        name, root = nbt_load(raw)
        if nbt_dump(name, root) != raw:
            raise NbtError('NBT does not dump back to the same bytes')
        fix_root(root, 'nbt', fs, rel)
    except (NbtError, RecursionError) as e:
        fs.errors.append('%s: %s, left as it is' % (rel, e))
        fs.dirty = False
        return fs
    if not fs.dirty:
        return fs
    fs.files_changed = 1
    if args.verbose:
        for note in fs.notes:
            print(note)
    fs.notes = []
    if not args.dry_run:
        new_raw = nbt_dump(name, root)
        new = deflate(comp, new_raw)
        if inflate(comp, new)[0] != new_raw:
            fs.errors.append('%s: new file does not read back, left as it is' % rel)
            fs.files_changed = 0
            return fs
        replace_file(path, new)
    return fs


def take_lock(world, dry_run):
    """Locks world/session.lock like the server does, or exits if a server holds it."""
    path = os.path.join(world, 'session.lock')
    if dry_run and not os.path.exists(path):
        return None
    try:
        fd = os.open(path, os.O_RDONLY if dry_run else (os.O_RDWR | os.O_CREAT), 0o644)
    except OSError as e:
        sys.exit('Cannot open %s: %s' % (path, e))
    try:
        # Java's FileChannel.tryLock is an fcntl lock over the whole file on Linux and macOS.
        fcntl.lockf(fd, (fcntl.LOCK_SH if dry_run else fcntl.LOCK_EX) | fcntl.LOCK_NB)
    except OSError as e:
        os.close(fd)
        if e.errno in (errno.EACCES, errno.EAGAIN):
            print('Refusing to run: a running server holds %s. Stop the server and wait until its java '
                  'process is gone.' % path, file=sys.stderr)
            sys.exit(2)
        raise
    return fd


def walk(world, other=None):
    """Yields (path, relative path, kind) for every file this script reads, sorted. The files it passes
    over go to the list other, if one is given, so the summary can count them."""
    for top, dirs, files in os.walk(world):
        rel_top = os.path.relpath(top, world)
        dirs[:] = sorted(d for d in dirs if not (d in SKIP_DIRS and (d != 'datapacks' or rel_top == '.')))
        base = os.path.basename(top)
        for name in sorted(files):
            path = os.path.join(top, name)
            rel = os.path.relpath(path, world)
            if os.path.islink(path) or name.startswith('.'):
                if other is not None:
                    other.append(rel)
                continue
            if base in ('region', 'entities') and REGION_NAME.match(name):
                yield path, rel, base
            elif name.endswith(NBT_SUFFIXES) and name not in SKIP_FILES:
                yield path, rel, 'nbt'
            elif other is not None:
                other.append(rel)


def main(argv=None):
    ap = argparse.ArgumentParser(
        description='Replace the five old blocks of Chocolate Quest with the normal blocks in a stopped world.')
    ap.add_argument('world', help='World folder, the one that holds level.dat')
    ap.add_argument('--dry-run', action='store_true', help='Read and count only, write nothing')
    ap.add_argument('--verbose', action='store_true', help='Also list every changed chunk and string')
    args = ap.parse_args(argv)
    world = os.path.abspath(args.world)
    if not os.path.isfile(os.path.join(world, 'level.dat')):
        print('Not a world folder, no level.dat in %s' % world, file=sys.stderr)
        return 2
    sys.setrecursionlimit(max(sys.getrecursionlimit(), 4 * MAX_DEPTH + 200))
    lock = take_lock(world, args.dry_run)
    t0 = time.time()
    total = Stats(args.verbose)
    kinds = {}
    other = []
    print('%s %s' % ('Dry run, nothing is written:' if args.dry_run else 'Rewriting', world))
    for path, rel, kind in walk(world, other):
        if kind == 'nbt':
            fs = process_nbt_file(path, rel, args)
        else:
            fs = process_region(path, rel, kind, args)
        kinds[kind] = kinds.get(kind, 0) + 1
        if fs.changes() or fs.starts_done or fs.keys or fs.inner:
            verb = 'would change' if args.dry_run else 'changed'
            if not fs.files_changed:
                verb = 'nothing to change'
            elif kind != 'nbt':
                verb += ' %d of %d chunks' % (fs.chunks_changed, fs.chunks)
            print('%s: %s  %s' % (rel, verb, fs.describe()))
        for err in fs.errors:
            print('  ERROR ' + err)
        total.add(fs)
    took = time.time() - t0
    print('')
    print('Summary%s' % (' of the dry run' if args.dry_run else ''))
    print('  Files read: %d (%s), %.1f MB in %.1f s' % (
        total.files, ', '.join('%s %d' % (k, kinds[k]) for k in sorted(kinds)), total.bytes / 1e6, took))
    print('  Chunks read: %d, %s: %d' % (total.chunks, 'to change' if args.dry_run else 'changed',
                                         total.chunks_changed))
    print('  Files %s: %d' % ('to change' if args.dry_run else 'changed', total.files_changed))
    for oid in sorted(OLD):
        print('  %s becomes %s: blocks %d, strings %d, item count %d' % (
            oid.decode(), OLD[oid].decode(), total.blocks.get(oid, 0), total.strings.get(oid, 0),
            total.items.get(oid, 0)))
    print('  Village starts patched: %d, already patched: %d' % (total.starts, total.starts_done))
    print('  Chunks to relight: %d, palette entries merged: %d' % (total.relit, total.merged))
    if total.keys or total.inner:
        print('  Not changed: old id as a key %d, inside a longer string %d' % (
            sum(total.keys.values()), sum(total.inner.values())))
    # Files with other names (level.dat, JSON, the folders in SKIP_DIRS are not walked at all) and NBT
    # files that do not read are not checked for old ids. --verbose lists them.
    print('  Files passed over: %d with other names, %d NBT files that do not read' % (
        len(other), len(total.unread)))
    if args.verbose:
        for rel in other:
            print('    passed over: %s' % rel)
    for note in total.unread:
        print('    does not read: %s' % note)
    print('  Errors: %d' % len(total.errors))
    if lock is not None:
        os.close(lock)
    return 1 if total.errors else 0


if __name__ == '__main__':
    sys.exit(main())
