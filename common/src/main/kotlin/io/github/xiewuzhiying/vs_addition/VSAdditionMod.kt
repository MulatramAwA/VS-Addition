package io.github.xiewuzhiying.vs_addition

import dev.architectury.event.events.client.ClientCommandRegistrationEvent
import dev.architectury.event.events.client.ClientTickEvent
import dev.architectury.event.events.common.CommandRegistrationEvent
import dev.architectury.event.events.common.EntityEvent
import dev.architectury.event.events.common.InteractionEvent
import dev.architectury.event.events.common.InteractionEvent.RightClickBlock
import dev.architectury.platform.Platform
import io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.link.DualLinkHandler
import io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.link.DualLinkRenderer
import io.github.xiewuzhiying.vs_addition.networking.VSAdditionMessage
import io.github.xiewuzhiying.vs_addition.networking.airpocket.SyncAllPocketsC2SPacket
import io.github.xiewuzhiying.vs_addition.stuff.EntityFreshCaller
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocket
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient
import org.valkyrienskies.core.impl.config.VSConfigClass
import org.valkyrienskies.core.impl.hooks.VSEvents

object VSAdditionMod {
    const val MOD_ID = "vs_addition"

    @JvmStatic var CREATE_ACTIVE = false
    @JvmStatic var CC_ACTIVE = false
    @JvmStatic var CLOCKWORK_ACTIVE = false
    @JvmStatic var CBC_ACTIVE = false
    @JvmStatic var EUREKA_ACTIVE = false
    @JvmStatic var INTERACTIVE_ACTIVE = false
    @JvmStatic var COMPUTERCRAT_ACTIVE = false
    @JvmStatic var FRAMEDBLOCKS_ACTIVE = false
    @JvmStatic var CBCMW_ACTIVE = false



    @JvmStatic
    fun init() {
        CREATE_ACTIVE = Platform.isModLoaded("create")
        CC_ACTIVE = Platform.isModLoaded("computercraft")
        CLOCKWORK_ACTIVE = Platform.isModLoaded("vs_clockwork")
        CBC_ACTIVE = Platform.isModLoaded("createbigcannons")
        EUREKA_ACTIVE = Platform.isModLoaded("vs_eureka")
        INTERACTIVE_ACTIVE = Platform.isModLoaded("create_interactive")
        COMPUTERCRAT_ACTIVE = Platform.isModLoaded("computercraft")
        FRAMEDBLOCKS_ACTIVE = Platform.isModLoaded("framedblocks")
        CBCMW_ACTIVE = Platform.isModLoaded("cbcmodernwarfare")

        VSConfigClass.registerConfig("vs_addition", VSAdditionConfig::class.java)

        EntityEvent.ADD.register(EntityEvent.Add { entity, world -> EntityFreshCaller.freshEntityInShipyard(entity, world) } )

        if (CLOCKWORK_ACTIVE) {
            InteractionEvent.RIGHT_CLICK_BLOCK.register(RightClickBlock { player, hand, pos, face ->
                DualLinkHandler.handler(player, hand, pos, face)
            })
        }

        VSAdditionMessage.registerC2SPackets()

        CommandRegistrationEvent.EVENT.register { dispatcher, registry, selection ->
            FakeAirPocket.registerCommands(dispatcher, registry, selection)
            registerCommands(dispatcher, registry, selection)
        }
    }



    @JvmStatic
    fun initClient() {
        if (CLOCKWORK_ACTIVE) {
            ClientTickEvent.CLIENT_POST.register(ClientTickEvent.Client { DualLinkRenderer.tick() })
        }

        VSAdditionMessage.registerS2CPackets()

        ClientCommandRegistrationEvent.EVENT.register { dispatcher, registry ->
            FakeAirPocketClient.registerCommands(dispatcher, registry)
        }

        VSEvents.ShipLoadEventClient.on { event ->
            if (VSAdditionConfig.COMMON.experimental.fakeAirPocket) {
                SyncAllPocketsC2SPacket(event.ship.id).sendToServer()
            }
        }
    }
}