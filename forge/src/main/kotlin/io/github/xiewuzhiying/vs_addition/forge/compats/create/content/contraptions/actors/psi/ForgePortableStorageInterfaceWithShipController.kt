package io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi

import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController

open class ForgePortableStorageInterfaceWithShipController(override var be: PortableStorageInterfaceBlockEntity) : PortableStorageInterfaceWithShipController(be) {
    override fun onContentTransferred() {
        be.onContentTransferred()
    }
}