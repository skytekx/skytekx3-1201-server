// SkyTekx3: Apotheosis bosses keep their riders and minibosses keep their passengers.
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
//
// The Withering Archer miniboss (2 percent of the skeletons and wither skeletons that spawn naturally in
// the nether) carries an area effect cloud of wither with a radius of 2.5 blocks as a passenger in its
// miniboss NBT. Apotheosis turns the mob into the miniboss in its own join event and mounts the cloud
// there. The spawn adds the mob with addFreshEntityWithPassengers, which reads the passenger list of the
// mob before that join event runs, so the cloud never joins the level. It rides the archer only in
// memory, never ticks and never gives wither. On the test server on 2026-11-04 such a cloud kept Age 0,
// its own position stayed at x 0, y 0, z 0, and a zombie standing in it got no effect.
//
// One tick after a skeleton or wither skeleton joins a level, every passenger of it that is not part of
// the level joins the level on the seat of its vehicle. The check waits a tick because the KubeJS spawn
// event runs before Apotheosis turns the mob into the miniboss. It cannot test the miniboss marker either,
// because persistentData in KubeJS is the KubeJS data of the entity and not its ForgeData. A chunk that
// loads from disk brings the saved passengers into the level together with the mob, so they are found in
// the level and get no copy. When the archer dies, Apotheosis discards its cloud. When it despawns, the
// startup script kubejs/startup_scripts/apotheosis_passengers.js does.

var ApothRidersMaxDistanceSqr = 64
var apothRidersPlaced = 0
var apothRidersErrors = 0
var apothPassengersAdded = 0
var apothPassengersErrors = 0

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

function apothPassengersError(e) {
  if (apothPassengersErrors++ < 5) console.error('Apotheosis riders: adding a passenger failed: ' + e)
}

function apothPassengersAdd(level, mob) {
  if (apothPassengersErrors >= 5) return
  try {
    if (mob.isRemoved()) return
    var passengers = mob.getPassengers()
    for (var i = 0; i < passengers.size(); i++) {
      var passenger = passengers.get(i)
      if (passenger.isRemoved() || level.getEntity(passenger.getUuid()) != null) continue
      mob.positionRider(passenger)
      if (!level.addFreshEntity(passenger)) continue
      apothPassengersAdded++
      if (apothPassengersAdded <= 100) {
        console.info('Apotheosis riders: added the ' + passenger.getEncodeId() + ' of ' + mob.getEncodeId() + ' at ' +
          Math.floor(mob.getX()) + ' ' + Math.floor(mob.getY()) + ' ' + Math.floor(mob.getZ()) + ' in ' +
          String(apothRidersCall(level, 'dimension')))
      }
    }
  } catch (e) {
    apothPassengersError(e)
  }
}

function apothPassengersCheck(event) {
  if (apothPassengersErrors >= 5) return
  try {
    var server = Utils.server
    if (server == null) return
    var mob = event.getEntity()
    var level = event.getLevel()
    server.scheduleInTicks(1, () => apothPassengersAdd(level, mob))
  } catch (e) {
    apothPassengersError(e)
  }
}

EntityEvents.spawned('minecraft:stray', apothRidersPlace)
EntityEvents.spawned('minecraft:wither_skeleton', apothPassengersCheck)
EntityEvents.spawned('minecraft:skeleton', apothPassengersCheck)

console.info('Apotheosis riders: active')
