package io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi

import com.simibubi.create.foundation.item.ItemHandlerWrapper
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

class InterfaceItemHandler(wrapped: Storage<ItemVariant>, private val behavior: IPSIWithShipBehavior) : ItemHandlerWrapper(wrapped) {
    override fun extract(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        if (!behavior.controller.canTransfer()) return 0
        val extracted = super.extract(resource, maxAmount, transaction)
        if (extracted != 0L) {
            TransactionCallback.onSuccess(
                transaction
            ) { this.keepAlive() }
        }
        return extracted
    }

    override fun insert(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        if (!behavior.controller.canTransfer()) return 0
        val inserted = super.insert(resource, maxAmount, transaction)
        if (inserted != 0L) {
            TransactionCallback.onSuccess(
                transaction
            ) { this.keepAlive() }
        }
        return inserted
    }

    fun setWrapped(wrapped: Storage<ItemVariant>) {
        this.wrapped = wrapped
    }

    private fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }
}