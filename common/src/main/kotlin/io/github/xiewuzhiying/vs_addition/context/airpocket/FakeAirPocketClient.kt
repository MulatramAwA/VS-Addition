package io.github.xiewuzhiying.vs_addition.context.airpocket

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.networking.airpocket.SyncAllPocketsC2SPacket
import io.github.xiewuzhiying.vs_addition.util.*
import io.netty.util.collection.LongObjectHashMap
import io.netty.util.collection.LongObjectMap
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import org.joml.Matrix4f
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.joml.primitives.Planed
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.Ship
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.api.ships.properties.ShipTransform
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider
import org.valkyrienskies.core.impl.game.ships.ShipObjectClientWorld
import org.valkyrienskies.mod.common.IShipObjectWorldClientProvider
import org.valkyrienskies.mod.common.VSClientGameUtils
import org.valkyrienskies.mod.common.ValkyrienSkiesMod.vsCore
import org.valkyrienskies.mod.common.util.toJOML
import kotlin.math.atan2

object FakeAirPocketClient {
    @JvmStatic
    val map : LongObjectMap<LongObjectMap<AABBdc>> = LongObjectHashMap()

    @JvmStatic
    fun setAirPocket(shipId: ShipId, pocketId: PocketId, aabb : AABBdc) {
        map[shipId]?.put(pocketId, aabb)
    }

    @JvmStatic
    fun setAirPockets(shipId: ShipId, pockets : LongObjectMap<AABBdc>) {
        map[shipId] = pockets
    }

