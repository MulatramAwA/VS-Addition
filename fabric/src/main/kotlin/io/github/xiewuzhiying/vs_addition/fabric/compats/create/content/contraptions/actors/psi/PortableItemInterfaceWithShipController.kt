package io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import io.github.xiewuzhiying.vs_addition.util.toVec3
import net.fabricmc.fabric.api.transfer.v1.storage.Storage

open class PortableItemInterfaceWithShipController(be: PortableItemInterfaceBlockEntity) : FabricPortableStorageInterfaceWithShipController(be) {
    var capability: InterfaceItemHandler? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): InterfaceItemHandler {
        return InterfaceItemHandler(Storage.empty(), behavior)
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableItemInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val wrapped = ItemStackHandler(8)
        capability?.setWrapped(wrapped)
        otherController.capability?.setWrapped(wrapped)

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        if (!this.isPassive) {
            val level = this.be.level
            if (capability != null && level != null) {
                val center = getConnectionCenter(level, this.be).toVec3
            }
            capability?.setWrapped(Storage.empty())
            (other as? PortableItemInterfaceWithShipController)?.capability?.setWrapped(Storage.empty())
            other?.let { it.isPassive = false }
        }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.setWrapped(Storage.empty())
    }
}