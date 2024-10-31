package io.github.xiewuzhiying.vs_addition

import com.mojang.authlib.minecraft.client.MinecraftClient
import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player

object PlatformUtils {
    @JvmStatic
    @ExpectPlatform
    fun isFakePlayer(player: Player): Boolean {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun getMinecraftServer(): MinecraftServer {
        throw AssertionError()
    }

    @JvmStatic
    @ExpectPlatform
    fun getMinecraftClient() : MinecraftClient {
        throw AssertionError()
    }
}