package io.github.xiewuzhiying.vs_addition.forge.mixin.createaddition.portable_energy_interface;

import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import io.github.xiewuzhiying.vs_addition.forge.compats.createaddition.content.contraptions.actors.psi.PortableEnergyInterfaceWithShipController;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.portable_interface.IPSIWithShipBehavior;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Restriction(
        require = @Condition("createaddition")
)
@Mixin(PortableStorageInterfaceBlockEntity.class)
public abstract class MixinPortableStorageInterfaceBlockEntity {
    @Inject(
            method = "initialize",
            at = @At("TAIL"),
            remap = false
    )
    private void onInitialize(CallbackInfo ci) {
        if (this instanceof IPSIWithShipBehavior behavior) {
            if ((PortableStorageInterfaceBlockEntity)(Object)this instanceof PortableEnergyInterfaceBlockEntity entity) {
                behavior.setController(new PortableEnergyInterfaceWithShipController(entity));
            }
        }
    }
}
