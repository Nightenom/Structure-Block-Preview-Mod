function initializeCoreMod() {
    return {
        'structure-block-tile-entity-set-mode': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.tileentity.StructureBlockTileEntity',
                'methodName': 'func_184405_a', // setMode
                'methodDesc': '(Lnet/minecraft/state/properties/StructureMode;)V'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

                var newInstructions = new InsnList();
                newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                newInstructions.add(ASMAPI.buildMethodCall(
                    "com/ldtteam/sbpm/CoremodListeners",
                    "sbteNextMode",
                    "(Lnet/minecraft/state/properties/StructureMode;)V",
                    ASMAPI.MethodType.STATIC
                ));
                method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);

                return method;
            }
        },
        'edit-structure-screen-done-pressed': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.gui.screen.EditStructureScreen',
                'methodName': 'func_195275_h', // func_195275_h
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var InsnList = Java.type('org.objectweb.asm.tree.InsnList');

                var newInstructions = new InsnList();
                newInstructions.add(ASMAPI.buildMethodCall(
                    "com/ldtteam/sbpm/CoremodListeners",
                    "essDonePressed",
                    "()V",
                    ASMAPI.MethodType.STATIC
                ));
                method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);

                return method;
            }
        }
    }
}