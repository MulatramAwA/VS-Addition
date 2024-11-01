package io.github.xiewuzhiying.vs_addition.fabric

import dan200.computercraft.api.peripheral.PeripheralLookup
import io.github.xiewuzhiying.vs_addition.VSAdditionMod.CC_ACTIVE
import io.github.xiewuzhiying.vs_addition.VSAdditionMod.init
import io.github.xiewuzhiying.vs_addition.VSAdditionMod.initClient
import io.github.xiewuzhiying.vs_addition.compats.computercraft.PeripheralCommon.registerGenericPeripheralCommon
import io.github.xiewuzhiying.vs_addition.fabric.compats.computercraft.FabricPeripheralLookup.peripheralProvider
import io.github.xiewuzhiying.vs_addition.fabric.stuff.FakeRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.mod.fabric.common.ValkyrienSkiesModFabric

class VSAdditionModFabric : ModInitializer {

    override fun onInitialize() {
        ValkyrienSkiesModFabric().onInitialize();
        init()
        if (CC_ACTIVE) {
            registerGenericPeripheralCommon()
            PeripheralLookup.get().registerFallback { level: Level, blockPos: BlockPos, _: BlockState, _: BlockEntity?, _: Direction -> peripheralProvider(level, blockPos) }
        }
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            PlatformUtilsImpl.minecraft = server
        }
    }
}
class VSAdditionModFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        initClient()
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(FakeRenderer())
        WorldRenderEvents.LAST.register(FakeRenderer())
    }
}
