package io.github.xiewuzhiying.vs_addition.mixinducks.minecraft;

import net.minecraft.world.phys.Vec3;

public interface HitResultMixinDuck {
    void vs_addition$setOriginPos(Vec3 vec3);
    Vec3 vs_addition$getOriginPos();
    void vs_addition$setLocation(Vec3 vec3);
}
