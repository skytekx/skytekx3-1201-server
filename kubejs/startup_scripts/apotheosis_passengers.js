// SkyTekx3: the wither cloud of a Withering Archer leaves the level together with its archer.
//
// The server script kubejs/server_scripts/apotheosis_riders.js adds the wither cloud of the Withering
// Archer miniboss to the level, where it rides the archer. Apotheosis discards that cloud when the archer
// dies, but not when the archer despawns. A mob that leaves the level for good only drops its passengers,
// so the cloud would stay behind on its own as a wither cloud with a radius of 2.5 blocks for up to
// 200000 ticks. KubeJS 6 registers Forge events only from startup scripts, so this part lives here and
// takes a restart to change. The client pack carries this file as well, and there it does nothing.
//
// When an area effect cloud stops riding a skeleton or wither skeleton, the cloud is discarded, unless the
// skeleton is only being unloaded with its chunk. Then the chunk saves the cloud as the passenger of the
// skeleton and loads it again with it.

var ApothCloud = Java.loadClass('net.minecraft.world.entity.AreaEffectCloud')
var ApothSkeleton = Java.loadClass('net.minecraft.world.entity.monster.AbstractSkeleton')
var apothCloudsDiscarded = 0
var apothCloudErrors = 0

function apothCloudCall(object, name) {
  // KubeJS turns some getters into properties with the same name as the vanilla method.
  return typeof object[name] == 'function' ? object[name]() : object[name]
}

ForgeEvents.onEvent('net.minecraftforge.event.entity.EntityMountEvent', event => {
  if (apothCloudErrors >= 5) return
  try {
    if (event.isMounting()) return
    var cloud = event.getEntityMounting()
    var vehicle = event.getEntityBeingMounted()
    if (!(cloud instanceof ApothCloud) || !(vehicle instanceof ApothSkeleton)) return
    if (apothCloudCall(event.getLevel(), 'isClientSide') || cloud.isRemoved()) return
    var reason = vehicle.getRemovalReason()
    if (reason != null && !reason.shouldDestroy()) return
    cloud.discard()
    apothCloudsDiscarded++
    if (apothCloudsDiscarded <= 100) {
      console.info('Apotheosis passengers: discarded the cloud of a ' + vehicle.getEncodeId() + ' that ' +
        (reason == null ? 'dropped it' : 'left the level') + ' at ' + Math.floor(vehicle.getX()) + ' ' +
        Math.floor(vehicle.getY()) + ' ' + Math.floor(vehicle.getZ()))
    }
  } catch (e) {
    if (apothCloudErrors++ < 5) console.error('Apotheosis passengers: discarding a cloud failed: ' + e)
  }
})

console.info('Apotheosis passengers: active')
