# Ender IO: vendored upstream source

`ports/enderio/` is the source of the SkyTekx build of Ender IO for Minecraft 1.20.1 / Forge 47.
It started as a plain snapshot of upstream's `1.20.1` branch. Later backports from the 1.21.1 line
are commits on top of that snapshot in this repository, see `okf/modpack/` for the plan.

## Upstream

| | |
|---|---|
| Repository | https://github.com/Team-EnderIO/EnderIO |
| Ref | branch `1.20.1` |
| Commit | `18ee1b17bf6879298e911fff751204b07f2e4228` |
| Commit date | 2026-03-16 22:25:44 UTC (Reece Mackie, "chore: Try to change how we break the block when wrenched to fix an obscure issue") |
| Last release on the ref | `v6.2.18-beta` = `907a9c3721730957fad6794ceed9ed0f591dabac` (2026-03-08) |
| Unreleased commits in the snapshot | `f9e1806c` "fix: Never trust inventory NBT Size key." (2026-03-16), `18ee1b17` (wrench block break) |
| Port source for later backports | branch `1.21.1` (Ender IO 8.2.x, NeoForge 1.21.1); merge-base with `1.20.1` is `41ed3d5e869f888c588d2460ebf9fcf8540fd1ab` |
| Reference only | branch `experiment/1.20.1-backport` (8 commits by Reece Mackie, 2026-03, on `1.21.1@010b057e`); read for API translations, its 8.x core is not used |

The snapshot was made with

```bash
git -C <clone> archive 18ee1b17bf6879298e911fff751204b07f2e4228 | tar -x -C ports/enderio
```

so it has no nested `.git` and keeps every upstream file, including `LICENSE.txt`, `doc/license/`
and the committed datagen output in `src/generated/resources/`. Upstream's `.gitattributes`
(`* text=auto`) applies inside this directory, so git stores `build.gradle`, `.gitattributes` and
`.gitignore`, which upstream committed with CRLF line endings, with LF. No other file changed.

## License summary

- Ender IO code and assets: **The Unlicense** (`LICENSE.txt`). Upstream documents third-party grants
  in `doc/license/`: NeepNoop's dust textures and DTFuel's alloy textures are CC0, and the
  TravelAnchors contributors allowed their code to be copied and relicensed.
- Code from other projects inside Ender IO's own classes:
  - **Patchouli** (Vazkii), **CC BY-NC-SA 3.0 Unported**: `GhostBuffers`, `GhostRenderLayer` and
    `initBuffers` in `src/machines/.../client/gui/widget/ioconfig/IOConfigWidget.java`, and the block
    rendering in `src/main/.../integrations/jei/category/FireCraftingCategory.java`. Because of these
    parts the built jar may only be distributed **non-commercially**, and changes to those parts
    are shared alike.
  - **Commoble** (Joseph Bettendorff), MIT: `src/machines/.../souldata/SoulDataReloadListener.java`.
  - **Mekanism** (Aidan C. Brady), MIT: `src/machines/.../io/fluid/IFluidItemInteractive.java` and the
    temp-file save in `src/conduits/.../network/ConduitSavedData.java`.
- Libraries the build packs into `META-INF/jarjar/`: Registrate (MPL-2.0), GraphLib3 (MIT,
  gigaherz / David Quintana), and GraphLib3's dependencies Guava, failureaccess, listenablefuture,
  Error Prone annotations, JSR 305 (Apache-2.0) and checker-qual (MIT).
