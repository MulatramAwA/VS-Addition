package io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi

import com.simibubi.create.foundation.item.ItemHandlerWrapper
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable

class InterfaceItemHandler(wrapped: IItemHandlerModifiable?, private val behavior: IPSIWithShipBehavior) : ItemHandlerWrapper(wrapped) {
    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (!behavior.controller.canTransfer()) return ItemStack.EMPTY
        val extractItem = super.extractItem(slot, amount, simulate)
        if (!simulate && !extractItem.isEmpty) this.keepAlive()
        return extractItem
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        if (!behavior.controller.canTransfer()) return stack
        val insertItem = super.insertItem(slot, stack, simulate)
        if (!simulate && !insertItem.equals(stack, false)) this.keepAlive()
        return insertItem
    }

    private fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }
}