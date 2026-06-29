// SkyTekx3 — gameplay recipe tweaks (ported from scripts-legacy-reference/thp_changes.zs)
// See docs/RECIPE_PORT.md for the full status table.

ServerEvents.recipes(event => {

  // [PORT] Nerf Quark rope (was too cheap). [VERIFY] confirm 'quark:rope' on 1.20.1.
  // event.remove({ id: 'quark:rope' })
  // event.shaped('4x quark:rope', ['SSS', 'S S', 'SSS'], { S: 'minecraft:string' })

  // [DROP] Quark stone-tool stick fix — 1.20.1 vanilla stone tools already accept
  //   #minecraft:stone_tool_materials + #forge:rods/wooden, so the legacy fix is moot.

  // [DROP] Quark wool/shulker duplicate removals — only re-add if a duplicate actually
  //   reappears in EMI on 1.20.1 (Quark 1.20.1 likely does not register them).

  // [PORT/VERIFY] Asgard Shield Reloaded giant swords using ore tags.
  //   AsgardShieldReloaded IS on 1.20.1 — confirm item ids, then enable.
  // event.remove({ output: /asgardshieldreloaded:.*_giant_sword/ })
  // ... re-add with #forge:rods/wooden + material tags ...
})

// [REAUTHOR] Loot tweaks (cow rolls x2, Hexxit-World arrow-drop nerf) require the
// LootJS mod (added to manifest). Implement in kubejs/server_scripts/loot_tiers.js
// once LootJS + the loot-bearing mods are present.
