package io.github.xiewuzhiying.vs_addition.networking.airpocket

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.PlatformUtils
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionMessage.FAKE_AIR_POCKET_SYNC_ALL
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketController
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import org.valkyrienskies.core.api.ships.properties.ShipId

class SyncAllPocketsC2SPacket(private val shipId: ShipId) {

    fun sendToServer() {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeLong(shipId)
        NetworkManager.sendToServer(FAKE_AIR_POCKET_SYNC_ALL, buf)
    }

    companion object {
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            PlatformUtils.getMinecraftServer().playerList.getPlayer(ctx.player.uuid) ?.let {
                val shipId = buf.readLong()
                val level = ctx.player.level()
                val controller = FakeAirPocketController.getOrCreate(shipId, level as ServerLevel) ?: return
                SyncAllPocketsS2CPacket(shipId,  controller.getAllAirPocket()).sendToPlayer(it)
            }
        }
    }
}