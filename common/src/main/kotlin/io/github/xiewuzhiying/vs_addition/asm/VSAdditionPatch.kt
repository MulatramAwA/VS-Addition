package io.github.xiewuzhiying.vs_addition.asm

import com.chocohead.mm.api.ClassTinkerers
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

object VSAdditionPatch {
    @JvmStatic
    fun run() {
        valkyrienskiesPatch()
        tallyhoPatch()
    }

    @JvmStatic
    fun tallyhoPatch() {
        ClassTinkerers.addTransformation(
            "edn.stratodonut.tallyho.client.ClientEvents"
        ) { classNode: ClassNode ->
            classNode.methods.removeIf { method: MethodNode ->
                method.name.equals("onBleedout")
            }
        }
    }

    @JvmStatic
    fun valkyrienskiesPatch() {
        ClassTinkerers.addTransformation("org.valkyrienskies.mod.common.ValkyrienSkiesMod") { classNode: ClassNode ->
            classNode.methods.forEach { methodNode: MethodNode ->
                if (methodNode.name.contains("init")) {
                    val instructions = methodNode.instructions
                    val iterator = instructions.iterator()
                    while (iterator.hasNext()) {
                        val insn = iterator.next()
                        if (insn is MethodInsnNode) {
                            if (insn.owner == "org/valkyrienskies/mod/common/BlockStateInfo" &&
                                insn.name == "init" &&
                                insn.desc == "()V"
                            ) {
                                iterator.remove()
                            }
                        }
                    }
                }
            }
        }
    }
}