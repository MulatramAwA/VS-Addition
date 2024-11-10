package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.xiewuzhiying.vs_addition.context.EntityShipCollisionDisabler;
import it.unimi.dsi.fastutil.longs.LongSet;
import kotlin.Pair;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.util.EntityShipCollisionUtils;

import java.util.Iterator;

@Pseudo
@Mixin(EntityShipCollisionUtils.class)
public abstract class MixinEntityShipCollisionUtils<ShipType extends Ship> {
    @ModifyExpressionValue(
            method = "getShipPolygonsCollidingWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/core/api/ships/QueryableShipData;getIntersecting(Lorg/joml/primitives/AABBdc;)Ljava/lang/Iterable;",
                    remap = false
            )
    )
    private Iterable<ShipType> getShipPolygonsCollidingWithEntity(Iterable<ShipType> original, @Local(argsOnly = true) Entity entity) {
        if (entity instanceof EntityShipCollisionDisabler disabler) {
            final Iterator<ShipType> iterator = original.iterator();
            final LongSet exclusions = disabler.getDisabledCollisionBodies();
            while (iterator.hasNext()) {
                final Ship ship = iterator.next();
                if (exclusions.contains(ship.getId())) {
                    iterator.remove();
                }
            }
        }
        return original;
    }

    @ModifyExpressionValue(
            method = "adjustEntityMovementForShipCollisions",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/valkyrienskies/core/apigame/collision/EntityPolygonCollider;adjustEntityMovementForPolygonCollisions(Lorg/joml/Vector3dc;Lorg/joml/primitives/AABBdc;DLjava/util/List;)Lkotlin/Pair;"
            ),
            remap = false
    )
    private Pair<Vector3dc, Long> setOnGround(Pair<Vector3dc, Long> original, @Local(argsOnly = true) Entity entity) {
        if(original.component2() != null && entity != null)
            entity.setOnGround(true);
        return original;
    }
}
