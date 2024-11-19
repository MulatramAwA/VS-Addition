package io.github.xiewuzhiying.vs_addition.networking.airpocket

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketClient.setAirPockets
import io.github.xiewuzhiying.vs_addition.context.airpocket.PocketId
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionMessage.FAKE_AIR_POCKET_SYNC_ALL
import io.github.xiewuzhiying.vs_addition.util.readAABBd
import io.github.xiewuzhiying.vs_addition.util.writeAABBdc
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId

class SyncAllPocketsS2CPacket(private val shipId: ShipId, private val pockets: Map<PocketId, AABBdc>) {

    fun sendToPlayer(player: ServerPlayer) {
        NetworkManager.sendToPlayer(player, FAKE_AIR_POCKET_SYNC_ALL, getBuf())
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        val buf = getBuf()
        players.forEach {
            NetworkManager.sendToPlayer(it, FAKE_AIR_POCKET_SYNC_ALL, buf)
        }
    }

    private fun getBuf() : FriendlyByteBuf {
        val buf = FriendlyByteBuf(Unpooled.buffer());
        buf.writeLong(shipId)
        buf.writeInt(pockets.size)
        pockets.forEach {
            buf.writeLong(it.key)
            val pocket = it.value
            buf.writeAABBdc(pocket)
        }
        return buf;
    }

    companion object {
        @JvmStatic
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            val shipId = buf.readLong()
            val size = buf.readInt()
            val pockets = Long2ObjectOpenHashMap<AABBdc>()
            for (i in 0 until size) {
                pockets.put(buf.readLong(), buf.readAABBd())
            }
            setAirPockets(shipId, pockets)
        }
    }
}