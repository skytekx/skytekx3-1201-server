// SkyTekx3: no hostile mobs in the Skylands.
//
// The owner ruled on 2026-11-02 that the Skylands are where everybody builds a home, so no hostile mobs
// may live there. Since the Skylands stay at noon, a fixed time that never counts as day, zombies and
// skeletons no longer burned there, and mobs from the caves piled up around the homes.
//
// Every entity that joins the Skylands and is an enemy (the vanilla Enemy interface, which all vanilla
// hostile mobs and every modded mob built on Monster carry) is refused. That covers natural
// spawns, spawners, mobs that come through a portal and mobs loaded with a chunk from disk. Animals and
// other peaceful mobs stay. When the script loads, it also removes the hostile mobs that are in the
// Skylands right now.

const SkyNoMobsEnemy = Java.loadClass('net.minecraft.world.entity.monster.Enemy')
var skyNoMobsErrors = 0

function skyNoMobsIsSkylands(level) {
  return String(level).indexOf('skytekx3/skylands') >= 0
}

function skyNoMobsIsHostile(entity) {
  return entity instanceof SkyNoMobsEnemy
}

function skyNoMobsPurge() {
  var server = Utils.server
  if (server == null) return
  var levels = server.getAllLevels().iterator()
  var level = null
  var candidate = null
  while (levels.hasNext()) {
    candidate = levels.next()
    if (skyNoMobsIsSkylands(candidate)) level = candidate
  }
  if (level == null) return
  var found = []
  var entities = level.getAllEntities().iterator()
  var entity = null
  while (entities.hasNext()) {
    entity = entities.next()
    if (skyNoMobsIsHostile(entity)) found.push(entity)
  }
  var i = 0
  for (i = 0; i < found.length; i++) found[i].discard()
  console.info('Skylands no mobs: removed ' + found.length + ' hostile mobs from ' + level)
}

EntityEvents.spawned(event => {
  var refuse = false
  try {
    refuse = skyNoMobsIsHostile(event.entity) && skyNoMobsIsSkylands(event.level)
  } catch (e) {
    if (skyNoMobsErrors++ < 3) console.error('Skylands no mobs: check failed: ' + e)
  }
  // event.cancel() leaves the handler through an exception, so it must stay outside the try block.
  if (refuse) event.cancel()
})

try {
  skyNoMobsPurge()
} catch (e) {
  console.error('Skylands no mobs: purge failed: ' + e)
}
console.info('Skylands no mobs: active')
