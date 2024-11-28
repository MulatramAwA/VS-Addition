package io.github.xiewuzhiying.vs_addition.forge.compats.createaddition.content.contraptions.actors.psi

import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraftforge.energy.IEnergyStorage
import kotlin.math.min

class InterfaceEnergyHandler(private var wrapped: IEnergyStorage?, private val behavior: IPSIWithShipBehavior) : IEnergyStorage {
    override fun receiveEnergy(maxReceive0: Int, simulate: Boolean): Int {
        var maxReceive1 = maxReceive0
        if (!behavior.controller.canTransfer()) {
            return 0
        } else {
            maxReceive1 = min(maxReceive1.toDouble(), VSAdditionConfig.SERVER.create.psi.energyMaxInput.toDouble()).toInt()
            val wrapped = this.wrapped ?: return 0
            val received = wrapped.receiveEnergy(maxReceive1, simulate)
            if (received != 0 && !simulate) {
                this.keepAlive()
            }

            return received
        }
    }

    override fun extractEnergy(maxExtract0: Int, simulate: Boolean): Int {
        var maxExtract1 = maxExtract0
        if (!behavior.controller.canTransfer()) {
            return 0
        } else {
            maxExtract1 = min(maxExtract1.toDouble(), VSAdditionConfig.SERVER.create.psi.energyMaxOutput.toDouble()).toInt()
            val wrapped = this.wrapped ?: return 0
            val extracted = wrapped.extractEnergy(maxExtract1, simulate)
            if (extracted != 0 && !simulate) {
                this.keepAlive()
            }

            return extracted
        }
    }

    override fun getEnergyStored(): Int {
        return this.wrapped?.energyStored ?: 0
    }

    override fun getMaxEnergyStored(): Int {
        return this.wrapped?.maxEnergyStored ?: 0
    }

    override fun canExtract(): Boolean {
        return true
    }

    override fun canReceive(): Boolean {
        return true
    }

    private fun keepAlive() {
        behavior.controller.onContentTransferred()
        behavior.controller.other?.onContentTransferred()
    }
}