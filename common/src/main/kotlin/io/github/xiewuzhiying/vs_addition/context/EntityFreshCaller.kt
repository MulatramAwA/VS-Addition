package io.github.xiewuzhiying.vs_addition.context

import dev.architectury.event.EventResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.mod.common.entity.handling.VSEntityManager.getHandler
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.util.toJOML

object EntityFreshCaller {
    @JvmStatic
    fun freshEntityInShipyard(entity: Entity, level: Level) : EventResult {
        val ship: LoadedShip? = level.getShipObjectManagingPos(entity.position().toJOML())
        if (ship != null) {
            getHandler(entity).freshEntityInShipyard(entity, ship)
        }
        return EventResult.pass()
    }
}