package io.github.xiewuzhiying.vs_addition.networking.airpocket

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionMessage.FAKE_AIR_POCKET_SYNC_BY_ID
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketClient.setAirPocket
import io.github.xiewuzhiying.vs_addition.context.airpocket.PocketId
import io.github.xiewuzhiying.vs_addition.util.readAABBd
import io.github.xiewuzhiying.vs_addition.util.writeAABBdc
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.api.ships.properties.ShipId

class SyncSinglePocketS2CPacket(private val shipId: ShipId, private val pocketId: PocketId, private val aabb: AABBdc) {

    fun sendToPlayer(player: ServerPlayer) {
        NetworkManager.sendToPlayer(player, FAKE_AIR_POCKET_SYNC_BY_ID, getBuf())
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        val buf = getBuf()
        players.forEach {
            NetworkManager.sendToPlayer(it, FAKE_AIR_POCKET_SYNC_BY_ID, buf)
        }
    }

    private fun getBuf() : FriendlyByteBuf {
        val buf = FriendlyByteBuf(Unpooled.buffer());
        buf.writeLong(shipId)
        buf.writeLong(pocketId)
        buf.writeAABBdc(aabb)
        return buf;
    }


    companion object {
        @JvmStatic
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            setAirPocket(buf.readLong(), buf.readLong(), buf.readAABBd())
        }
    }
}