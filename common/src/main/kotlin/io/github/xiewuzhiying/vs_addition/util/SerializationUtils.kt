package io.github.xiewuzhiying.vs_addition.util

import io.netty.buffer.ByteBuf
import net.minecraft.nbt.CompoundTag
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

val AABBdc.toCompoundTag : CompoundTag
    get() {
    val nbt = CompoundTag()
    nbt.putDouble("minX", this.minX())
    nbt.putDouble("minY", this.minY())
    nbt.putDouble("minZ", this.minZ())
    nbt.putDouble("maxX", this.maxX())
    nbt.putDouble("maxY", this.maxY())
    nbt.putDouble("maxZ", this.maxZ())
    return nbt
}

val CompoundTag.toAABBd : AABBd
    get() = AABBd(this.getDouble("minX"), this.getDouble("minY"), this.getDouble("minZ"), this.getDouble("maxX"), this.getDouble("maxY"), this.getDouble("maxZ"))