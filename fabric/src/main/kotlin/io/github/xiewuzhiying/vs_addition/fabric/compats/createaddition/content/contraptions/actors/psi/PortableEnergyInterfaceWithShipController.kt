package io.github.xiewuzhiying.vs_addition.fabric.compats.createaddition.content.contraptions.actors.psi

import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
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
        capability = InterfaceEnergyHandler(
            SimpleEnergyStorage(
                VSAdditionConfig.SERVER.create.psi.energyTemp.toLong(),
                VSAdditionConfig.SERVER.create.psi.energyMaxInput.toLong(),
                VSAdditionConfig.SERVER.create.psi.energyMaxOutput.toLong()
            ),
            be as IPSIWithShipBehavior
        )
        otherController.capability = capability

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