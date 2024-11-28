package io.github.xiewuzhiying.vs_addition.mixin.vs_clockwork.flapBearing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.valkyrienskies.clockwork.content.contraptions.flap.FlapBearingBlockEntity;

@Pseudo
@Mixin(FlapBearingBlockEntity.class)
public interface FlapBearingBlockEntityAccessor {
    @Accessor(remap = false)
    float getBearingAngle();
}
