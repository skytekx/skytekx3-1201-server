# Upstream: Expanded Delight (Forge 1.20.1 port)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Expanded Delight, the
base of the SkyTekx port of its NeoForge 1.21.1 line to Forge 1.20.1. Changes made on top of the
snapshot are separate commits in this repository's history (`git log -- ports/expandeddelight`).

| Item | Value |
|---|---|
| Repository | https://github.com/ianm1647/expandeddelight |
| Content ref | Branch `1.21-neo` |
| Commit | `ea826400654702587e86c9e94544bfd74f065083` ("fix workspace") |
| Commit date | 2026-08-25T14:55:01-05:00 |
| Upstream version | `mod_version=0.1.4` (Minecraft 1.21.1, NeoForge 21.1.218, Farmer's Delight 1.21.1-1.2.6) |
| Release | Modrinth version `RTRYPIJp` (0.1.4-neoforge, `expandeddelight-0.1.4.jar`), built from `28d9561714a176eb10ae0c425fcee18ad8e6b3b1` on the same branch |
| API reference | Branch `1.20` @ `8d8fe44d9b4cba54c47f84c5836872a803388cb2` (2026-01-14, 0.4.0 for Fabric 1.20.1) |
| Not used | Branch `1.21-rework` @ `5ef645b2f68eb19ab337233932a0912cfd4fa29f` (0.2.0), an emptied restart whose commits say "complete emptiness" and "content cut" |

## Why 1.21-neo

The 1.20 branch is the Fabric line for Minecraft 1.20.1. It has 44 item names, among them the
mortar and pestle, ground cinnamon and ground salt. The 1.21-neo line has 99 items and adds the
cinnamon wood set with boats, signs and a cabinet, casks and cheese wheels, goat milk, cranberries,
peanuts, crates, salt ore and many dishes, so it is the newest complete content set. The 1.20 code
shows the 1.20.1 shapes of the vanilla API, but it is written for Fabric, so the port takes the
Forge 1.20.1 forms from Farmer's Delight 1.20.1 instead, which is a required dependency.

## Files left out of the snapshot

Every file in the snapshot is byte identical to `git archive ea82640`, except these seven, which
never entered git here:

| Upstream file | Why |
|---|---|
| File `src/main/resources/assets/expandeddelight/textures/gui/juicer.png` | Built on the kettle sheet of Farmer's Respite (All Rights Reserved), sha1 `2a2bffad` |
| File `src/main/resources/assets/expandeddelight/textures/gui/jei/juicer.png` | The same file as the one above |
| File `src/main/resources/assets/expandeddelight/textures/item/empty_container_slot_bottle.png` | Byte identical to the icon of Farmer's Respite since 1.16.5, sha1 `64863cf1` |
| File `src/main/java/ianm1647/expandeddelight/common/block/entity/container/JuicerMenu.java` | Decompiled from the `KettleMenu` of Farmer's Respite. It keeps an SRG method name and the kettle's slot constants |
| File `src/main/resources/assets/expandeddelight/textures/item/glass_bottle.png` | Byte copy of the vanilla texture, used by no model |
| File `src/main/resources/assets/expandeddelight/textures/item/sweet_berries.png` | Pixel copy of the vanilla texture, used by no model |
| File `src/main/resources/assets/expandeddelight/textures/block/rice_bag_bottom.png` | Byte copy of the Farmer's Delight texture. The cranberry bag model already points at `farmersdelight:block/rice_bag_bottom` |

The license review behind this list compared every texture with Farmer's Delight, Farmer's Respite,
Brewin' and Chewin', Cultural Delights, Delightful, Pam's HarvestCraft 2, Twilight Delight, Farming
for Blockheads and vanilla 1.20.1. Croptopia was not compared, because it is not on Modrinth. Code
provenance was checked in depth only for the juicer.

## License

- Expanded Delight declares MIT: `mod_license=MIT` in `gradle.properties` of 1.21-neo, `license="MIT"`
  in the mods.toml of the 0.1.4 jar, MIT on Modrinth (project `e9V6wFcR`) and on CurseForge (project
  620770). Neither 1.21-neo nor 1.21-rework has a LICENSE file.
- The file `LICENSE-MIT` is the author's own MIT text, `Copyright (c) 2022 ianm1647`, from branch 1.19, commit
  `6c73c9877dbf59f310630080f11971dba25c8b0b` (2022-06-10). MIT is treated as the governing license,
  since it has conditions and CC0 has none.
- The file `LICENSE` is the root LICENSE of branch 1.20 at `8d8fe44`, CC0 1.0 Universal, unchanged. It came in
  with commit `afa8a30` (2024-09-10) together with the stock Fabric example README.
- The file `LICENSE_farmersdelight` is the MIT text of Farmer's Delight, `Copyright (c) 2020 vectorwing`, from
  its branch 1.20 (also the same text on 1.21). Expanded Delight copies or derives Farmer's Delight
  work: its juicer block entity, screen and recipe follow the cooking pot classes, `jei/fermenting.png`
  matches 96 percent of the custom pixels of `jei/decomposition.png`, and textures such as
  `cinnamon_rice`, `cranberry_juice`, `goat_milk_bottle` and the cinnamon cabinet recolor its art.
- The file `TEMPLATE_LICENSE.txt` is upstream's MIT text for the NeoForged MDK template files. It stays,
  because `pack.mcmeta` and `.gitattributes` come from that template.
- The jar carries all four texts and the `NOTICE` of this build: `LICENSE-MIT_expandeddelight`,
  `LICENSE_expandeddelight`, `LICENSE_farmersdelight`, `TEMPLATE_LICENSE_expandeddelight.txt` and
  `NOTICE_expandeddelight`.
- Every commit reachable from 1.21-neo is by the author (25 as ianm1647, 1 as ianmc with the same
  email). This port takes no translation, tag or recipe from the 1.20 branch, which has about 20
  outside contributors. The only 1.20 file it uses is the author's juicer GUI sheet.

## Build changes

The snapshot builds with ModDevGradle for NeoForge 1.21.1 on Java 21. The port replaces that setup
with the one of `../skytekx-defixes`.

1. The Gradle wrapper files `gradlew`, `gradlew.bat` and `gradle/wrapper/` are the copies that the other
   ports of this repository use (wrapper jar SHA-256 `33ad4583...`, Gradle 8.2.1).
2. The file `build.gradle` uses ForgeGradle 6.0.21 (`net.neoforged.gradle`), Parchment 2023.07.30-1.20.1,
   NeoForged Forge 47.1.70 userdev, MixinGradle 0.7.38 and Java 17. Farmer's Delight comes from the
   Modrinth CDN as the exact file of the pack's `modrinth.index.json`, and `verifyPackJars` checks its
   SHA-1 before the compile, as do KubeJS, Rhino and Architectury, which are compile only. The JEI
   API 15.20.0.106 is compile only from maven.blamejared.com.
3. The file `settings.gradle` sets `rootProject.name = 'expandeddelight'`.
4. The version is `mod_version=0.1.4-skytekx.1`. The jar is `expandeddelight-0.1.4-skytekx.1.jar`.
5. The jar is reproducible. The `jar` task sets `preserveFileTimestamps = false` and
   `reproducibleFileOrder = true`. Two clean builds of the same commit give the same SHA-1.
6. A new `META-INF/mods.toml` replaces `neoforge.mods.toml`. It requires Forge 47, Minecraft 1.20.1 and
   Farmer's Delight 1.20.1-1.3.4 or newer, points `issueTrackerURL` and `displayURL` at this
   repository and credits the author. `enumextensions.json` is gone, Forge 1.20.1 extends enums
   through `IExtensibleEnum`.
7. The port has no mixin and no access transformer, so the build has no MixinGradle and the manifest
   names no mixin config. Upstream's only mixin goes away with source change 6, and its access
   transformer served the datagen only.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew build -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

## Source changes

These changes translate the 1.21.1 code to Forge 1.20.1. Each keeps upstream's ids, block states,
recipe keys and NBT names unless it says otherwise.

1. Registries: `DeferredRegister` and `RegistryObject` of Forge, the menu type through
   `IForgeMenuType`, the recipe serializer with `fromJson`, `fromNetwork` and `toNetwork`, and the
   1.20.1 constructors of the wood set (`ButtonBlock`, `PressurePlateBlock`, `DoorBlock`,
   `TrapDoorBlock`, `FenceGateBlock`, the four sign blocks), of `DropExperienceBlock` and of the stairs.
   The sapling grows through an `AbstractTreeGrower`, the foliage placer has a `Codec`. The creative
   tab, the wood type and the block set type keep upstream's names.
2. Data components: the juicer item keeps its drink and container as `BlockEntityTag` NBT, as the
   cooking pot of Farmer's Delight 1.20.1 does, and `EDDataComponents` is gone. A new loot function
   `expandeddelight:copy_drink` (`CopyDrinkFunction`, written like Farmer's Delight's
   `CopyMealFunction`) writes it into the dropped juicer, and pick block and the custom name of a
   placed juicer use the same NBT.
3. Capabilities: the juicer answers `ForgeCapabilities.ITEM_HANDLER` in `getCapability`, with
   upstream's split: the top takes ingredients, every other side and no side give the output.
4. The juicer menu is new (`JuicerMenu`), written from Farmer's Delight's `CookingPotMenu` (MIT) for
   two input slots. Its slots match the author's juicer sheet `textures/gui/juicer_gui.png` of the
   1.20 branch: inputs at (48, 25) and (48, 43), the drink at (116, 35), the container at (80, 60) and
   the output at (116, 60). The screen draws that sheet and its 22 pixel progress arrow at (79, 35),
   and the JEI juicing category cuts its background out of the same sheet. The JEI container slot
   is a catalyst, as in Farmer's Delight, upstream calls it an output.
5. The recipe book: `RecipeBookType.create("EXPANDEDDELIGHT_JUICING")` in the main class and three
   `RecipeBookCategories` on the client, as Farmer's Delight 1.20.1 does. The filter button reads
   `textures/gui/recipe_book_buttons.png`, the four upstream sprites of
   `textures/gui/sprites/recipe_book/` laid out in one sheet for 1.20.1's `StateSwitchingButton`.
   The tab enum `JuicerRecipeBookTab` moves to `common/crafting`, because the common recipe uses it.
6. Goat milk: upstream's `RemoveMilkGoatMixin` answered `FAIL` in `Goat#mobInteract` for every
   bucket, so that the vanilla milking could not run after its own. The port drops the mixin and
   cancels the Forge interaction event when it fills a bucket or a bottle, which ends the
   interaction before the goat sees it. No mixin touches the goat, a class that Bukkit patches on the
   Arclight server.
7. The goat milk bucket is a `GoatMilkBucketItem` that cures effects as the vanilla milk bucket
   does, since Forge 1.20.1 cures only effects whose curative items include the drunk item.
8. Cinnamon logs and wood are stripped through Forge's `BlockToolModificationEvent` for the
   `axe_strip` action, which gives the sticks only when the change is real and keeps the log's axis.
   Upstream stripped them itself in a right click event, damaged the tool in the wrong hand and
   turned every log upright.
9. Cranberries plant through the `cranberry_plant` item, a vanilla `PlaceOnWaterBlockItem`, so the
   placement checks the spot and Bukkit sees it as a block placement. Upstream set the block above
   any water source it hit without such a check. The cranberry plant reports the plant type `WATER`,
   which Forge 1.20.1 needs to keep it on water.
10. The client classes register through Forge 1.20.1 events: the juicer screen and the cinnamon wood
    type sheets in `FMLClientSetupEvent`, the boat layers in `RegisterLayerDefinitions`. The tooltip
    data record `JuicerTooltipComponent` moves to common code, so that no common class names a
    client class. The tooltip and screen use the language keys of Farmer's Delight 1.20.1
    (`tooltip.farmersdelight.cooking_pot.*`, `container.farmersdelight.cooking_pot.served_on`).
11. Composting chances come from code in `EDCompostables` instead of upstream's NeoForge data map
    `data/neoforge/data_maps/item/compostables.json`, with the same values.
12. The KubeJS plugin uses the KubeJS 6 API of the pack's KubeJS 2001.6.5 with upstream's keys and
    order: `event.recipes.expandeddelight.juicing(ingredients, result, experience, juicingtime,
    container)`.
13. The tags in `EDCommonTags` are forge: tags, see the data changes.

Fixes of upstream bugs, each small and local:

- The crops named themselves as their seed (`EDCropBlock#getBaseSeedId` compared a block with a
  supplier), so pick block gave nothing for asparagus and chili peppers.
- The juicer saved its progress as the total time, and handing out a drink with a held bottle neither
  marked the block entity for saving nor synced it, so a drink taken just before a restart could
  come back.
- The cinnamon chest boat did not call its parent's save and load, so the chest lost its items at
  every save.
- The juicer's inventory has the five slots that the code uses, upstream allocated nine.
- The wild sweet potato gives Absorption in suspicious stew, as all wild crops of the 1.20 branch do,
  because Oozing does not exist before Minecraft 1.20.5.

Left out of the code:

- The datagen classes in `data/` (19 files) and the worldgen bootstrap classes `EDBiomeModifiers`,
  `EDPlacedFeatures` and `OrePlacement`. They build upstream's JSON on NeoForge. This port converts
  that JSON once and keeps it in `src/generated/resources`. `EDConfiguredFeatures` keeps the keys.
- `EDTableLootModifier`, which nothing registers upstream.
- `EnumParameters`, the NeoForge enum extension parameters, replaced by `IExtensibleEnum` calls.

## Data changes

The script `tools/convert_data_to_1_20_1.py` converted upstream's data once, in place, and its
docstring lists each step. It is idempotent, a second run changes nothing. The data stays where
upstream keeps it, the datagen output in `src/generated/resources` and the hand written files in
`src/main/resources`, so `git log --follow` shows each file's 1.21 form.

1. Folder names: `recipe` to `recipes`, `loot_table` to `loot_tables`, `advancement` to
   `advancements`, `tags/item` and `tags/block` to `tags/items` and `tags/blocks`,
   `neoforge/biome_modifier` to `forge/biome_modifier` and `data/neoforge/loot_modifiers` to
   `data/forge/loot_modifiers`.
2. Formats: item stacks lose `id` for `item`, cooking results of the vanilla furnaces become plain
   item ids, cutting board results become Farmer's Delight 1.20.1 chance results, item predicates
   take lists and the 1.20.1 enchantment form, `neoforge:can_item_perform_ability` becomes
   `forge:can_tool_perform_action`, `neoforge:loot_table_id` becomes `forge:loot_table_id` and the
   biome modifiers use `forge:add_features`. The juicer's loot table copies the name with
   `minecraft:copy_name` and the drink with the new `expandeddelight:copy_drink`, the cabinet's
   only its name.
3. Cutting board tools follow Farmer's Delight 1.20.1, a tool action and then the tag:
   `knife_dig` with `forge:tools/knives` for upstream's `c:tools/knife`, and `axe_dig` with
   `minecraft:axes` for upstream's `minecraft:axes`. The crushing mallet keeps its own tag.
4. Common tags: upstream's c: tags become forge: tags with the names that Farmer's Delight 1.20.1
   and Pam's HarvestCraft 2 Food Core use, so the crops, cheese, cookies, juices and salt of this
   mod meet the same tags as theirs. The table is `TAG_MAP` in the script. Three c: tags have no
   1.20.1 convention and are dropped: `c:foods` (a parent of the two cheese tags),
   `c:foods/edible_when_placed` and `c:foods/soup`. The hanging sign recipe names
   `minecraft:chain` instead of `c:chains`, which Forge 1.20.1 does not have.
5. Worldgen: `minecraft:short_grass` is `minecraft:grass` in 1.20.1. The biome modifiers use
   `#minecraft:is_jungle`, `#minecraft:is_ocean`, `#minecraft:is_savanna`, `#forge:is_desert`,
   `#forge:is_plains` and `#forge:is_swamp`. None of them holds `skytekx3:sky_void` or another
   biome of the SkyTekx Skylands, and the wild crop and cranberry patches also keep upstream's
   placement filter `farmersdelight:biome_tag` for `#minecraft:is_overworld`.
6. The pie models of the cranberry cobbler and the honeyed goat cheese tart point at Farmer's
   Delight 1.20.1's `template_pie` models, which 1.21 renamed to `pie`.
7. The composting chances of `data/neoforge/data_maps/item/compostables.json` moved to code, see
   source change 11.

Dropped data, with the reason:

- Overrides of other mods' recipes: `farmersdelight:wheat_dough_from_water` and
  `wheat_dough_from_eggs` (dough needs salt), `farmersdelight:melon_juice` and
  `farmersrespite:brewing/melon_juice_from_water` (disabled), and the two advancements of the dough
  recipes. The SkyTekx pack edits no recipe of another mod per mod, and Farmer's Delight 1.20.1 has
  other dough recipe ids (`wheat_dough_from_egg`) and a special recipe type, so the files would add
  recipes instead of replacing them.
- The item tags `minecraft:chicken_food`, `parrot_food`, `pig_food` and `fox_food`, which only
  exist from Minecraft 1.20.5 on.
- The entry `expandeddelight:add_loot_sniffer_digging` of the global loot modifier list, for which
  upstream has no file.
- The datagen cache `src/generated/resources/.cache`.

Assets:

- The file `textures/gui/juicer_gui.png` is the author's juicer sheet of branch 1.20, unchanged
  since commit `50e9c102aaa44dedae924ee1dc381af17aaa3d69` (2022-05-25), SHA-1 `94706b23`. The
  license review found it mostly the author's own drawing, with 121 scattered pixels that coincide
  with the slot frames of Farmer's Respite's kettle. It replaces the two left out juicer sheets.
- The file `textures/gui/recipe_book_buttons.png` is built by `tools/make_recipe_book_buttons.py`
  from upstream's four sprites, which stay in `textures/gui/sprites/recipe_book/`.
- Upstream's `models/block/mortar_and_pestle.json` names a texture that does not exist. No block
  state uses the model, so the game never loads it, and it stays as upstream has it.
- No language file other than upstream's `en_us.json` is added. The 1.20 branch has eleven
  translations with outside contributors and other item names.
