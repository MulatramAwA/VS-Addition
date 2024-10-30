package io.github.xiewuzhiying.vs_addition.stuff.airpocket

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionNetworking.FAKE_AIR_POCKET_PACKET_ID
import io.github.xiewuzhiying.vs_addition.util.ShipUtils.getShipManagingPos2
import io.netty.buffer.Unpooled
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId
import org.valkyrienskies.core.util.writeVec3d


object FakeAirPocket {
    val map : MutableMap<ShipId, MutableList<AABBdc>> = mutableMapOf()

    @JvmOverloads
    fun addAirPocket(id: ShipId, aabb: AABBdc, player: Player? = null) {
        map[id]?.add(aabb) ?: map.put(id, mutableListOf(aabb))
        val pos1 = Vector3d(aabb.minX(), aabb.minY(), aabb.minZ())
        val pos2 = Vector3d(aabb.maxX(), aabb.maxY(), aabb.maxZ())
        player?.sendSystemMessage(Component.literal("Add fake air pocket from (${pos1.x}, ${pos1.y}, ${pos1.z}) to (${pos2.x}, ${pos2.y}, ${pos2.z})"))
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
                                    val ship1 = level.getShipManagingPos2(pos1)
                                    val ship2 = level.getShipManagingPos2(pos2)
                                    if (ship1 != ship2) {
                                        return@executes 0
                                    }
                                    val shipId = ship1?.id ?: ship2?.id ?: return@executes 0
                                    addAirPocket(ship1?.id ?: ship2?.id ?: return@executes 0,
                                        AABBd(pos1.x.coerceAtMost(pos2.x).toDouble(), pos1.y.coerceAtMost(pos2.y).toDouble(), pos1.z.coerceAtMost(pos2.z).toDouble(),
                                            pos1.x.coerceAtLeast(pos2.x) + 1.0, pos1.y.coerceAtLeast(pos2.y) + 1.0, pos1.z.coerceAtLeast(pos2.z) + 1.0)
                                            .correctBounds()
                                        , source.player)
                                    source.player?.sendSystemMessage(Component.literal("Add fake air pocket from (${pos1.x}, ${pos1.y}, ${pos1.z}) to (${pos2.x}, ${pos2.y}, ${pos2.z}), ShipId: $shipId"))
                                    val aabbs: MutableList<AABBdc> = map[shipId] ?: return@executes 0
                                    val buf = FriendlyByteBuf(Unpooled.buffer())
                                    buf.writeLong(shipId)
                                    buf.writeInt(aabbs.size)
                                    for (aabb in aabbs) {
                                        buf.writeVec3d(Vector3d(aabb.minX(), aabb.minY(), aabb.minZ()))
                                        buf.writeVec3d(Vector3d(aabb.maxX(), aabb.maxY(), aabb.maxZ()))
                                    }

                                    source.player?.let {
                                        NetworkManager.sendToPlayer(it, FAKE_AIR_POCKET_PACKET_ID, buf)
                                        it.sendSystemMessage(Component.literal("Called /fake-air-pocket"))
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
                    player.sendSystemMessage(Component.literal("ShipId: ${ship.id}, ShipSlug: ${ship.slug}"))
                    return@executes 1
                }
            )
        )
    }
}
