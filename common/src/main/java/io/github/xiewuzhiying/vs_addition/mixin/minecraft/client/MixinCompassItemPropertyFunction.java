package io.github.xiewuzhiying.vs_addition.mixin.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(CompassItemPropertyFunction.class)
public abstract class MixinCompassItemPropertyFunction {
    @ModifyExpressionValue(
            method = "getAngleFromEntityToPos",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 transformToWorldSpace(Vec3 original) {
        return ConversionUtilsKt.toRenderWorldCoordinates(original);
    }
}
