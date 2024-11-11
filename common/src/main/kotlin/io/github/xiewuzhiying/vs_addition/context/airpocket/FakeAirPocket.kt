package io.github.xiewuzhiying.vs_addition.context.airpocket

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.networking.airpocket.SyncAllPocketsS2CPacket
import io.github.xiewuzhiying.vs_addition.networking.airpocket.SyncSinglePocketS2CPacket
import io.github.xiewuzhiying.vs_addition.util.getLoadedShipsIntersecting
import io.github.xiewuzhiying.vs_addition.util.toDirection
import io.github.xiewuzhiying.vs_addition.util.toTranslatable
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.LoadedShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider
import org.valkyrienskies.mod.common.ValkyrienSkiesMod.vsCore
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toJOML


object FakeAirPocket {

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>, registry: CommandBuildContext, selection: Commands.CommandSelection) {
        dispatcher.register(Commands.literal("fake-air-pocket")
            .requires { source -> VSAdditionConfig.COMMON.experimental.fakeAirPocket && source.hasPermission(1) }
            .then(Commands.literal("add")
                .then(Commands.argument("blockPos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("blockPos2", BlockPosArgument.blockPos())
                                .executes { context: CommandContext<CommandSourceStack> ->
                                    val source = context.source
                                    val level = source.level

                                    val pos1 = BlockPosArgument.getBlockPos(context, "blockPos1")
                                    val pos2 = BlockPosArgument.getBlockPos(context, "blockPos2")
                                    val ship1 = level.getShipManagingPos(pos1)
                                    val ship2 = level.getShipManagingPos(pos2)
                                    if (ship1 != ship2) {
                                        return@executes 0
                                    }
                                    val ship = ship1 ?: ship2 ?: return@executes 0
                                    val controller = FakeAirPocketController.getOrCreate(ship, level)
                                    val aabb = AABBd(pos1.x.coerceAtMost(pos2.x).toDouble(), pos1.y.coerceAtMost(pos2.y).toDouble(), pos1.z.coerceAtMost(pos2.z).toDouble(),
                                        pos1.x.coerceAtLeast(pos2.x) + 1.0, pos1.y.coerceAtLeast(pos2.y) + 1.0, pos1.z.coerceAtLeast(pos2.z) + 1.0)
                                        .correctBounds()
                                    val pocketId = controller.addAirPocket(aabb)

                                    source.player?.sendSystemMessage(Component.translatable("vs_addition.command.fake_air_pocket.add", pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, ship.id, pocketId))

                                    SyncSinglePocketS2CPacket(ship.id, pocketId, aabb).sendToPlayers(level.players())

                                    return@executes 1
                                }
                        )
                )
                .then(Commands.argument("vector1", Vec3Argument.vec3())
                    .then(Commands.argument("vector2", Vec3Argument.vec3())
                        .executes { context: CommandContext<CommandSourceStack> ->
                            val source = context.source
                            val level = source.level

                            val pos1 = Vec3Argument.getVec3(context, "vector1")
                            val pos2 = Vec3Argument.getVec3(context, "vector2")
                            val ship1 = level.getShipManagingPos(pos1)
                            val ship2 = level.getShipManagingPos(pos2)
                            if (ship1 != ship2) {
                                return@executes 0
                            }
                            val ship = ship1 ?: ship2 ?: return@executes 0
                            val controller = FakeAirPocketController.getOrCreate(ship as ServerShip, level)
                            val aabb = AABBd(pos1.toJOML(), pos2.toJOML()).correctBounds()
                            val pocketId = controller.addAirPocket(AABBd(pos1.toJOML(), pos2.toJOML()).correctBounds())

                            source.player?.sendSystemMessage(Component.translatable("vs_addition.command.fake_air_pocket.add", pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z, ship.id, pocketId))

                            SyncSinglePocketS2CPacket(ship.id, pocketId, aabb).sendToPlayers(level.players())

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

                            source.player?.sendSystemMessage(Component.translatable("vs_addition.command.fake_air_pocket.remove", shipId, pocketId))

                            SyncAllPocketsS2CPacket(shipId, controller.getAllAirPocket()).sendToPlayers(level.players())

                            return@executes 1
                        }
                    )
                    .then(Commands.argument("all", StringArgumentType.string())
                        .executes { context: CommandContext<CommandSourceStack> ->
                            val source = context.source
                            val level = source.level

                            val shipId = LongArgumentType.getLong(context, "shipId")
                            val controller = FakeAirPocketController.getOrCreate(shipId, level) ?: return@executes 0
                            if ("all" != (StringArgumentType.getString(context, "all") ?: return@executes 0)) return@executes 0
                            controller.removeAllAirPocket()

                            SyncAllPocketsS2CPacket(shipId, controller.getAllAirPocket()).sendToPlayers(level.players())

                            return@executes 1
                        }
                    )
                )
            )
            .then(Commands.literal("extend")
                .then(Commands.argument("shipId", LongArgumentType.longArg())
                    .then(Commands.argument("pocketId", LongArgumentType.longArg())
                        .then(Commands.argument("distance", DoubleArgumentType.doubleArg())
                            .executes { context: CommandContext<CommandSourceStack> ->
                                val player = context.source.player ?: return@executes 0
                                val source = context.source
                                val level = source.level

                                val shipId = LongArgumentType.getLong(context, "shipId")
                                val ship = level.shipObjectWorld.allShips.getById(shipId) ?: return@executes 0
                                val controller = FakeAirPocketController.getOrCreate(ship, level)
                                val pocketId = LongArgumentType.getLong(context, "pocketId")
                                val aabb = AABBd(controller.getAirPocket(pocketId) ?: return@executes 0)
                                val distance = DoubleArgumentType.getDouble(context, "distance")
                                val view = player.getViewVector(1.0f).toJOML()
                                ship.worldToShip.transformDirection(view)
                                val direction = view.toDirection()
                                when (direction) {
                                    Direction.UP -> {
                                        aabb.maxY += distance
                                    }
                                    Direction.DOWN -> {
                                        aabb.minY -= distance
                                    }
                                    Direction.EAST -> {
                                        aabb.maxX += distance
                                    }
                                    Direction.WEST -> {
                                        aabb.minX -= distance
                                    }
                                    Direction.SOUTH -> {
                                        aabb.maxZ += distance
                                    }
                                    Direction.NORTH -> {
                                        aabb.minZ -= distance
                                    }
                                }
                                controller.setAirPocket(pocketId, aabb)

                                source.player?.sendSystemMessage(Component.translatable("vs_addition.command.fake_air_pocket.extend", distance, direction.toTranslatable, shipId, pocketId))

                                SyncAllPocketsS2CPacket(shipId, controller.getAllAirPocket()).sendToPlayers(level.players())

                                return@executes 1
                            }
                        )
                    )
                )
            )
            .then(Commands.literal("get-all-pocket")
                .then(Commands.argument("shipId", LongArgumentType.longArg())
                    .executes { context: CommandContext<CommandSourceStack> ->
                        val player = context.source.player ?: return@executes 0
                        val source = context.source
                        val level = source.level

                        val shipId = LongArgumentType.getLong(context, "shipId")
                        val ship = level.shipObjectWorld.allShips.getById(shipId) ?: return@executes 0
                        val controller = FakeAirPocketController.getOrCreate(ship, level)
                        val pockets = controller.getAllAirPocket()
                        pockets.forEach { pocket ->
                            val aabb = pocket.value
                            player.sendSystemMessage(Component.translatable("vs_addition.command.fake_air_pocket.get", aabb.minX(), aabb.minY(), aabb.minZ(), aabb.maxX(), aabb.maxY(), aabb.maxZ(), pocket.key))
                        }
                        return@executes 1
                    }
                )
            )
        )
    }
}
