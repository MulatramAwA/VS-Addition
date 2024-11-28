package io.github.xiewuzhiying.vs_addition.forge.compats.computercraft.peripherals

import com.simibubi.create.content.kinetics.base.KineticBlockEntity
import dan200.computercraft.api.lua.LuaFunction
import dan200.computercraft.api.peripheral.GenericPeripheral
import dan200.computercraft.api.peripheral.PeripheralType
import io.github.xiewuzhiying.vs_addition.VSAdditionMod
import io.github.xiewuzhiying.vs_addition.forge.mixin.cbcmodernwarfare.CompactCannonMountBlockEntityAccessor
import io.github.xiewuzhiying.vs_addition.mixinducks.createbigcannons.MountedAutocannonContraptionMixinDuck
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import rbasamoyai.createbigcannons.cannon_control.ControlPitchContraption
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption

open class CompactCannonMountMethods : GenericPeripheral {
    override fun id(): String {
        return "${VSAdditionMod.MOD_ID}:cbc_cannon_mount"
    }

    override fun getType(): PeripheralType {
        return PeripheralType.ofAdditional("cbc_cannon_mount")
    }

    @LuaFunction(mainThread = true)
    fun assemble(tileEntity: CompactCannonMountBlockEntityAccessor): Any {
        tileEntity as KineticBlockEntity
        tileEntity as ControlPitchContraption.Block
        if (!tileEntity.`vs_addition$isRunning`()) {
            tileEntity.`vs_addition$assemble`()
            (tileEntity.`vs_addition$getContraption`()?.contraption as AbstractMountedCannonContraption).onRedstoneUpdate(
                tileEntity.level as ServerLevel,
                tileEntity.`vs_addition$getContraption`(),
                false,
                0,
                tileEntity
            )
            return true
        }
        return false
    }

    @LuaFunction(mainThread = true)
    fun disassemble(tileEntity: CompactCannonMountBlockEntityAccessor): Any {
        tileEntity as KineticBlockEntity
        if (tileEntity.`vs_addition$isRunning`()) {
            tileEntity.`vs_addition$disassemble`()
            tileEntity.sendData()
            return true
        }
        return false
    }

    @LuaFunction(mainThread = true)
    fun fire(tileEntity: CompactCannonMountBlockEntityAccessor) {
        if (tileEntity.`vs_addition$getContraption`()?.level() is ServerLevel) {
            (tileEntity.`vs_addition$getContraption`()?.contraption as? MountedAutocannonContraptionMixinDuck)?.`vs_addition$setIsCalledByComputer`()
            (tileEntity.`vs_addition$getContraption`()?.contraption as AbstractMountedCannonContraption).fireShot(tileEntity.`vs_addition$getContraption`()?.level() as ServerLevel, tileEntity.`vs_addition$getContraption`())
        }
    }

    @LuaFunction(mainThread = true)
    fun isRunning(tileEntity: CompactCannonMountBlockEntityAccessor): Boolean {
        return tileEntity.`vs_addition$isRunning`()
    }

    @LuaFunction
    fun getPitch(tileEntity: CompactCannonMountBlockEntityAccessor): Double {
        return tileEntity.`vs_addition$getCannonPitch`().toDouble()
    }

    @LuaFunction
    fun getYaw(tileEntity: CompactCannonMountBlockEntityAccessor): Double {
        return tileEntity.`vs_addition$getCannonYaw`().toDouble()
    }

    @LuaFunction
    fun getX(tileEntity: CompactCannonMountBlockEntityAccessor): Int {
        return tileEntity.`vs_addition$getControllerBlockPos`().x
    }

    @LuaFunction
    fun getY(tileEntity: CompactCannonMountBlockEntityAccessor): Int {
        return tileEntity.`vs_addition$getControllerBlockPos`().y
    }

    @LuaFunction
    fun getZ(tileEntity: CompactCannonMountBlockEntityAccessor): Int {
        return tileEntity.`vs_addition$getControllerBlockPos`().z
    }

    @LuaFunction
    fun getMaxDepress(tileEntity: CompactCannonMountBlockEntityAccessor): Double? {
        return tileEntity.`vs_addition$getContraption`()?.maximumDepression()?.toDouble()
    }

    @LuaFunction
    fun getMaxElevate(tileEntity: CompactCannonMountBlockEntityAccessor): Double? {
        return tileEntity.`vs_addition$getContraption`()?.maximumElevation()?.toDouble()
    }

    @LuaFunction
    fun getDirection(tileEntity: CompactCannonMountBlockEntityAccessor): String {
        return (tileEntity as  KineticBlockEntity).blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toString()
    }
}