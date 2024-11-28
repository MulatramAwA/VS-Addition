package io.github.xiewuzhiying.vs_addition.forge.compats.computercraft.peripherals

import dan200.computercraft.api.lua.LuaFunction
import io.github.xiewuzhiying.vs_addition.forge.mixin.cbcmodernwarfare.CompactCannonMountBlockEntityAccessor

class CheatCompactCannonMountMethods : CompactCannonMountMethods() {
    @LuaFunction(mainThread = true)
    fun setPitch(tileEntity: CompactCannonMountBlockEntityAccessor, value: Double) {
        if (this.isRunning(tileEntity)) tileEntity.`vs_addition$setPitch`(value.toFloat())
    }

    @LuaFunction(mainThread = true)
    fun setYaw(tileEntity: CompactCannonMountBlockEntityAccessor, value: Double) {
        if (this.isRunning(tileEntity)) tileEntity.`vs_addition$setYaw`(value.toFloat())
    }
}