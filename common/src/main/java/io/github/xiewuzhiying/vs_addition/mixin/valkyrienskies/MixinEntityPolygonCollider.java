package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.valkyrienskies.core.impl.collision.k;

@Mixin(k.class)
public abstract class MixinEntityPolygonCollider {
    @ModifyConstant(
            method = "a(Lorg/joml/primitives/AABBdc;Lorg/joml/Vector3dc;DLjava/util/List;)Lkotlin/Pair;",
            constant = @Constant(doubleValue = 45.0),
            require = 0,
            remap = false
    )
    private double makeThisConfigurable(double constant) {
        return VSAdditionConfig.COMMON.getMaxTiltAngle();
    }
}
