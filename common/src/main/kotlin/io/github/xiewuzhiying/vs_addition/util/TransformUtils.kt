package io.github.xiewuzhiying.vs_addition.util

import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.ClipContextMixinDuck
import io.github.xiewuzhiying.vs_addition.mixinducks.valkyrienskies.EntityDraggingInformationMixinDuck
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import org.joml.*
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore
import org.valkyrienskies.mod.common.*
import org.valkyrienskies.mod.common.util.EntityDraggingInformation
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toMinecraft
import java.io.Serializable
import java.lang.Math
import kotlin.math.abs

val Direction.directionToQuaterniond : Quaterniond
    get() =
        when (this) {
            Direction.UP -> Quaterniond()
            Direction.DOWN -> Quaterniond(AxisAngle4d(Math.PI, Vector3d(1.0, 0.0, 0.0)))
            Direction.EAST -> Quaterniond(AxisAngle4d(0.5 * Math.PI, Vector3d(0.0, 1.0, 0.0))).mul(
                Quaterniond(
                    AxisAngle4d(
                        Math.PI / 2.0, Vector3d(1.0, 0.0, 0.0)
                    )
                )
            ).normalize()

            Direction.WEST -> Quaterniond(AxisAngle4d(1.5 * Math.PI, Vector3d(0.0, 1.0, 0.0))).mul(
                Quaterniond(
                    AxisAngle4d(
                        Math.PI / 2.0, Vector3d(1.0, 0.0, 0.0)
                    )
                )
            ).normalize()

            Direction.SOUTH -> Quaterniond(AxisAngle4d(Math.PI / 2.0, Vector3d(1.0, 0.0, 0.0))).normalize()
            Direction.NORTH -> Quaterniond(AxisAngle4d(Math.PI, Vector3d(0.0, 1.0, 0.0))).mul(
                Quaterniond(
                    AxisAngle4d(
                        Math.PI / 2.0, Vector3d(1.0, 0.0, 0.0)
                    )
                )
            ).normalize()
        }

val Direction.directionToQuaternionf : Quaternionf
    get() = Quaternionf(this.directionToQuaterniond)

fun Vec3.toShipyardCoordinates(ship: Ship): Vec3 {
    val vector3d = ship.worldToShip.transformPosition(this.toJOML())
    return vector3d.toMinecraft()
}

val Vector3dc.toVec3i : Vec3i
    get() = Vec3i(this.x().toInt(), this.y().toInt(), this.z().toInt())

val Vector3fc.toVec3i : Vec3i
    get() = Vec3i(this.x().toInt(), this.y().toInt(), this.z().toInt())

val Vec3.toVec3i : Vec3i
    get() = Vec3i(this.x().toInt(), this.y().toInt(), this.z().toInt())

val Vector3dc.toBlockPos: BlockPos
    get() = BlockPos(this.x().toInt(), this.y().toInt(), this.z().toInt());

val Vector3fc.toBlockPos: BlockPos
    get() = BlockPos(this.x().toInt(), this.y().toInt(), this.z().toInt());

val Vec3.toBlockPos: BlockPos
    get() = BlockPos(this.x().toInt(), this.y().toInt(), this.z().toInt());

val Vector3dc.toVector3i: Vector3i
    get() = Vector3i(this.x().toInt(), this.y().toInt(), this.z().toInt());

val Vector3fc.toVector3i: Vector3i
    get() = Vector3i(this.x().toInt(), this.y().toInt(), this.z().toInt());

val Vec3.toVector3i: Vector3i
    get() = Vector3i(this.x().toInt(), this.y().toInt(), this.z().toInt());

val Vec3i.toVec3: Vec3
    get() = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble());

val Vec3i.toVector3d: Vector3d
    get() = Vector3d(this.x.toDouble(), this.y.toDouble(), this.z.toDouble());

val Vec3i.toVector3i: Vector3i
    get() = Vector3i(this.x, this.y, this.z);

fun Vec3.toWorld(level: Level): Vec3 {
    val ship = level.getShipManagingPos(this)
    if (ship != null) {
        return ship.toWorldCoordinates(this)
    }
    return this
}

fun Vec3.toWorld(ship: Ship): Vec3 {
    return ship.toWorldCoordinates(this)
}

fun Vec3.below(distance: Double): Vec3 {
    val direction = Direction.DOWN
    return Vec3(
        this.x + direction.stepX * distance,
        this.y + direction.stepY * distance,
        this.z + direction.stepZ * distance
    )
}

fun BlockPos.front(direction: Direction): Vec3 {
    return when (direction) {
        Direction.EAST -> Vec3(this.x + 1.0, this.y + 0.5, this.z + 0.5)
        Direction.SOUTH -> Vec3(this.x + 0.5, this.y + 0.5, this.z + 1.0)
        Direction.WEST -> Vec3(this.x.toDouble(), this.y + 0.5, this.z + 0.5)
        Direction.NORTH -> Vec3(this.x + 0.5, this.y + 0.5, this.z.toDouble())
        Direction.UP -> Vec3(this.x + 0.5, this.y + 1.0, this.z + 0.5)
        else -> Vec3(this.x + 0.5, this.y.toDouble(), this.z + 0.5)
    }
}

