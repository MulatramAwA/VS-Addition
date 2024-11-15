package io.github.xiewuzhiying.vs_addition.fabric.mixin.tacz;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.util.block.ProjectileExplosion;
import io.github.xiewuzhiying.vs_addition.util.ShipUtilsKt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.List;

@Mixin(ProjectileExplosion.class)
public abstract class MixinProjectileExplosion{

    @Mutable
    @Shadow(remap = false) @Final private double x;

    @Mutable
    @Shadow(remap = false) @Final private double y;

    @Mutable
    @Shadow(remap = false) @Final private double z;

    @Shadow(remap = false) @Final private float radius;

    @Shadow(remap = false) @Final private Level world;

    @Unique
    private boolean noRayTrace = false;

    @WrapMethod(
            method = "explode()V"
    )
    private void explode(Operation<Void> original) {
        ShipUtilsKt.explosionWrapper(
                this.world,
                () -> { original.call(); return null; },
                this::getOriginalPos,
                (Vector3dc vector3dc) -> { this.setPos(vector3dc); return null;},
                this.radius,
                (Boolean bl) -> { this.setNoRayTrace(bl); return null; },
                (Level level, Vector3dc pos, Double radius) -> { ShipUtilsKt.doExplodeForce(level, pos, radius); return null; }
        );
    }

    @WrapOperation(method = "explode()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"
            )
    )
    private List<Entity> noRayTrace(final Level instance, final Entity entity, final AABB aabb,
                                    final Operation<List<Entity>> getEntities) {
        if (this.noRayTrace) {
            return Collections.emptyList();
        } else {
            return getEntities.call(instance, entity, aabb);
        }
    }

    @Unique
    private Vector3d getOriginalPos() {
        return new Vector3d(this.x, this.y, this.z);
    }

    @Unique
    private void setPos(Vector3dc vector) {
        this.x = vector.x();
        this.y = vector.y();
        this.z = vector.z();
    }

    @Unique
    private void setNoRayTrace(boolean bl) {
        this.noRayTrace = bl;
    }
}
