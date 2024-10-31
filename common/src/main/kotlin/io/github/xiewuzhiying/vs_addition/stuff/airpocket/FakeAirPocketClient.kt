package io.github.xiewuzhiying.vs_addition.stuff.airpocket

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionNetworking.REQUEST_ALL_FAKE_AIR_POCKET
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionNetworking.REQUEST_FAKE_AIR_POCKET_BY_ID
import io.github.xiewuzhiying.vs_addition.util.ShipUtils.getLoadedShipsIntersecting
import io.netty.buffer.Unpooled
import io.netty.util.collection.LongObjectHashMap
import io.netty.util.collection.LongObjectMap
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.ServerShip
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

object FakeAirPocketClient {
    val map : LongObjectMap<LongObjectMap<AABBdc>> = LongObjectHashMap()

    fun addAirPocket(shipId: ShipId, pocketId: PocketId, aabb : AABBdc) {
        map[shipId]?.put(pocketId, aabb)
    }

    fun addAirPockets(shipId: ShipId, pockets : LongObjectMap<AABBdc>) {
        map[shipId] = pockets
    }

    fun requestAirPocket(shipId: ShipId, pocketId : PocketId) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeLong(shipId)
        buf.writeLong(pocketId)
        NetworkManager.sendToServer(REQUEST_FAKE_AIR_POCKET_BY_ID, buf)
    }

    fun requestAllAirPockets(shipId: ShipId) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeLong(shipId)
        NetworkManager.sendToServer(REQUEST_ALL_FAKE_AIR_POCKET, buf)
    }

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
        RenderType.CompositeState.builder().setShaderState(
            RenderStateShard.RENDERTYPE_WATER_MASK_SHADER
        ).setTextureState(RenderStateShard.NO_TEXTURE).setWriteMaskState(
            RenderStateShard.DEPTH_WRITE
        ).createCompositeState(false)
    )

    fun render(ms: PoseStack, camera: Camera, bufferSource: MultiBufferSource?) {
        val consumer = bufferSource?.getBuffer(WATER_MASK) ?: return
        val cameraPosition = camera.position
        val shipClientWorld = ((Minecraft.getInstance() as IShipObjectWorldClientProvider).shipObjectWorld as ShipObjectClientWorld)
        val loadedShips = shipClientWorld.loadedShips
        map.entries.forEach { entry ->
            val transform = loadedShips.getById(entry.key)?.renderTransform
            entry.value.forEach { (pocketId, aabb) ->
                /*if (timer == 0) {
                    timer = 250
                }
                timer -= 1*/
                ms.pushPose()
                val extent = Vector3d()
                aabb.extent(extent)
                val k: Double = -extent.x()
                val l: Double = -extent.y()
                val m: Double = -extent.z()
                val n: Double = extent.x()
                val o: Double = extent.y()
                val p: Double = extent.z()
                val vertices : List<Vector3dc> = listOf(
                    Vector3d(k, l, m),
                    Vector3d(n, l, m),
                    Vector3d(k, o, m),
                    Vector3d(n, o, m),
                    Vector3d(k, l, p),
                    Vector3d(n, l, p),
                    Vector3d(k, o, p),
                    Vector3d(n, o, p)
                )
                val plane = if (transform != null) {
                    Planed(transform.worldToShip.transformDirection(Vector3d(0.0, 1.0, 0.0)), transform.worldToShip.transformPosition(Vector3d(0.0, SEA_LEVEL, 0.0)).sub(aabb.center(Vector3d())))
                } else {
                    Planed(Vector3d(0.0, 1.0, 0.0), Vector3d(0.0, SEA_LEVEL, 0.0))
                }
                //debug("plane", "$plane")
                val section = calculateSection(vertices, plane)
                /*section.forEach {
                    debug("section", "(${it.x()}, ${it.y()}, ${it.z()})")
                }*/
                val vertices2 = sortVertices(section, plane.normal())
                /*vertices2.forEach {
                    debug("vertices2", "(${it.x()}, ${it.y()}, ${it.z()})")
                }*/
                renderPolygon(vertices2, consumer, ms, transform, cameraPosition.toJOML(), aabb.center(Vector3d()))

                ms.popPose()
            }
        }
    }

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

    private fun renderPolygon(vertices: List<Vector3dc>, consumer: VertexConsumer, ms: PoseStack, transform: ShipTransform?, camera: Vector3dc, offset: Vector3dc) {
        if (vertices.size < 3) return

        ms.pushPose()
        if (transform != null) {
            VSClientGameUtils.transformRenderWithShip(transform, ms, offset.x(), offset.y(), offset.z(), camera.x(), camera.y() + ZFIGHT, camera.z())
        } else {
            ms.translate(offset.x() - camera.x(), offset.y() - (camera.y() + ZFIGHT), offset.z() - camera.z())
        }
        val matrix1 = ms.last().pose()
        for (i in 1 until vertices.size - 1) {
            consumer.vertex(matrix1, vertices[0].x().toFloat(), vertices[0].y().toFloat(), vertices[0].z().toFloat()).endVertex()
            consumer.vertex(matrix1, vertices[i].x().toFloat(), vertices[i].y().toFloat(), vertices[i].z().toFloat()).endVertex()
            consumer.vertex(matrix1, vertices[i + 1].x().toFloat(), vertices[i + 1].y().toFloat(), vertices[i + 1].z().toFloat()).endVertex()
            //debug("renderPolygon", "(${vertices[i].x()}, ${vertices[i].y()}, ${vertices[i].z()})")
        }
        ms.popPose()
        ms.pushPose()
        if (transform != null) {
            VSClientGameUtils.transformRenderWithShip(transform, ms, offset.x(), offset.y(), offset.z(), camera.x(), camera.y(), camera.z())
        } else {
            ms.translate(offset.x() - camera.x(), offset.y() - camera.y(), offset.z() - camera.z())
        }
        val matrix2 = ms.last().pose()
        for (i in 1 until vertices.size - 1) {
            consumer.vertex(matrix2, vertices[0].x().toFloat(), vertices[0].y().toFloat(), vertices[0].z().toFloat()).endVertex()
            consumer.vertex(matrix2, vertices[i + 1].x().toFloat(), vertices[i + 1].y().toFloat(), vertices[i + 1].z().toFloat()).endVertex()
            consumer.vertex(matrix2, vertices[i].x().toFloat(), vertices[i].y().toFloat(), vertices[i].z().toFloat()).endVertex()
        }
        ms.popPose()
    }

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

                    val buf = FriendlyByteBuf(Unpooled.buffer())
                    buf.writeLong(ship.id)
                    NetworkManager.sendToServer(REQUEST_ALL_FAKE_AIR_POCKET, buf)

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
}