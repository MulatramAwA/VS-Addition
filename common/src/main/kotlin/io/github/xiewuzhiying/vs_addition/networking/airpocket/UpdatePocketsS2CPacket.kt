package io.github.xiewuzhiying.vs_addition.networking.airpocket

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionMessage.FAKE_AIR_POCKET_SYNC_BY_ID
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.mod.common.shipObjectWorld

class UpdatePocketsS2CPacket {

    companion object {
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            val shipId = buf.readLong()
            val pocketId = buf.readLong()
            val level = ctx.player.level()
            val ship = level.shipObjectWorld.loadedShips.getById(shipId) ?: return
            ship as ClientShip
            val buf2 = FriendlyByteBuf(Unpooled.buffer())
            buf2.writeLong(shipId)
            buf2.writeLong(pocketId)
            NetworkManager.sendToServer(FAKE_AIR_POCKET_SYNC_BY_ID, buf2)
        }
    }
}