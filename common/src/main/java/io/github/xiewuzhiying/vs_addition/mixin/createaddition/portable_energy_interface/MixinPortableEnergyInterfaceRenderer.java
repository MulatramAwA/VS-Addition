package io.github.xiewuzhiying.vs_addition.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceRenderer;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortableEnergyInterfaceRenderer.class)
public abstract class MixinPortableEnergyInterfaceRenderer {
    @WrapOperation(
            method = "renderSafe(Lcom/mrh0/createaddition/blocks/portable_energy_interface/PortableEnergyInterfaceBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mrh0/createaddition/blocks/portable_energy_interface/PortableEnergyInterfaceBlockEntity;isConnected()Z"
            ),
            remap = false
    )
    private boolean isConnected(PortableEnergyInterfaceBlockEntity instance, Operation<Boolean> original) {
        if (instance instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            final PortableStorageInterfaceWithShipController controller = behavior.getController();
            if (controller != null) {
                return controller.isConnected();
            }
        }
        return original.call(instance);
    }
}
