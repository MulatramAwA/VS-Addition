package io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.displayLink

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats
import io.github.xiewuzhiying.vs_addition.VSAdditionMod.MOD_ID
import io.github.xiewuzhiying.vs_addition.util.centerJOMLD
import io.github.xiewuzhiying.vs_addition.util.toVector3d
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
        return if (ship != null) {
            when (context.sourceConfig().getInt("Mode")) {
                0 -> { Component.literal(ship.id.toString()) }
                1 -> { ship.slug?.let { Component.translatable(it) } ?: notShip }
                2 -> { Component.literal(format(ship.velocity.length())) }
                3 -> { vector3dcToComponent(ship.velocity) }
                4 -> { Component.literal(format(ship.omega.length())) }
                5 -> { vector3dcToComponent(ship.omega) }
                6 -> { vector3dcToComponent(ship.transform.positionInWorld) }
                7 -> { vector3dcToComponent(ship.transform.shipToWorld.transformPosition(source.centerJOMLD)) }
                8 -> { vector3dcToComponent(ship.transform.positionInShip) }
                9 -> { vector3dcToComponent(source.toVector3d) }
                else -> { notShip }
            }
        } else {
            notShip
        }
    }

    private fun vector3dcToComponent(vector3dc: Vector3dc): MutableComponent {
        return Component.literal("${format(vector3dc.x())} ${format(vector3dc.y())} ${format(vector3dc.z())}")
    }

    private fun format(double: Double) : String {
        return String.format("%.1f", double)
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