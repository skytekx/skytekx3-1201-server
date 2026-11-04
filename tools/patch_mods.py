#!/usr/bin/env python3
"""Re-apply local boot-log fixes to Modrinth-resolved mod jars.

mods/ is assembled as in README step 4 (copy local-mods/*.jar, then a sha1-checked fetch of the
server-side files in modrinth.index.json). Re-fetching or replacing a jar overwrites these jar-level
fixes, so run this after every change to mods/. Idempotent: safe to run repeatedly.

Fixes third-party boot WARN/ERROR spam that can only be addressed inside the mod jar:
- mixin configs missing the required "minVersion" property (logged as ERROR by Mixin),
- orphan compat mixins that @Mixin-target classes from mods that are NOT installed (logged as
  "Error loading class" WARN),
- a stray 4-char-path loot table in ad_astra that crashes Bird's Nests' unguarded substring,
- a class file fix: a mixin whose target name is not the runtime name (TiCEX).

The Bird's Nests crash itself is fixed durably by a mixin in the skytekx worldgen mod (local-mods/),
not here. Bukkit/EssentialsX hybrid warning is intentionally left as-is.
"""
import zipfile, json, glob, io, os, re, struct

MODS = os.path.normpath(os.path.join(os.path.dirname(__file__), "..", "mods"))


def find_jar(pattern):
    m = sorted(glob.glob(os.path.join(MODS, pattern)))
    return m[0] if m else None


def _rewrite(jar, mutate):
    """Read jar entries, let mutate(filename, data)->(newdata|None drop|data) rebuild it. Returns changed."""
    if not jar or not os.path.exists(jar):
        print(f"  skip (jar missing): {pattern_of(jar)}")
        return False
    with zipfile.ZipFile(jar) as z:
        items = [(i, z.read(i.filename)) for i in z.infolist()]
    changed = False
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as z:
        for info, data in items:
            new = mutate(info.filename, data)
            if new is _DROP:
                changed = True
                continue
            if new is not None and new != data:
                data = new
                changed = True
            z.writestr(info, data)
    if changed:
        with open(jar, "wb") as f:
            f.write(buf.getvalue())
    print(f"  {'patched' if changed else 'noop'}: {os.path.basename(jar)}")
    return changed


_DROP = object()


def pattern_of(j):
    return j if j else "(none)"


def add_min_version(jar, inner):
    def mutate(name, data):
        if name != inner:
            return None
        obj = json.loads(data)
        if obj.get("minVersion") == "0.8":
            return None
        obj["minVersion"] = "0.8"
        return json.dumps(obj, indent=2).encode("utf-8")
    return _rewrite(jar, mutate)


def remove_mixins(jar, inner, entries):
    entries = set(entries)
    def mutate(name, data):
        if name != inner:
            return None
        obj = json.loads(data)
        touched = False
        for key in ("mixins", "client", "server"):
            arr = obj.get(key)
            if isinstance(arr, list):
                kept = [m for m in arr if m not in entries]
                if len(kept) != len(arr):
                    obj[key] = kept
                    touched = True
        return json.dumps(obj, indent=2).encode("utf-8") if touched else None
    return _rewrite(jar, mutate)


def delete_entry(jar, inner):
    def mutate(name, data):
        return _DROP if name == inner else None
    return _rewrite(jar, mutate)


def edit_json(jar, inner, transform):
    """Apply transform(obj)->obj to a json entry inside the jar."""
    def mutate(name, data):
        if name != inner:
            return None
        out = transform(json.loads(data))
        nd = json.dumps(out, indent=2).encode("utf-8")
        return nd if nd != data else None
    return _rewrite(jar, mutate)


def make_tag_optional(jar, inner):
    """Rewrite a tag inside the jar so every value is required:false (skips missing references)."""
    def t(obj):
        return {"replace": True, "values": [
            {"id": (v.get("id") if isinstance(v, dict) else v), "required": False}
            for v in obj.get("values", [])]}
    return edit_json(jar, inner, t)


