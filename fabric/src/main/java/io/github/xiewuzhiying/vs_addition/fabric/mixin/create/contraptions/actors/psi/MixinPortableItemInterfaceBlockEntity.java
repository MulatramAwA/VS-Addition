package io.github.xiewuzhiying.vs_addition.fabric.mixin.create.contraptions.actors.psi;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi.InterfaceItemHandler;
import io.github.xiewuzhiying.vs_addition.fabric.compats.create.content.contraptions.actors.psi.PortableItemInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
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
@Mixin(PortableItemInterfaceBlockEntity.class)
public abstract class MixinPortableItemInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {

    public MixinPortableItemInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "<init>",
            at = @At("TAIL"),
            remap = false
    )
    private void onInit(BlockEntityType<?> type, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getController() instanceof PortableItemInterfaceWithShipController controller) {
            controller.setCapability(controller.createEmptyHandler(behavior));
        }
    }

    @WrapMethod(
            method = "getItemStorage",
            remap = false
    )
    private Storage<ItemVariant> getItemStorage(@Nullable Direction face, Operation<Storage<ItemVariant>> original) {
        if (this instanceof IPSIWithShipBehavior behavior && behavior.getWorkingMode().get() == IPSIWithShipBehavior.WorkigMode.WITH_SHIP && behavior.getController() instanceof PortableItemInterfaceWithShipController controller) {
            final InterfaceItemHandler capability = controller.getCapability();
            if (capability != null) {
                return capability;
            }
        }
        return original.call(face);
    }
}
