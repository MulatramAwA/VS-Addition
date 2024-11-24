package io.github.xiewuzhiying.vs_addition.forge.mixin.createaddition.portable_energy_interface;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.forge.compats.createaddition.content.contraptions.actors.psi.PortableEnergyInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
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

    @ModifyExpressionValue(
            method = "getCapability",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/util/LazyOptional;cast()Lnet/minecraftforge/common/util/LazyOptional;"
            ),
            remap = false
    )
    private <X> LazyOptional<X> modify(LazyOptional<X> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableEnergyInterfaceWithShipController controller) {
            final LazyOptional<IEnergyStorage> capability = controller.getCapability();
            if (capability != null) {
                return capability.cast();
            }
        }
        return original;
    }

    @WrapMethod(
            method = "getEnergy",
            remap = false
    )
    private int modifyEnergy(Operation<Integer> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableEnergyInterfaceWithShipController controller) {
            final LazyOptional<IEnergyStorage> capability = controller.getCapability();
            if (capability != null) {
                return capability.map((IEnergyStorage::getEnergyStored)).orElse(-1);
            }
        }
        return original.call();
    }

    @WrapMethod(
            method = "getCapacity",
            remap = false
    )
    private int modifyCapacity(Operation<Integer> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableEnergyInterfaceWithShipController controller) {
            final LazyOptional<IEnergyStorage> capability = controller.getCapability();
            if (capability != null) {
                return capability.map(IEnergyStorage::getMaxEnergyStored).orElse(-1);
            }
        }
        return original.call();
    }
}
