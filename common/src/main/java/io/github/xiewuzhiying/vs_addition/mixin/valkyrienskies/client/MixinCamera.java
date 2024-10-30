package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient;
import io.github.xiewuzhiying.vs_addition.util.ShipUtils;
import net.minecraft.client.Camera;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.primitives.AABBdc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.core.api.ships.LoadedShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow private BlockGetter level;

    @Shadow private Vec3 position;

    @WrapMethod(
            method = "getFluidInCamera"
    )
    private FogType inclFakeAirPocket(Operation<FogType> original) {
        if (VSAdditionConfig.COMMON.getExperimental().getFakeAirPocket()) {
            final Vec3 position = this.position;
            final Iterable<LoadedShip> ships = ShipUtils.getLoadedShipsIntersecting((Level) this.level, new AABB(position, new Vec3(position.x + 1, position.y + 1, position.z + 1)));
            final Iterator<LoadedShip> iterator = ships.iterator();
            final Map<Long, List<AABBdc>> map = FakeAirPocketClient.INSTANCE.getMap();
            while (iterator.hasNext()) {
                Ship ship = iterator.next();
                long shipId = ship.getId();
                final Iterable<AABBdc> aabbs = map.get(shipId);
                if (aabbs != null) {
                    for (AABBdc aabb : aabbs) {
                        if (aabb.containsPoint(ship.getWorldToShip().transformPosition(VectorConversionsMCKt.toJOML(this.position)))) {
                            return FogType.NONE;
                        }
                    }
                }
            }
        }
        return original.call();
    }
}
