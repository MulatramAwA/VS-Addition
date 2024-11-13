package io.github.xiewuzhiying.vs_addition.networking

import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.networking.airpocket.*
import io.github.xiewuzhiying.vs_addition.networking.create.sticker.StickerSoundPacketS2CPacket
import io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision.EntityShipCollisionDisablerS2CPacket
import io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision.EntityShipCollisionDisablerS2CPacket.Companion.ENTITY_SHIP_COLLISION_DISABLER
import net.minecraft.resources.ResourceLocation

object VSAdditionMessage {
    @JvmStatic
    val FAKE_AIR_POCKET_UPDATE: ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "air_pocket_update")

    @JvmStatic
    val FAKE_AIR_POCKET_SYNC_BY_ID : ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "air_pocket_sync_by_id")

    @JvmStatic
    val FAKE_AIR_POCKET_SYNC_ALL : ResourceLocation = ResourceLocation(VSAdditionMod.MOD_ID, "air_pocket_sync_all")

    @JvmStatic
    fun registerC2SPackets() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FAKE_AIR_POCKET_SYNC_BY_ID, SyncSinglePocketC2SPacket::receive)
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, FAKE_AIR_POCKET_SYNC_ALL, SyncAllPocketsC2SPacket::receive)
    }

    @JvmStatic
    fun registerS2CPackets() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, FAKE_AIR_POCKET_UPDATE, UpdatePocketsS2CPacket::receive)
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, FAKE_AIR_POCKET_SYNC_BY_ID, SyncSinglePocketS2CPacket::receive)
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, FAKE_AIR_POCKET_SYNC_ALL, SyncAllPocketsS2CPacket::receive)
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ENTITY_SHIP_COLLISION_DISABLER, EntityShipCollisionDisablerS2CPacket::receive)
        if (VSAdditionMod.CREATE_ACTIVE) {
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, StickerSoundPacketS2CPacket.STICKER_SOUND, StickerSoundPacketS2CPacket::receive)
        }
    }
}