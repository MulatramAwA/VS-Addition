package io.github.xiewuzhiying.vs_addition.mixin.create.contraptions.actors.psi;

import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PortableItemInterfaceBlockEntity.class)
public abstract class MixinPortableItemInterfaceBlockEntity {
    @Inject(
            method = "invalidateCapability",
            at = @At("TAIL"),
            remap = false
    )
    private void invalidateCapability(CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior) {
            final PortableStorageInterfaceWithShipController controller = behavior.getController();
            if (controller != null) {
                controller.invalidateCapability();
            }
        }
    }
}
