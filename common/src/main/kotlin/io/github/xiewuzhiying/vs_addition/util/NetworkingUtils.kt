package io.github.xiewuzhiying.vs_addition.util

import io.netty.buffer.ByteBuf
import org.joml.primitives.AABBd
import org.joml.primitives.AABBdc

fun ByteBuf.writeAABBdc(aabBdc: AABBdc) {
    this.writeDouble(aabBdc.minX())
    this.writeDouble(aabBdc.minY())
    this.writeDouble(aabBdc.minZ())
    this.writeDouble(aabBdc.maxX())
    this.writeDouble(aabBdc.maxY())
    this.writeDouble(aabBdc.maxZ())
}

fun ByteBuf.readAABBd(): AABBd {
    return AABBd(this.readDouble(), this.readDouble(), this.readDouble(), this.readDouble(), this.readDouble(), this.readDouble())
}