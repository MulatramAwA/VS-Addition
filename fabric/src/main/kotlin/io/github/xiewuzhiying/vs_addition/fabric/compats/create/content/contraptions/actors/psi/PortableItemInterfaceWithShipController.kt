package io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.world.entity.item.ItemEntity

open class PortableItemInterfaceWithShipController(be: PortableItemInterfaceBlockEntity) : FabricPortableStorageInterfaceWithShipController(be) {
    var capability: InterfaceItemHandler? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): InterfaceItemHandler {
        return InterfaceItemHandler(Storage.empty(), behavior)
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableItemInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        this.capability = InterfaceItemHandler(ItemStackHandler(VSAdditionConfig.SERVER.create.psi.itemTemp), be as IPSIWithShipBehavior)
        otherController.capability = this.capability

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        if (!this.isPassive) {
            val level = this.be.level
            val capability = this.capability
            if (capability != null && level != null) {
                val center = getConnectionCenter()
                val iterator = capability.iterator()
                if (iterator.hasNext()) {
                    val storage = iterator.next()
                    val entity = ItemEntity(level, center.x, center.y, center.z, storage.resource.toStack(storage.amount.toInt()))
                    level.addFreshEntity(entity)
                }
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