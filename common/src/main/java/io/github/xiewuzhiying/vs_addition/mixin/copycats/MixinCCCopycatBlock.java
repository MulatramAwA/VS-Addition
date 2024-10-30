package io.github.xiewuzhiying.vs_addition.mixin.copycats;

import com.copycatsplus.copycats.foundation.copycat.CCCopycatBlock;
import com.copycatsplus.copycats.foundation.copycat.CCCopycatBlockEntity;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.block.IBE;
import io.github.xiewuzhiying.vs_addition.compats.create.content.decoration.copycat.CopycatMassHandler;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.copycat.CopycatBlockEntityMixinDuck;
import io.github.xiewuzhiying.vs_addition.stuff.conditiontester.CopycatConditionTester;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Restriction(
        require = @Condition(type = Condition.Type.TESTER, tester = CopycatConditionTester.class)
)
@Mixin(CCCopycatBlock.class)
public abstract class MixinCCCopycatBlock extends Block implements IBE<CCCopycatBlockEntity> {
    public MixinCCCopycatBlock(Properties properties) {
        super(properties);
    }

    @Inject(
            method = "onRemove",
            at = @At("HEAD")
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
