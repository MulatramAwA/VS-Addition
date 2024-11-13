package io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.displayLink

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats
import io.github.xiewuzhiying.vs_addition.VSAdditionMod.MOD_ID
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3dc
import org.valkyrienskies.mod.common.getShipManagingPos

open class ShipDataDisplaySource : NumericSingleLineDisplaySource() {
    override fun getTranslationKey(): String {
        return "ship_data"
    }

    override fun provideLine(context: DisplayLinkContext, stats: DisplayTargetStats): MutableComponent {
        val source = context.sourcePos
        val level = context.level()
        val ship = level.getShipManagingPos(source)
        if (ship != null) {
            when (context.sourceConfig().getInt("Mode")) {
                0 -> {
                    return Component.literal(ship.id.toString())
                }
                1 -> {
                    return ship.slug?.let { Component.translatable(it) } ?: notShip
                }
                2 -> {
                    return vector3dcToComponent(ship.velocity)
                }
                3 -> {
                    return vector3dcToComponent(ship.omega)
                }
                4 -> {
                    return vector3dcToComponent(ship.transform.positionInWorld)
                }
                5 -> {
                    return vector3dcToComponent(ship.transform.positionInShip)
                }
                else -> {
                    return notShip
                }
            }
        } else {
            return notShip
        }
    }

    private fun vector3dcToComponent(vector3dc: Vector3dc): MutableComponent {
        val x = String.format("%.1f", vector3dc.x())
        val y = String.format("%.1f", vector3dc.y())
        val z = String.format("%.1f", vector3dc.z())
        return Component.literal("$x $y $z")
    }

    override fun allowsLabeling(context: DisplayLinkContext): Boolean {
        return true
    }

    companion object {
        private val notShip: MutableComponent =
            Component.translatable("vs_addition.display_source.ship_data.not_ship")
        val id = ResourceLocation(MOD_ID, "ship_data")
    }
}