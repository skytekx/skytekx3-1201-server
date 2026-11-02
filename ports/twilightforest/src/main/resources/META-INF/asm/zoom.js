// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

// Zoom (Traveller's Goggles): slows the mouse down while zooming. 4.8 listens to NeoForge's CalculatePlayerTurnEvent,
// which replaces the mouse sensitivity option MouseHandler#turnPlayer reads; Forge 47 has no such event.
// The target is a client-only class, so this transformer never fires on a dedicated server.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'zoom_sensitivity': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.MouseHandler',
                'methodName': ASM.mapMethod('m_91523_'), // turnPlayer
                'methodDesc': '()V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
                // double d = this.minecraft.options.sensitivity().get() * 0.6 + 0.2;
                //   -> double d = TravellersClientEvents.modifyMouseSensitivity(this.minecraft.options.sensitivity().get()) * 0.6 + 0.2;
                var /*org.objectweb.asm.tree.MethodInsnNode*/ sensitivity = ASM.findFirstMethodCall(
                    methodNode,
                    ASM.MethodType.VIRTUAL,
                    'net/minecraft/client/Options',
                    ASM.mapMethod('m_231964_'), // sensitivity
                    '()Lnet/minecraft/client/OptionInstance;'
                    );
                var /*org.objectweb.asm.tree.AbstractInsnNode*/ unboxing = sensitivity === null ? null : sensitivity.getNext();
                while (unboxing !== null && !(unboxing.getOpcode() === Opcodes.INVOKEVIRTUAL && equate(unboxing.owner, 'java/lang/Double') && equate(unboxing.name, 'doubleValue'))) {
                    unboxing = unboxing.getNext();
                }
                if (unboxing === null) {
                    ASM.log('ERROR', 'Twilight Forest zoom.js: MouseHandler#turnPlayer has an unexpected shape, the Traveller\'s Goggles zoom will not slow the mouse down');
                    return methodNode;
                }
                instructions.insert(
                    unboxing,
                    new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        'twilightforest/client/TravellersClientEvents',
                        'modifyMouseSensitivity',
                        '(D)D',
                        false
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
