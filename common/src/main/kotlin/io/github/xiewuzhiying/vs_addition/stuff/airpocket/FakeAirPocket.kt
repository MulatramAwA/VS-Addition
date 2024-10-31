package io.github.xiewuzhiying.vs_addition.stuff.airpocket

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionNetworking.REQUEST_ALL_FAKE_AIR_POCKET
import io.github.xiewuzhiying.vs_addition.util.ShipUtils.getLoadedShipsIntersecting
import io.github.xiewuzhiying.vs_addition.util.ShipUtils.getShipManagingPos2
import io.netty.buffer.Unpooled
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.BlockHitResult
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider
import org.valkyrienskies.core.util.writeVec3d
import org.valkyrienskies.mod.common.ValkyrienSkiesMod.vsCore


object FakeAirPocket {

    @JvmOverloads
    fun checkIfPointInAirPocket(point: Vector3dc, level : ServerLevel, checkRange: AABBdc? = null) : Boolean {
        val ships: Iterable<LoadedShip> =  level.getLoadedShipsIntersecting(checkRange ?: AABBd(point, Vector3d(point.x() + 1, point.y() + 1, point.z() + 1)))
        val iterator = ships.iterator()
        while (iterator.hasNext()) {
            val ship: ServerShip = iterator.next() as ServerShip
            val controller = FakeAirPocketController.getOrCreate(ship, level)
            controller.getAllAirPocket().forEach { (id, pocket) ->
                if (pocket.containsPoint(ship.worldToShip.transformPosition(point, Vector3d()))) {
                    return true
                }
            }
        }
        return false
    }

