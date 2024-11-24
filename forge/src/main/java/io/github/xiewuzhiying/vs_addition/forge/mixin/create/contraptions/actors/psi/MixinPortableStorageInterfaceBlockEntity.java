package io.github.xiewuzhiying.vs_addition.forge.mixin.create.contraptions.actors.psi;

import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableItemInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.compats.create.content.contraptions.actors.psi.PortableStorageInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi.PortableFluidInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.forge.compats.create.content.contraptions.actors.psi.PortableItemInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PortableStorageInterfaceBlockEntity.class)
public abstract class MixinPortableStorageInterfaceBlockEntity implements IPSIWithShipBehavior {

    @Unique
    private PortableStorageInterfaceWithShipController vs_addition$controller = null;

    @Inject(
            method = "initialize",
            at = @At("TAIL"),
            remap = false
    )
    private void onInitialize(CallbackInfo ci) {
        if ((PortableStorageInterfaceBlockEntity)(Object)this instanceof PortableItemInterfaceBlockEntity entity) {
            this.vs_addition$controller = new PortableItemInterfaceWithShipController(entity);
        } else if((PortableStorageInterfaceBlockEntity)(Object)this instanceof PortableFluidInterfaceBlockEntity entity) {
            this.vs_addition$controller = new PortableFluidInterfaceWithShipController(entity);
        }
    }

    @Override
    public void setController(PortableStorageInterfaceWithShipController controller) {
        this.vs_addition$controller = controller;
    }

    @Override
    public PortableStorageInterfaceWithShipController getController() {
        return this.vs_addition$controller;
    }
}
