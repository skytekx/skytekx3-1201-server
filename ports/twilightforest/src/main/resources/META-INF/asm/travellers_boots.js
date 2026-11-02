// noinspection ES6ConvertVarToLetConst

var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var ENTITY = 'net/minecraft/world/entity/Entity';
var LOCAL_PLAYER = 'net/minecraft/client/player/LocalPlayer';
var ENTITY_HOOKS = 'twilightforest/asmhooks/EntityHooks';
var BLOCK_HOOKS = 'twilightforest/asmhooks/BlockHooks';

// Traveller's Boots modifiers Water Walk and Unrestrained. Ports of TF 4.8's tf-asm transformers WaterWalkTransformer,
// UnrestrainedBlockSpeedAndJumpFactorTransformer, ResetStuckUnrestrainedTransformer, WaterSprintTransformer,
// UnrestrainedFrictionTransformer, SlimeBlockMomentumTransformer and SlimeBlockBounceUpTransformer.
// LocalPlayer is client-only, so water_sprint never fires on a dedicated server.
// noinspection JSUnusedGlobalSymbols
function initializeCoreMod() {
    return {
        'travellers_boots_water_walk': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.LivingEntity',
                'methodName': ASM.mapMethod('m_203441_'), // canStandOnFluid
                'methodDesc': '(Lnet/minecraft/world/level/material/FluidState;)Z'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                // return <o>; -> return EntityHooks.processWaterWalking(<o>, this, fluidState);
                findOpcodes(methodNode, Opcodes.IRETURN).forEach(function (/*org.objectweb.asm.tree.InsnNode*/ value, index, array) {
                    methodNode.instructions.insertBefore(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new VarInsnNode(Opcodes.ALOAD, 1),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                ENTITY_HOOKS,
                                'processWaterWalking',
                                '(ZLnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/material/FluidState;)Z',
                                false
                                )
                            )
                        );
                });
                return methodNode;
            }
        },
        'travellers_boots_block_jump_factor': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.Entity',
                'methodName': ASM.mapMethod('m_20098_'), // getBlockJumpFactor
                'methodDesc': '()F'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                return resetFactorWithUnrestrained(methodNode);
            }
        },
        'travellers_boots_block_speed_factor': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.Entity',
                'methodName': ASM.mapMethod('m_6041_'), // getBlockSpeedFactor
                'methodDesc': '()F'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                return resetFactorWithUnrestrained(methodNode);
            }
        },
        'travellers_boots_reset_stuck': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.Entity',
                'methodName': ASM.mapMethod('m_6478_'), // move
                'methodDesc': '(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                // this.stuckSpeedMultiplier -> EntityHooks.resetStuckUnrestrained(this).stuckSpeedMultiplier
                var fieldName = ASM.mapField('f_19865_'); // stuckSpeedMultiplier
                var reads = [];
                var instructions = methodNode.instructions;
                for (var index = 0; index < instructions.size(); index++) {
                    var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
                    if (node.getOpcode() === Opcodes.GETFIELD && equate(node.owner, ENTITY) && equate(node.name, fieldName)) {
                        reads.push(node);
                    }
                }
                if (reads.length === 0) {
                    ASM.log('ERROR', 'Twilight Forest travellers_boots.js: Entity#move reads no stuckSpeedMultiplier, Unrestrained boots stay slowed by cobwebs on the server');
                }
                reads.forEach(function (/*org.objectweb.asm.tree.FieldInsnNode*/ value, index, array) {
                    instructions.insertBefore(
                        value,
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            ENTITY_HOOKS,
                            'resetStuckUnrestrained',
                            '(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity;',
                            false
                            )
                        );
                });
                return methodNode;
            }
        },
        'travellers_boots_water_sprint': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.player.LocalPlayer',
                'methodName': ASM.mapMethod('m_8107_'), // aiStep
                'methodDesc': '()V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                var isInWater = ASM.mapMethod('m_20069_'); // isInWater
                var waterChecks = [];
                var fluidChecks = [];
                var instructions = methodNode.instructions;
                for (var index = 0; index < instructions.size(); index++) {
                    var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
                    if (node.getOpcode() !== Opcodes.INVOKEVIRTUAL || !equate(node.owner, LOCAL_PLAYER)) {
                        continue;
                    }
                    if (equate(node.name, isInWater) && equate(node.desc, '()Z')) {
                        waterChecks.push(node);
                    } else if (equate(node.name, 'isInFluidType') && equate(node.desc, '(Ljava/util/function/BiPredicate;)Z')) { // added by Forge, no SRG name
                        fluidChecks.push(node);
                    }
                }
                // this.isInWater() -> EntityHooks.unrestrainedSprintingInWater(this.isInWater(), this)
                waterChecks.forEach(function (/*org.objectweb.asm.tree.MethodInsnNode*/ value, index, array) {
                    instructions.insert(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                ENTITY_HOOKS,
                                'unrestrainedSprintingInWater',
                                '(ZLnet/minecraft/world/entity/LivingEntity;)Z',
                                false
                                )
                            )
                        );
                });
                // this.isInFluidType(predicate) -> this.isInFluidType(EntityHooks.unrestrainedSwimPredicate(predicate, this))
                fluidChecks.forEach(function (/*org.objectweb.asm.tree.MethodInsnNode*/ value, index, array) {
                    instructions.insertBefore(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 0),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                ENTITY_HOOKS,
                                'unrestrainedSwimPredicate',
                                '(Ljava/util/function/BiPredicate;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/function/BiPredicate;',
                                false
                                )
                            )
                        );
                });
                return methodNode;
            }
        },
        'travellers_boots_friction': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraftforge.common.extensions.IForgeBlock',
                'methodName': 'getFriction', // added by Forge, no SRG name
                'methodDesc': '(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                // return <o>; -> return BlockHooks.resetBlockFrictionWithUnrestrained(<o>, entity);
                findOpcodes(methodNode, Opcodes.FRETURN).forEach(function (/*org.objectweb.asm.tree.InsnNode*/ value, index, array) {
                    methodNode.instructions.insertBefore(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 4),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                BLOCK_HOOKS,
                                'resetBlockFrictionWithUnrestrained',
                                '(FLnet/minecraft/world/entity/Entity;)F',
                                false
                                )
                            )
                        );
                });
                return methodNode;
            }
        },
        'travellers_boots_slime_momentum': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.SlimeBlock',
                'methodName': ASM.mapMethod('m_141947_'), // stepOn
                'methodDesc': '(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                var isSteppingCarefully = ASM.mapMethod('m_20161_'); // isSteppingCarefully
                var checks = [];
                var instructions = methodNode.instructions;
                for (var index = 0; index < instructions.size(); index++) {
                    var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
                    if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && equate(node.owner, ENTITY) && equate(node.name, isSteppingCarefully) && equate(node.desc, '()Z')) {
                        checks.push(node);
                    }
                }
                // entity.isSteppingCarefully() -> BlockHooks.resetSlimeMomentumWithUnrestrained(entity.isSteppingCarefully(), entity)
                checks.forEach(function (/*org.objectweb.asm.tree.MethodInsnNode*/ value, index, array) {
                    instructions.insert(
                        value,
                        ASM.listOf(
                            new VarInsnNode(Opcodes.ALOAD, 4),
                            new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                BLOCK_HOOKS,
                                'resetSlimeMomentumWithUnrestrained',
                                '(ZLnet/minecraft/world/entity/Entity;)Z',
                                false
                                )
                            )
                        );
                });
                return methodNode;
            }
        },
        'travellers_boots_slime_bounce': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.level.block.SlimeBlock',
                'methodName': ASM.mapMethod('m_56403_'), // bounceUp
                'methodDesc': '(Lnet/minecraft/world/entity/Entity;)V'
            },
            'transformer': function (/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
                // Vec3 vec3 = entity.getDeltaMovement(); -> BlockHooks.stopBouncing(entity); Vec3 vec3 = entity.getDeltaMovement();
                var /*org.objectweb.asm.tree.MethodInsnNode*/ target = ASM.findFirstMethodCall(
                    methodNode,
                    ASM.MethodType.VIRTUAL,
                    ENTITY,
                    ASM.mapMethod('m_20184_'), // getDeltaMovement
                    '()Lnet/minecraft/world/phys/Vec3;'
                    );
                if (target === null) {
                    ASM.log('ERROR', 'Twilight Forest travellers_boots.js: SlimeBlock#bounceUp has an unexpected shape, slime blocks keep bouncing Unrestrained boots');
                    return methodNode;
                }
                methodNode.instructions.insertBefore(
                    target,
                    ASM.listOf(
                        new VarInsnNode(Opcodes.ALOAD, 1),
                        new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            BLOCK_HOOKS,
                            'stopBouncing',
                            '(Lnet/minecraft/world/entity/Entity;)V',
                            false
                            )
                        )
                    );
                return methodNode;
            }
        }
    }
}

// return <o>; -> return EntityHooks.resetFactorWithUnrestrained(<o>, this);
function resetFactorWithUnrestrained(/*org.objectweb.asm.tree.MethodNode*/ methodNode) {
    findOpcodes(methodNode, Opcodes.FRETURN).forEach(function (/*org.objectweb.asm.tree.InsnNode*/ value, index, array) {
        methodNode.instructions.insertBefore(
            value,
            ASM.listOf(
                new VarInsnNode(Opcodes.ALOAD, 0),
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    ENTITY_HOOKS,
                    'resetFactorWithUnrestrained',
                    '(FLnet/minecraft/world/entity/Entity;)F',
                    false
                    )
                )
            );
    });
    return methodNode;
}

function findOpcodes(/*org.objectweb.asm.tree.MethodNode*/ methodNode, opcode) {
    var found = [];
    var instructions = methodNode.instructions;
    for (var index = 0; index < instructions.size(); index++) {
        var /*org.objectweb.asm.tree.AbstractInsnNode*/ node = instructions.get(index);
        if (node.getOpcode() === opcode) {
            found.push(node);
        }
    }
    return found;
}

function equate(/*java.lang.Object*/ a, b) {
    return a.equals(b);
}
