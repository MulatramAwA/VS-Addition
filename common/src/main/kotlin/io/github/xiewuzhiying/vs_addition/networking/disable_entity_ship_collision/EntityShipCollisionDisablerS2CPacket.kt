package io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.context.EntityShipCollisionDisabler
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.ints.IntArrayList
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
        NetworkManager.sendToPlayers(players, ENTITY_SHIP_COLLISION_DISABLER, getBuf())
    }

    private fun getBuf() : FriendlyByteBuf {
        val buf = FriendlyByteBuf(Unpooled.buffer());
        buf.writeLong(shipId)
        buf.writeBoolean(addOrRemove)
        val list = IntArrayList()
        entities.forEach {
            list.add(it.id)
        }
        buf.writeIntIdList(list)
        return buf;
    }

    companion object {
        @JvmStatic
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            val shipId = buf.readLong()
            val addOrRemove = buf.readBoolean()
            val list = buf.readIntIdList()
            val level = ctx.player.level()
            list.forEach {
                (level.getEntity(it) as? EntityShipCollisionDisabler)?.let { entity ->
                    if (addOrRemove) {
                        entity.addDisabledCollisionBody(shipId)
                    } else {
                        entity.removeDisabledCollisionBody(shipId)
                    }
                }
            }
        }

        @JvmStatic
        val ENTITY_SHIP_COLLISION_DISABLER : ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "entity_ship_collision_disabler")
    }
}