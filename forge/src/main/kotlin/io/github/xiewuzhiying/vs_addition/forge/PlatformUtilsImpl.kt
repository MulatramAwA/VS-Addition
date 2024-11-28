package io.github.xiewuzhiying.vs_addition.forge

import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.fluids.FluidType.BUCKET_VOLUME
import net.minecraftforge.server.ServerLifecycleHooks

object PlatformUtilsImpl {
    @JvmStatic
    fun isFakePlayer(player: Player): Boolean {
        return player is FakePlayer
    }

    @JvmStatic
    fun getMinecraftServer(): MinecraftServer {
        return ServerLifecycleHooks.getCurrentServer() ?: throw IllegalStateException("Cannot get MinecraftServer!")
    }

    @JvmStatic
    fun getBucketToFluidUnit(buckets: Int) : Int {
        return buckets * BUCKET_VOLUME
    }
}