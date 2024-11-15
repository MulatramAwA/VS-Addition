package io.github.xiewuzhiying.vs_addition.mixin.create.client.kinetics.mechanical_arm;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointHandler;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(ArmInteractionPointHandler.class)
public abstract class MixinArmInteractionPointHandler {

    @Redirect(
            method = "flushSettings",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"
            )
    )
    private static boolean closerThan(BlockPos instance, Vec3i vec3i, double v, @Local(ordinal = 0) ArmInteractionPoint point ) {
        if(!VSAdditionConfig.CLIENT.getEnablePointRemoval())
            return true;
        return ConversionUtilsKt.squaredDistanceBetweenInclShips(point.getLevel(), instance, vec3i) <= Mth.square(v);
    }
}
