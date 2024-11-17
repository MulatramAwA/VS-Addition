package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import io.github.xiewuzhiying.vs_addition.mixinducks.minecraft.HitResultMixinDuck;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;

@Mixin(HitResult.class)
public abstract class MixinHitResult implements HitResultMixinDuck {
    @Shadow @Final @Mutable
    protected Vec3 location;

    @Unique
    private Vec3 vs_addition$originPos = null;

    @Override
    public Vec3 vs_addition$getOriginPos() {
        return this.vs_addition$originPos;
    }

    @Override
    public void vs_addition$setOriginPos(Vec3 pos) {
        this.vs_addition$originPos = pos;
    }

    @Override
    public void vs_addition$setLocation(Vec3 pos) {
        this.location = pos;
    }
}
