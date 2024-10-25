package io.github.xiewuzhiying.vs_addition.forge

import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.util.FakePlayer

object PlatformUtilsImpl {
    @JvmStatic
    fun isFakePlayer(player: Player): Boolean {
        return player is FakePlayer
    }
}