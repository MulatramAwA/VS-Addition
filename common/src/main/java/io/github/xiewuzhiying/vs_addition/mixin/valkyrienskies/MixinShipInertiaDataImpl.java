package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import io.github.xiewuzhiying.vs_addition.mixinducks.valkyrienskies.ShipInertiaDataImplMixinDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.core.impl.game.ships.ShipInertiaDataImpl;

@Pseudo
@Mixin(value = ShipInertiaDataImpl.class, remap = false)
public abstract class MixinShipInertiaDataImpl implements ShipInertiaDataImplMixinDuck {
    @Shadow protected abstract void addMassAt(double x, double y, double z, double addedMass);

    @Override
    public void vs_addition$addMassAt(double x, double y, double z, double mass) {
        this.addMassAt(x, y, z, mass);
    }
}
