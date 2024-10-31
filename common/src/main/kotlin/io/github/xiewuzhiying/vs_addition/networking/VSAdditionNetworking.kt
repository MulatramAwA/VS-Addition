package io.github.xiewuzhiying.vs_addition.networking

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.PlatformUtils
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketController
import io.netty.buffer.Unpooled
import io.netty.util.collection.LongObjectHashMap
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.core.util.readVec3d
import org.valkyrienskies.core.util.writeVec3d
import org.valkyrienskies.mod.common.shipObjectWorld

object VSAdditionNetworking {
    val UPDATE_FAKE_AIR_POCKET: ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "update_air_pocket")
    val REQUEST_FAKE_AIR_POCKET_BY_ID : ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "request_air_pocket")
    val REQUEST_ALL_FAKE_AIR_POCKET : ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "request_all_air_pocket")

    fun registerServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_FAKE_AIR_POCKET_BY_ID)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val shipId = buf.readLong()
            val pocketId = buf.readLong()
            val level = ctx.player.level()
            val controller = FakeAirPocketController.getOrCreate(shipId, level as ServerLevel) ?: return@registerReceiver
            val pocket = controller.getAirPocket(pocketId) ?: return@registerReceiver
            val buf2 = FriendlyByteBuf(Unpooled.buffer());

            buf2.writeLong(shipId)
            buf2.writeLong(pocketId)
            buf2.writeVec3d(Vector3d(pocket.minX(), pocket.minY(), pocket.minZ()))
            buf2.writeVec3d(Vector3d(pocket.maxX(), pocket.maxY(), pocket.maxZ()))
            NetworkManager.sendToPlayer(PlatformUtils.getMinecraftServer().playerList.getPlayer(ctx.player.uuid), REQUEST_FAKE_AIR_POCKET_BY_ID, buf2)
        }
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, REQUEST_ALL_FAKE_AIR_POCKET)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val shipId = buf.readLong()
            val level = ctx.player.level()
            val controller = FakeAirPocketController.getOrCreate(shipId, level as ServerLevel) ?: return@registerReceiver
            val pockets = controller.getAllAirPocket()
            val buf2 = FriendlyByteBuf(Unpooled.buffer());
            buf2.writeLong(shipId)
            buf2.writeInt(pockets.size)
            pockets.forEach {
                buf2.writeLong(it.key)
                val pocket = it.value
                buf2.writeVec3d(Vector3d(pocket.minX(), pocket.minY(), pocket.minZ()))
                buf2.writeVec3d(Vector3d(pocket.maxX(), pocket.maxY(), pocket.maxZ()))
            }
            NetworkManager.sendToPlayer(PlatformUtils.getMinecraftServer().playerList.getPlayer(ctx.player.uuid), REQUEST_ALL_FAKE_AIR_POCKET, buf2)
        }
    }

    fun registerClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, UPDATE_FAKE_AIR_POCKET)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val shipId = buf.readLong()
            val pocketId = buf.readLong()
            val level = ctx.player.level()
            val ship = level.shipObjectWorld.loadedShips.getById(shipId) ?: return@registerReceiver
            ship as ClientShip
            val buf2 = FriendlyByteBuf(Unpooled.buffer())
            buf2.writeLong(shipId)
            buf2.writeLong(pocketId)
            NetworkManager.sendToServer(REQUEST_FAKE_AIR_POCKET_BY_ID, buf2)
        }
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, REQUEST_FAKE_AIR_POCKET_BY_ID)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val shipId = buf.readLong()
            val pocketId = buf.readLong()
            val pos1 = buf.readVec3d()
            val pos2 = buf.readVec3d()
            val aabb = AABBd(pos1, pos2)
            FakeAirPocketClient.setAirPocket(shipId, pocketId, aabb)
        }
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, REQUEST_ALL_FAKE_AIR_POCKET)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val shipId = buf.readLong()
            val size = buf.readInt()
            val pockets = LongObjectHashMap<AABBdc>()
            for (i in 0 until size) {
                val id = buf.readLong()
                val pos1 = buf.readVec3d()
                val pos2 = buf.readVec3d()
                pockets.put(id, AABBd(pos1, pos2))
            }
            FakeAirPocketClient.setAirPockets(shipId, pockets)
        }
    }
}