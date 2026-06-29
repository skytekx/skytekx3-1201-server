// SkyTekx3 — Aether-style "fall to the overworld" from the Skylands void.
//
// When a player who is in skytekx3:skylands drops off the islands into the void
// below them, we relocate them to the SAME X/Z in minecraft:overworld but high in
// the sky (Y 300) so they free-fall down into the overworld — exactly like falling
// off the edge of the Aether.
//
// WHY THE THRESHOLD IS Y < 0 (see VOID_Y):
//   The skylands dimension has min_y = 0, height 256 (buildable 0..255). The island
//   noise floor (skytekx3:skylands noise_settings) makes EVERY column below y12 pure
//   air, and the solid island mass sits ~y28..90 (spawn/landing platform is y60).
//   So a player can never be standing on terrain below y0; reaching y < 0 means they
//   have fallen clean off the bottom of the world into the void. Void damage in this
//   dimension only starts at y < min_y - 64 = -64, so catching at y < 0 gives a full
//   64-block buffer before any damage — and it also respects player-built platforms
//   anywhere in the legal 0..255 range (we never yank someone off a low base).
//
// EFFICIENCY: the per-tick hot path is a single double compare (player.getY() >= 0)
//   and returns immediately for everyone who is not currently in the void. Only the
//   rare already-falling player pays for the dimension check + teleport.

const Component = Java.loadClass('net.minecraft.network.chat.Component')
const ChatFormatting = Java.loadClass('net.minecraft.ChatFormatting')

const SKYLANDS = 'skytekx3:skylands'
const VOID_Y = 0      // below this Y in the skylands = clearly in the void (see header)
const DROP_Y = 300.0  // re-enter the overworld near the top of the world and free-fall

PlayerEvents.tick(event => {
  const player = event.player

  // Cheapest possible gate first: one Y compare. Anyone above the void floor is done.
  if (player.getY() >= VOID_Y) return

  // Only the void-fall case gets here. Make sure they are actually in the Skylands.
  if (player.level().dimension().location().toString() !== SKYLANDS) return

  const server = player.getServer()
  if (server == null) return
  const overworld = server.overworld()

  const x = player.getX()
  const z = player.getZ()

  // ServerPlayer.teleportTo(ServerLevel, x, y, z, yaw, pitch) performs the cross-
  // dimension move (and keeps their facing) — same as `/execute in overworld run tp`.
  player.teleportTo(overworld, x, DROP_Y, z, player.getYRot(), player.getXRot())

  player.sendSystemMessage(
    Component.literal('You fell from the Skylands…').withStyle(ChatFormatting.AQUA)
  )
})
