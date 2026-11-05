# AGENTS.md

## Constraints

- Keep the canonical identity stable: mod ID `mekanism_extra_modules`, Java package `com.github.mekanismextramodules`, and repository `Lumysia/mekanism-extra-modules`.
- Use Mekanism's native module, energy, configuration UI, and HUD APIs. Do not add an independent FE capability fallback or custom network/HUD layer without an explicit requirement.
- Configuration energy values are FE. Convert them through `IEnergyConversionHelper.INSTANCE.feConversion()` before calling Mekanism APIs, which consume Joules.
- Phase Guard must block damage before NeoForge creates its `DamageContainer`; do not cancel `LivingIncomingDamageEvent`, which leaks containers in the pinned NeoForge version.
- Chaos Anchor is always subordinate to an enabled and powered Phase Guard. It never runs independently.
- Draconic Evolution is optional. Keep its mixin gated by mod presence so the mod loads without Draconic Evolution installed.
- Treat registry IDs, translation keys, and shipped config keys as persistent interfaces; change them only with an explicit migration decision.
- Keep every locale on the same key set. Do not put configurable numeric values in static module descriptions.
- Keep `README.md` and public docs useful to players or contributors. Put maintainer and agent procedures in `AGENTS.md`, not public guides.
- Add a new file or public section only when a stated requirement or correctness needs it; related or potentially useful work is not enough.

## Project Decisions

- Develop on `main`; create a version branch only when maintaining another Minecraft line.
- For nontrivial commits, use a concise Conventional Commit-style title plus a short body explaining why and the important behavior change. Do not use title-only commits for substantive work.
- Release versions progress through `0.1.0-alpha.N`, `0.1.0-beta.N`, then `1.0.0`. Tags must be annotated and exactly match `v${minecraft_version}-${mod_version}`.