# ticex catalyst stats whose target mod is absent or does not satisfy the integration
_TICEX_DROP_CATALYSTS = {
    "ticex:catalyst_gem_boots", "ticex:catalyst_gem_chestplate", "ticex:catalyst_gem_helmet",
    "ticex:catalyst_gem_leggings", "ticex:catalyst_kinetic_gun", "ticex:catalyst_meka_bow",
    "ticex:catalyst_meka_tana",
}


def _trim_ticex_recon(obj):
    obj["stats"] = {k: v for k, v in obj.get("stats", {}).items() if k not in _TICEX_DROP_CATALYSTS}
    return obj


def _drop_catalyst_part(obj):
    """Remove ticex:catalyst_* parts (absent integrations) from a tool definition so it still loads."""
    for m in obj.get("modules", []):
        if m.get("type") == "tconstruct:part_stats" and isinstance(m.get("parts"), list):
            m["parts"] = [p for p in m["parts"]
                          if not (isinstance(p, str) and p.startswith("ticex:catalyst_"))]
    return obj


def _drop_catalyst_slot(obj):
    """Remove tinker-station input slots that filter for an absent ticex:catalyst_* item."""
    if isinstance(obj.get("input_slots"), list):
        obj["input_slots"] = [s for s in obj["input_slots"]
                              if not str((s.get("filter") or {}).get("item", "")).startswith("ticex:catalyst_")]
    return obj


def _mixin_to_client(entry):
    """Move a client-only mixin from the common 'mixins' list to 'client' (it errors on a server otherwise)."""
    def t(obj):
        if entry in obj.get("mixins", []):
            obj["mixins"] = [m for m in obj["mixins"] if m != entry]
            obj.setdefault("client", []).append(entry)
        return obj
    return t


def patch_nested_min_version(outer_jar, inner_jar_substr, inner_mixins):
    """Add minVersion to a mixins.json inside a jar-in-jar (e.g. terraform-wood inside traverse-forge)."""
    if not outer_jar or not os.path.exists(outer_jar):
        print("  skip (jar missing): nested")
        return False
    def mutate(name, data):
        if inner_jar_substr not in name or not name.endswith(".jar"):
            return None
        nbuf = io.BytesIO()
        nchanged = False
        with zipfile.ZipFile(io.BytesIO(data)) as nz:
            nitems = [(i, nz.read(i.filename)) for i in nz.infolist()]
        with zipfile.ZipFile(nbuf, "w", zipfile.ZIP_DEFLATED) as nz:
            for ninfo, ndata in nitems:
                if ninfo.filename == inner_mixins:
                    obj = json.loads(ndata)
                    if obj.get("minVersion") != "0.8":
                        obj["minVersion"] = "0.8"
                        ndata = json.dumps(obj, indent=2).encode("utf-8")
                        nchanged = True
                nz.writestr(ninfo, ndata)
        return nbuf.getvalue() if nchanged else None
    return _rewrite(outer_jar, mutate)


_OPLEN = {op: 2 for op in (0x10, 0x12, *range(0x15, 0x1a), *range(0x36, 0x3b), 0xa9, 0xbc)}
_OPLEN.update({op: 3 for op in (0x11, 0x13, 0x14, 0x84, *range(0x99, 0xa9), *range(0xb2, 0xb9),
                                 0xbb, 0xbd, 0xc0, 0xc1, 0xc6, 0xc7)})
_OPLEN.update({0xc5: 4, 0xb9: 5, 0xba: 5, 0xc8: 5, 0xc9: 5})


def _insn_len(code, pc):
    """Length of the JVM instruction at pc, including the switch padding and the wide forms."""
    op = code[pc]
    if op in (0xaa, 0xab):
        p = pc + 1 + (-(pc + 1) % 4)
        if op == 0xaa:
            low, high = struct.unpack(">ii", code[p + 4:p + 12])
            return p + 12 + 4 * (high - low + 1) - pc
        return p + 8 + 8 * struct.unpack(">i", code[p + 4:p + 8])[0] - pc
    if op == 0xc4:
        return 6 if code[pc + 1] == 0x84 else 4
    return _OPLEN.get(op, 1)


