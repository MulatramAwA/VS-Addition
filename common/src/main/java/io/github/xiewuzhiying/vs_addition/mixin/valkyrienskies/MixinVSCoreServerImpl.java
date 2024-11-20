/*
package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.core.impl.program.VSCoreServerImpl;

import java.util.List;

@Pseudo
@Mixin(VSCoreServerImpl.class)
public abstract class MixinVSCoreServerImpl {
    @Inject(
            method = "deleteShips",
            at = @At("HEAD")
    )
    private void deleteShips(ServerShipWorld world, List<ServerShip> ships, CallbackInfo ci) {
        ships.forEach(ship -> {
            ship.getChunkClaim().get
        });
    }
}
*/
