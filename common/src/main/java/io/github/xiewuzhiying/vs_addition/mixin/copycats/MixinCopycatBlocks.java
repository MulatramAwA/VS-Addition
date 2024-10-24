package io.github.xiewuzhiying.vs_addition.mixin.copycats;

import com.copycatsplus.copycats.foundation.copycat.multistate.MultiStateCopycatBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import io.github.xiewuzhiying.vs_addition.compats.create.content.decoration.copycat.CopycatMassHandler;
import io.github.xiewuzhiying.vs_addition.mixinducks.create.copycat.CopycatBlockEntityMixinDuck;
import io.github.xiewuzhiying.vs_addition.stuff.CopycatConditionTester;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Restriction(
        require = @Condition(type = Condition.Type.TESTER, tester = CopycatConditionTester.class)
)
@Mixin(targets = {
        "com.copycatsplus.copycats.content.copycat.layer.CopycatLayerBlock",
        "com.copycatsplus.copycats.content.copycat.half_layer.CopycatHalfLayerBlock",
        "com.copycatsplus.copycats.content.copycat.board.CopycatBoardBlock",
        "com.copycatsplus.copycats.content.copycat.slab.CopycatSlabBlock"
})
public abstract class MixinCopycatBlocks {
    @Inject(
            method = "getStateForPlacement",
            at = @At("HEAD")
    )
    private void beforeGetStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        ((IBE<MultiStateCopycatBlockEntity>)this).withBlockEntityDo(context.getLevel(), context.getClickedPos(), ufte -> {
            if (ufte instanceof CopycatBlockEntityMixinDuck duck) {
                final CopycatMassHandler handler = duck.vs_addition$getCopycatMassHandler();
                if (handler != null) {
                    handler.beforeSetMaterial();
                }
            }
        });
    }

    @Inject(
            method = "getStateForPlacement",
            at = @At("RETURN")
    )
    private void afterGetStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        ((IBE<MultiStateCopycatBlockEntity>)this).withBlockEntityDo(context.getLevel(), context.getClickedPos(), ufte -> {
            if (ufte instanceof CopycatBlockEntityMixinDuck duck) {
                final CopycatMassHandler handler = duck.vs_addition$getCopycatMassHandler();
                if (handler != null) {
                    handler.afterSetMaterial();
                }
            }
        });
    }
}
