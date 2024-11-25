package io.github.xiewuzhiying.vs_addition.mixin.minecraft;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.function.BiFunction;

@Mixin(value = BlockCollisions.class, priority = 1200)
public abstract class MixinBlockCollisions<T> {

    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.world.level.MixinBlockCollisions",
            name = "postInit"
    )
    @WrapMethod(
            method = "@MixinSquared:Handler",
            remap = false
    )
    private void postInit(CollisionGetter collisionGetter, Entity entity, AABB aabb, boolean bl, BiFunction<BlockPos.MutableBlockPos, VoxelShape, T> biFunction, CallbackInfo originalCi, Operation<Void> original) {
        if (entity != null) {
            final Level level = entity.level();
            final Ship ship0 = VSGameUtilsKt.getShipManagingPos(level, aabb.minX, aabb.minY, aabb.minZ);
            final Ship ship1 = VSGameUtilsKt.getShipManagingPos(level, aabb.maxX, aabb.maxY, aabb.maxZ);
            if (ship0 != ship1) {
                if (ship0 != null) {
                    final Vector3dc newPos = ship0.getTransform().getShipToWorld().transformPosition(aabb.minX, aabb.minY, aabb.minZ, new Vector3d());
                    aabb.setMinX(newPos.x());
                    aabb.setMinY(newPos.y());
                    aabb.setMinZ(newPos.z());
                }
                if (ship1 != null) {
                    final Vector3dc newPos = ship1.getTransform().getShipToWorld().transformPosition(aabb.maxX, aabb.maxY, aabb.maxZ, new Vector3d());
                    aabb.setMaxX(newPos.x());
                    aabb.setMaxY(newPos.y());
                    aabb.setMaxZ(newPos.z());
                }
            }
        }
        original.call(collisionGetter, entity, aabb, bl, biFunction, originalCi);
    }

}
