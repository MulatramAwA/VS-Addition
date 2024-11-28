package io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi

import com.simibubi.create.foundation.item.ItemHandlerWrapper
import com.simibubi.create.foundation.utility.fabric.ListeningStorageView
import com.simibubi.create.foundation.utility.fabric.ProcessingIterator
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

class InterfaceItemHandler(wrapped: Storage<ItemVariant>, private val behavior: IPSIWithShipBehavior) : ItemHandlerWrapper(wrapped) {
    override fun extract(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        if (!behavior.controller.canTransfer()) return 0L
        val extracted = super.extract(resource, maxAmount, transaction)
        if (extracted != 0L) {
            TransactionCallback.onSuccess(
                transaction
            ) { this.keepAlive() }
        }
        return extracted
    }

    override fun insert(resource: ItemVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
        if (!behavior.controller.canTransfer()) return 0L
        val inserted = super.insert(resource, maxAmount, transaction)
        if (inserted != 0L) {
            TransactionCallback.onSuccess(
                transaction
            ) { this.keepAlive() }
        }
        return inserted
    }

    override fun exactView(resource: ItemVariant?): StorageView<ItemVariant?> {
        return this.listen(super.exactView(resource))
    }

    override fun iterator(): MutableIterator<StorageView<ItemVariant>> {
        return ProcessingIterator(super.iterator()) { view -> this.listen(view) }
    }

    fun <T> listen(view: StorageView<T?>?): StorageView<T?> {
        return ListeningStorageView<T?>(view) { this.keepAlive() }
    }

    fun setWrapped(wrapped: Storage<ItemVariant>) {
        this.wrapped = wrapped
    }

    private fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }
}