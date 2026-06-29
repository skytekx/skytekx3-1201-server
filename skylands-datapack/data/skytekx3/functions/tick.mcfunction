# Re-enable triggers each tick + handle activations
scoreboard players enable @a sktx_skylands
scoreboard players enable @a sktx_overworld
execute as @a[scores={sktx_skylands=1..}] at @s run function skytekx3:tp_skylands
execute as @a[scores={sktx_overworld=1..,sktx_ey=1..}] at @s run function skytekx3:tp_overworld
scoreboard players set @a sktx_skylands 0
scoreboard players set @a sktx_overworld 0
