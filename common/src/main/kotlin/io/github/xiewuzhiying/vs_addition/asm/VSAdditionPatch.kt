package io.github.xiewuzhiying.vs_addition.asm

import com.chocohead.mm.api.ClassTinkerers
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

object VSAdditionPatch {
    @JvmStatic
    fun run() {
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
}