package io.github.xiewuzhiying.vs_addition.fabric.mixin.create.contraptions.actors.psi;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi.InterfaceFluidHandler;
import io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi.PortableFluidInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PortableFluidInterfaceBlockEntity.class)
public abstract class MixinPortableFluidInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

    public MixinPortableFluidInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void onInit(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getController() instanceof PortableFluidInterfaceWithShipController controller) {
            controller.setCapability(controller.createEmptyHandler(behavior));
        }
    }

    @WrapMethod(
            method = "getFluidStorage",
            remap = false
    )
    private Storage<FluidVariant> getFluidStorage(@Nullable Direction face, Operation<Storage<FluidVariant>> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableFluidInterfaceWithShipController controller) {
            final InterfaceFluidHandler capability = controller.getCapability();
            if (capability != null) {
                return capability;
            }
        }
        return original.call(face);
    }
}
