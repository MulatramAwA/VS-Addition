package io.github.xiewuzhiying.vs_addition.forge.mixin.tallyho;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "edn.stratodonut.tallyho.TallyhoMod")
public abstract class MixinTallyhoMod {
    @ModifyExpressionValue(
            method = "onCommonSetup",
            at = @At(
                    value = "CONSTANT",
                    args = "intValue==0"
            ),
            remap = false
    )
    private int onCommonSetup(int value, @Local(ordinal = 0) int var4) {
        return var4;
    }
}