def _insns(code):
    """Yield (pc, length) of every instruction of a method's bytecode."""
    pc = 0
    while pc < len(code):
        n = _insn_len(code, pc)
        yield pc, n
        pc += n


def _u2(b, p):
    return struct.unpack(">H", b[p:p + 2])[0]


def _p2(i):
    return struct.pack(">H", i)


def _find_insns(code, pattern):
    """Offsets of the instructions at which pattern (bytes, or a compiled bytes regex) matches."""
    rx = pattern if hasattr(pattern, "match") else re.compile(re.escape(pattern), re.S)
    return [pc for pc, _ in _insns(code) if rx.match(code, pc)]


def _constant_pool(data, spans=None):
    """Parse the constant pool of a class file. Returns the entries and the offset after them.
    spans, when given, gets index -> (start, end) of the bytes of every entry."""
    count = struct.unpack(">H", data[8:10])[0]
    cp, p, i = [None] * count, 10, 1
    while i < count:
        tag, start = data[p], p
        if tag == 1:
            n = struct.unpack(">H", data[p + 1:p + 3])[0]
            cp[i], p = (1, data[p + 3:p + 3 + n].decode("utf-8", "replace")), p + 3 + n
        elif tag in (3, 4):
            cp[i], p = (tag,), p + 5
        elif tag in (5, 6):
            cp[i], p = (tag,), p + 9
        elif tag in (7, 8, 16, 19, 20):
            cp[i], p = (tag, struct.unpack(">H", data[p + 1:p + 3])[0]), p + 3
        elif tag in (9, 10, 11, 12, 17, 18):
            cp[i], p = (tag, *struct.unpack(">HH", data[p + 1:p + 5])), p + 5
        elif tag == 15:
            cp[i], p = (tag,), p + 4
        else:
            raise ValueError(f"unknown constant pool tag {tag}")
        if spans is not None:
            spans[i] = (start, p)
        i += 2 if tag in (5, 6) else 1
    return cp, p


def _code_ranges(data, p, cp):
    """Yield (start, length) of the bytecode of every method, starting after the constant pool."""
    p += 6
    p += 2 + 2 * struct.unpack(">H", data[p:p + 2])[0]
    for members in ("fields", "methods"):
        n, p = struct.unpack(">H", data[p:p + 2])[0], p + 2
        for _ in range(n):
            attrs, p = struct.unpack(">H", data[p + 6:p + 8])[0], p + 8
            for _ in range(attrs):
                name, size = struct.unpack(">HI", data[p:p + 6])
                if members == "methods" and cp[name][1] == "Code":
                    yield p + 14, struct.unpack(">I", data[p + 10:p + 14])[0]
                p += 6 + size


