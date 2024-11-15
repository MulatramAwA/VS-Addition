package io.github.xiewuzhiying.vs_addition.mixin.create.kinetics.mechanicalArm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import io.github.xiewuzhiying.vs_addition.util.ConversionUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity.getRange;

@Pseudo
@Mixin(ArmBlockEntity.class)
public abstract class MixinArmBlockEntity extends KineticBlockEntity {

    @Shadow(remap = false)
    boolean updateInteractionPoints;

    public MixinArmBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @WrapOperation(
            method = "searchForItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPoint;isValid()Z"
            ),
            remap = false
    )
    public boolean searchForItem(ArmInteractionPoint instance, Operation<Boolean> original, @Local ArmInteractionPoint armInteractionPoint) {
        Vector3d armPos = ConversionUtilsKt.getCenterJOMLD(getBlockPos());
        Vector3d pointPos = ConversionUtilsKt.getCenterJOMLD(instance.getPos());
        if (ConversionUtilsKt.squaredDistanceBetweenInclShips(getLevel(), armPos, pointPos) > Mth.square(getRange())) {
            return false;
        } else {
            return original.call(instance);
        }
    }


    @WrapOperation(
            method = "searchForDestination",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/mechanicalArm/ArmInteractionPoint;isValid()Z"
            ),
            remap = false
    )
    public boolean searchForDestination(ArmInteractionPoint instance, Operation<Boolean> original, @Local ArmInteractionPoint armInteractionPoint) {
        Vector3d armPos = ConversionUtilsKt.getCenterJOMLD(getBlockPos());
        Vector3d pointPos = ConversionUtilsKt.getCenterJOMLD(instance.getPos());
        if (ConversionUtilsKt.squaredDistanceBetweenInclShips(getLevel(), armPos, pointPos) > Mth.square(getRange()))
            return false;
        else {
            return original.call(instance);
        }
    }

    @Inject(
            method = "lazyTick",
            at = @At("HEAD"),
            remap = false
    )
    public void updatePoints(CallbackInfo ci) {
        updateInteractionPoints = true;
    }
}
