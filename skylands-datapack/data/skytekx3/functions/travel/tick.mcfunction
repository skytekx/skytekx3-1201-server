# skylands travel, replaces the retired SkylandsFallHandler from the skytekx mod
scoreboard players remove @a[scores={sktx_cd=1..}] sktx_cd 1
execute as @a[scores={sktx_pend=1..}] at @s run function skytekx3:travel/ensure_platform
execute as @a[predicate=skytekx3:travel/in_skylands_void] at @s unless score @s sktx_cd matches 1.. unless score @s sktx_pend matches 1.. run function skytekx3:travel/descend
execute as @a[predicate=skytekx3:travel/at_overworld_ceiling] at @s unless score @s sktx_cd matches 1.. run function skytekx3:travel/ascend
