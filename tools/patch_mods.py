#!/usr/bin/env python3
"""Re-apply local boot-log fixes to Modrinth-resolved mod jars.

mods/ is rebuilt from modrinth.index.json by tools/resolve_all.py, which overwrites these jar-level
fixes. Run this AFTER resolve_all so the fixes persist. Idempotent: safe to run repeatedly.

Fixes third-party boot WARN/ERROR spam that can only be addressed inside the mod jar:
- mixin configs missing the required "minVersion" property (logged as ERROR by Mixin),
- orphan compat mixins that @Mixin-target classes from mods that are NOT installed (logged as
  "Error loading class" WARN),
- a stray 4-char-path loot table in ad_astra that crashes Bird's Nests' unguarded substring.

The Bird's Nests crash itself is fixed durably by a mixin in the skytekx worldgen mod (local-mods/),
not here. Bukkit/EssentialsX hybrid warning is intentionally left as-is.
"""
import zipfile, json, glob, io, os

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


def main():
    print("patch_mods: applying jar-level boot fixes")
    add_min_version(find_jar("doomangelring*.jar"), "doomangelring.forge.mixins.json")
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
        delete_entry(tj, f"data/ticex/tinkering/tool_definitions/{tool}.json")
        delete_entry(tj, f"data/ticex/tinkering/station_layouts/{tool}.json")
    # trim the reconstruction material to only catalyst stats whose mod is actually present
    edit_json(tj, "data/ticex/tinkering/materials/stats/reconstruction.json", _trim_ticex_recon)

    # Medieval Embroidery ships entity tags referencing an unregistered griffin entity; make optional
    mej = find_jar("Medieval_Embroidery*.jar")
    for tag in ("forge/tags/entity_types/large_predators.json",
                "forge/tags/entity_types/medium_predators.json",
                "forge/tags/entity_types/small_predators.json",
                "forge/tags/entity_types/predators.json",
                "minecraft/tags/entity_types/powder_snow_walkable_mobs.json"):
        make_tag_optional(mej, "data/" + tag)
    print("patch_mods: done")


if __name__ == "__main__":
    main()
