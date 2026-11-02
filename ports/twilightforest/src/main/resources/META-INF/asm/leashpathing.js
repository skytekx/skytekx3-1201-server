// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Backport of TF 4.8's PathFinderUnrestrainedByLeashTransformer: the zombies that the Lich Tower leashes to its fences and posts
// stop pathing back to the knot while they are within 6 blocks of it, so they roam (and lunge) at the end of their lead.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'leashpathing': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.PathfinderMob',
                'methodName': ASM.mapMethod('m_213814_'), // shouldStayCloseToLeashHolder
                'methodDesc': '()Z'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
                var returns = [];
                for (var index = 0; index < instructions.size(); index++) {
                    var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
                    if (node.getOpcode() === Opcodes.IRETURN) {
                        returns.push(node);
                    }
                }
                returns.forEach(function (/*org.objectweb.asm.tree.AbstractInsnNode*/ value, index, array) {
                    instructions.insertBefore(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                'twilightforest/ASMHooks',
                                'overrideStayCloseToHolder',
                                '(ZLnet/minecraft/world/entity/PathfinderMob;)Z',
                                false
                            )
                        )
                    )
                });
                return methodNode;
            }
        }
    }
}
