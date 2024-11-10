package io.github.xiewuzhiying.vs_addition.networking.airpocket

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.PlatformUtils
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionMessage.FAKE_AIR_POCKET_SYNC_BY_ID
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketController
import io.github.xiewuzhiying.vs_addition.context.airpocket.PocketId
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import org.valkyrienskies.core.api.ships.properties.ShipId

class SyncSinglePocketC2SPacket(private val shipId: ShipId, private val pocketId: PocketId) {

    fun sendToServer() {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeLong(shipId)
        buf.writeLong(pocketId)
        NetworkManager.sendToServer(FAKE_AIR_POCKET_SYNC_BY_ID, buf)
    }

    companion object {
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            PlatformUtils.getMinecraftServer().playerList.getPlayer(ctx.player.uuid)?.let {
                val shipId = buf.readLong()
                val pocketId = buf.readLong()
                val level = ctx.player.level()
                val controller = FakeAirPocketController.getOrCreate(shipId, level as ServerLevel) ?: return
                val pocket = controller.getAirPocket(pocketId) ?: return
                SyncSinglePocketS2CPacket(shipId, pocketId, pocket).sendToPlayer(it)
            }
        }
    }
}