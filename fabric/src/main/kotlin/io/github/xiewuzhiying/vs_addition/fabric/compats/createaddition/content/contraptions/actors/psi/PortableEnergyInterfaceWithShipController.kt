package io.github.xiewuzhiying.vs_addition.fabric.compats.createaddition.content.contraptions.actors.psi

import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity
import com.mrh0.createaddition.config.Config
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi.FabricPortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.api.base.SimpleEnergyStorage

open class PortableEnergyInterfaceWithShipController(be: PortableEnergyInterfaceBlockEntity) : FabricPortableStorageInterfaceWithShipController(be) {
    var capability: InterfaceEnergyHandler? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): InterfaceEnergyHandler {
        return InterfaceEnergyHandler(SimpleEnergyStorage(0L, 0L, 0L), behavior);
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableEnergyInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val wrapped = SimpleEnergyStorage(Config.ACCUMULATOR_CAPACITY.get() / 2, Config.ACCUMULATOR_MAX_INPUT.get(), Config.ACCUMULATOR_MAX_OUTPUT.get())
        capability?.setWrapped(wrapped)
        otherController.capability?.setWrapped(wrapped)

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        capability?.setWrapped(EnergyStorage.EMPTY)
        (other as? PortableEnergyInterfaceWithShipController)?.capability?.setWrapped(EnergyStorage.EMPTY)
        other?.let { it.isPassive = false }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.setWrapped(EnergyStorage.EMPTY)
    }
}