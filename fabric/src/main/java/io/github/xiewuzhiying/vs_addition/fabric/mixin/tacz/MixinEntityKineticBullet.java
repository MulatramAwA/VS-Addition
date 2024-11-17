package io.github.xiewuzhiying.vs_addition.fabric.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.HitResultMixinDuck;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityKineticBullet.class)
public abstract class MixinEntityKineticBullet {

    @WrapOperation(
            method = "onBulletTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tacz/guns/entity/EntityKineticBullet;onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void getOriginPos(EntityKineticBullet instance, BlockHitResult result, Vec3 fireState, Vec3 offsetPos, Operation<Void> original) {
        if (result instanceof HitResultMixinDuck duck) {
            final Vec3 originPos = duck.vs_addition$getOriginPos();
            if (originPos != null) {
                duck.vs_addition$setLocation(duck.vs_addition$getOriginPos());
                original.call(instance, duck, fireState, duck.vs_addition$getOriginPos());
                return;
            }
        }
        original.call(instance, result, fireState, offsetPos);
    }

}
