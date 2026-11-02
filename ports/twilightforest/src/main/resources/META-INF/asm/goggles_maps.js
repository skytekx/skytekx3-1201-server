// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

var INVENTORY = 'net/minecraft/world/entity/player/Inventory';

// Item Display (Traveller's Goggles): a map shown by the goggles counts as carried, so the wearer keeps their marker on
// it and keeps receiving its updates. Port of 4.8's tf-asm UpdateMapsInGogglesTransformer, which hooks the 1.21
// Inventory#contains(Predicate) calls of MapItemSavedData#tickCarriedBy; 1.20.1 calls Inventory#contains(ItemStack).
// The inventory is duplicated before each call, so the hook asks the goggles of the inventory's own player: the ticking
// player for the first call, each holding player in the loop (4.8 passes the ticking player to both).
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'goggles_maps_tick_carried_by': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.saveddata.maps.MapItemSavedData',
                'methodName': ASM.mapMethod('m_77918_'), // tickCarriedBy
                'methodDesc': '(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                // inventory.contains(stack) -> MapHooks.updateMapsInGoggles(inventory, stack, inventory.contains(stack))
                var /*org.objectweb.asm.tree.InsnList*/ instructions = methodNode.instructions;
                var contains = ASM.mapMethod('m_36063_'); // contains
                var calls = [];
                for (var index = 0; index < instructions.size(); index++) {
                    var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
                    if (node.getOpcode() === Opcodes.INVOKEVIRTUAL
                        && equate(node.owner, INVENTORY)
                        && equate(node.name, contains)
                        && equate(node.desc, '(Lnet/minecraft/world/item/ItemStack;)Z')) {
                        calls.push(node);
                    }
                }
                if (calls.length === 0) {
                    ASM.log('ERROR', 'Twilight Forest goggles_maps.js: MapItemSavedData#tickCarriedBy has no Inventory#contains call, maps in the Traveller\'s Goggles will not keep their wearer\'s marker');
                    return methodNode;
                }
                calls.forEach(function (/*org.objectweb.asm.tree.MethodInsnNode*/ value, index, array) {
                    instructions.insertBefore(value, new InsnNode(Opcodes.DUP2));
                    instructions.insert(
                        value,
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'twilightforest/asmhooks/MapHooks',
                            'updateMapsInGoggles',
                            '(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;Z)Z',
                            false
                            )
                        );
                });
                return methodNode;
            }
        }
    }
}

function equate(/*java.lang.Object*/ a, b) {
    return a.equals(b);
}
