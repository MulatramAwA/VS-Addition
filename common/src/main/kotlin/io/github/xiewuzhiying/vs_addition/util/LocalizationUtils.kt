package io.github.xiewuzhiying.vs_addition.util

import net.minecraft.core.Direction
import net.minecraft.core.Direction.*
import net.minecraft.network.chat.Component

val Direction.toTranslatable : Component
    get() = when (this) {
        UP -> Component.translatable("direction.up")
        DOWN -> Component.translatable("direction.down")
        NORTH -> Component.translatable("direction.north")
        SOUTH -> Component.translatable("direction.south")
        WEST -> Component.translatable("direction.west")
        EAST -> Component.translatable("direction.east")
    }