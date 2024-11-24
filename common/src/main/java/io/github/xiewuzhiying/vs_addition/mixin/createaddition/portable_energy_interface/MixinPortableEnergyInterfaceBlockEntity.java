package io.github.xiewuzhiying.vs_addition.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PortableEnergyInterfaceBlockEntity.class)
public abstract class MixinPortableEnergyInterfaceBlockEntity {
    @WrapMethod(
            method = "getExtensionDistance",
            remap = false
    )
    private float replace(float partialTicks, Operation<Float> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP) {
            final PortableStorageInterfaceWithShipController controller = behavior.getController();
            if (controller != null) {
                return controller.getExtensionDistance(partialTicks);
            }
        }
        return original.call(partialTicks);
    }
}
