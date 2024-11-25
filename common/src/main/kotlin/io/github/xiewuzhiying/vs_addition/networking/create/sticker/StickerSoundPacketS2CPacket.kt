package io.github.xiewuzhiying.vs_addition.networking.create.sticker

import com.simibubi.create.content.contraptions.chassis.StickerBlock
import com.simibubi.create.content.contraptions.chassis.StickerBlockEntity
import com.simibubi.create.content.contraptions.glue.SuperGlueItem
import dev.architectury.networking.NetworkManager
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.netty.buffer.Unpooled
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

class StickerSoundPacketS2CPacket(private val blockPos: BlockPos, private val attach: Boolean) {

    fun sendToPlayer(player: ServerPlayer) {
        NetworkManager.sendToPlayer(player, STICKER_SOUND, getBuf())
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        val buf = getBuf()
        players.forEach {
            NetworkManager.sendToPlayer(it, STICKER_SOUND, buf)
        }
    }

    private fun getBuf() : FriendlyByteBuf {
        val buf = FriendlyByteBuf(Unpooled.buffer());
        buf.writeBlockPos(blockPos)
        buf.writeBoolean(attach)
        return buf;
    }

    companion object {
        @JvmStatic
        fun receive(buf: FriendlyByteBuf, ctx: NetworkManager.PacketContext) {
            val blockPos = buf.readBlockPos()
            val attach = buf.readBoolean()

            val level = ctx.player.level()
            val be = level.getBlockEntity(blockPos)
            if (be is StickerBlockEntity) {
                if (attach) {
                    SuperGlueItem.spawnParticles(
                        level,
                        blockPos,
                        level.getBlockState(blockPos).getValue(StickerBlock.FACING),
                        true
                    )
                }

                be.playSound(attach)
            }
        }
        @JvmStatic
        val STICKER_SOUND = ResourceLocation(VSAdditionMod.MOD_ID, "sticker_sound")
    }
}