package io.github.xiewuzhiying.vs_addition.forge.mixin.tallyho;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "edn.stratodonut.tallyho.TallyhoMod")
public abstract class MixinTallyhoMod {
    @ModifyVariable(
            method = "onCommonSetup",
            at = @At("STORE"),
            ordinal = 0,
            remap = false
    )
    private int onCommonSetup(int value) {
        return 0;
    }
}
