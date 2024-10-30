package io.github.xiewuzhiying.vs_addition.stuff.airpocket

import org.joml.Vector3d
import org.joml.Vector3dc
import kotlin.math.atan2

const val WATER_OFFSET : Double = 8.0 / 9.0

const val SEA_LEVEL : Double = 62.0 + WATER_OFFSET

const val ZFIGHT : Double = 0.002

fun intersectEdgeWithPlane(edge: Edged, plane: Planed): Vector3d? {
    val start = edge.start
    val end = edge.end
    val dir = Vector3d(end).sub(start)

    val ndotd = plane.normal.dot(dir)
    if (ndotd == 0.0) return null

    val t = -(plane.normal.dot(start) + plane.getD()) / ndotd
    return if (t in 0.0..1.0) Vector3d(start).fma(t, dir) else null
}

fun calculateSection(vertices: List<Vector3dc>, plane: Planed): List<Vector3dc> {
    val edges = listOf(
        Edged(vertices[0], vertices[1]), Edged(vertices[1], vertices[3]), Edged(vertices[3], vertices[2]), Edged(vertices[2], vertices[0]), // Bottom face edges
        Edged(vertices[4], vertices[5]), Edged(vertices[5], vertices[7]), Edged(vertices[7], vertices[6]), Edged(vertices[6], vertices[4]), // Top face edges
        Edged(vertices[0], vertices[4]), Edged(vertices[1], vertices[5]), Edged(vertices[3], vertices[7]), Edged(vertices[2], vertices[6])  // Side edges
    )

    val intersections = edges.mapNotNull { edge -> intersectEdgeWithPlane(edge, plane) }

    return intersections
}

fun sortVertices(vertices: List<Vector3dc>, planeNormal: Vector3dc): List<Vector3dc> {
    if (vertices.size < 3) return vertices

    val centroid = Vector3d()
    vertices.forEach { centroid.add(it) }
    centroid.div(vertices.size.toDouble())

    val right = Vector3d(1.0, 0.0, 0.0).apply {
        if (dot(planeNormal) > 0.999) set(0.0, 1.0, 0.0)
    }.cross(planeNormal, Vector3d()).normalize()
    val forward = Vector3d(right).cross(planeNormal).normalize()

    return vertices.sortedBy { v ->
        val offset = Vector3d(v).sub(centroid)
        atan2(offset.dot(forward), offset.dot(right))
    }
}

data class Planed(var normal: Vector3dc, var offset: Vector3dc)  {
    override fun toString(): String = "($normal, $offset)"
    fun normal() : Vector3dc = normal
    fun offset() : Vector3dc = offset

    fun getD(): Double {
        return -normal.dot(offset)
    }
}
data class Edged(val start: Vector3dc, val end: Vector3dc) {
    override fun toString(): String = "($start, $end)"
}