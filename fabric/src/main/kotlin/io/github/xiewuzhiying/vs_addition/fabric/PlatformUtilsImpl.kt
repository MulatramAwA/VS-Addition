package io.github.xiewuzhiying.vs_addition.fabric

import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player

object PlatformUtilsImpl {
    var minecraft: MinecraftServer? = null

    @JvmStatic
    fun isFakePlayer(player: Player): Boolean {
        return player is FakePlayer
    }

    @JvmStatic
    fun getMinecraftServer(): MinecraftServer {
        return minecraft ?: throw IllegalStateException("Cannot get MinecraftServer!")
    }
}