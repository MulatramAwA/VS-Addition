package io.github.xiewuzhiying.vs_addition.context

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision.EntityShipCollisionDisablerS2CPacket
import io.github.xiewuzhiying.vs_addition.util.errorText1
import it.unimi.dsi.fastutil.longs.LongSet
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import org.valkyrienskies.core.api.ships.properties.ShipId

interface EntityShipCollisionDisabler {
    fun getDisabledCollisionBodies() : LongSet
    fun setDisabledCollisionBodies(list: LongSet)
    fun addDisabledCollisionBody(shipId : ShipId)
    fun removeDisabledCollisionBody(shipId : ShipId)
}

fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>, registry: CommandBuildContext, selection: Commands.CommandSelection) {
    dispatcher.register(Commands.literal("entity-ship-collision")
        .requires { source -> VSAdditionConfig.COMMON.experimental.fakeAirPocket && source.hasPermission(1) }
        .then(
            Commands.argument("entities", EntityArgument.entities())
            .then(
                Commands.argument("ship", LongArgumentType.longArg())
                    .then(
                        Commands.argument("boolean", BoolArgumentType.bool())
                            .executes {
                                val entities = EntityArgument.getEntities(it, "entities") ?: return@executes 0
                                val shipId = LongArgumentType.getLong(it, "ship")
                                val bl = BoolArgumentType.getBool(it, "boolean")
                                if (bl) {
                                    entities.forEach { entity ->
                                        if (entity is EntityShipCollisionDisabler) {
                                            entity.removeDisabledCollisionBody(shipId)
                                            it.source.player?.sendSystemMessage(Component.literal("Enabled!"))
                                        } else {
                                            it.source.player?.sendSystemMessage(errorText1)
                                        }
                                    }

                                } else {
                                    entities.forEach { entity ->
                                        (entity as EntityShipCollisionDisabler).addDisabledCollisionBody(shipId)
                                        it.source.player?.sendSystemMessage(Component.literal("Disabled!"))
                                    }
                                }
                                EntityShipCollisionDisablerS2CPacket(shipId, bl, entities).sendToPlayers(it.source.level.players())
                                return@executes 1
                            }
                    )
            )
        )
    )
}