class _ClassFile:
    """Just enough of a class file to add constants and methods and to edit the code of a method.

    New constants go to the end of the pool, so every index the class uses keeps its meaning. A field
    or method is [access flags, name index, descriptor index, [[attribute name index, bytes], ...]]."""

    def __init__(self, data):
        spans = {}
        self.cp, end = _constant_pool(data, spans)
        self.raw = {i: bytes(data[s:e]) for i, (s, e) in spans.items()}
        self.head = bytes(data[:8])
        n = _u2(data, end + 6)
        self.body, p = bytes(data[end:end + 8 + 2 * n]), end + 8 + 2 * n
        self.fields, p = self._members(data, p)
        self.methods, p = self._members(data, p)
        self.attrs = bytes(data[p:])

    @staticmethod
    def _members(data, p):
        out, n, p = [], _u2(data, p), p + 2
        for _ in range(n):
            flags, name, desc, count = struct.unpack(">HHHH", data[p:p + 8])
            p, attrs = p + 8, []
            for _ in range(count):
                an, size = struct.unpack(">HI", data[p:p + 6])
                attrs.append([an, bytes(data[p + 6:p + 6 + size])])
                p += 6 + size
            out.append([flags, name, desc, attrs])
        return out, p

    def dump(self):
        out = bytearray(self.head) + _p2(len(self.cp))
        for i in sorted(self.raw):
            out += self.raw[i]
        out += self.body
        for members in (self.fields, self.methods):
            out += _p2(len(members))
            for flags, name, desc, attrs in members:
                out += struct.pack(">HHHH", flags, name, desc, len(attrs))
                for an, body in attrs:
                    out += struct.pack(">HI", an, len(body)) + body
        return bytes(out + self.attrs)

    def text(self, i):
        return self.cp[i][1]

    def this_name(self):
        return self.text(self.cp[_u2(self.body, 2)][1])

    def _find(self, match):
        hits = [i for i, e in enumerate(self.cp) if e is not None and match(e)]
        return hits[0] if hits else None

    def _add(self, entry, raw):
        self.cp.append(entry)
        self.raw[len(self.cp) - 1] = raw
        return len(self.cp) - 1

    def utf8(self, s):
        i = self._find(lambda e: e[0] == 1 and e[1] == s)
        if i is None:
            b = s.encode("utf-8")
            i = self._add((1, s), b"\x01" + _p2(len(b)) + b)
        return i

    def set_utf8(self, i, s):
        b = s.encode("utf-8")
        self.cp[i], self.raw[i] = (1, s), b"\x01" + _p2(len(b)) + b

    def find_string(self, s):
        return self._find(lambda e: e[0] == 8 and self.text(e[1]) == s)

    def find_class(self, name):
        return self._find(lambda e: e[0] == 7 and self.text(e[1]) == name)

    def klass(self, name):
        i = self.find_class(name)
        if i is None:
            u = self.utf8(name)
            i = self._add((7, u), b"\x07" + _p2(u))
        return i

    def find_ref(self, tag, owner, name, desc):
        """Index of a Fieldref (9), Methodref (10) or InterfaceMethodref (11), or None."""
        return self._find(lambda e: e[0] == tag and self.text(self.cp[e[1]][1]) == owner
                          and self.text(self.cp[e[2]][1]) == name and self.text(self.cp[e[2]][2]) == desc)

    def ref(self, tag, owner, name, desc):
        i = self.find_ref(tag, owner, name, desc)
        if i is None:
            nat = self._find(lambda e: e[0] == 12 and self.text(e[1]) == name and self.text(e[2]) == desc)
            if nat is None:
                n, d = self.utf8(name), self.utf8(desc)
                nat = self._add((12, n, d), b"\x0c" + _p2(n) + _p2(d))
            c = self.klass(owner)
            i = self._add((tag, c, nat), bytes([tag]) + _p2(c) + _p2(nat))
        return i

    def method(self, name, desc):
        hits = [m for m in self.methods if self.text(m[1]) == name and self.text(m[2]) == desc]
        return hits[0] if len(hits) == 1 else None

    def _code_attr(self, m):
        return next(a for a in m[3] if self.text(a[0]) == "Code")

    def code(self, m):
        body = self._code_attr(m)[1]
        return bytearray(body[8:8 + struct.unpack(">I", body[4:8])[0]])

    def put_code(self, m, code):
        """Write back bytecode of the same length, so branch offsets and stack map frames stay valid."""
        a = self._code_attr(m)
        n = struct.unpack(">I", a[1][4:8])[0]
        if len(code) != n:
            raise ValueError("put_code needs code of the same length")
        a[1] = a[1][:8] + bytes(code) + a[1][8 + n:]

    def _new_code(self, max_stack, max_locals, code):
        # No exception table and no attributes. Code without branches needs no StackMapTable.
        return [self.utf8("Code"), struct.pack(">HHI", max_stack, max_locals, len(code)) + bytes(code) + bytes(4)]

    def set_code(self, m, max_stack, max_locals, code):
        """Replace the Code attribute of a method with straight-line code (no branches)."""
        m[3] = [self._new_code(max_stack, max_locals, code) if self.text(a[0]) == "Code" else a for a in m[3]]

    def add_method(self, flags, name, desc, max_stack, max_locals, code):
        self.methods.append([flags, self.utf8(name), self.utf8(desc), [self._new_code(max_stack, max_locals, code)]])


