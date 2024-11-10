package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import io.github.xiewuzhiying.vs_addition.context.airpocket.FakeAirPocketClient;
import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

@Pseudo
@Mixin(Camera.class)
public abstract class MixinCamera {
    @Shadow private Vec3 position;

    @WrapMethod(
            method = "getFluidInCamera"
    )
    private FogType inclFakeAirPocket(Operation<FogType> original) {
        if (VSAdditionConfig.COMMON.getExperimental().getFakeAirPocket() && VSAdditionConfig.CLIENT.getExperimental().getRemoveFogInFakeAirPocket()) {
            if (FakeAirPocketClient.INSTANCE.checkIfPointInAirPocket(VectorConversionsMCKt.toJOML(this.position))) {
                return FogType.NONE;
            }
        }
        return original.call();
    }
}
