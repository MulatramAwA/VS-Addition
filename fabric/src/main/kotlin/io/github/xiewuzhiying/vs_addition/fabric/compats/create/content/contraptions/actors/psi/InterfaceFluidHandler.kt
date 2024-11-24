package io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi

import com.simibubi.create.foundation.utility.fabric.ListeningStorageView
import com.simibubi.create.foundation.utility.fabric.ProcessingIterator
import io.github.fabricators_of_create.porting_lib.transfer.WrappedStorage
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext

class InterfaceFluidHandler(wrapped: Storage<FluidVariant>, private val behavior: IPSIWithShipBehavior) : WrappedStorage<FluidVariant>(wrapped) {
    override fun insert(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
        if (!behavior.controller.isConnected) return 0
        val fill = wrapped.insert(resource, maxAmount, transaction)
        if (fill > 0) TransactionCallback.onSuccess(transaction) { this.keepAlive() }
        return fill
    }

    override fun extract(resource: FluidVariant, maxAmount: Long, transaction: TransactionContext): Long {
        if (!behavior.controller.canTransfer()) return 0
        val drain = wrapped.extract(resource, maxAmount, transaction)
        if (drain != 0L) TransactionCallback.onSuccess(transaction) { this.keepAlive() }
        return drain
    }

    override fun iterator(): MutableIterator<StorageView<FluidVariant>> {
        return ProcessingIterator(super.iterator()) { view: StorageView<FluidVariant> -> this.listen(view) }
    }

    private fun <T> listen(view: StorageView<T>?): StorageView<T> {
        return ListeningStorageView(view) { this.keepAlive() }
    }

    fun setWrapped(wrapped: Storage<FluidVariant>) {
        this.wrapped = wrapped
    }

    private fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }
}