def edit_classes(jar, transforms):
    """Apply {class entry: transform(_ClassFile)} to the classes of one jar in one rewrite.

    A transform returns True when it changed the class, False when the class is patched already, or
    a reason (str) to leave the class as it is, which is printed."""
    seen = set()
    def mutate(name, data):
        t = transforms.get(name)
        if t is None:
            return None
        seen.add(name)
        cf = _ClassFile(data)
        r = t(cf)
        if isinstance(r, str):
            print(f"  skip {name}: {r}")
            return None
        return cf.dump() if r else None
    changed = _rewrite(jar, mutate)
    for name in sorted(set(transforms) - seen):
        if jar:
            print(f"  skip {name}: not in the jar")
    return changed


def swap_field_for_getter(jar, inner, owner, field, getter, desc, expect):
    """Turn every `getfield owner.field` of one class into `invokevirtual owner.getter desc`.

    Both instructions are three bytes long and leave the same type on the stack, so the class keeps
    its size, its stack map frames and its max stack. The getter must already be in the constant pool.
    Patches only when exactly `expect` reads are found, so a changed mod version is left alone."""
    def mutate(name, data):
        if name != inner:
            return None
        cp, end = _constant_pool(data)

        def ref(i):
            _, cls, nat = cp[i]
            return cp[cp[cls][1]][1], cp[cp[nat][1]][1], cp[cp[nat][2]][1]
        fields = [i for i, e in enumerate(cp) if e and e[0] == 9 and ref(i)[:2] == (owner, field)]
        methods = [i for i, e in enumerate(cp) if e and e[0] == 10 and ref(i) == (owner, getter, desc)]
        if len(fields) != 1 or len(methods) != 1:
            print(f"  skip {inner}: {field} or {getter} not in the constant pool")
            return None
        hits = []
        for start, size in _code_ranges(data, end, cp):
            pc = 0
            while pc < size:
                at = start + pc
                if data[at] == 0xb4 and struct.unpack(">H", data[at + 1:at + 3])[0] == fields[0]:
                    hits.append(at)
                pc += _insn_len(data[start:start + size], pc)
        if not hits:
            return None
        if len(hits) != expect:
            print(f"  skip {inner}: {len(hits)} reads of {field}, expected {expect}")
            return None
        out = bytearray(data)
        for at in hits:
            out[at:at + 3] = bytes([0xb6]) + struct.pack(">H", methods[0])
        return bytes(out)
    return _rewrite(jar, mutate)


def rename_method(jar, inner, old, new, desc):
    """Rename the method `old` (descriptor desc) of one class by rewriting its name constant.

    Written for a mixin with remap = false whose @Inject names its target by the name in the sources.
    javac keeps the handler name and the target in the annotation in one Utf8 constant, so the rewrite
    renames the handler and points the injection at the runtime name. Mixin renames injector handlers
    when it merges them, so the new handler name cannot clash. Patches only when no other constant
    and no other member uses that Utf8 constant."""
    def t(cf):
        hits = [i for i, e in enumerate(cf.cp) if e and e[0] == 1 and e[1] == old]
        if not hits:
            return False if cf.method(new, desc) else f"no constant {old} and no method {new}"
        if len(hits) != 1:
            return f"{len(hits)} constants {old}"
        i = hits[0]
        users = [e for e in cf.cp if e and ((e[0] in (7, 8, 16, 19, 20) and e[1] == i) or (e[0] == 12 and i in e[1:]))]
        named = [m for m in cf.fields + cf.methods if m[1] == i]
        if users or len(named) != 1 or cf.text(named[0][2]) != desc or cf.method(new, desc):
            return f"{old} is used by another constant or member, or has another descriptor"
        cf.set_utf8(i, new)
        return True
    return edit_classes(jar, {inner: t})


