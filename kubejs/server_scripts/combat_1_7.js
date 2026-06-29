// SkyTekx3 — "1.7 melee feel": remove the 1.9 attack-speed cooldown (server-authoritative).
//
// How it works:
//   The post-1.9 cooldown is driven by the `minecraft:generic.attack_speed` attribute.
//   getAttackStrengthScale() = (ticksSinceLastSwing) / (20 / attack_speed). When attack_speed
//   is huge, the recharge window is < 1 tick, so EVERY click lands at full strength = 1.7
//   spam-click damage. Because this runs on the server, the bar/damage stay correct even
//   against players who don't have any client mod (attack_speed is server -> client synced,
//   so the client cooldown UI follows automatically).
//
// TUNING / REVERT:
//   - VALUE below is the only knob. Bigger = closer to instant (default 1024 = effectively no
//     cooldown for every weapon, including slow modded ones). If full spam-click feels too
//     strong, LOWER it (e.g. 20 gives a small cooldown back to the slowest weapons; 8 is a
//     gentler middle ground). Set the file aside / delete it and run /reload (or relog) to
//     return to fully-vanilla 1.9 cooldown.
//   - The modifier is TRANSIENT (not written to player NBT), so deleting this script and
//     relogging cleanly reverts with nothing left behind.

const Attributes = Java.loadClass('net.minecraft.world.entity.ai.attributes.Attributes')
const AttributeModifier = Java.loadClass('net.minecraft.world.entity.ai.attributes.AttributeModifier')
const Operation = Java.loadClass('net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation')
const UUID = Java.loadClass('java.util.UUID')

// Resolve these at script-load so a bad name/mapping fails loudly at boot, not silently on login.
const ATTACK_SPEED = Attributes.ATTACK_SPEED
const OP_ADDITION = Operation.ADDITION
const MOD_UUID = UUID.fromString('2f8c9d10-7b3a-4e5f-9a1b-c2d3e4f5a6b7')
const VALUE = 1024.0 // additive attack_speed bonus -> recharge < 1 tick -> every click = full damage

function removeAttackCooldown(player) {
  if (!player) return
  const inst = player.getAttribute(ATTACK_SPEED)
  if (inst === null) return
  // Idempotent: drop our previous modifier (if any) before re-adding, so it never stacks.
  if (inst.getModifier(MOD_UUID) !== null) inst.removeModifier(MOD_UUID)
  inst.addTransientModifier(new AttributeModifier(MOD_UUID, 'skytekx3_no_attack_cooldown', VALUE, OP_ADDITION))
}

// Apply on join and on respawn (respawn rebuilds the player's attribute map from scratch).
PlayerEvents.loggedIn(event => removeAttackCooldown(event.player))
PlayerEvents.respawned(event => removeAttackCooldown(event.player))
