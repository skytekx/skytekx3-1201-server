// SkyTekx3: the riders of Apotheosis bosses sit on their mounts.
//
// Apotheosis 7.4.8 creates the rider of a mounted boss from NBT without a position, so the rider starts
// at x 0, y 0, z 0. ApothBoss.createBoss then moves only the mount to the spawn point, and the spawn adds
// both with addFreshEntityWithPassengers. A rider moves onto its mount in its first tick as a passenger,
// but it only ticks while its own chunk at x 0, z 0 is entity ticking. In a world whose spawn lies near
// the origin, that chunk belongs to the spawn chunks. Here the world spawn is at 308 64 392 and Ksyxis
// removes the spawn chunks anyway, so the chunk at the origin stays idle unless a player stands near it.
//
// The overworld Stray boss rides a spider. A mob rider switches off the walking goals of its mount, so
// players met a spider that did not walk, while the Stray waited at the origin without ever ticking
// (Spigot.ticksLived 0, its glowing effect still at 3600 ticks). When the spider died or despawned, the
// Stray dropped to x 0, z 0 at the height of the spider. By 2026-11-03 21:04 twelve boss Strays stood
// there.
//
// When a Stray joins a level as a passenger more than 8 blocks away from its vehicle, the vehicle places
// it on itself. Forge fires the join event before the game puts the entity into a chunk section, so the
// rider lands in the section of its mount and ticks from then on. Riders that already sit on their
// vehicle are left alone.

var ApothRidersMaxDistanceSqr = 64
var apothRidersPlaced = 0
var apothRidersErrors = 0

function apothRidersCall(object, name) {
  // KubeJS turns some getters into properties with the same name as the vanilla method.
  return typeof object[name] == 'function' ? object[name]() : object[name]
}

function apothRidersError(e) {
  if (apothRidersErrors++ < 5) console.error('Apotheosis riders: placing a rider failed: ' + e)
}

function apothRidersPlace(event) {
  if (apothRidersErrors >= 5) return
  try {
    var rider = event.getEntity()
    var vehicle = rider.getVehicle()
    if (vehicle == null) return
    if (rider.distanceToSqr(vehicle) <= ApothRidersMaxDistanceSqr) return
    var from = Math.floor(rider.getX()) + ' ' + Math.floor(rider.getY()) + ' ' + Math.floor(rider.getZ())
    vehicle.positionRider(rider)
    apothRidersPlaced++
    if (apothRidersPlaced <= 100) {
      console.info('Apotheosis riders: placed ' + rider.getEncodeId() + ' from ' + from + ' onto ' +
        vehicle.getEncodeId() + ' at ' + Math.floor(vehicle.getX()) + ' ' + Math.floor(vehicle.getY()) + ' ' +
        Math.floor(vehicle.getZ()) + ' in ' + String(apothRidersCall(event.getLevel(), 'dimension')))
    }
  } catch (e) {
    apothRidersError(e)
  }
}

EntityEvents.spawned('minecraft:stray', apothRidersPlace)

console.info('Apotheosis riders: active')
