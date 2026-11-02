// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Melee hits with a CustomDamageProvider in the main hand (Stale Bread) use the item's own damage source
// instead of minecraft:mob_attack / minecraft:player_attack. Port of 4.8's tf-asm DamageSourcesTransformer.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'damagesources_mob_attack': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.damagesource.DamageSources',
                'methodName': ASM.mapMethod('m_269333_'), // mobAttack
                'methodDesc': '(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                return wrapReturnedSource(methodNode);
            }
        },
        'damagesources_player_attack': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.damagesource.DamageSources',
                'methodName': ASM.mapMethod('m_269075_'), // playerAttack
                'methodDesc': '(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/damagesource/DamageSource;'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                return wrapReturnedSource(methodNode);
            }
        }
    }
}

// return <source>; -> return DamageSourceHooks.getCustomDamageSource(<source>, attacker);
function wrapReturnedSource(/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
    var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
    var returns = [];
    for (var index = 0; index < instructions.size(); index++) {
        var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
        if (node.getOpcode() === Opcodes.ARETURN) {
            returns.push(node);
        }
    }
    returns.forEach(function (/*org.objectweb.asm.tree.InsnNode*/ value, index, array) {
        instructions.insertBefore(
            value,
            ASM.listOf(
                new VarInsnNode(Opcodes.ALOAD, 1), // the attacking LivingEntity / Player parameter
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    'twilightforest/asmhooks/DamageSourceHooks',
                    'getCustomDamageSource',
                    '(Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;',
                    false
                    )
                )
            );
    });
    return methodNode;
}
