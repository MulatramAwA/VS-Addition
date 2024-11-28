package io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity
import io.github.xiewuzhiying.vs_addition.PlatformUtils
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.templates.FluidTank

open class PortableFluidInterfaceWithShipController(be: PortableFluidInterfaceBlockEntity) : ForgePortableStorageInterfaceWithShipController(be) {
    var capability: LazyOptional<IFluidHandler>? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): LazyOptional<IFluidHandler> {
        return LazyOptional.of { InterfaceFluidHandler(FluidTank(0), behavior) }
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableFluidInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val oldCap0 = capability
        val oldCap1 = otherController.capability
        capability = LazyOptional.of { InterfaceFluidHandler(FluidTank(PlatformUtils.getBucketToFluidUnit(VSAdditionConfig.SERVER.create.psi.fluidTemp)), be as IPSIWithShipBehavior) }
        otherController.capability = capability
        oldCap0?.invalidate()
        oldCap1?.invalidate()

        super.startTransferringTo(otherController)
    }

    override fun stopTransferring() {
        val oldCap = capability
        capability = createEmptyHandler(be as IPSIWithShipBehavior)
        oldCap?.invalidate()
        other?.let { it.isPassive = false }
        super.stopTransferring()
    }

    override fun invalidateCapability() {
        capability?.invalidate()
    }
}