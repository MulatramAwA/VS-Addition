package io.github.xiewuzhiying.vs_addition.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;

import java.util.List;

public class VSAdditionCommonMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if (mixinClassName.contains("org.valkyrienskies.create_interactive.mixin.deployer")) {
            return VSAdditionConfig.COMMON.getCreate().getInsteadCreateInteractiveDeployer();
        }
        if (mixinClassName.contains("org.valkyrienskies.mod.mixin.feature.explosions.MixinExplosion")) {
            return VSAdditionConfig.SERVER.getExperimental().getExplosion();
        }
        if (mixinsShouldCancel.contains(mixinClassName)) {
            return true;
        }
        return false;
    }

    public static List<String> mixinsShouldCancel = List.of(
            "org.valkyrienskies.mod.mixin.feature.transform_particles.MixinLevelRenderer",
            "org.valkyrienskies.mod.mixin.mod_compat.create_big_cannons.MixinCBCUtils"
    );
}
