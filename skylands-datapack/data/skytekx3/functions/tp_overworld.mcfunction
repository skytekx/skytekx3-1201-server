# Return to the stored overworld entry position
execute in minecraft:overworld run tp @s 0 0 0
execute store result entity @s Pos[0] double 1 run scoreboard players get @s sktx_ex
execute store result entity @s Pos[1] double 1 run scoreboard players get @s sktx_ey
execute store result entity @s Pos[2] double 1 run scoreboard players get @s sktx_ez
tellraw @s {"text":"Back to the overworld.","color":"green"}
