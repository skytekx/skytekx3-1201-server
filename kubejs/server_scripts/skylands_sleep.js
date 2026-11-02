// SkyTekx3: sleeping in the Skylands turns the overworld night into day.
//
// The Skylands share the clock of the overworld, but their dimension type fixes the time at noon. A level
// with a fixed time never counts as day, so players can lie down there at any hour. When they all sleep,
// vanilla sets the time of the Skylands only, and that time follows the overworld and ignores the change.
// The players wake up and the overworld night goes on (owner report of 2026-11-02).
//
// This script moves the overworld to the next morning once enough players in the Skylands sleep (the
// playersSleepingPercentage rule, counted among the players in the Skylands, as vanilla counts each
// dimension on its own) while it is night or a thunderstorm in the overworld. It also ends the rain, like a
// night slept through in the overworld. When the overworld is at day, the sleepers get the vanilla message.
//
// It triggers one tick before vanilla wakes the sleepers, at a sleep timer of 99 instead of 100, because
// vanilla wakes everybody in the Skylands in the tick in which the timers reach 100.
//
// Since 2026-11-02 the server runs with playersSleepingPercentage 0, so one sleeping player is enough, in the
// Skylands as in every other dimension. With the default of 100, a second player who was awake in the
// Skylands kept the night going, and the owner reported that sleeping there still did not work.

const SkylandsSleepGameRules = Java.loadClass('net.minecraft.world.level.GameRules')
const SkylandsSleepComponent = Java.loadClass('net.minecraft.network.chat.Component')
var skylandsSleepFailed = false

function skylandsSleepCall(object, name) {
  // KubeJS turns some getters into properties with the same name as the vanilla method.
  return typeof object[name] == 'function' ? object[name]() : object[name]
}

function skylandsSleepTick(level) {
  var players = skylandsSleepCall(level, 'players')
  var active = 0
  var sleeping = 0
  var deep = 0
  var i = 0
  var p = null
  for (i = 0; i < players.size(); i++) {
    p = players.get(i)
    if (p.isSpectator()) continue
    active++
    if (p.isSleeping()) {
      sleeping++
      if (p.getSleepTimer() >= 99) deep++
    }
  }
  if (deep == 0) return
  var overworld = skylandsSleepCall(level.getServer(), 'overworld')
  var rules = overworld.getGameRules()
  var needed = Math.max(1, Math.ceil(active * rules.getInt(SkylandsSleepGameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE) / 100))
  if (sleeping < needed || deep < needed) return
  if (overworld.isDay()) {
    for (i = 0; i < players.size(); i++) {
      p = players.get(i)
      if (p.isSleeping() && p.getSleepTimer() == 99) {
        p.displayClientMessage(SkylandsSleepComponent.translatable('block.minecraft.bed.no_sleep'), true)
      }
    }
    return
  }
  if (!rules.getBoolean(SkylandsSleepGameRules.RULE_DAYLIGHT)) return
  var next = overworld.getDayTime() + 24000
  overworld.setDayTime(next - next % 24000)
  if (rules.getBoolean(SkylandsSleepGameRules.RULE_WEATHER_CYCLE) && overworld.isRaining()) {
    overworld.setWeatherParameters(0, 0, false, false)
  }
  console.info('Skylands sleep: ' + deep + ' of ' + active + ' players in the Skylands slept, the overworld moved to the next morning')
}

LevelEvents.tick('skytekx3:skylands', event => {
  if (skylandsSleepFailed) return
  try {
    skylandsSleepTick(event.level)
  } catch (e) {
    skylandsSleepFailed = true
    console.error('Skylands sleep: disabled after an error: ' + e)
  }
})

console.info('Skylands sleep: active')
