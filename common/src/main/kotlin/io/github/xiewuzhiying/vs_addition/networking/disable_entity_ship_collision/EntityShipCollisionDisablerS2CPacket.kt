package io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.context.EntityShipCollisionDisabler
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import org.valkyrienskies.core.api.ships.properties.ShipId

class EntityShipCollisionDisablerS2CPacket(private val shipId: ShipId, private val addOrRemove: Boolean, private val entities: Iterable<Entity>) {

    fun sendToPlayer(player: ServerPlayer) {
        NetworkManager.sendToPlayer(player, ENTITY_SHIP_COLLISION_DISABLER, getBuf())
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        val buf = getBuf()
        players.forEach {
            NetworkManager.sendToPlayer(it, ENTITY_SHIP_COLLISION_DISABLER, buf)
        }
    }

    private fun getBuf() : FriendlyByteBuf {
        val buf = FriendlyByteBuf(Unpooled.buffer());
        buf.writeLong(shipId)
        buf.writeBoolean(addOrRemove)
        buf.writeInt(entities.count())
        entities.forEach {
            buf.writeInt(it.id)
        }
        return buf;
    }

    companion object {
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            val shipId = buf.readLong()
            val addOrRemove = buf.readBoolean()
            val level = ctx.player.level()
            val count = buf.readInt()
            for (i in 0 until count) {
                (level.getEntity(buf.readInt()) as? EntityShipCollisionDisabler)?.let {
                    if (addOrRemove) {
                        it.addDisabledCollisionBody(shipId)
                    } else {
                        it.removeDisabledCollisionBody(shipId)
                    }
                }
            }
        }

        @JvmStatic
        val ENTITY_SHIP_COLLISION_DISABLER : ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "entity_ship_collision_disabler")
    }
}