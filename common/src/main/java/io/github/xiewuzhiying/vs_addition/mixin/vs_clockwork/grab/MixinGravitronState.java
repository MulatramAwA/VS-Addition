package io.github.xiewuzhiying.vs_addition.mixin.vs_clockwork.grab;

import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.context.EntityShipCollisionDisabler;
import io.github.xiewuzhiying.vs_addition.networking.disable_entity_ship_collision.EntityShipCollisionDisablerS2CPacket;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.clockwork.content.curiosities.tools.gravitron.GravitronState;

@Mixin(GravitronState.Companion.class)
public abstract class MixinGravitronState {
    @Inject(
            method = "leftClickItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
            )
    )
    private void leftClickItem(Player player, GravitronState state, CallbackInfoReturnable<Boolean> cir) {
        if (VSAdditionConfig.COMMON.getClockwork().getDisableGrabbedShipCollision() && player != null) {
            final Level level = player.level();
            if (!level.isClientSide()) {
                Long shipId = state.getShipID();
                if (shipId != null && player instanceof EntityShipCollisionDisabler disabler) {
                    disabler.removeDisabledCollisionBody(shipId);
                    new EntityShipCollisionDisablerS2CPacket(shipId, false, ObjectOpenHashSet.of((Entity)player)).sendToPlayers(((ServerLevel)level).players());
                }
            }
        }
    }
}