def main():
    print("patch_mods: applying jar-level boot fixes")
    add_min_version(find_jar("doomangelring*.jar"), "doomangelring.forge.mixins.json")
    add_min_version(find_jar("tf_dnv-*.jar"), "mixins.tf_dnv.json")
    remove_mixins(find_jar("aether-redux*.jar"), "aether_redux.mixins.json",
                  ["common.block.GreenAercloudBlockMixin", "common.block.OrangeTreeMixin",
                   "common.block.PurpleAercloudBlockMixin"])
    remove_mixins(find_jar("mekanism_extras*.jar"), "mekanism_extras.mixins.json",
                  ["integration.mekaf.MixinAdvancedFactory", "integration.mekmm.MixinMoreMachineFactory"])
    remove_mixins(find_jar("deep_aether*.jar"), "deep_aether.mixins.json", ["AerwhaleKingMixin"])
    remove_mixins(find_jar("Quark*.jar"), "quark_integrations.mixins.json", ["lootr.ConfigManagerMixin"])
    delete_entry(find_jar("ad_astra*.jar"), "data/minecraft/loot_tables/loot.json")
    patch_nested_min_version(find_jar("traverse-forge*.jar"), "terraform-wood-api", "mixins.terraform-wood.json")
    # ticex: drop tool definitions + station layouts for tools whose target mod is NOT installed or
    # does not provide the integration. SlashBlade IS installed (reforged_slashblade works). TaCz
    # (blitz_gun) is intentionally absent, and Re:Avaritia does not satisfy ticex's "gem" tools, so
    # those are dropped.
    tj = find_jar("*[Tt]icex*.jar")
    for tool in ("blitz_gun", "singular_gem_boots", "singular_gem_chestplate",
                 "singular_gem_helmet", "singular_gem_leggings"):
        edit_json(tj, f"data/ticex/tinkering/tool_definitions/{tool}.json", _drop_catalyst_part)
        edit_json(tj, f"data/ticex/tinkering/station_layouts/{tool}.json", _drop_catalyst_slot)
    # trim the reconstruction material to only catalyst stats whose mod is actually present
    edit_json(tj, "data/ticex/tinkering/materials/stats/reconstruction.json", _trim_ticex_recon)
    # SummonedSwordMixin injects into "onHitEntity" with remap = false, but SlashBlade's
    # EntityAbstractSummonedSword has the runtime name m_5790_, so every boot logged "Mixin apply failed"
    # and the PROJECTILE_HIT modifiers of a Tinkers tool never fired for its summoned swords.
    rename_method(tj, "moffy/ticex/mixin/slashblade/SummonedSwordMixin.class", "onHitEntity", "m_5790_",
                  "(Lnet/minecraft/world/phys/EntityHitResult;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
    # cofhcore's MultiPlayerGameMode mixin is client-only but listed as common -> errors on a server
    edit_json(find_jar("cofh_core*.jar"), "mixins.cofhcore.json", _mixin_to_client("MultiPlayerGameModeMixin"))

    # Medieval Embroidery ships entity tags referencing an unregistered griffin entity; make optional
    mej = find_jar("Medieval_Embroidery*.jar")
    for tag in ("forge/tags/entity_types/large_predators.json",
                "forge/tags/entity_types/medium_predators.json",
                "forge/tags/entity_types/small_predators.json",
                "forge/tags/entity_types/predators.json",
                "minecraft/tags/entity_types/powder_snow_walkable_mobs.json"):
        make_tag_optional(mej, "data/" + tag)

    # Asgard Shield Reloaded 0.2 reads Player.swingingArm (f_20912_) in its attack handlers. The field
    # stays null until the player swings after a login or a respawn, so a hit on such a player threw
    # "Invalid hand null" and was lost (2026-10-31 22:37, all PvP hits failed). The server copy reads
    # getUsedItemHand() (m_7655_) instead, which is never null and is the hand a blocking shield is in.
    swap_field_for_getter(find_jar("AsgardShieldReloaded*.jar"), "me/mangregory/asr/util/handlers/EventHandler.class",
                          "net/minecraft/world/entity/player/Player", "f_20912_", "m_7655_",
                          "()Lnet/minecraft/world/InteractionHand;", expect=2)
    print("patch_mods: done")


if __name__ == "__main__":
    main()
