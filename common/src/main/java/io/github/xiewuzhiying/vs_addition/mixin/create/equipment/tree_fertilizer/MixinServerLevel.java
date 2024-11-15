package io.github.xiewuzhiying.vs_addition.mixin.create.equipment.tree_fertilizer;

import com.bawnorton.mixinsquared.TargetHandler;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Restriction(
        require = @Condition("create")
)
@Mixin(value = ServerLevel.class, priority = 1500)
public abstract class MixinServerLevel {
    @TargetHandler(
            mixin = "org.valkyrienskies.mod.mixin.server.world.MixinServerLevel",
            name = "onInit"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void filter(final CallbackInfo originalCi, final CallbackInfo ci) {
        if (this instanceof TreesDreamWorldAccssor) {
            ci.cancel();
        }
    }
}
