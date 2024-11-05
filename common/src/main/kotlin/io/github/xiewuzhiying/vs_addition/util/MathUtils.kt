package io.github.xiewuzhiying.vs_addition.util

import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.*
import kotlin.math.abs

val epsilon = 1e-10

var LineSegmentd.start : Vector3d
    get() = Vector3d(this.aX, this.aY, this.aZ)
    set(value) {
        this.aX = value.x
        this.aY = value.y
        this.aZ = value.z
    }

var LineSegmentd.end : Vector3d
    get() = Vector3d(this.bX, this.bY, this.bZ)
    set(value) {
        this.bX = value.x
        this.bY = value.y
        this.bZ = value.z
    }

fun LineSegmentd.intersect(plane: Planed, dest: Vector3d) : Boolean {
    return Intersectiond.intersectLineSegmentPlane(this.aX, this.aY, this.aZ, this.bX, this.bY, this.bZ, plane.a, plane.b, plane.c, plane.d, dest)
}

var Planed.normal : Vector3d
    get() = Vector3d(this.a, this.b, this.c)
    set(value) {
        this.a = value.x
        this.b = value.y
        this.c = value.z
    }

fun Planed.intersect(line: LineSegmentd, dest: Vector3d) : Boolean {
    return Intersectiond.intersectLineSegmentPlane(line.aX, line.aY, line.aZ, line.bX, line.bY, line.bZ, this.a, this.b, this.c, this.d, dest)
}

fun AABBdc.toPoints() : List<Vector3dc> {
    val k: Double = this.minX()
    val l: Double = this.minY()
    val m: Double = this.minZ()
    val n: Double = this.maxX()
    val o: Double = this.maxY()
    val p: Double = this.maxZ()
    return listOf(
        Vector3d(k, l, m),
        Vector3d(n, l, m),
        Vector3d(k, o, m),
        Vector3d(n, o, m),
        Vector3d(k, l, p),
        Vector3d(n, l, p),
        Vector3d(k, o, p),
        Vector3d(n, o, p)
    )
}

fun AABBdc.toLineSegments(): List<LineSegmentd> {
    val points = this.toPoints()
    return listOf(
        LineSegmentd(points[0], points[1]),
        LineSegmentd(points[1], points[3]),
        LineSegmentd(points[3], points[2]),
        LineSegmentd(points[2], points[0]), // Bottom face edges
        LineSegmentd(points[4], points[5]),
        LineSegmentd(points[5], points[7]),
        LineSegmentd(points[7], points[6]),
        LineSegmentd(points[6], points[4]), // Top face edges
        LineSegmentd(points[0], points[4]),
        LineSegmentd(points[1], points[5]),
        LineSegmentd(points[3], points[7]),
        LineSegmentd(points[2], points[6])  // Side edges
    )
}

fun AABBdc.toPlanes() : List<Planed> {
    val points = this.toPoints()
    return listOf(
        Planed(points[4], points[5], points[0]),
        Planed(points[0], points[1], points[2]),
        Planed(points[1], points[5], points[3]),
        Planed(points[5], points[4], points[7]),
        Planed(points[4], points[0], points[6]),
        Planed(points[2], points[3], points[6]),
    )
}

fun AABBdc.getMin() : Vector3d {
    return Vector3d(this.minX(), this.minY(), this.minZ())
}

fun AABBdc.getMax() : Vector3d {
    return Vector3d(this.maxX(), this.maxY(), this.maxZ())
}

fun AABBdc.moveToOrigin() : AABBdc {
    val center = this.center(Vector3d())
    val aabb = AABBd(this)
    aabb.setMax(aabb.getMax().sub(center))
    aabb.setMin(aabb.getMin().sub(center))
    return aabb
}

fun AABBdc.testPlane(plane: Planed) : Boolean {
    return Intersectiond.testAabPlane(this.minX(), this.minY(), this.minZ(), this.maxX(), this.maxY(), this.maxZ(), plane.a, plane.b, plane.c, plane.d)
}

fun Planed.testAABB(aabb: AABBdc) : Boolean {
    return Intersectiond.testAabPlane(aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ(), this.a, this.b, this.c, this.d)
}

fun isPointOnPlane(x: Double, y: Double, z: Double, a: Double, b: Double, c: Double, d: Double): Boolean {
    return abs(a * x + b * y + c * z + d) < epsilon
}

fun Vector3dc.isOnPlane(a: Double, b: Double, c: Double, d: Double): Boolean {
    return isPointOnPlane(this.x(), this.y(), this.z(), a, b, c, d)
}

fun Vector3dc.isOnPlane(plane: Planed): Boolean {
    return this.isOnPlane(plane.a, plane.b, plane.c, plane.d)
}

fun isPointInFrontOfPlane(x: Double, y: Double, z: Double, a: Double, b: Double, c: Double, d: Double): Boolean {
    return a * x + b * y + c * z + d > 0
}

fun Vector3dc.isInFrontOfPlane (a: Double, b: Double, c: Double, d: Double): Boolean {
    return isPointInFrontOfPlane(this.x(), this.y(), this.z(), a, b, c, d)
}

fun Vector3dc.isInFrontOfPlane (plane: Planed): Boolean {
    return this.isInFrontOfPlane(plane.a, plane.b, plane.c, plane.d)
}

fun isPointBehindPlane(x: Double, y: Double, z: Double, a: Double, b: Double, c: Double, d: Double): Boolean {
    return a * x + b * y + c * z + d < 0
}

fun Vector3dc.isBehindPlane (a: Double, b: Double, c: Double, d: Double): Boolean {
    return isPointBehindPlane(this.x(), this.y(), this.z(), a, b, c, d)
}

fun Vector3dc.isBehindPlane (plane: Planed): Boolean {
    return this.isBehindPlane(plane.a, plane.b, plane.c, plane.d)
}


interface Triangledc {
    fun a() : Vector3dc
    fun b() : Vector3dc
    fun c() : Vector3dc
    fun normal() : Vector3dc
    fun area(): Double
}

data class Triangled(var a: Vector3dc, var b: Vector3dc, var c: Vector3dc) : Triangledc {
    override fun a(): Vector3dc {
        return this.a
    }
    override fun b(): Vector3dc {
        return this.b
    }
    override fun c(): Vector3dc {
        return this.c
    }
    override fun normal() : Vector3dc {
        return b().sub(a(), Vector3d()).cross(c().sub(a(), Vector3d()), Vector3d()).normalize()
    }
    override fun area(): Double {
        return abs(b().sub(a(), Vector3d()).cross(c().sub(a(), Vector3d())).length()) / 2.0
    }
}