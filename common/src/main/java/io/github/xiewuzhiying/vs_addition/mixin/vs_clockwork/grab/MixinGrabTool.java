package io.github.xiewuzhiying.vs_addition.mixin.vs_clockwork.grab;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision.EntityShipCollisionDisablerS2CPacket;
import io.github.xiewuzhiying.vs_addition.context.EntityShipCollisionDisabler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.tool.GrabTool.Companion;
import org.valkyrienskies.core.api.ships.ServerShip;

@Pseudo
@Mixin(Companion.class)
public abstract class MixinGrabTool {
    @WrapMethod(
            method = "grabShip"
    )
    private void onGrab(Player player, ServerShip ship, Vector3dc grabPosInShip, Operation<Void> original) {
        if (VSAdditionConfig.COMMON.getClockwork().getDisableGrabbedShipCollision() && player != null) {
            final Level level = player.level();
            if (!level.isClientSide() && player instanceof EntityShipCollisionDisabler disabler) {
                final long shipId = ship.getId();
                disabler.addDisabledCollisionBody(shipId);
                new EntityShipCollisionDisablerS2CPacket(shipId, true, ObjectOpenHashSet.of((Entity)player)).sendToPlayers(((ServerLevel)level).players());
            }
        }
        original.call(player, ship, grabPosInShip);
    }

    @ModifyArg(
            method = "dropShip",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/core/api/ships/QueryableShipData;getById(J)Lorg/valkyrienskies/core/api/ships/Ship;",
                    remap = false
            ),
            index = 0
    )
    private long onDrop(long shipId, @Local(argsOnly = true) Player player) {
        if (VSAdditionConfig.COMMON.getClockwork().getDisableGrabbedShipCollision() && player != null) {
            final Level level = player.level();
            if (!level.isClientSide() && player instanceof EntityShipCollisionDisabler disabler) {
                disabler.removeDisabledCollisionBody(shipId);
                new EntityShipCollisionDisablerS2CPacket(shipId, false, ObjectOpenHashSet.of((Entity)player)).sendToPlayers(((ServerLevel)level).players());
            }
        }
        return shipId;
    }
}
