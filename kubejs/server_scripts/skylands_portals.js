// SkyTekx3: nether portals work in the Skylands.
//
// Vanilla, and Arclight with it, lights nether portals only in the overworld and the nether, and a portal
// in the nether always leads to the overworld. The owner asked on 2026-11-02 that nether portals work in
// the Skylands too.
//
// Lighting: flint and steel or a fire charge used on a complete obsidian frame in the Skylands opens the
// portal one tick later, as in the overworld. A portal in the Skylands then leads to the nether like any
// other, at an eighth of the coordinates. Vanilla does that part on its own.
//
// Way back: a player who goes through a portal in the nether returns to the dimension of the last portal
// the player stood in outside the nether, which the player data keeps. Who came from the Skylands goes
// back to the Skylands, at eight times the coordinates, to the closest portal within 128 blocks or to a new
// one. Everybody else goes to the overworld as before. The script moves the player after 60 ticks in the
// portal, before vanilla would after 80, and counts like vanilla: one up per tick inside, four down per
// tick outside, nothing while the portal cooldown runs. Players in creative mode cross after one tick,
// before the script can act, so they land in the overworld.

const SkyPortalShape = Java.loadClass('net.minecraft.world.level.portal.PortalShape')
const SkyPortalAxis = Java.loadClass('net.minecraft.core.Direction$Axis')
const SkyPortalOriginKey = 'skytekx3PortalOrigin'
const SkyPortalDelay = 60
var skyPortalTicks = {}
var skyPortalErrors = 0

function skyPortalCall(object, name) {
  // KubeJS turns some getters into properties with the same name as the vanilla method.
  return typeof object[name] == 'function' ? object[name]() : object[name]
}

function skyPortalDim(level) {
  return String(skyPortalCall(level, 'dimension'))
}

function skyPortalIsSkylands(level) {
  return skyPortalDim(level).indexOf('skytekx3:skylands') >= 0
}

function skyPortalIsNether(level) {
  return skyPortalDim(level).indexOf('minecraft:the_nether') >= 0
}

function skyPortalIsOverworld(level) {
  return skyPortalDim(level).indexOf('minecraft:overworld') >= 0
}

function skyPortalSkylands() {
  var levels = Utils.server.getAllLevels().iterator()
  var level = null
  while (levels.hasNext()) {
    level = levels.next()
    if (skyPortalIsSkylands(level)) return level
  }
  return null
}

function skyPortalIsPortalBlock(level, pos) {
  return String(level.getBlockState(pos).getBlock().getDescriptionId()) == 'block.minecraft.nether_portal'
}

function skyPortalInside(level, player) {
  var pos = player.blockPosition()
  return skyPortalIsPortalBlock(level, pos) || skyPortalIsPortalBlock(level, pos.above())
}

function skyPortalKey(player) {
  return String(player.uuid)
}

function skyPortalError(what, e) {
  if (skyPortalErrors++ < 5) console.error('Skylands portals: ' + what + ' failed: ' + e)
}

function skyPortalLight(level, pos) {
  try {
    var shape = SkyPortalShape.findEmptyPortalShape(level, pos, SkyPortalAxis.X)
    if (!shape.isPresent()) return
    shape.get().createPortalBlocks()
    console.info('Skylands portals: opened a portal at ' + pos.getX() + ' ' + pos.getY() + ' ' + pos.getZ())
  } catch (e) {
    skyPortalError('lighting', e)
  }
}

function skyPortalTick(player) {
  var level = skyPortalCall(player, 'level')
  var key = skyPortalKey(player)
  var ticks = skyPortalTicks[key] || 0
  var inside = skyPortalInside(level, player)
  if (!inside || player.isOnPortalCooldown()) {
    skyPortalTicks[key] = ticks > 4 ? ticks - 4 : 0
    return
  }
  if (!skyPortalIsNether(level)) {
    if (skyPortalIsSkylands(level)) player.persistentData.putString(SkyPortalOriginKey, 'skylands')
    else if (skyPortalIsOverworld(level)) player.persistentData.putString(SkyPortalOriginKey, 'overworld')
    return
  }
  if (String(player.persistentData.getString(SkyPortalOriginKey)) != 'skylands' || player.isPassenger()) return
  ticks++
  skyPortalTicks[key] = ticks
  if (ticks < SkyPortalDelay) return
  skyPortalTicks[key] = 0
  var skylands = skyPortalSkylands()
  if (skylands == null) return
  player.changeDimension(skylands)
  player.setPortalCooldown()
  console.info('Skylands portals: ' + player.username + ' went back to the Skylands')
}

BlockEvents.rightClicked(event => {
  try {
    var id = String(event.item.id)
    if (id != 'minecraft:flint_and_steel' && id != 'minecraft:fire_charge') return
    var level = event.block.level
    if (!skyPortalIsSkylands(level)) return
    var pos = event.block.pos.relative(event.facing)
    // One tick later, after vanilla placed its fire, so the portal replaces it as in the overworld.
    Utils.server.scheduleInTicks(1, () => skyPortalLight(level, pos))
  } catch (e) {
    skyPortalError('right click', e)
  }
})

PlayerEvents.tick(event => {
  if (skyPortalErrors >= 5) return
  try {
    skyPortalTick(event.player)
  } catch (e) {
    skyPortalError('player tick', e)
  }
})

console.info('Skylands portals: active')
