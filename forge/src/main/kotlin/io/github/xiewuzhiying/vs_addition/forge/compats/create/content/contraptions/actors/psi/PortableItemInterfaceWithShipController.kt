package io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraft.world.entity.item.ItemEntity
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.ItemStackHandler

open class PortableItemInterfaceWithShipController(be: PortableItemInterfaceBlockEntity) : ForgePortableStorageInterfaceWithShipController(be) {
    var capability: LazyOptional<IItemHandlerModifiable>? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): LazyOptional<IItemHandlerModifiable> {
        return LazyOptional.of { InterfaceItemHandler(ItemStackHandler(0), behavior) }
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableItemInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val oldCap0 = capability
        val oldCap1 = otherController.capability
        capability = LazyOptional.of { InterfaceItemHandler(ItemStackHandler(VSAdditionConfig.SERVER.create.psi.itemTemp), be as IPSIWithShipBehavior) }
        otherController.capability = capability
        oldCap0?.invalidate()
        oldCap1?.invalidate()

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        if (!this.isPassive) {
            val level = this.be.level
            val capability = this.capability
            if (capability != null && level != null) {
                val center = getConnectionCenter()
                val storage = capability.resolve().get()
                for (i in 0 until  storage.slots) {
                    val entity = ItemEntity(level, center.x, center.y, center.z, storage.getStackInSlot(i))
                    level.addFreshEntity(entity)
                }
            }
            var oldCap = capability
            this.capability = createEmptyHandler(be as IPSIWithShipBehavior)
            oldCap?.invalidate()
            oldCap = (other as? PortableItemInterfaceWithShipController)?.capability
            (other as? PortableItemInterfaceWithShipController)?.capability = createEmptyHandler(be as IPSIWithShipBehavior)
            oldCap?.invalidate()
            other?.let { it.isPassive = false }
        }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.invalidate()
    }
}