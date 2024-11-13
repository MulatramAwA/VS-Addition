package io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.displayLink

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats
import io.github.xiewuzhiying.vs_addition.VSAdditionMod.MOD_ID
import io.github.xiewuzhiying.vs_addition.util.centerJOMLD
import io.github.xiewuzhiying.vs_addition.util.toVector3d
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis.*
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.Ship
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
            when (Mode.entries[context.sourceConfig().getInt("Mode")]) {
                Mode.ID -> { Component.literal(ship.id.toString()) }
                Mode.SLUG -> { ship.slug?.let { Component.translatable(it) } ?: notShip }
                Mode.SPEED_MS -> { Component.literal("${format(ship.velocity.length())} m/s") }
                Mode.SPEED_KMH -> { Component.literal("${format(ship.velocity.length() * 3.6)} km/h") }
                Mode.VELOCITY -> { vector3dcToComponent(ship.velocity) }
                Mode.ANGLE_SPEED_RADS -> { Component.literal("${format(ship.omega.length())} rad/s") }
                Mode.OMEGA -> { vector3dcToComponent(ship.omega) }
                Mode.MASS_CENTER_WORLD -> { vector3dcToComponent(ship.transform.positionInWorld) }
                Mode.SOURCE_WORLD -> { vector3dcToComponent(ship.transform.shipToWorld.transformPosition(source.centerJOMLD)) }
                Mode.MASS_CENTER_SHIPYARD -> { vector3dcToComponent(ship.transform.positionInShip) }
                Mode.SOURCE_SHIPYARD -> { vector3dcToComponent(source.toVector3d) }
                Mode.ROLL_RAD -> { Component.literal(format(getEulerAngle(Direction.Axis.X, ship, context))) }
                Mode.ROLL_DEG -> { Component.literal(format(Math.toDegrees(getEulerAngle(Direction.Axis.X, ship, context)))) }
                Mode.PITCH_RAD -> { Component.literal(format(getEulerAngle(Direction.Axis.Z, ship, context))) }
                Mode.PITCH_DEG -> { Component.literal(format(Math.toDegrees(getEulerAngle(Direction.Axis.Z, ship, context)))) }
                Mode.YAW_RAD -> { Component.literal(format(getEulerAngle(Direction.Axis.Y, ship, context))) }
                Mode.YAW_DEG -> { Component.literal(format(Math.toDegrees(getEulerAngle(Direction.Axis.Y, ship, context)))) }
            }
        } else {
            notShip
        }
    }

    private fun vector3dcToComponent(vector3dc: Vector3dc): MutableComponent {
        return Component.literal("${format(vector3dc.x())}  ${format(vector3dc.y())}  ${format(vector3dc.z())}")
    }

    private fun format(double: Double) : String {
        return String.format("%.1f", double)
    }

    private fun getEulerAngle(axis: Direction.Axis, ship: Ship, context: DisplayLinkContext) : Double {
        return ship.transform.shipToWorldRotation
            //.mul(context.blockEntity().blockState.getValue(BlockStateProperties.FACING).toQuaterniond, Quaterniond())
            .getEulerAnglesXYZ(Vector3d()).let {
                when (axis) {
                    X -> it.x
                    Y -> it.y
                    Z -> it.z
                }
            }
    }

    override fun allowsLabeling(context: DisplayLinkContext): Boolean {
        return true
    }

    enum class Mode {
        ID,
        SLUG,
        SPEED_MS,
        SPEED_KMH,
        VELOCITY,
        ANGLE_SPEED_RADS,
        OMEGA,
        MASS_CENTER_WORLD,
        SOURCE_WORLD,
        MASS_CENTER_SHIPYARD,
        SOURCE_SHIPYARD,
        ROLL_RAD,
        ROLL_DEG,
        PITCH_RAD,
        PITCH_DEG,
        YAW_RAD,
        YAW_DEG;
    }

    companion object {
        private val notShip: MutableComponent =
            Component.translatable("vs_addition.display_source.ship_data.not_ship")
        val id = ResourceLocation(MOD_ID, "ship_data")
    }
}