    @JvmOverloads
    fun checkIfAABBInAirPocket(aabb: AABBdc, level : ServerLevel, mustBeContained : Boolean = false, checkRange: AABBdc? = null) : Boolean {
        val ships: Iterable<LoadedShip> =  level.getLoadedShipsIntersecting(checkRange ?: aabb)
        val iterator = ships.iterator()
        while (iterator.hasNext()) {
            val ship: ServerShip = iterator.next() as ServerShip
            val controller = FakeAirPocketController.getOrCreate(ship, level)
            var polygon: ConvexPolygonc? = null
            controller.getAllAirPocket().forEach { (_, pocket) ->
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
    fun checkIfPointAndAABBInAirPocket(point: Vector3dc, aabb: AABBdc, level : ServerLevel, mustBeContained : Boolean = false, checkRange: AABBdc? = null) : Pair<Boolean, Boolean> {
        var pointBl = false
        var aabbBl = false
        val ships: Iterable<LoadedShip> =  level.getLoadedShipsIntersecting(checkRange ?: aabb)
        val iterator = ships.iterator()
        while (iterator.hasNext()) {
            val ship: ServerShip = iterator.next() as ServerShip
            val controller = FakeAirPocketController.getOrCreate(ship, level)
            var polygon: ConvexPolygonc? = null
            controller.getAllAirPocket().forEach { (_, pocket) ->
                if (!pointBl) {
                    if (pocket.containsPoint(ship.worldToShip.transformPosition(point, Vector3d()))) {
                        pointBl = true
                    }
                }
                if (!aabbBl) {
                    if (!mustBeContained) {
                        if (polygon == null) {
                            polygon = getCollider().createPolygonFromAABB(aabb, ship.transform.worldToShip, ship.id)
                        }
                        polygon!!.points.forEach { point2 ->
                            if (pocket.containsPoint(point2)) {
                                aabbBl = true
                            }
                        }
                    } else {
                        if (polygon == null) {
                            polygon = getCollider().createPolygonFromAABB(aabb, ship.transform.worldToShip, ship.id)
                        }
                        val notContained = mutableListOf(false)
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

    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>, registry: CommandBuildContext, selection: Commands.CommandSelection) {
        dispatcher.register(Commands.literal("fake-air-pocket")
            .requires { source -> VSAdditionConfig.COMMON.experimental.fakeAirPocket && source.hasPermission(1) }
            .then(Commands.literal("add")
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                .executes { context: CommandContext<CommandSourceStack> ->
                                    val source = context.source
                                    val level = source.level

                                    val pos1 = BlockPosArgument.getBlockPos(context, "pos1")
                                    val pos2 = BlockPosArgument.getBlockPos(context, "pos2")
                                    val ship1 = level.getShipManagingPos2(pos1) as? ServerShip?
                                    val ship2 = level.getShipManagingPos2(pos2) as? ServerShip?
                                    if (ship1 != ship2) {
                                        return@executes 0
                                    }
                                    val ship = ship1 ?: ship2 ?: return@executes 0
                                    val controller = FakeAirPocketController.getOrCreate(ship, level)
                                    val pocketId = controller.addAirPocket(
                                        AABBd(pos1.x.coerceAtMost(pos2.x).toDouble(), pos1.y.coerceAtMost(pos2.y).toDouble(), pos1.z.coerceAtMost(pos2.z).toDouble(),
                                        pos1.x.coerceAtLeast(pos2.x) + 1.0, pos1.y.coerceAtLeast(pos2.y) + 1.0, pos1.z.coerceAtLeast(pos2.z) + 1.0)
                                        .correctBounds()
                                    )
                                    source.player?.sendSystemMessage(Component.literal("Add fake air pocket from (${pos1.x}, ${pos1.y}, ${pos1.z}) to (${pos2.x}, ${pos2.y}, ${pos2.z}), ShipID: ${ship.id}, PocketID: ${pocketId}"))
                                    val pockets = controller.getAllAirPocket()
                                    val buf = FriendlyByteBuf(Unpooled.buffer());
                                    buf.writeLong(ship.id)
                                    buf.writeInt(pockets.size)
                                    pockets.forEach {
                                        buf.writeLong(it.key)
                                        val pocket = it.value
                                        buf.writeVec3d(Vector3d(pocket.minX(), pocket.minY(), pocket.minZ()))
                                        buf.writeVec3d(Vector3d(pocket.maxX(), pocket.maxY(), pocket.maxZ()))
                                    }
                                    level.players().forEach {
                                        NetworkManager.sendToPlayer(it, REQUEST_ALL_FAKE_AIR_POCKET, buf)
                                    }
                                    source.player?.sendSystemMessage(Component.literal("Called /fake-air-pocket"))

                                    return@executes 1
                                }
                        )
                )
            )
            .then(Commands.literal("remove")
                .then(Commands.argument("shipId", LongArgumentType.longArg())
                    .then(Commands.argument("pocketId", LongArgumentType.longArg())
                        .executes { context: CommandContext<CommandSourceStack> ->
                            val source = context.source
                            val level = source.level

                            val shipId = LongArgumentType.getLong(context, "shipId")
                            val controller = FakeAirPocketController.getOrCreate(shipId, level) ?: return@executes 0
                            val pocketId = LongArgumentType.getLong(context, "pocketId")
                            controller.removeAirPocket(pocketId)
                            val pockets = controller.getAllAirPocket()
                            val buf = FriendlyByteBuf(Unpooled.buffer());
                            buf.writeLong(shipId)
                            buf.writeInt(pockets.size)
                            pockets.forEach {
                                buf.writeLong(it.key)
                                val pocket = it.value
                                buf.writeVec3d(Vector3d(pocket.minX(), pocket.minY(), pocket.minZ()))
                                buf.writeVec3d(Vector3d(pocket.maxX(), pocket.maxY(), pocket.maxZ()))
                            }
                            level.players().forEach {
                                NetworkManager.sendToPlayer(it, REQUEST_ALL_FAKE_AIR_POCKET, buf)
                            }
                            return@executes 1
                        }
                    )
                )
            )
            .then(Commands.literal("getIdAndSlug")
                .executes { context: CommandContext<CommandSourceStack> ->
                    val source = context.source
                    val player = source.player ?: return@executes 0
                    val ray = player.pick(10.0, 1.0.toFloat(), false) ?: return@executes 0
                    val level = source.level
                    if (ray !is BlockHitResult) return@executes 0
                    val ship = level.getShipManagingPos2(ray.location) ?: return@executes 0
                    player.sendSystemMessage(Component.literal("ShipID: ${ship.id}, ShipSlug: ${ship.slug}"))
                    return@executes 1
                }
            )
        )
    }
}