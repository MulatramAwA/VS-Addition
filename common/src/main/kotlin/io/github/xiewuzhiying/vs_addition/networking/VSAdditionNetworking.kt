package io.github.xiewuzhiying.vs_addition.networking

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.PlatformUtils
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocket
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc
import org.valkyrienskies.core.util.readVec3d
import org.valkyrienskies.core.util.writeVec3d

object VSAdditionNetworking {
    val FAKE_AIR_POCKET_PACKET_ID: ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "vs_addition_packet")
    fun register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, FAKE_AIR_POCKET_PACKET_ID)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val shipId = buf.readLong()
            val times = buf.readInt()
            val aabbs = mutableListOf<AABBdc>()
            ctx.player.sendSystemMessage(Component.literal("Received Server Packet! ShipId: $shipId Times: $times"))
            for (i in 0 until times) {
                aabbs.add(AABBd(buf.readVec3d(), buf.readVec3d()).correctBounds())
            }
            FakeAirPocketClient.addAirPockets(shipId, aabbs)
        }

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FAKE_AIR_POCKET_PACKET_ID)
        { buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext ->
            val buf2 = FriendlyByteBuf(Unpooled.buffer());
            val shipId = buf.readLong()
            val aabbs : MutableList<AABBdc> = FakeAirPocket.map[shipId] ?: return@registerReceiver
            val times = aabbs.size
            ctx.player.sendSystemMessage(Component.literal("Received Client Packet! ShipId: $shipId Times: $times"))
            buf2.writeLong(shipId)
            buf2.writeInt(aabbs.size)
            for (aabb in aabbs) {
                buf2.writeVec3d(Vector3d(aabb.minX(), aabb.minY(), aabb.minZ()))
                buf2.writeVec3d(Vector3d(aabb.maxX(), aabb.maxY(), aabb.maxZ()))
            }
            NetworkManager.sendToPlayer(PlatformUtils.getMinecraftServer().playerList.getPlayer(ctx.player.uuid), FAKE_AIR_POCKET_PACKET_ID, buf2)
        }
    }

}