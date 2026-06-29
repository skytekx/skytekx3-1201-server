// SkyTekx3 — Aether-style "fall to the overworld" from the Skylands void.
//
// When a player who is in skytekx3:skylands drops off the islands into the void
// below them, we relocate them to the SAME X/Z in minecraft:overworld but high in
// the sky (Y 300) so they free-fall down into the overworld — exactly like falling
// off the edge of the Aether.
//
// THE THRESHOLD (VOID_Y): the skylands island mass sits well above y0 and every
// column below ~y12 is pure air, so reaching y < 0 means a clean fall off the bottom
// of the world. Catching at y < 0 fires long before any void damage and respects
// player-built platforms in the legal 0..255 range.
//
// RHINO BINDING NOTE (this is what was broken before): in KubeJS/Rhino here, the
// no-arg accessors level()/dimension()/location() are exposed as bean PROPERTIES,
// so they MUST be read without parentheses (`player.level`, not `player.level()`);
// calling them throws "TypeError: ... is not a function, it is object" every tick.
// getX()/getY()/getServer()/getLevel(...) keep their parens (getter / arg methods).
//
// EFFICIENCY: the per-tick hot path is a single double compare (player.getY() >= 0)
//   and returns immediately for everyone who is not currently in the void.

const Component = Java.loadClass('net.minecraft.network.chat.Component')
const ChatFormatting = Java.loadClass('net.minecraft.ChatFormatting')
const Level = Java.loadClass('net.minecraft.world.level.Level')
const OVERWORLD = Level.OVERWORLD   // ResourceKey<Level> for minecraft:overworld

const SKYLANDS = 'skytekx3:skylands'
const VOID_Y = 0      // below this Y in the skylands = clearly in the void
const DROP_Y = 300.0  // re-enter the overworld near the top of the world and free-fall

PlayerEvents.tick(event => {
  const player = event.player

  // Cheapest possible gate first: one Y compare. Anyone above the void floor is done.
  if (player.getY() >= VOID_Y) return

  // Property access (no parens) — see RHINO BINDING NOTE above.
  if (('' + player.level.dimension.location) !== SKYLANDS) return

  const server = player.getServer()
  if (server == null) return
  const overworld = server.getLevel(OVERWORLD)   // arg method -> not bean-mapped -> safe
  if (overworld == null) return

  player.teleportTo(overworld, player.getX(), DROP_Y, player.getZ(), player.getYRot(), player.getXRot())
  player.sendSystemMessage(
    Component.literal('You fell from the Skylands…').withStyle(ChatFormatting.AQUA)
  )
})
