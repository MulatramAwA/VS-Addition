package io.github.xiewuzhiying.vs_addition

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.world.entity.player.Player

object PlatformUtils {
    @JvmStatic
    @ExpectPlatform
    fun isFakePlayer(player: Player): Boolean {
        throw AssertionError()
    }
}