// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Emperor's Cloth: shrouded armor, elytra and the cape behind a shrouded elytra.
// All three targets are client-only renderer classes, so these transformers never fire on a dedicated server.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'armor_rendering': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer',
                'methodName': ASM.mapMethod('m_117118_'), // renderArmorPiece
                'methodDesc': '(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
                // ItemStack stack = entity.getItemBySlot(slot); is the first local the method stores (slot 7 in 1.20.1)
                var /*org.objectweb.asm.tree.VarInsnNode*/ stackStore = ASM.findFirstInstruction(methodNode, Opcodes.ASTORE);
                // if (stack.getItem() instanceof ArmorItem armoritem) -> if (cancelArmorRendering(<instanceof>, stack))
                var /*org.objectweb.asm.tree.TypeInsnNode*/ armorCheck = ASM.findFirstInstruction(methodNode, Opcodes.INSTANCEOF);
                if (stackStore === null || armorCheck === null || !equate(armorCheck.desc, 'net/minecraft/world/item/ArmorItem')) {
                    ASM.log('ERROR', 'Twilight Forest armor.js: HumanoidArmorLayer#renderArmorPiece has an unexpected shape, Emperor\'s Cloth armor will still render');
                    return methodNode;
                }
                instructions.insert(
                    armorCheck,
                    ASM.listOf(
                        new VarInsnNode(Opcodes.ALOAD, stackStore.var),
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'twilightforest/asmhooks/ArmorHooks',
                            'cancelArmorRendering',
                            '(ZLnet/minecraft/world/item/ItemStack;)Z',
                            false
                            )
                        )
                    );
                return methodNode;
            }
        },
        'elytra_rendering': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.layers.ElytraLayer',
                'methodName': 'shouldRender', // added by Forge, no SRG name
                'methodDesc': '(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z'
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
                returns.forEach(function (/*org.objectweb.asm.tree.InsnNode*/ value, index, array) {
                    instructions.insertBefore(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 1),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                'twilightforest/asmhooks/ArmorHooks',
                                'cancelArmorRendering',
                                '(ZLnet/minecraft/world/item/ItemStack;)Z',
                                false
                                )
                            )
                        )
                });
                return methodNode;
            }
        },
        'cape_rendering': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.layers.CapeLayer',
                'methodName': ASM.mapMethod('m_6494_'), // render
                'methodDesc': '(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
                // if (!chest.is(Items.ELYTRA)) -> if (!fixCapeRendering(chest.is(Items.ELYTRA), chest))
                var /*org.objectweb.asm.tree.MethodInsnNode*/ elytraCheck = ASM.findFirstMethodCall(
                    methodNode,
                    ASM.MethodType.VIRTUAL,
                    'net/minecraft/world/item/ItemStack',
                    ASM.mapMethod('m_150930_'), // is(Item)
                    '(Lnet/minecraft/world/item/Item;)Z'
                    );
                // the chest stack is loaded right before Items.ELYTRA (slot 11 in 1.20.1)
                var /*org.objectweb.asm.tree.VarInsnNode*/ stackLoad = elytraCheck === null ? null : elytraCheck.getPrevious().getPrevious();
                if (stackLoad === null || stackLoad.getOpcode() !== Opcodes.ALOAD) {
                    ASM.log('ERROR', 'Twilight Forest armor.js: CapeLayer#render has an unexpected shape, capes stay hidden behind Emperor\'s Cloth elytra');
                    return methodNode;
                }
                instructions.insert(
                    elytraCheck,
                    ASM.listOf(
                        new VarInsnNode(Opcodes.ALOAD, stackLoad.var),
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'twilightforest/asmhooks/ArmorHooks',
                            'fixCapeRendering',
                            '(ZLnet/minecraft/world/item/ItemStack;)Z',
                            false
                            )
                        )
                    );
                return methodNode;
            }
        }
    }
}

function equate(/*java.lang.Object*/ a, b) {
    return a.equals(b);
}
