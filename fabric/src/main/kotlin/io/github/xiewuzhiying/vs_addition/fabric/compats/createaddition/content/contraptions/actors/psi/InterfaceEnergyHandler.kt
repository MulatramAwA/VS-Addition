package io.github.xiewuzhiying.vs_addition.fabric.compats.createaddition.content.contraptions.actors.psi

import com.mrh0.createaddition.config.Config
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import team.reborn.energy.api.EnergyStorage
import kotlin.math.min

class InterfaceEnergyHandler(private var wrapped: EnergyStorage?, private val behavior: IPSIWithShipBehavior) : EnergyStorage {
    override fun insert(maxReceive0: Long, transaction: TransactionContext?): Long {
        var maxReceive1 = maxReceive0
        if (!behavior.controller.canTransfer()) {
            return 0L
        } else {
            maxReceive1 = min(maxReceive1.toDouble(), (Config.PEI_MAX_INPUT.get() as Long).toDouble())
                .toLong()
            if (this.wrapped == null) {
                return 0L
            } else {
                val received = wrapped!!.insert(maxReceive1, transaction)
                if (received != 0L) {
                    TransactionCallback.onSuccess(transaction) { this.keepAlive() }
                }

                return received
            }
        }
    }

    override fun extract(maxExtract0: Long, transaction: TransactionContext?): Long {
        var maxExtract1 = maxExtract0
        if (!behavior.controller.canTransfer()) {
            return 0L
        } else {
            maxExtract1 = min(maxExtract1.toDouble(), (Config.PEI_MAX_OUTPUT.get() as Long).toDouble())
                .toLong()
            if (this.wrapped == null) {
                return 0L
            } else {
                val extracted = wrapped!!.extract(maxExtract1, transaction)
                if (extracted != 0L) {
                    TransactionCallback.onSuccess(transaction) { this.keepAlive() }
                }

                return extracted
            }
        }
    }

    override fun getAmount(): Long {
        return if (this.wrapped == null) 0L else wrapped!!.amount
    }

    override fun getCapacity(): Long {
        return if (this.wrapped == null) 0L else wrapped!!.capacity
    }

    override fun supportsExtraction(): Boolean {
        return true
    }

    override fun supportsInsertion(): Boolean {
        return true
    }

    fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }

    fun setWrapped(wrapped: EnergyStorage) {
        this.wrapped = wrapped
    }
}