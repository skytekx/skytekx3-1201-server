// SkyTekx3: no Read Me note of The One Probe.
//
// The One Probe 10.0.4 puts its Read Me note into the inventory at the first login and marks the player
// in a capability. Its clone handler copies that mark from the dead player without reviving the old
// capabilities first, so the mark is lost at every death and the next login brings another note. The
// owner asked on 2026-11-04 to stop the notes ("spawn thousands"). spawnNote = false in
// config/theoneprobe-common.toml stops new notes from the next server start on. Until then, and for the
// notes that players carry already, this script removes every note from the inventory and the ender chest
// at login and from every chest that a player opens.

const ProbeNoteId = 'theoneprobe:probenote'
var probeNoteLogged = 0
var probeNoteErrors = 0

function probeNoteError(what, e) {
  if (probeNoteErrors++ < 5) console.error('Probe note: ' + what + ' failed: ' + e)
}

function probeNoteClear(container, where) {
  var removed = 0
  for (var i = 0; i < container.getContainerSize(); i++) {
    var stack = container.getItem(i)
    if (stack == null || stack.isEmpty() || String(stack.id) != ProbeNoteId) continue
    removed += stack.count
    container.setItem(i, Item.empty)
  }
  if (removed > 0) {
    container.setChanged()
    if (probeNoteLogged++ < 50) console.info('Probe note: removed ' + removed + ' from ' + where)
  }
  return removed
}

function probeNotePlayer(player) {
  var name = String(player.username)
  probeNoteClear(player.inventory, 'the inventory of ' + name)
  probeNoteClear(player.getEnderChestInventory(), 'the ender chest of ' + name)
}

// The One Probe gives the note in its own login handler, so look one second later.
PlayerEvents.loggedIn(event => {
  var player = event.player
  Utils.server.scheduleInTicks(20, () => {
    try {
      probeNotePlayer(player)
    } catch (e) {
      probeNoteError('login', e)
    }
  })
})

PlayerEvents.chestOpened(event => {
  try {
    probeNoteClear(event.inventory, 'a chest opened by ' + String(event.player.username))
  } catch (e) {
    probeNoteError('chest', e)
  }
})

// After a script reload on a running server, clean the players who are online already.
if (Utils.server != null) {
  Utils.server.scheduleInTicks(20, () => {
    var players = Utils.server.getPlayerList().getPlayers()
    for (var i = 0; i < players.size(); i++) {
      try {
        probeNotePlayer(players.get(i))
      } catch (e) {
        probeNoteError('reload', e)
      }
    }
  })
}

console.info('Probe note: active')
