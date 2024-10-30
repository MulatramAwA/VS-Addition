package io.github.xiewuzhiying.vs_addition.util

import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc

open class ConvexPolygon(
    override var normals: Iterable<Vector3dc>,
    override var points: Iterable<Vector3dc> = emptyList(),
    override var shipFrom: ShipId? = null
) : ConvexPolygonc {
    override var aabb: AABBdc = AABBd().apply { getEnclosingAABB(this) }
}