val Vector3i.centerMinecraft : Vec3
    get() = Vec3(this.x + 0.5, this.y + 0.5, this.z + 0.5)

val Vector3i.centerJOMLD : Vector3d
    get() = Vector3d(this.x + 0.5, this.y + 0.5, this.z + 0.5)

val Vector3i.centerJOMLF : Vector3f
    get() = Vector3f(this.x + 0.5f, this.y + 0.5f, this.z + 0.5f)

val Vec3i.centerMinecraft : Vec3
    get() = Vec3(this.x + 0.5, this.y + 0.5, this.z + 0.5)

val Vec3i.centerJOMLD : Vector3d
    get() = Vector3d(this.x + 0.5, this.y + 0.5, this.z + 0.5)

val Vec3i.centerDJOMLF : Vector3f
    get() = Vector3f(this.x + 0.5f, this.y + 0.5f, this.z + 0.5f)

val Entity.isOnShip : Boolean
    get() = this.level().getShipsIntersecting(this.boundingBox).any()

var ClipContext.block : ClipContext.Block
    get() = (this as ClipContextMixinDuck).block
    set(value) { (this as ClipContextMixinDuck).block = value }

var ClipContext.fluid : ClipContext.Fluid
    get() = (this as ClipContextMixinDuck).fluid
    set(value) { (this as ClipContextMixinDuck).fluid = value }

var ClipContext.entity : Entity?
    get() = (this as ClipContextMixinDuck).entity
    set(value) { (this as ClipContextMixinDuck).entity = value}

val ClipContext.collisionContext : CollisionContext
    get() = (this as ClipContextMixinDuck).collisionContext

fun ClipContext.setForm(vec3: Vec3) {
    (this as ClipContextMixinDuck).setForm(vec3)
}

fun ClipContext.setTo(vec3: Vec3) {
    (this as ClipContextMixinDuck).setTo(vec3)
}

fun ClipContext.setCollisionContext(ctx: CollisionContext) {
    (this as ClipContextMixinDuck).collisionContext = ctx
}

fun ServerLevel.getBodyId(pos: Any) : ShipId {
    val vector = toVector3dc(pos)
    return this.getShipManagingPos(vector.x().toInt() shr 4, vector.z().toInt() shr  4)?.id ?: this.shipObjectWorld.dimensionToGroundBodyIdImmutable[this.dimensionId]!!
}

var EntityDraggingInformation.addedPitchRotLastTick : Double
    get() = (this as EntityDraggingInformationMixinDuck).addedPitchRotLastTick
    set(value) { (this as EntityDraggingInformationMixinDuck).addedPitchRotLastTick = value }

fun Level.squaredDistanceBetweenInclShips(inputPos1: Any, inputPos2: Any) : Double {
    val vector1 = toVector3dc(inputPos1)
    val vector2 = toVector3dc(inputPos2)
    return this.squaredDistanceBetweenInclShips(vector1.x(),vector1.y(), vector1.z(), vector2.x(), vector2.y(), vector2.z())
}

data class Quadruple<A,B,C,D>(var first: A, var second: B, var third: C, var fourth: D): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

fun toVector3dc(inputPos: Any) : Vector3dc {
    return when (inputPos) {
        is Vec3i -> inputPos.centerJOMLD
        is Position -> inputPos.toJOML()
        is Vector3i -> inputPos.centerJOMLD
        is Vector3fc -> Vector3d(inputPos.x().toDouble(), inputPos.y().toDouble(), inputPos.z().toDouble())
        is Vector3dc -> inputPos
        else -> throw IllegalArgumentException("Unsupported type: ${inputPos::class.simpleName}")
    }
}

fun Vec3.toDirection(): Direction {
    return when {
        abs(this.x()) > abs(this.y()) && abs(this.x()) > abs(this.z()) -> {
            if (this.x() > 0) Direction.EAST else Direction.WEST
        }
        abs(this.y()) > abs(this.x()) && abs(this.y()) > abs(this.z()) -> {
            if (this.y() > 0) Direction.UP else Direction.DOWN
        }
        else -> {
            if (this.z() > 0) Direction.SOUTH else Direction.NORTH
        }
    }
}

fun Vector3dc.toDirection(): Direction {
    return when {
        abs(this.x()) > abs(this.y()) && abs(this.x()) > abs(this.z()) -> {
            if (this.x() > 0) Direction.EAST else Direction.WEST
        }
        abs(this.y()) > abs(this.x()) && abs(this.y()) > abs(this.z()) -> {
            if (this.y() > 0) Direction.UP else Direction.DOWN
        }
        else -> {
            if (this.z() > 0) Direction.SOUTH else Direction.NORTH
        }
    }
}