// SkyTekx3: one item per material.
//
// Almost Unified (config/almostunified/unify.json) makes every recipe give the preferred item of a
// material and accept all of its variants. Ores, block drops, chest loot and items made earlier still
// come as the variant of the mod that made them, so two stacks of the same material did not stack. The
// owner asked on 2026-11-02 for one item per material.
//
// This script swaps every other variant of a unified material for the preferred item at the same count.
// It does so when the item lands in a player's inventory, when a player logs in (inventory and ender
// chest) and when a player opens a chest. It uses the replacement table of Almost Unified itself, so the
// result fits every recipe that the variant fitted. Items with NBT data stay as they are, and so does
// everything from Extreme Reactors, whose yellorium Almost Unified would turn into Mekanism uranium.
//
// The same swap turns the five old decoration blocks of Chocolate Quest into the normal blocks, which
// the owner asked for on 2026-11-04 at 17:33. The offline tool tools/replace_blocks.py converts the
// world itself, so this only catches stacks that it could not reach.

const UnifySkip = /^bigreactors:/
const UnifyOldBlocks = {
  'chocolate_quest:old_cobblestone': 'minecraft:cobblestone',
  'chocolate_quest:old_iron_block': 'minecraft:iron_block',
  'chocolate_quest:old_diamond_ore': 'minecraft:diamond_ore',
  'chocolate_quest:old_glass': 'minecraft:glass',
  'chocolate_quest:old_obsidian': 'minecraft:obsidian'
}
var unifyPending = {}
var unifyLogged = 0
var unifyErrors = 0

function unifyError(what, e) {
  if (unifyErrors++ < 5) console.error('Unify inventory: ' + what + ' failed: ' + e)
}

function unifyTarget(stack) {
  if (stack == null || stack.isEmpty() || stack.nbt != null) return null
  var id = String(stack.id)
  if (UnifyOldBlocks.hasOwnProperty(id)) return UnifyOldBlocks[id]
  if (UnifySkip.test(id)) return null
  var replacement = AlmostUnified.getReplacementForItem(stack)
  if (replacement == null || replacement.isEmpty()) return null
  var target = String(replacement.id)
  return target == id ? null : target
}

// Moves a swapped stack onto other stacks of the same item in the container, so it does not stay apart.
function unifyMerge(container, slot) {
  var stack = container.getItem(slot)
  var id = String(stack.id)
  for (var j = 0; j < container.getContainerSize() && stack.count > 0; j++) {
    if (j == slot) continue
    var other = container.getItem(j)
    if (other.isEmpty() || String(other.id) != id || other.nbt != null) continue
    var move = Math.min(other.getMaxStackSize() - other.count, stack.count)
    if (move <= 0) continue
    other.setCount(other.count + move)
    stack.setCount(stack.count - move)
  }
  if (stack.count <= 0) container.setItem(slot, Item.empty)
}

function unifyContainer(container, where) {
  var swapped = 0
  for (var i = 0; i < container.getContainerSize(); i++) {
    var stack = container.getItem(i)
    var target = unifyTarget(stack)
    if (target == null) continue
    var count = stack.count
    var from = String(stack.id)
    container.setItem(i, Item.of(target, count))
    swapped += count
    if (unifyLogged++ < 50) console.info('Unify inventory: ' + count + ' ' + from + ' became ' + target + ' in ' + where)
    try {
      unifyMerge(container, i)
    } catch (e) {
      unifyError('merge', e)
    }
  }
  if (swapped > 0) container.setChanged()
  return swapped
}

function unifyPlayer(player, withEnderChest) {
  var name = String(player.username)
  unifyContainer(player.inventory, 'the inventory of ' + name)
  if (withEnderChest) unifyContainer(player.getEnderChestInventory(), 'the ender chest of ' + name)
}

PlayerEvents.inventoryChanged(event => {
  try {
    if (unifyTarget(event.item) == null) return
    var player = event.player
    var key = String(player.uuid)
    if (unifyPending[key]) return
    unifyPending[key] = true
    // One tick later, outside the slot update that fired the event.
    Utils.server.scheduleInTicks(1, () => {
      unifyPending[key] = false
      try {
        unifyPlayer(player, false)
      } catch (e) {
        unifyError('inventory', e)
      }
    })
  } catch (e) {
    unifyError('inventory change', e)
  }
})

PlayerEvents.loggedIn(event => {
  var player = event.player
  Utils.server.scheduleInTicks(20, () => {
    try {
      unifyPlayer(player, true)
    } catch (e) {
      unifyError('login', e)
    }
  })
})

PlayerEvents.chestOpened(event => {
  try {
    unifyContainer(event.inventory, 'a chest opened by ' + String(event.player.username))
  } catch (e) {
    unifyError('chest', e)
  }
})

// After a script reload on a running server, check the players who are online already.
if (Utils.server != null) {
  Utils.server.scheduleInTicks(20, () => {
    var players = Utils.server.getPlayerList().getPlayers()
    for (var i = 0; i < players.size(); i++) {
      try {
        unifyPlayer(players.get(i), true)
      } catch (e) {
        unifyError('reload', e)
      }
    }
  })
}

console.info('Unify inventory: active')
