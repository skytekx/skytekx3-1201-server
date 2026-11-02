#!/usr/bin/env python3
"""Re-apply local boot-log fixes to Modrinth-resolved mod jars.

mods/ is assembled as in README step 4 (copy local-mods/*.jar, then a sha1-checked fetch of the
server-side files in modrinth.index.json). Re-fetching or replacing a jar overwrites these jar-level
fixes, so run this after every change to mods/. Idempotent: safe to run repeatedly.

Fixes third-party boot WARN/ERROR spam that can only be addressed inside the mod jar:
- mixin configs missing the required "minVersion" property (logged as ERROR by Mixin),
- orphan compat mixins that @Mixin-target classes from mods that are NOT installed (logged as
  "Error loading class" WARN),
- a stray 4-char-path loot table in ad_astra that crashes Bird's Nests' unguarded substring.

The Bird's Nests crash itself is fixed durably by a mixin in the skytekx worldgen mod (local-mods/),
not here. Bukkit/EssentialsX hybrid warning is intentionally left as-is.
"""
import zipfile, json, glob, io, os, struct

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


def _constant_pool(data):
    """Parse the constant pool of a class file. Returns the entries and the offset after them."""
    count = struct.unpack(">H", data[8:10])[0]
    cp, p, i = [None] * count, 10, 1
    while i < count:
        tag = data[p]
        if tag == 1:
            n = struct.unpack(">H", data[p + 1:p + 3])[0]
            cp[i], p = (1, data[p + 3:p + 3 + n].decode("utf-8", "replace")), p + 3 + n
        elif tag in (3, 4):
            cp[i], p = (tag,), p + 5
        elif tag in (5, 6):
            cp[i], p, i = (tag,), p + 9, i + 1
        elif tag in (7, 8, 16, 19, 20):
            cp[i], p = (tag, struct.unpack(">H", data[p + 1:p + 3])[0]), p + 3
        elif tag in (9, 10, 11, 12, 17, 18):
            cp[i], p = (tag, *struct.unpack(">HH", data[p + 1:p + 5])), p + 5
        elif tag == 15:
            cp[i], p = (tag,), p + 4
        else:
            raise ValueError(f"unknown constant pool tag {tag}")
        i += 1
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
