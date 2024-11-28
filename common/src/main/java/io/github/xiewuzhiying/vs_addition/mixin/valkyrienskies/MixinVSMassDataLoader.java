package io.github.xiewuzhiying.vs_addition.mixin.valkyrienskies;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.config.MassDatapackResolver;

@Pseudo
@Mixin(MassDatapackResolver.VSMassDataLoader.class)
public abstract class MixinVSMassDataLoader {
    @Inject(
            method = "_init_$lambda$4",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void _init_$lambda$4(CallbackInfo ci) {
        ci.cancel();
    }
}
