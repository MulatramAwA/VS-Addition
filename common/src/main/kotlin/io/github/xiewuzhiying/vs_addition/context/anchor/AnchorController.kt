package io.github.xiewuzhiying.vs_addition.context.anchor

import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.ShipForcesInducer
import org.valkyrienskies.core.api.ships.properties.ShipId

open class AnchorController : ShipForcesInducer {
    override fun applyForces(physShip: PhysShip) {
        TODO("Not yet implemented")
    }

    override fun applyForcesAndLookupPhysShips(physShip: PhysShip, lookupPhysShip: (ShipId) -> PhysShip?) {
        // Default implementation to not break existing implementations

    }
}