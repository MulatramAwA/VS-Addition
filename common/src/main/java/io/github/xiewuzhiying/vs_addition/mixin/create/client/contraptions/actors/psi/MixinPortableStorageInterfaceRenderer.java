package io.github.xiewuzhiying.vs_addition.mixin.create.client.contraptions.actors.psi;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(PortableStorageInterfaceRenderer.class)
public abstract class MixinPortableStorageInterfaceRenderer {
    @WrapOperation(
            method = "renderSafe(Lcom/simibubi/create/content/contraptions/actors/psi/PortableStorageInterfaceBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/actors/psi/PortableStorageInterfaceBlockEntity;isConnected()Z"
            )
    )
    private boolean isConnected(PortableStorageInterfaceBlockEntity instance, Operation<Boolean> original) {
        if (instance instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            final PortableStorageInterfaceWithShipController controller = behavior.getController();
            if (controller != null) {
                return controller.isConnected();
            }
        }
        return original.call(instance);
    }
}
