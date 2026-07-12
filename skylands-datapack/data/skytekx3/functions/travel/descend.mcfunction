# fell below the skylands floor, drop into the overworld sky at the same x z
scoreboard players set @s sktx_cd 40
execute in minecraft:overworld run tp @s ~ 300 ~
tellraw @s {"text":"You fell from the Skylands...","color":"aqua"}
