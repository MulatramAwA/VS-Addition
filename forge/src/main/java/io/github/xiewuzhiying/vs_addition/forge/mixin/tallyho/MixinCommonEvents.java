package io.github.xiewuzhiying.vs_addition.forge.mixin.tallyho;

import dev.architectury.platform.Platform;
import edn.stratodonut.tallyho.CommonEvents;
import net.minecraftforge.event.level.ExplosionEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(CommonEvents.class)
public abstract class MixinCommonEvents {
    @Inject(
            method = "onExplosion",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void cancel(ExplosionEvent.Detonate event, CallbackInfo ci) {
        if (!Platform.isModLoaded("createdieselgenerators")) {
            ci.cancel();
        }
    }
}
