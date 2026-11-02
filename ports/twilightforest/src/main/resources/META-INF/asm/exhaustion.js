// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var PLAYER_HOOKS = 'twilightforest/asmhooks/PlayerHooks';

// Efficient Eater (Traveller's Vest): reduces the food exhaustion of moving and jumping. Port of 4.8's tf-asm
// ReduceMovementFoodExhaustionTransformer; 1.20.1 keeps checkMovementStatistics in Player, 1.21 moved it to ServerPlayer.
// The jump exhaustion is reduced inside causeFoodExhaustion instead of before its calls in jumpFromGround: Arclight's
// PlayerMixin replaces that argument with the Spigot world config's jump exhaustion (@ModifyArg, applied after the JS
// coremods), which would throw a value reduced at the call site away. jumpFromGround only marks the jumping player.
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
                // PlayerHooks.beginJump(this); <body>; PlayerHooks.endJump(); before every return
                var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
                var returns = [];
                for (var index = 0; index < instructions.size(); index++) {
                    var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
                    if (node.getOpcode() === Opcodes.RETURN)
                        returns.push(node);
                }
                returns.forEach(function (/*org.objectweb.asm.tree.InsnNode*/ value, index, array) {
                    instructions.insertBefore(
                        value,
                        new MethodInsnNode(Opcodes.INVOKESTATIC, PLAYER_HOOKS, 'endJump', '()V', false)
                        );
                });
                instructions.insert(
                    ASM.listOf(
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            PLAYER_HOOKS,
                            'beginJump',
                            '(Lnet/minecraft/world/entity/player/Player;)V',
                            false
                            )
                        )
                    );
                return methodNode;
            }
        },
        'efficient_eater_jump_exhaustion': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.player.Player',
                'methodName': ASM.mapMethod('m_36399_'), // causeFoodExhaustion
                'methodDesc': '(F)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                // f = PlayerHooks.getJumpFoodExhaustion(f, this); <body>
                methodNode.instructions.insert(
                    ASM.listOf(
                        new VarInsnNode(Opcodes.FLOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 0),
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            PLAYER_HOOKS,
                            'getJumpFoodExhaustion',
                            '(FLnet/minecraft/world/entity/player/Player;)F',
                            false
                            ),
                        new VarInsnNode(Opcodes.FSTORE, 1)
                        )
                    );
                return methodNode;
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
                    PLAYER_HOOKS,
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
