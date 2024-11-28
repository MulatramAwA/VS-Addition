package io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.fabricmc.fabric.api.transfer.v1.storage.Storage

open class PortableFluidInterfaceWithShipController(be: PortableFluidInterfaceBlockEntity) : FabricPortableStorageInterfaceWithShipController(be) {
    var capability: InterfaceFluidHandler? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): InterfaceFluidHandler {
        return InterfaceFluidHandler(Storage.empty(), behavior)
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableFluidInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val wrapped = FluidTank(4000)
        capability?.setWrapped(wrapped)
        otherController.capability?.setWrapped(wrapped)

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        capability?.setWrapped(Storage.empty())
        other?.let { it.isPassive = false }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.setWrapped(Storage.empty())
    }
}