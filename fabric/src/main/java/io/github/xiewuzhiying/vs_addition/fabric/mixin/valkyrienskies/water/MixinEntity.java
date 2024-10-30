package io.github.xiewuzhiying.vs_addition.fabric.mixin.valkyrienskies.water;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocket;
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient;
import io.github.xiewuzhiying.vs_addition.util.ShipUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc;
import org.valkyrienskies.core.apigame.collision.EntityPolygonCollider;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    private Level level;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getZ();

    @Shadow public abstract Level level();

    @Shadow public abstract double getEyeY();

    @Shadow public abstract void setSwimming(boolean swimming);

    @Shadow @Final private Set<TagKey<Fluid>> fluidOnEyes;
    @Unique
    private static EntityPolygonCollider collider = null;
    @Unique
    private boolean isTouchFakeAirPocket = false;
    @Unique
    private boolean isEyeInFakeAirPocket = false;

    @Inject(
            method = "baseTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;updateInWaterStateAndDoFluidPushing()Z"
            )
    )
    private void baseTick(CallbackInfo ci) {
        if (VSAdditionConfig.COMMON.getExperimental().getFakeAirPocket() && this.level() != null) {
            this.isTouchFakeAirPocket = false;
            this.isEyeInFakeAirPocket = false;
            final Iterable<LoadedShip> ships = ShipUtils.getLoadedShipsIntersecting(this.level, this.getBoundingBox());
            final Iterator<LoadedShip> iterator = ships.iterator();
            final Map<Long, List<AABBdc>> map = this.level().isClientSide() ? FakeAirPocketClient.INSTANCE.getMap() : FakeAirPocket.INSTANCE.getMap();
            while (iterator.hasNext()) {
                final Ship ship = iterator.next();
                final long shipId = ship.getId();
                ConvexPolygonc entityPolyInShipCoordinates = null;
                final Iterable<AABBdc> aabbs = map.get(shipId);
                if (aabbs != null) {
                    for (AABBdc aabb : map.get(shipId)) {
                        boolean contain = false;
                        if (entityPolyInShipCoordinates == null) {
                            entityPolyInShipCoordinates = getCollider().createPolygonFromAABB(VectorConversionsMCKt.toJOML(this.getBoundingBox()), ship.getWorldToShip(), shipId);
                        }
                        final Iterable<Vector3dc> points = entityPolyInShipCoordinates.getPoints();
                        for (Vector3dc vector3dc : points) {
                            if (!aabb.containsPoint(vector3dc)) {
                                contain = true;
                            }
                        }
                        this.isTouchFakeAirPocket = !contain;
                        this.isEyeInFakeAirPocket = aabb.containsPoint(ship.getWorldToShip().transformPosition(new Vector3d(this.getX(), this.getEyeY() - 0.1111111119389534, this.getZ())));
                        if (this.isTouchFakeAirPocket && this.isEyeInFakeAirPocket) {
                            return;
                        }
                    }
                }
            }
        }
    }

    @WrapMethod(
            method = "updateFluidHeightAndDoFluidPushing"
    )
    private boolean onUpdateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluidTag, double motionScale, Operation<Boolean> original) {
        if (this.isTouchFakeAirPocket) {
            return false;
        }
        return original.call(fluidTag, motionScale);
    }

    @WrapMethod(
            method = "updateFluidOnEyes"
    )
    private void onUpdateFluidOnEyes(Operation<Void> original) {
        if (this.isEyeInFakeAirPocket) {
            this.fluidOnEyes.clear();
            return;
        }
        original.call();
    }

    @WrapMethod(
            method = "updateSwimming"
    )
    private void onUpdateSwimming(Operation<Void> original) {
        if (this.isTouchFakeAirPocket || this.isEyeInFakeAirPocket) {
            this.setSwimming(false);
            return;
        }
        original.call();
    }

    @Unique
    private static EntityPolygonCollider getCollider() {
        if (collider == null) {
            collider = ValkyrienSkiesMod.vsCore.getEntityPolygonCollider();
        }
        return collider;
    }
}
