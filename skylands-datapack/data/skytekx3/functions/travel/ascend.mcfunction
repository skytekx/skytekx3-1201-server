# crossed the overworld build ceiling, arrive on a dirt platform at the skylands floor
scoreboard players set @s sktx_cd 40
scoreboard players set @s sktx_pend 100
execute in skytekx3:skylands run tp @s ~ 1 ~
tellraw @s {"text":"You ascended into the Skylands!","color":"aqua"}
execute at @s run function skytekx3:travel/ensure_platform
