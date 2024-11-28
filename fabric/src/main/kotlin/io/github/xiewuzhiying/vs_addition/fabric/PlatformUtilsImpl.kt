package io.github.xiewuzhiying.vs_addition.fabric

import net.fabricmc.fabric.api.entity.FakePlayer
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET
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

    @JvmStatic
    fun getBucketToFluidUnit(buckets: Int) : Int {
        return buckets * BUCKET.toInt()
    }
}