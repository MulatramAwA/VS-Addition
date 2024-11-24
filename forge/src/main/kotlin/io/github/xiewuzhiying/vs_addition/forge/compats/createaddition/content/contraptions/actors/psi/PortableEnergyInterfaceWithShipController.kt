package io.github.xiewuzhiying.vs_addition.forge.compats.createaddition.content.contraptions.actors.psi

import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity
import com.mrh0.createaddition.config.Config
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi.ForgePortableStorageInterfaceWithShipController
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.energy.IEnergyStorage

open class PortableEnergyInterfaceWithShipController(be: PortableEnergyInterfaceBlockEntity) : ForgePortableStorageInterfaceWithShipController(be) {
    var capability: LazyOptional<IEnergyStorage>? = null

    fun createEmptyHandler(behavior: IPSIWithShipBehavior): LazyOptional<IEnergyStorage> {
        return LazyOptional.of { InterfaceEnergyHandler(EnergyStorage(0), behavior) }
    }

    override fun startTransferringTo(otherController: PortableStorageInterfaceWithShipController) {
        if (otherController !is PortableEnergyInterfaceWithShipController || this == otherController || this.other == otherController) {
            return
        }
        val oldCap0 = capability
        val oldCap1 = otherController.capability
        capability = LazyOptional.of { InterfaceEnergyHandler(EnergyStorage(Config.ACCUMULATOR_CAPACITY.get() / 2, Config.ACCUMULATOR_MAX_INPUT.get(), Config.ACCUMULATOR_MAX_OUTPUT.get()), be as IPSIWithShipBehavior) }
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