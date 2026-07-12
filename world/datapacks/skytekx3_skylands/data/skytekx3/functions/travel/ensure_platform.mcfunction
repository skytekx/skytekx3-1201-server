# retry loop, places the 3x3 arrival platform once the chunk is server loaded
scoreboard players remove @s sktx_pend 1
execute if predicate skytekx3:travel/in_skylands if loaded ~ ~ ~ run fill ~-1 0 ~-1 ~1 0 ~1 minecraft:dirt
execute if predicate skytekx3:travel/in_skylands if loaded ~ ~ ~ run scoreboard players reset @s sktx_pend
