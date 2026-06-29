# Remember the player's overworld entry position (for the return trip)
execute store result score @s sktx_ex run data get entity @s Pos[0] 1
execute store result score @s sktx_ey run data get entity @s Pos[1] 1
execute store result score @s sktx_ez run data get entity @s Pos[2] 1
# Guarantee a safe 3x3 obsidian landing platform at (0,60,0) in the Skylands (column is now 0..96)
execute in skytekx3:skylands run forceload add 0 0
execute in skytekx3:skylands run fill -1 60 -1 1 60 1 minecraft:obsidian keep
# Teleport in, then drop the forceload
execute in skytekx3:skylands run tp @s 0.5 61 0.5
execute in skytekx3:skylands run forceload remove 0 0
tellraw @s {"text":"Whoosh! Welcome to the Skylands. Use /trigger sktx_overworld to return.","color":"aqua"}
