package io.github.xiewuzhiying.vs_addition.mixin.create.copycat;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.decoration.copycat.CopycatBlock;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import io.github.xiewuzhiying.vs_addition.compats.create.content.decoration.copycat.CopycatMassHandler;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.copycat.CopycatBlockEntityMixinDuck;
import io.github.xiewuzhiying.vs_addition.stuff.conditiontester.CopycatConditionTester;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Restriction(
        require = @Condition(type = Condition.Type.TESTER, tester = CopycatConditionTester.class)
)
@Mixin(CopycatBlock.class)
public abstract class MixinCopycatBlock implements IBE<CopycatBlockEntity> {
    @Inject(
            method = "onRemove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void onRemove(CallbackInfo ci, @Local(argsOnly = true) BlockPos pPos, @Local(argsOnly = true) Level pLevel) {
        this.withBlockEntityDo(pLevel, pPos, ufte -> {
            if (ufte instanceof CopycatBlockEntityMixinDuck duck) {
                final CopycatMassHandler handler = duck.vs_addition$getCopycatMassHandler();
                if (handler != null) {
                    handler.onRemove();
                }
            }
        });
    }
}
