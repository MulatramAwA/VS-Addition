package io.github.xiewuzhiying.vs_addition.util

import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.ClipContextMixinDuck
import io.github.xiewuzhiying.vs_addition.mixinducks.valkyrienskies.EntityDraggingInformationMixinDuck
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import org.joml.*
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.mod.common.*
import org.valkyrienskies.mod.common.util.EntityDraggingInformation
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.common.util.toMinecraft
import java.io.Serializable
import kotlin.math.abs

val Direction.toVector3d : Vector3d
    get() = directionIndexToVector3d[this.ordinal]

val directionIndexToVector3d = arrayOf(
    Vector3d(0.0, -1.0, 0.0), Vector3d(0.0, 1.0, 0.0), // ±Y
    Vector3d(0.0, 0.0, -1.0), Vector3d(0.0, 0.0, 1.0), // ±Z
    Vector3d(-1.0, 0.0, 0.0), Vector3d(1.0, 0.0, 0.0) // ±X
)

fun Vec3.toShipyardCoordinates(ship: Ship): Vec3 {
    val vector3d = (ship as? ClientShip)?.renderTransform?.worldToShip?.transformPosition(this.toJOML()) ?: ship.transform.worldToShip.transformPosition(this.toJOML())
    return vector3d.toMinecraft()
}

val Vector3dc.toVec3i : Vec3i
    get() = Vec3i(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vector3fc.toVec3i : Vec3i
    get() = Vec3i(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vec3.toVec3i : Vec3i
    get() = Vec3i(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vector3dc.toBlockPos: BlockPos
    get() = BlockPos(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vector3fc.toBlockPos: BlockPos
    get() = BlockPos(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vec3.toBlockPos: BlockPos
    get() = BlockPos(Mth.floor(this.x), Mth.floor(this.y), Mth.floor(this.z))

val Vector3dc.toVector3i: Vector3i
    get() = Vector3i(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vector3fc.toVector3i: Vector3i
    get() = Vector3i(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vec3.toVector3i: Vector3i
    get() = Vector3i(Mth.floor(this.x()), Mth.floor(this.y()), Mth.floor(this.z()))

val Vector3dc.toVec3: Vec3
    get() = Vec3(this.x(), this.y(), this.z())

val Vec3i.toVec3: Vec3
    get() = Vec3(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())

val Vec3i.toVector3d: Vector3d
    get() = Vector3d(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())

val Vec3i.toVector3i: Vector3i
    get() = Vector3i(this.x, this.y, this.z)

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

var ClipContext.collisionContext : CollisionContext
    get() = (this as ClipContextMixinDuck).collisionContext
    set(value) { (this as ClipContextMixinDuck).collisionContext = value }

fun ClipContext.setForm(vec3: Vec3) {
    (this as ClipContextMixinDuck).setForm(vec3)
}

fun ClipContext.setTo(vec3: Vec3) {
    (this as ClipContextMixinDuck).setTo(vec3)
}

fun ServerLevel.getBodyId(pos: Any) : ShipId {
    val vector = toVector3d(pos)
    return this.getShipManagingPos(vector.x().toInt() shr 4, vector.z().toInt() shr  4)?.id ?: this.shipObjectWorld.dimensionToGroundBodyIdImmutable[this.dimensionId]!!
}

fun toRenderWorldCoordinates(pos: Vector3d) : Vector3d {
    return VSClientGameUtils.getClientShip(pos.x, pos.y, pos.z)?.renderTransform?.shipToWorld?.transformPosition(pos) ?: pos
}

fun toRenderWorldCoordinates(pos: Vec3) : Vec3 {
    return toRenderWorldCoordinates(pos.toJOML()).toMinecraft()
}

var EntityDraggingInformation.addedPitchRotLastTick : Double
    get() = (this as EntityDraggingInformationMixinDuck).addedPitchRotLastTick
    set(value) { (this as EntityDraggingInformationMixinDuck).addedPitchRotLastTick = value }

fun Level.squaredDistanceBetweenInclShips(inputPos1: Any, inputPos2: Any) : Double {
    val vector1 = toVector3d(inputPos1)
    val vector2 = toVector3d(inputPos2)
    return this.squaredDistanceBetweenInclShips(vector1.x(),vector1.y(), vector1.z(), vector2.x(), vector2.y(), vector2.z())
}

data class Quadruple<A,B,C,D>(var first: A, var second: B, var third: C, var fourth: D): Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

fun toVector3d(inputPos: Any) : Vector3d {
    return when (inputPos) {
        is Vec3i -> inputPos.centerJOMLD
        is Position -> inputPos.toJOML()
        is Vector3i -> inputPos.centerJOMLD
        is Vector3fc -> Vector3d(inputPos.x().toDouble(), inputPos.y().toDouble(), inputPos.z().toDouble())
        is Vector3dc -> Vector3d(inputPos)
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

val AABB.copy : AABB
    get() = AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ)

fun getAABBFromCenterAndExtent(center: Vector3dc, extent: Vector3dc) : AABBd {
    return AABBd(center.x() - extent.x(), center.y() - extent.y(), center.z() - extent.z(), center.x() + extent.x(), center.y() + extent.y(), center.z() + extent.z())
}