# SkyTekx DE Fixes (Forge 1.20.1)

SkyTekx DE Fixes is an addon for Draconic Evolution 3.1.2.621 on Forge 1.20.1. It applies bug fixes that
upstream only made on its 1.21 branches. Draconic Evolution, Brandon's Core and CodeChicken Lib stay the
unmodified jars that the pack downloads from Modrinth. This mod changes them at runtime with Mixin and
adds one language entry of its own. The mod id is `skytekx_defixes`. Our code is MIT licensed, a few
portions that reproduce upstream statements stay under the license of Draconic Evolution and are listed
in [LICENSE](LICENSE), and the file [NOTICE](NOTICE) holds the credits, the license terms and the count
of those portions.

The pack does not backport Draconic Evolution 1.21 itself. The 1.21.1 line has the same 131
registrations, items, modules, reactor and fusion logic as 1.20.1, so a port would move about 20,000
lines of API churn for no content. It would also break the Draconic Evolution module of TiCEX. And the
"Don't Be a Jerk" license of Draconic Evolution forbids redistributing a modified jar without the
written permission of the copyright holder. The license does allow copying portions into other projects
and writing addons that use Draconic Evolution as a dependency, as long as their source is public, which
this directory is.

## Fixes

The upstream commits are on [Draconic-Inc/Draconic-Evolution](https://github.com/Draconic-Inc/Draconic-Evolution),
branch `1.21`, and the last one on [Draconic-Inc/BrandonsCore](https://github.com/Draconic-Inc/BrandonsCore).
The 1.20 branch ends at 4e7fc310 (2025-07-21) and has none of them.

| Number | Upstream | Bug on 1.20.1 | Fix here |
|---|---|---|---|
| 1 | Commit [e405445b](https://github.com/Draconic-Inc/Draconic-Evolution/commit/e405445ba6dc7355d99e536188479c0e3a2acca0) | The 3x3 grid of a Draconium Chest lives on the chest, but every open menu has its own result and computes it again only when the grid changes through that menu. With two players in the same chest, the second can take a result whose ingredients the first already used, which is a dupe. | Our own mechanism, not upstream's. Upstream moves the result into the chest. Here the mixin `DraconiumChestMenuMixin` keeps a copy of the grid each time the menu computes its result (`slotChangedCraftingGrid`), and `DETileMenuMixin` hooks `clicked`, which every kind of click passes. Before the server handles a click in the chest menu, the menu computes its result again if the grid differs from that copy, so a click can only take what the current grid makes. Nothing is saved, so chests and their items keep the stock format. |
| 2 | Commit [4683a54f](https://github.com/Draconic-Inc/Draconic-Evolution/commit/4683a54f7c5a2dd1df501bae6a78a994e7f2857b) | Insta kill damage of `Float.MAX_VALUE` (a Chaos Crystal placed outside creative) or `MAX_VALUE / 5` (Mob Grinder, Chaos Guardian laser, /kill through modular armor) overflows to infinity under any damage multiplier, Apothic Attributes among them, and can leave the target half dead. | A `@ModifyConstant` sets `MAX_VALUE / 100` in `ChaosCrystal.setPlacedBy`, `TileGrinder.attackTarget`, `LaserBeamPhase.serverTick` and `ModularArmorEventHandler.onEntityAttacked`. The `== Float.MAX_VALUE` check for /kill stays. |
| 3 | Commit [8ec180ab](https://github.com/Draconic-Inc/Draconic-Evolution/commit/8ec180abc13d87fbf51a579ec6c5d8b63da71dc0) | The method `ChaosCrystal.getShape` throws a NullPointerException for a crystal part without a parent position. | A `@Redirect` of the `BlockPos.subtract` call gives a zero offset when there is no parent, which is the unmoved shape that upstream returns. |
| 4 | Commit [0c85f0d9](https://github.com/Draconic-Inc/Draconic-Evolution/commit/0c85f0d9da50d0f3bd11295230661b8e28a9eca7) | The Chaotic Shield Capacity module takes a Chaotic Core where an Awakened Core was meant. | Not in this jar. The pack swaps the ingredient with a `replaceInput` in `kubejs/server_scripts/recipe_fixes.js`, so no copy of the recipe file of Draconic Evolution ships. |
| 5 | Commit [4d360c95](https://github.com/Draconic-Inc/Draconic-Evolution/commit/4d360c95bb8282c27071079e51011d248442eba2) | Draconic Evolution 1.20.1 puts none of its tools into the vanilla tool tags. | Not in this jar. The pack adds the Wyvern, Draconic and Chaotic tools to the vanilla tags for pickaxes, axes, shovels, swords and hoes, and both staffs to the first four, which is the tag set of 1.21, in `kubejs/server_scripts/tag_fixes.js`. |
| 6 | Commit [d297c265](https://github.com/Draconic-Inc/Draconic-Evolution/commit/d297c2657cefa6ac2af19a96ee12a51ab07d36f5) | Squid, fish and other water animals drop souls with the hostile chance instead of the passive one, because the check is `instanceof Animal`. | A `@Redirect` of that `instanceof` in `LootEventHandler.handleSoulDrops` accepts `Animal` or `WaterAnimal`. |
| 7 | Commit [9282fa7c](https://github.com/Draconic-Inc/Draconic-Evolution/commit/9282fa7cb0e5d6101b868a1f4ee72a04d472b4aa) | The Chaos Implosion death message shows its raw key. | The key `death.attack.draconicevolution.chaos_implosion` sits in `assets/skytekx_defixes/lang/en_us.json` with an English text of our own, "%1$s was caught in a Chaos Implosion". The game merges the language files of all namespaces, so the key works from this mod's namespace and cannot hide the language file of Draconic Evolution. |
| 8 | Commit [51ab0afb](https://github.com/Draconic-Inc/Draconic-Evolution/commit/51ab0afb2839512b4a165b3ea5020e960635fc14) | The Dislocator Receptacle and the Creative OP Capacitor are full opaque cubes with the `noOcclusion()` machine properties, so light passes through them. | A `@ModifyArg` on their super constructor call swaps the shared `DEContent.MACHINE` for the `MACHINE_OPAQUE` of upstream, the same chain without `noOcclusion`. Other machines keep `MACHINE`. |
| 9 | Commit [3ae9baa2](https://github.com/Draconic-Inc/Draconic-Evolution/commit/3ae9baa2531927471d98bb49be2cc08e9b68697f) | The Claim XP button of the Mob Grinder is labelled "Collect XP". | A client `@ModifyConstant` in `GrinderGui.lambda$buildGui$14`, the Claim XP label in the 3.1.2.621 jar, asks for `claim.xp`. The kill box half of the commit is not needed. The 1.20.1 code builds the box with `new AABB(pos1, pos2)`, which is the right size. It grew by one block only when the 1.20.4 port switched to `AABB.encapsulatingFullBlocks`. |
| 10 | Commit [d6a3ceae](https://github.com/Draconic-Inc/Draconic-Evolution/commit/d6a3ceaebe3f1349fa1f3cec51046775089dc062) | The tier `DRACONIC_TIER` is `new DETier(TechLevel.DRACONIUM)`, so the Draconic tools have harvest level 3, below Wyvern (32), and the draconium harvest speed, damage, swing speed and enchantability. | A `@ModifyArg` on the second `DETier` constructor call in `DEContent.<clinit>` passes `TechLevel.DRACONIC`. The Draconic tools then have level 128, harvest speed 25, damage bonus 1.75, swing speed 1.5 and enchantability 25 with the default equipment config, instead of 3, 10, 1.125, 1.125 and 12. |
| 11 | Commit [4d6fc7bf](https://github.com/Draconic-Inc/Draconic-Evolution/commit/4d6fc7bfe19abd4ac6e75517e8bcb5806a87ff81) | Shift and scroll with an Advanced Dislocator does nothing, because the client code is commented out and there is no server handler. | Not applied. Any version of it would reproduce most of upstream's statements, and it would raise the share of such statements in this mod toward the limit of the license, see NOTICE. |
| 12 | Brandon's Core commit [6d6c5a55](https://github.com/Draconic-Inc/BrandonsCore/commit/6d6c5a55fc9f725b80d44c388c612583c022308e) | Upstream fixed the HUD elements of Brandon's Core (the shield, energy and item HUDs of Draconic Evolution) staying on screen in F1 mode on the GUI path of 1.21, where the event that draws them always fires. On 1.20.1, `GameRenderer.render` skips `Gui.render`, and with it the Forge `RenderGuiEvent`, while `hideGui` is set and no screen is open (javap of the mapped Forge 1.20.1-47.4.0 jar, `GameRenderer.render`, bytecode offsets 562 to 613). So on stock 1.20.1 the HUDs can only show in F1 mode while a screen is open. | A client `@Inject` at the head of `HudManager.onDrawOverlayPre`, `onDrawOverlayPost` and `onClientTick` cancels them while `options.hideGui` is set, as upstream does. Nobody has checked it in a client yet. |

## Mixin rules

- Every injector has `require = 1` (config `defaultRequire`, the HUD injector `require = 3`), so a changed
  target fails at class load instead of silently doing nothing. The `mods.toml` pins Draconic Evolution
  `[3.1.2.621]` and Brandon's Core `[3.2.1.302]`, so a different build stops the game with a clear message
  first. Every target was checked with `javap` against the pinned jars.
- Only classes of Draconic Evolution and Brandon's Core are targeted, no Minecraft class, so nothing that
  the Bukkit patches of Arclight touch. No mixin uses `@Overwrite`. The class `ModuleEntity`, which TiCEX
  mixes into, is not touched.
- Client code (`mixin/client/`) is only loaded on the client.

## Sides

The mod is required on both sides. Its network channel `skytekx_defixes:main` carries no messages. It
exists because Forge compares the channels of both sides at login and accepts only the same protocol on
the other side, so a client without the mod cannot join a server with it and the other way round. That
keeps the Draconic tool tier, which decides the mining speed on both sides, from going out of sync.

## Build

Build with Java 17:

```bash
./gradlew build
```

The jar lands in `build/libs/skytekx_defixes-<version>.jar`. The compile dependencies are the exact files
of the pack's `modrinth.index.json`, fetched from the Modrinth CDN by an ivy repository in `build.gradle`
and checked against the SHA-1 values in `gradle.properties` (task `verifyPackJars`). Keep those values in
step with the index. The build is byte reproducible. After a source change, rebuild, copy the jar to
`local-mods/` and update the SHA-1 and size in `modrinth-permissions/skytekx_defixes-<version>.jar.md`,
so that the shipped jar always matches this source. The build pins its Gradle plugins (NeoGradle 6.0.21,
Librarian 1.2.0.7-dev-SNAPSHOT, MixinGradle 0.7.38), which are what the open version ranges gave for the
first build.

## Probe

The folder [probe/](probe) holds a test-only mod that is built apart and never ships. On a test server
next to the fix pack, its console command `deprobe` checks the recipe, the tool tags, the tier, the
occlusion, the death message, the crystal shape, the chest dupe with five kinds of click and the
handshake channel, and logs one `[deprobe]` line per check. Build it with `./gradlew -p probe build`.
