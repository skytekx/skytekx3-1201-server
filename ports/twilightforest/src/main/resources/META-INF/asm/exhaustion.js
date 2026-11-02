// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Efficient Eater (Traveller's Vest): reduces the food exhaustion of moving and jumping. Port of 4.8's tf-asm
// ReduceMovementFoodExhaustionTransformer; 1.20.1 keeps checkMovementStatistics in Player, 1.21 moved it to ServerPlayer.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'efficient_eater_movement': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.player.Player',
                'methodName': ASM.mapMethod('m_36378_'), // checkMovementStatistics
                'methodDesc': '(DDD)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                return reduceFoodExhaustion(methodNode, 'checkMovementStatistics');
            }
        },
        'efficient_eater_jump': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.player.Player',
                'methodName': ASM.mapMethod('m_6135_'), // jumpFromGround
                'methodDesc': '()V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                return reduceFoodExhaustion(methodNode, 'jumpFromGround');
            }
        }
    }
}

// this.causeFoodExhaustion(f); -> this.causeFoodExhaustion(PlayerHooks.getFoodExhaustion(f, this));
function reduceFoodExhaustion(/*org.objectweb.asm.tree.MethodNode*/ methodNode, /*String*/ name) {
    var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
    var causeFoodExhaustion = ASM.mapMethod('m_36399_'); // causeFoodExhaustion
    var calls = [];
    for (var index = 0; index < instructions.size(); index++) {
        var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
        if (node.getOpcode() === Opcodes.INVOKEVIRTUAL
            && (equate(node.owner, 'net/minecraft/world/entity/player/Player') || equate(node.owner, 'net/minecraft/server/level/ServerPlayer'))
            && equate(node.name, causeFoodExhaustion)
            && equate(node.desc, '(F)V')) {
            calls.push(node);
        }
    }
    if (calls.length === 0) {
        ASM.log('ERROR', 'Twilight Forest exhaustion.js: Player#' + name + ' has no causeFoodExhaustion call, Efficient Eater will not reduce its exhaustion');
        return methodNode;
    }
    calls.forEach(function (/*org.objectweb.asm.tree.MethodInsnNode*/ value, index, array) {
        instructions.insertBefore(
            value,
            ASM.listOf(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    'twilightforest/asmhooks/PlayerHooks',
                    'getFoodExhaustion',
                    '(FLnet/minecraft/world/entity/player/Player;)F',
                    false
                    )
                )
            );
    });
    return methodNode;
}

function equate(/*java.lang.Object*/ a, b) {
    return a.equals(b);
}