    @JvmStatic
    @JvmOverloads
    fun checkIfPointInAirPocket(point: Vector3dc, checkRange: AABBdc? = null) : Boolean {
        val level = Minecraft.getInstance().level
        val ships: Iterable<LoadedShip> =  level.getLoadedShipsIntersecting(checkRange ?: AABBd(point, Vector3d(point.x() + 1, point.y() + 1, point.z() + 1)))
        val iterator = ships.iterator()
        while (iterator.hasNext()) {
            val ship: Ship = iterator.next()
            val shipId = ship.id
            val pockets: LongObjectMap<AABBdc> = map[shipId] ?: continue
            pockets.forEach { (_, pocket) ->
                if (pocket.containsPoint(ship.worldToShip.transformPosition(point, Vector3d()))) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    @JvmOverloads
    fun checkIfAABBInAirPocket(aabb: AABBdc, mustBeContained : Boolean = false, checkRange: AABBdc? = null) : Boolean {
        val level = Minecraft.getInstance().level
        val ships: Iterable<LoadedShip> =  level.getLoadedShipsIntersecting(checkRange ?: aabb)
        val iterator = ships.iterator()
        while (iterator.hasNext()) {
            val ship: Ship = iterator.next()
            val shipId = ship.id
            val pockets: LongObjectMap<AABBdc> = map[shipId] ?: continue
            var polygon: ConvexPolygonc? = null
            pockets.forEach { (_, pocket) ->
                if (polygon == null) polygon = getCollider().createPolygonFromAABB(aabb, ship.transform.worldToShip, ship.id)
                if (!mustBeContained) {
                    polygon!!.points.forEach { point ->
                        if (pocket.containsPoint(ship.worldToShip.transformPosition(point, Vector3d()))) {
                            return true
                        }
                    }
                } else {
                    val notContained = mutableListOf(false)
                    polygon!!.points.forEach { point ->
                        if (!pocket.containsPoint(ship.worldToShip.transformPosition(point, Vector3d()))) {
                            notContained[0] = true
                        }
                    }
                    if (!notContained[0]) {
                        return true
                    }
                }
            }
        }
        return false
    }

    @JvmStatic
    @JvmOverloads
    fun checkIfPointAndAABBInAirPocket(point: Vector3dc, aabb: AABBdc, mustBeContained : Boolean = false, checkRange: AABBdc? = null) : Pair<Boolean, Boolean> {
        val level = Minecraft.getInstance().level
        var pointBl = false
        var aabbBl = false
        val ships: Iterable<LoadedShip> =  level.getLoadedShipsIntersecting(checkRange ?: aabb)
        val iterator = ships.iterator()
        while (iterator.hasNext()) {
            val ship: Ship = iterator.next()
            val shipId = ship.id
            val pockets: LongObjectMap<AABBdc> = map[shipId] ?: continue
            var polygon: ConvexPolygonc? = null
            pockets.forEach { (_, pocket) ->
                if (!pointBl) {
                    if (pocket.containsPoint(ship.worldToShip.transformPosition(point, Vector3d()))) {
                        pointBl = true
                    }
                }
                if (!aabbBl) {
                    if (!mustBeContained) {
                        if (polygon == null) polygon = getCollider().createPolygonFromAABB(aabb, ship.worldToShip, ship.id)
                        polygon!!.points.forEach { point2 ->
                            if (pocket.containsPoint(point2)) {
                                aabbBl = true
                            }
                        }
                    } else {
                        if (polygon == null) polygon = getCollider().createPolygonFromAABB(aabb, ship.worldToShip, ship.id)
                        val notContained: MutableList<Boolean> = mutableListOf(false)
                        polygon!!.points.forEach { point2 ->
                            if (!pocket.containsPoint(point2)) {
                                notContained[0] = true
                            }
                        }
                        aabbBl = !notContained[0]
                    }
                }
                if (pointBl && aabbBl) {
                    return Pair(true, true)
                }
            }
        }
        return Pair(pointBl, aabbBl)
    }

    private var collider : EntityPolygonCollider? = null

    @JvmStatic
    private fun getCollider(): EntityPolygonCollider {
        if (collider == null) {
            collider = vsCore.entityPolygonCollider
        }
        return collider!!
    }

    private val WATER_MASK: RenderType.CompositeRenderType = RenderType.create(
        "water_mask_triangles",
        DefaultVertexFormat.POSITION,
        VertexFormat.Mode.TRIANGLES,
        256,
        RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_WATER_MASK_SHADER)
            .setTextureState(RenderStateShard.NO_TEXTURE)
            .setWriteMaskState(RenderStateShard.DEPTH_WRITE)
            .createCompositeState(false)
    )

    @JvmStatic
    fun render(ms: PoseStack, camera: Camera, bufferSource: MultiBufferSource?) {
        if (!(VSAdditionConfig.COMMON.experimental.fakeAirPocket && VSAdditionConfig.CLIENT.experimental.cullWaterSurfaceInFakeAirPocket)) return
        val waterMaskConsumer = bufferSource?.getBuffer(WATER_MASK) ?: return
        val shipClientWorld = ((Minecraft.getInstance() as? IShipObjectWorldClientProvider)?.shipObjectWorld as? ShipObjectClientWorld) ?: return
        val cameraPosition = camera.position.toJOML()
        val loadedShips = shipClientWorld.loadedShips
        map.entries.forEach ship@{ entry ->
            val transform = loadedShips.getById(entry.key)?.renderTransform ?: return@ship
            entry.value.forEach pocket@{ (pocketId, aabb) ->
                val aabb2 = aabb.moveToOrigin()
                val plane = Planed(transform.worldToShip.transformPosition(Vector3d(0.0, SEA_LEVEL, 0.0)).sub(aabb.center(Vector3d())), transform.worldToShip.transformDirection(Vector3d(0.0, 1.0, 0.0)))

                val mask = mutableListOf<Vector3dc>()

                if (aabb2.testPlane(plane)) {
                    aabb2.toLineSegments().forEach { line ->
                        val dest = Vector3d()
                        if (line.intersect(plane, dest)) {
                            mask.add(dest)
                        }
                    }
                }

                /*val section = aabb2.toPoints().filter { it.isBehindPlane(plane) }.toMutableList()
                mask.forEach { section.add(it) }*/

                val cameraPosition2 = transform.worldToShip.transformPosition(Vector3d(cameraPosition)).sub(aabb.center(Vector3d()))

                /*aabb2.toPlanes()
                    .mapNotNull { aabbPlane ->
                        val list = section.filter { it.isOnPlane(aabbPlane) }
                        if (list.size < 3) {
                            return@mapNotNull null
                        } else {
                            return@mapNotNull Triple(list, aabbPlane.normal, aabbPlane)
                        }
                    }
                    .forEach { face ->
                        renderMask2(ms, transform, cameraPosition, aabb.center(Vector3d()), sortVertices(face.first, face.second), waterSurfaceConsumer, cameraPosition2.isInFrontOfPlane(face.third))
                    }*/

                renderMask(ms, transform, cameraPosition, aabb.center(Vector3d()), sortVertices(mask, plane.normal), waterMaskConsumer, cameraPosition2.isInFrontOfPlane(plane))
            }
        }
    }

    private fun renderMask(ms: PoseStack, transform: ShipTransform, camera: Vector3dc, offset: Vector3dc, vertices: List<Vector3dc>, maskConsumer: VertexConsumer, bl: Boolean) {
        if (vertices.size < 3) return

        ms.pushPose()
        VSClientGameUtils.transformRenderWithShip(
            transform,
            ms,
            offset.x(),
            offset.y(),
            offset.z(),
            camera.x(),
            camera.y(),
            camera.z()
        )
        renderTriangle(maskConsumer, ms.last().pose(), vertices, true)
        ms.popPose()

        ms.pushPose()
        VSClientGameUtils.transformRenderWithShip(
            transform,
            ms,
            offset.x(),
            offset.y(),
            offset.z(),
            camera.x(),
            camera.y() + ZFIGHT,
            camera.z()
        )
        renderTriangle(maskConsumer, ms.last().pose(), vertices, false)
        ms.popPose()
    }

    private fun renderTriangle(consumer: VertexConsumer, matrix: Matrix4f, vertices: List<Vector3dc>, order: Boolean) {
        for (i in 1 until vertices.size - 1) {
            val (first, finally) = if (order) {
                Pair(i+1, 0)
            } else {
                Pair(0, i+1)
            }
            consumer
                .vertex(matrix, vertices[first].x().toFloat(), vertices[first].y().toFloat(), vertices[first].z().toFloat())
                .uv(0f, 0f)
                .endVertex()

            consumer
                .vertex(matrix, vertices[i].x().toFloat(), vertices[i].y().toFloat(), vertices[i].z().toFloat())
                .uv(0f, 1f)
                .endVertex()

            consumer
                .vertex(matrix, vertices[finally].x().toFloat(), vertices[finally].y().toFloat(), vertices[finally].z().toFloat())
                .uv(1f, 0f)
                .endVertex()
        }
    }

    private fun sortVertices(vertices: List<Vector3dc>, planeNormal: Vector3dc): List<Vector3dc> {
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


    fun renderHighLight(ms: PoseStack, camera: Camera, bufferSource: MultiBufferSource?) {
        if (!(VSAdditionConfig.COMMON.experimental.fakeAirPocket && VSAdditionConfig.CLIENT.experimental.highLightFakedAirPocket) || bufferSource == null) return
        val cameraPosition = camera.position
        val shipClientWorld = ((Minecraft.getInstance() as IShipObjectWorldClientProvider).shipObjectWorld as ShipObjectClientWorld)
        val loadedShips = shipClientWorld.loadedShips
        map.entries.forEach { entry ->
            val transform = loadedShips.getById(entry.key)?.renderTransform ?: return@forEach
            entry.value.forEach { (pocketId, aabb) ->
                val center = aabb.center(Vector3d())
                val shipVoxelAABBAfterOffset = AABB(aabb.minX() - center.x(), aabb.minY() - center.y(), aabb.minZ() - center.z(), aabb.maxX() - center.x(), aabb.maxY() - center.y(), aabb.maxZ() - center.z())
                ms.pushPose()
                VSClientGameUtils.transformRenderWithShip(transform, ms, center.x(), center.y(), center.z(), cameraPosition.x(), cameraPosition.y(), cameraPosition.z())
                LevelRenderer.renderLineBox(ms, bufferSource.getBuffer(RenderType.lines()), shipVoxelAABBAfterOffset, 0.0F, 0.0F, 1.0F, 1.0F);
                ms.popPose()
            }
        }
    }

    @JvmStatic
    fun registerCommands(dispatcher: CommandDispatcher<ClientCommandRegistrationEvent.ClientCommandSourceStack>, context: CommandBuildContext) {
        dispatcher.register(
            ClientCommandRegistrationEvent.literal("fresh-fake-air-pocket")
                .requires{ source -> source.hasPermission(1) && VSAdditionConfig.COMMON.experimental.fakeAirPocket }
                .executes { ctx: CommandContext<ClientCommandRegistrationEvent. ClientCommandSourceStack> ->
                    val player = ctx.source.`arch$getPlayer`()
                    val ray = player.pick(10.0, 1.0.toFloat(), false)

                    if (ray !is BlockHitResult) return@executes 0
                    val ship = VSClientGameUtils.getClientShip(ray.blockPos.x.toDouble(),
                        ray.blockPos.y.toDouble(), ray.blockPos.z.toDouble()
                    ) ?: return@executes 0

                    SyncAllPocketsC2SPacket(ship.id).sendToServer()

                    player.sendSystemMessage(Component.literal("Called /fresh-fake-air-pocket"))
                    return@executes 1
                }
        )
        dispatcher.register(
            ClientCommandRegistrationEvent.literal("get-ship-id-and-slug")
                .requires{ source -> source.hasPermission(1) }
                .executes { ctx: CommandContext<ClientCommandRegistrationEvent. ClientCommandSourceStack> ->
                    val player = ctx.source.`arch$getPlayer`()
                    val ray = player.pick(10.0, 1.0.toFloat(), false)

                    if (ray !is BlockHitResult) return@executes 0
                    val ship = VSClientGameUtils.getClientShip(ray.blockPos.x.toDouble(), ray.blockPos.y.toDouble(), ray.blockPos.z.toDouble()) ?: return@executes 0

                    player.sendSystemMessage(Component.literal("ShipId: ${ship.id} ShipSlug ${ship.slug}"))
                    return@executes 1
                }
        )
    }




    private const val WATER_OFFSET : Double = 8.0 / 9.0

    private const val ZFIGHT : Double = 0.002

    @JvmStatic
    private var SEA_LEVEL = VSAdditionConfig.CLIENT.experimental.seaLevel + WATER_OFFSET

    @JvmStatic
    private var timer = 0

    @JvmStatic
    private fun debug(title: String, message: String) {
        if (timer == 0) {
            Minecraft.getInstance().level?.profiler?.push(title)
            println(message)
            Minecraft.getInstance().level?.profiler?.pop()
        }
    }
}