package io.github.xiewuzhiying.vs_addition.fabric.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.fabric.compats.createaddition.content.contraptions.actors.psi.InterfaceEnergyHandler;
import io.github.xiewuzhiying.vs_addition.fabric.compats.createaddition.content.contraptions.actors.psi.PortableEnergyInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PortableEnergyInterfaceBlockEntity.class)
public abstract class MixinPortableEnergyInterfaceBlockEntity {
    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void onInit(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getController() instanceof PortableEnergyInterfaceWithShipController controller) {
            controller.setCapability(controller.createEmptyHandler(behavior));
        }
    }

    @WrapMethod(
            method = "getEnergy",
            remap = false
    )
    private long modifyEnergy(Operation<Long> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableEnergyInterfaceWithShipController controller) {
            final InterfaceEnergyHandler capability = controller.getCapability();
            if (capability != null) {
                return capability.getAmount();
            }
        }
        return original.call();
    }

    @WrapMethod(
            method = "getCapacity",
            remap = false
    )
    private long modifyCapacity(Operation<Long> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableEnergyInterfaceWithShipController controller) {
            final InterfaceEnergyHandler capability = controller.getCapability();
            if (capability != null) {
                return capability.getCapacity();
            }
        }
        return original.call();
    }
}
