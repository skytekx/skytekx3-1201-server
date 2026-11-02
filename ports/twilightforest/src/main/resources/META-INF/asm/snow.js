// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Backport of TF 4.8's KeepGrassSnowyForSnowloggableBlocksTransformer: grass and podzol under a berry bush that
// holds snow layers keep their snowy texture.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'snow': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.SnowyDirtBlock',
                'methodName': ASM.mapMethod('m_154648_'), // isSnowySetting
                'methodDesc': '(Lnet/minecraft/world/level/block/state/BlockState;)Z'
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
                                'snowySetting',
                                '(ZLnet/minecraft/world/level/block/state/BlockState;)Z',
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
